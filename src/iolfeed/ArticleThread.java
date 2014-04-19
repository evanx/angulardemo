package iolfeed;


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.jx.JMap;

/**
 *
 * @author evanx
 */
public class ArticleThread extends Thread {
    static Logger logger = LoggerFactory.getLogger(ArticleThread.class);
    static Pattern pattern = Pattern.compile("^\\s*<img src=\"(/polopoly_fs/\\S*/[0-9]*.jpg)\"\\s");
    JMap map;
    Throwable exception;
    String link;
    String imageLink;

    ArticleThread(JMap map, String link) {
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
                    imageLink = "http://www.iol.co.za/" + imageLink;
                    fetchImage();
                    return;
                }
            }
        } catch (Throwable e) {
            exception = e;
        }
    }

    private void fetchImage() {
        try {
            String pubDate = map.getString("pubDate");
            logger.info("imageLink {} {}", pubDate, imageLink);
            File imageDirectory = new File(pubDate);
            URLConnection urlConnection = new URL(imageLink).openConnection();
            urlConnection.getInputStream();
        } catch (Exception e) {
            logger.warn("fetchImage " + imageLink, e);
        }
    }
}
