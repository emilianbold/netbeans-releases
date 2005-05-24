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
    
    public static Test suite() {
        TestSuite suite = new TestSuite(SunDeploymentManagerTest.class);
        return suite;
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

}
