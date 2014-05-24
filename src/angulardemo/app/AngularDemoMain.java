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
import vellum.monitor.TimestampedMonitor;

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
            JMap properties = JMaps.map(System.getProperties());
            TaskManager taskManager = new TaskManager();
            TimestampedMonitor monitor = new TimestampedMonitor(properties.getMap("monitor"));
            ContentStorage storage = new ContentStorage(monitor, properties.getMap("storage"));
            new GitteryServer().start(new GitteryContext(storage,
                    "angulardemo/web", "index.html",
                    "https://raw.githubusercontent.com/evanx/angulardemo/master/angulardemo/web"));
            new FeedsTask().start(new FeedsContext(monitor, taskManager, storage,
                    properties.getMap("feeds")));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

}
