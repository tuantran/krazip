package net.sourceforge.cruisecontrol.publishers;

import junit.framework.Assert;
import net.sourceforge.cruisecontrol.CruiseControlException;
import org.jdom.Element;
import org.junit.Test;

/**
 * Tests for KrazipIRCPublisher
 *
 * @author Pongvech Vechprasit (pun@abctech-thailand.com)
 */

public class KrazipIRCPublisherTest {

    private KrazipIRCPublisher publisher;

    @Test
    public void testBuildMessage() throws Exception {
        publisher = new KrazipIRCPublisher();
        publisher.setHost("irc.somehost.com");
        publisher.setChannel("#someChannel");
        publisher.setResultURL("http://www.someurl.com/someProjectName");
        KrazipIRCPublisherTest publisherTest = new KrazipIRCPublisherTest();
        Element cruiseControlBuildLog = publisherTest.createcruiseControlBuildLog();
        Assert.assertEquals
                ("Build completed successfully for project \"someProjectname\". " +
                        "Please see more details at http://www.someurl.com/someProjectName?log=log123456789",
                        publisher.buildMessage(cruiseControlBuildLog));
    }

    @Test
    public void testValidatePass() throws Exception {
        publisher = new KrazipIRCPublisher();
        publisher.setHost("irc.somehost.com");
        publisher.setChannel("#someChannel");
        publisher.setResultURL("http://www.someurl.com/");
        try {
            publisher.validate();
        } catch (CruiseControlException e) {
            Assert.fail("must NOT throw exception if everything are set");
        }
    }

    @Test
    public void testValidateFail() throws Exception {
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

    protected Element createcruiseControlBuildLog(){
        Element createcruiseControlBuildLog = new Element("cruisecontrol");
        Element infoElement = new Element("info");

        Element logFileElement = new Element("property");
        logFileElement.setAttribute("name", "logfile");
        logFileElement.setAttribute("value", "log123456789.xml");

        Element projNameElement = new Element("property");
        projNameElement.setAttribute("name", "projectname");
        projNameElement.setAttribute("value", "someProjectname");

        infoElement.addContent(projNameElement);
        infoElement.addContent(logFileElement);

        createcruiseControlBuildLog.addContent(infoElement);
        
        Element buildElement = new Element("build");
        createcruiseControlBuildLog.addContent(buildElement);

        return createcruiseControlBuildLog;
    }
}
