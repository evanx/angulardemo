
package iolfeed;

import vellum.jx.JMap;

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
    
    public JMap map() {
        JMap map = new JMap();
        map.put("link", source);
        map.put("title", title);
        return map;
    }
    
    @Override
    public String toString() {
        return title;
    }
    
}
