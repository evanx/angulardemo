package src.gittery;

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

    GitteryServer server;
    HttpExchange he;
    String path;
    
    public GitteryHandler(GitteryServer server) {
        this.server = server;
    }

    @Override
    public void handle(HttpExchange he) throws IOException {
        this.he = he;
        path = he.getRequestURI().getPath();
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
        File file = new File("." + path);
        file.getParentFile().mkdirs();
        Streams.transmit(he.getRequestBody(), file);
        logger.info("post {} {}", file.length(), file.getAbsolutePath());
    }
    
    private void get() throws Exception {
        if (path.equals("/")) {
            path = server.defaultPath;
        }
        File rootFile = new File(server.root + path);
        File file = rootFile;
        File currentFile = new File("." + path);
        if (path.endsWith(".json")) {
            if (currentFile.exists()) {
                file = currentFile;
            } else {
                logger.warn("no file {}", currentFile);
            }
        }
        URL url = new URL(server.repo + path);
        int length;
        byte[] content;
        if (file.exists()) {
            content = Streams.readBytes(file);
            length = content.length;
        } else {
            URLConnection connection = url.openConnection();
            length = connection.getContentLength();
            if (length < 0) {
                content = Streams.readBytes(connection.getInputStream());
            } else {
                content = new byte[length];
                connection.getInputStream().read(content);
            }
        }
        logger.info("response {} {}", length, url);
        he.sendResponseHeaders(200, length);
        he.getResponseHeaders().set("Content-Type", GitteryUtil.getContentType(path));
        he.getResponseBody().write(content);
    }
}
