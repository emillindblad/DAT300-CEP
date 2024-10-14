package dat300;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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

    public long getUnixTimeStamp() {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd HH:mm:ss");
        int currentYear = LocalDateTime.now().getYear();
        
        String timestampWithYear = this.timeStamp + " " + currentYear;
        DateTimeFormatter formatterWithYear = DateTimeFormatter.ofPattern("MMM dd HH:mm:ss yyyy", Locale.ENGLISH);
        LocalDateTime dateTime = LocalDateTime.parse(timestampWithYear, formatterWithYear);

        long unixTime = dateTime.toEpochSecond(ZoneOffset.UTC);
        return  unixTime * 1000;
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

