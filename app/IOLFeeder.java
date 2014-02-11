package app;

import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.jx.JMap;

/**
 *
 * @author evanx
 */
public class IOLFeeder {

    static Logger logger = LoggerFactory.getLogger(GitteryApp.class);
    List<LinkThread> threadList = new ArrayList();

    List list(int count, String feedUrl) throws Exception {
        logger.info("feedUrl {}", feedUrl);
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(new URL(feedUrl)));
        for (Object object : feed.getEntries()) {
            SyndEntryImpl entry = (SyndEntryImpl) object;
            JMap map = new JMap();
            map.put("title", entry.getTitle());
            map.put("description", IOLFeeds.cleanDescription(entry.getDescription().getValue()));
            map.put("pubDate", entry.getPublishedDate());
            map.put("link", entry.getLink());
            LinkThread linkThread = new LinkThread(map, entry.getLink());
            linkThread.start();
            threadList.add(linkThread);
            count--;
            if (count == 0) break;
        }
        
        List articleList = new ArrayList();
        for (LinkThread linkThread : threadList) {
            linkThread.join();
            if (linkThread.imageLink != null) {
                linkThread.map.put("image", "http://www.iol.co.za/" + linkThread.imageLink);
            }
            articleList.add(linkThread.map);
            logger.info(linkThread.map.toJson());
        }
        return articleList;
    }
}

