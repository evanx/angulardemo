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
public class SectionItem {
    String section;
    List<JMap> articleList = new ArrayList();
    Map<String, JMap> articleMap;

    SectionItem(String section) {
        this.section = section;
    }
    
    SectionItem(String section, List<JMap> articles) throws JMapException {
        this.section = section;
        this.articleList = articles;
        for (JMap article : articles) {
            articleMap.put(article.getString("articleId"), article);
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

    void addAll(List<JMap> articleList) {
        articleList.addAll(articleList);
    }
}
