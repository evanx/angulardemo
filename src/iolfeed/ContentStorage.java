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
import vellum.util.Streams;

/**
 *
 * @author evanx
 */
public class ContentStorage {
    Logger logger = LoggerFactory.getLogger(ContentStorage.class);
    final String prefetchLinkPattern = "<!--link-->\n";                
    
    Map<String, byte[]> map = new HashMap();
    
    public String contentUrl = System.getProperty("storage.contentUrl", "http://chronica.co");
    public String contentDir = System.getProperty("storage.contentDir", "/home/evanx/angulardemo/html");
    public String defaultHtml;
    public final String defaultPath = "index.html";
    public final String prefetchPath = "prefetch.html";
    public byte[] prefetchContent;
    public byte[] prefetchGzippedContent;
    File prefetchFile;
    Set<String> linkSet = new ConcurrentSkipListSet();
    
    public void init() throws IOException {
        this.prefetchFile = new File(contentDir, prefetchPath);
        this.defaultHtml = Streams.readString(new File(contentDir, defaultPath));
    }
    
    public synchronized void put(String key, byte[] value) {
        map.put(key, value);
    }
        
    public synchronized byte[] get(String key) {
        return map.get(key);
    }

    public synchronized void buildPrefetchContent() throws IOException {
        logger.info("buildPrefetchContent {}", linkSet.size());
        defaultHtml = Streams.readString(new File(contentDir, defaultPath));
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
    
    public void putJson(String path, String json) throws IOException {
        putContent(path, json.getBytes());
    }

    public void putContent(String path, byte[] content) throws IOException {
        logger.info("putContent {} {}", path, content.length);
        put(path, content);
        File file = new File(contentDir, path);
        file.getParentFile().mkdirs();
        if (file.exists() && file.length() == content.length) {
            logger.info("unchanged {}" + path);
        } else {
            Streams.write(content, file);        
        }
    }
    
    public void postContent(String path, byte[] content) throws IOException {
        String localImageUrl = String.format("%s/%s", contentUrl, path);
        Streams.postHttp(content, new URL(localImageUrl));
        content = Streams.readContent(localImageUrl);
        logger.info("imageUrl {} {}", content.length, localImageUrl);
    }       
    
}
