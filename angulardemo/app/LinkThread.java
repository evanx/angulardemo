package angulardemo.app;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import vellum.jx.JMap;

/**
 *
 * @author evanx
 */
public class LinkThread extends Thread {

    static Pattern pattern = Pattern.compile("^\\s*<img src=\"(/polopoly_fs/\\S*/[0-9]*.jpg)\"\\s");
    JMap map;
    Throwable exception;
    String link;
    String imageLink;

    LinkThread(JMap map, String link) {
        this.map = map;
        this.link = link;
    }

    @Override
    public void run() {
        try {
            URLConnection connection = new URL(link).openConnection();
            connection.setDoOutput(false);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    imageLink = matcher.group(1);
                    return;
                }
            }
        } catch (Throwable e) {
            exception = e;
        }
    }
}
