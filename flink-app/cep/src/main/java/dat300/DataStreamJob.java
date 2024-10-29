package dat300;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.OnCheckpointRollingPolicy;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DataStreamJob {
    //used to convert from minutes to seconds
    private static Long firstTimestamp = null; // Store the first timestamp
    private static final int SCALE_FACTOR = 60; // Scale factor (1 minute = 1 second)
    private static Long lastTimestamp;
    private static Long currentLoopTimestamp;
    private static Long lastScaled;



    public static void main(String[] args) throws Exception {
        int batchSize;
        int sleepPeriod;
        int parallelismLevel;
        int bufferLimit;
        long bufferTimeOut;

        if (args.length != 0) {
            batchSize = Integer.parseInt(args[0]);
            sleepPeriod = Integer.parseInt(args[1]);
            parallelismLevel = Integer.parseInt(args[2]);
            bufferLimit = Integer.parseInt(args[3]);
            bufferTimeOut = Integer.parseInt(args[4]);
        } else {
            batchSize = 1000;
            sleepPeriod = 1000000; // 1000000 Nanoseconds = 1 ms
            parallelismLevel = 4;
            bufferLimit = 1024; // For example, 1024 KB (1 MB)
            bufferTimeOut = 100; // Milliseconds, default = 100
        }

        System.out.printf(
            "Configuration: Batchsize: %d items, SleepPeriod: %d ns ParallelismLevel: %d threads, BufferLimit %d kb BufferTimeout: %d ms\n",
            batchSize,sleepPeriod,parallelismLevel,bufferLimit,bufferTimeOut
        );
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(parallelismLevel);
        System.out.println(env.getConfig());
        env.setBufferTimeout(bufferTimeOut);

        DataStream<EntryWithTimeStamp> stream = env.addSource(new DataIngestionSource(
            "athena-sshd-processed.log",
            batchSize,
            sleepPeriod,
            1000 * 100)
        ).assignTimestampsAndWatermarks(WatermarkStrategy.<EntryWithTimeStamp>forBoundedOutOfOrderness(Duration.ofSeconds(10))
            .withTimestampAssigner((entry, timestamp) -> scaleTimestamp(entry.logLine.getUnixTimeStamp()))); //source should not be parallel

        KeyedStream<EntryWithTimeStamp,String> keyedStream = stream.keyBy(value -> value.logLine.getIp());

        // WindowFilter

        /*
        Pattern<EntryWithTimeStamp, ?> pattern = Pattern.<EntryWithTimeStamp>begin("InvalidUser")
                .where(new IterativeCondition<EntryWithTimeStamp>() {
                    @Override
                    public boolean filter(EntryWithTimeStamp currentEvent, Context<EntryWithTimeStamp> ctx) throws Exception {
                        if (currentEvent.getLogLine().message.contains("Invalid user")) {
                            //System.out.println(currentEvent.sequentialId + " " + currentEvent.getLogLine().message);
                            //System.out.println("----");
                            return true;
                        }
                        return false;
                    }
                }).followedBy("RepeatedIP").where(new IterativeCondition<EntryWithTimeStamp>() {
                    @Override
                    public boolean filter(EntryWithTimeStamp currentEvent, Context<EntryWithTimeStamp> ctx) throws Exception {
                        if (!currentEvent.getLogLine().message.contains("Invalid user")) {
                            return false;
                        }
                        for (EntryWithTimeStamp previousEvent : ctx.getEventsForPattern("InvalidUser")) {
                            if (currentEvent.getLogLine().message.split(" ")[4].equals(previousEvent.getLogLine().message.split(" ")[4])) {
                                //System.out.println(currentEvent.getLogLine().timeStamp + " " + currentEvent.getLogLine().message);
                                //System.out.println(previousEvent.getLogLine().timeStamp + " " + previousEvent.getLogLine().message);
                                //System.out.println("-");
                                return true;
                            }
                        }
                        return false;
                    }
                }).within(Duration.ofMinutes(2));
         */

        // SimpleFilter
        Pattern<EntryWithTimeStamp, ?> pattern = Pattern.<EntryWithTimeStamp>begin("InvalidUser")
        .where(new IterativeCondition<EntryWithTimeStamp>() {
            @Override
            public boolean filter(EntryWithTimeStamp currentEvent, Context<EntryWithTimeStamp> ctx) throws Exception {
                // Check if the message contains "Invalid user"
                if (currentEvent.getLogLine().message.contains("Invalid user")) {
                    return true; // This event matches the "InvalidUser" pattern
                }
                return false; // This event does not match
            }
        });

        PatternStream<EntryWithTimeStamp> patternStream = CEP.pattern(keyedStream, pattern);

        DataStream<EntryWithTimeStamp> patternMatches = patternStream.select(
            new PatternSelectFunction<EntryWithTimeStamp, EntryWithTimeStamp>() {
                @Override
                public EntryWithTimeStamp select(Map<String, List<EntryWithTimeStamp>> pattern) throws Exception {
                    return pattern.get("InvalidUser").get(0);
                }
            }
        );

        DataStream<EntryWithTimeStamp> exitStamp = patternMatches
        .map(new MapFunction<EntryWithTimeStamp, EntryWithTimeStamp>() {
            @Override
            public EntryWithTimeStamp map(EntryWithTimeStamp entry) throws Exception {
                entry.setPostTimeStamp(System.nanoTime());
                //System.out.println(entry);
                return entry;
            }
        });

        String prefix = "SimpleTest";
        CustomBucketAssigner bucket = new CustomBucketAssigner(prefix,batchSize, sleepPeriod, parallelismLevel, bufferLimit, GetDateTime());
        FileSink<EntryWithTimeStamp> outSink = FileSink
        .forRowFormat(new Path("./outSink"), new SimpleStringEncoder<EntryWithTimeStamp>("UTF-8"))
        .withBucketAssigner(bucket)
        .withRollingPolicy(
            OnCheckpointRollingPolicy.build()
        ).build();

        exitStamp.sinkTo(outSink);

        env.execute("DataStreamJob");
        System.out.println(bucket);
    }
    public static long scaleTimestamp(long timestamp) {
        if (firstTimestamp == null) {
            firstTimestamp = timestamp;
            lastTimestamp = timestamp;
            currentLoopTimestamp = timestamp;
            return timestamp; // The first scaled timestamp is the same as the original
        }
        if(timestamp < lastTimestamp) {
            currentLoopTimestamp = lastScaled;
            //System.out.println("New base:" + currentLoopTimestamp);
        }

        lastTimestamp = timestamp;
        long timeDifference = timestamp - firstTimestamp;

        // Scale the time difference (1 minute = 1 second)
        long scaledDifference = timeDifference / SCALE_FACTOR;

        // Create a new valid timestamp based on the scaled difference
        long newScaledTimestamp = currentLoopTimestamp + scaledDifference;
        lastScaled = newScaledTimestamp;
        //System.out.println("Scaled ts: "+ timestamp);

        return newScaledTimestamp;
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
