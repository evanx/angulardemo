
package iolfeed;

/**
 *
 * @author evanx
 */
public class RelatedArticleItem {
    String title;
    String source;
    String path;

    public RelatedArticleItem(String source, String title) {
        this.source = source;
        this.title = title;
    }
    
    @Override
    public String toString() {
        return title;
    }
    
}
