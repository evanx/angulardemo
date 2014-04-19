package iolfeed;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author evanx
 */
public class FeedsContext {

    String contextName = "iol";
    String baseUrl = "http://chronica.co:8088";
    boolean once = false;
    int articleCount = 0;
    Map<String, String> feedMap = new HashMap();

    public FeedsContext() {
        put("business", "http://www.iol.co.za/cmlink/1.730910");
        put("news", "http://iol.co.za/cmlink/1.640");
        put("sport", "http://iol.co.za/cmlink/sport-category-rss-1.704");
        if (false) {
            put("multimedia", "http://iol.co.za/cmlink/1.738");
        }
    }
    
    public FeedsContext(boolean once, int articleCount) {
        this();
        this.once = once;
        this.articleCount = articleCount;
    }

    void put(String name, String url) {
        feedMap.put(name, url);       
    }

    
    
}
