package storage;

import iolfeed.FeedException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.data.Millis;
import vellum.exception.ParseException;
import vellum.jx.JMap;
import vellum.jx.JMapsException;
import vellum.monitor.TimestampedMonitor;
import vellum.monitor.Tx;
import vellum.ssl.OpenHostnameVerifier;
import vellum.ssl.OpenTrustManager;
import vellum.ssl.SSLContexts;
import vellum.util.Streams;

/**
 *
 * @author evanx
 */
public class PostgraSync implements Runnable {

    Logger logger = LoggerFactory.getLogger(PostgraSync.class);

    FtpSyncManager manager;
    Deque<String> pathDeque = new ArrayDeque();
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    ScheduledFuture future;
    long initialDelay = Millis.fromSeconds(30);
    long delay = Millis.fromSeconds(15);
    int connectTimeout;
    int readTimeout;
    boolean enabled;
    int warningSize = 100;
    int port = 21;
    String hostname;
    String username;
    String storageDir;
    Deque<StorageItem> deque = new ArrayDeque();
    boolean cancelled = false;
    TimestampedMonitor monitor;
    Tx tx;
    SSLContext sslContext;
        
    public PostgraSync(TimestampedMonitor monitor, JMap properties) throws JMapsException, ParseException {
        this.monitor = monitor;
        enabled = properties.getBoolean("enabled", true);
        if (enabled) {
            port = properties.getInt("port", port);
            hostname = properties.getString("hostname", "localhost:8443");
            connectTimeout = (int) properties.getMillis("connectTimeout", 45000);
            readTimeout = (int) properties.getMillis("readTimeout", 30000);
            logger.info("properties {}", properties);
            logger.info("{}", hostname);
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
        sslContext = SSLContexts.create(new OpenTrustManager());
        future = executorService.scheduleWithFixedDelay(this, initialDelay, delay, TimeUnit.MILLISECONDS);
    }

    public void shutdown() throws Exception {
        executorService.shutdown();
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
        tx = monitor.begin("PostgraSync");
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
        } finally {
            tx.fin();
            tx = null;
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

    void handle(StorageItem item) throws IOException {
        String url = "https://localhost:8443/api/content/" + item.path;
        post(url, item.content);
    }
    
    public String post(String urlString, byte[] bytes) throws IOException {
        logger.trace("post {} {}", urlString, bytes.length);
        HttpsURLConnection connection = (HttpsURLConnection) new URL(urlString).openConnection();
        try {
            connection.setHostnameVerifier(new OpenHostnameVerifier());
            connection.setSSLSocketFactory(sslContext.getSocketFactory());
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Content-Length", Integer.toString(bytes.length));
            connection.setDoOutput(true);
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(bytes);
            }
            logger.info("responseCode {}", connection.getResponseCode());
            String response;
            try (InputStream inputStream = connection.getInputStream()) {
                response = Streams.readString(inputStream);
            }
            logger.info("response {}", response);
            return response.trim();
        } finally {
            connection.disconnect();
        }
    }    
}
