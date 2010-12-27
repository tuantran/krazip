/*
 * Copyright 2010 ABC Tech Ltd. (Thailand) / APDM (A-pressen digitale medier, Norway)
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package net.sourceforge.cruisecontrol.publishers;

import org.apache.log4j.Logger;
import org.schwering.irc.lib.IRCConnection;

import java.util.ArrayList;
import java.util.List;

public class MockIRCConnection extends IRCConnection {
    private static final Logger log = Logger.getLogger(MockIRCConnection.class);
    private List<String> messageLog;

    public MockIRCConnection() {
        super("localhost", new int[]{6667}, "mock", "mockNick", "junit", "test");
        messageLog = new ArrayList<String>();
    }

    public List<String> getMessageLog() {
        return messageLog;
    }

    @Override
    public void send(String line) {
        log.debug("###IRC###: "+line);
        messageLog.add(line);
    }

}
