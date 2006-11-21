/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5;

import java.io.File;
import javax.enterprise.deploy.spi.Target;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.tomcat5.TomcatManager.TomcatVersion;
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
    
    public static final String BUNDLED_TOMCAT_URI = "tomcat55:home=$bundled_home:base=$bundled_base";
    
    public TomcatManagerTest (String testName) {
        super (testName);
    }
    
    protected void setUp () throws Exception {
        super.setUp ();
        tm = (TomcatManager)TomcatFactory.create55().getDeploymentManager(BUNDLED_TOMCAT_URI, null, null);
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
        assertEquals ("Uri string doesn't match", BUNDLED_TOMCAT_URI, tm.getUri ());
    }
    
    public void testGetPorts() throws Exception {
        clearWorkDir();
        
        String home = getDataDir().getAbsolutePath() + "/server/home0";
        String base = getWorkDir().getAbsolutePath() + "/base_dir";
        
        String url = TomcatFactory.TOMCAT_URI_PREFIX_55;        
        url += "home=" + home + ":base=" + base;
        
        // register the test tomcat instance
        InstanceProperties ip = InstanceProperties.createInstanceProperties(
                url, "", "", "Test Tomcat");
        
        TomcatManager manager = (TomcatManager)TomcatFactory.create55().getDeploymentManager(url, null, null);
        
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
        assertEquals(TomcatVersion.TOMCAT_55, tm.getTomcatVersion());
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
