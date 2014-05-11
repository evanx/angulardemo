/*
 */

package iolfeed;

import storage.ContentStorage;
import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.jx.JMap;
import vellum.provider.VellumProvider;

/**
 *
 * @author evanx
 */
public class TestFeedContexts {

    static Logger logger = LoggerFactory.getLogger(TestFeedContexts.class);

    public static FeedsContext newFeedContext() {
        try {
            JMap storageProperties = new JMap();
            ContentStorage contentStorage = new ContentStorage(storageProperties);
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
