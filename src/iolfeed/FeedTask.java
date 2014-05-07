package iolfeed;

import com.google.gson.Gson;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

    Logger logger = LoggerFactory.getLogger(FeedTask.class);

    FeedsContext context;     
    List<ArticleTask> articleTaskList = new ArrayList();

    String section;
    int articleCount;
    String feedUrl;
    Exception exception;
    boolean refresh;
    
    public FeedTask(FeedsContext context) {
        this.context = context;
    }

    public void start(String section, String feedUrl, int articleCount) throws Exception {
        logger = LoggerFactory.getLogger("FeedTask." + section);
        this.section = section;
        this.feedUrl = feedUrl;
        this.articleCount = articleCount;
        start();
    }
    
    @Override
    public void run() {
        try {
            perform();
        } catch (RuntimeException e) {
            logger.warn("run: " + e.getMessage(), e);
            this.exception = e;
        } catch (Exception e) {
            logger.warn(String.format("run %s: %s", e.getClass().getSimpleName(), e.getMessage()));
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
        logger.info("entries {} {}", feedUrl, feed.getEntries().size());
        for (Object object : feed.getEntries()) {
            SyndEntryImpl entry = (SyndEntryImpl) object;
            String title = FeedsUtil.cleanText(entry.getTitle());
            logger.info("title {}", title);
            if (!FeedsUtil.isText(entry.getTitle())) {
                logger.warn("invalid title {}", entry.getTitle());
                continue;
            }
            String description = FeedsUtil.cleanDescription(entry.getDescription().getValue());
            if (description.isEmpty()) {
                logger.warn("empty lead {}", description, entry.getLink());
            } else if (!FeedsUtil.isText(description)) {
                logger.warn("invalid lead [{}] {}", description, entry.getLink());
                continue;
            }
            JMap map = new JMap();
            map.put("section", section);
            map.put("title", title);
            map.put("description", description);
            if (entry.getPublishedDate() == null) {
                entry.setPublishedDate(new Date());
            }
            map.put("isoDate", isoTimestampFormat.format(entry.getPublishedDate()));
            map.put("numDate", numericDateFormat.format(entry.getPublishedDate()));
            map.put("pubDate", displayTimestampFormat.format(entry.getPublishedDate()).replace("AM", "am").replace("PM", "pm"));
            map.put("link", entry.getLink());
            ArticleTask articleTask = new ArticleTask(map);
            articleTask.init();
            articleTaskList.add(articleTask);
            if (articleCount > 0) {
                articleCount--;
            }
            if (articleCount == 0) {
                break;
            }
        }        
        if (articleTaskList.isEmpty()) {
            logger.warn("empty article list");
        } else {
            while (!performTasks()) {
                logger.warn("performTasks incomplete");
            }
            for (int i = 0; !write() && i <= context.retryCount; i++) {
                logger.warn("write incomplete");
                performTasks();
            }
        }
    }
    
    private boolean performTasks() {
        ExecutorService executorService = Executors.newFixedThreadPool(context.articleTaskThreadPoolSize);
        for (ArticleTask articleTask : articleTaskList) {
            if (articleTask.isCompleted()) {
                if (articleTask.imagePath != null) {
                    context.storage.addLink(section, articleTask.imagePath);
                }
            } else if (articleTask.currentThread != null) {
                logger.error("articleTask running");
            } else {
                executorService.submit(articleTask);
            }
        }
        executorService.shutdown();
        try {
            while (!executorService.awaitTermination(context.articleTaskTimeoutSeconds, TimeUnit.SECONDS)) {
                logger.warn("executor awaitTermination {}s", context.articleTaskTimeoutSeconds);
            }
            return true;
        } catch (InterruptedException e) {
            logger.warn("performTasks: " + e.getMessage(), e);
            return false;
        }
    }
    
    private boolean write() throws IOException {
        boolean completed = true;
        List<JMap> completedArticleList = new ArrayList();
        for (ArticleTask articleTask : articleTaskList) {
            if (articleTask.isCompleted()) {
                completedArticleList.add(articleTask.map);
            } else if (articleTask.isRetry()) {
                completed = false;
            } else {
            }
        }        
        if (completedArticleList.isEmpty()) {
            logger.error("empty completed article list");
            return false;
        } else if (completedArticleList.size() < articleTaskList.size()/2) {
            logger.error("too few articles completed");
            return false;
        }
        try {
            context.storage.putJson(String.format("%s/articles.json", section), 
                    new Gson().toJson(completedArticleList));
            context.storage.buildPrefetchContent();
            return completed;
        } catch (Throwable e) {
            logger.error("write", e);
            return false;            
        }
    }       
}
