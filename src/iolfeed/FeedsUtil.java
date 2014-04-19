package src.iolfeed;

/**
 *
 * @author evanx
 */
public class FeedsUtil {

    public static String cleanDescription(String description) {
        description = description.replaceAll("\u003c", "<");
        description = description.replaceAll("\u003e", ">");
        int index = description.lastIndexOf("\u003c");
        if (index > 0) {
            description = description.substring(0, index);
        }
        index = description.lastIndexOf("\u003e");
        if (index > 0) {
            description = description.substring(index + 1);
        }
        description = description.replaceAll("\\u0027", "'");
        description = description.replaceAll("&#8217;", "'");
        description = description.replaceAll("\\u0026#8220;", "\"");
        description = description.replaceAll("\\u0026#8221;", "\"");
        return description;
    }
}
