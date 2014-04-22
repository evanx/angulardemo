package iolfeed;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author evanx
 */
public class ContentStorage {
    static Logger logger = LoggerFactory.getLogger(ContentStorage.class);
    
    Map<String, byte[]> map = new HashMap();
    
    private String defaultHtml;
    public byte[] fastContent;
    public byte[] fastGzippedContent;
    
    public synchronized void init(String defaultHtml) {
        this.defaultHtml = defaultHtml;        
    }
    
    public synchronized void put(String key, byte[] value) {
        map.put(key, value);
    }
        
    public synchronized byte[] get(String key) {
        return map.get(key);
    }

    public synchronized void buildFastContent() {
        try {
            fastContent = new FastJsonBuilder().build(this, defaultHtml);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (OutputStream stream = new GZIPOutputStream(baos)) {
                stream.write(fastContent);
            }
            fastGzippedContent = baos.toByteArray();
        } catch (Throwable e) {
            logger.error("prepareFastContent", e);
        }
    }
}
