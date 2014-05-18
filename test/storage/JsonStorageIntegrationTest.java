
package storage;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.jx.JMaps;
import vellum.util.Streams;

/**
 *
 * @author evanx
 */
public class JsonStorageIntegrationTest {
    Logger logger = LoggerFactory.getLogger(JsonStorageIntegrationTest.class);
    String baseUrl = "http://za.chronica.co";
    String storageDir = "/pri/angulardemo/storage";
    
    public JsonStorageIntegrationTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        BasicConfigurator.configure();
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    @Test 
    public void loadJson() throws Exception {
        String section = "top";
        URLConnection connection = new URL(String.format("%s/%s/articles.json", baseUrl, section)).openConnection();
        String content = Streams.readString(connection.getInputStream());
        logger.info(content);
    }
    
    private void loadJson(String path) throws Exception {
        File file = new File(storageDir, path);
        if (file.exists()) {
            byte[] bytes = Streams.readBytes(file);
            JMaps.parse(new String(bytes));
        }
    }    
}
