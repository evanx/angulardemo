package vellum.monitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.data.Timestamped;

/**
 *
 * @author evanx
 */
public class Tx implements Timestamped {

    final static Logger logger = LoggerFactory.getLogger("tx");
    final static ThreadLocal<Tx> threadLocal = new ThreadLocal();
    String type;
    Object[] id;
    long timestamp;
    long duration;
    TimestampedMonitor monitor;
    Object error;
    Exception exception;
    List<Tx> subs = new ArrayList();

    Tx() {
        this(null, "none");
    }

    Tx(TimestampedMonitor monitor, String type, Object... id) {
        this(System.currentTimeMillis(), monitor, type, id);
    }

    Tx(long timestamp, TimestampedMonitor monitor, String type, Object[] id) {
        this.monitor = monitor;
        this.timestamp = timestamp;
        this.type = type;
        this.id = id;
    }

    public static Tx get() {
        Tx tx = threadLocal.get();
        if (tx == null) {
            logger.warn("");
            tx = new Tx();
            threadLocal.set(tx);
        }
        return tx;
    }

    public Tx sub(String subType, Object... subId) {
        subType = String.format("%s.%s", toString(), subType);
        Tx sub = new Tx(monitor, subType, subId);
        subs.add(sub);
        logger.info("checkpoint {}", sub);
        return sub;
    }

    public boolean isError() {
        return error != null;
    }

    public Exception getException() {
        return exception;
    }

    public void warn(Object error) {
        this.error = error;
        if (error instanceof Exception) {
            exception = (Exception) error;
        }
        logger.warn(toString());
        duration(0);
    }

    public void error(Object error) {
        this.error = error;
        if (error instanceof Exception) {
            exception = (Exception) error;
            logger.warn(toString(), exception);
        } else {
            logger.warn(toString());
        }
        duration(0);
    }

    public void ok() {
        duration(0);
    }

    public void expire() {        
    }

    boolean isCompleted() {
        return duration > 0;
    }

    void duration(long duration) {
        if (duration == 0) {
            duration = System.currentTimeMillis() - timestamp;
            if (duration == 0) {
                duration = 1;
            }
        }
        this.duration = duration;
        if (monitor != null) {
            monitor.finish(this);
        }
    }

    public String getType() {
        return type;
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
        String idString = Arrays.toString(id);
        if (error != null) {
            return String.format("%s.%s:(%s)", type, idString, error.toString());
        } else if (duration > 0) {
            return String.format("%s.%s:%sms", type, idString, duration);
        } else {
            return String.format("%s.%s", type, idString);
        }
    }
}
