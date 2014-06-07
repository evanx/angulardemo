package iolfeed;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import storage.ContentStorage;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
    Map<String, Future> futureMap = new HashMap();

    public void start(FeedsContext context) throws Exception {
        logger.info("start");
        this.context = context;
        taskExecutorService = Executors.newFixedThreadPool(context.feedTaskThreadPoolSize);
        if (context.once) {
            logger.info("once");
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
        logger.info("run");
        for (FeedEntity entity : context.feedEntityList) {
            if (!entity.getId().equals("top")) {
                submit(entity.getId());
            }
        }
    }

    private final Runnable topTask = new Runnable() {
        final String section = "top";

        @Override
        public void run() {
            submit(section);
        }
    };

    private void submit(String section) {
        logger.info("submit: {}", section);
        Future future = futureMap.get(section);
        if (future != null) {
            if (!future.isDone()) {
                logger.warn("not done: {}", section);
                return;
            } else {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    logger.warn("interrupted {}", section);
                } catch (ExecutionException e) {
                    logger.error("exception {}", e.getCause());
                }
            }
        }
        futureMap.put(section,
                taskExecutorService.submit(
                        new FeedTask(context, section)));
        logger.info("submitted: {}", section);
    }
}
