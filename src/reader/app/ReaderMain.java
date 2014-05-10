package reader.app;

import gittery.GitteryContext;
import iolfeed.FeedsTask;
import gittery.GitteryServer;
import storage.ContentStorage;
import iolfeed.FeedsContext;
import iolfeed.TaskManager;
import org.apache.log4j.BasicConfigurator;
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
            ContentStorage contentStorage = new ContentStorage(JMaps.map("storage", System.getProperties()));
            contentStorage.init();
            TaskManager taskManager = new TaskManager();
            JMap feedsProperties = JMaps.map("feeds", System.getProperties());
            FeedsContext feedsContext = new FeedsContext(taskManager, contentStorage, feedsProperties);
            GitteryContext gitteryContext = new GitteryContext(contentStorage, "reader/web", "index.html",
                    "https://raw.githubusercontent.com/evanx/angulardemo/master/src/reader/web");
            gitteryContext.init();
            new GitteryServer().start(gitteryContext);
            VellumProvider.provider.put(feedsContext);
            VellumProvider.provider.put(contentStorage);
            VellumProvider.provider.put(gitteryContext);
            feedsContext.init();
            new FeedsTask().start(feedsContext);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }    
}
