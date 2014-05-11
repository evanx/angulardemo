package vellum.monitor;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.exception.ParseException;
import vellum.jx.JMap;
import vellum.jx.JMapException;

/**
 *
 * @author evanx
 */
public class TimestampedMonitor implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(TimestampedMonitor.class);
    private final Map<String, TimestampedTransaction> map = new ConcurrentHashMap();
    private final Deque<TimestampedTransaction> activeDeque = new ConcurrentLinkedDeque();
    private final Deque<TimestampedTransaction> completedDeque = new ConcurrentLinkedDeque();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final long limitDuration;
    private final long period;
    private ScheduledFuture future; 
    
    public TimestampedMonitor(JMap properties) throws JMapException, ParseException {
        this.limitDuration = properties.getMillis("limit");
        this.period = properties.getMillis("period", 0);
    }
    
    public void init() {
        if (period > 0) {
            future = executorService.scheduleAtFixedRate(this, period, period, TimeUnit.MILLISECONDS);        
        } else {
            logger.warn("no period");
        }
    }

    public void begin(String type, Object id) {
        begin(type, id, System.currentTimeMillis());
    }
    
    public void begin(String type, Object id, long time) {
        TimestampedTransaction tx = new TimestampedTransaction(type, id, time);
        map.put(tx.getKey(), tx);
        activeDeque.add(tx);
    }

    public boolean end(String type, Object id) {
        return end(type, id, 0);
    }
    
    public boolean end(String type, Object id, long duration) {
        String key = TimestampedTransaction.newKey(type, id);
        TimestampedTransaction tx = map.remove(key);
        if (tx == null) {
            logger.warn("end: tx not found: {}", key);
            return false;
        }
        tx.end(duration);
        completedDeque.add(tx);
        logger.info("end: {} {}", key, tx.getDuration());
        return true;
    }

    TxAggregateMap completedMap;
    TxAggregateMap expiredMap;
    
    @Override
    public void run() {
        try {
            run(System.currentTimeMillis());
        } catch (Throwable t) {
            logger.warn("run", t);
        }
    }

    void run(long time) {
        completedMap = new TxAggregateMap();
        expiredMap = new TxAggregateMap();
        logger.info("run before {}",
                String.format("map %d, active %d, completed %d", map.size(), activeDeque.size(), completedDeque.size()));
        handleCompleted();
        handleExpired(time);
        logger.info("run after {}",
                String.format("map %d, active %d, completed %d", map.size(), activeDeque.size(), completedDeque.size()));
        logger.info("run expired {}", expiredMap.size());
        logger.info("run completed {} {}", completedMap.size(), completedMap.all);
    }
    
    private void handleExpired(long time) {
        while (!activeDeque.isEmpty()) {
            TimestampedTransaction tx = activeDeque.peek();
            if (tx != null) {
                if (tx.getDuration() == 0) {
                    long duration = time - tx.getTimestamp();
                    if (duration >= limitDuration) {
                        logger.warn("expired {} {}", tx.getKey(), duration);
                        expiredMap.ingest(tx);
                        activeDeque.remove();
                        continue;
                    }
                } else {
                    activeDeque.remove();
                    continue;
                }
            }
            break;
        }
    }
    
    private void handleCompleted() {
        while (!completedDeque.isEmpty()) {
            TimestampedTransaction tx = completedDeque.pop();
            completedMap.ingest(tx);
        }
    }    

}
