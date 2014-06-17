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
import vellum.jx.JMapsException;
import vellum.monitor.Tx;
import vellum.util.Streams;

/**
 *
 * @author evanx
 */
public class ArticleTask implements Runnable {

    Logger logger = LoggerFactory.getLogger(ArticleTask.class);

    static final Pattern linkPattern
            = Pattern.compile("http://www.iol.co.za/([^/]*)/([^/]*)");
    static final Pattern bylinePattern
            = Pattern.compile("^\\s*<p class=\"byline\">");
    static final Pattern bylineTimestampPattern
            = Pattern.compile("^\\s*(.* 20.* at .*\\w)\\s*<br/>");
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
    String multimediaCaption;
    String multimediaTimestamp;
    String section;
    String topic;
    Integer maxWidth;
    Integer maxHeight;
    FeedsContext context;
    ContentStorage storage;
    List<String> paragraphs = new ArrayList();
    List<ImageItem> imageList = new ArrayList();
    List<RelatedArticleItem> relatedArticleList = new ArrayList();
    List<YoutubeItem> youtubeList = new ArrayList();
    boolean completed = false;
    boolean retry = false;
    String galleryCaption;
    YoutubeItem youtubeItem;
    ImageItem articleImage;
    int depth = 0;
    Thread currentThread;
    boolean byline;

    ArticleTask(FeedsContext context, JMap map) throws FeedException, JMapsException {
        this.context = context;
        this.map = map;
        sourceArticleUrl = map.getString("link");
        articleId = parseArticleId(sourceArticleUrl);
        logger = LoggerFactory.getLogger(String.format("ArticleTask.%s", articleId));
        articlePath = formatArticlePath(articleId);
    }

    ArticleTask(FeedsContext context, RelatedArticleItem storyItem, int depth) 
            throws FeedException, JMapsException {
        this(context, storyItem.map());
        this.depth = depth;
    }

    public void init() throws JMapsException, FeedException {
        this.storage = context.storage;
    }

    private static String formatArticlePath(String id) {
        return String.format("article/%s.json", id);
    }

    private static String parseArticleId(String source) throws FeedException {
        int index = source.lastIndexOf("/");
        if (index > 0) {
            String id = source.substring(index + 1);
            index = id.lastIndexOf("-");
            if (index > 0) {
                id = id.substring(0, index);
            }
            return id;
        }
        throw new FeedException("Invalid link: " + source);
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
            return;
        }
        currentThread = Thread.currentThread();
        retry = false;
        Tx tx = context.getMonitor().begin("ArticleTask", articleId);
        try {
            if (!context.storage.refresh && context.storage.containsKey(articlePath)) {
                logger.info("containsKey {}", articlePath);
                map = context.storage.getMap(articlePath);
            } else {
                clear();
                parseArticle();
                if (relatedArticleList.size() > 0 && depth < context.maxDepth) {
                    tx.sub("related");
                    parseRelatedArticles();
                } else {
                    relatedArticleList.clear();
                }
                store();
            }
            completed = true;
            tx.ok();
        } catch (FeedException | FileNotFoundException e) {
            tx.error(e);
        } catch (IOException e) {
            retry = true;
            tx.error(e);
        } catch (Exception e) {
            tx.error(e);
        } catch (Throwable e) {
            tx.error(e);
            logger.error("throwable", e);
        } finally {
            if (!completed && !tx.isError()) {
                logger.error("finally");
            }
            tx.fin();
            exception = tx.getException();
            currentThread = null;
        }
    }

    private void parseRelatedArticles() throws Exception {
        List<RelatedArticleItem> parsedRelatedArticleList = new ArrayList();
        for (RelatedArticleItem item : relatedArticleList) {
            String id = parseArticleId(item.source);
            item.path = String.format("article/%s", id);
            String jsonPath = String.format("article/%s.json", id);
            if (!context.storage.refresh && context.storage.exists(jsonPath)) {
                logger.info("relatedArticlePath exists {}", jsonPath);
                parsedRelatedArticleList.add(item);
            } else {
                ArticleTask task = new ArticleTask(context, item, depth + 1);
                task.run();
                if (task.isCompleted()) {
                    parsedRelatedArticleList.add(item);
                }
            }
        }
        logger.info("parseRelatedArticle {} {}", relatedArticleList.size(), parsedRelatedArticleList.size());
        relatedArticleList.clear();
        relatedArticleList.addAll(parsedRelatedArticleList);
    }

    private void parseArticle() throws Exception {
        URLConnection connection = new URL(sourceArticleUrl).openConnection();
        connection.setDoOutput(false);
        connection.setConnectTimeout(context.connectTimeout);
        connection.setReadTimeout(context.readTimeout);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {
            parseLink(connection.getURL().toString());           
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (byline) {
                    byline = false;
                    if (matchTimestamp(bylineTimestampPattern.matcher(line))) {
                        continue;
                    }
                }
                if (bylinePattern.matcher(line).matches()) {
                    byline = true;
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
                if (depth == 0) {
                    logger.warn("empty lead");
                }
            }
            if (paragraphs.isEmpty() && youtubeList.isEmpty() && imageList.isEmpty()) {
                throw new ArticleImportException("no content");
            }
        }
        logger.info("parseArticle {} {}", depth, relatedArticleList.size());
    }

    private void parseLink(String link) {
        sourceArticleUrl = link;
        Matcher matcher = linkPattern.matcher(link);
        if (matcher.find()) {
            section = matcher.group(1);
            topic = matcher.group(2);
            logger.info("parseLink {} {}", section, topic);
        }
    }
    
    private boolean matchTimestamp(Matcher matcher) {
        if (matcher.find()) {
            String string = matcher.group(1);
            logger.info("parseTimestamp {}", string);
            if (depth > 0) {
                map.put("pubDate", string);
                return true;
            }
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
            youtubeItem.image = String.format("http://i1.ytimg.com/vi/%s/hq1.jpg", id);
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
                if (paragraphs.size() == 1 && paragraphs.get(0).endsWith(" -")) {
                    paragraph = paragraphs.get(0) + " " + paragraph;
                    paragraphs.clear();
                }
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
        if (!imageList.isEmpty()) {
            map.put("imageList", imageList);
        }
        if (articleImage != null) {
            map.put("articleImage", articleImage);
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
        if (section != null) {
            map.put("section", section);
        }
        if (topic != null) {
            map.put("topic", topic);
        }
        context.storage.putJsonArticle(articlePath, map);
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
            articleImage = new ImageItem(sourceImageUrl);
            imagePath = loadImage(articleImage);
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
}
