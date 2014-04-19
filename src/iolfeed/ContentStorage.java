package iolfeed;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author evanx
 */
public class ContentStorage {
    Map<String, byte[]> map = new HashMap();
    
    public void put(String key, byte[] value) {
        map.put(key, value);
    }
    
    public byte[] get(String key) {
        return map.get(key);
    }
    
}
