package iolfeed;

import org.apache.log4j.BasicConfigurator;

/**
 *
 * @author evanx
 */
public class FeedsMain {
    
    public static void main(String[] args) throws Exception {
        try {
            BasicConfigurator.configure();
            new FeedsTask().start(new FeedsContext(
                    new TaskManager(), 
                    new ContentStorage()));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
