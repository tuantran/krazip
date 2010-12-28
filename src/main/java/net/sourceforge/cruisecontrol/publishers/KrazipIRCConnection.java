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
import org.schwering.irc.lib.IRCEventListener;

import java.io.IOException;

/**
 * This is a class that implementing Singleton pattern to ensure that Krazip only have one connection to IRC server.
 */
public final class KrazipIRCConnection {

    private static final Logger log = Logger.getLogger(KrazipIRCConnection.class);
    private static KrazipIRCConnection instance;
    private IRCConnection realConnection;

    private KrazipIRCConnection(String host, int port, String nickName, String userName, String realName, String channel, IRCEventListener listener) {
        IRCConnection ircConnection = new IRCConnection(host, new int[]{port}, null, nickName, userName, realName);
        ircConnection.addIRCEventListener(listener);
        ircConnection.setEncoding("UTF-8");
        ircConnection.setPong(true);
        ircConnection.setDaemon(false);
        ircConnection.setColors(true);
        try {
            ircConnection.connect();
            log.info("Connected to IRC server");
        } catch (IOException ioe) {
            log.error("Error: Could not connect to IRC server", ioe);
        }
        ircConnection.doJoin(channel);
        log.info("Joined channel: " + channel);
        realConnection = ircConnection;
    }

    public static synchronized KrazipIRCConnection establishInstance(String host, int port, String nickName, String userName, String realName, String channel, KrazipIRCPublisher krazip) {
        if ( instance == null ) {
            instance = new KrazipIRCConnection(host, port, nickName, userName, realName, channel, new KrazipIRCListener( krazip ));
        }
        return instance;
    }

    public static synchronized IRCConnection retrieveInstance() {
        if ( instance == null) {
            throw new KrazipUsageException("You should always establish the instance before using it.");            
        }
        return instance.realConnection;
    }

    public static synchronized void destroyInstance() {
        instance = null;
    }
}
