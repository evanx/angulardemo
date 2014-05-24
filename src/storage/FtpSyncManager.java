package storage;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.net.ftp.FtpClient;
import vellum.data.Millis;
import vellum.exception.ParseException;
import vellum.jx.JConsoleMap;
import vellum.jx.JMap;
import vellum.jx.JMapException;
import vellum.monitor.TimestampedMonitor;
import vellum.monitor.Tx;
import vellum.system.NullConsole;

/**
 *
 * @author evanx
 */
public class FtpSyncManager implements Runnable {

    Logger logger = LoggerFactory.getLogger(FtpSyncManager.class);

    Deque<String> pathDeque = new ArrayDeque();
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    ScheduledFuture future;
    long initialDelay = Millis.fromSeconds(30);
    long delay = Millis.fromSeconds(15);
    int warningSize = 100;
    Deque<StorageItem> deque = new ArrayDeque();
    boolean cancelled = false;
    FtpClient ftpClient;
    boolean enabled;
    TimestampedMonitor monitor; 
    Tx tx;
    List<FtpSync> clients = new ArrayList();
    
    public FtpSyncManager(TimestampedMonitor monitor, JMap properties) throws JMapException, ParseException {
        this.monitor = monitor;
        logger.info("properties {}", properties);
        enabled = properties.getBoolean("enabled", true);
        if (enabled) {
            for (JMap clientProperties : properties.getListMap("clients")) {
                logger.info("client {}", properties);
                FtpSync client = new FtpSync(this, new JConsoleMap(new NullConsole(), clientProperties));
                if (client.isEnabled()) {
                    clients.add(client);
                }
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Deque<StorageItem> getDeque() {
        return deque;
    }
    
    public void start() throws Exception {
        logger.info("schedule {} {}", initialDelay, delay);
        future = executorService.scheduleWithFixedDelay(this, initialDelay, delay, TimeUnit.MILLISECONDS);
        for (FtpSync client : clients) {
            client.start();
        }
    }

    @Override
    public void run() {        
        if (deque.isEmpty()) {
            logger.info("empty");
            return;
        }
        if (tx != null) {
            logger.error("still running");
        }
        tx = monitor.begin("FtpSyncManager");
        if (deque.size() > warningSize) {
            tx.warnf("size %d", deque.size());
        }
        try {
            handle();
            tx.ok();
        } catch (RuntimeException e) {
            tx.error(e);
        } catch (Exception e) {
            tx.error(e);
        } catch (Error e) {
            tx.error(e);
        } catch (Throwable e) {
            tx.error(e);
        } finally {
            tx.fin();
            tx = null;
        }
    }
    
    private void handle() throws Exception {
        while (!deque.isEmpty()) {
            StorageItem item = deque.peek();
            if (item == null) {
                logger.warn("queue inconsistency");
            } else {
                for (FtpSync client : clients) {
                    if (client.isEnabled()) {
                        client.getDeque().add(item);
                    }
                }
                deque.remove(item);
            }
        }
    }
}
