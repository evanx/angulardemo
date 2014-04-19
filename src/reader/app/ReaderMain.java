package reader.app;

import gittery.GitteryContext;
import iolfeed.FeedsTask;
import gittery.GitteryServer;
import iolfeed.ContentStorage;
import iolfeed.FeedsContext;
import iolfeed.TaskManager;
import org.apache.log4j.BasicConfigurator;

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
                    "/home/evanx/NetBeansProjects/git/angulardemo/src/reader/web"));
            new FeedsTask().start(new FeedsContext(taskManager, contentStorage, true, 4));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }    
}