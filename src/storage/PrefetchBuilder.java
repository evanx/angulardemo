package storage;

import storage.ContentStorage;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author evanx
 */
public class PrefetchBuilder {
    static Logger logger = LoggerFactory.getLogger(PrefetchBuilder.class);

    ContentStorage storage;
    StringBuilder contentBuilder = new StringBuilder();
    String[] sections = {"top", "news", "sport", "business", "scitech", "lifestyle", "motoring", "tonight", "travel"};
    int sectionLimit = 1;
    
    public PrefetchBuilder() {
    }

    public byte[] build(ContentStorage storage) throws IOException {
        this.storage = storage;
        contentBuilder = new StringBuilder(storage.defaultHtml);
        int index = contentBuilder.indexOf("</body>");
        if (index > 0) {
            contentBuilder.insert(index, formatPrefetchLinks());
        }
        contentBuilder.append("<script>\n");
        int sectionCount = 0;
        for (String section : sections) {
            String key = String.format("%s/articles.json", section);
            byte[] content = storage.map.get(key);
            if (content != null) {
                append(section, content);
            }
            if (++sectionCount == sectionLimit) break;
        }
        contentBuilder.append("console.log('sections', sections);\n");
        contentBuilder.append("</script>\n");
        return contentBuilder.toString().getBytes();
    }
    
    private void append(String key, byte[] content) {
        contentBuilder.append(String.format("sections['%s'] = {", key));
        contentBuilder.append(" articles: ");
        contentBuilder.append(new String(content));
        contentBuilder.append("};\n");
    }

    private String formatPrefetchLinks() {
        StringBuilder linkBuilder = new StringBuilder();
        for (String link : storage.linkSet) {
            linkBuilder.append(String.format("<link rel='prefetch' href='%s'>\n", link));
        }
        return linkBuilder.toString();
    }
    
}
