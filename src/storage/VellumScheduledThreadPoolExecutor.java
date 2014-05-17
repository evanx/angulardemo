package storage;

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
        logger.error("", t);
    }        
}
