package vellum.monitor;

import vellum.data.Timestamped;

/**
 *
 * @author evanx
 */
public class TimestampedTransaction implements Timestamped {
    String type;
    Object id;
    long timestamp;
    long duration;

    public TimestampedTransaction(String type, Object id) {
        this(type, id, System.currentTimeMillis());
    }
    
    public TimestampedTransaction(String type, Object id, long timestamp) {
        this.timestamp = timestamp;
        this.type = type;
        this.id = id;
    }

    public void end(long duration) {
        if (duration == 0) {
            end();
        } else {
            this.duration = duration;
        }
    }
    
    public void end() {
        duration = System.currentTimeMillis() - timestamp;
        if (duration == 0) {
            duration = 1;
        }
    }

    public String getType() {
        return type;
    }
        
    public String getKey() {
        return newKey(type, id);
    }    

    public static String newKey(String type, Object id) {
        return String.format("%s:%s", type, id);
    }

    public long getDuration() {
        return duration;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
}
