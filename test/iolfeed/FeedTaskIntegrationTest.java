/*
 */

package iolfeed;

import junit.framework.Assert;
import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author evanx
 */
public class FeedTaskIntegrationTest {

    Logger logger = LoggerFactory.getLogger(FeedTaskIntegrationTest.class);
    FeedsContext feedsContext = TestFeedContexts.newFeedContext();

    public FeedTaskIntegrationTest() throws Exception {
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
        FeedTask feedTask = new FeedTask(feedsContext, section);
        feedTask.run();
        for (ArticleTask articleTask : feedTask.articleTaskList) {
            logger.info("article {} {}", articleTask.completed, articleTask.articleId);
        }
        Assert.assertTrue(feedTask.articleTaskList.size() > 5);
    }
    
}
