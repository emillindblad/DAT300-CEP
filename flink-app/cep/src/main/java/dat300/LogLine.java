package dat300;

public class LogLine {
    public String timestamp;
    public String hostname;
    public String count;
    public String message;

    public LogLine(String message, String count, String hostname, String timestamp) {
        this.message = message;
        this.count = count;
        this.hostname = hostname;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "";
    }
}

