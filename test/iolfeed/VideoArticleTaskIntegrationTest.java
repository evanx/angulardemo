/*
 */

package iolfeed;

import static com.sun.xml.internal.ws.util.ServiceFinder.find;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
public class VideoArticleTaskIntegrationTest {

    Logger logger = LoggerFactory.getLogger(VideoArticleTaskIntegrationTest.class);
    ContentStorage contentStorage = new ContentStorage();
    TaskManager taskManager = new TaskManager();
    JMap feedsProperties = new JMap();
    FeedsContext feedsContext = new FeedsContext(taskManager, contentStorage, feedsProperties);

    public VideoArticleTaskIntegrationTest() {
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
    public void parseVideoTitle() {
        Pattern videoTitlePattern = Pattern.compile(
            "\\s*<h1 class=\"article_headers_multimedia\">(.*)</h1>");
        Matcher matcher = videoTitlePattern.matcher(" <h1 class=\"article_headers_multimedia\">VIDEO: DA seeks gains in Free State</h1> ");
        Assert.assertTrue(matcher.find());
        Assert.assertTrue(matcher.group(1).equals("VIDEO: DA seeks gains in Free State"));
    }

    @Test
    public void parseVideo() {
    }
    
    //@Test
    public void parseArticle() throws Exception {
        String link = "http://www.iol.co.za/news/politics/video-da-seeks-gains-in-free-state-1.1678471";
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
