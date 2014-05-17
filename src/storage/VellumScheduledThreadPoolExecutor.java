package storage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author evanx
 */
public class VellumScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {

    Logger logger = LoggerFactory.getLogger(VellumScheduledThreadPoolExecutor.class);

    public VellumScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) { 
        super.afterExecute(r, t);
        if (t != null) {
            logger.error("throwable", t);
            t.printStackTrace(System.err);
        }
        if (t instanceof ExecutionException) {
            ExecutionException ee = (ExecutionException) t;
            logger.error("exception", ee.getCause());
        }
    }        
}
