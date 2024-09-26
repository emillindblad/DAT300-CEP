package dat300;

public class EntryWithTimeStamp {
   public int logLine;
   public long preTimeStamp;
   public long postTimeStamp;

    public EntryWithTimeStamp(int logLine, long preTimeStamp) {
        this.logLine = logLine;
        this.preTimeStamp = preTimeStamp;
        this.postTimeStamp = -1;
    }

    @Override
    public String toString() {
        return logLine+ ","+preTimeStamp+","+postTimeStamp;
    }

    public int getLogLine() {
        return logLine;
    }

    public void setLogLine(int logLine) {
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
