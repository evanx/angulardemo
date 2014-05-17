/*
 */

package iolfeed;

import storage.ContentStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.jx.JMap;
import vellum.monitor.TimestampedMonitor;

/**
 *
 * @author evanx
 */
public class TestFeedContexts {

    static Logger logger = LoggerFactory.getLogger(TestFeedContexts.class);

    public static FeedsContext newFeedContext() {
        try {
            JMap monitorProperties = new JMap();
            JMap storageProperties = new JMap();
            JMap feedsProperties = new JMap();
            TimestampedMonitor monitor = new TimestampedMonitor(monitorProperties);
            ContentStorage contentStorage = new ContentStorage(monitor, storageProperties);
            TaskManager taskManager = new TaskManager();
            FeedsContext feedsContext = new FeedsContext(monitor, taskManager, contentStorage, feedsProperties);
            return feedsContext;
        } catch (Exception e) {
            throw new RuntimeException("newFeedContext", e);
        }
    }
}
