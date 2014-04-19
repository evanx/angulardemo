package gittery;

import iolfeed.ContentStorage;

public class GitteryContext {
    String res;
    String repo;
    String dir;
    String defaultPath = "index.html";
    int port = 8088; 
    ContentStorage storage;
    
    public GitteryContext(ContentStorage storage, String res, String repo, 
            String dir) throws Exception {
        this.storage = storage;
        this.res = res;
        this.repo = repo;
        this.dir = dir;
    }

    @Override
    public String toString() {
        return repo;
    }       
}
