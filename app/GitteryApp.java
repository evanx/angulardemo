package app;

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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.httpserver.HttpServerProperties;
import vellum.httpserver.VellumHttpServer;

/**
 *
 * @author evanx
 */
public class GitteryApp implements HttpHandler {
    static Logger logger = LoggerFactory.getLogger(GitteryApp.class);

    VellumHttpServer httpServer = new VellumHttpServer();

    String repo = "https://raw.github.com/evanx/angulardemo/master";
    String root = "/home/evanx/NetBeansProjects/git/angulardemo";
    String defaultPath = "/index.html";
    int port = 8088; 
    
    public void start() throws Exception {
        httpServer.start(new HttpServerProperties(port), this);
        new IOLFeederApp().start();
    }

    @Override
    public void handle(HttpExchange he) throws IOException {
        String path = he.getRequestURI().getPath();
        if (path.equals("/")) {
            path = defaultPath;
        }
        File rootFile = new File(root + path);
        File file = rootFile;
        File currentFile = new File("." + path);
        if (path.endsWith(".json") && currentFile.exists()) {
            file = currentFile;
        }
        logger.info("file {}", file);
        URL url = new URL(repo + path);
        logger.info("url {}", url);
        try {
            int length;
            byte[] content;
            if (file.exists()) {
                length = (int) file.length();
                content = new byte[length];
                new FileInputStream(file).read(content);
            } else {
                URLConnection connection = url.openConnection();
                length = connection.getContentLength();
                content = new byte[length];
                connection.getInputStream().read(content);
            }
            logger.info("content {}", new String(content));
            he.sendResponseHeaders(200, length);
            he.getResponseHeaders().set("Content-Type", getContentType(path));
            he.getResponseBody().write(content);
        } catch (Throwable e) {
            e.printStackTrace(System.err);
        } finally {
            he.close();
        }
    }
    
    public static String getContentType(String path) {
        if (path.endsWith(".png")) {
            return "image/png";
        } else if (path.endsWith(".jpg")) {
            return "image/jpeg";
        } else if (path.endsWith(".html")) {
            return "text/html";
        } else if (path.endsWith(".css")) {
            return "text/css";
        } else if (path.endsWith(".js")) {
            return "text/javascript";
        } else if (path.endsWith(".txt")) {
            return "text/plain";
        } else if (path.endsWith(".json")) {
            return "text/json";
        } else if (path.endsWith(".html")) {
            return "text/html";
        } else {
            logger.warn(path);
            return "text/html";
        }
    }
    
    public static void main(String[] args) throws Exception {
        try {
            BasicConfigurator.configure();
            GitteryApp app = new GitteryApp();            
            app.start();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    
}
