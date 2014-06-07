package storage;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import vellum.format.CalendarFormats;
import vellum.jx.JMap;
import vellum.jx.JMapsException;
import vellum.util.Lists;

/**
 *
 * @author evanx
 */
public class SectionEntity {
    String section;
    Deque<JMap> articleDeque = new ArrayDeque();
    Map<String, JMap> articleMap = new HashMap();
    String previousPath;
    
    SectionEntity(String section) {
        this.section = section;
    }

    SectionEntity(String section, List<JMap> articles, int count) throws JMapsException {
        this.section = section;
        for (JMap article : articles) {
            String articleId = article.getString("articleId");
            if (!articleMap.containsKey(articleId)) {
                articleMap.put(articleId, article);
                articleDeque.addLast(article);
                if (count > 0 && articleDeque.size() >= count) {
                    break;
                }
            }
        }
    }
    
    SectionEntity(String section, List<JMap> articles) throws JMapsException {
        this(section, articles, 0);
    }

    public void setPreviousPath(String previousPath) {
        this.previousPath = previousPath;
    }
    
    public JMap map(int count) {
        JMap sectionMap = new JMap();
        sectionMap.put("section", section);
        sectionMap.put("timestamp", CalendarFormats.numericTimestampMinuteFormat.formatNow());
        sectionMap.put("previous", previousPath);
        sectionMap.put("articles", Lists.list(articleDeque.iterator(), count));
        return sectionMap;
    }

    public JMap mapExcess(int count) {
        JMap sectionMap = new JMap();
        sectionMap.put("section", section);
        sectionMap.put("timestamp", CalendarFormats.numericTimestampMinuteFormat.formatNow());
        sectionMap.put("previous", previousPath);
        sectionMap.put("articles", Lists.listExcess(articleDeque.iterator(), count));
        return sectionMap;
    }

    int size() {
        return articleDeque.size();
    }
    
    void trim(int limit) throws JMapsException {
        while (articleDeque.size() > limit) {
            JMap article = articleDeque.removeLast();
            String articleId = article.getString("articleId");
            articleMap.remove(articleId);
        }
    }
        
    public List<StorageItem> produce() {
        List<StorageItem> itemList = new ArrayList();
        return itemList;
    }
    
    @Override
    public String toString() {
        return String.format("%s %d", section, articleDeque.size());
    }       

    void addAll(List<JMap> articles) throws JMapsException {
        for (JMap article : Lists.reverse(articles)) {
            String articleId = article.getString("articleId");
            if (!articleMap.containsKey(articleId)) {
                articleMap.put(articleId, article);
                articleDeque.addFirst(article);
            }
        }
    }

}
