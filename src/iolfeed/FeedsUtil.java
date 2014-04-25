package iolfeed;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author evanx
 */
public class FeedsUtil {
    
    private static final Map<String, Character> unicodeHtmlMap = mapUnicodeHtml();
    
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
        if (text == null) return false;
        text = text.trim();
        if (text.isEmpty()) return false;
        if (text.contains("<")) return false;
        if (text.contains(">")) return false;
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
        for (String key : unicodeHtmlMap.keySet()) {
            char ch = unicodeHtmlMap.get(key);
            text = text.replaceAll(String.format("&%s;", key), "" + ch);
            text = text.replaceAll(String.format("&#%d;", (int) ch), "" + ch);
        }
        return text;
    }
    
    private static Map<String, Character> mapUnicodeHtml() {
        Map<String, Character> map = new HashMap();
        map.put("Agrave", (char) 192);
        map.put("Egrave", (char) 200);
        map.put("Igrave", (char) 204);
        map.put("Ograve", (char) 210);
        map.put("Ugrave", (char) 217);
        map.put("agrave", (char) 224);
        map.put("egrave", (char) 232);
        map.put("igrave", (char) 236);
        map.put("ograve", (char) 242);
        map.put("ugrave", (char) 249);
        map.put("Aacute", (char) 193); 
        map.put("Eacute", (char) 201);        
        map.put("Iacute", (char) 205);        
        map.put("Oacute", (char) 211);        
        map.put("Uacute", (char) 218);        
        map.put("Yacute", (char) 221);        
        map.put("aacute", (char) 225);        
        map.put("eacute", (char) 233);        
        map.put("iacute", (char) 237);
        map.put("oacute", (char) 243);        
        map.put("uacute", (char) 250);        
        map.put("yacute", (char) 253);        
        map.put("Acirc", (char) 194);
        map.put("Ecirc", (char) 202);
        map.put("Icirc", (char) 206);
        map.put("Ocirc", (char) 212);
        map.put("Ucirc", (char) 219);
        map.put("acirc", (char) 226);
        map.put("ecirc", (char) 234);
        map.put("icirc", (char) 238);
        map.put("ocirc", (char) 244);
        map.put("ucirc", (char) 251);
        map.put("Atilde", (char) 195);
        map.put("Ntilde", (char) 209);
        map.put("Otilde", (char) 213);
        map.put("atilde", (char) 227);
        map.put("ntilde", (char) 241);
        map.put("otilde", (char) 245);
        map.put("Auml", (char) 196);
        map.put("Euml", (char) 203);
        map.put("Iuml", (char) 207);
        map.put("Ouml", (char) 214);
        map.put("Uuml", (char) 220);
        map.put("Yuml", (char) 228);
        map.put("auml", (char) 235);
        map.put("euml", (char) 239);
        map.put("iuml", (char) 246);
        map.put("ouml", (char) 252);
        return map;        
    }
}
