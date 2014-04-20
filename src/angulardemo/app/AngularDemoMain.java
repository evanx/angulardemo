package angulardemo.app;

import gittery.GitteryContext;
import iolfeed.FeedsTask;
import gittery.GitteryServer;
import iolfeed.ContentStorage;
import iolfeed.FeedsContext;
import iolfeed.TaskManager;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.jx.JMap;

/**
 *
 * @author evanx
 */
public class AngularDemoMain {
    static Logger logger = LoggerFactory.getLogger(AngularDemoMain.class);

    public void start() throws Exception {
    }
    
    public static void main(String[] args) throws Exception {
        try {
            BasicConfigurator.configure();
            new GitteryServer().start(new GitteryContext(
                    new ContentStorage(), "angulardemo/web",
                    "https://raw.githubusercontent.com/evanx/angulardemo/master/angulardemo/web",
                "/home/evanx/NetBeansProjects/git/angulardemo/angulardemo/web"));
            JMap feedsProperties = new JMap();
            new FeedsTask().start(new FeedsContext(new TaskManager(), new ContentStorage(), feedsProperties));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    
}
