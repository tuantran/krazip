package net.sourceforge.cruisecontrol.publishers;

import net.sourceforge.cruisecontrol.CruiseControlException;
import net.sourceforge.cruisecontrol.Publisher;
import net.sourceforge.cruisecontrol.util.ValidationHelper;
import net.sourceforge.cruisecontrol.util.XMLLogHelper;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.schwering.irc.lib.IRCConnection;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Krazip is for sending a simple CruiseControl build result into IRC.
 * <p>Krazip requires IRClib for working.  (http://moepii.sourceforge.net/)<br>
 *
 * @author Pongvech Vechprasit (pun@abctech-thailand.com)
 */

// This class will always generate a sonar findbugs error as it is not serializable.
// This has been approved by Erlend as OK

@edu.umd.cs.findbugs.annotations.SuppressWarnings("SE_BAD_FIELD")
public class KrazipIRCPublisher implements Publisher {

    private static final Logger log = Logger.getLogger(KrazipIRCPublisher.class);
    private static final int DEFAULT_IRC_PORT = 6667;
    private static final String PASS = "pass";
    private static final String FIXED = "fixed";
    private static final String FAIL = "fail";
    private static final int ONE_ARGUMENT_PASSED = 1;
    private static final int TWO_ARGUMENTS_PASSED = 2;
    private static final int THREE_ARGUMENTS_PASSED = 3;
    private static List<KrazipBuildResult> krazipBuildList = new ArrayList<KrazipBuildResult>();
    private static List<KrazipFollowProject> krazipFollowList = new ArrayList<KrazipFollowProject>();
    private int port = DEFAULT_IRC_PORT;
    private String host;
    private String nickName = "Krazip";
    private String userName = "Krazip";
    private String realName = "Krazip CruiseControl IRC publisher";
    private String resultURL;
    private String channel;
    private String loggingLevel = FAIL; // pass, fail(including fixed), off
    private String buildResult;
    private KrazipIRCConnection irc = null;

    /**
     * The main method for publishing build result into IRC. Firstly, initialize an IRC connection,
     * and then if the connection is OK, call sendMessage method. Which will build the message body by <code> buildMessage </code> method.
     * Finally, send the message according to logging level setting.
     *
     * @param cruiseControlBuildLog - A CruiseControl build log
     * @throws CruiseControlException on any error
     */
    public final void publish(Element cruiseControlBuildLog) throws CruiseControlException {

        ensureIrcConnection();
        sendMessage(cruiseControlBuildLog);

    }

    private IRCConnection ensureIrcConnection() {
        if (irc == null) {
            irc = KrazipIRCConnection.establishInstance(host, port, nickName, userName, realName, channel, this);
        }
        return KrazipIRCConnection.retrieveInstance();
    }

    protected final void sendMessage(Element cruiseControlBuildLog) throws CruiseControlException {

        String message = buildMessage(cruiseControlBuildLog);

        if (buildResult != null && loggingLevel != null) {
            if (loggingLevel.trim().equalsIgnoreCase(PASS)) {
                log.info("Logging level: \"pass\" sending build result to IRC server...");
                ensureIrcConnection().doPrivmsg(channel, message);
            } else if (loggingLevel.trim().equalsIgnoreCase(FAIL)) {
                log.info("Logging level: \"fail\" sending only fail and fixed result to IRC server...");
                if (buildResult.equals(FIXED) || buildResult.equals("fail")) {
                    ensureIrcConnection().doPrivmsg(channel, message);
                }
            } else {
                log.info("Logging level: \"off\" not sending any build result to IRC server...");
            }
        } else {
            log.error("Error: Could not retrieve buildResult or loggingLevel info");
        }
    }

    /**
     * Build the IRC body message from a CruiseControl build log
     *
     * @param cruiseControlBuildLog A CruiseControl build log
     * @return <code>String</code> as a body of IRC message
     * @throws CruiseControlException on any error
     */
    protected final String buildMessage(Element cruiseControlBuildLog) throws CruiseControlException {
        XMLLogHelper ccBuildLog = new XMLLogHelper(cruiseControlBuildLog);
        String projectName = ccBuildLog.getProjectName();
        String buildTimeStamp = ccBuildLog.getBuildTimestamp();
        String msg = "";
        if (ccBuildLog.isBuildSuccessful()) {
            buildResult = PASS;
            msg += "\"" + projectName + "\" build completed successfully.";
        } else if (ccBuildLog.isBuildFix()) {
            buildResult = FIXED;
            msg += "\"" + projectName + "\" build fixed.";
        } else {
            buildResult = FAIL;
            msg += "\"" + projectName + "\" build failed. ";
            msg += "Includes changes by ";
            Set<String> changeSet = ccBuildLog.getBuildParticipants();
            Iterator<String> iter = changeSet.iterator();
            StringBuilder sb = new StringBuilder();
            while (iter.hasNext()) {
                sb.append(iter.next());
                if (iter.hasNext()) {
                    sb.append(", ");
                }
            }
            msg += sb.toString();
        }
        if (!buildResult.equals(PASS)) {
            msg += ". (" + getResultURL(ccBuildLog) + ")";
        }
        krazipBuildList.add(new KrazipBuildResult(projectName, msg, buildTimeStamp));
        log.info("buildResult added to krazipBuildList, Total item : " + krazipBuildList.size());
        sendMessageToFollower(projectName, msg);
        log.info("Sending build result to follower...");
        return msg;
    }

    /**
     * Build the IRC body message from a CruiseControl build log.
     *
     * @param ccBuildLog <code>XMLLogHelper</code> wrapper for the build log
     * @return <code>String</code> as a URL for build log.
     */
    protected final String getResultURL(XMLLogHelper ccBuildLog) {
        String logFileName = "";
        try {
            logFileName = ccBuildLog.getLogFileName();
        } catch (CruiseControlException e) {
            log.error("Error: Could not get log file name", e);
        }
        String baseLogFileName =
                logFileName.substring(
                        logFileName.lastIndexOf(File.separator) + 1,
                        logFileName.lastIndexOf('.'));

        StringBuffer str = new StringBuffer();
        str.append(getResultURL());

        if (resultURL.indexOf('?') == -1) {
            str.append("?");
        } else {
            str.append("&");
        }

        str.append("log=");
        str.append(baseLogFileName);

        return str.toString();
    }


    public void responsePrivateMessage(String sender, String msg) {
        String[] msgTmp = msg.split(" ");
        String scope = channel;
        if (msgTmp[0].trim().equalsIgnoreCase("krazip")) {
            if (msgTmp.length == TWO_ARGUMENTS_PASSED) {
                if (msgTmp[1].trim().equalsIgnoreCase("help")) {
                    sendBuildResult(null, null, scope); // Send help
                } else if (msgTmp[1].trim().equalsIgnoreCase("list")) {
                    listFollowingProject(sender);
                } else {
                    sendBuildResult(getLastBuild(krazipBuildList, msgTmp[1]), msgTmp[1], scope);
                }
            } else if (msgTmp.length == THREE_ARGUMENTS_PASSED) {
                if (msgTmp[1].trim().equalsIgnoreCase("follow")) {
                    followProject(msgTmp[2], sender);
                } else if (msgTmp[1].trim().equalsIgnoreCase("unfollow")) {
                    unfollowProject(msgTmp[2], sender);
                }
            }
        }

    }

    public void responsePrivatePrivateMessage(String sender, String msg) {
        String[] msgTmp = msg.split(" ");
        if (msgTmp.length == ONE_ARGUMENT_PASSED) {
            if (msgTmp[0].trim().equalsIgnoreCase("help")) {
                sendBuildResult(null, null, sender); // Send help
            } else if (msgTmp[0].trim().equalsIgnoreCase("list")) {
                listFollowingProject(sender);
            } else {
                sendBuildResult(getLastBuild(krazipBuildList, msgTmp[0]), msgTmp[0], sender);
            }
        } else if (msgTmp.length == TWO_ARGUMENTS_PASSED) {
            if (msgTmp[0].trim().equalsIgnoreCase("follow")) {
                followProject(msgTmp[1], sender);
            } else if (msgTmp[0].trim().equalsIgnoreCase("unfollow")) {
                unfollowProject(msgTmp[1], sender);
            }
        }
    }

    public void followProject(String requestedProjectName, String sender) {
        KrazipBuildResult krazipBuildResult = getLastBuild(krazipBuildList, requestedProjectName);
        if (krazipBuildResult != null && krazipBuildResult.getProjectName() != null) {
            boolean alreadyFollow = false;
            for (int i = 0; i < krazipFollowList.size(); i++) {
                String projectNameTmp = krazipFollowList.get(i).getProjectName();
                String followerTmp = krazipFollowList.get(i).getFollower();
                if (projectNameTmp.equalsIgnoreCase(requestedProjectName.trim()) && followerTmp.equalsIgnoreCase(sender.trim())) {
                    ensureIrcConnection().doPrivmsg(sender, "You are already following project \"" + projectNameTmp + "\"");
                    log.info(sender + " is already following " + projectNameTmp);
                    alreadyFollow = true;
                }
            }
            if (!alreadyFollow) {
                String projectName = krazipBuildResult.getProjectName();
                krazipFollowList.add(new KrazipFollowProject(projectName, sender));
                ensureIrcConnection().doPrivmsg(sender, "You are now following project \"" + projectName + "\"");
                log.info("krazipFollowList = " + projectName + " : " + sender + " (ADDED) size=" + krazipFollowList.size());
                log.info(sender + " is now following " + projectName);
            }
        } else {
            sendBuildResult(null, requestedProjectName, sender);
            log.info(sender + " is trying to follow not existing project : " + requestedProjectName);
        }
    }

    public void unfollowProject(String requestedProjectName, String sender) {
        boolean found = false;
        for (int i = 0; i < krazipFollowList.size(); i++) {
            String projectName = krazipFollowList.get(i).getProjectName();
            String follower = krazipFollowList.get(i).getFollower();
            log.info("krazipFollowList = " + projectName + " : " + follower);
            if (projectName.equalsIgnoreCase(requestedProjectName.trim()) && follower.equalsIgnoreCase(sender.trim())) {
                krazipFollowList.remove(i);
                ensureIrcConnection().doPrivmsg(sender, "You are stop following project \"" + projectName + "\"");
                log.info("krazipFollowList = " + projectName + " : " + follower + " (DELETED) size=" + krazipFollowList.size());
                log.info(sender + " is stop following " + projectName);
                found = true;
                break;
            }
        }
        if (!found) {
            ensureIrcConnection().doPrivmsg(sender, "You are currently not following project \"" + requestedProjectName + "\"");
            log.info(sender + " is not currently following " + requestedProjectName);
        }
    }

    public void listFollowingProject(String sender) {
        boolean found = false;
        StringBuilder msg = new StringBuilder();
        msg.append("You are now following : ");
        for (int i = 0; i < krazipFollowList.size(); i++) {
            String projectName = krazipFollowList.get(i).getProjectName();
            String follower = krazipFollowList.get(i).getFollower();
            if (follower.equalsIgnoreCase(sender.trim())) {
                msg.append("\"" + projectName + "\"");
                if (i + 1 < krazipFollowList.size()) {
                    msg.append(", ");
                } else {
                    msg.append(".");
                }
                found = true;
            }
        }
        if (found) {
            ensureIrcConnection().doPrivmsg(sender, msg.toString());
            log.info(msg.toString());
        } else {
            ensureIrcConnection().doPrivmsg(sender, "You are not following any project");
            log.info(sender + " is not following any project");
        }
    }

    public void sendMessageToFollower(String projectName, String msg) {
        for (int i = 0; i < krazipFollowList.size(); i++) {
            String followedProject = krazipFollowList.get(i).getProjectName();
            if (followedProject.equalsIgnoreCase(projectName)) {
                String follower = krazipFollowList.get(i).getFollower();
                ensureIrcConnection().doPrivmsg(follower, msg);
            }
        }

    }

    public void sendBuildResult(KrazipBuildResult krazipBuildResult, String requestedProjectName, String scope) {
        if (krazipBuildResult == null && requestedProjectName == null) {
            String helpMessage = "Usage : krazip [projectName] to display last build result for specified project, " +
                    "[follow {projectName}] to follow specified project, [unfollow {projectName}] to unfollow " +
                    "specified project, [list] to list currently following project, [help] to display this message";
            ensureIrcConnection().doPrivmsg(scope, helpMessage);
        } else {
            if (krazipBuildResult != null && krazipBuildResult.getMessage() != null) {
                // Send response message to IRC
                ensureIrcConnection().doPrivmsg(scope, krazipBuildResult.getMessage());
            } else {
                // Requested projectName not found in ArrayList
                ensureIrcConnection().doPrivmsg(scope, "Project name \"" + requestedProjectName + "\" not found or it haven't been built" +
                        " since CruiseControl last re-started");
            }
        }
    }

    public KrazipBuildResult getLastBuild(List<KrazipBuildResult> krazipBuildList, String projectName) {
        KrazipBuildResult resultKrazip = new KrazipBuildResult();
        for (int i = krazipBuildList.size() - 1; i > -1; i--) {
            if (krazipBuildList.get(i).getProjectName().trim().equalsIgnoreCase(projectName.trim())) {
                resultKrazip = krazipBuildList.get(i);
                break;
            }
        }
        return resultKrazip;
    }


    /**
     * Called after the configuration is read to make sure that all the mandatory parameters were specified.
     *
     * @throws CruiseControlException if there was a configuration error.
     */
    public final void validate() throws CruiseControlException {
        ValidationHelper.assertIsSet(host, "host", this.getClass());
        ValidationHelper.assertIsSet(nickName, "nickName", this.getClass());
        ValidationHelper.assertIsSet(userName, "userName", this.getClass());
        ValidationHelper.assertIsSet(channel, "channel", this.getClass());
        ValidationHelper.assertIsSet(resultURL, "resultURL", this.getClass());
        ValidationHelper.assertIsSet(loggingLevel, "loggingLevel", this.getClass());
    }


    public final String getChannel() {
        return channel;
    }

    public final void setChannel(String channel) {
        this.channel = channel;
    }

    public final String getHost() {
        return host;
    }

    public final void setHost(String host) {
        this.host = host;
    }

    public final String getNickName() {
        return nickName;
    }

    public final void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public final int getPort() {
        return port;
    }

    public final void setPort(int port) {
        this.port = port;
    }

    public final String getUserName() {
        return userName;
    }

    public final void setUserName(String userName) {
        this.userName = userName;
    }

    public final String getRealName() {
        return realName;
    }

    public final void setRealName(String realName) {
        this.realName = realName;
    }

    public final String getResultURL() {
        return resultURL;
    }

    public final void setResultURL(String resultURL) {
        this.resultURL = resultURL;
    }

    public String getLoggingLevel() {
        return loggingLevel;
    }

    public void setLoggingLevel(String loggingLevel) {
        this.loggingLevel = loggingLevel;
    }

}
