package iolfeed;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.zip.GZIPOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Signal;
import sun.misc.SignalHandler;
import vellum.jx.JMap;
import vellum.util.Streams;

/**
 *
 * @author evanx
 */
public class ContentStorage {
    Logger logger = LoggerFactory.getLogger(ContentStorage.class);
    final String prefetchLinkPattern = "<!--link-->\n";                
    
    Map<String, byte[]> map = new HashMap();
    Map<String, JMap> jsonMap = new HashMap();
    
    public String contentUrl = System.getProperty("storage.contentUrl", "http://chronica.co");
    public String storageDir = System.getProperty("storage.storageDir", "/home/evanx/angulardemo/storage");
    public String appDir = System.getProperty("storage.appDir", "/home/evanx/angulardemo/app");
    public String defaultHtml;
    public final String defaultPath = "index.html";
    public final String prefetchPath = "prefetch.html";
    public byte[] prefetchContent;
    public byte[] prefetchGzippedContent;
    File prefetchFile;
    Set<String> linkSet = new ConcurrentSkipListSet();
    boolean evict = false;
    
    public void init() throws IOException {        
        prefetchFile = new File(storageDir, prefetchPath);
        prefetchFile.delete();
        this.defaultHtml = Streams.readString(new File(appDir, defaultPath));
        loadContent("top/articles.json");
        loadContent("news/articles.json");
        buildPrefetchContent();
        Signal.handle(new Signal("HUP"), new SignalHandler() {
            @Override
            public void handle(Signal signal) {
                logger.info("signal {}", signal);
                evict = true;
            }
        });
    }
    
    private void loadContent(String path) throws IOException {
        map.put(path, Streams.readBytes(new File(storageDir, path)));
    }
    
    public synchronized void put(String key, byte[] value) {
        map.put(key, value);
    }
        
    public synchronized byte[] get(String key) {
        if (evict) {
            evict = false;
            map.clear();
            return null;
        }
        return map.get(key);
    }

    public synchronized void buildPrefetchContent() throws IOException {
        logger.info("buildPrefetchContent {}", linkSet.size());
        defaultHtml = Streams.readString(new File(appDir, defaultPath));
        prefetchContent = new PrefetchBuilder().build(this);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (OutputStream stream = new GZIPOutputStream(baos)) {
            stream.write(prefetchContent);
        }
        prefetchGzippedContent = baos.toByteArray();
        Streams.write(prefetchContent, prefetchFile);
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
        putContent(path, map.toJson().getBytes());
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
