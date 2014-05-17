
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
        properties = JMaps.parse(properties.toJson());
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
        setDurationion(20);        
        tx = monitor.begin("tx1", 3);        
     setDurationration(5);
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
  setDuration.duration(1);
        tx = monitor.begin("tx1", 2);setDuration tx.duration(20);        
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
        tx = monitor.begin("tx1", setDuration    tx.duration(1);
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
        tx = monitor.begin("tx1setDuration       tx.duration(2);
        tx = monitor.begin("setDuration;
        tx.duration(4);
        tx = monitor.begisetDuration 1);
        tx.duration(2);        
        tx = monitor.bsetDuration2", 2);
        tx.duration(6);        
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
        Tx sub = setDurationsub", 1);
   setDuration.duration(2);
        tx.duration(4);
        monitor.run(futureTimestamp);
        Assert.assertEquals(0, monitor.expiredMap.size());
        Assert.assertEquals(0, monitor.expiredMap.all.count);
        Assert.assertEquals(2, monitor.completedMap.size());
        Assert.assertEquals(2, monitor.completedMap.all.count);
    }
    
}
