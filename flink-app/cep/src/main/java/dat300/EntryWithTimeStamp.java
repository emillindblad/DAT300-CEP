package dat300;

public class EntryWithTimeStamp {
   public LogLine logLine;
   public long sequentialId;
   public long preTimeStamp;
   public long postTimeStamp;
   public int queueSize;

    public EntryWithTimeStamp(long id, LogLine logLine, long preTimeStamp, int queue) {
        this.sequentialId = id;
        this.logLine = logLine;
        this.preTimeStamp = preTimeStamp;
        this.postTimeStamp = -1;
        this.queueSize = queue;
    }

    @Override
    public String toString() {
        return sequentialId + ","+preTimeStamp+","+postTimeStamp+","+queueSize;
    }

    public LogLine getLogLine() {
        return logLine;
    }

    public void setPostTimeStamp(long postTimeStamp) {
        this.postTimeStamp = postTimeStamp;
    }
}
