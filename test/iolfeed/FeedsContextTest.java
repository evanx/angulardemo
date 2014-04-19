/*
 */

package iolfeed;

import java.text.DateFormat;
import java.text.ParseException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import java.text.SimpleDateFormat;
import java.util.Date;
import junit.framework.Assert;

/**
 *
 * @author evanx
 */
public class FeedsContextTest {

    FeedsContext context = new FeedsContext(new ContentStorage());

    public FeedsContextTest() {
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
    public void dateFormat() throws ParseException {
    }        
}
