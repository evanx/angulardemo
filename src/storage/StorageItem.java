package storage;

/**
 *
 * @author evanx
 */
public class StorageItem {
    String path;
    byte[] content;
    long cacheSeconds;

    StorageItem(String path, byte[] content, long cacheSeconds) {
        this.path = path;
        this.content = content;
        this.cacheSeconds = cacheSeconds;
    }

    @Override
    public String toString() {
        return String.format("%s %ds %d", path, cacheSeconds, content.length);
    }
    
    
}
