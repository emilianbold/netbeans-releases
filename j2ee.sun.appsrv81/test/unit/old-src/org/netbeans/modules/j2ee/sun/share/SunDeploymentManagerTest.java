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
 * SunDeploymentManagerTest.java
 * JUnit based test
 *
 * Created on December 24, 2003, 11:34 AM
 */

package org.netbeans.modules.j2ee.sun.share;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
//import java.util.Locale;
//import javax.enterprise.deploy.model.DeployableObject;
//import javax.enterprise.deploy.spi.Target;
//import javax.enterprise.deploy.spi.TargetModuleID;
//import javax.enterprise.deploy.spi.status.ProgressObject;
//import javax.enterprise.deploy.spi.status.ProgressListener;
//import javax.enterprise.deploy.spi.status.ProgressEvent;
//import javax.enterprise.deploy.spi.status.DeploymentStatus;
//import javax.enterprise.deploy.shared.DConfigBeanVersionType;
//import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.DeploymentManager;
//import javax.enterprise.deploy.spi.DeploymentConfiguration;
//import javax.enterprise.deploy.shared.ModuleType;
//import javax.enterprise.deploy.spi.exceptions.TargetException;
//import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;
//import java.util.jar.JarOutputStream;
//import java.io.FileOutputStream;
//import org.netbeans.modules.j2ee.sun.share.plan.Util;
import junit.framework.*;

/**
 *
 * @author vkraemer
 */
public class SunDeploymentManagerTest extends TestCase {
    
    //String validPlan1 = "<deployment-plan></deployment-plan>";
    
    public void testGetInternalPlanFileWithNotWellFormedString() {
        SunDeploymentManager dm = new SunDeploymentManager((DeploymentManager) null, (SunDeploymentFactory) null, "", 0);
        InputStream is1 = new java.io.StringBufferInputStream(TestConstants.invalidPlan1);
        try {
            File retVal = dm.getInternalPlanFile(is1);
            retVal.delete();
            fail("The file was was not well formed.  I should not get here");
        }
        catch (IllegalStateException ise) {
            assertEquals("file handling issues", ise.getMessage());
            //ise.printStackTrace();
        }
    }
    
    
    public void testGetInternalPlanFileInvalidContentString() {
        SunDeploymentManager dm = new SunDeploymentManager((DeploymentManager) null, (SunDeploymentFactory) null, "", 0);
        InputStream is1 = new java.io.StringBufferInputStream(TestConstants.invalidPlan2);
        try {
            File retVal = dm.getInternalPlanFile(is1);
            retVal.delete();
            fail("The file was was not a valid plan.  I should not get here");
        }
        catch (IllegalStateException ise) {
            assertEquals("file handling issues", ise.getMessage());
            //ise.printStackTrace();
        }
    }

    public void testGetInternalPlanFileWithDPString() {
        SunDeploymentManager dm = new SunDeploymentManager((DeploymentManager) null, (SunDeploymentFactory) null, "", 0);
        InputStream is1 = new java.io.StringBufferInputStream(TestConstants.validPlan2);
        File retVal = dm.getInternalPlanFile(is1);
        assertNotNull(retVal);
        retVal.delete();
    }
    
    public void testGetInternalPlanFileWithSWAString() {
        SunDeploymentManager dm = new SunDeploymentManager((DeploymentManager) null, (SunDeploymentFactory) null, "", 0);
        InputStream is1 = new java.io.StringBufferInputStream(TestConstants.validPlan3);
        File retVal = dm.getInternalPlanFile(is1);
        assertNotNull(retVal);
        retVal.delete(); 
    }
    

    public void testGetInternalPlanFileWithDPFile() {
        SunDeploymentManager dm = new SunDeploymentManager((DeploymentManager) null, (SunDeploymentFactory) null, "", 0);
        InputStream is1 = null;
        File myTestFile = new File("validDeploymentPlan.xml");
        System.out.println("path to file:"+myTestFile.getAbsolutePath());
        try {
            is1 = new FileInputStream(myTestFile);
            File retVal = dm.getInternalPlanFile(is1);
            assertNotNull(retVal);
        }
        catch (java.io.FileNotFoundException fnfe) {
            fail("file not found issue");
        }
    }            
    public void testGetInternalPlanFileWithSWAFile() {
        SunDeploymentManager dm = new SunDeploymentManager((DeploymentManager) null, (SunDeploymentFactory) null, "", 0);
        InputStream is1 = null;
        File myTestFile = new File("validSunWebApp.xml");
        System.out.println("path to file:"+myTestFile.getAbsolutePath());
        try {
            is1 = new FileInputStream(myTestFile);
            File retVal = dm.getInternalPlanFile(is1);
            assertNotNull(retVal);
        }
        catch (java.io.FileNotFoundException fnfe) {
            fail("file not found issue");
        }
    }
    
    public SunDeploymentManagerTest(java.lang.String testName) {
        super(testName);
    }
    
}
