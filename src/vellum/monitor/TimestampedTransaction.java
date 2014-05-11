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
    TimestampedMonitor monitor;
    boolean completed = false;

    TimestampedTransaction(TimestampedMonitor monitor, String type, Object id) {
        this(monitor, type, id, System.currentTimeMillis());
    }

    TimestampedTransaction(TimestampedMonitor monitor, String type, Object id, long timestamp) {
        this.monitor = monitor;
        this.timestamp = timestamp;
        this.type = type;
        this.id = id;
    }

    public void end() {
        end(0);
    }

    public void end(long duration) {
        if (duration == 0) {
            duration = System.currentTimeMillis() - timestamp;
            if (duration == 0) {
                duration = 1;
            }
        }
        this.duration = duration;
        monitor.end(this);
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

    @Override
        public String toString() {
        return String.format("%s:%s:%s", type, id, duration);
    }        
}
