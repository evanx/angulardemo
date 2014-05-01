/*
 */

package iolfeed;

import storage.ContentStorage;
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
        Pattern videoSizePattern = Pattern.compile(
            "\\s*<object width=\"([0-9]*)\" height=\"([0-9]*)\">");
        String line = " <object width=\"550\" height=\"440\"> ";
        Matcher matcher = videoSizePattern.matcher(line);
        Assert.assertTrue(matcher.find());
        Assert.assertTrue(matcher.group(1).equals("550"));        
        Assert.assertTrue(matcher.group(2).equals("440"));
    }

    @Test
    public void parseVideoId() {
        Pattern videoIdPattern = Pattern.compile(
            "\\s*<param name=\"movie\" value=\"([^&]*).*\">");
        String line = " <param name=\"movie\" value=\"IO3xDa1Hvcc&feature=youtu.be\"> ";
        Matcher matcher = videoIdPattern.matcher(line);
        Assert.assertTrue(matcher.find());
        Assert.assertEquals("IO3xDa1Hvcc", matcher.group(1));
    }

    @Test
    public void parseVideoCaption() {
        final Pattern videoCaptionPattern = Pattern.compile(
            "\\s*<p class=\"multimedia_gal_captions\">([^<]*)");
        String line = "<p class=\"multimedia_gal_captions\">DA leader Helen Zille and parliamentary member Lindiwe Mazibuko joined Free State provincial candidate Patricia Kopane at an election rally at Seeisoville stadium in Kroonstad on Monday.  <br>";
        Matcher matcher = videoCaptionPattern.matcher(line);
        Assert.assertTrue(matcher.find());
        Assert.assertEquals("DA leader Helen Zille and parliamentary member Lindiwe Mazibuko joined Free State provincial candidate Patricia Kopane at an election rally at Seeisoville stadium in Kroonstad on Monday.  ", 
                matcher.group(1));        
    }
    
    String[] videoLinks = {
        "http://www.iol.co.za/news/politics/video-eff-s-banned-ad-1.1678775",
        "http://www.iol.co.za/news/politics/video-da-seeks-gains-in-free-state-1.1678471",
        "http://www.iol.co.za/motoring/motorsport/footkhana-ken-block-takes-on-neymar-1.1679737",
    };

    @Test
    public void parseFootKhanaVideoArticle() throws Exception {
        parseArticle("motoring", "April 24 2014 at 11:09am", "20140424",
                "FootKhana! Ken Block takes on Neymar",
               "With World Cup fever on the rise, what could be more topical than for top stunt driver Ken Block to challenge Brazilian football star Neymar to a game of Footkhana. Never heard of it? Watch and learn.",
               "http://www.iol.co.za/motoring/motorsport/footkhana-ken-block-takes-on-neymar-1.1679737");
    }
    
    @Test
    public void parseArticle() throws Exception {
        String link = "http://www.iol.co.za/news/politics/video-da-seeks-gains-in-free-state-1.1678471";
        JMap map = new JMap();
        map.put("section", "news");
        map.put("title", "VIDEO: DA seeks gains in Free State");
        map.put("description", "DA leader Helen Zille and parliamentary member Lindiwe Mazibuko joined Free State provincial candidate Patricia Kopane at an election rally at Seeisoville stadium in Kroonstad on Monday.");
        map.put("pubDate", "April 22 2014 at 07:08am");
        map.put("numDate", "20140423");
        map.put("link", link);
        ArticleTask articleTask = new ArticleTask(map);
        articleTask.init();
        articleTask.run();
        System.out.println(articleTask.map.toJson());
        Assert.assertTrue(articleTask.isCompleted());
        Assert.assertTrue(articleTask.youtubeList.size() == 1);
        Assert.assertEquals("DA leader Helen Zille and parliamentary member Lindiwe Mazibuko joined Free State provincial candidate Patricia Kopane at an election rally at Seeisoville stadium in Kroonstad on Monday.", 
                articleTask.multimediaCaption);
        Assert.assertEquals("April 22 2014 at 07:08am", articleTask.multimediaTimestamp);
    }     
    
    private ArticleTask parseArticle(String section, String pubDate, String numDate, 
            String title, String description, 
            String link) throws Exception {
        JMap map = new JMap();
        map.put("section", section);
        map.put("title", title);
        map.put("description", description);
        map.put("pubDate", pubDate);
        map.put("numDate", numDate);
        map.put("link", link);
        ArticleTask articleTask = new ArticleTask(map);
        articleTask.init();
        articleTask.run();
        System.out.println(articleTask.map.toJson());
        return articleTask;
    }
    
}
