package iolfeed;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.provider.VellumProvider;

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
        VellumProvider.provider.put(context);
        VellumProvider.provider.put(context.storage);
        if (context.once) {
            run();
        } else {
            elapsedExecutorService.scheduleAtFixedRate(this, context.initialDelay, 
                    context.period, TimeUnit.MILLISECONDS);
        }
    }
    
    @Override
    public void run() {
        logger.info("user.dir {}", System.getProperty("user.dir"));
        for (String section : context.feedMap.keySet()) {
            String feedUrl = context.feedMap.get(section);
            try {
                new FeedTask(context).start(section, feedUrl, context.articleCount);
            } catch (Exception e) {
                logger.warn("run", e);
            }
        }
    }
}
