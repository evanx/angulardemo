package reader.app;

import gittery.GitteryContext;
import iolfeed.FeedsManager;
import gittery.GitteryServer;
import iolfeed.ContentStorage;
import iolfeed.FeedsContext;
import org.apache.log4j.BasicConfigurator;

/**
 *
 * @author evanx
 */
public class ReaderServer {

    public static void main(String[] args) throws Exception {
        try {
            BasicConfigurator.configure();
            ContentStorage contentStorage = new ContentStorage();
            new GitteryServer().start(new GitteryContext(contentStorage, "reader/web",
                    "https://raw.githubusercontent.com/evanx/angulardemo/master/src/reader/web",
                    "/home/evanx/NetBeansProjects/git/angulardemo/src/reader/web"));
            new FeedsManager().start(new FeedsContext(contentStorage, true, 4));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }    
}
