/*
 */

package iolfeed;

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
public class ArticleTaskIntegrationTest {

    Logger logger = LoggerFactory.getLogger(ArticleTaskIntegrationTest.class);
    ContentStorage contentStorage = new ContentStorage();
    TaskManager taskManager = new TaskManager();
    JMap feedsProperties = new JMap();
    FeedsContext feedsContext = new FeedsContext(taskManager, contentStorage, feedsProperties);

    public ArticleTaskIntegrationTest() {
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
    public void parseArticle() throws Exception {
        String link = "http://www.iol.co.za/sport/boxing/klitschko-brushes-off-softie-whiner-jibes-1.1679494";
        JMap map = new JMap();
        map.put("section", "news");
        map.put("title", "test0title");
        map.put("description", "test0description");
        map.put("pubDate", "April 23 2014 at 16:27");
        map.put("numDate", "20140423");
        map.put("link", link);
        ArticleTask articleTask = new ArticleTask(map);
        articleTask.init();
        articleTask.run();
        Assert.assertTrue(articleTask.isCompleted());
        Assert.assertTrue(articleTask.paragraphs.size() > 5);
        Assert.assertTrue(articleTask.imagePath.contains("/image/"));
        logger.info("imagePath {}", articleTask.imagePath);
    }

    
    
    
}