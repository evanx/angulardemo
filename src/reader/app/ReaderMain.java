package reader.app;

import gittery.GitteryContext;
import iolfeed.FeedsTask;
import gittery.GitteryServer;
import storage.ContentStorage;
import iolfeed.FeedsContext;
import iolfeed.TaskManager;
import java.io.File;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.json.JsonObjectDelegate;
import vellum.jx.JMap;
import vellum.monitor.TimestampedMonitor;
import vellum.provider.VellumProvider;

/**
 *
 * @author evanx
 */
public class ReaderMain {

    public static void main(String[] args) throws Exception {
        try {
            org.apache.log4j.Logger.getRootLogger().getLoggerRepository().resetConfiguration();
            org.apache.log4j.Logger.getRootLogger().addAppender(new ConsoleAppender(
                    new PatternLayout("%d{ISO8601} %p [%c{1}] %m%n")));
            Logger logger = LoggerFactory.getLogger(ReaderMain.class);
            JsonObjectDelegate object = new JsonObjectDelegate(new File("config.json"));
            logger.info("storage {}", object.getMap("storage"));
            logger.info("feeds {}", object.getMap("feeds"));
            logger.info("webServer {}", object.getMap("webServer"));            
            JMap properties = object.getMap();
            TimestampedMonitor monitor = new TimestampedMonitor(properties.getMap("monitor"));
            ContentStorage contentStorage = new ContentStorage(monitor, properties.getMap("storage"));
            contentStorage.init();
            VellumProvider.provider.put(contentStorage);
            GitteryContext gitteryContext = new GitteryContext(contentStorage, "reader/web", "index.html", null);
            gitteryContext.init();
            VellumProvider.provider.put(gitteryContext);
            new GitteryServer().start(gitteryContext);
            TaskManager taskManager = new TaskManager();
            FeedsContext feedsContext = new FeedsContext(taskManager, contentStorage, 
                properties.getMap("feeds"));
            feedsContext.init();
            contentStorage.initSchedule();
            VellumProvider.provider.put(feedsContext);
            new FeedsTask().start(feedsContext);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }    
}
