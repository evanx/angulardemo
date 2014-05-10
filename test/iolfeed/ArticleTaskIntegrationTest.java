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
import vellum.jx.JMaps;
import vellum.provider.VellumProvider;

/**
 *
 * @author evanx
 */
public class ArticleTaskIntegrationTest {

    Logger logger = LoggerFactory.getLogger(ArticleTaskIntegrationTest.class);
    ContentStorage contentStorage = new ContentStorage(JMaps.map("storage", System.getProperties()));
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
        String link = "http://www.iol.co.za/motoring/cars/nissan/self-cleaning-car-created-by-nissan-1.1680061";
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
        Assert.assertTrue(articleTask.imageList.size() == 4);
    }
}
