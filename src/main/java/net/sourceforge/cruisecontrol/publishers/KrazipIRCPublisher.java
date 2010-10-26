package net.sourceforge.cruisecontrol.publishers;

import net.sourceforge.cruisecontrol.CruiseControlException;
import net.sourceforge.cruisecontrol.Publisher;
import net.sourceforge.cruisecontrol.util.ValidationHelper;
import net.sourceforge.cruisecontrol.util.XMLLogHelper;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.schwering.irc.lib.IRCConnection;
import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

/**
 * Krazip is for sending a simple CruiseControl build result into IRC.
 * <p>Krazip requires IRClib for working.  (http://moepii.sourceforge.net/)<br>
 *
 * @author Pongvech Vechprasit
 */

public class KrazipIRCPublisher implements Publisher {


    private static final Logger LOG = Logger.getLogger(KrazipIRCPublisher.class);
    private IRCConnection IRCconnection;
    private String host;
    private int port = 6667;
    private String nickName = "Krazip";
    private String userName = "Krazip";
    private String realName = "Krazip CruiseControl IRC publisher";
    private String channel;
    private String quitMessage = "G'day mate!";
    private String resultURL;

    /**
     * The main method for publishing build result into IRC. Firstly, initialize an IRC connection,
     * and then if the connection is OK, build the message body by <code> buildMessage </code> method.
     * Finally, send the message and quit the IRC.
     *
     * @param cruiseControlBuildLog - A CruiseControl build log
     * @throws CruiseControlException on any error
     */
    public void publish(Element cruiseControlBuildLog) throws CruiseControlException {

        init();
        try {
            IRCconnection.connect();
        } catch (IOException ioe) {
            LOG.error("Error: Could not connect to IRC server", ioe);
        }
        IRCconnection.doJoin(channel);
        String message = buildMessage(cruiseControlBuildLog);
        IRCconnection.doPrivmsg(channel, message);
        IRCconnection.doQuit(quitMessage);
    }

    /**
     * Initialize an IRC connection.
     */
    protected void init() {
        IRCconnection = new IRCConnection(host, new int[]{port}, null, nickName, userName, realName);
        IRCconnection.addIRCEventListener(new Listener());
        IRCconnection.setEncoding("UTF-8");
        IRCconnection.setPong(true);
        IRCconnection.setDaemon(false);
        IRCconnection.setColors(true);
    }

    /**
     * Build the IRC body message from a CruiseControl build log
     *
     * @param cruiseControlBuildLog A CruiseControl build log
     * @return <code>String</code> as a body of IRC message
     * @throws CruiseControlException on any error
     */
    protected String buildMessage(Element cruiseControlBuildLog) throws CruiseControlException {
        XMLLogHelper ccBuildLog = new XMLLogHelper(cruiseControlBuildLog);
        String msg = "";
        if (ccBuildLog.isBuildSuccessful()) {
            msg += "Build completed successfully : " + ccBuildLog.getProjectName();
        } else if (ccBuildLog.isBuildFix()) {
            msg += "Build fixed : " + ccBuildLog.getProjectName();
        } else {
            msg += "Build FAILURE : " + ccBuildLog.getProjectName() + ". ";
            msg += "Includes changes by ";
            Set changeSet = ccBuildLog.getBuildParticipants();
            Iterator iter = changeSet.iterator();
            while (iter.hasNext()) {
                String modname = (String) iter.next();
                msg += modname;
                if (iter.hasNext()) {
                    msg += ", ";
                }
            }
        }
        msg += ". Please see more details : " + getResultURL(ccBuildLog);
        return msg;
    }

    /**
     * Build the IRC body message from a CruiseControl build log
     *
     * @param ccBuildLog <code>XMLLogHelper</code> - wrapper for the build log
     * @return <code>String</code> as a URL for build log.
     */
    protected String getResultURL(XMLLogHelper ccBuildLog) {
        String logFileName = "";
        try {
            logFileName = ccBuildLog.getLogFileName();
        } catch (CruiseControlException e) {
            LOG.error("Error: Could not get log file name", e);
        }
        String baseLogFileName =
                logFileName.substring(
                        logFileName.lastIndexOf(File.separator) + 1,
                        logFileName.lastIndexOf("."));

        StringBuffer str = new StringBuffer();
        str.append(getResultURL());

        if (resultURL.indexOf("?") == -1) {
            str.append("?");
        } else {
            str.append("&");
        }

        str.append("log=");
        str.append(baseLogFileName);

        return str.toString();
    }

    /**
     * Called after the configuration is read to make sure that all the mandatory parameters were specified..
     *
     *  @throws CruiseControlException if there was a configuration error.
     */
    public void validate() throws CruiseControlException {
        ValidationHelper.assertIsSet(host, "host", this.getClass());
        ValidationHelper.assertIsSet(nickName, "nickName", this.getClass());
        ValidationHelper.assertIsSet(userName, "userName", this.getClass());
        ValidationHelper.assertIsSet(channel, "channel", this.getClass());
        ValidationHelper.assertIsSet(resultURL, "resultURL", this.getClass());
    }


    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getQuitMessage() {
        return quitMessage;
    }

    public void setQuitMessage(String quitMessage) {
        this.quitMessage = quitMessage;
    }

    public String getResultURL() {
        return resultURL;
    }

    public void setResultURL(String resultURL) {
        this.resultURL = resultURL;
    }

    /**
     * Implementation of IRCEventListener
     *
     * <i>Quote from <code>org.schwering.irc.lib.IRCEventListener</code></i>
     *
     * Used as listener for incoming events like messages.
     * <p>
     * The <code>IRCEventListener</code> is used by the
     * <code>IRCConnection.addEventListener(IRCEventListener)</code> method to add
     * a listener which listens to the connection for incoming IRC events like
     * <code>PRIVMSG</code>s or numeric replies...
     * </p>
     *
     */
    public class Listener implements IRCEventListener {

        public void onDisconnected() {
            LOG.info("Disconnected");
        }

        public void onError(String msg) {
            LOG.warn("Error: " + msg);
        }

        public void onError(int num, String msg) {
            LOG.warn("Error: " + num + " : " + msg);
        }

        public void onInvite(String chan, IRCUser user, String passiveNick) {
            LOG.info("Invite: " + chan + " from " + user);
        }

        public void onJoin(String chan, IRCUser user) {
            LOG.info("Join: " + chan);
        }

        public void onKick(String chan, IRCUser user, String passiveNick,
                           String msg) {
            LOG.info("Kick: " + chan + ": " + user + " (" + msg + ")");
        }

        public void onMode(String chan, IRCUser user, IRCModeParser modeParser) {
            LOG.info("Mode " + modeParser.getLine() + " (" + user + "@" + chan
                    + ")");
        }

        public void onMode(IRCUser user, String passiveNick, String mode) {
            LOG.info("Mode: " + mode + " (" + user + ")");
        }

        public void onNick(IRCUser user, String newNick) {
            LOG.info("Nickname change:" + user + ": " + newNick);
        }

        public void onNotice(String target, IRCUser user, String msg) {
            LOG.info("Notice: " + target + " " + user + ": " + msg);
        }

        public void onPart(String chan, IRCUser user, String msg) {
            LOG.info("Part: " + chan + " " + user + " " + msg);
        }

        public void onPing(String ping) {
            LOG.info("Ping");
        }

        public void onPrivmsg(String target, IRCUser user, String msg) {
            LOG.info("Private Message: " + target + " " + user + " " + msg);
        }

        public void onQuit(IRCUser user, String msg) {
            LOG.info("Quite: " + user + " " + msg);
        }

        public void onRegistered() {
            LOG.info("Registered");
        }

        public void onReply(int num, String value, String msg) {
            LOG.info("Reply: " + num + " " + value + " " + msg);
        }

        public void onTopic(String chan, IRCUser user, String topic) {
            LOG.info("Topic: " + chan + " " + user + " " + topic);
        }

        public void unknown(String prefix, String command, String middle,
                            String trailing) {
            LOG.warn("Unknown: " + command);
        }

    }
}
