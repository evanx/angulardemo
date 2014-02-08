
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.jx.JMap;

/**
 *
 * @author evanx
 */
public class IOLFeeder {

    static Logger logger = LoggerFactory.getLogger(GitteryApp.class);
    Map<String, String> feedMap = new HashMap();
    List<LinkThread> threadList = new ArrayList();

    private void start() throws Exception {
        feed("Business", "http://www.iol.co.za/cmlink/1.730910");
        if (false) {
            feed("News", "http://iol.co.za/cmlink/1.640");
            feed("Sport", "http://iol.co.za/cmlink/sport-category-rss-1.704");
            feed("Multimedia", "http://iol.co.za/cmlink/1.738");
        }
    }

    private void feed(String sectionLabel, String feedUrl) throws Exception {
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(new URL(feedUrl)));
        for (Object object : feed.getEntries()) {
            SyndEntryImpl entry = (SyndEntryImpl) object;
            JMap map = new JMap();
            map.put("title", entry.getTitle());
            map.put("description", cleanDescription(entry.getDescription().getValue()));
            map.put("pubDate", entry.getPublishedDate());
            map.put("link", entry.getLink());
            LinkThread linkThread = new LinkThread(map, entry.getLink());
            linkThread.start();
            threadList.add(linkThread);
        }
        List articleList = new ArrayList();
        for (LinkThread linkThread : threadList) {
            linkThread.join();
            if (linkThread.imageLink != null) {
                linkThread.map.put("image", "http://www.iol.co.za/" + linkThread.imageLink);
            }
            articleList.add(linkThread.map);
            System.out.println(linkThread.map.toJson());
        }
        JMap map = new JMap();
        map.put("section", sectionLabel);
        map.put("articles", articleList);
        System.out.println(map.toJson());
    }

    private String cleanDescription(String description) {
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

    public static void main(String[] args) throws Exception {
        try {
            BasicConfigurator.configure();
            IOLFeeder app = new IOLFeeder();
            app.start();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}

class LinkThread extends Thread {

    static Pattern pattern = Pattern.compile("^\\s*<img src=\"(/polopoly_fs/\\S*/[0-9]*.jpg)\"\\s");
    JMap map;
    Exception exception;
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
        } catch (IOException e) {
            exception = e;
        }
    }
}
