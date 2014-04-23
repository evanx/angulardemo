package iolfeed;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author evanx
 */
public class FastJsonBuilder {
    static Logger logger = LoggerFactory.getLogger(FastJsonBuilder.class);

    StringBuilder builder = new StringBuilder();
    String[] sections = {"top", "news", "sport", "business", "scitech", "lifestyle", "motoring", "tonight", "travel"};
    int sectionLimit = 4;
    
    public FastJsonBuilder() {
    }

    public byte[] build(ContentStorage storage, String defaultHtml) throws IOException {
        builder.append(defaultHtml);
        builder.append("<script>\n");
        int sectionCount = 0;
        for (String section : sections) {
            String key = String.format("%s/articles.json", section);
            byte[] content = storage.get(key);
            if (content != null) {
                append(section, content);
            }
            if (++sectionCount == sectionLimit) break;
        }
        builder.append("console.log('sections', sections);\n");
        builder.append("</script>\n");
        return builder.toString().getBytes();
    }
    
    private void append(String key, byte[] content) {
        builder.append(String.format("sections['%s'] = {", key));
        builder.append(" articles: ");
        builder.append(new String(content));
        builder.append("};\n");
    }
}
