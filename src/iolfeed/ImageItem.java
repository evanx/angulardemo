
package iolfeed;

import vellum.util.Args;

/**
 *
 * @author evanx
 */
public class ImageItem {
    String source;
    String text;
    String path;
    Integer width;
    Integer height;

    public ImageItem(String source) {
        this.source = source;
    }
    
    public ImageItem(String source, String text) {
        this.source = source;
        this.text = text;
    }

    @Override
    public String toString() {
        return Args.format(path, width, height);
    }
    
}
