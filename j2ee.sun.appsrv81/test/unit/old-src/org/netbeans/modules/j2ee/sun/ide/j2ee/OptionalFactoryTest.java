/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.j2ee.deployment.plugins.spi.TargetModuleIDResolver;
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
