package net.sourceforge.cruisecontrol.publishers;

import junit.framework.Assert;
import net.sourceforge.cruisecontrol.CruiseControlException;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.junit.Test;
import org.schwering.irc.lib.IRCUser;

import java.util.List;

/**
 * Tests for KrazipIRCPublisher
 *
 * @author Pongvech Vechprasit (pun@abctech-thailand.com)
 */

public class KrazipIRCPublisherTest {
    private static final Logger log = Logger.getLogger(KrazipIRCPublisherTest.class);
    private static final String PASS = "pass";
    private static final String FAIL = "fail";
    private static final String FIXED = "fixed";
    private static final String OFF = "off";
    private KrazipIRCPublisher publisher;


    @Test
    public void testPublishPassMessage() throws Exception {
        List<String> messageLog;
        Element cruiseControlBuildLog = new KrazipIRCPublisherTest().createcruiseControlBuildLog(PASS);
        MockKrazipIRCPublisher mockPublisher = new MockKrazipIRCPublisher();
        mockPublisher.publish(cruiseControlBuildLog);
        messageLog = mockPublisher.getMessageLog();
        log.debug("\""+messageLog.get(0)+"\"");
        Assert.assertEquals("PRIVMSG testChannel :\"someProjectname\" build completed successfully.",messageLog.get(0));
    }

    @Test
    public void testPublishFailMessage() throws Exception {
        List<String> messageLog;
        Element cruiseControlBuildLog = new KrazipIRCPublisherTest().createcruiseControlBuildLog(FAIL);
        MockKrazipIRCPublisher mockPublisher = new MockKrazipIRCPublisher();
        mockPublisher.publish(cruiseControlBuildLog);
        messageLog = mockPublisher.getMessageLog();
        log.debug("\""+messageLog.get(0)+"\"");
        Assert.assertEquals("PRIVMSG testChannel :\"someProjectname\" build failed. Includes changes by someUser, someUser2.",messageLog.get(0));
    }

    @Test
    public void testPublishFixedMessage() throws Exception {
        List<String> messageLog;
        Element cruiseControlBuildLog = new KrazipIRCPublisherTest().createcruiseControlBuildLog(FIXED);
        MockKrazipIRCPublisher mockPublisher = new MockKrazipIRCPublisher();
        mockPublisher.publish(cruiseControlBuildLog);
        messageLog = mockPublisher.getMessageLog();
        log.debug("\""+messageLog.get(0)+"\"");
        Assert.assertEquals("PRIVMSG testChannel :\"someProjectname\" build fixed. Includes changes by someUser, someUser2.",messageLog.get(0));
    }

    @Test
    public void testLoggingLevelFailWithPassResult() throws Exception {
        List<String> messageLog;
        Element cruiseControlBuildLog = new KrazipIRCPublisherTest().createcruiseControlBuildLog(PASS);
        MockKrazipIRCPublisher mockPublisher = new MockKrazipIRCPublisher();
        mockPublisher.setLoggingLevel(FAIL);
        mockPublisher.publish(cruiseControlBuildLog);
        messageLog = mockPublisher.getMessageLog();
        Assert.assertEquals(0, messageLog.size());
    }

    @Test
    public void testLoggingLevelFailWithFailResult() throws Exception {
        List<String> messageLog;
        Element cruiseControlBuildLog = new KrazipIRCPublisherTest().createcruiseControlBuildLog(FAIL);
        MockKrazipIRCPublisher mockPublisher = new MockKrazipIRCPublisher();
        mockPublisher.setLoggingLevel(FAIL);
        mockPublisher.publish(cruiseControlBuildLog);
        messageLog = mockPublisher.getMessageLog();
        Assert.assertEquals(1, messageLog.size());
    }

    @Test
    public void testLoggingLevelOff() throws Exception {
        List<String> messageLog;
        Element cruiseControlBuildLog = new KrazipIRCPublisherTest().createcruiseControlBuildLog(FAIL);
        MockKrazipIRCPublisher mockPublisher = new MockKrazipIRCPublisher();
        mockPublisher.setLoggingLevel(OFF);
        mockPublisher.publish(cruiseControlBuildLog);
        messageLog = mockPublisher.getMessageLog();
        Assert.assertEquals(0, messageLog.size());
    }

    @Test
    public void testResponsePrivateMsgHelp() throws Exception {
        List<String> messageLog;
        MockKrazipIRCPublisher mockPublisher = new MockKrazipIRCPublisher();
        KrazipIRCListener listener = new KrazipIRCListener(mockPublisher);
        listener.onPrivmsg("#testChannel", new IRCUser("testNick", "testUser", "testHost"), "kraziptest help");
        messageLog = mockPublisher.getMessageLog();
        Assert.assertEquals("PRIVMSG testChannel :Usage : krazip [projectName] to display last build result for specified project, [follow {projectName}] to follow specified project, [unfollow {projectName}] to unfollow specified project, [list] to list currently following project, [help] to display this message, [logging] to display current logging level, [logging {PASS},{FAIL},{OFF}] to override global logging level", messageLog.get(0));
    }

    @Test
    public void testResponsePrivateMsgList() throws Exception {
        List<String> messageLog;
        MockKrazipIRCPublisher mockPublisher = new MockKrazipIRCPublisher();
        KrazipIRCListener listener = new KrazipIRCListener(mockPublisher);
        listener.onPrivmsg("#testChannel", new IRCUser("testNick", "testUser", "testHost"), "kraziptest list");
        messageLog = mockPublisher.getMessageLog();
        Assert.assertEquals("PRIVMSG testNick :Project list : \"someProjectname\".", messageLog.get(0));
        Assert.assertEquals("PRIVMSG testNick :You are not following any project", messageLog.get(1));
    }

    @Test
    public void testResponsePrivateMsgLogging() throws Exception {
        List<String> messageLog;
        MockKrazipIRCPublisher mockPublisher = new MockKrazipIRCPublisher();
        KrazipIRCListener listener = new KrazipIRCListener(mockPublisher);
        listener.onPrivmsg("#testChannel", new IRCUser("testNick", "testUser", "testHost"), "kraziptest logging");
        messageLog = mockPublisher.getMessageLog();
        Assert.assertEquals("PRIVMSG testChannel :Global logging level is : \"PASS\"", messageLog.get(0));
        listener.onPrivmsg("#testChannel", new IRCUser("testNick", "testUser", "testHost"), "kraziptest logging off");
        Assert.assertEquals(OFF, KrazipOverrideGlobalLogging.getOverrideValue());
    }

    @Test
    public void testResponsePrivateMsgFollow() throws Exception {
        List<String> messageLog;
        MockKrazipIRCPublisher mockPublisher = new MockKrazipIRCPublisher();
        KrazipIRCListener listener = new KrazipIRCListener(mockPublisher);
        listener.onPrivmsg("#testChannel", new IRCUser("testNick", "testUser", "testHost"), "kraziptest follow krazip");
        listener.onPrivmsg("#testChannel", new IRCUser("testNick", "testUser", "testHost"), "kraziptest unfollow krazip");
        messageLog = mockPublisher.getMessageLog();
        Assert.assertEquals("PRIVMSG testNick :You are now following project \"krazip\"", messageLog.get(0));
        Assert.assertEquals("PRIVMSG testNick :You have stopped following project \"krazip\"", messageLog.get(1));
    }

    @Test
    public void testResponsePrivateMsgUnfollowNotExist() throws Exception {
        List<String> messageLog;
        MockKrazipIRCPublisher mockPublisher = new MockKrazipIRCPublisher();
        KrazipIRCListener listener = new KrazipIRCListener(mockPublisher);
        listener.onPrivmsg("#testChannel", new IRCUser("testNick", "testUser", "testHost"), "kraziptest unfollow krazip");
        messageLog = mockPublisher.getMessageLog();
        Assert.assertEquals("PRIVMSG testNick :You are currently not following project \"krazip\"", messageLog.get(0));
    }

    @Test
    public void testBuildMessage() throws Exception {
        publisher = new KrazipIRCPublisher();
        publisher.setHost("irc.somehost.com");
        publisher.setChannel("#someChannel");
        publisher.setResultURL("http://www.someurl.com/someProjectName");
        KrazipIRCPublisherTest publisherTest = new KrazipIRCPublisherTest();
        Element cruiseControlBuildLog = publisherTest.createcruiseControlBuildLog(PASS);
        Assert.assertEquals
                ("\"someProjectname\" build completed successfully." ,
                        publisher.buildMessage(cruiseControlBuildLog));
    }

    @Test
    public void testBuildFailMessage() throws Exception {
        publisher = new KrazipIRCPublisher();
        publisher.setHost("irc.somehost.com");
        publisher.setChannel("#someChannel");
        publisher.setResultURL("http://www.someurl.com/someProjectName");
        KrazipIRCPublisherTest publisherTest = new KrazipIRCPublisherTest();
        Element cruiseControlBuildLog = publisherTest.createcruiseControlBuildLog(FAIL);
        Assert.assertEquals
                ("\"someProjectname\" build failed. Includes changes by someUser, " +
                        "someUser2. (" +
                        "http://www.someurl.com/someProjectName?log=log123456789)",
                        publisher.buildMessage(cruiseControlBuildLog));
    }

    @Test
    public void testValidate() throws Exception {
        publisher = new KrazipIRCPublisher();
        try {
            publisher.validate();
            Assert.fail("exception expected here because variables are not set");
        } catch (CruiseControlException e) {
            // Expected.
        }
    }

    @Test
    public void testChannel() throws Exception {
        publisher = new KrazipIRCPublisher();
        publisher.setChannel("#someChannel");
        Assert.assertEquals("#someChannel", publisher.getChannel());
    }

    @Test
    public void testHost() throws Exception {
        publisher = new KrazipIRCPublisher();
        publisher.setHost("irc.somehost.com");
        Assert.assertEquals("irc.somehost.com", publisher.getHost());
    }

    @Test
    public void testNickName() throws Exception {
        publisher = new KrazipIRCPublisher();
        publisher.setNickName("someNick");
        Assert.assertEquals("someNick", publisher.getNickName());
    }

    @Test
    public void testPort() throws Exception {
        publisher = new KrazipIRCPublisher();
        publisher.setPort(1983);
        Assert.assertEquals(1983, publisher.getPort());
    }

    @Test
    public void testUserName() throws Exception {
        publisher = new KrazipIRCPublisher();
        publisher.setUserName("someUserName");
        Assert.assertEquals("someUserName", publisher.getUserName());
    }

    @Test
    public void testRealName() throws Exception {
        publisher = new KrazipIRCPublisher();
        publisher.setRealName("someRealName");
        Assert.assertEquals("someRealName",publisher.getRealName());
    }

    @Test
    public void testResultURL() throws Exception {
        publisher = new KrazipIRCPublisher();
        publisher.setResultURL("http://www.someurl.com/");
        Assert.assertEquals("http://www.someurl.com/", publisher.getResultURL());
    }

    protected Element createcruiseControlBuildLog(String status){

        // START: building modifications element
        Element modifications = new Element("modifications");

        Element modification = new Element("modification");
        Element userElement = new Element("user");
        userElement.addContent("someUser");
        Element emailElement = new Element("email");
        emailElement.addContent("someEmail@someHost.com");

        Element modification2 = new Element("modification");
        Element userElement2 = new Element("user");
        userElement2.addContent("someUser2");
        Element emailElement2 = new Element("email");
        emailElement2.addContent("someEmail2@someHost.com");

        modification.addContent(userElement);
        modification.addContent(emailElement);
        modification2.addContent(userElement2);
        modification2.addContent(emailElement2);

        modifications.addContent(modification);
        modifications.addContent(modification2);
        // END: building modifications element

        // START: building 'info' element
        Element infoElement = new Element("info");

        Element logFileElement = new Element("property");
        logFileElement.setAttribute("name", "logfile");
        logFileElement.setAttribute("value", "log123456789.xml");

        Element projNameElement = new Element("property");
        projNameElement.setAttribute("name", "projectname");
        projNameElement.setAttribute("value", "someProjectname");

        Element lastsuccessfulbuild = new Element("property");
        lastsuccessfulbuild.setAttribute("name", "lastsuccessfulbuild");
        lastsuccessfulbuild.setAttribute("value", "20101027155629");


        Element lastbuildsuccessful = new Element("property");
        lastbuildsuccessful.setAttribute("name", "lastbuildsuccessful");
        if(status.equalsIgnoreCase(FIXED)){
            lastbuildsuccessful.setAttribute("value", "false");    
        } else {
            lastbuildsuccessful.setAttribute("value", "true");
        }

        Element ccTimeStamp = new Element("property");
        ccTimeStamp.setAttribute("name", "cctimestamp");
        ccTimeStamp.setAttribute("value", "20101109095534");

        infoElement.addContent(ccTimeStamp);
        infoElement.addContent(lastbuildsuccessful);
        infoElement.addContent(lastsuccessfulbuild);
        infoElement.addContent(projNameElement);
        infoElement.addContent(logFileElement);
        // END: building 'info' element

        // START: building build element
        Element buildElement = new Element("build");
        if (status.equalsIgnoreCase(FAIL)){
             buildElement.setAttribute("error","true");
        }
        // END: building build element

        Element createcruiseControlBuildLog = new Element("cruisecontrol");
        createcruiseControlBuildLog.addContent(modifications);
        createcruiseControlBuildLog.addContent(infoElement);
        createcruiseControlBuildLog.addContent(buildElement);

        return createcruiseControlBuildLog;
    }

}
