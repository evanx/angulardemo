package iolfeed;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author evanx
 */
public class FeedsUtil {

    public static final Map<Character, String> unicodeHtmlMap = mapUnicodeHtml();

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

    public static boolean isText(String text) {
        if (text == null) {
            return false;
        }
        text = text.trim();
        if (text.isEmpty()) {
            return false;
        }
        if (text.contains("<")) {
            return false;
        }
        if (text.contains(">")) {
            return false;
        }
        return true;
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
        text = text.replaceAll("&mdash;", "-");
        text = text.replaceAll("`", "'"); // backtick
        text = text.replaceAll("‘", "'"); // left single quote
        text = text.replaceAll("’", "'"); // right single quote
        text = text.replaceAll("&#8216;", "'");
        text = text.replaceAll("&#8217;", "'");
        text = text.replaceAll("\u8216;", "'");
        text = text.replaceAll("\u8217;", "'");
        text = text.replaceAll("&#38;", "&");
        text = text.replaceAll("&amp;", "&");
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
        return accentHtmlToUnicode(text);
    }

    public static String accentHtmlToUnicode(String text) {
        for (char ch: unicodeHtmlMap.keySet()) {
            String htmlName = unicodeHtmlMap.get(ch);
            text = text.replaceAll(String.format("&%s;", htmlName), "" + ch);
            text = text.replaceAll(String.format("&#%d;", (int) ch), "" + ch);
        }
        return text;
    }

    private static Map<Character, String> mapUnicodeHtml() {
        Map<Character, String> map = new HashMap();
        map.put((char) 60, "lt");
        map.put((char) 62, "gt");
        map.put((char) 247, "divide");
        map.put((char) 176, "deg");
        map.put((char) 172, "not");
        map.put((char) 177, "plusmn");
        map.put((char) 181, "micro");
        map.put((char) 8734, "infin");
        map.put((char) 8776, "asymp");
        map.put((char) 8800, "ne");
        map.put((char) 8804, "le");
        map.put((char) 8805, "ge");
        map.put((char) 162, "cent");
        map.put((char) 163, "pound");
        map.put((char) 165, "yen");
        map.put((char) 8364, "euro");
        map.put((char) 171, "laquo");
        map.put((char) 187, "raquo");
        map.put((char) 8249, "lsaquo");
        map.put((char) 8250, "rsaquo");
        map.put((char) 8218, "sbquo");
        map.put((char) 8222, "bdquo");
        map.put((char) 8220, "ldquo");
        map.put((char) 8221, "rdquo");
        map.put((char) 147, "ldquo");
        map.put((char) 148, "rdquo");
        map.put((char) 8216, "lsquo");
        map.put((char) 8217, "rsquo");
        map.put((char) 145, "lsquo");
        map.put((char) 146, "rsquo");
        map.put((char) 38, "amp");
        map.put((char) 32, "nbsp");
        map.put((char) 174, "reg");
        map.put((char) 169, "copy");
        map.put((char) 153, "trade");
        map.put((char) 8482, "trade");
        map.put((char) 182, "para");
        map.put((char) 149, "bull");
        map.put((char) 8226, "bull");
        map.put((char) 183, "middot");
        map.put((char) 167, "sect");
        map.put((char) 150, "ndash");
        map.put((char) 151, "mdash");
        map.put((char) 8211, "ndash");
        map.put((char) 8212, "mdash");
        map.put((char) 192, "Agrave");
        map.put((char) 200, "Egrave");
        map.put((char) 204, "Igrave");
        map.put((char) 210, "Ograve");
        map.put((char) 217, "Ugrave");
        map.put((char) 224, "agrave");
        map.put((char) 232, "egrave");
        map.put((char) 236, "igrave");
        map.put((char) 242, "ograve");
        map.put((char) 249, "ugrave");
        map.put((char) 193, "Aacute");
        map.put((char) 201, "Eacute");
        map.put((char) 205, "Iacute");
        map.put((char) 211, "Oacute");
        map.put((char) 218, "Uacute");
        map.put((char) 221, "Yacute");
        map.put((char) 225, "aacute");
        map.put((char) 233, "eacute");
        map.put((char) 237, "iacute");
        map.put((char) 243, "oacute");
        map.put((char) 250, "uacute");
        map.put((char) 253, "yacute");
        map.put((char) 194, "Acirc");
        map.put((char) 202, "Ecirc");
        map.put((char) 206, "Icirc");
        map.put((char) 212, "Ocirc");
        map.put((char) 219, "Ucirc");
        map.put((char) 226, "acirc");
        map.put((char) 234, "ecirc");
        map.put((char) 238, "icirc");
        map.put((char) 244, "ocirc");
        map.put((char) 251, "ucirc");
        map.put((char) 195, "Atilde");
        map.put((char) 209, "Ntilde");
        map.put((char) 213, "Otilde");
        map.put((char) 227, "atilde");
        map.put((char) 241, "ntilde");
        map.put((char) 245, "otilde");
        map.put((char) 196, "Auml");
        map.put((char) 203, "Euml");
        map.put((char) 207, "Iuml");
        map.put((char) 214, "Ouml");
        map.put((char) 220, "Uuml");
        map.put((char) 228, "Yuml");
        map.put((char) 235, "auml");
        map.put((char) 239, "euml");
        map.put((char) 246, "iuml");
        map.put((char) 252, "ouml");
        return Collections.unmodifiableMap(map);
    }
}
