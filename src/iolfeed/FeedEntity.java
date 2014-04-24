
package iolfeed;

/**
 *
 * @author evans
 */
public class FeedEntity {
    String id;
    String label;
    String url;

    public FeedEntity(String id, String label, String url) {
        this.id = id;
        this.label = label;
        this.url = url;
    }

    public String getId() {
        return id;
    }
    
    
}
