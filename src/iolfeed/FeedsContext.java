package iolfeed;

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
    long initialDelay = Millis.fromSeconds(60);
    long period = Millis.fromMinutes(30);
    boolean once = false;
    int articleCount = 4;
    Map<String, String> feedMap = new HashMap();
    ContentStorage storage;
    TaskManager taskManager;
    
    public FeedsContext(TaskManager taskManager, ContentStorage storage, JMap properties) {
        this.storage = storage;
        put("news", "http://iol.co.za/cmlink/1.640");
        put("sport", "http://iol.co.za/cmlink/sport-category-rss-1.704");
        put("business", "http://www.iol.co.za/cmlink/1.730910");
        if (false) {
            put("multimedia", "http://iol.co.za/cmlink/1.738");
        }
    }
    
    void put(String name, String url) {
        feedMap.put(name, url);       
    }    
}
