package iolfeed;

import static iolfeed.FeedTask.logger;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import vellum.data.Millis;
import vellum.jx.JMap;

/**
 *
 * @author evanx
 */
public final class FeedsContext {
    
    String contentHost = System.getProperty("reader.host", "localhost:8088");
    String contentPath = "iol";
    String isoDateTimeFormatString = "yyyy-MM-dd HH:mm";
    String displayDateTimeFormatString = "MMMM dd, yyyy 'at' hh:mma";
    String numericDateFormatString = "yyyyMMdd";
    long initialDelay = Millis.fromSeconds(5);
    long period = Millis.fromMinutes(30);
    long articleTaskTimeoutSeconds = 300;
    int articleTaskThreadPoolSize = 99;
    boolean once = false;
    int articleCount = 99;
    Map<String, String> feedMap = new HashMap();
    ContentStorage storage;
    TaskManager taskManager;
    
    public FeedsContext(TaskManager taskManager, ContentStorage storage, JMap properties) {
        this.storage = storage;
        putFeed("news", "News", "http://www.iol.co.za/cmlink/1.640");
        if (true) {
            putFeed("motoring", "Motoring", "http://www.iol.co.za/cmlink/1.746734");
            putFeed("sport", "Sport", "http://www.iol.co.za/cmlink/sport-category-rss-1.704");
            putFeed("business", "Business", "http://www.iol.co.za/cmlink/1.730910");
            putFeed("scitech", "SciTech", "http://www.iol.co.za/cmlink/science-technology-business-rss-1.847516");
            putFeed("tonight", "Tonight", "http://www.iol.co.za/cmlink/1.891206");
        }
        if (false) {
            putFeed("multimedia", "Multimedia", "http://www.iol.co.za/cmlink/1.738");
        }
        if (false) {
            putFeed("home", "Home Page", "http://www.iol.co.za/cmlink/home-page-rss-1.1538217");
            putFeed("home", "Home Page", "http://www.iol.co.za/cmlink/home-page-extended-1.628986");
            putFeed("tvbox", "TV Box Teaser", "http://www.iol.co.za/cmlink/tv-box-teaser-rss-1.1537631");
            putFeed("mostcommented", "Most Commmented", "http://www.iol.co.za/cmlink/most-commmented-stories-1.1625");
            putFeed("mostviewed", "Most Viewed", "http://www.iol.co.za/cmlink/most-viewed-stories-1.1624");
            putFeed("editorspick", "Editors Pick", "http://www.iol.co.za/cmlink/editors-pick-extended-rss-1.1137157");
            putFeed("news/videos", "News Videos", "http://www.iol.co.za/cmlink/news-rss-multimedia-videos-feed-1.1152520");
            putFeed("news/galleries", "News Galleries", "http://www.iol.co.za/cmlink/news-rss-multimedia-galleries-feed-1.1149195");
            putFeed("news/africa", "News Africa", "http://www.iol.co.za/cmlink/news-africa-extended-1.679216");
            putFeed("news/backpage", "News Back Page", "http://www.iol.co.za/cmlink/news-back-page-extended-1.628990");
            putFeed("news/southafrica", "News South Africa", "http://www.iol.co.za/cmlink/news-south-africa-extended-1.679178");
            putFeed("news/world", "News World", "http://www.iol.co.za/cmlink/news-world-extended-1.679217");
            putFeed("news/westerncape", "News Western Cape", "http://www.iol.co.za/cmlink/western-cape-extended-1.679223");
            putFeed("news/gauteng", "News Gauteng", "http://www.iol.co.za/cmlink/news-gauteng-extended-1.679235");
            putFeed("news/kzn", "News KwaZulu-Natal", "http://www.iol.co.za/cmlink/news-kwazulu-natal-extended-1.679236");
            putFeed("sport", "Sport", "http://www.iol.co.za/cmlink/sport-extended-1.628987");
            putFeed("sport/soccer", "Soccer", "http://www.iol.co.za/cmlink/soccer-soccer-extended-rss-1.679215");
            putFeed("sport/rugby", "Sport Rugby", "http://www.iol.co.za/cmlink/sport-rugby-extended-1.628988");
            putFeed("sport/cricket", "Sport Cricket", "http://www.iol.co.za/cmlink/sport-cricket-extended-1.628989");
            putFeed("sport/golf", "Sport Golf", "http://www.iol.co.za/cmlink/sport-golf-extended-1.679220");
        }
    }
    
    void putFeed(String name, String label, String url) {
        feedMap.put(name, url);       
    }    
    
    public void putJson(String path, String json) throws IOException {
        logger.info("write file {}", path);
        storage.put(path, json.getBytes());
        File file = new File(path);
        file.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json);
        }
    }
    
}
