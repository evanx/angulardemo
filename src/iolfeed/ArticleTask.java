package iolfeed;

import static iolfeed.FeedTask.logger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
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
public class ArticleTask implements Runnable {

    static Logger logger = LoggerFactory.getLogger(ArticleTask.class);
    static Pattern imageLinkPattern = Pattern.compile("^\\s*<img src=\"(/polopoly_fs/\\S*/[0-9]*.jpg)\"\\s");
    static Pattern imageCreditPattern = Pattern.compile("<p class=\"captions_credit_article\">(.*)</p>");
    static Pattern imageCaptionPattern = Pattern.compile("<p class=\"captions\">(.*)</p>");
    static Pattern paragraphPattern = Pattern.compile("<p class=\"arcticle_text\">(.*)</p>");

    JMap map;
    Throwable exception;
    String articleLink;
    String articleId;
    String articlePath;
    String sourceImageUrl;
    String imageUrl;
    String imageCredit;
    String imageCaption;
    String section;
    String numDate;
    FeedsContext context = FeedsProvider.getContext();
    ContentStorage storage = FeedsProvider.getStorage();
    List<String> paragraphs = new ArrayList();

    ArticleTask(JMap map, String articleLink) {
        this.map = map;
        this.articleLink = articleLink;
    }

    @Override
    public void run() {
        articleId = articleLink;
        int index = articleId.lastIndexOf("/");
        if (index > 0) {
            articleId = articleId.substring(index + 1);
            index = articleId.lastIndexOf("-");
            if (index > 0) {
                articleId = articleId.substring(0, index);
            }
        }
        try {
            this.numDate = map.getString("numDate");
            this.section = map.getString("section");
            articlePath = String.format("%s/articles/%s/%s", numDate, section, articleId);
            URLConnection connection = new URL(articleLink).openConnection();
            connection.setDoOutput(false);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (matchImageLink(imageLinkPattern.matcher(line))) {                    
                } else if (matchCaption(imageCaptionPattern.matcher(line))) {                    
                } else if (matchCaptionCredit(imageCreditPattern.matcher(line))) {
                } else if (matchParagraph(paragraphPattern.matcher(line))) { 
                } else {                    
                }
            }
            post();
        } catch (Throwable e) {
            exception = e;
        }
    }

    private boolean matchImageLink(Matcher matcher) {
        if (matcher.find()) {
            sourceImageUrl = matcher.group(1);
            fetchImage();
            return true;
        }
        return false;
    }

    private boolean matchCaptionCredit(Matcher matcher) {
        if (matcher.find()) {
            imageCredit = FeedsUtil.cleanText(matcher.group(1));
            return true;
        }
        return false;
    }

    private boolean matchCaption(Matcher matcher) {
        if (matcher.find()) {
            String paragraph = matcher.group(1);
            if (paragraph.trim().length() > 0) {
                imageCaption = FeedsUtil.cleanText(paragraph);
            }
            return true;
        }
        return false;
    }

    private boolean matchParagraph(Matcher matcher) {
        if (matcher.find()) {
            String paragraph = matcher.group(1);
            if (paragraph.trim().length() > 0) {
                paragraphs.add(FeedsUtil.cleanParagraph(paragraph));
            }
            return true;
        }
        return false;

    }    
    
    private void post() throws IOException {
        String articleUrl = String.format("http://%s/%s/articles/%s/%s.json", 
                context.contentHost, numDate, section, articleId);
        map.put("imageLink", imageUrl);
        map.put("imageCredit", imageCredit);
        map.put("imageCaption", imageCaption);
        map.put("articleId", articleId);
        map.put("articlePath", articlePath);
        map.put("articleUrl", articleUrl);
        map.put("paragraphs", paragraphs);
        String path = String.format("%s/articles/%s/%s.json", numDate, section, articleId);
        context.storage.put(path, map.toJson().getBytes());
        File file = new File(path);
        file.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(map.toJson());
        }        
        path = String.format("articles/%s.json", articleId);
        context.storage.put(path, map.toJson().getBytes());
        file = new File(path);
        file.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(map.toJson());
        }        
        logger.info("write file {}", file.getAbsolutePath());        
    }
    
    private void fetchImage() {
        try {
            sourceImageUrl = "http://www.iol.co.za" + sourceImageUrl;
            byte[] content = Streams.readContent(sourceImageUrl);
            logger.info("content {} {}", content.length, sourceImageUrl);
            String name = Streams.parseFileName(sourceImageUrl);
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
            logger.warn("fetchImage " + sourceImageUrl, e);
        }
    }
}
