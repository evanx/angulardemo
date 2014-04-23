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
            new GitteryServer().start(new GitteryContext(contentStorage, "reader/web",
                    "https://raw.githubusercontent.com/evanx/angulardemo/master/src/reader/web",
                    "/home/evanx/nb/git/angulardemo/src/reader/web"));
            JMap feedsProperties = new JMap();
            new FeedsTask().start(new FeedsContext(taskManager, contentStorage, feedsProperties));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }    
}
