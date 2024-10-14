package dat300;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.OnCheckpointRollingPolicy;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DataStreamJob {
    public static void main(String[] args) throws Exception {

        int batchSize = 300;
        long sleepPeriod = 1000000; //Nanoseconds
        int parallelismLevel = 2;
        int bufferLimit = 1024; // For example, 1024 KB (1 MB)

        Configuration configuration = new Configuration();
        // Set the buffer size (convert KB to bytes)
        configuration.setLong("taskmanager.memory.network.size", bufferLimit * 1024); // Network buffer size in bytes
        configuration.setLong("taskmanager.memory.task.size", bufferLimit * 1024);

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(configuration);
        env.setParallelism(1);

        DataStream<EntryWithTimeStamp> stream = env.addSource(new DataIngestionSource(
                "athena-sshd-processed.log",
                batchSize,
                sleepPeriod,
                1000 * 200)
        ).assignTimestampsAndWatermarks(WatermarkStrategy.<EntryWithTimeStamp>forBoundedOutOfOrderness(Duration.ofSeconds(10))
                .withTimestampAssigner((entry, timestamp) -> entry.logLine.getUnixTimeStamp()));

        Pattern<EntryWithTimeStamp, ?> pattern = Pattern.<EntryWithTimeStamp>begin("InvalidUser")
                .where(new IterativeCondition<EntryWithTimeStamp>() {
                    @Override
                    public boolean filter(EntryWithTimeStamp currentEvent, Context<EntryWithTimeStamp> ctx) throws Exception {
                        if (currentEvent.getLogLine().message.contains("Invalid user")) {
                            System.out.println(currentEvent.sequentialId + " " + currentEvent.getLogLine().message);
                            System.out.println("----");
                            return true;
                        }
                        return false;
                    }
                }).followedBy("RepeatedIP").where(new IterativeCondition<EntryWithTimeStamp>() {
                    @Override
                    public boolean filter(EntryWithTimeStamp currentEvent, Context<EntryWithTimeStamp> ctx) throws Exception {
                        for (EntryWithTimeStamp previousEvent : ctx.getEventsForPattern("InvalidUser")) {
                           if (currentEvent.getLogLine().message.split(" ")[4].equals(previousEvent.getLogLine().message.split(" ")[4])) {
                               System.out.println(currentEvent.getLogLine().timeStamp + " " + currentEvent.getLogLine().message);
                               System.out.println(previousEvent.getLogLine().timeStamp + " " + previousEvent.getLogLine().message);
                               System.out.println("-");
                                return true;
                            }
                        }
                        return false;
                    }
                }).within(Duration.ofMinutes(2));

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
                .withBucketAssigner(new CustomBucketAssigner(batchSize, sleepPeriod, parallelismLevel, bufferLimit, GetDateTime()))
                .withRollingPolicy(
                        OnCheckpointRollingPolicy.build()
                ).build();

        exitStamp.sinkTo(outSink);

        env.execute("DataStreamJob");
    }

    public static String GetDateTime(){
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH-mm-ss");
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(dateFormatter);
        String time = now.format(timeFormatter);
        return date + "_" + time;
    }
}
