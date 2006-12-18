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

import junit.textui.TestRunner;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.tomcat5.util.TestBase;

/**
 *
 * @author Radim Kubacki
 */
public class TomcatManagerTest extends TestBase {
    
    public TomcatManagerTest (String testName) {
        super(testName);
    }
    
    protected void setUp () throws Exception {
        super.setUp ();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new TomcatManagerTest("testGetPorts"));
        return suite;
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
        
        TomcatManager manager = (TomcatManager) TomcatFactory.create55().getDeploymentManager(url, null, null);
        
        assertEquals(9999, manager.getServerPort());
        assertEquals(7777, manager.getShutdownPort());
        
        manager.ensureCatalinaBaseReady();
        
        assertEquals(9999, manager.getServerPort());
        assertEquals(7777, manager.getShutdownPort());
        
    }
    
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
}
