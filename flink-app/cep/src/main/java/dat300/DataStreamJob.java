package dat300;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.OnCheckpointRollingPolicy;

import java.util.List;
import java.util.Map;

public class DataStreamJob {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(4);


        DataStream<EntryWithTimeStamp> stream = env.addSource(new DataIngestionSource(
                "athena-sshd-processed.log",
                1000,
                10,
                1000 * 5)
        );

        Pattern<EntryWithTimeStamp, ?> pattern = Pattern.<EntryWithTimeStamp>begin("firstInvalid")
                .where(new IterativeCondition<EntryWithTimeStamp>() {
                    @Override
                    public boolean filter(EntryWithTimeStamp currentEvent, Context<EntryWithTimeStamp> ctx) throws Exception {
                        System.out.println("Checking event");
                        if (!currentEvent.getLogLine().contains("Invalid user")) {
                            System.out.println("False");
                            return false;
                        }
                        System.out.println("True");

                        int attemptCount = 1;
                        for (EntryWithTimeStamp previousEvent : ctx.getEventsForPattern("firstInvalid")) {
                            if (currentEvent.getLogLine().split(" ")[8].equals(previousEvent.getLogLine().split(" ")[8])) {
                                attemptCount++;
                            }
                        }
                        return attemptCount > 1;
                    }
                })
                .within(Time.minutes(1).toDuration());

        PatternStream<EntryWithTimeStamp> patternStream = CEP.pattern(stream, pattern);

        DataStream<EntryWithTimeStamp> patternMatches = patternStream.select(
            new PatternSelectFunction<EntryWithTimeStamp, EntryWithTimeStamp>() {
                @Override
                public EntryWithTimeStamp select(Map<String, List<EntryWithTimeStamp>> pattern) throws Exception {
                   return pattern.get("firstInvalid").get(0);
                }
            }
        );

        DataStream<EntryWithTimeStamp> exitStamp = patternMatches
                .map(new MapFunction<EntryWithTimeStamp, EntryWithTimeStamp>() {
                    @Override
                    public EntryWithTimeStamp map(EntryWithTimeStamp entry) throws Exception {
                        entry.setPostTimeStamp(System.nanoTime());
                        System.out.println(entry);
                        return entry;
                    }
                });

        FileSink<EntryWithTimeStamp> outSink= FileSink
                .forRowFormat( new Path("./outSink"), new SimpleStringEncoder<EntryWithTimeStamp>("UTF-8"))
                .withRollingPolicy(
                        OnCheckpointRollingPolicy.build()
                ).build();


        exitStamp.sinkTo(outSink).setParallelism(1);

        env.execute("DataStreamJob");

    }
}
