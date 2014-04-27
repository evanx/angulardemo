package reader.app;

import gittery.GitteryContext;
import iolfeed.FeedsTask;
import gittery.GitteryServer;
import iolfeed.ContentStorage;
import iolfeed.FeedsContext;
import iolfeed.TaskManager;
import org.apache.log4j.BasicConfigurator;
import vellum.jx.JMap;

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
            JMap feedsProperties = new JMap();
            FeedsContext feedsContext = new FeedsContext(taskManager, contentStorage, feedsProperties);
            feedsContext.init();
            GitteryContext gitteryContext = new GitteryContext(contentStorage, "reader/web",
                    "index.html", "/home/evanx/nb/git/angulardemo/src/reader/web",
                    "https://raw.githubusercontent.com/evanx/angulardemo/master/src/reader/web");
            gitteryContext.init();
            new GitteryServer().start(gitteryContext);
            new FeedsTask().start(feedsContext);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }    
}
