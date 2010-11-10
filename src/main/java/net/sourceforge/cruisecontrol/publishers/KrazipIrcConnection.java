package net.sourceforge.cruisecontrol.publishers;

import org.apache.log4j.Logger;
import org.schwering.irc.lib.IRCConnection;
import org.schwering.irc.lib.IRCEventListener;

import java.io.IOException;


public final class KrazipIrcConnection {

    private static final Logger log = Logger.getLogger(KrazipIrcConnection.class);

    private static KrazipIrcConnection instance;
    private IRCConnection realConnection;

    private KrazipIrcConnection(String host, int port, String nickName, String userName, String realName, String channel, IRCEventListener listener) {
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

    public static synchronized KrazipIrcConnection establishInstance(String host, int port, String nickName, String userName, String realName, String channel, KrazipIRCPublisher krazip) {
        if ( instance == null ) {
            instance = new KrazipIrcConnection(host, port, nickName, userName, realName, channel, new Listener( krazip ));
        }
        return instance;
    }

    public static synchronized IRCConnection retrieveInstance() {
        if ( instance == null) {
            throw new KrazipUsageException("You should always establish the instance before using it.");            
        }
        return instance.realConnection;
    }
}
