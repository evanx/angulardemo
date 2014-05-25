package storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import vellum.jx.JMap;
import vellum.jx.JMapException;

/**
 *
 * @author evanx
 */
public class SectionEntity {
    String section;
    List<JMap> articleList = new ArrayList();
    Map<String, JMap> articleMap;

    SectionEntity(String section) {
        this.section = section;
    }
    
    SectionEntity(String section, List<JMap> articles) throws JMapException {
        this.section = section;
        this.articleList = articles;
        for (JMap article : articles) {
            String articleId = article.getString("articleId");
            if (!articleMap.containsKey(articleId)) {
                articleMap.put(articleId, article);
            }
        }
    }

    public JMap map() {
        JMap sectionMap = new JMap();
        sectionMap.put("section", section);
        sectionMap.put("articles", articleList);
        return sectionMap;
    }
    
    @Override
    public String toString() {
        return String.format("%s %d", section, articleList.size());
    }       

    void addAll(List<JMap> articleList) throws JMapException {
        for (JMap article : articleList) {
            String articleId = article.getString("articleId");
            if (!articleMap.containsKey(articleId)) {
                articleMap.put(articleId, article);
                this.articleList.add(article);
            }
        }
    }
}
