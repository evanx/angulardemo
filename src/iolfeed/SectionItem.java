
package iolfeed;

import java.util.List;
import vellum.jx.JMap;

/**
 *
 * @author evanx
 */
public class SectionItem {
    String id;
    String label;
    List<JMap> articles;

    public SectionItem(String id, String label) {
        this.id = id;
        this.label = label;
    }
    
    
}
