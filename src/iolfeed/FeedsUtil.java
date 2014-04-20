package iolfeed;

/**
 *
 * @author evanx
 */
public class FeedsUtil {

    public static String cleanParagraph(String text) {
        text = text.trim();
        if (text.startsWith("\"")) {
            text = text.substring(1);
        }
        if (text.endsWith("\"")) {
            text = text.substring(0, text.length() - 1);
        }
        if (text.startsWith("<strong>")) {
            text = text.substring(8);
        }
        if (text.endsWith("</strong>")) {
            text = text.substring(0, text.length() - 9);
        }
        return cleanText(text);
    }
    
    public static String cleanDescription(String text) {
        int index = text.lastIndexOf("<");
        if (index > 0) {
            text = text.substring(0, index);
        }
        index = text.lastIndexOf(">");
        if (index > 0) {
            text = text.substring(index + 1);
        }
        return cleanText(text);
    }
        
    public static String cleanText(String text) {
        text = text.replaceAll("\u003c", "<");
        text = text.replaceAll("\u003e", ">");
        text = text.replaceAll("\u0027", "'");
        //text = text.replaceAll("\\u0027", "'");
        if (false) {
            text = text.replaceAll("‘", "&lsquo;"); // left single quote
            text = text.replaceAll("’", "&rsquo;"); // right single quote
        }
        text = text.replaceAll("&nbsp;", " ");
        text = text.replaceAll("&ldquo;", "\"");
        text = text.replaceAll("&rdquo;", "\"");
        text = text.replaceAll("&lsquo;", "'");
        text = text.replaceAll("&rsquo;", "'");
        text = text.replaceAll("&#8211;", "-");
        text = text.replaceAll("&ndash;", "-");
        text = text.replaceAll("`", "'"); // backtick
        text = text.replaceAll("‘", "'"); // left single quote
        text = text.replaceAll("’", "'"); // right single quote
        text = text.replaceAll("&#8216;", "'");
        text = text.replaceAll("&#8217;", "'");
        text = text.replaceAll("\u8216;", "'");
        text = text.replaceAll("\u8217;", "'");
        //text = text.replaceAll("\\u0026#8216;", "'");
        //text = text.replaceAll("\\u0026#8217;", "'");
        text = text.replaceAll("\u8220;", "\"");
        text = text.replaceAll("\u8221;", "\"");
        text = text.replaceAll("&#8220;", "\"");
        text = text.replaceAll("&#8221;", "\"");
        //text = text.replaceAll("\\u0026#8220;", "\"");
        //text = text.replaceAll("\\u0026#8221;", "\"");
        if (false) {
            text = text.replaceAll("\"", "&quot;");
            text = text.replaceAll("'", "&#39;");
            text = text.replaceAll("<", "&lt;");
            text = text.replaceAll(">", "&gt;");
        }
        return text;
    }
}
