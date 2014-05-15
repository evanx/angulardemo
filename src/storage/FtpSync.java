package storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
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
import sun.net.ftp.FtpClientProvider;
import sun.net.ftp.FtpDirEntry;
import sun.net.ftp.FtpProtocolException;
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
        port = properties.getInt("port", port);
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
            ftpClient.close();
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
            login();
            while (!deque.isEmpty()) {
                StorageItem item = deque.peek();
                upload(item);
                deque.remove(item);
            }
            ftpClient.close();
        } catch (Exception e) {
            logger.warn("run", e);
        }
    }

    void upload(StorageItem item) throws Exception {
        logger.info("upload {}", item);
        Deque<String> parentDirList = new ArrayDeque();
        String path = item.path;
        while (true) {
            int index = path.lastIndexOf('/');
            if (index > 0) {
                path = path.substring(0, index);
                parentDirList.add(path);
            } else {
                break;
            }
        }
        while (!parentDirList.isEmpty()) {
            String dir = parentDirList.removeLast();
            try {
                ftpClient.makeDirectory(storageDir + '/' + dir);
            } catch (IOException | FtpProtocolException e) {
                if (!e.getMessage().contains("exists")) {
                    logger.warn("makeDirectory {} {}", dir, e.getMessage());
                }
            }
        }
        path = storageDir + '/' + item.path;
        ftpClient.putFile(path, new ByteArrayInputStream(item.content));
    }

    void list() throws Exception {
        logger.info("list {} {}", username, storageDir);
        for (FtpDirEntry entry : Lists.list(ftpClient.listFiles(storageDir))) {
            logger.info("entry {}", entry);
        }
    }
}
