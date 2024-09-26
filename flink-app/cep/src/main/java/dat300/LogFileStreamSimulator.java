package dat300;

import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.io.BufferedReader;
import java.io.FileReader;

public abstract class LogFileStreamSimulator implements SourceFunction<String> {
    private final String filePath;
    private boolean isRunning = true;

    public LogFileStreamSimulator(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void run(SourceContext<String> ctx) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;

        while (isRunning) {
            while((line = reader.readLine()) != null) {
                ctx.collect(line);
            }
            Thread.sleep(1000);
        }
        reader.close();
    }

    @Override
    public void cancel() {
        isRunning = false;
    }
}
