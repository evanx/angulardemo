package gittery;

import iolfeed.ContentStorage;
import iolfeed.PrefetchBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import vellum.util.Streams;

public class GitteryContext {
    String res;
    String repo;
    String dir;
    String defaultPath = "index.html";
    String prefetchPath = "prefetch.html";
    int port = 8088; 
    ContentStorage storage;
    
    public GitteryContext(ContentStorage storage, String res, String repo, 
            String dir) throws Exception {
        this.storage = storage;
        this.res = res;
        this.repo = repo;
        this.dir = dir;
    }

    void init() throws IOException {
        storage.init(new String(Streams.readResourceBytes(getClass(), 
                String.format("/%s/%s", res, defaultPath))), prefetchPath);
    }
    
    @Override
    public String toString() {
        return repo;
    }       

}
