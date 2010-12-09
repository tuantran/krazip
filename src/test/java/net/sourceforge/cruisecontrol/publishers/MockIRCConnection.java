package net.sourceforge.cruisecontrol.publishers;

import org.apache.log4j.Logger;
import org.schwering.irc.lib.IRCConnection;

import java.util.ArrayList;
import java.util.List;

public class MockIRCConnection extends IRCConnection {
    private static final Logger log = Logger.getLogger(MockIRCConnection.class);
    private List<String> messageLog = new ArrayList<String>();

    public MockIRCConnection() {
        super("localhost", new int[]{6667}, "mock", "mockNick", "junit", "test");
    }

    public List<String> getMessageLog() {
        return messageLog;
    }

    @Override
    public void send(String line) {
        //log.debug("send: "+line);
        messageLog.add(line);
    }

}
