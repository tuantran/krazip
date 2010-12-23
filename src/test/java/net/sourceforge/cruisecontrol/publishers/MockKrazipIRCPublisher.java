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

import org.schwering.irc.lib.IRCConnection;

import java.util.List;

public class MockKrazipIRCPublisher extends KrazipIRCPublisher {

    private MockIRCConnection mockConn = null;

    public MockKrazipIRCPublisher () {
        mockConn = new MockIRCConnection();
        super.setLoggingLevel("pass");
        super.setHost("irc.somehost.com");
        super.setPort(6667);
        super.setNickName("KrazipTest");
        super.setChannel("testChannel");
    }
    @Override
    protected IRCConnection ensureIrcConnection() {
        return mockConn;
    }

    protected List<String> getMessageLog() {
        return mockConn.getMessageLog();    
    }
}
