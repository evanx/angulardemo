package storage;

/**
 *
 * @author evanx
 */
public class StorageItem {
    String path;
    byte[] content;

    StorageItem(String path, byte[] content) {
        this.path = path;
        this.content = content;
    }
}
