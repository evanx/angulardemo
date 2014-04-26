package iolfeed;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
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

    Logger logger = LoggerFactory.getLogger(ArticleTask.class);
    static final Pattern imageLinkPattern
            = Pattern.compile("^\\s*<img src=\"(/polopoly_fs/\\S*/[0-9]*\\.jpe?g)\" .* class=\"pics\"/>");    
    static final Pattern galleryCaptionPattern
            = Pattern.compile("^\\s*<div class=\"tn3 description\">\\s*(.*)\\s*");
    static final Pattern galleryImageLinkPattern
            = Pattern.compile("^\\s*<a href=\"(/polopoly_fs/\\S*/landscape_600/[0-9]*.jpe?g)\">");
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
    String subsection;
    String numDate;    
    FeedsContext context = FeedsProvider.getContext();
    ContentStorage storage = FeedsProvider.getStorage();
    List<String> paragraphs = new ArrayList();
    List<ImageEntity> imageList = new ArrayList();
    boolean completed = false;
    boolean retry = false;
    String galleryCaption;
    
    ArticleTask(JMap map) {
        this.map = map;
    }

    public void init() throws JMapException {
        sourceArticleUrl = map.getString("link");
        articleId = sourceArticleUrl;
        int index = articleId.lastIndexOf("/");
        if (index > 0) {
            articleId = articleId.substring(index + 1);
            index = articleId.lastIndexOf("-");
            if (index > 0) {
                articleId = articleId.substring(0, index);
            }
        }
        logger = LoggerFactory.getLogger("ArticleTask." + articleId);
        numDate = map.getString("numDate");
        section = map.getString("section");
        articlePath = String.format("%s/%s/%s.json", numDate, section, articleId);
        if (section.equals("top")) {
            if (sourceArticleUrl.contains("/news/")) {
                section = "news";
            } else if (sourceArticleUrl.contains("/sport/")) {
                section = "sport";
            } else if (sourceArticleUrl.contains("/business/")) {
                section = "business";
            } else if (sourceArticleUrl.contains("/scitech/")) {
                section = "scitech";
            } else if (sourceArticleUrl.contains("/motoring/")) {
                section = "motoring";
            } else if (sourceArticleUrl.contains("/lifestyle/")) {
                section = "lifestyle";
            } else if (sourceArticleUrl.contains("/tonight/")) {
                section = "tonight";
            } else if (sourceArticleUrl.contains("/travel/")) {
                section = "travel";
            }
        }
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
                if (matchGalleryCaption(galleryCaptionPattern.matcher(line))) {
                } else if (matchGalleryImageLink(galleryImageLinkPattern.matcher(line))) {
                } else if (matchImageLink(imageLinkPattern.matcher(line))) {
                } else if (matchCaption(imageCaptionPattern.matcher(line))) {
                } else if (matchCaptionCredit(imageCreditPattern.matcher(line))) {
                } else if (matchParagraph(paragraphPattern.matcher(line))) {
                } else {
                }
            }
            if (paragraphs.isEmpty()) {
                throw new ArticleImportException("no paragraphs");
            }
            reader.close();
            store();
            completed = true;
        } catch (FeedException e) {
            logger.error(String.format("run %s: %s", e.getClass().getSimpleName(), e.getMessage()));
        } catch (FileNotFoundException e) {
            logger.error(String.format("run %s: %s", e.getClass().getSimpleName(), e.getMessage()));
        } catch (IOException e) {
            logger.error(String.format("run %s: %s", e.getClass().getSimpleName(), e.getMessage()));
            retry = true;
        } catch (NullPointerException e) {
            logger.error("run", e);
        } catch (Throwable e) {
            logger.error(String.format("run %s: %s", e.getClass().getSimpleName(), e.getMessage()));
            exception = e;
        }
    }

    public boolean isRetry() {
        return retry;
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

    private boolean matchGalleryCaption(Matcher matcher) {
        if (matcher.find()) {
            String caption = matcher.group(1);            
            if (FeedsUtil.isText(caption)) {
                galleryCaption = caption;
            } else {
                galleryCaption = null;
            }
            return true;
        }
        return false;
    }
    
    private boolean matchGalleryImageLink(Matcher matcher) {
        if (matcher.find()) {
            String galleryImageUrl = matcher.group(1);
            imageList.add(new ImageEntity(galleryImageUrl, galleryCaption));
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
        map.put("imageList", imageList);
        context.putJson(articlePath, map.toJson());
        context.putJson(String.format("article/%s.json", articleId), map.toJson());
    }

    private void loadImage() throws IOException {
        if (!imageList.isEmpty()) {
            for (ImageEntity image : imageList) {
                image.path = loadImage(image.sourceUrl);
            }
            imagePath = imageList.get(0).path;
            imageCaption = imageList.get(0).caption;
        } else if (sourceImageUrl != null) {
            imagePath = loadImage(sourceImageUrl);
        }    
    }

    private String loadImage(String sourceImageUrl) throws IOException {
        sourceImageUrl = "http://www.iol.co.za" + sourceImageUrl;
        byte[] content = Streams.readContent(sourceImageUrl);
        logger.info("content {} {}", content.length, sourceImageUrl);
        String name = Streams.parseFileName(sourceImageUrl);
        String path = numDate + "/image/" + name;
        context.putContent(path, content);
        context.storage.linkSet.add(path);
        return path;
    }
    
    void postContent(String path, byte[] content) throws IOException {
        String localImageUrl = String.format("%s/%s", context.contentUrl, path);
        Streams.postHttp(content, new URL(localImageUrl));
        content = Streams.readContent(localImageUrl);
        logger.info("imageUrl {} {}", content.length, localImageUrl);
    }
}
