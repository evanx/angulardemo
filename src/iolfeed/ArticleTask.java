package iolfeed;

import storage.ContentStorage;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
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
    static final Pattern timestampPattern
            = Pattern.compile("^\\s*<p class=\"byline\">([^<]*)");
    static final Pattern relatedArticlePattern
            = Pattern.compile("^\\s*<li><a class=\"related_articles\" href=\"(.*)\">(.*)</a>");
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
    static final Pattern videoTitlePattern = Pattern.compile(
            "\\s*<h1 class=\"article_headers_multimedia\">(.*)</h1>");
    static final Pattern videoSizePattern = Pattern.compile(
            "\\s*<object width=\"([0-9]*)\" height=\"([0-9]*)\">");
    static final Pattern videoIdPattern = Pattern.compile(
            "\\s*<param name=\"movie\" value=\"([^&]*).*\">");
    static final Pattern multimediaCaptionPattern = Pattern.compile(
            "\\s*<p class=\"multimedia_gal_captions\">([^<]*)");
    static final Pattern multimediaTimestampPattern = Pattern.compile(
            "<span class=\"lead-stories-comment\">(.*)</span");

    JMap map;
    Throwable exception;
    String sourceArticleUrl;
    String sourceImageUrl;
    String articleId;
    String articlePath;
    String imagePath;
    String imageCredit;
    String imageCaption;
    String originalSection;
    String timestampLabel;
    String multimediaCaption;
    String multimediaTimestamp;
    Integer maxWidth;
    Integer maxHeight;
    FeedsContext context = FeedsProvider.getContext();
    ContentStorage storage = FeedsProvider.getStorage();
    List<String> paragraphs = new ArrayList();
    List<ImageItem> imageList = new ArrayList();
    List<RelatedArticleItem> relatedArticleList = new ArrayList();
    List<YoutubeItem> youtubeList = new ArrayList();
    boolean completed = false;
    boolean retry = false;
    String galleryCaption;
    YoutubeItem youtubeItem;
    int depth = 0;
    Thread currentThread;
    
    ArticleTask(JMap map) {
        this.map = map;
    }

    ArticleTask(int depth, RelatedArticleItem storyItem) {
        this.depth = depth;
        this.map = new JMap();
        map.put("link", storyItem.source);
        map.put("title", storyItem.title);
    }
    
    public void init() throws JMapException {
        sourceArticleUrl = map.getString("link");
        int index = sourceArticleUrl.lastIndexOf("/");
        if (index > 0) {
            articleId = sourceArticleUrl.substring(index + 1);
            index = articleId.lastIndexOf("-");
            if (index > 0) {
                articleId = articleId.substring(0, index);
            }
        }
        logger = LoggerFactory.getLogger(String.format("ArticleTask.%s", articleId));
        articlePath = String.format("article/%s.json", articleId);
        // todo category from source link
    }

    public boolean isRetry() {
        return retry;
    }

    public boolean isCompleted() {
        return completed;
    }

    private void clear() {
        paragraphs.clear();
        imageList.clear();
        youtubeList.clear();
        relatedArticleList.clear();
    }
    
    @Override
    public void run() {
        if (currentThread != null) {
            logger.error("currentThread: " + currentThread.getName());
        }
        currentThread = Thread.currentThread();
        try {
            if (!context.storage.refresh && context.storage.containsKey(articlePath)) {
                logger.info("containsKey {}", articlePath);
                map = context.storage.getMap(articlePath);
            } else {
                clear();
                parseArticle();
                if (relatedArticleList.size() > 0 && depth < context.maxDepth) {
                    parseRelatedArticle();
                } else {
                    relatedArticleList.clear();
                }
                store();
            }
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
            logger.error(String.format("run %s: %s", e.getClass().getSimpleName(), e.getMessage()), e);
            exception = e;
        }
        currentThread = null;
    }

    private void parseRelatedArticle() throws Exception {
        List<RelatedArticleItem> parsedRelatedArticleList = new ArrayList();
        for (RelatedArticleItem item : relatedArticleList) {
            ArticleTask task = new ArticleTask(depth + 1, item);
            task.init();
            task.run();
            if (task.isCompleted()) {
                item.path = String.format("article/%s", task.articleId);    
                parsedRelatedArticleList.add(item);
            }
        }
        logger.info("parseRelatedArticle {} {}", relatedArticleList.size(), parsedRelatedArticleList.size());
        relatedArticleList.clear();
        relatedArticleList.addAll(parsedRelatedArticleList);
    }
    
    private void parseArticle() throws Exception {
        URLConnection connection = new URL(sourceArticleUrl).openConnection();
        connection.setDoOutput(false);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (matchTimestamp(timestampPattern.matcher(line))) {
                } else if (matchRelatedArticle(relatedArticlePattern.matcher(line))) {
                } else if (matchParagraph(paragraphPattern.matcher(line))) {
                } else if (matchMultimediaCaption(multimediaCaptionPattern.matcher(line))) {
                } else if (matchMultimediaTimestamp(multimediaTimestampPattern.matcher(line))) {
                } else if (matchVideoTitle(videoTitlePattern.matcher(line))) {
                } else if (matchVideoSize(videoSizePattern.matcher(line))) {
                } else if (matchVideoId(videoIdPattern.matcher(line))) {
                } else if (matchGalleryCaption(galleryCaptionPattern.matcher(line))) {
                } else if (matchGalleryImageLink(galleryImageLinkPattern.matcher(line))) {
                } else if (matchImageLink(imageLinkPattern.matcher(line))) {
                } else if (matchCaption(imageCaptionPattern.matcher(line))) {
                } else if (matchCaptionCredit(imageCreditPattern.matcher(line))) {
                } else {
                }
            }
            String description = map.getString("description", null);
            if (multimediaCaption != null) {
                if (paragraphs.isEmpty()) {
                    paragraphs.add(multimediaCaption);
                }
                if (description == null) {
                    description = multimediaCaption;
                    map.put("description", description);
                }
            }
            if (description == null || description.isEmpty()) {
                if (depth > 0) {
                    logger.warn("empty lead");
                }
            }
            if (paragraphs.isEmpty() && youtubeList.isEmpty() && imageList.isEmpty()) {
                throw new ArticleImportException("no content");
            }
        }
        logger.info("parseArticle {} {}", depth, relatedArticleList.size());
    }

    private boolean matchTimestamp(Matcher matcher) {
        if (matcher.find()) {
            String string = matcher.group(1).trim();
            if (string.contains(" 20")) {
                timestampLabel = string;
                if (depth > 0) {
                    map.put("pubDate", timestampLabel);
                }
            }
            return true;
        }
        return false;
    }
    
    private boolean matchRelatedArticle(Matcher matcher) {
        if (matcher.find()) {
            RelatedArticleItem relatedArticle = new RelatedArticleItem(matcher.group(1), 
                    FeedsUtil.cleanText(matcher.group(2)));
            relatedArticleList.add(relatedArticle);        
            logger.info("relatedArticle: {}", relatedArticle);
            return true;
        }
        return false;
    }
    
    private boolean matchMultimediaCaption(Matcher matcher) {
        if (matcher.find()) {
            multimediaCaption = FeedsUtil.cleanText(matcher.group(1));
            return true;
        }
        return false;
    }

    private boolean matchMultimediaTimestamp(Matcher matcher) {
        if (matcher.find()) {
            multimediaTimestamp = FeedsUtil.cleanText(matcher.group(1));
            return true;
        }
        return false;
    }

    private boolean matchVideoTitle(Matcher matcher) {
        if (matcher.find()) {
            String title = matcher.group(1);
            youtubeItem = new YoutubeItem(FeedsUtil.cleanText(title));
            return true;
        }
        return false;
    }

    private boolean matchVideoSize(Matcher matcher) {
        if (matcher.find()) {
            if (youtubeItem != null) {
                youtubeItem.width = matcher.group(1);
                youtubeItem.height = matcher.group(2);
            }
            return true;
        }
        return false;
    }

    private boolean matchVideoId(Matcher matcher) {
        if (matcher.find()) {
            String id = matcher.group(1);
            youtubeItem.url = String.format("http://www.youtube.com/embed/%s", id);
            youtubeItem.thumbnail = String.format("http://i1.ytimg.com/vi/%s/default.jpg", id);
            youtubeList.add(youtubeItem);
            youtubeItem = null;
            return true;
        }
        return false;
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
                galleryCaption = caption.trim();
            } else {
                galleryCaption = null;
            }
            return true;
        }
        return false;
    }

    private boolean matchGalleryImageLink(Matcher matcher) {
        if (matcher.find()) {
            String source = matcher.group(1);
            imageList.add(new ImageItem(source, galleryCaption));
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
            String paragraph = FeedsUtil.cleanParagraph(matcher.group(1));
            if (!paragraph.isEmpty()) {
                paragraphs.add(paragraph);
            }
            return true;
        }
        return false;
    }

    private void store() throws IOException {
        logger.info("store {} {}", depth, relatedArticleList.size());
        map.put("articleId", articleId);
        map.put("articlePath", articlePath);
        map.put("paragraphs", paragraphs);
        map.put("imageCredit", imageCredit);
        map.put("imageCaption", imageCaption);
        loadImage();
        if (maxWidth != null && maxWidth > 0) {
            map.put("maxWidth", maxWidth);
        }
        if (maxHeight != null && maxHeight > 0) {
            map.put("maxHeight", maxHeight);
        }
        map.put("imagePath", imagePath);
        map.put("imageList", imageList);
        if (timestampLabel != null) {
            map.put("timestampLabel", timestampLabel);
        }
        if (!relatedArticleList.isEmpty()) {
            map.put("relatedArticleList", relatedArticleList);
        }
        if (!youtubeList.isEmpty()) {
            map.put("youtubeList", youtubeList);
        }
        if (multimediaCaption != null) {
            map.put("multimediaCaption", multimediaCaption);
        }
        if (multimediaTimestamp != null) {
            map.put("multimediaTimetamp", multimediaTimestamp);
        }
        context.storage.putJson(articlePath, map);
    }

    private void loadImage() throws IOException {
        if (!imageList.isEmpty()) {
            maxWidth = 0;
            maxHeight = 0;
            for (ImageItem image : imageList) {
                loadImage(image);
                if (image.width != null && image.width > maxWidth) {
                    maxWidth = image.width;
                }
                if (image.height != null && image.height > maxHeight) {
                    maxHeight = image.height;
                }
            }
            imagePath = imageList.get(0).path;
            imageCaption = imageList.get(0).text;
        } else if (sourceImageUrl != null) {
            imagePath = loadImage(new ImageItem(sourceImageUrl));
        }
    }

    private String loadImage(ImageItem image) throws IOException {
        image.source = "http://www.iol.co.za" + image.source;
        String name = Streams.parseFileName(image.source);
        image.path = "image/" + name;
        byte[] content = context.storage.get(image.path);
        if (content != null) {
            logger.info("containsKey {}", image.path);
        } else {
            logger.info("loadImage {}", image.path);
            content = Streams.readContent(image.source);
            context.storage.putContent(image.path, content);
        }
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(content));
        image.width = img.getWidth();
        image.height = img.getHeight();
        logger.info("loadImage {} {}", content.length, image);
        if (image.width <= 0 || image.height <= 0) {
            throw new IOException("invalid image size");
        }
        return image.path;
    }

    void postContent(String path, byte[] content) throws IOException {
        String localImageUrl = String.format("%s/%s", context.storage.contentUrl, path);
        Streams.postHttp(content, new URL(localImageUrl));
        content = Streams.readContent(localImageUrl);
        logger.info("imageUrl {} {}", content.length, localImageUrl);
    }
}
