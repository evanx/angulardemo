package gittery;

import storage.ContentStorage;
import vellum.util.Args;

public class GitteryContext {
    String webResourcePath;
    String repo;
    final int port = 8888; 
    ContentStorage storage;
    
    public GitteryContext(ContentStorage storage, String webResourcePath, String defaultPath, 
            String repo) throws Exception {
        this.storage = storage;
        this.webResourcePath = webResourcePath;
        this.repo = repo;
    }

    @Override
    public String toString() {
        return Args.format(port, repo);
    }
}
