package reader.app;

import gittery.GitteryContext;
import iolfeed.FeedsTask;
import gittery.GitteryServer;
import storage.ContentStorage;
import iolfeed.FeedsContext;
import iolfeed.TaskManager;
import java.io.File;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.json.JsonObjectDelegate;
import vellum.jx.JMap;
import vellum.jx.JMaps;
import vellum.provider.VellumProvider;

/**
 *
 * @author evanx
 */
public class ReaderMain {

    public static void main(String[] args) throws Exception {
        try {
            BasicConfigurator.configure();
            Logger logger = LoggerFactory.getLogger(ReaderMain.class);
            JsonObjectDelegate object = new JsonObjectDelegate(new File("config.json"));
            logger.info("storage {}", object.getMap("storage"));
            logger.info("feeds {}", object.getMap("feeds"));
            logger.info("webServer {}", object.getMap("webServer"));
            ContentStorage contentStorage = new ContentStorage(JMaps.map("storage", System.getProperties()));
            contentStorage.init();
            VellumProvider.provider.put(contentStorage);
            GitteryContext gitteryContext = new GitteryContext(contentStorage, "reader/web", "index.html", null);
            gitteryContext.init();
            VellumProvider.provider.put(gitteryContext);
            new GitteryServer().start(gitteryContext);
            TaskManager taskManager = new TaskManager();
            JMap feedsProperties = JMaps.map("feeds", System.getProperties());
            FeedsContext feedsContext = new FeedsContext(taskManager, contentStorage, feedsProperties);
            feedsContext.init();
            VellumProvider.provider.put(feedsContext);
            new FeedsTask().start(feedsContext);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }    
}
