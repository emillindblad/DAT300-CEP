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
        ArrayList<String> internalBuffer = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        String line = reader.readLine();
        while (line != null) {
            internalBuffer.add(line);
            line = reader.readLine();
        }
        reader.close();
        int size = internalBuffer.size() - 1;
        System.out.println("SIZE is " + size);

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime <= 30000) {}

        int internalBufferIdx = 0;
        long totalEvents = 0;

        while (System.currentTimeMillis() - startTime <= duration) {
            long beforeBatchTime = System.nanoTime();
            if (internalBufferIdx + batchSize >= size) {
                internalBufferIdx = 0;
                //System.out.println("Reset internalBufferIdx to " + internalBufferIdx);
            }
            //System.out.println("Starting for-loop with i = " + internalBufferIdx);

            //moved here for optimization, will reduce accuracy but should be good enough
            int internalQueuSize = internalQueue.size();
            long inputTimestamp = System.nanoTime(); //syscall == expensive in time
            int endIdx = internalBufferIdx + batchSize;

            for (int i = internalBufferIdx ; i < endIdx; i++) {
                LogLine logData = new LogLine(internalBuffer.get(i));
                internalQueue.add(new EntryWithTimeStamp(i + internalBufferIdx, logData, inputTimestamp,internalQueuSize));
                inputTimestamp++; //adding 1 for ordering (part of optimization)
                totalEvents++;
            }
            internalBufferIdx = internalBufferIdx+batchSize;
            //System.out.println("Batch completed");
            //System.out.println("InternalBufferIdx is " + internalBufferIdx);
            while (System.nanoTime() < beforeBatchTime + sleepPeriod ) {
            }
        }
        System.out.println("Total events pushed: " + totalEvents);
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

