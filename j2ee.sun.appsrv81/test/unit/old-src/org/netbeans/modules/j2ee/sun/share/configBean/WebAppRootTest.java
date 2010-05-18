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
    
}
    
