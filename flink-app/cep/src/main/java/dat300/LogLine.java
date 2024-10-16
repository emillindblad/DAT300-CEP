package dat300;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogLine {
    public LocalDateTime timeStamp;
    public String hostName;
    public String message;
    public String ip;

    public LogLine(String rawLine) {
        String[] splitTimeMsg = rawLine.split("]: ");
        String[] splitTimeHostname = splitTimeMsg[0].split(" ");

        this.timeStamp = createTimeStamp(splitTimeHostname);
        this.hostName = splitTimeHostname[3];
        this.message = splitTimeMsg[1];
        this.ip = getIp();
    }

    public LocalDateTime getTimeStamp() {
        return this.timeStamp;
    }

    public String getIp() {
        Pattern pattern = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
        Matcher matcher = pattern.matcher(this.message);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return "";
        }
    }


    private LocalDateTime createTimeStamp(String[] splitTimeHostname) {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd HH:mm:ss");
        String timeStampString = String.format("%s %s %s",splitTimeHostname[0], splitTimeHostname[1], splitTimeHostname[2]);
        int currentYear = LocalDateTime.now().getYear();
        
        String timestampWithYear = timeStampString + " " + currentYear;
        DateTimeFormatter formatterWithYear = DateTimeFormatter.ofPattern("MMM dd HH:mm:ss yyyy", Locale.ENGLISH);
        return LocalDateTime.parse(timestampWithYear, formatterWithYear);
    }

    public long getUnixTimeStamp() {
        long unixTime = this.timeStamp.toEpochSecond(ZoneOffset.UTC);
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

