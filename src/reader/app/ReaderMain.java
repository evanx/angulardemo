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
            TaskManager taskManager = new TaskManager();
            JMap feedsProperties = new JMap();
            FeedsContext feedsContext = new FeedsContext(taskManager, contentStorage, feedsProperties);
            feedsContext.init();
            new GitteryServer().start(new GitteryContext(contentStorage, feedsContext.webResourcePath,
                    feedsContext.defaultPath, "/home/evanx/nb/git/angulardemo/src/reader/web",
                    "https://raw.githubusercontent.com/evanx/angulardemo/master/src/reader/web"));
            new FeedsTask().start(feedsContext);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }    
}
