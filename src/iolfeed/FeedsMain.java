package iolfeed;

import storage.ContentStorage;
import org.apache.log4j.BasicConfigurator;
import vellum.jx.JMap;
import vellum.jx.JMaps;
import vellum.monitor.TimestampedMonitor;

/**
 *
 * @author evanx
 */
public class FeedsMain {
    
    public static void main(String[] args) throws Exception {
        try {
            BasicConfigurator.configure();
            JMap properties = JMaps.map(System.getProperties());
            TimestampedMonitor monitor = new TimestampedMonitor(properties.getMap("monitor"));
            TaskManager taskManager = new TaskManager();
            ContentStorage storage = new ContentStorage(monitor, properties.getMap("storage"));
            new FeedsTask().start(new FeedsContext(monitor, taskManager, storage,
                    properties.getMap("feeds")));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
