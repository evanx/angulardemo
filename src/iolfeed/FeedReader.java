package iolfeed;

import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.jx.JMap;

/**
 *
 * @author evanx
 */
public class FeedReader {

    static Logger logger = LoggerFactory.getLogger(FeedReader.class);

    FeedsContext context;     
    List<ArticleThread> threadList = new ArrayList();
    DateFormat numericDateFormat = new SimpleDateFormat();
    
    public FeedReader(FeedsContext context) {
        this.context = context;
        numericDateFormat = new SimpleDateFormat(context.numericDateFormatString);
    }

    List<JMap> list(int count, String feedUrl) throws Exception {
        logger.info("feedUrl {}", feedUrl);
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(new URL(feedUrl)));
        for (Object object : feed.getEntries()) {
            SyndEntryImpl entry = (SyndEntryImpl) object;
            logger.info("title {} {}", entry.getContents().size(), entry.getTitle());
            JMap map = new JMap();
            map.put("title", entry.getTitle());
            map.put("description", FeedsUtil.cleanDescription(entry.getDescription().getValue()));
            map.put("pubDate", entry.getPublishedDate());
            map.put("numDate", numericDateFormat.format(entry.getPublishedDate()));
            map.put("link", entry.getLink());
            ArticleThread linkThread = new ArticleThread(map, entry.getLink());
            linkThread.start();
            threadList.add(linkThread);
            if (count > 0) {
                count--;
            }
            if (count == 0) {
                break;
            }
        }        
        List<JMap> articleList = new ArrayList();
        for (ArticleThread linkThread : threadList) {
            linkThread.join();
            if (linkThread.imageUrl != null) {
                linkThread.map.put("imageLink", linkThread.imageUrl);
                articleList.add(linkThread.map);
            }
        }
        return articleList;
    }
}

