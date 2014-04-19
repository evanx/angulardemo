package iolfeed;

import java.util.HashMap;
import java.util.Map;

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
    boolean once = false;
    int articleCount = 4;
    Map<String, String> feedMap = new HashMap();
    ContentStorage storage;
    TaskManager taskManager;
    
    public FeedsContext(TaskManager taskManager, ContentStorage storage) {
        this.storage = storage;
        put("news", "http://iol.co.za/cmlink/1.640");
        put("sport", "http://iol.co.za/cmlink/sport-category-rss-1.704");
        put("business", "http://www.iol.co.za/cmlink/1.730910");
        if (false) {
            put("multimedia", "http://iol.co.za/cmlink/1.738");
        }
    }
    
    public FeedsContext(TaskManager taskManager, ContentStorage storage, boolean once, int articleCount) {
        this(taskManager, storage);
        this.once = once;
        this.articleCount = articleCount;
    }

    void put(String name, String url) {
        feedMap.put(name, url);       
    }    
}
