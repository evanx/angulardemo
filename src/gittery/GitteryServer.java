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
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.httpserver.HttpServerProperties;
import vellum.httpserver.VellumHttpServer;

/**
 *
 * @author evanx
 */
public class GitteryServer implements HttpHandler {
    static Logger logger = LoggerFactory.getLogger(GitteryServer.class);

    GitteryContext context;
    VellumHttpServer httpServer = new VellumHttpServer();
        
    public void start(GitteryContext context) throws Exception {
        this.context = context;
        logger.info("start {}", context);
        httpServer.start(new HttpServerProperties(context.port), this);
    }

    @Override
    public void handle(HttpExchange he) throws IOException {
        try {
            new GitteryHandler(context).handle(he);
        } catch (IOException e) {
            logger.warn(e.getMessage());
            throw e;
        }
    }
}
