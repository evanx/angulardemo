package iolfeed;

import org.apache.log4j.BasicConfigurator;

/**
 *
 * @author evanx
 */
public class FeedMain {
    
    public static void main(String[] args) throws Exception {
        try {
            BasicConfigurator.configure();
            FeedManager app = new FeedManager();
            app.start();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
