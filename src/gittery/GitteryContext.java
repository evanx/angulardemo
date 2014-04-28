package gittery;

import iolfeed.ContentStorage;

public class GitteryContext {
    String webResourcePath;
    String repo;
    int port = 8088; 
    ContentStorage storage;
    
    public GitteryContext(ContentStorage storage, String webResourcePath, String defaultPath, 
            String repo) throws Exception {
        this.storage = storage;
        this.webResourcePath = webResourcePath;
        this.repo = repo;
    }

    public void init() {
    }
    
    @Override
    public String toString() {
        return repo;
    }
}
