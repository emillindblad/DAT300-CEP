/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dat300;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.connector.file.src.reader.TextLineInputFormat;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;


/**
 * Skeleton for a Flink DataStream Job.
 *
 * <p>For a tutorial how to write a Flink application, check the
 * tutorials and examples on the <a href="https://flink.apache.org">Flink Website</a>.
 *
 * <p>To package your application into a JAR file for execution, run
 * 'mvn clean package' on the command line.
 *
 * <p>If you change the name of the main class (with the public static void main(String[] args))
 * method, change the respective entry in the POM.xml file (simply search for 'mainClass').
 */
public class DataStreamJob {

    public static void main(String[] args) throws Exception {
        // Sets up the execution environment, which is the main entry point
        // to building Flink applications.
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Load a file
        FileSource<String> source =
            FileSource.forRecordStreamFormat(new TextLineInputFormat(), new Path("athena-sshd-processed.log"))
                .build();

        // Add FileSource to our execution environment
        DataStream<String> stream = env.fromSource(
            source,
            WatermarkStrategy.noWatermarks(),
            "FileSource"
        );


//        DataStream<EntryWithTimeStamp> testStream = env.addSource(new LogFileStreamSimulator("./athena-sshd-processed.log")).name("foo");

        DataStream<EntryWithTimeStamp> logLinesWithPreStamp = stream
            .map(new MapFunction<String, EntryWithTimeStamp>() {
//                @Override
                int foo = 1;
                public EntryWithTimeStamp map(String logLine) throws Exception {
                    long preTimeStamp = System.nanoTime();
                    EntryWithTimeStamp entry = new EntryWithTimeStamp(foo,preTimeStamp);
                    foo += 1;
                    return entry;
                }
            });

        DataStream<EntryWithTimeStamp> exitStamp = logLinesWithPreStamp
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
                    DefaultRollingPolicy.builder().build()
                )
                .build();

//        stream.sinkTo(outSink);

        exitStamp.sinkTo(outSink);


        // Execute program, beginning computation.
        env.execute("DataStreamJob");

    }
}
