package iolfeed;

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
import vellum.jx.JMapException;
import vellum.util.Streams;

/**
 *
 * @author evanx
 */
public class ArticleTask implements Runnable {

    static Logger logger = LoggerFactory.getLogger(ArticleTask.class);
    static final Pattern imageLinkPattern
            = Pattern.compile("^\\s*<img src=\"(/polopoly_fs/\\S*/[0-9]*.jpe?g)\"\\s*");
    static final Pattern galleryImageLinkPattern
            = Pattern.compile("^\\s*<a href=\"(/polopoly_fs/\\S*/landscape_600/[0-9]*.jpe?g)\">\\s*");
    static final Pattern imageCreditPattern
            = Pattern.compile("<p class=\"captions_credit_article\">(.*)</p>");
    static final Pattern imageCaptionPattern
            = Pattern.compile("<p class=\"captions\">(.*)</p>");
    static final Pattern paragraphPattern
            = Pattern.compile("<p class=\"arcticle_text\">(.*)</p>");

    JMap map;
    Throwable exception;
    String sourceArticleUrl;
    String sourceImageUrl;
    String articleId;
    String articlePath;
    String imagePath;
    String imageCredit;
    String imageCaption;
    String section;
    String numDate;
    FeedsContext context = FeedsProvider.getContext();
    ContentStorage storage = FeedsProvider.getStorage();
    List<String> paragraphs = new ArrayList();
    List<String> imageList = new ArrayList();
    boolean completed = false;

    ArticleTask(JMap map) {
        this.map = map;
    }

    public void init() throws JMapException {
        sourceArticleUrl = map.getString("link");
        numDate = map.getString("numDate");
        section = map.getString("section");
        articleId = sourceArticleUrl;
        int index = articleId.lastIndexOf("/");
        if (index > 0) {
            articleId = articleId.substring(index + 1);
            index = articleId.lastIndexOf("-");
            if (index > 0) {
                articleId = articleId.substring(0, index);
            }
        }
        articlePath = String.format("%s/articles/%s/%s.json", numDate, section, articleId);
    }

    @Override
    public void run() {
        try {
            URLConnection connection = new URL(sourceArticleUrl).openConnection();
            connection.setDoOutput(false);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (matchGalleryImageLink(galleryImageLinkPattern.matcher(line))) {
                } else if (matchImageLink(imageLinkPattern.matcher(line))) {
                } else if (matchCaption(imageCaptionPattern.matcher(line))) {
                } else if (matchCaptionCredit(imageCreditPattern.matcher(line))) {
                } else if (matchParagraph(paragraphPattern.matcher(line))) {
                } else {
                }
            }
            store();
            completed = true;
        } catch (Throwable e) {
            logger.error("run: " + e.getMessage());
            exception = e;
        }
    }

    public boolean isCompleted() {
        return completed;
    }
    
    private boolean matchImageLink(Matcher matcher) {
        if (matcher.find()) {
            sourceImageUrl = matcher.group(1);
            return true;
        }
        return false;
    }

    private boolean matchGalleryImageLink(Matcher matcher) {
        if (matcher.find()) {
            String galleryImageUrl = matcher.group(1);
            imageList.add(galleryImageUrl);
            return true;
        }
        return false;
    }

    private boolean matchCaptionCredit(Matcher matcher) {
        if (matcher.find()) {
            String text = matcher.group(1);
            if (text.trim().length() > 1) {
                imageCredit = FeedsUtil.cleanText(text);
            }
            return true;
        }
        return false;
    }

    private boolean matchCaption(Matcher matcher) {
        if (matcher.find()) {
            String text = matcher.group(1);
            if (FeedsUtil.isText(text)) {
                imageCaption = FeedsUtil.cleanText(text);
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

    private void store() throws IOException {
        map.put("articleId", articleId);
        map.put("articlePath", articlePath);
        map.put("paragraphs", paragraphs);
        map.put("imageCredit", imageCredit);
        map.put("imageCaption", imageCaption);
        loadImage();
        map.put("imagePath", imagePath);
        context.putJson(articlePath, map.toJson());
        context.putJson(String.format("articles/%s.json", articleId), map.toJson());
    }

    private void loadImage() throws IOException {
        if (imageList.size() > 0) {
            sourceImageUrl = imageList.get(0);
        }
        if (sourceImageUrl != null) {
            imagePath = loadImage(sourceImageUrl);
        }
    }

    private String loadImage(String sourceImageUrl) throws IOException {
        sourceImageUrl = "http://www.iol.co.za" + sourceImageUrl;
        byte[] content = Streams.readContent(sourceImageUrl);
        logger.info("content {} {}", content.length, sourceImageUrl);
        String name = Streams.parseFileName(sourceImageUrl);
        String path = numDate + "/images/" + name;
        context.putContent(path, content);
        return path;
    }
    
    void postContent(String path, byte[] content) throws IOException {
        String localImageUrl = String.format("%s/%s", context.contentUrl, path);
        Streams.postHttp(content, new URL(localImageUrl));
        content = Streams.readContent(localImageUrl);
        logger.info("imageUrl {} {}", content.length, localImageUrl);
    }
}
