/*
 */

package iolfeed;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.jx.JMap;
import vellum.jx.JMaps;

/**
 *
 * @author evanx
 */
public class JsonTest {
    Logger logger = LoggerFactory.getLogger(JsonTest.class);

    public JsonTest() {
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
    public void test() throws Exception {
        JMap article = new JMap();
        article.put("articleId", "test-article-id");
        article.put("image", new ImageItem("image-source", "image-text"));
        logger.info("json {}", article.toJson());
    }
}
