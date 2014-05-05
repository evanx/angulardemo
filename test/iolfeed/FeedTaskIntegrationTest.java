/*
 */

package iolfeed;

import java.io.IOException;
import storage.ContentStorage;
import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.AfterClass;
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
public class FeedTaskIntegrationTest {

    Logger logger = LoggerFactory.getLogger(FeedTaskIntegrationTest.class);
    ContentStorage contentStorage = new ContentStorage();
    TaskManager taskManager = new TaskManager();
    JMap feedsProperties = new JMap();
    FeedsContext feedsContext = new FeedsContext(taskManager, contentStorage, feedsProperties);

    public FeedTaskIntegrationTest() throws IOException {
        feedsContext.maxDepth = 3;
        contentStorage.init();
        feedsContext.init();
        VellumProvider.provider.put(feedsContext);
        VellumProvider.provider.put(contentStorage);        
    }
    
    @BeforeClass
    public static void setUpClass() {
        BasicConfigurator.configure();
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void parseFeed() throws Exception {
        perform("scitech");
    }
    
    private void perform(String section) throws Exception {
        new FeedTask(feedsContext).start(section, 
                feedsContext.feedMap.get(section), feedsContext.articleCount);
    }
    
}
