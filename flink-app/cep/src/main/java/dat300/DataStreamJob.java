package dat300;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
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

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class DataStreamJob {
    public static void main(String[] args) throws Exception {

        int batchSize = 300;
        long sleepPeriod = 1000000;

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStream<EntryWithTimeStamp> stream = env.addSource(new DataIngestionSource(
                "athena-sshd-processed.log",
                batchSize,
                sleepPeriod,
                1000 * 60)
        ).assignTimestampsAndWatermarks(WatermarkStrategy.<EntryWithTimeStamp>forBoundedOutOfOrderness(Duration.ofSeconds(10))
                .withTimestampAssigner((entry, timestamp) -> entry.getPreTimeStamp()));

        Pattern<EntryWithTimeStamp, ?> pattern = Pattern.<EntryWithTimeStamp>begin("InvalidUser")
                .where(new IterativeCondition<EntryWithTimeStamp>() {
                    @Override
                    public boolean filter(EntryWithTimeStamp currentEvent, Context<EntryWithTimeStamp> ctx) throws Exception {
                        return currentEvent.getLogLine().contains("Invalid user");
                    }
                }).next("RepeatedIP").where(new IterativeCondition<EntryWithTimeStamp>() {
                    @Override
                    public boolean filter(EntryWithTimeStamp currentEvent, Context<EntryWithTimeStamp> ctx) throws Exception {
                        for (EntryWithTimeStamp previousEvent : ctx.getEventsForPattern("InvalidUser")) {
                            if (currentEvent.getLogLine().split(" ")[8].equals(previousEvent.getLogLine().split(" ")[8])) {
                                return true;
                            }
                        }
                        return false;
                    }
                }).within(Time.minutes(10).toDuration());

        PatternStream<EntryWithTimeStamp> patternStream = CEP.pattern(stream, pattern);

        DataStream<EntryWithTimeStamp> patternMatches = patternStream.select(
            new PatternSelectFunction<EntryWithTimeStamp, EntryWithTimeStamp>() {
                @Override
                public EntryWithTimeStamp select(Map<String, List<EntryWithTimeStamp>> pattern) throws Exception {
                   return pattern.get("RepeatedIP").get(0);
                }
            }
        );

        DataStream<EntryWithTimeStamp> exitStamp = patternMatches
        //DataStream<EntryWithTimeStamp> exitStamp = stream
                .map(new MapFunction<EntryWithTimeStamp, EntryWithTimeStamp>() {
                    @Override
                    public EntryWithTimeStamp map(EntryWithTimeStamp entry) throws Exception {
                        entry.setPostTimeStamp(System.nanoTime());
                        //System.out.println(entry);
                        return entry;
                    }
                });

        FileSink<EntryWithTimeStamp> outSink = FileSink
                .forRowFormat( new Path("./outSink"), new SimpleStringEncoder<EntryWithTimeStamp>("UTF-8"))
                .withBucketAssigner(new CustomBucketAssigner(batchSize, sleepPeriod))
                .withRollingPolicy(
                        OnCheckpointRollingPolicy.build()
                ).build();


        exitStamp.sinkTo(outSink);

        env.execute("DataStreamJob");

    }
}
