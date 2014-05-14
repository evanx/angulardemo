package storage;

import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.net.ftp.FtpClient;
import sun.net.ftp.FtpClientProvider;
import sun.net.ftp.FtpDirEntry;
import vellum.data.Millis;
import vellum.jx.JConsoleMap;
import vellum.jx.JMap;
import vellum.jx.JMapException;
import vellum.util.Lists;

/**
 *
 * @author evanx
 */
public class FtpSync implements Runnable {
    Logger logger = LoggerFactory.getLogger(FtpSync.class);

    Deque<String> pathDeque = new ArrayDeque();
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    long initialDelay = Millis.fromSeconds(60);
    long delay = Millis.fromSeconds(60);
    ScheduledFuture future;
    JMap properties;
    int port = 21;
    String hostname;
    String username;    
    char[] password;
    Deque<StorageItem> deque;
    boolean cancelled = false;
    String storageDir;
    FtpClient ftpClient;
    boolean enabled;
    
    public FtpSync(JConsoleMap properties, Deque<StorageItem> deque) throws JMapException {
        this.deque = deque;
        logger.info("properties {}", properties);
        enabled = properties.getBoolean("enabled", true);
        hostname = properties.getString("hostname");
        username = properties.getString("username");
        password = properties.getPassword("password");        
        storageDir = properties.getString("storageDir");
        logger.info("{} {}", username, storageDir);
    }

    public void init() throws Exception {        
        if (enabled) {
            login();
            list();
            logger.info("schedule {} {}", initialDelay, delay);
            executorService.scheduleWithFixedDelay(this, initialDelay, delay, TimeUnit.MILLISECONDS);
        }
    }    

    private void login() throws Exception {
        logger.info("login {} {}", username, storageDir);
        ftpClient = FtpClientProvider.provider().createFtpClient();
        ftpClient.connect(new InetSocketAddress(hostname, port));
        ftpClient.login(username, password);
    }
    
    @Override
    public void run() {
        try {
            while (!deque.isEmpty()) {
                logger.info("poll {}", deque.poll());
            }
        } catch (Exception e) {
            logger.warn("run", e);
        }
    }
    
    void list() throws Exception {
        logger.info("list {} {}", username, storageDir);
        for (FtpDirEntry entry : Lists.list(ftpClient.listFiles(storageDir))) {
            logger.info("entry {}", entry);
        }
    }    
}
