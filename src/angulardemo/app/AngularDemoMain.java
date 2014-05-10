package angulardemo.app;

import gittery.GitteryContext;
import iolfeed.FeedsTask;
import gittery.GitteryServer;
import storage.ContentStorage;
import iolfeed.FeedsContext;
import iolfeed.TaskManager;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.jx.JMap;
import vellum.jx.JMaps;

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
            ContentStorage storage = new ContentStorage(JMaps.map("storage", System.getProperties()));
            new GitteryServer().start(new GitteryContext(storage,
                    "angulardemo/web", "index.html",
                    "https://raw.githubusercontent.com/evanx/angulardemo/master/angulardemo/web"));
            new FeedsTask().start(new FeedsContext(new TaskManager(), storage,
                    JMaps.map("feeds", System.getProperties())));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

}
