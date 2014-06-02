
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
public class TimestampedMonitorTest {

    JMap properties = new JMap();
    long futureTimestamp = System.currentTimeMillis() + 9999;

    public TimestampedMonitorTest() {
        properties.put("limit", "5000");
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
        properties.put("limit", "2s");
        properties = JMaps.parseMap(properties.toJson());
        Assert.assertEquals(2000, properties.getMillis("limit"));
    }
    
    @Test
    public void testSingleTransaction() throws Exception {
        properties.put("limit", "10");
        TimestampedMonitor monitor = new TimestampedMonitor(properties);
        Tx tx;
        tx = monitor.begin("tx1", 1);
        tx.setDuration(1);
        tx = monitor.begin("tx1", 2);        
        tx.setDuration(20);        
        tx = monitor.begin("tx1", 3);        
        tx.setDuration(5);
        monitor.run();
        Assert.assertEquals(0, monitor.expiredMap.size());
        Assert.assertEquals(1, monitor.completedMap.size());
        Assert.assertEquals(0, monitor.expiredMap.all.count);
        Assert.assertEquals(3, monitor.completedMap.all.count);
    }
    
    @Test
    public void testSingleExpired() throws Exception {
        properties.put("limit", "10");
        TimestampedMonitor monitor = new TimestampedMonitor(properties);
        Tx tx;
        tx = monitor.begin("tx1", 1);
        tx.setDuration(1);
        tx = monitor.begin("tx1", 2);
        tx.setDuration(20);        
        monitor.begin("tx1", 3);
        monitor.run(futureTimestamp);
        Assert.assertEquals(1, monitor.expiredMap.size());
        Assert.assertEquals(1, monitor.completedMap.size());
        Assert.assertEquals(1, monitor.expiredMap.all.count);
        Assert.assertEquals(2, monitor.completedMap.all.count);
    }

    @Test
    public void testExpired() throws Exception {
        properties.put("limit", "5000");
        TimestampedMonitor monitor = new TimestampedMonitor(properties);
        Tx tx;
        tx = monitor.begin("tx1", 1);
        tx.setDuration(1);
        monitor.begin("tx2", 1);
        monitor.run(futureTimestamp);
        Assert.assertEquals(1, monitor.expiredMap.size());
        Assert.assertEquals(1, monitor.completedMap.size());
        Assert.assertEquals(1, monitor.expiredMap.all.count);
        Assert.assertEquals(1, monitor.completedMap.all.count);
    }
 
   @Test
    public void testTransactions() throws Exception {
        TimestampedMonitor monitor = new TimestampedMonitor(properties);
        Tx tx;
        tx = monitor.begin("tx1", 1);
        tx.setDuration(2);
        tx = monitor.begin("tx1", 2);
        tx.setDuration(4);
        tx = monitor.begin("tx2", 1);
        tx.setDuration(2);        
        tx = monitor.begin("tx2", 2);
        tx.setDuration(6);        
        monitor.begin("tx1", 0);
        monitor.begin("tx2", 0);
        monitor.begin("tx3", 0);
        monitor.run(futureTimestamp);
        Assert.assertEquals(3, monitor.expiredMap.size());
        Assert.assertEquals(2, monitor.completedMap.size());
        Assert.assertEquals(3, monitor.expiredMap.all.count);
        Assert.assertEquals(4, monitor.completedMap.all.count);
    }

   @Test
    public void testSub() throws Exception {
        TimestampedMonitor monitor = new TimestampedMonitor(properties);
        Tx tx;
        tx = monitor.begin("tx", 1);
        Tx sub = tx.sub("sub", 1);
        sub.setDuration(2);
        tx.setDuration(4);
        monitor.run(futureTimestamp);
        Assert.assertEquals(0, monitor.expiredMap.size());
        Assert.assertEquals(0, monitor.expiredMap.all.count);
        Assert.assertEquals(2, monitor.completedMap.size());
        Assert.assertEquals(2, monitor.completedMap.all.count);
    }
    
}
