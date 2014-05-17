package storage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.zip.GZIPOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Signal;
import sun.misc.SignalHandler;
import vellum.jx.JConsoleMap;
import vellum.jx.JMap;
import vellum.jx.JMapException;
import vellum.monitor.TimestampedMonitor;
import vellum.util.Streams;

/**
 *
 * @author evanx
 */
public class ContentStorage {

    Logger logger = LoggerFactory.getLogger(ContentStorage.class);
    final String prefetchLinkPattern = "<!--link-->\n";
    final String[] sections = {
        "top", "news", "sport", "business", "scitech", "lifestyle", "motoring", "tonight", "travel", "multimedia", "videos"
    };

    Map<String, byte[]> map = new ConcurrentHashMap();
    Map<String, JMap> jsonMap = new ConcurrentHashMap();

    public String contentUrl;
    public String storageDir;
    public String appDir;
    public boolean caching;
    public boolean refresh;
    public boolean prefetching = false;
    public String defaultHtml;
    public final String defaultPath = "index.html";
    public final String prefetchPath = "prefetch.html";
    public byte[] prefetchContent;
    public byte[] prefetchGzippedContent;
    File prefetchFile;
    Set<String> linkSet = new ConcurrentSkipListSet();
    boolean evict = false;
    Deque<StorageItem> deque;
    FtpSync ftpSync;
    TimestampedMonitor monitor;
    
    public ContentStorage(TimestampedMonitor monitor, JMap properties) throws JMapException {
        this.monitor = monitor;
        logger.info("properties: " + properties);
        contentUrl = properties.getString("contentUrl", "http://chronica.co");
        storageDir = properties.getString("storageDir", "/home/evanx/angulardemo/storage");
        appDir = properties.getString("appDir", "/home/evanx/angulardemo/app");
        caching = properties.getBoolean("caching", false);
        refresh = properties.getBoolean("refresh", false);
        ftpSync = new FtpSync(monitor, JConsoleMap.map(properties, "ftpClient"));
    }

    public void initCore() throws IOException {
        defaultHtml = Streams.readString(new File(appDir, defaultPath));
    }

    public void init() throws Exception {
        initCore();
        prefetchFile = new File(storageDir, prefetchPath);
        prefetchFile.delete();
        for (String section : sections) {
            String path = String.format("%s/articles.json", section);
            logger.info("section {} {}", section, path);
            loadContent(path);
            if (false) {
                linkSet.add(path);
            }
        }
        buildPrefetchContent();
        Signal.handle(new Signal("HUP"), new SignalHandler() {
            @Override
            public void handle(Signal signal) {
                logger.info("signal {}", signal);
                evict = true;
            }
        });
    }

    public void initSchedule() throws Exception {
        if (ftpSync.isEnabled()) {
            ftpSync.initSchedule();
            deque = ftpSync.getDeque();
        }
    }

    public TimestampedMonitor getMonitor() {
        return monitor;
    }
        
    public void sync() {
        ftpSync.run();
    }

    private void loadContent(String path) throws IOException {
        File file = new File(storageDir, path);
        if (file.exists()) {
            map.put(path, Streams.readBytes(file));
        }
    }

    public synchronized void put(String key, byte[] value) {
        if (caching || key.endsWith(".json")) {
            map.put(key, value);
        }
    }

    public synchronized byte[] get(String key) {
        if (evict) {
            evict = false;
            map.clear();
            return null;
        }
        if (caching) {
            return map.get(key);
        }
        return null;
    }

    public synchronized void buildPrefetchContent() throws IOException {
        logger.info("buildPrefetchContent {}", linkSet.size());
        if (prefetching) {
            defaultHtml = Streams.readString(new File(appDir, defaultPath));
            prefetchContent = new PrefetchBuilder().build(this);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (OutputStream stream = new GZIPOutputStream(baos)) {
                stream.write(prefetchContent);
            }
            prefetchGzippedContent = baos.toByteArray();
            Streams.write(prefetchContent, prefetchFile);
        }
    }

    public void addLink(String section, String path) {
        if (section.equals("top") || section.equals("news")) {
            linkSet.add(path);
        }
    }

    public JMap getMap(String path) throws IOException {
        return jsonMap.get(path);
    }

    public void putJson(String path, JMap map) throws IOException {
        jsonMap.put(path, map);
        byte[] content = map.toJson().getBytes();
        putContent(path, content);
    }

    public void putJson(String path, String json) throws IOException {
        putContent(path, json.getBytes());
    }

    public void putContent(String path, byte[] content) throws IOException {
        logger.info("putContent {} {}", path, content.length);
        put(path, content);
        File file = new File(storageDir, path);
        file.getParentFile().mkdirs();
        if (file.exists() && file.length() == content.length) {
            logger.info("unchanged {}", path);
        } else {
            Streams.write(content, file);
        }
        if (path.endsWith(".json")) {
            if (deque != null) {
                deque.add(new StorageItem(path, content));
                logger.info("Ftp deque {}", deque.size());
            } else {
                logger.warn("null deque");
            }
        }
    }

    public boolean containsKey(String path) {
        return map.containsKey(path);
    }

    public boolean exists(String path) {
        File file = new File(storageDir, path);
        return file.exists();
    }

    public void postContent(String path, byte[] content) throws IOException {
        String localImageUrl = String.format("%s/%s", contentUrl, path);
        Streams.postHttp(content, new URL(localImageUrl));
        content = Streams.readContent(localImageUrl);
        logger.info("imageUrl {} {}", content.length, localImageUrl);
    }
}
