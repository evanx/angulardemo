package storage;

import com.google.gson.JsonSyntaxException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.zip.GZIPOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Signal;
import sun.misc.SignalHandler;
import vellum.exception.ParseException;
import vellum.jx.JMap;
import vellum.jx.JMapException;
import vellum.jx.JMaps;
import vellum.monitor.TimestampedMonitor;
import vellum.util.Streams;

/**
 *
 * @author evanx
 */
public class ContentStorage {

    Logger logger = LoggerFactory.getLogger(ContentStorage.class);
    final String prefetchLinkPattern = "<!--link-->\n";
    static final String[] sections = {
        "top", "news", "sport", "business", "scitech", "lifestyle", "motoring", "tonight", "travel", 
        "backpage", "multimedia", "videos"
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
    public final String storagePath = "storage";
    public final String defaultPath = "index.html";
    public final String prefetchPath = "prefetch.html";
    public byte[] prefetchContent;
    public byte[] prefetchGzippedContent;
    File prefetchFile;
    Set<String> linkSet = new ConcurrentSkipListSet();
    boolean evict = false;
    Deque<StorageItem> deque;
    FtpSyncManager ftpSync;
    TimestampedMonitor monitor;
    Map<String, SectionEntity> sectionItemMap = new HashMap();
    
    public ContentStorage(TimestampedMonitor monitor, JMap properties) throws JMapException, IOException, ParseException {
        this.monitor = monitor;
        logger.info("properties: " + properties);
        contentUrl = properties.getString("contentUrl", "http://chronica.co");
        storageDir = properties.getString("storageDir", "/home/evanx/angulardemo/storage");
        appDir = properties.getString("appDir", "/home/evanx/angulardemo/app");
        caching = properties.getBoolean("caching", false);
        refresh = properties.getBoolean("refresh", false);
        ftpSync = new FtpSyncManager(monitor, properties.getMap("ftpSync"));
        if (ftpSync.isEnabled()) {
            deque = ftpSync.getDeque();
        }
        defaultHtml = Streams.readString(new File(appDir, defaultPath));
        prefetchFile = new File(storageDir, prefetchPath);
    }

    public void start() throws Exception {
        if (ftpSync.isEnabled()) {
            ftpSync.start();
            deque = ftpSync.getDeque();
        }
        prefetchFile.delete();
        for (String section : sections) {
            try {
                loadSection(section);
            } catch (Exception e) {
                logger.error("loadJson " + section, e);
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

    public void sync() {
        if (ftpSync.isEnabled()) {
            ftpSync.run();
        }
    }
            
    private void loadSection(String sectionName) {
        String path = String.format("%s/articles.json", sectionName);
        logger.info("section {} {}", sectionName, path);        
        File file = new File(storageDir, path);
        if (file.exists()) {
            try {
                byte[] bytes = Streams.readBytes(file);
                map.put(path, bytes);
                String json = new String(bytes);                
                SectionEntity sectionItem = getSection(sectionName);
                if (json.startsWith("[")) {
                    sectionItem.addAll(JMaps.listMap(json));
                } else if (json.startsWith("{")) {
                    sectionItem.addAll(JMaps.parse(json).getList("articles"));
                }                        
                logger.info("loadSection {}", sectionItem);
                for (JMap articleMap : sectionItem.articleList) {
                    logger.info("loadSection {} {}", sectionName, articleMap.get("articleId"));
                }
            } catch (JsonSyntaxException | IOException | IllegalStateException | JMapException e) {
                e.printStackTrace(System.err);
                String errorMessage = e.getMessage();
                if (errorMessage.length() > 99) {
                    errorMessage = errorMessage.substring(0, 99);
                }
                logger.warn("loadSection {} {}", path, errorMessage);
            }
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
        writeContent(path, content);
        if (path.endsWith(".json")) {
            writeContent(path + "p", buildJsonp(path, content));
            if (ftpSync.isEnabled()) {
                if (deque == null) {
                    logger.warn("putContent: deque is null");
                } else {
                    deque.add(new StorageItem(path, content));
                    logger.info("putContent: Ftp deque {}", deque.size());
                }
            }
        }
    }

    void writeContent(String path, byte[] content) throws IOException {
        File file = new File(storageDir, path);
        file.getParentFile().mkdirs();
        if (file.exists() && file.length() == content.length) {
            logger.info("unchanged {}", path);
        } else {
            Streams.write(content, file);
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
    
    static byte[] buildJsonp(String path, byte[] content) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("jsonpCallback('%s', ", path));
        builder.append(new String(content));
        builder.append(");");
        return builder.toString().getBytes();
    }

    public void putSection(String sectionName, List<JMap> articleList) throws IOException, JMapException {
        String path = String.format("%s/articles.json", sectionName);
        SectionEntity section = getSection(sectionName);
        if (articleList.isEmpty()) {
            logger.error("putSection empty", sectionName);
        } else {
            section.addAll(articleList);
            putJson(path, section.map());
        }
    }    
    
    private SectionEntity getSection(String section) {
        SectionEntity sectionItem = sectionItemMap.get(section);
        if (sectionItem == null) {
            sectionItem = new SectionEntity(section);
            sectionItemMap.put(section, sectionItem);
        }
        return sectionItem;
    }
}
