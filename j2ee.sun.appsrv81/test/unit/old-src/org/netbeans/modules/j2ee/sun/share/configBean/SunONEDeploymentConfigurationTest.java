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
 * SunONEDeploymentConfigurationTest.java
 * JUnit based test
 *
 * Created on March 11, 2004, 12:24 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.io.StringWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import junit.framework.*;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.Schema2BeansRuntimeException;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DConfigBeanRoot;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import org.netbeans.modules.j2ee.sun.share.Constants;
import org.netbeans.modules.j2ee.sun.share.SunDeploymentManager;
import org.netbeans.modules.j2ee.sun.share.plan.DeploymentPlan;
import org.netbeans.modules.j2ee.sun.share.plan.FileEntry;
import org.netbeans.modules.j2ee.sun.common.dd.webapp.SunWebApp;

import org.netbeans.modules.j2ee.sun.share.SunDeploymentFactory;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.sun.share.MockDeployableObject;
import org.netbeans.modules.j2ee.sun.share.TestConstants;
/**
 *
 * @author vkraemer
 */
public class SunONEDeploymentConfigurationTest extends TestCase {
    
    public void testRemove() {
        WebAppRoot war = null;
        try {
            war = (WebAppRoot) dc2.getDConfigBeanRoot(waddbean);
            assertNotNull(war);
            dc2.removeDConfigBean(war);
            //war = (WebAppRoot) dc2.getDConfigBeanRoot(waddbean);
            dc2.removeDConfigBean(war);
            fail("I should not be here");
        }
        catch (BeanNotFoundException bnfe) {
            assertTrue(bnfe.getMessage().startsWith("No match for"));
        }
        catch (ConfigurationException ce) {
            ce.printStackTrace();
        }
    }
        
    
    public void testDirectoryDeploySupport() {
            WebAppRoot war = null;
            try {
                war = (WebAppRoot) warDC.getDConfigBeanRoot(waddbean); 
                assertNotNull(war);
                war.setContextRoot("myTestCR");
                java.io.File f = new java.io.File("sun-web.xml");
                f.createNewFile();
                warDC.extractFileFromPlanForModule(f, warDC.getDeployableObject()); // DeployableObject mod)
                war.setContextRoot("anotherCR");
                warDC.addFileToPlanForModule(f, warDC.getDeployableObject());
                assertEquals(war.getContextRoot(), "myTestCR");
            }
            catch (java.io.IOException ioe) {
                ioe.printStackTrace();
                fail("got an ioe");
            }
            catch (java.beans.PropertyVetoException pve) {
                pve.printStackTrace();
                fail("got a pve");
            }
            catch (ConfigurationException ce) {
                ce.printStackTrace();
                fail("go a ce");
            }
    }
    
    public void testContextRootStuff() {
            WebAppRoot war = null;
            try {
                war = (WebAppRoot) warDC.getDConfigBeanRoot(waddbean); 
                assertNotNull(war);
                war.setContextRoot("testContextRootValue");
                assertEquals(war.getContextRoot(), warDC.getContextRoot());
                warDC.setContextRoot("newContextRoot");
                assertEquals(war.getContextRoot(), warDC.getContextRoot());
            }
            catch (java.beans.PropertyVetoException pve) {
                pve.printStackTrace();
                fail("got a pve");
            }
            catch (ConfigurationException ce) {
                ce.printStackTrace();
                fail("got a ce");
            }
    }            
        
        
    public void testSaveRestore() {
        try {
            java.io.ByteArrayOutputStream baos =
                new java.io.ByteArrayOutputStream();
            //warDC.save(baos);
            //assertEquals("<sun-web-app></sun-web-app>",baos.toString());
            /*MockDDBeanRoot waddbean = new MockDDBeanRoot();
            /*waddbean.setXpath("/web-app");
            waddbean.setRoot(waddbean);*/
            WebAppRoot war = null;
            war = (WebAppRoot) warDC.getDConfigBeanRoot(waddbean); 
            assertNotNull(war);
            war.setContextRoot("testContextRootValue");
            baos = new java.io.ByteArrayOutputStream();
            warDC.save(baos);
            assertTrue(baos.toString().indexOf("testContextRootValue") > 0);
            war.setContextRoot("ContextRootValueTest");
            warDC.restore(new java.io.ByteArrayInputStream(baos.toByteArray()));
            assertEquals(war.getContextRoot(), "testContextRootValue");
            //assertFalse(baos.toString()
        }
        catch (java.beans.PropertyVetoException pve) {
            fail("setter got veto'ed");
        }
        catch (ConfigurationException ce) {
            ce.printStackTrace();
            fail("check on this");
        }
    }
    
    public void testCreate() {
        assertNotNull(warDC);
    }
    
    public SunONEDeploymentConfigurationTest(java.lang.String testName) {
        super(testName);
    }
    
    static SunDeploymentFactory DF = new SunDeploymentFactory();
    static DeploymentManager DM = null;
    static SunONEDeploymentConfiguration warDC = null;
    static DConfigBeanRoot WAR = null;
    static MockDDBeanRoot waddbean = null;
    static DeploymentConfiguration dc2 = null;
    static {
        try {
            DM = DF.getDisconnectedDeploymentManager("deployer:Sun:AppServer::localhost:4848");
            MockDeployableObject dObj = new MockDeployableObject();
            waddbean = new MockDDBeanRoot();
            waddbean.setXpath("/web-app");
            dObj.setDDBeanRoot(waddbean);
            waddbean.setRoot(waddbean);
            warDC =  (SunONEDeploymentConfiguration) DM.createConfiguration(dObj);
            dc2 = DM.createConfiguration(dObj);
//            WAR = DC.getDConfigBeanRoot(new MockDDBeanRoot());
        }
        catch (Throwable t) {
            fail(t.getMessage());
        }
    }


    
}
