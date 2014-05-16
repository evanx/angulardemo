package vellum.monitor;

import java.util.Deque;
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
import vellum.util.Lists;

/**
 *
 * @author evanx
 */
public class TimestampedMonitor implements Runnable {
    private final Logger logger = LoggerFactory.getLogger("tx");
    private final Deque<Tx> activeDeque = new ConcurrentLinkedDeque();
    private final Deque<Tx> completedDeque = new ConcurrentLinkedDeque();
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
    
    public void shutdown() {
        executorService.shutdown();
    }

    public Tx begin(String type, Object... id) {
        return begin(System.currentTimeMillis(), type, id);
    }
    
    public Tx begin(long timestamp, String type, Object... id) {
        Tx tx = new Tx(timestamp, this, type, id);
        activeDeque.add(tx);
        Tx.threadLocal.set(tx);
        logger.info("begin {}", tx);
        return tx;
    }

    public void finish(Tx tx) {
        logger.info("finish {}", tx);
        completedDeque.add(tx);
        for (Tx sub : tx.subs) {
            if (!sub.isCompleted()) {
                sub.duration(0);
            }
        }
        tx.subs.clear();
}
    
    LongAggregateMap completedMap;
    LongAggregateMap expiredMap;
    
    @Override
    public void run() {
        try {
            run(System.currentTimeMillis());
        } catch (Throwable t) {
            logger.warn("run", t);
        }
    }

    void run(long timestamp) {
        completedMap = new LongAggregateMap();
        expiredMap = new LongAggregateMap();
        logger.info("run before {}",
                String.format("active %d, completed %d", activeDeque.size(), completedDeque.size()));
        handleCompleted();
        handleExpired(timestamp);
        logger.info("run after {}",
                String.format("active %d, completed %d", activeDeque.size(), completedDeque.size()));
        logger.info("run expired {}", expiredMap);
        expiredMap.println(System.out);
        logger.info("run completed {}", completedMap);
        completedMap.println(System.out);
        for (Tx tx : Lists.list(activeDeque.iterator())) {
            logger.info("- {}", tx);  
        }
    }
    
    private void handleExpired(long timestamp) {
        while (!activeDeque.isEmpty()) {
            Tx tx = activeDeque.peek();
            if (tx != null) {
                if (tx.getDuration() == 0) {
                    long duration = timestamp - tx.getTimestamp();
                    if (duration >= limitDuration) {
                        logger.warn("expired {}", tx);
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
            Tx tx = completedDeque.pop();
            completedMap.ingest(tx);
        }
    }    

}
