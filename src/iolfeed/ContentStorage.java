package iolfeed;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.zip.GZIPOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author evanx
 */
public class ContentStorage {
    Logger logger = LoggerFactory.getLogger(ContentStorage.class);
    final String prefetchLinkPattern = "<!--link-->\n";                
    
    Map<String, byte[]> map = new HashMap();
    
    String defaultHtml;
    public byte[] prefetchContent;
    public byte[] prefetchGzippedContent;
    public Set<String> linkSet = new ConcurrentSkipListSet();
    
    public synchronized void init(String defaultHtml) {
        this.defaultHtml = defaultHtml;        
    }
    
    public synchronized void put(String key, byte[] value) {
        map.put(key, value);
    }
        
    public synchronized byte[] get(String key) {
        return map.get(key);
    }

    public synchronized void buildPrefetchContent() {
        logger.info("buildPrefetchContent {}", linkSet.size());
        try {
            prefetchContent = new PrefetchBuilder().build(this);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (OutputStream stream = new GZIPOutputStream(baos)) {
                stream.write(prefetchContent);
            }
            prefetchGzippedContent = baos.toByteArray();
        } catch (Throwable e) {
            logger.error("prepareFastContent", e);
        }
    }
}
