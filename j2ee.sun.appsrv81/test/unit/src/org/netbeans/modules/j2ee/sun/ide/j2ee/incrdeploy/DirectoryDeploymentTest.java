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
 * DirectoryDeploymentTest.java
 * JUnit based test
 *
 * Created on May 27, 2004, 2:26 PM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee.incrdeploy;

import junit.framework.*;

import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import org.netbeans.modules.j2ee.sun.bridge.DirectoryDeployment;

/*import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.io.File;
import junit.framework.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.api.DeploymentPlanSplitter;
import org.netbeans.modules.j2ee.deployment.plugins.api.AppChangeDescriptor;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import org.netbeans.modules.j2ee.sun.ide.j2ee.Constants;
import org.netbeans.modules.j2ee.sun.ide.j2ee.Status;
import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;
import com.sun.enterprise.deployapi.SunTargetModuleID;
import org.netbeans.modules.j2ee.sun.common.dd.webapp.SunWebApp;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.netbeans.modules.j2ee.sun.ide.j2ee.ProgressEventSupport;
 **/

/**
 *
 * @author vkraemer
 */
public class DirectoryDeploymentTest extends TestCase {
    
    public void testCoverage() {
        DirectoryDeployment foo = new DirectoryDeployment();
        try {
            foo.setDeploymentManager(null);
            fail("null is an IllegalArgument");
        } catch (IllegalArgumentException iae) {
        }
        try {
            foo = new DirectoryDeployment(null);
            fail("null is an IllegalArgument");
        } catch (IllegalArgumentException iae) {
        }
        boolean ret = foo.canFileDeploy(null,null);
        //assertFalse("the null target cannot file deploy a null module", ret);
        assertTrue("the null target cannot file deploy a null module", !ret);
        foo.getDeploymentPlanFileNames(ModuleType.EAR);
        foo.getDeploymentPlanFileNames(ModuleType.WAR);
        foo.getDeploymentPlanFileNames(ModuleType.EJB);
        foo.getDeploymentPlanFileNames(ModuleType.CAR);
        foo.getDeploymentPlanFileNames(ModuleType.RAR);
        /*java.io.File f = foo.getDirectoryForModule(null);
        assertNull("a null TMID deploys to the null directory",f);
        f = foo.getDirectoryForNewApplication(null, null,null);
        assertNull(f);
        f = foo.getDirectoryForNewModule(null, null, null, null);
        assertNull(f);
        String s = foo.getModuleUrl(null);
        assertNull(s);
         **/
        try {
            foo.readDeploymentPlanFiles(null, null, null);
            fail("I shouldn't get here: readDeploymentPlanFiles(null,null,null)");
        } catch (ConfigurationException ce) {
        }
        try {
            foo.writeDeploymentPlanFiles(null, null, null);
            fail("I shouldn't get here: readDeploymentPlanFiles(null,null,null)");
        } catch (ConfigurationException ce) {
            
        }
    }
    
    public DirectoryDeploymentTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(DirectoryDeploymentTest.class);
        return suite;
    }
    
}
