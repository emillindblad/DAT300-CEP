package dat300;

public class EntryWithTimeStamp {
   public LogLine logLine;
   public long sequentialId;
   public long preTimeStamp;
   public long postTimeStamp;
   public int queueSize;

    public EntryWithTimeStamp(long id, LogLine logLine, long preTimeStamp) {
        this.sequentialId = id;
        this.logLine = logLine;
        this.preTimeStamp = preTimeStamp;
        this.postTimeStamp = -1;
        this.queueSize = -1;
    }

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

    public long getSequentialId(){return this.sequentialId;}

    //public void setLogLine(String logLine) {
    //    this.logLine = logLine;
    //}

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
