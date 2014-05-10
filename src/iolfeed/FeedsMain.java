package iolfeed;

import storage.ContentStorage;
import org.apache.log4j.BasicConfigurator;
import vellum.jx.JMaps;

/**
 *
 * @author evanx
 */
public class FeedsMain {
    
    public static void main(String[] args) throws Exception {
        try {
            BasicConfigurator.configure();
            ContentStorage storage = new ContentStorage(JMaps.map("storage", System.getProperties()));
            new FeedsTask().start(new FeedsContext(
                    new TaskManager(), 
                    storage,
                    JMaps.map("feeds", System.getProperties())));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
