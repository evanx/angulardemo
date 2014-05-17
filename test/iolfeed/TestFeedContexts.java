/*
 */

package iolfeed;

import storage.ContentStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.jx.JMap;
import vellum.monitor.TimestampedMonitor;
import vellum.provider.VellumProvider;

/**
 *
 * @author evanx
 */
public class TestFeedContexts {

    static Logger logger = LoggerFactory.getLogger(TestFeedContexts.class);

    public static FeedsContext newFeedContext() {
        try {
            JMap monitorProperties = new JMap();
            TimestampedMonitor monitor = new TimestampedMonitor(monitorProperties);
            JMap storageProperties = new JMap();
            ContentStorage contentStorage = new ContentStorage(monitor, storageProperties);
            contentStorage.initCore();
            TaskManager taskManager = new TaskManager();
            VellumProvider.provider.put(contentStorage);
            JMap feedsProperties = new JMap();
            FeedsContext feedsContext = new FeedsContext(taskManager, contentStorage, feedsProperties);
            VellumProvider.provider.put(feedsContext);
            feedsContext.initCore();
            return feedsContext;
        } catch (Exception e) {
            throw new RuntimeException("newFeedContext", e);
        }
    }
}
