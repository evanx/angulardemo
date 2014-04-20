package iolfeed;

import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.jx.JMap;

/**
 *
 * @author evanx
 */
public class FeedTask extends Thread {

    static Logger logger = LoggerFactory.getLogger(FeedTask.class);

    FeedsContext context;     
    List<ArticleTask> taskList = new ArrayList();

    String section;
    int articleCount;
    String feedUrl;
    Exception exception;

    ExecutorService executorService;
    
    public FeedTask(FeedsContext context) {
        this.context = context;
        executorService = Executors.newFixedThreadPool(context.articleTaskThreadPoolSize);
    }

    public void start(String section, String feedUrl, int articleCount) throws Exception {
        logger = LoggerFactory.getLogger("feedtask." + section);
        this.section = section;
        this.feedUrl = feedUrl;
        this.articleCount = articleCount;
        start();
    }
    
    @Override
    public void run() {
        try {
            perform();
        } catch (Exception e) {
            logger.warn("run", e);
            this.exception = e;
        }
    }
    
    public void perform() throws Exception {
        DateFormat numericDateFormat = new SimpleDateFormat(context.numericDateFormatString);
        DateFormat isoTimestampFormat = new SimpleDateFormat(context.isoDateTimeFormatString);
        DateFormat displayTimestampFormat = new SimpleDateFormat(context.displayDateTimeFormatString);
        logger.info("feedUrl {}", feedUrl);
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(new URL(feedUrl)));
        for (Object object : feed.getEntries()) {
            SyndEntryImpl entry = (SyndEntryImpl) object;
            logger.info("title {}", entry.getTitle());
            logger.info("title {}", FeedsUtil.cleanText(entry.getTitle()));
            JMap map = new JMap();
            map.put("section", section);
            map.put("title", FeedsUtil.cleanText(entry.getTitle()));
            map.put("description", FeedsUtil.cleanDescription(entry.getDescription().getValue()));
            map.put("isoDate", isoTimestampFormat.format(entry.getPublishedDate()));
            map.put("numDate", numericDateFormat.format(entry.getPublishedDate()));
            map.put("pubDate", displayTimestampFormat.format(entry.getPublishedDate()).replace("AM", "am").replace("PM","pm"));
            map.put("link", entry.getLink());
            ArticleTask articleTask = new ArticleTask(map, entry.getLink());
            executorService.submit(articleTask);
            taskList.add(articleTask);
            if (articleCount > 0) {
                articleCount--;
            }
            if (articleCount == 0) {
                break;
            }
        }        
        executorService.shutdown();
        boolean completed = executorService.awaitTermination(context.articleTaskTimeoutSeconds, TimeUnit.SECONDS);
        if (!completed) {
            logger.error("timeout");
        }
        StringBuilder json = new StringBuilder();
        for (ArticleTask task : taskList) {
            if (json.length() > 0) {
                json.append(",\n");
            }
            json.append("  ").append(task.map.toJson());
        }
        json.insert(0, "[\n");
        json.append("\n]\n");
        logger.info("json {}", json);
        File file = new File(section + ".json");
        logger.info("write file {}", file.getAbsolutePath());
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json.toString());
        }
    }
}
