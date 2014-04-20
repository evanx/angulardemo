/*
 */

package iolfeed;

import java.text.ParseException;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import vellum.jx.JMap;

/**
 *
 * @author evanx
 */
public class FeedsTest {

    JMap feedsProperties = new JMap();
    FeedsContext context = new FeedsContext(new TaskManager(), new ContentStorage(), feedsProperties);

    public FeedsTest() {
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
    public void parseGallery() throws ParseException {
        String line = "\t <a href=\"/polopoly_fs/iol-mot-apr20-audi-tt-concept-a-1.1678226!/image/449629179.jpg_gen/derivatives/landscape_600/449629179.jpg\">";
        Assert.assertTrue(ArticleTask.galleryImageLinkPattern.matcher(line).find());
    }        
}
