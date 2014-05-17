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
            JMap monitorProperties = new JMap();
            TimestampedMonitor monitor = new TimestampedMonitor(monitorProperties);
            ContentStorage storage = new ContentStorage(monitor, 
                    JMaps.map("storage", System.getProperties()));
            new FeedsTask().start(new FeedsContext(
                    new TaskManager(), 
                    storage,
                    JMaps.map("feeds", System.getProperties())));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
