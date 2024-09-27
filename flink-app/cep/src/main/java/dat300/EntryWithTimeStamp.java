package dat300;

public class EntryWithTimeStamp {
   public String logLine;
   public long preTimeStamp;
   public long postTimeStamp;

    public EntryWithTimeStamp(String logLine, long preTimeStamp) {
        this.logLine = logLine;
        this.preTimeStamp = preTimeStamp;
        this.postTimeStamp = -1;
    }

    @Override
    public String toString() {
        return logLine.hashCode() + ","+preTimeStamp+","+postTimeStamp;
    }

    public String getLogLine() {
        return logLine;
    }

    public void setLogLine(String logLine) {
        this.logLine = logLine;
    }

    public long getPreTimeStamp() {
        return preTimeStamp;
    }

    public void setPreTimeStamp(long preTimeStamp) {
        this.preTimeStamp = preTimeStamp;
    }

    public long getPostTimeStamp() {
        return postTimeStamp;
    }

    public void setPostTimeStamp(long postTimeStamp) {
        this.postTimeStamp = postTimeStamp;
    }
}
