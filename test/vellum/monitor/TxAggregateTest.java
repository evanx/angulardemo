
package vellum.monitor;

import junit.framework.Assert;
import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import vellum.jx.JMap;
import vellum.jx.JMaps;

/**
 *
 * @author evanx
 */
public class TxAggregateTest {
    
    public TxAggregateTest() {
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
    public void testProperties() throws Exception {
        JMap properties = new JMap();
        properties.put("limit", "2s");
        properties = JMaps.parse(properties.toJson());
        Assert.assertEquals(2000, properties.getMillis("limit"));
    }
    
    @Test
    public void testSingleTransaction() throws Exception {
        JMap properties = new JMap();
        properties.put("limit", "10");
        TimestampedMonitor monitor = new TimestampedMonitor(properties);
        monitor.begin("tx1", 1);
        monitor.end("tx1", 1, 1);
        monitor.begin("tx1", 2);        
        monitor.end("tx1", 2, 20);        
        monitor.begin("tx1", 3);        
        monitor.end("tx1", 3, 5);
        monitor.run();
        Assert.assertEquals(0, monitor.expiredMap.size());
        Assert.assertEquals(1, monitor.completedMap.size());
        Assert.assertEquals(0, monitor.expiredMap.all.count);
        Assert.assertEquals(3, monitor.completedMap.all.count);
    }
    
    @Test
    public void testSingleExpired() throws Exception {
        JMap properties = new JMap();
        properties.put("limit", "10");
        long start = System.currentTimeMillis();
        TimestampedMonitor monitor = new TimestampedMonitor(properties);
        monitor.begin("tx1", 1, start);
        monitor.end("tx1", 1, 1);
        monitor.begin("tx1", 2, start + 1);        
        monitor.end("tx1", 2, 20);        
        monitor.begin("tx1", 3, start + 2);        
        monitor.run(start + 12);
        Assert.assertEquals(1, monitor.expiredMap.size());
        Assert.assertEquals(1, monitor.completedMap.size());
        Assert.assertEquals(1, monitor.expiredMap.all.count);
        Assert.assertEquals(2, monitor.completedMap.all.count);
    }

    @Test
    public void testExpired() throws Exception {
        JMap properties = new JMap();
        properties.put("limit", "10");
        long start = System.currentTimeMillis();
        TimestampedMonitor monitor = new TimestampedMonitor(properties);
        monitor.begin("tx1", 1, start);
        monitor.end("tx1", 1, 1);
        monitor.begin("tx2", 1, start + 1);        
        monitor.run(start + 12);
        Assert.assertEquals(1, monitor.expiredMap.size());
        Assert.assertEquals(1, monitor.completedMap.size());
        Assert.assertEquals(1, monitor.expiredMap.all.count);
        Assert.assertEquals(1, monitor.completedMap.all.count);
    }
 
   @Test
    public void testTransactions() throws Exception {
        JMap properties = new JMap();
        properties.put("limit", "10");
        long start = System.currentTimeMillis();
        TimestampedMonitor monitor = new TimestampedMonitor(properties);
        monitor.begin("tx1", 1, start);
        monitor.end("tx1", 1, 2);
        monitor.begin("tx1", 2, start);
        monitor.end("tx1", 2, 4);
        monitor.begin("tx2", 1, start);
        monitor.end("tx2", 1, 2);        
        monitor.begin("tx2", 2, start);
        monitor.end("tx2", 2, 6);        
        monitor.begin("tx1", 0, start);
        monitor.begin("tx2", 0, start);
        monitor.begin("tx3", 0, start);
        monitor.run(start + 12);
        Assert.assertEquals(3, monitor.expiredMap.size());
        Assert.assertEquals(2, monitor.completedMap.size());
        Assert.assertEquals(3, monitor.expiredMap.all.count);
        Assert.assertEquals(4, monitor.completedMap.all.count);
    }
     
}
