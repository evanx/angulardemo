package iolfeed;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
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
    
    File prefetchFile;
    String defaultHtml;
    public byte[] prefetchContent;
    public byte[] prefetchGzippedContent;
    public Set<String> linkSet = new ConcurrentSkipListSet();
    
    public synchronized void init(String defaultHtml, String prefetchPath) {
        this.defaultHtml = defaultHtml;        
        this.prefetchFile = new File(prefetchPath);
    }
    
    public synchronized void put(String key, byte[] value) {
        map.put(key, value);
    }
        
    public synchronized byte[] get(String key) {
        return map.get(key);
    }

    public synchronized void buildPrefetchContent() throws IOException {
        logger.info("buildPrefetchContent {}", linkSet.size());
        prefetchContent = new PrefetchBuilder().build(this);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (OutputStream stream = new GZIPOutputStream(baos)) {
            stream.write(prefetchContent);
        }
        prefetchGzippedContent = baos.toByteArray();
        Streams.write(prefetchContent, prefetchFile);
    }
}
