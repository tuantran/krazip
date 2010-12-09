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
