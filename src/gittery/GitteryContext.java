package gittery;

import iolfeed.ContentStorage;

public class GitteryContext {
    String repo;
    String root;
    String defaultPath = "/index.html";
    int port = 8088; 
    ContentStorage storage;
    
    public GitteryContext(ContentStorage storage, String repo, String root) throws Exception {
        this.storage = storage;
        this.repo = repo;
        this.root = root;
    }

    @Override
    public String toString() {
        return repo;
    }       
}
