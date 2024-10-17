#!/bin/sh

args_cep=(
    "200 1000000 1 1024"
    "200 1000000 2 1024"
    "200 1000000 4 1024"
    "200 1000000 8 1024"
    "400 1000000 1 1024"
    "400 1000000 2 1024"
    "400 1000000 4 1024"
    "400 1000000 8 1024"
    "600 1000000 1 1024"
    "600 1000000 2 1024"
    "600 1000000 4 1024"
    "600 1000000 8 1024"
    "800 1000000 1 1024"
    "800 1000000 2 1024"
    "800 1000000 4 1024"
    "800 1000000 8 1024"
    "1000 1000000 1 1024"
    "1000 1000000 2 1024"
    "1000 1000000 4 1024"
    "1000 1000000 8 1024"
    "1200 1000000 1 1024"
    "1200 1000000 2 1024"
    "1200 1000000 4 1024"
    "1200 1000000 8 1024"
    "1400 1000000 1 1024"
    "1400 1000000 2 1024"
    "1400 1000000 4 1024"
    "1400 1000000 8 1024"
)

args_simple=(
    "200 1000000 1 1024"
    "200 1000000 2 1024"
    "200 1000000 4 1024"
    "200 1000000 8 1024"
    "400 1000000 1 1024"
    "400 1000000 2 1024"
    "400 1000000 4 1024"
    "400 1000000 8 1024"
    "600 1000000 1 1024"
    "600 1000000 2 1024"
    "600 1000000 4 1024"
    "600 1000000 8 1024"
    "800 1000000 1 1024"
    "800 1000000 2 1024"
    "800 1000000 4 1024"
    "800 1000000 8 1024"
    "1000 1000000 1 1024"
    "1000 1000000 2 1024"
    "1000 1000000 4 1024"
    "1000 1000000 8 1024"
    "1200 1000000 1 1024"
    "1200 1000000 2 1024"
    "1200 1000000 4 1024"
    "1200 1000000 8 1024"
    "1400 1000000 1 1024"
    "1400 1000000 2 1024"
    "1400 1000000 4 1024"
    "1400 1000000 8 1024"
    "200 1000000 4 1024"
    "200 1000000 4 2048"
    "200 1000000 4 4096"
    "200 1000000 4 6144"
    "400 1000000 4 1024"
    "400 1000000 4 2048"
    "400 1000000 4 4096"
    "400 1000000 4 6144"
    "600 1000000 4 1024"
    "600 1000000 4 2048"
    "600 1000000 4 4096"
    "600 1000000 4 6144"
    "800 1000000 4 1024"
    "800 1000000 4 2048"
    "800 1000000 4 4096"
    "800 1000000 4 6144"
    "1000 1000000 4 1024"
    "1000 1000000 4 2048"
    "1000 1000000 4 4096"
    "1000 1000000 4 6144"
    "1200 1000000 4 1024"
    "1200 1000000 4 2048"
    "1200 1000000 4 4096"
    "1200 1000000 4 6144"
    "1400 1000000 4 1024"
    "1400 1000000 4 2048"
    "1400 1000000 4 4096"
    "1400 1000000 4 6144"
)

args_test_timeout=(
    "1000 1000000 4 1024 25"
    "1000 1000000 4 1024 50"
    "1000 1000000 4 1024 75"
    "1000 1000000 4 1024 100"
)



script_dir=$(pwd)
go_cmd="go run . ../outSink/"
flink="/usr/lib/jvm/java-11-openjdk/bin/java -Xms10g -Xmx10g -javaagent:/usr/share/idea/lib/idea_rt.jar=40495:/usr/share/idea/bin -Dfile.encoding=UTF-8 -classpath /home/emil/github/edu/DAT300-CEP/flink-app/cep/target/classes:/home/emil/.m2/repository/org/apache/flink/flink-streaming-java/1.20.0/flink-streaming-java-1.20.0.jar:/home/emil/.m2/repository/org/apache/flink/flink-core/1.20.0/flink-core-1.20.0.jar:/home/emil/.m2/repository/org/apache/flink/flink-core-api/1.20.0/flink-core-api-1.20.0.jar:/home/emil/.m2/repository/org/apache/flink/flink-metrics-core/1.20.0/flink-metrics-core-1.20.0.jar:/home/emil/.m2/repository/org/apache/flink/flink-annotations/1.20.0/flink-annotations-1.20.0.jar:/home/emil/.m2/repository/org/apache/flink/flink-shaded-asm-9/9.5-17.0/flink-shaded-asm-9-9.5-17.0.jar:/home/emil/.m2/repository/org/apache/flink/flink-shaded-jackson/2.14.2-17.0/flink-shaded-jackson-2.14.2-17.0.jar:/home/emil/.m2/repository/org/apache/commons/commons-lang3/3.12.0/commons-lang3-3.12.0.jar:/home/emil/.m2/repository/org/snakeyaml/snakeyaml-engine/2.6/snakeyaml-engine-2.6.jar:/home/emil/.m2/repository/org/apache/commons/commons-text/1.10.0/commons-text-1.10.0.jar:/home/emil/.m2/repository/com/esotericsoftware/kryo/kryo/2.24.0/kryo-2.24.0.jar:/home/emil/.m2/repository/com/esotericsoftware/minlog/minlog/1.2/minlog-1.2.jar:/home/emil/.m2/repository/org/objenesis/objenesis/2.1/objenesis-2.1.jar:/home/emil/.m2/repository/commons-collections/commons-collections/3.2.2/commons-collections-3.2.2.jar:/home/emil/.m2/repository/org/apache/commons/commons-compress/1.26.0/commons-compress-1.26.0.jar:/home/emil/.m2/repository/org/apache/flink/flink-file-sink-common/1.20.0/flink-file-sink-common-1.20.0.jar:/home/emil/.m2/repository/org/apache/flink/flink-runtime/1.20.0/flink-runtime-1.20.0.jar:/home/emil/.m2/repository/org/apache/flink/flink-rpc-core/1.20.0/flink-rpc-core-1.20.0.jar:/home/emil/.m2/repository/org/apache/flink/flink-rpc-akka-loader/1.20.0/flink-rpc-akka-loader-1.20.0.jar:/home/emil/.m2/repository/org/apache/flink/flink-queryable-state-client-java/1.20.0/flink-queryable-state-client-java-1.20.0.jar:/home/emil/.m2/repository/org/apache/flink/flink-hadoop-fs/1.20.0/flink-hadoop-fs-1.20.0.jar:/home/emil/.m2/repository/commons-io/commons-io/2.15.1/commons-io-2.15.1.jar:/home/emil/.m2/repository/org/apache/flink/flink-shaded-netty/4.1.91.Final-17.0/flink-shaded-netty-4.1.91.Final-17.0.jar:/home/emil/.m2/repository/org/apache/flink/flink-shaded-zookeeper-3/3.7.1-17.0/flink-shaded-zookeeper-3-3.7.1-17.0.jar:/home/emil/.m2/repository/org/javassist/javassist/3.24.0-GA/javassist-3.24.0-GA.jar:/home/emil/.m2/repository/org/xerial/snappy/snappy-java/1.1.10.4/snappy-java-1.1.10.4.jar:/home/emil/.m2/repository/tools/profiler/async-profiler/2.9/async-profiler-2.9.jar:/home/emil/.m2/repository/org/lz4/lz4-java/1.8.0/lz4-java-1.8.0.jar:/home/emil/.m2/repository/org/apache/flink/flink-java/1.20.0/flink-java-1.20.0.jar:/home/emil/.m2/repository/com/twitter/chill-java/0.7.6/chill-java-0.7.6.jar:/home/emil/.m2/repository/org/apache/flink/flink-shaded-guava/31.1-jre-17.0/flink-shaded-guava-31.1-jre-17.0.jar:/home/emil/.m2/repository/org/apache/commons/commons-math3/3.6.1/commons-math3-3.6.1.jar:/home/emil/.m2/repository/org/apache/flink/flink-connector-datagen/1.20.0/flink-connector-datagen-1.20.0.jar:/home/emil/.m2/repository/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar:/home/emil/.m2/repository/com/google/code/findbugs/jsr305/1.3.9/jsr305-1.3.9.jar:/home/emil/.m2/repository/org/apache/flink/flink-clients/1.20.0/flink-clients-1.20.0.jar:/home/emil/.m2/repository/org/apache/flink/flink-optimizer/1.20.0/flink-optimizer-1.20.0.jar:/home/emil/.m2/repository/commons-cli/commons-cli/1.5.0/commons-cli-1.5.0.jar:/home/emil/.m2/repository/org/apache/flink/flink-datastream/1.20.0/flink-datastream-1.20.0.jar:/home/emil/.m2/repository/org/apache/flink/flink-datastream-api/1.20.0/flink-datastream-api-1.20.0.jar:/home/emil/.m2/repository/org/apache/flink/flink-cep/1.20.0/flink-cep-1.20.0.jar:/home/emil/.m2/repository/org/apache/flink/flink-connector-files/1.20.0/flink-connector-files-1.20.0.jar:/home/emil/.m2/repository/org/apache/logging/log4j/log4j-slf4j-impl/2.17.1/log4j-slf4j-impl-2.17.1.jar:/home/emil/.m2/repository/org/apache/logging/log4j/log4j-api/2.17.1/log4j-api-2.17.1.jar:/home/emil/.m2/repository/org/apache/logging/log4j/log4j-core/2.17.1/log4j-core-2.17.1.jar dat300.DataStreamJob"

for arg_set in "${args_test_timeout[@]}"; do
    echo "Started $arg_set"
    program="$flink $arg_set"
    bucket=$($program | tail -n 1)

    echo "Done with $arg_set"
    echo "Creating plot"

    cd "${script_dir}/plots"
    $go_cmd$bucket
    echo "Done with $arg_set"
    cd $script_dir
done
# shutdown
