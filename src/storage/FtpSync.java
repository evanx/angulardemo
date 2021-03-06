package storage;

import iolfeed.FeedException;
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
import vellum.exception.ParseException;
import vellum.jx.JConsoleMap;
import vellum.jx.JMapsException;
import vellum.monitor.TimestampedMonitor;
import vellum.monitor.Tx;
import vellum.util.Args;
import vellum.util.Lists;
import vellum.util.MimeTypes;

/**
 *
 * @author evanx
 */
public class FtpSync implements Runnable {

    Logger logger = LoggerFactory.getLogger(FtpSync.class);

    FtpSyncManager manager;
    Deque<String> pathDeque = new ArrayDeque();
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    ScheduledFuture future;
    long initialDelay = Millis.fromSeconds(30);
    long delay = Millis.fromSeconds(15);
    long connectTimeout;
    long readTimeout;
    int warningSize = 100;
    int port = 21;
    String id;
    String hostname;
    String username;
    char[] password;
    String storageDir;
    Deque<StorageItem> deque = new ArrayDeque();
    boolean cancelled = false;
    FtpClient ftpClient;
    boolean enabled;
    Set<String> articleIdSet = new HashSet();
    Set<String> existingDirs = new HashSet();
    TimestampedMonitor monitor;
    Tx tx;

    public FtpSync(FtpSyncManager manager, JConsoleMap properties) throws JMapsException, ParseException {
        this.manager = manager;
        monitor = manager.monitor;
        enabled = properties.getBoolean("enabled", true);
        if (enabled) {
            port = properties.getInt("port", port);
            id = properties.getString("id");
            hostname = properties.getString("hostname");
            username = properties.getString("username");
            password = properties.getPassword("password");
            storageDir = properties.getString("storageDir");
            connectTimeout = properties.getMillis("connectTimeout");
            readTimeout = properties.getMillis("readTimeout");
            logger = LoggerFactory.getLogger("FtpSync:" + id);
            logger.info("properties {}", properties);
            logger.info("{} {}", username, hostname);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Deque<StorageItem> getDeque() {
        return deque;
    }

    public void start() throws Exception {
        try {
            login();
            listApp();
        } catch (Exception e) {
            logger.warn("initSchedule", e.getMessage());
        } finally {
            close();
        }
        logger.info("schedule {} {}", initialDelay, delay);
        future = executorService.scheduleWithFixedDelay(this, initialDelay, delay, TimeUnit.MILLISECONDS);
    }

    public void shutdown() throws Exception {
        executorService.shutdown();
    }
    
    private void login() throws Exception {
        logger.info("login {} {}", username, storageDir);
        if (ftpClient != null) {
            logger.warn("login: ftpClient not null");
        }
        ftpClient = FtpClientProvider.provider().createFtpClient();
        ftpClient.setConnectTimeout((int) connectTimeout);
        ftpClient.setReadTimeout((int) readTimeout);
        ftpClient.connect(new InetSocketAddress(hostname, port));
        ftpClient.login(username, password);
    }

    @Override
    public void run() {
        if (deque.isEmpty()) {
            logger.info("empty");
            return;
        }
        if (tx != null) {
            logger.error("still running");
            return;
        }
        tx = monitor.begin("FtpSync", id);
        if (deque.size() > warningSize) {
            tx.warnf("size %d", deque.size());
        }
        try {
            login();
            handle();
            tx.ok();
        } catch (RuntimeException e) {
            tx.error(e);
        } catch (Exception e) {
            tx.error(e);
        } finally {
            tx.fin();
            tx = null;
            close();
        }
    }

    void handle() throws Exception {
        while (!deque.isEmpty()) {
            StorageItem item = deque.peek();
            if (item == null) {
                throw new FeedException("queue inconsistency");
            } else {
                handle(item);
                deque.remove(item);
            }
        }
    }

    void handle(StorageItem item) {
        handle0(item);
        if (!item.path.startsWith("storage/")) {
            handle0(new StorageItem("storage/" + item.path, item.content, item.cacheSeconds));
        }
    }

    void handle0(StorageItem item) {
        logger.info("handle {}", item);
        if (item.path.endsWith("/articles.json")) {
            upload(item);
            upload(buildJsonp(item));
        } else {
            sync(item);
            sync(buildJsonp(item));
        }
    }

    static StorageItem buildJsonp(StorageItem item) {
        byte[] content = ContentStorage.buildJsonp(item.path, item.content);
        return new StorageItem(item.path + "p", content, item.cacheSeconds);
    }

    void close() {
        try {
            if (ftpClient == null) {
                logger.warn("close: ftpClient is null");
            } else {
                ftpClient.close();
                ftpClient = null;
            }
        } catch (IOException e) {
            logger.warn("close", e.getMessage());
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
        int index = path.lastIndexOf('/');
        if (!existingDirs.contains(path.substring(0, index))) {
            int fromIndex = path.indexOf('/');
            while (fromIndex > 0) {
                index = path.indexOf('/', fromIndex + 1);
                if (index > 0) {
                    String dir = path.substring(0, index);
                    ensureDirectory(dir);
                    fromIndex = index;
                } else {
                    break;
                }
            }
        }
    }

    private void move(StorageItem item, String newPath) throws FtpProtocolException, IOException {
        ensureDirectoryPath(newPath);
        ftpClient.rename(item.path, newPath);
    }

    private void sync(StorageItem item) {
        String path = storageDir + "/" + item.path;
        Tx sub = monitor.begin("sync", id, item.path);
        try {
            long size = ftpClient.getSize(path);
            if (size != item.content.length) {
                logger.info("sync changed {} {}", item, size);
                ftpClient.putFile(path, new ByteArrayInputStream(item.content));
            } else {
                logger.info("sync unchanged {} {}", item, size);
            }
            sub.ok();
        } catch (IOException | FtpProtocolException e) {
            try {
                ensureDirectoryPath(path);
                ftpClient.putFile(path, new ByteArrayInputStream(item.content));
                sub.ok();
            } catch (IOException | FtpProtocolException ex) {
                sub.error(ex);
            }
        } finally {
            sub.fin();
        }
    }

    private void upload(StorageItem item) {
        String path = storageDir + "/" + item.path;
        Tx tx = monitor.begin("upload", id, item.path);
        try {
            ensureDirectoryPath(path);
            ftpClient.putFile(path, new ByteArrayInputStream(item.content));
            tx.ok();
        } catch (IOException | FtpProtocolException ex) {
            tx.error(ex);
        } finally {
            tx.fin();
        }
    }

    void listApp() throws Exception {
        logger.info("list {} {}", username, storageDir);
        for (FtpDirEntry entry : Lists.list(ftpClient.listFiles(storageDir))) {
            if (MimeTypes.getContentType(entry.getName(), null) != null) {
                logger.info("entry {}", Args.format(entry.getName(), entry.getSize(), entry.getLastModified()));
            }
        }
    }
}
