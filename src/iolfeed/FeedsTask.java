package iolfeed;

import java.util.concurrent.ExecutorService;
import storage.ContentStorage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author evanx
 */
public class FeedsTask implements Runnable {

    static Logger logger = LoggerFactory.getLogger(FeedsTask.class);
    
    ScheduledExecutorService elapsedExecutorService = Executors.newSingleThreadScheduledExecutor();
    ExecutorService taskExecutorService;
    FeedsContext context;
    ContentStorage storage;
    
    public void start(FeedsContext context) throws Exception {
        this.context = context;
        taskExecutorService = Executors.newFixedThreadPool(context.feedTaskThreadPoolSize);
        if (context.once) {
            run();
        } else {
            elapsedExecutorService.scheduleAtFixedRate(topTask, context.topInitialDelay,
                    context.topPeriod, TimeUnit.MILLISECONDS);
            elapsedExecutorService.scheduleAtFixedRate(this, context.initialDelay, 
                    context.period, TimeUnit.MILLISECONDS);
        }
    }
    
    @Override
    public void run() {
        for (FeedEntity entity : context.feedEntityList) {
            try {
                if (!entity.getId().equals("top")) {
                    submit(entity.getId());
                }
            } catch (Exception e) {
                logger.warn("run: " + entity, e);
            }
        }
    }
    
    private final Runnable topTask = new Runnable() {
        final String section = "top";
        
        @Override
        public void run() {
            try {
                submit(section);
            } catch (Exception e) {
                logger.warn("run: " + section, e);
            }
        }
    };
            
    private void submit(String section) throws Exception {
        taskExecutorService.submit(new FeedTask(context, section));
    }
}
