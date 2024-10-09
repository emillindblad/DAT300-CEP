package dat300;

import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.streaming.api.functions.sink.filesystem.BucketAssigner;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.SimpleVersionedStringSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomBucketAssigner implements BucketAssigner<EntryWithTimeStamp, String> {


    private final int batchSize;
    private final long sleepPeriod;


    public CustomBucketAssigner(int batchSize, long sleepPeriod) {
        this.batchSize = batchSize;
        this.sleepPeriod = sleepPeriod;
    }
    @Override
    public String getBucketId(EntryWithTimeStamp element, Context context) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH-mm-ss");
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(dateFormatter);
        String time = now.format(timeFormatter);

        return date + "-" + time + "-b" + batchSize + "-s" + sleepPeriod;
    }

    @Override
    public SimpleVersionedSerializer<String> getSerializer() {
        return SimpleVersionedStringSerializer.INSTANCE;
    }
}
