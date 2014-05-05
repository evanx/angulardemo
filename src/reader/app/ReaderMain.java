package reader.app;

import gittery.GitteryContext;
import iolfeed.FeedsTask;
import gittery.GitteryServer;
import storage.ContentStorage;
import iolfeed.FeedsContext;
import iolfeed.TaskManager;
import org.apache.log4j.BasicConfigurator;
import vellum.jx.JMap;
import vellum.provider.VellumProvider;

/**
 *
 * @author evanx
 */
public class ReaderMain {

    public static void main(String[] args) throws Exception {
        try {
            BasicConfigurator.configure();
            ContentStorage contentStorage = new ContentStorage();
            contentStorage.init();
            TaskManager taskManager = new TaskManager();
            JMap feedsProperties = new JMap(System.getProperties());
            FeedsContext feedsContext = new FeedsContext(taskManager, contentStorage, feedsProperties);
            feedsContext.init();
            GitteryContext gitteryContext = new GitteryContext(contentStorage, "reader/web", "index.html",
                    "https://raw.githubusercontent.com/evanx/angulardemo/master/src/reader/web");
            gitteryContext.init();
            VellumProvider.provider.put(feedsContext);
            VellumProvider.provider.put(contentStorage);
            VellumProvider.provider.put(gitteryContext);
            new GitteryServer().start(gitteryContext);
            new FeedsTask().start(feedsContext);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }    
}
