package angulardemo.app;

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


import gittery.GitteryServer;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author evanx
 */
public class AngularDemoServerMain {
    static Logger logger = LoggerFactory.getLogger(AngularDemoServerMain.class);

    public void start() throws Exception {
    }
    
    public static void main(String[] args) throws Exception {
        try {
            BasicConfigurator.configure();
            GitteryServer server = new GitteryServer();
            server.start("https://raw.githubusercontent.com/evanx/angulardemo/master/angulardemo/web",
                    "/home/evanx/NetBeansProjects/git/angulardemo/angulardemo/web"
            );
            new IOLFeederApp().start();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    
}
