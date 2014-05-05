package iolfeed;

import storage.ContentStorage;
import org.apache.log4j.BasicConfigurator;
import vellum.jx.JMap;

/**
 *
 * @author evanx
 */
public class FeedsMain {
    
    public static void main(String[] args) throws Exception {
        try {
            BasicConfigurator.configure();
            ContentStorage storage = new ContentStorage(new JMap("storage", System.getProperties()));
            new FeedsTask().start(new FeedsContext(
                    new TaskManager(), 
                    storage,
                    new JMap("feeds", System.getProperties())));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
