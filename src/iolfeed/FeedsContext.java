package iolfeed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.data.Millis;
import vellum.jx.JMap;

/**
 *
 * @author evanx
 */
public final class FeedsContext {
    static Logger logger = LoggerFactory.getLogger(FeedsContext.class);
    
    String defaultHtml;
    String isoDateTimeFormatString = "yyyy-MM-dd HH:mm";
    String displayDateTimeFormatString = "MMMM dd, yyyy 'at' hh:mma";
    String numericDateFormatString = "yyyyMMdd";
    long initialDelay = Millis.fromSeconds(30);
    long period = Millis.fromMinutes(60);
    long topInitialDelay = Millis.fromSeconds(15);
    long topPeriod = Millis.fromMinutes(5);
    boolean refresh = false;
    long articleTaskTimeoutSeconds = 120;
    int articleTaskThreadPoolSize = 4;
    int retryCount = 4;
    boolean once = false;
    int articleCount = 99;
    Map<String, String> feedMap = new HashMap();
    List<FeedEntity> feedEntityList = new ArrayList();
    ContentStorage storage;
    TaskManager taskManager;
    
    public FeedsContext(TaskManager taskManager, ContentStorage storage, JMap properties) {
        this.storage = storage;
        //putFeed("lxer", "LXer", "http://lxer.com/module/newswire/headlines.rdf");
        putFeed("top", "Top stories", "http://www.iol.co.za/cmlink/home-page-rss-1.1538217");
        putFeed("news", "News", "http://www.iol.co.za/cmlink/1.640");
        putFeed("motoring", "Motoring", "http://www.iol.co.za/cmlink/1.746734");
        putFeed("sport", "Sport", "http://www.iol.co.za/cmlink/sport-category-rss-1.704");
        putFeed("business", "Business", "http://www.iol.co.za/cmlink/1.730910");
        putFeed("scitech", "SciTech", "http://www.iol.co.za/cmlink/science-technology-business-rss-1.847516");
        putFeed("lifestyle", "Lifestyle", "http://www.iol.co.za/cmlink/1.999916");
        putFeed("travel", "Travel", "http://www.iol.co.za/cmlink/1.875733");
        putFeed("tonight", "Tonight", "http://www.iol.co.za/cmlink/1.891206");
        putFeed("multimedia", "Multimedia", "http://www.iol.co.za/cmlink/1.738");
        if (false) {
            putFeed("home", "Home Page", "http://www.iol.co.za/cmlink/home-page-rss-1.1538217");
            putFeed("home", "Home Page", "http://www.iol.co.za/cmlink/home-page-extended-1.628986");
            putFeed("top", "Top stories", "http://www.iol.co.za/cmlink/tv-box-teaser-rss-1.1537631");
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
    
    void putFeed(String id, String label, String url) {
        FeedEntity feedEntity = new FeedEntity(id, label, url);
        feedEntityList.add(feedEntity);
        feedMap.put(id, url);       
    }    

    public void init() {
    }           
}
