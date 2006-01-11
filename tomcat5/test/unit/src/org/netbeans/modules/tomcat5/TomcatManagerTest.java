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

package org.netbeans.modules.tomcat5;

import java.io.File;
import javax.enterprise.deploy.spi.Target;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.tomcat5.util.TomcatProperties;
import org.netbeans.modules.tomcat5.util.TestUtils;

/**
 *
 * @author Radim Kubacki
 */
public class TomcatManagerTest extends NbTestCase {
    
    /** Bundled Tomcat deployment manager */
    private TomcatManager tm;
    private File datadir;
    
    public TomcatManagerTest (String testName) {
        super (testName);
    }
    
    protected void setUp () throws Exception {
        super.setUp ();
        tm = (TomcatManager)TomcatFactory55.create().getDeploymentManager(TomcatFactory55Test.TOMCAT_URI, null, null);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new TomcatManagerTest("testBundledTomcatDefaultPorts"));
        suite.addTest(new TomcatManagerTest("testGetUri"));
        suite.addTest(new TomcatManagerTest("testGetPorts"));
        suite.addTest(new TomcatManagerTest("testBundledTomcatDefaults"));
        suite.addTest(new TomcatManagerTest("testGetServerUri"));
        suite.addTest(new TomcatManagerTest("testGetTargets"));
        suite.addTest(new TomcatManagerTest("testIsRedeploySupported"));
        return suite;
    }
    
    /** Test of getUri method, of class org.netbeans.modules.tomcat5.TomcatManager. */
    public void testGetUri () {
        assertEquals ("Uri string doesn't match", TomcatFactory55Test.TOMCAT_URI, tm.getUri ());
    }
    
    public void testGetPorts() throws Exception {
        clearWorkDir();
        
        String home = getDataDir().getAbsolutePath() + "/server/home0";
        String base = getWorkDir().getAbsolutePath() + "/base_dir";
        
        String url = TomcatFactory55.tomcatUriPrefix;        
        url += "home=" + home + ":base=" + base;
        
        // register the test tomcat instance
        InstanceProperties ip = InstanceProperties.createInstanceProperties(
                url, "", "", "Test Tomcat");
        
        TomcatManager manager = (TomcatManager)TomcatFactory55.create().getDeploymentManager(url, null, null);
        
        assertEquals(9999, manager.getServerPort());
        assertEquals(7777, manager.getShutdownPort());
        
        manager.ensureCatalinaBaseReady();
        
        assertEquals(9999, manager.getServerPort());
        assertEquals(7777, manager.getShutdownPort());
        
    }
    
    public void testBundledTomcatDefaultPorts() {
        
        TestUtils.rm(tm.getTomcatProperties().getCatalinaBase());
        assertFalse(tm.getTomcatProperties().getCatalinaBase().exists());
        tm.getInstanceProperties().setProperty(TomcatProperties.PROP_SERVER_PORT, null);
        tm.getTomcatProperties().setTimestamp(0);
        assertEquals(8084, tm.getServerPort());
        
        TestUtils.rm(tm.getTomcatProperties().getCatalinaBase());
        assertFalse(tm.getTomcatProperties().getCatalinaBase().exists());
        tm.getInstanceProperties().setProperty(TomcatProperties.PROP_SHUTDOWN, null);
        tm.getTomcatProperties().setTimestamp(0);
        assertEquals(8025, tm.getShutdownPort());
        
        TestUtils.rm(tm.getTomcatProperties().getCatalinaBase());
        assertFalse(tm.getTomcatProperties().getCatalinaBase().exists());
        tm.ensureCatalinaBaseReady();
        assertEquals(8084, tm.getServerPort());
        
        TestUtils.rm(tm.getTomcatProperties().getCatalinaBase());
        assertFalse(tm.getTomcatProperties().getCatalinaBase().exists());
        tm.ensureCatalinaBaseReady();
        assertEquals(8025, tm.getShutdownPort());
    }
    
    public void testBundledTomcatDefaults() {
        assertEquals(tm.TOMCAT_55, tm.getTomcatVersion());
        assertTrue(tm.isTomcat55());
        assertFalse(tm.isTomcat50());
    }

    public void testGetServerUri() {
        assertEquals("http://localhost:8084", tm.getServerUri());
    }
    
    public void testGetTargets () {
        Target [] tgts = tm.getTargets ();
        assertTrue ("There should be one target", tgts != null && tgts.length == 1);
    }
    
    public void testIsRedeploySupported () {
        assertFalse(tm.isRedeploySupported ());
    }
    
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
}
