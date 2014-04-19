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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author evanx
 */
public class GitteryUtil {
    static Logger logger = LoggerFactory.getLogger(GitteryUtil.class);

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
        } else if (path.endsWith(".ico")) {
            return "image/x-icon";
        } else {
            logger.warn(path);
            return "text/html";
        }
    }
    
    
}
