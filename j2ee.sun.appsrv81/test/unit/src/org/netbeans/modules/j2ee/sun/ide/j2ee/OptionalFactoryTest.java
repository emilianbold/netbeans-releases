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
/*
 * OptionalFactoryTest.java
 * JUnit based test
 *
 * Created on March 30, 2004, 2:16 PM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import javax.enterprise.deploy.spi.DeploymentManager;
import junit.framework.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.api.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.api.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.api.StartServer;
import org.netbeans.modules.j2ee.deployment.plugins.api.TargetModuleIDResolver;
import org.netbeans.modules.j2ee.sun.ide.j2ee.jsps.FindJSPServletImpl;
import org.netbeans.modules.j2ee.sun.ide.j2ee.incrdeploy.DirectoryDeploymentFacade;


import org.netbeans.modules.j2ee.sun.share.SunDeploymentManager;

/**
 *
 * @author ludo
 */
public class OptionalFactoryTest extends TestCase {
    private SunDeploymentManager dm;
    public OptionalFactoryTest(java.lang.String testName) {
        super(testName);
        dm =new SunDeploymentManager(null,null,"localhost",4848);
    }
    
    /**
     * Test of getFindJSPServlet method, of class org.netbeans.modules.j2ee.sun.ide.j2ee.OptionalFactory.
     */
    public void testGetFindJSPServlet() {
        System.out.println("testGetFindJSPServlet");
        OptionalFactory f = new OptionalFactory();
        assertTrue(null!=f.getFindJSPServlet(dm));
        
        // TODO add your test code below by replacing the default call to fail.
     //   fail("The test case is empty.");
    }
    
    /**
     * Test of getIncrementalDeployment method, of class org.netbeans.modules.j2ee.sun.ide.j2ee.OptionalFactory.
     */
    public void testGetIncrementalDeployment() {
        System.out.println("testGetIncrementalDeployment");
        OptionalFactory f = new OptionalFactory( );
        assertTrue(null!=f.getIncrementalDeployment(dm));
        
        // TODO add your test code below by replacing the default call to fail.
    //    fail("The test case is empty.");
    }
    
    /**
     * Test of getStartServer method, of class org.netbeans.modules.j2ee.sun.ide.j2ee.OptionalFactory.
     */
    public void testGetStartServer() {
        System.out.println("testGetStartServer");
        OptionalFactory f = new OptionalFactory( );
        assertTrue(null!=f.getStartServer(dm));
        
        // TODO add your test code below by replacing the default call to fail.
      //  fail("The test case is empty.");
    }
    
    /**
     * Test of getTargetModuleIDResolver method, of class org.netbeans.modules.j2ee.sun.ide.j2ee.OptionalFactory.
     */
    public void testGetTargetModuleIDResolver() {
        System.out.println("testGetTargetModuleIDResolver");
        OptionalFactory f = new OptionalFactory( );
        assertTrue(null==f.getTargetModuleIDResolver(dm));

    }
    
    // TODO add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    
}
