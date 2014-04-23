package gittery;

/*
 * Source https://github.com/evanx by @evanxsummers

 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements. See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership. The ASF licenses this file to
 you under the Apache License, Version 2.0 (the "License").
 You may not use this file except in compliance with the
 License. You may obtain a copy of the License at:

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.  
 */
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.data.Millis;
import vellum.util.Streams;

/**
 *
 * @author evanx
 */
public class GitteryHandler implements HttpHandler {

    static Logger logger = LoggerFactory.getLogger(GitteryHandler.class);

    GitteryContext context;
    HttpExchange he;
    String path;
    
    public GitteryHandler(GitteryContext context) {
        this.context = context;
    }

    @Override
    public void handle(HttpExchange he) throws IOException {
        this.he = he;
        path = he.getRequestURI().getPath();
        logger.trace("path [{}]", path);
        try {
            if (path.equals("/fast")) {
                if (context.storage.fastContent != null) {
                    if (requestHeaderMatches("Accept-Encoding", "gzip") &&
                            context.storage.fastGzippedContent != null) {
                        writeFast(context.storage.fastGzippedContent);
                    } else {
                        write(context.storage.fastContent);
                    }
                    return;
                } else {
                    path = context.defaultPath;                    
                }
            } else if (path.equals("/")) {
                path = context.defaultPath;
            } else if (path.startsWith("/")) {
                path = path.substring(1);
            }
            if (he.getRequestMethod().equals("POST")) {
                post();
            } else if (requestHeaderMatches("Accept-Encoding", "gzip")) {
                writeGzip(get());
            } else {
                logger.warn("Accept-Encoding: {}", he.getRequestHeaders().get("Accept-Encoding"));
                write(get());                
            }
        } catch (Throwable e) {
            writeError();
            e.printStackTrace(System.err);
        } finally {
            he.close();
        }
    }
    
    private boolean requestHeaderMatches(String header, String pattern) {
        List<String> values = he.getRequestHeaders().get(header);
        if (values != null && !values.isEmpty()) {
            return values.get(0).contains(pattern);
        }
        return false;        
    }

    private void post() throws IOException {
        File file = new File(path);
        file.getParentFile().mkdirs();
        Streams.transmit(he.getRequestBody(), file);
        logger.info("post {} {}", file.length(), file.getAbsolutePath());
        context.storage.put(path, Streams.readBytes(file));
    }

    private byte[] get() throws Exception {
        if (path.equals("fast") && context.storage.fastContent != null) {
            return context.storage.fastContent;
        }
        byte[] content = context.storage.get(path);
        if (content != null) {
            return content;
        }
        File file = new File(path);
        if (file.exists()) {
            return Streams.readBytes(file);
        } else if (path.startsWith("2")) {
            throw new IOException("no file: " + file.getAbsolutePath());
        }
        File sourceFile = new File(context.dir + "/" + path);
        if (sourceFile.exists()) {
            return Streams.readBytes(sourceFile);
        }
        logger.trace("not local file: " + file.getAbsolutePath());
        String contentUrl = context.repo + "/" + path;
        try {
            return Streams.readContent(contentUrl);
        } catch (FileNotFoundException e) {
            logger.trace("not on github: " + e.getMessage());
        } catch (IOException e) {
            logger.warn("readContent " + contentUrl, e);
        }
        try {
            String resourcePath = "/" + context.res + "/" + path;
            logger.trace("resourcePath {}", resourcePath);
            return Streams.readResourceBytes(getClass(), resourcePath);
        } catch (Throwable e) {
            logger.warn(e.getMessage());
            throw new FileNotFoundException(path);
        }
    }

    void writeError() throws IOException {
        logger.info("not found {}", path);
        he.sendResponseHeaders(404, 0);
    }
    
    void write(byte[] content) throws IOException {
        logger.info("response {} {}", content.length);
        setContentType();
        he.sendResponseHeaders(200, content.length);
        he.getResponseBody().write(content);
    }
    
    void writeGzip(byte[] content) throws IOException {
        logger.info("gzip response {} {}", content.length);
        he.getResponseHeaders().set("Content-Encoding", "gzip");
        setContentType();
        he.sendResponseHeaders(200, 0);
        try (OutputStream stream = new GZIPOutputStream(he.getResponseBody())) {
            stream.write(content);
        }
    }        
    
    void writeFast(byte[] content) throws IOException {
        logger.info("fast response {} {}", content.length);
        he.getResponseHeaders().set("Content-Encoding", "gzip");
        setContentType();
        he.sendResponseHeaders(200, 0);
        he.getResponseBody().write(content);
        he.getResponseBody().close();
    }        

    private void setContentType() {
        he.getResponseHeaders().set("Content-Type", Streams.getContentType(path));
        if (Streams.getContentType(path).startsWith("image/")) {
            he.getResponseHeaders().set("Cache-Control", "max-age=" + Millis.toSeconds(Millis.fromDays(3)));
        }
    }
    
}
