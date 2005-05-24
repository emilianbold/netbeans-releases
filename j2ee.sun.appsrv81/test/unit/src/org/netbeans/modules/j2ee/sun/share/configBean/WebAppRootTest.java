/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * WebAppRootTest.java
 * JUnit based test
 *
 * Created on February 23, 2004, 5:22 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean;

import junit.framework.*;
import org.netbeans.modules.j2ee.sun.share.SunDeploymentFactory;
import org.netbeans.modules.j2ee.sun.share.MockDeployableObject;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.sun.share.SunDeploymentManager;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;
import javax.enterprise.deploy.spi.DConfigBeanRoot;
import javax.enterprise.deploy.spi.DConfigBean;

import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

/**
 *
 * @author vkraemer
 */
public class WebAppRootTest extends TestCase {
    
    // requires a fix to Base.constructFileName...
    //
    /*public void testGetXPaths() {
        doTestGetXPaths("");
    }*/

    public void testGetXPathsAbsolutely() {
        doTestGetXPaths("/web-app/");
    }
    
    private void doTestGetXPaths(String prefix) {
        MockDDBeanRoot waddbean = new MockDDBeanRoot();
        waddbean.setXpath("/web-app");
        waddbean.setRoot(waddbean);
        WebAppRoot war = null;
        try {
            war = (WebAppRoot) DC.getDConfigBeanRoot(waddbean);
            assertNotNull(war);
        }
        catch (ConfigurationException ce) {
            fail("this should not fail");
        }
        String[] xpaths = war.getXpaths();
        assertNotNull(xpaths);
        assertEquals(6, xpaths.length);
        for (int i = 0; i < xpaths.length; i++) {
            MockDDBean mddb = new MockDDBean();
            mddb.setXpath(prefix+xpaths[i]);
            mddb.setRoot(waddbean);
            DConfigBean dcb = null;
            try {
                dcb = war.getDConfigBean(mddb);
                assertNotNull("got null dcb for xpath: "+xpaths[i],dcb);
                if (null != dcb)
                    try {
                        war.removeDConfigBean(dcb);
                    }
                    catch (javax.enterprise.deploy.spi.exceptions.BeanNotFoundException bnfe) {
                        fail("got a bean not found, I should not!");
                    }
            }
            catch (ConfigurationException ce) {
                ce.printStackTrace();
                fail("got a ce for xpath: "+xpaths[i]);
            }
            try {
                war.removeDConfigBean(dcb);
                fail("I should not be able to find the dcb... I had removed it");
            }
            catch (javax.enterprise.deploy.spi.exceptions.BeanNotFoundException bnfe) {
                assertTrue(bnfe.getMessage(),bnfe.getMessage().startsWith("No match for bean"));
            }
        }
    }

    
    
    public void testProperCreate() {
        MockDDBeanRoot waddbean = new MockDDBeanRoot();
        WebAppRoot war = null;
        try {
            war = (WebAppRoot) DC.getDConfigBeanRoot(null);
            fail(war.toString());
        }
        catch (ConfigurationException ce) {
            assertEquals("DDBean cannot be null.",ce.getMessage());
        }
        try {
            war = (WebAppRoot) DC.getDConfigBeanRoot(waddbean);
            fail(war.toString());
        }
        catch (ConfigurationException ce) {
            assertEquals("DDBean cannot have a null Xpath.",ce.getMessage());
        }
        waddbean.setXpath("/web-app");
        waddbean.setRoot(waddbean);
        try {
            war = (WebAppRoot) DC.getDConfigBeanRoot(waddbean); 
            assertTrue(war.isValid());
        }
        catch (ConfigurationException ce) {
            fail("I should not fail here");
        }
    }
    
    static SunDeploymentFactory DF = new SunDeploymentFactory();
    static DeploymentManager DM = null;
    static DeploymentConfiguration DC = null;
    static DConfigBeanRoot WAR = null;
    static {
        try {
            DM = DF.getDisconnectedDeploymentManager("deployer:Sun:AppServer::localhost:4848");
            DC =  DM.createConfiguration(new MockDeployableObject());
//            WAR = DC.getDConfigBeanRoot(new MockDDBeanRoot());
        }
        catch (Throwable t) {
            fail(t.getMessage());
        }
    }

    public WebAppRootTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(WebAppRootTest.class);
        return suite;
    }
}
    
