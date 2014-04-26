package gittery;

import iolfeed.ContentStorage;

public class GitteryContext {
    String webResourcePath;
    String repo;
    String dir;
    String defaultPath;
    int port = 8088; 
    ContentStorage storage;
    
    public GitteryContext(ContentStorage storage, String webResourcePath, String defaultPath, 
            String appResourceDir, String repo) throws Exception {
        this.storage = storage;
        this.webResourcePath = webResourcePath;
        this.defaultPath = defaultPath;
        this.dir = appResourceDir;
        this.repo = repo;
    }

    @Override
    public String toString() {
        return repo;
    }       

}
