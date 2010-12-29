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

import net.sourceforge.cruisecontrol.CruiseControlException;
import net.sourceforge.cruisecontrol.Publisher;
import net.sourceforge.cruisecontrol.util.ValidationHelper;
import net.sourceforge.cruisecontrol.util.XMLLogHelper;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.schwering.irc.lib.IRCConnection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * In short, Krazip is a CruiseControl plug-in for sending a CruiseControl build result into IRC server.
 * However you can interact with Krazip via IRC channel by input Krazip known commands. Please refer to Krazip manual.
 * <p>Krazip requires IRClib for working.  (http://moepii.sourceforge.net/)
 *
 * @author Pongvech Vechprasit (pun@abctech-thailand.com)
 */
public class KrazipIRCPublisher implements Publisher {

    private static final Logger log = Logger.getLogger(KrazipIRCPublisher.class);
    private static final String PASS = "pass";
    private static final String FIXED = "fixed";
    private static final String FAIL = "fail";
    private static final String OFF = "off";
    private static final String HELP = "help";
    private static final String LIST = "list";
    private static final String LOGGING = "logging";
    private static final String UNFOLLOW = "unfollow";
    private static final String FOLLOW = "follow";
    private static final String QUOTATION = "\"";
    private static final String KRAZIP_PROPERTY_FILE = "krazip.properties";
    private static final int ONE_ARGUMENT_PASSED = 1;
    private static final int TWO_ARGUMENTS_PASSED = 2;
    private static final int THREE_ARGUMENTS_PASSED = 3;
    private static final int DEFAULT_IRC_PORT = 6667;
    private static List<KrazipBuildResult> krazipBuildList = new ArrayList<KrazipBuildResult>();
    private static List<KrazipFollowProject> krazipFollowList = new ArrayList<KrazipFollowProject>();

    private int port = DEFAULT_IRC_PORT;
    private String host;
    private String nickName = "Krazip";
    private String userName = "Krazip";
    private String realName = "Krazip CruiseControl IRC publisher";
    private String resultURL = "";
    private String channel;
    private String loggingLevel = FAIL;
    private String buildResult;
    private boolean useNotice = false;

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

    /**
     * Create a connection to IRC is connection is not existing.
     *
     * @return IRCConnection return an IRC connection that uses for sending messages to IRC server
     */
    protected IRCConnection ensureIrcConnection() {
        KrazipIRCConnection.establishInstance(host, port, nickName, userName, realName, channel, this);
        return KrazipIRCConnection.retrieveInstance();
    }

    /**
     * For sending a build message according to logging level and check for override global logging level from IRC
     *
     * @param cruiseControlBuildLog a build log from CruiseControl
     * @throws CruiseControlException throw <code>CruiseControlException</code> if something wrong
     */
    protected final void sendMessage(Element cruiseControlBuildLog) throws CruiseControlException {
        String message = buildMessage(cruiseControlBuildLog);
        if (!KrazipOverrideGlobalLogging.getOverrideValue().equalsIgnoreCase(OFF)) {
            if (buildResult != null && loggingLevel != null) {
                if (!KrazipOverrideGlobalLogging.getOverrideValue().equalsIgnoreCase("nothing")) {
                    loggingLevel = KrazipOverrideGlobalLogging.getOverrideValue();
                    log.info("Logging level has been overridden to : \"" + KrazipOverrideGlobalLogging.getOverrideValue().toUpperCase() + "\"");
                }
                if (loggingLevel.trim().equalsIgnoreCase(PASS)) {
                    log.info("Logging level: \"pass\" sending build result to IRC server...");
                    publishMessageToIrc(channel, message);
                } else if (loggingLevel.trim().equalsIgnoreCase(FAIL)) {
                    log.info("Logging level: \"fail\" sending only fail and fixed result to IRC server...");
                    if (buildResult.equals(FIXED) || buildResult.equals(FAIL)) {
                        publishMessageToIrc(channel, message);
                    }
                } else {
                    log.info("Logging level: \"off\" not sending any build result to IRC server...");
                }
            } else {
                log.error("ERROR: Could not retrieve buildResult or loggingLevel info");
            }
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
        if (ccBuildLog.isBuildFix()) {
            buildResult = FIXED;
            msg += "\"" + projectName + "\" build fixed. ";
            msg += "Includes changes by ";
            msg += retrieveBuildParticipant(ccBuildLog);
        } else if (ccBuildLog.isBuildSuccessful()) {
            buildResult = PASS;
            msg += "\"" + projectName + "\" build completed successfully";
        } else {
            buildResult = FAIL;
            msg += "\"" + projectName + "\" build failed. ";
            msg += "Includes changes by ";
            msg += retrieveBuildParticipant(ccBuildLog);
        }
        if (buildResult.equals(FAIL) && !resultURL.equals("")) {
            msg += ". (" + getResultURL(ccBuildLog) + ")";
        } else {
            msg += ".";
        }
        krazipBuildList.add(new KrazipBuildResult(projectName, msg, buildTimeStamp));
        log.info("buildResult added to krazipBuildList, Total number of items : " + krazipBuildList.size());
        sendMessageToFollower(projectName, msg);
        log.info("Sending build result to follower...");
        return msg;
    }

    /**
     * Get build participants from build log
     *
     * @param ccBuildLog A CruiseControl build log
     * @return String A list of build participant
     */
    protected String retrieveBuildParticipant(XMLLogHelper ccBuildLog) {
        log.info("Getting build participants list...");
        Set<String> changeSet = ccBuildLog.getBuildParticipants();
        int i = 1;
        StringBuilder sb = new StringBuilder();
        for (String name : changeSet) {
            log.info(changeSet.size() + " build participant(s) found...");
            String nameMapping = readNickFromMapping(name);
            sb.append(nameMapping);
            if (i < changeSet.size()) {
                sb.append(", ");
                i++;
            }
        }
        return sb.toString();
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
            log.error("ERROR: Could not get log file name", e);
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

    /**
     * Response to a private message.
     *
     * @param sender person who send the message
     * @param msg    the message body
     */
    protected void responsePrivateMessage(String sender, String msg) {
        String[] msgTmp = msg.split("\\s+");
        String krazipNickname = this.getNickName().trim();
        msgTmp[0] = msgTmp[0].replace(',', ' ').trim();
        msgTmp[0] = msgTmp[0].replace(':', ' ').trim();
        if (msgTmp[0].equalsIgnoreCase(krazipNickname)) {
            if (msgTmp.length == TWO_ARGUMENTS_PASSED) {
                treatSimpleCommand(sender, msgTmp[1].trim());
            } else if (msgTmp.length == THREE_ARGUMENTS_PASSED) {
                treatComplexCommand(sender, msgTmp[1].trim(),msgTmp[2].trim());
            }
        }
    }

    /**
     * Response to a <i>private</i> (Eg. "/msg krazip logging off") private message.
     *
     * @param sender person who send the message
     * @param msg    the message body
     */
    protected void responsePrivatePrivateMessage(String sender, String msg) {
        String[] msgTmp = msg.split("\\s+");
        if (msgTmp.length == ONE_ARGUMENT_PASSED) {
            treatSimpleCommand(sender, msgTmp[0].trim());
        } else if (msgTmp.length == TWO_ARGUMENTS_PASSED) {
            treatComplexCommand(sender, msgTmp[0].trim(), msgTmp[1].trim());
        }
    }

    /**
     * Response to following commands...
     * <ul>
     * <li>[help] - For display help message</li>
     * <li>[list] - To display project list and project that requester currently following</li>
     * <li>[logging] - To display current logging level</li>
     * </ul>
     * @param sender person who send the message
     * @param command Krazip's command
     */
    protected void treatSimpleCommand(String sender, String command) {
        if (command.equalsIgnoreCase(HELP)) {
            sendBuildResult(null, null, sender); // Send help
        } else if (command.equalsIgnoreCase(LIST)) {
            listProject(sender);
            listFollowingProject(sender);
        } else if (command.equalsIgnoreCase(LOGGING)) {
            getOverrideGlobalLoggingLevel();
        } else {
            sendBuildResult(findNewestBuildByName(krazipBuildList, command), command, sender);
        }
    }

    /**
     * Response to following commands...
     * <ul>
     * <li>[follow {projectName}] - To follow a project</li>
     * <li>[unfollow {projectName}] - To unfollow a project</li>
     * <li>[logging {PASS}{FAIL}{OFF}] - To override global logging level</li>
     * </ul>
     * @param sender person who send the message
     * @param command Krazip's command
     * @param target command's argument
     */
    protected void treatComplexCommand(String sender, String command, String target) {
        if (command.equalsIgnoreCase(FOLLOW)) {
            followProject(target, sender);
        } else if (command.equalsIgnoreCase(UNFOLLOW)) {
            unfollowProject(target, sender);
        } else if (command.equalsIgnoreCase(LOGGING)) {
            setOverrideGlobalLoggingLevel(target, sender, channel);
        }
    }

    /**
     * To follow the specified project. Add user to <code>krazipFollowList</code> If user not already exists in the list.
     *
     * @param requestedProjectName a project name that user wishes to follow
     * @param sender               a user that requested to follow
     */
    protected void followProject(String requestedProjectName, String sender) {
        boolean alreadyFollow = false;
        for (KrazipFollowProject aKrazipFollowList : krazipFollowList) {
            String projectNameTmp = aKrazipFollowList.getProjectName();
            String followerTmp = aKrazipFollowList.getFollower();
            if (projectNameTmp.equalsIgnoreCase(requestedProjectName.trim()) &&
                    followerTmp.equalsIgnoreCase(sender.trim())) {
                publishMessageToIrc(sender, "You are already following project \"" +
                        projectNameTmp + "\"");
                log.info(sender + " is already following " + projectNameTmp);
                alreadyFollow = true;
            }
        }
        if (!alreadyFollow) {
            krazipFollowList.add(new KrazipFollowProject(requestedProjectName, sender));
            publishMessageToIrc(sender, "You are now following project \"" + requestedProjectName + "\"");
            log.info("krazipFollowList = " + requestedProjectName + " : " + sender + " (ADDED) size=" +
                    krazipFollowList.size());
            log.info(sender + " is now following " + requestedProjectName);
        }
    }

    /**
     * To unfollow the specified project. Remove user from <code>krazipFollowList</code> If user exists in the list.
     *
     * @param requestedProjectName a project name that user wishes to unfollow
     * @param sender               a user that requested to unfollow
     */
    protected void unfollowProject(String requestedProjectName, String sender) {
        boolean found = false;
        for (int i = 0; i < krazipFollowList.size(); i++) {
            String projectName = krazipFollowList.get(i).getProjectName();
            String follower = krazipFollowList.get(i).getFollower();
            log.info("krazipFollowList = " + projectName + " : " + follower);
            if (projectName.equalsIgnoreCase(requestedProjectName.trim()) && follower.equalsIgnoreCase(sender.trim())) {
                krazipFollowList.remove(i);
                publishMessageToIrc(sender, "You have stopped following project \"" + projectName + "\"");
                log.info("krazipFollowList = " + projectName + " : " + follower + " (DELETED) size=" +
                        krazipFollowList.size());
                log.info(sender + " has stopped following " + projectName);
                found = true;
                break;
            }
        }
        if (!found) {
            publishMessageToIrc(sender, "You are currently not following project \"" +
                    requestedProjectName + "\"");
            log.info(sender + " is not currently following " + requestedProjectName);
        }
    }

    /**
     * For listing project that user following
     *
     * @param sender a user who requested the list
     */
    protected void listFollowingProject(String sender) {
        boolean found = false;
        StringBuilder msg = new StringBuilder();
        msg.append("You are following : ");
        for (int i = 0; i < krazipFollowList.size(); i++) {
            String projectName = krazipFollowList.get(i).getProjectName();
            String follower = krazipFollowList.get(i).getFollower();
            if (follower.equalsIgnoreCase(sender.trim())) {
                msg.append("\"").append(projectName).append("\"");
                if (i + 1 < krazipFollowList.size()) {
                    msg.append(", ");
                } else {
                    msg.append(".");
                }
                found = true;
            }
        }
        if (found) {
            publishMessageToIrc(sender, msg.toString());
            log.info(msg.toString());
        } else {
            publishMessageToIrc(sender, "You are not following any project");
            log.info(sender + " is not following any project");
        }
    }

    /**
     * For listing projects in KrazipBuildList
     *
     * @param sender a user who requested the list
     */
    protected void listProject(String sender) {
        boolean found = false;
        StringBuilder msg = new StringBuilder();
        msg.append("Project list : ");
        Set<String> projects = new HashSet<String>();
        for (KrazipBuildResult build : krazipBuildList) {
            projects.add(build.getProjectName());
            found = true;
        }
        Object[] toBeAdded = projects.toArray();
        for (int i = 0; i < toBeAdded.length; i++) {
            String projectName = toBeAdded[i].toString();
            msg.append("\"").append(projectName).append("\"");
            if (i + 1 < toBeAdded.length) {
                msg.append(", ");
            } else {
                msg.append(".");
            }
        }
        if (found) {
            publishMessageToIrc(sender, msg.toString());
            log.info(msg.toString());
        } else {
            publishMessageToIrc(sender, "CruiseControl has not built any projects since started.");
            log.info("Can't list project : CruiseControl has not built any projects since started");
        }
    }

    /**
     * Send build message to project's follower by querying the <code>krazipFollowList</code>
     *
     * @param projectName name of the project
     * @param msg         build message
     */
    protected void sendMessageToFollower(String projectName, String msg) {
        for (KrazipFollowProject aKrazipFollowList : krazipFollowList) {
            String followedProject = aKrazipFollowList.getProjectName();
            if (followedProject.equalsIgnoreCase(projectName)) {
                String follower = aKrazipFollowList.getFollower();
                publishMessageToIrc(follower, msg);
            }
        }
    }

    /**
     * For sending last build result and help message to IRC. If buildResult and requestedProjectName are null, Krazip will send help message.
     *
     * @param krazipBuildResult    last build result get from <code>findNewestBuildByName</code>
     * @param requestedProjectName project name that was requested to see the last build result
     * @param scope                message scope to be sent to IRC
     */
    protected void sendBuildResult(KrazipBuildResult krazipBuildResult, String requestedProjectName, String scope) {
        if (krazipBuildResult == null && requestedProjectName == null) {
            String helpMessage = "Usage : krazip [projectName] to display last build result for specified project, " +
                    "[follow {projectName}] to follow specified project, [unfollow {projectName}] to unfollow " +
                    "specified project, [list] to list currently following project, [help] to display this message" +
                    ", [logging] to display current logging level, [logging {PASS},{FAIL},{OFF}] to override global" +
                    " logging level";
            publishMessageToIrc(scope, helpMessage);
        } else {
            if (krazipBuildResult != null && krazipBuildResult.getMessage() != null) {
                // Send response message to IRC
                publishMessageToIrc(scope, krazipBuildResult.getMessage());
            } else {
                // Requested projectName not found in ArrayList
                publishMessageToIrc(scope, "Project name \"" + requestedProjectName +
                        "\" not found or it haven't been built" +
                        " since CruiseControl started (type \"krazip help\" for help)");
            }
        }
    }

    /**
     * Override current logging level with new logging level passed from IRC
     *
     * @param setting new logging level passed from IRC
     * @param sender  user that issued the command
     * @param scope   message scope to be sent to IRC
     */
    protected void setOverrideGlobalLoggingLevel(String setting, String sender, String scope) {

        if (setting.trim().equalsIgnoreCase(PASS) || setting.trim().equalsIgnoreCase(FAIL) ||
                setting.trim().equalsIgnoreCase(OFF)) {
            if (KrazipOverrideGlobalLogging.getOverrideValue().equalsIgnoreCase(setting)) {
                 publishMessageToIrc(scope, "Current global logging level is already at " +
                        QUOTATION + KrazipOverrideGlobalLogging.getOverrideValue().toUpperCase() + QUOTATION +
                        ". Keeping the current setting.");
            } else {
                KrazipOverrideGlobalLogging.setOverrideValue(setting);
                 publishMessageToIrc(scope, "Global logging level has been overridden to : " +
                        QUOTATION + KrazipOverrideGlobalLogging.getOverrideValue().toUpperCase() + QUOTATION + " by " + sender);
                log.info(sender + " has overridden global logging level to : " +
                        QUOTATION + KrazipOverrideGlobalLogging.getOverrideValue().toUpperCase() + QUOTATION);
            }

        } else {
             publishMessageToIrc(sender, "Incorrect logging level : {" + PASS.toUpperCase() +
                    "} {" + FAIL.toUpperCase() + "} {" + OFF.toUpperCase() + "}");
            log.info(sender + " has put incorrect global logging level :" + setting + " (IGNORED)");
        }
    }

    /**
     * Return current logging level to sender in IRC
     */
    protected void getOverrideGlobalLoggingLevel() {
        if (!KrazipOverrideGlobalLogging.getOverrideValue().equalsIgnoreCase("nothing")) {
             publishMessageToIrc(channel, "Global logging level has been overridden to :" +
                    " \"" + KrazipOverrideGlobalLogging.getOverrideValue().toUpperCase() + "\"");
        } else {
             publishMessageToIrc(channel, "Global logging level is : \"" + loggingLevel.toUpperCase() + "\"");
        }
    }

    /**
     * Find the build that matches the given project name.
     * <p> Scope is protected for the benefit of junit tests. </p>
     *
     * @param krazipBuildList a list that contains a history build list
     * @param projectName     a project name that we going to search in krazipBuildList
     * @return The newest build with the given project name, or null if none were found.
     */
    protected KrazipBuildResult findNewestBuildByName(List<KrazipBuildResult> krazipBuildList, String projectName) {
        List<KrazipBuildResult> reverse = new ArrayList<KrazipBuildResult>(krazipBuildList);
        Collections.reverse(reverse);
        for (KrazipBuildResult build : reverse) {
            if (build.getProjectName().trim().equalsIgnoreCase(projectName.trim())) {
                return build;
            }
        }
        return null;
    }

    /**
     * Look up participant mapping name with IRC user name in Krazip properties file
     *
     * @param participant key name to look in mapping file
     * @return IRC nick mapping to participant name if found
     */
    protected String readNickFromMapping(String participant) {
        String mappingName = participant;
        InputStream is = null;
        try {
            log.info("Reading Krazip properties file...");
            File propertyFile = new File(KRAZIP_PROPERTY_FILE);
            if (!propertyFile.exists()) {
                log.info("INFO : krazip.properties file not found, not using name mapping");
                return mappingName;
            }
            is = new FileInputStream(propertyFile);
            Properties properties = new Properties();
            properties.load(is);
            if (properties.containsKey(participant)) {
                mappingName = properties.getProperty(participant);
                log.info("Mapping name found for " + participant + ", as " + mappingName);
            }
        } catch (IOException e) {
            log.error("ERROR: Error in mapping name : " + e.getMessage());
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                log.error("ERROR:  Error closing file stream : " + e.getMessage());
            }
        }
        return mappingName;
    }

    /**
     * For publish message to IRC. By default, Krazip will use normal PRIVMSG method. But optionally user can set it to use
     * NOTICE method for sending message
     *
     * @param scope A publish scope
     * @param message A message to be published
     */
    protected void publishMessageToIrc(String scope, String message) {
        if (!useNotice){
            ensureIrcConnection().doPrivmsg(scope, message);
        } else {
            ensureIrcConnection().doNotice(scope, message);
        }
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
        ValidationHelper.assertIsSet(loggingLevel, "loggingLevel", this.getClass());
        ensureIrcConnection();
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

    public boolean isUseNotice() {
        return useNotice;
    }

    public void setUseNotice(boolean useNotice) {
        this.useNotice = useNotice;
    }
}
