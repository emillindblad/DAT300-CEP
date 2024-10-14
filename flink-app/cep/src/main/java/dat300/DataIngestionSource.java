package dat300;

import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.apache.flink.configuration.Configuration;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.ArrayList;

public class DataIngestionSource extends RichSourceFunction<EntryWithTimeStamp> {
    private final String inputFile;
    private final int batchSize;
    private final long sleepPeriod;
    private final long duration;

    private ConcurrentLinkedQueue<EntryWithTimeStamp> internalQueue;
    private boolean internalThreadCompleted = false;

    public DataIngestionSource(String inputFile, int batchSize, long sleepPeriod, long duration) {
        this.inputFile = inputFile;
        this.batchSize = batchSize;
        this.sleepPeriod = sleepPeriod;
        this.duration = duration;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        System.out.println("Starting file ingestion");
        internalQueue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void close() throws Exception {

    }

    private void fillInternalQueue() throws IOException {
        int id = 0;
        ArrayList<String> internalBuffer = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        String line = reader.readLine();
        while (line != null && internalBuffer.size() < 10000) {
            internalBuffer.add(line);
            line = reader.readLine();
        }
        reader.close();

        long startTime = System.currentTimeMillis();
        int internalBufferIdx = 0;

        while (System.currentTimeMillis() - startTime <= duration) {
            long beforeBatchTime = System.nanoTime();
            //System.out.println("Adding to queue");
            //System.out.println("In queue before");
            //System.out.println(internalQueue.size());
            while (System.currentTimeMillis() - startTime <= 30000) {
            }

            for (int i = 0; i < batchSize; i++) {
//                String logData = internalBuffer.get(internalBufferIdx);
                LogLine logData = new LogLine(internalBuffer.get(internalBufferIdx));
                //System.out.println(logData);
                long inputTimestamp = System.nanoTime();
                //internalQueue.add(new EntryWithTimeStamp(id, logData, inputTimestamp));
                internalQueue.add(new EntryWithTimeStamp(id, logData, inputTimestamp,internalQueue.size()));

                id++;
                internalBufferIdx = (internalBufferIdx + 1) % internalBuffer.size();
            }
            //System.out.println("In queue at wait");
            //System.out.println(internalQueue.size());
            while (System.nanoTime() < beforeBatchTime + sleepPeriod ) {
            }
        }
        internalThreadCompleted = true;
    }

    @Override
    public void run(SourceContext<EntryWithTimeStamp> ctx) throws Exception {
        System.out.println("Creating thread");
        new Thread(() -> {
            try {
                fillInternalQueue();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        while (!internalThreadCompleted) {
            if (!internalQueue.isEmpty()) {
                ctx.collect(internalQueue.poll());
            }
        }
    }

    @Override
    public void cancel() {}
}

