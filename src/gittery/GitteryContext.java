package gittery;

import iolfeed.ContentStorage;

public class GitteryContext {
    String webResourcePath;
    String repo;
    String appResourceDir;
    String defaultPath;
    int port = 8088; 
    ContentStorage storage;
    
    public GitteryContext(ContentStorage storage, String webResourcePath, String defaultPath, 
            String repo) throws Exception {
        this.storage = storage;
        this.webResourcePath = webResourcePath;
        this.defaultPath = defaultPath;
        this.appResourceDir = storage.storageDir;
        this.repo = repo;
    }

    public void init() {
    }
    
    @Override
    public String toString() {
        return repo;
    }
}
