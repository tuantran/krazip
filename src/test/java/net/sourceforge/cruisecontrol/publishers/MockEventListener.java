/*
 * Copyright 2010 ABC Tech Ltd. (Thailand)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sourceforge.cruisecontrol.publishers;

import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;

public class MockEventListener implements IRCEventListener {
    @Override
    public void onRegistered() {
        
    }

    @Override
    public void onDisconnected() {
        
    }

    @Override
    public void onError(String msg) {
        
    }

    @Override
    public void onError(int num, String msg) {
        
    }

    @Override
    public void onInvite(String chan, IRCUser user, String passiveNick) {
        
    }

    @Override
    public void onJoin(String chan, IRCUser user) {
        
    }

    @Override
    public void onKick(String chan, IRCUser user, String passiveNick, String msg) {
        
    }

    @Override
    public void onMode(String chan, IRCUser user, IRCModeParser modeParser) {
        
    }

    @Override
    public void onMode(IRCUser user, String passiveNick, String mode) {
        
    }

    @Override
    public void onNick(IRCUser user, String newNick) {
        
    }

    @Override
    public void onNotice(String target, IRCUser user, String msg) {
        
    }

    @Override
    public void onPart(String chan, IRCUser user, String msg) {
        
    }

    @Override
    public void onPing(String ping) {
        
    }

    @Override
    public void onPrivmsg(String target, IRCUser user, String msg) {
        
    }

    @Override
    public void onQuit(IRCUser user, String msg) {
        
    }

    @Override
    public void onReply(int num, String value, String msg) {
        
    }

    @Override
    public void onTopic(String chan, IRCUser user, String topic) {
        
    }

    @Override
    public void unknown(String prefix, String command, String middle, String trailing) {
        
    }
}
