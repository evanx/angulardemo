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
package vellum.monitor;

import java.io.PrintStream;
import java.util.TreeMap;

/**
 *
 * @author evan.summers
 */
public class LongAggregateMap extends TreeMap<String, LongAggregate> {
    LongAggregate all = new LongAggregate("all");
    
    public LongAggregateMap() {
    }

    public LongAggregate get(String type) {
        LongAggregate aggregate = super.get(type);
        if (aggregate == null) {
            aggregate = new LongAggregate(type);
            super.put(type, aggregate);
        }
        return aggregate;
    }
    
    void ingest(Tx tx) {
        LongAggregate agg = get(tx.getType());
        agg.ingest(tx.getDuration());
        all.ingest(tx.getDuration());        
    }

    @Override
    public String toString() {
        return String.format("agg %d, all %s", size(), all);
    }

    public void println(PrintStream stream) {
        stream.println(toString());
        for (LongAggregate agg : values()) {
            stream.printf("+%s\n", agg);
        }
    }
    
    
}
