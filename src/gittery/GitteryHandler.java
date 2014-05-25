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
import vellum.util.MimeTypes;
import vellum.util.Streams;

/**
 *
 * @author evanx
 */
public class GitteryHandler implements HttpHandler {

    static Logger logger = LoggerFactory.getLogger(GitteryHandler.class);

    GitteryContext context;
    HttpExchange he;
    String method;
    String path;
    
    public GitteryHandler(GitteryContext context) {
        this.context = context;
    }

    @Override
    public void handle(HttpExchange he) throws IOException {
        this.he = he;
        path = he.getRequestURI().getPath();
        logger.info("handle {} {}", he.getRequestMethod(), path);
        try {
            if (path.startsWith("/storage/")) {
                if (!new File(context.storage.appDir, path).exists()) {
                    path = path.substring(9);
                }
            } else if (path.contains("undefined")) {
                logger.warn("path {}", path);
            } else if (path.equals("/")) {
                path = context.storage.defaultPath;
            } else if (path.startsWith("/")) {
                path = path.substring(1);
            }
            if (he.getRequestMethod().equals("OPTIONS")) {
                logger.warn("OPTIONS");
                writeOptions();
            } else if (he.getRequestMethod().equals("POST")) {
                logger.warn("POST");
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
        byte[] content = context.storage.get(path);
        if (content != null) {
            logger.info("memory: {}", path);
            return content;
        }
        File file = new File(context.storage.appDir, path);
        if (file.exists()) {
            return Streams.readBytes(file);
        }
        logger.info("not app file: " + file.getAbsolutePath());
        file = new File(context.storage.storageDir, path);
        if (file.exists()) {
            return Streams.readBytes(file);
        } else if (path.startsWith("20") || path.endsWith(".json")) {
            throw new IOException("no file: " + file.getAbsolutePath());
        }
        logger.info("not storage file: " + file.getAbsolutePath());
        String contentUrl = context.repo + "/" + path;
        try {
            if (false) {
                return Streams.readContent(contentUrl);
            }
        } catch (FileNotFoundException e) {
            logger.trace("not on github: " + e.getMessage());
        } catch (IOException e) {
            logger.warn("readContent " + contentUrl, e);
        }
        try {
            String resourcePath = "/" + context.webResourcePath + "/" + path;
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
        logger.info("response {} {}", path, content.length);
        setContentType();
        he.sendResponseHeaders(200, content.length);
        he.getResponseBody().write(content);
    }

    void writeGzip(byte[] content) throws IOException {
        logger.info("gzip response {} {}", path, content.length);
        he.getResponseHeaders().set("Content-Encoding", "gzip");
        setContentType();
        he.sendResponseHeaders(200, 0);
        try (OutputStream stream = new GZIPOutputStream(he.getResponseBody())) {
            stream.write(content);
        }
    }

    void writeGzipped(byte[] gzippedContent) throws IOException {
        logger.info("gzipped response {} {}", path, gzippedContent.length);
        he.getResponseHeaders().set("Content-Encoding", "gzip");
        setContentType();
        he.sendResponseHeaders(200, 0);
        he.getResponseBody().write(gzippedContent);
        he.getResponseBody().close();
    }

    void writeOptions() throws IOException {
        logger.info("options {} {}", path);
        setContentType();
        he.getResponseHeaders().set("Content-length", "0");
        File file = new File(context.storage.storageDir, path);
        if (file.exists()) {
            he.sendResponseHeaders(200, 0);
        } else {
            he.sendResponseHeaders(404, 0);
        }
        he.getResponseBody().close();
    }

    final static long CACHE_MIRROR_MILLIS = Millis.fromDays(28);
    final static long CACHE_IMAGE_MILLIS = Millis.fromDays(3);
    final static long CACHE_ARTICLES_MILLIS = Millis.fromMinutes(3);

    private void setContentType() {
        he.getResponseHeaders().set("Content-type", MimeTypes.getContentType(path, "text/html"));
        if (path.startsWith("mirror/")) {
            he.getResponseHeaders().set("Cache-control", "max-age=" + Millis.toSeconds(CACHE_MIRROR_MILLIS));
        } else if (MimeTypes.getContentType(path, "").startsWith("image/")) {
            he.getResponseHeaders().set("Cache-control", "max-age=" + Millis.toSeconds(CACHE_IMAGE_MILLIS));
        } else if (path.endsWith("/articles.json")) {
            he.getResponseHeaders().set("Access-control-allow-headers", "if-modified-since");
            he.getResponseHeaders().set("Access-control-allow-origin", "*");
            he.getResponseHeaders().set("Cache-control", "max-age=" + Millis.toSeconds(CACHE_ARTICLES_MILLIS));
        } else if (path.endsWith(".json")) {
            he.getResponseHeaders().set("Access-control-allow-headers", "if-modified-since");
            he.getResponseHeaders().set("Access-control-allow-origin", "*");
            he.getResponseHeaders().set("Cache-control", "max-age=" + Millis.toSeconds(CACHE_ARTICLES_MILLIS));
        }
    }

}
