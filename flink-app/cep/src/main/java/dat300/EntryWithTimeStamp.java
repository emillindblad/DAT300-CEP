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
}
