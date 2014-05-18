
package storage;

import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.jx.JMaps;
import vellum.util.Streams;

/**
 *
 * @author evanx
 */
public class JsonStorageTest {
    Logger logger = LoggerFactory.getLogger(JsonStorageTest.class);
    String storageDir = "/pri/angulardemo/storage";
    
    public JsonStorageTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
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
        for (String section : ContentStorage.sections) {
            String path = String.format("%s/articles.json", section);
            logger.info("section {} {}", section, path);
            loadJson(path);
        }
    }
    
    private void loadJson(String path) throws Exception {
        File file = new File(storageDir, path);
        if (file.exists()) {
            byte[] bytes = Streams.readBytes(file);
            JMaps.parse(new String(bytes));
        }
    }
    
}
