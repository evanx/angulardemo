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
            JMap feedsProperties = new JMap();
            new FeedsTask().start(new FeedsContext(
                    new TaskManager(), 
                    new ContentStorage(),
                    feedsProperties));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
