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
import vellum.util.Streams;

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
    String imageUrl;
    FeedsContext context = FeedsProvider.getContext();
    ContentStorage storage = FeedsProvider.getStorage();
    
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
                    imageUrl = matcher.group(1);
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
            imageUrl = "http://www.iol.co.za" + imageUrl;
            byte[] content = Streams.readContent(imageUrl);
            logger.info("content {} {}", content.length, imageUrl);
            String numDate = map.getString("numDate");
            String name = Streams.parseFileName(imageUrl);
            String path = numDate + "/images/" + name;
            File file = new File(path);
            file.getParentFile().mkdirs();
            Streams.write(content, file);
            logger.info("file {} {}", file.length(), file.getCanonicalPath());
            context.storage.put(path, content);
            imageUrl = String.format("http://%s/%s", context.contentHost, path);
            Streams.postHttp(content, new URL(imageUrl));
            content = Streams.readContent(imageUrl);
            logger.info("imageUrl {} {}", content.length, imageUrl);
        } catch (Exception e) {
            logger.warn("fetchImage " + imageUrl, e);
        }
    }
}
