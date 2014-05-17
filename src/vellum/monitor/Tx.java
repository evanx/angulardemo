package vellum.monitor;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.data.Timestamped;
import vellum.util.Lists;

/**
 *
 * @author evanx
 */
public class Tx implements Timestamped, Thread.UncaughtExceptionHandler {

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
        subType = String.format("%s.%s", type, subType);
        Tx sub = new Tx(monitor, subType, Lists.concatenate(id, subId));
        subs.add(sub);
        logger.info("sub {}", sub);
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
        setDuration();
    }

    public void error(Object error) {
        this.error = error;
        if (error instanceof Exception) {
            exception = (Exception) error;
            logger.warn(toString(), exception);
        } else {
            logger.warn(toString());
        }
        setDuration();
    }

    public void ok() {
        setDuration();
    }

    public void expire() {
    }

    boolean isCompleted() {
        return duration > 0;
    }

    void setDuration() {
        setDuration(System.currentTimeMillis() - timestamp);
    }
    
    void setDuration(long duration) {
        this.duration = duration;
        if (duration == 0) {
            duration = 1;
        }
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

    String buildLabel() {
        StringBuilder builder = new StringBuilder(type);
        for (Object item : id) {
            builder.append(":");
            builder.append(item.toString());
        }
        return builder.toString();        
    }
    
    @Override
    public String toString() {
        String label = buildLabel();
        if (error != null) {
            return String.format("%s:(%s)", label, error.toString());
        } else if (duration == 0) {
            long activeDuration = System.currentTimeMillis() - timestamp;
            return String.format("%s:%d:+%dms", label, subs.size(), activeDuration);
        } else if (!subs.isEmpty()) {
            return String.format("%s:%d:%dms", label, subs.size(), duration);
        } else {
            return String.format("%s:%dms", label, duration);
        }
    }

    public void fin() {
        if (duration == 0 && error == null) {
            logger.error("fin " + buildLabel());
            new Exception().printStackTrace(System.err);
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        logger.error("uncaught " + buildLabel(), e);
    }
}
