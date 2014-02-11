package app;


import com.google.gson.Gson;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author evanx
 */
public class IOLFeederApp implements Runnable {

    static Logger logger = LoggerFactory.getLogger(GitteryApp.class);
    Map<String, String> feedMap = new HashMap();
    List<LinkThread> threadList = new ArrayList();
    ScheduledExecutorService elapsedExecutorService = Executors.newSingleThreadScheduledExecutor();
    
    void start() throws Exception {
        put("business", "http://www.iol.co.za/cmlink/1.730910");
        put("news", "http://iol.co.za/cmlink/1.640");
        put("sport", "http://iol.co.za/cmlink/sport-category-rss-1.704");
        if (false) {
            put("multimedia", "http://iol.co.za/cmlink/1.738");
        }
        if (true) {
            elapsedExecutorService.scheduleAtFixedRate(this, 0, 3600, TimeUnit.SECONDS);
        } else {
            run();
        }
    }
    
    void put(String name, String url) {
        feedMap.put(name, url);       
    }

    @Override
    public void run() {
        for (String key : feedMap.keySet()) {
            String feedUrl = feedMap.get(key);
            try {
                List articleList = new IOLFeeder().list(10, feedUrl);
                String json = new Gson().toJson(articleList);
                logger.info("json {}", json);
                File file = new File(key + ".json");
                logger.info("file {}", file.getAbsolutePath());
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(json);
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
