package iolfeed;

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
    FeedsContext context;
    ContentStorage storage;
    
    public void start(FeedsContext context) throws Exception {
        this.context = context;
        if (context.once) {
            run();
        } else {
            elapsedExecutorService.scheduleAtFixedRate(this, context.initialDelay, 
                    context.period, TimeUnit.MILLISECONDS);
            elapsedExecutorService.scheduleAtFixedRate(topTask, context.topInitialDelay,
                    context.topPeriod, TimeUnit.MILLISECONDS);
        }
    }
    
    @Override
    public void run() {
        for (FeedEntity entity : context.feedEntityList) {
            try {
                perform(entity.getId());
            } catch (Exception e) {
                logger.warn("run", e);
            }
        }
    }
    
    private final Runnable topTask = new Runnable() {
        final String section = "top";
        
        @Override
        public void run() {
            try {
                perform(section);
            } catch (Exception e) {
                logger.warn("run: " + section, e);
            }
        }
    };
            
    private void perform(String section) throws Exception {
        new FeedTask(context).start(section, context.feedMap.get(section), context.articleCount);
    }
}
