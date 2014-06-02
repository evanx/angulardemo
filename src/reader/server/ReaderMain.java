package reader.server;

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
import vellum.jx.JMap;
import vellum.jx.JMaps;
import vellum.monitor.TimestampedMonitor;
import vellum.provider.VellumProvider;
import vellum.util.Streams;

/**
 *
 * @author evanx
 */
public class ReaderMain {

    public static void main(String[] args) throws Exception {
        try {
            org.apache.log4j.Logger.getRootLogger().getLoggerRepository().resetConfiguration();
            org.apache.log4j.Logger.getRootLogger().addAppender(new ConsoleAppender(
                    new PatternLayout("%d{ISO8601} %p [%c] %m%n")));
            Logger logger = LoggerFactory.getLogger(ReaderMain.class);
            JMap properties = JMaps.parseMap(Streams.readString(new File("config.json")));
            logger.info("storage {}", properties.getMap("storage"));
            logger.info("feeds {}", properties.getMap("feeds"));
            logger.info("webServer {}", properties.getMap("webServer"));            
            TimestampedMonitor monitor = new TimestampedMonitor(properties.getMap("monitor"));
            VellumProvider.provider.put(monitor);
            ContentStorage contentStorage = new ContentStorage(monitor, properties.getMap("storage"));
            VellumProvider.provider.put(contentStorage);
            GitteryContext gitteryContext = new GitteryContext(contentStorage, "reader/web", "index.html", null);
            VellumProvider.provider.put(gitteryContext);
            new GitteryServer().start(gitteryContext);
            TaskManager taskManager = new TaskManager();
            FeedsContext feedsContext = new FeedsContext(monitor, taskManager, contentStorage, 
                properties.getMap("feeds"));
            VellumProvider.provider.put(feedsContext);
            if (feedsContext.start()) {
                contentStorage.start();
                monitor.start();
                new FeedsTask().start(feedsContext);
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }    
}
