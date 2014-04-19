package iolfeed;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.jx.JMap;

/**
 *
 * @author evanx
 */
public class FeedsManager implements Runnable {

    static Logger logger = LoggerFactory.getLogger(FeedsManager.class);
    
    ScheduledExecutorService elapsedExecutorService = Executors.newSingleThreadScheduledExecutor();
    FeedsContext context = new FeedsContext();
    
    public FeedsManager() {
    }

    public FeedsManager(FeedsContext context) {
        this.context = context;
    }
    
    public void start() throws Exception {
        if (context.once) {
            run();
        } else {
            elapsedExecutorService.scheduleAtFixedRate(this, 1, 3600, TimeUnit.SECONDS);
        }
    }
    
    @Override
    public void run() {
        logger.info("user.dir {}", System.getProperty("user.dir"));
        for (String key : context.feedMap.keySet()) {
            String feedUrl = context.feedMap.get(key);
            try {
                List<JMap> articleList = new FeedReader(this).list(context.articleCount, feedUrl);
                StringBuilder json = new StringBuilder();
                for (JMap map : articleList) {
                    if (json.length() > 0) {
                        json.append(",\n");
                    }
                    json.append("  ").append(map.toJson());
                }
                json.insert(0, "[\n");
                json.append("\n]\n");
                logger.info("json {}", json);
                File file = new File(key + ".json");
                logger.info("write file {}", file.getAbsolutePath());
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(json.toString());
                }
            } catch (Exception e) {
                logger.warn("run", e);
            }
        }
    }
}
