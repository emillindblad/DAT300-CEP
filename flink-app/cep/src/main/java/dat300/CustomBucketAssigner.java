package dat300;

import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.streaming.api.functions.sink.filesystem.BucketAssigner;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.SimpleVersionedStringSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomBucketAssigner implements BucketAssigner<EntryWithTimeStamp, String> {


    private final int batchSize;
    private final long sleepPeriod;
    private final int parallelismLevel;
    private final String dateTime;
    private final int bufferLimit;
    private final String prefix;


    public CustomBucketAssigner(String prefix, int batchSize, long sleepPeriod, int parallelismLevel, int bufferLimit, String dateTime) {
        this.batchSize = batchSize;
        this.sleepPeriod = sleepPeriod;
        this.parallelismLevel = parallelismLevel;
        this.dateTime = dateTime;
        this.bufferLimit = bufferLimit;
        this.prefix = prefix;
    }
    @Override
    public String getBucketId(EntryWithTimeStamp element, Context context) {
        return dateTime + "-b" + batchSize + "-s" + sleepPeriod + "-p" + parallelismLevel + "-bL" + bufferLimit;
    }

    @Override
    public SimpleVersionedSerializer<String> getSerializer() {
        return SimpleVersionedStringSerializer.INSTANCE;
    }
}
