
package storage;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.io.File;
import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.util.Streams;

/**
 *
 * @author evanx
 */
public class JsonStorageIntegrationTest {
    Logger logger = LoggerFactory.getLogger(JsonStorageIntegrationTest.class);
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
        String path = "top/articles.json";
        File file = new File(storageDir, path);
        if (file.exists()) {
            byte[] bytes = Streams.readBytes(file);
            String json = new String(bytes);
            JsonArray array = new JsonParser().parse(json).getAsJsonArray();
            Assert.assertTrue(array.size() == 5);
        }
    }    
}
