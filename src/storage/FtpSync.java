package storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.net.ftp.FtpClient;
import sun.net.ftp.FtpClientProvider;
import sun.net.ftp.FtpDirEntry;
import sun.net.ftp.FtpProtocolException;
import vellum.data.Millis;
import vellum.jx.JConsoleMap;
import vellum.jx.JMap;
import vellum.jx.JMapException;
import vellum.monitor.TimestampedMonitor;
import vellum.monitor.Tx;
import vellum.util.Lists;

/**
 *
 * @author evanx
 */
public class FtpSync implements Runnable {

    Logger logger = LoggerFactory.getLogger(FtpSync.class);

    Deque<String> pathDeque = new ArrayDeque();
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    ScheduledFuture future;
    long initialDelay = Millis.fromSeconds(30);
    long delay = Millis.fromSeconds(15);
    JMap properties;
    int port = 21;
    String hostname;
    String username;
    char[] password;
    Deque<StorageItem> deque = new ArrayDeque();
    boolean cancelled = false;
    String storageDir;
    FtpClient ftpClient;
    boolean enabled;
    Set<String> articleIdSet = new HashSet();
    Set<String> existingDirs = new HashSet();
    TimestampedMonitor monitor; 
    
    public FtpSync(TimestampedMonitor monitor, JConsoleMap properties) throws JMapException {
        this.monitor = monitor;
        logger.info("properties {}", properties);
        enabled = properties.getBoolean("enabled", true);
        if (enabled) {
            port = properties.getInt("port", port);
            hostname = properties.getString("hostname");
            username = properties.getString("username");
            password = properties.getPassword("password");
            storageDir = properties.getString("storageDir");
            logger.info("{} {}", username, storageDir);
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
        try {
            login();
            list();
            ftpClient.close();
        } catch (Exception e) {
            logger.warn("initSchedule", e.getMessage());
        }
    }

    private void login() throws Exception {
        logger.info("login {} {}", username, storageDir);
        ftpClient = FtpClientProvider.provider().createFtpClient();
        ftpClient.connect(new InetSocketAddress(hostname, port));
        ftpClient.login(username, password);
    }

    @Override
    public synchronized void run() {        
        if (deque.isEmpty()) {
            logger.info("empty");
            return;
        }
        Tx tx = monitor.begin("FtpSync");
        try {
            login();
            while (!deque.isEmpty()) {
                StorageItem item = deque.peek();
                sync(item);
                deque.remove(item);
            }
            ftpClient.close();
            tx.ok();
        } catch (Exception e) {
            tx.error(e);            
        } finally {
            tx.fin();
        }
    }

    void ensureDirectory(final String path) throws IOException, FtpProtocolException {
        try {
            if (!existingDirs.contains(path)) {
                logger.info("ensureDirectory check {}", path);
                ftpClient.makeDirectory(path);
                logger.info("ensureDirectory created {}", path);
                existingDirs.add(path);
            }
        } catch (IOException | FtpProtocolException e) {
            if (e.getMessage().contains("exists")) {
                logger.info("ensureDirectory exists {}", path);
                existingDirs.add(path);
            } else {
                throw e;
            }
        }
    }
    
    void ensureDirectoryPath(final String path) throws IOException, FtpProtocolException {
        int fromIndex = path.indexOf('/');
        while (fromIndex > 0) {
            logger.info("ensureDirectoryPath {} {}", path, fromIndex);
            int index = path.indexOf('/', fromIndex + 1);
            if (index > 0) {
                String dir = path.substring(0, index);
                ensureDirectory(dir);
                fromIndex = index;
            } else {
                break;
            }
        }
    }
    
    private void sync(StorageItem item) {
        String path = storageDir + "/" + item.path;
        Tx tx = monitor.begin("sync", item.path);
        try {
            long size = ftpClient.getSize(path);
            if (size != item.content.length) {
                logger.info("sync changed {} {}", item, size);
                ftpClient.putFile(path, new ByteArrayInputStream(item.content));
            } else {
                logger.info("sync unchanged {} {}", item, size);
            }
            tx.ok();
        } catch (IOException | FtpProtocolException e) {
            try {
                ensureDirectoryPath(path);
                ftpClient.putFile(path, new ByteArrayInputStream(item.content));
                tx.ok();
            } catch (IOException | FtpProtocolException ex) {
                tx.error(ex);
            }
        } finally {
            tx.fin();
        }
    }

    void list() throws Exception {
        logger.info("list {} {}", username, storageDir);
        for (FtpDirEntry entry : Lists.list(ftpClient.listFiles(storageDir))) {
            logger.info("entry {}", entry);
        }
    }    
}
