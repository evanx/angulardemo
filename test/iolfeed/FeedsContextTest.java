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
        DateFormat dateFormat = new SimpleDateFormat(context.otherTimestampFormatString);
        Assert.assertEquals(1397740583000L, dateFormat.parse("Apr 17, 2014 3:16:23 PM").getTime());
        Assert.assertEquals("Apr 17, 2014 03:16:23 PM", dateFormat.format(new Date(1397740583000L)));
    }
}
