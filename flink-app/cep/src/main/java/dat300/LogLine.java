package dat300;

public class LogLine {
    public String timeStamp;
    public String hostName;
    public String message;

    public LogLine(String rawLine) {
        String[] splitTimeMsg = rawLine.split("]: ");
        String[] splitTimeHostname = splitTimeMsg[0].split(" ");

        this.timeStamp = String.format("%s %s %s",splitTimeHostname[0], splitTimeHostname[1], splitTimeHostname[2]);
        this.hostName = splitTimeHostname[3];
        this.message = splitTimeMsg[1];
    }

    @Override
    public String toString() {
        return "LogLine{" +
                "timestamp='" + timeStamp + '\'' +
                ", hostname='" + hostName + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}

