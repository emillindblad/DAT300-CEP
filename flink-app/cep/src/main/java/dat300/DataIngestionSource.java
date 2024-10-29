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
    public void close() throws Exception {}

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

        //create loglines before loop input loop for optimization
        ArrayList<LogLine> logLineBuffer = new ArrayList<>();
        populateLoglines(logLineBuffer, internalBuffer);

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime <= 10000) {}

        int internalBufferIdx = 0;
        long id = 1;

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
                internalQueue.add(new EntryWithTimeStamp(id, logLineBuffer.get(i), inputTimestamp,internalQueuSize));
                inputTimestamp++; //adding 1 for ordering (part of optimization)
                id++;
            }
            internalBufferIdx = internalBufferIdx+batchSize;
            //System.out.println("Batch completed");
            //System.out.println("InternalBufferIdx is " + internalBufferIdx);
            while (System.nanoTime() < beforeBatchTime + sleepPeriod ) {
            }
        }
        System.out.println("Total events pushed: " + id);
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

    private void populateLoglines(ArrayList<LogLine> logLineBuffer,ArrayList<String> internalBuffer){
        for (int i = 0; i < internalBuffer.size(); i++) {
            LogLine logData = new LogLine(internalBuffer.get(i));
            logLineBuffer.add(logData);
        }
    }

    @Override
    public void cancel() {}
}

