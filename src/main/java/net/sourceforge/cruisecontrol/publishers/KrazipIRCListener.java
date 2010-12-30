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
import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;

/**
 * This is a class that implement the <code>IRCEventListener</code> In
 * this case Krazip will handle the event <code>onPrivmsg</code>
 * only. It's for krazip commands input from IRC
 * <p/>
 * Implementation of IRCEventListener
 * <p/>
 * <i>Quote from <code>org.schwering.irc.lib.IRCEventListener</code></i>
 * <p/>
 * Used as listener for incoming events like messages.
 * <p>
 * The <code>IRCEventListener</code> is used by the
 * <code>IRCConnection.addEventListener(IRCEventListener)</code> method to add
 * a listener which listens to the connection for incoming IRC events like
 * <code>PRIVMSG</code>s or numeric replies...
 * </p>
 */
public class KrazipIRCListener implements IRCEventListener {

    private static final Logger log = Logger.getLogger(KrazipIRCListener.class);
    private final KrazipIRCPublisher krazipIRCPublisher;

    public KrazipIRCListener(KrazipIRCPublisher krazipIRCPublisher) {
        this.krazipIRCPublisher = krazipIRCPublisher;
    }

    public void onDisconnected() {
        log.info("Disconnected");
    }

    public void onError(String msg) {
        log.warn("Error: " + msg);
        releaseConnection();
    }

    public void onError(int num, String msg) {
        log.warn("Error: " + num + " : " + msg);
        if (num != 401) { // Eror401 : user/channel not found. We don't need to release connection.
            releaseConnection();
        }
    }

    public void onInvite(String chan, IRCUser user, String passiveNick) {
        log.info("Invite: " + chan + " from " + user);
    }

    public void onJoin(String chan, IRCUser user) {
        log.info("Join: " + user + " joins " + chan);
    }

    public void onKick(String chan, IRCUser user, String passiveNick,
                       String msg) {
        log.info("Kick: " + chan + ": " + user + " (" + msg + ")");
    }

    public void onMode(String chan, IRCUser user, IRCModeParser modeParser) {
        log.info("Mode " + modeParser.getLine() + " (" + user + "@" + chan
                + ")");
    }

    public void onMode(IRCUser user, String passiveNick, String mode) {
        log.info("Mode: " + mode + " (" + user + ")");
    }

    public void onNick(IRCUser user, String newNick) {
        log.info("Nickname change:" + user + ": " + newNick);
    }

    public void onNotice(String target, IRCUser user, String msg) {
        log.info("Notice: " + target + " " + user + ": " + msg);
    }

    public void onPart(String chan, IRCUser user, String msg) {
        log.info("Part: " + chan + " " + user + " " + msg);
    }

    public void onPing(String ping) {
        log.info("Ping");
    }

    public void onPrivmsg(String target, IRCUser user, String msg) {
        log.info("Private Message: " + target + " " + user + " \"" + msg + "\"");
        String nickName = krazipIRCPublisher.getNickName().trim();
        if (!target.trim().equalsIgnoreCase(nickName)) {
            krazipIRCPublisher.responsePrivateMessage(user.toString(), msg);
        } else {
            krazipIRCPublisher.responsePrivatePrivateMessage(user.toString(), msg);
        }
    }

    public void onQuit(IRCUser user, String msg) {
        log.info("Quit: " + user + " " + msg);
    }

    public void onRegistered() {
        log.info("Registered");
    }

    public void onReply(int num, String value, String msg) {
        log.info("Reply: " + num + " " + value + " " + msg);
    }

    public void onTopic(String chan, IRCUser user, String topic) {
        log.info("Topic: " + chan + " " + user + " " + topic);
    }

    public void unknown(String prefix, String command, String middle,
                        String trailing) {
        log.warn("Unknown: " + command);
    }

    public void releaseConnection() {
        log.error("An error occurred. Releasing IRC connection and try connect again next build");
        if (KrazipIRCConnection.retrieveInstance() != null) {
            krazipIRCPublisher.ensureIrcConnection().doQuit();
        }
        KrazipIRCConnection.destroyInstance();
    }

}