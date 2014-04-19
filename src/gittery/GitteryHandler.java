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
import java.net.URL;
import java.net.URLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        if (path.startsWith("/")) {
            path = path.substring(1);
        }        
        logger.info("path {}", path);
        try {
            if (he.getRequestMethod().equals("POST")) {
                post();
            } else {
                get();
            }
        } catch (Throwable e) {
            e.printStackTrace(System.err);
        } finally {
            he.close();
        }
    }

    private void post() throws IOException {
        File file = new File(path);
        file.getParentFile().mkdirs();
        Streams.transmit(he.getRequestBody(), file);
        logger.info("post {} {}", file.length(), file.getAbsolutePath());
        context.storage.put(path, Streams.readBytes(file));
    }

    private void get() throws Exception {
        byte[] content = context.storage.get(path);
        if (content == null) {
            if (path.equals("/")) {
                path = context.defaultPath;
            }
            File file = new File(context.dir + "/" + path);
            if (file.exists()) {
                content = Streams.readBytes(file);
            } else {
                try {
                    content = Streams.readContent(context.repo + "/" + path);
                } catch (FileNotFoundException e) {
                    logger.info(e.getMessage());
                    String resourcePath = "/" + context.res + "/" + path;
                    logger.info("resourcePath {}", resourcePath);
                    content = Streams.readResourceBytes(getClass(), resourcePath);
                }
            }
        }
        write(content);
    }

    void write(byte[] content) throws IOException {
        logger.info("response {} {}", content.length);
        he.sendResponseHeaders(200, content.length);
        he.getResponseHeaders().set("Content-Type", Streams.getContentType(path));
        he.getResponseBody().write(content);
    }
}
