package dat300;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.connector.file.src.reader.TextLineInputFormat;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.OnCheckpointRollingPolicy;

public class DataStreamJob {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(4);
        // Load a file
//        FileSource<String> source =
//            FileSource.forRecordStreamFormat(new TextLineInputFormat(), new Path("athena-sshd-processed.log"))
//            .build();


        DataStream<EntryWithTimeStamp> stream = env.addSource(new DataIngestionSource(
                "athena-sshd-processed.log",
                1000,
                10,
                1000 * 5)
        );

//        DataIngestionSource source = new DataIngestionSource(
//                "athena-sshd-processed.log",
//                1000,
//                10,
//                1000
//        );
//
//        // Add FileSource to our execution environment
//        DataStream<String> stream = env.fromSource(
//            source,
//            WatermarkStrategy.noWatermarks(),
//            "FileSource"
//        );


//DataStream<EntryWithTimeStamp> testStream = env.addSource(new LogFileStreamSimulator("./athena-sshd-processed.log")).name("foo");

//        DataStream<EntryWithTimeStamp> logLinesWithPreStamp = stream
//                .map(new MapFunction<EntryWithTimeStamp, EntryWithTimeStamp>() {
//                    @Override
//                    int foo = 1;
//                    public EntryWithTimeStamp map(EntryWithTimeStamp logLine) throws Exception {
//                        long preTimeStamp = System.nanoTime();
//                        EntryWithTimeStamp entry = new EntryWithTimeStamp(logLine,preTimeStamp);
//                        foo += 1;
//                        return entry;
//                    }
//                });

        DataStream<EntryWithTimeStamp> exitStamp = stream
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

        //DataStream<String> input = env.readTextFile("./outSink");
        //input.writeAsText("./outSink/out.csv", FileSystem.WriteMode.OVERWRITE);

        // Execute program, beginning computation.
        env.execute("DataStreamJob");

    }
}
