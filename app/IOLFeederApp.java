package app;


import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.jx.JMap;

/**
 *
 * @author evanx
 */
public class IOLFeederApp implements Runnable {

    static Logger logger = LoggerFactory.getLogger(IOLFeederApp.class);
    
    Map<String, String> feedMap = new HashMap();
    ScheduledExecutorService elapsedExecutorService = Executors.newSingleThreadScheduledExecutor();
    
    void start() throws Exception {
        put("business", "http://www.iol.co.za/cmlink/1.730910");
        put("news", "http://iol.co.za/cmlink/1.640");
        put("sport", "http://iol.co.za/cmlink/sport-category-rss-1.704");
        if (false) {
            put("multimedia", "http://iol.co.za/cmlink/1.738");
        }
        if (true) {
            elapsedExecutorService.scheduleAtFixedRate(this, 1, 3600, TimeUnit.SECONDS);
        } else {
            run();
        }
    }
    
    void put(String name, String url) {
        feedMap.put(name, url);       
    }

    @Override
    public void run() {
        logger.info("user.dir {}", System.getProperty("user.dir"));
        for (String key : feedMap.keySet()) {
            String feedUrl = feedMap.get(key);
            try {
                List<JMap> articleList = new IOLFeeder().list(3, feedUrl);
                StringBuilder json = new StringBuilder();
                for (JMap map : articleList) {
                    if (json.length() > 0) {
                        json.append(",\n");
                    }
                    json.append("  " + map.toJson());
                }
                json.insert(0, "[\n");
                json.append("\n]\n");
                logger.info("json {}", json);
                File file = new File(key + ".json");
                logger.info("file {}", file.getAbsolutePath());
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(json.toString());
                }
            } catch (Exception e) {
                logger.warn("run", e);
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
        try {
            BasicConfigurator.configure();
            IOLFeederApp app = new IOLFeederApp();
            app.start();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
