/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
 * SunConfigurationMethods.java
 *
 * Created on April 19, 2006, 4:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.sun.test;

/**
 *
 * @author ak199487
 */

import java.io.File;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider.ConfigSupport;
//import org.netbeans.modules.j2ee.deployment.execution.DeploymentConfigurationProvider;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.glassfish.eecommon.api.config.GlassfishConfiguration;
import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 *
 * @author Peter Williams
 */
public class SunConfigurationMethods extends NbTestCase {
    
    public SunConfigurationMethods(String testName) {
        super(testName);
    }
    
    public void loadSunWebConfigVersion() {
        loadConfiguration(Util.WEB_PROJECT_NAME, Util.WEB_PROJECT_PATH,
                "WEB-INF/sun-web.xml");
    }
    
    public void loadSunEjbJarConfigVersion() {
        loadConfiguration(Util.EJB_PROJECT_NAME, Util.EJB_PROJECT_PATH,
                "sun-ejb-jar.xml");
    }
    
    public void loadSunApplicationConfigVersion() {
        loadConfiguration(Util.STATEFUL_PROJECT_NAME, Util.STATEFUL_PROJECT_PATH,
                "sun-application.xml");
    }
    public void loadSunAppClientConfigVersion() {
        loadConfiguration(Util.CUSTOMER_CLIENT_PROJECT_NAME, Util.CUSTOMER_PROJECT_PATH+Util._SEP+Util.CUSTOMER_CLIENT_PROJECT_NAME,
                "sun-application-client.xml");
    }
    
    private void loadConfiguration(String testProjectName, String testProjectPath, String testConfigPath) {
        try {
            Project project = (Project)Util.openProject(new File(testProjectPath));
            
            // Test: Verify existence of primary configuration file.  (Note this test should not be applied to
            // trivial JavaEE 5.0 modules which don't have server specific DD files.)
            J2eeModuleProvider provider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
            J2eeModule mod = provider.getJ2eeModule();
            File primaryConfigFile = mod.getDeploymentConfigurationFile(testConfigPath);
            if(!primaryConfigFile.exists()) {
                fail("Primary Sun configuration file (" + primaryConfigFile.getName() + ") for project " +
                        testProjectName + " does not exist.  (Path = " + primaryConfigFile.getPath() + ")");
            }
            
            // Test: Get deployment configuration object instance of type SunONEDeploymentConfiguration
            ConfigSupport support = provider.getConfigSupport();
            support.ensureConfigurationReady();
            Util.sleep(5000);
            //DeploymentConfigurationProvider dcProvider = (DeploymentConfigurationProvider) support; // Implementation dependency on ConfigSupportImpl in j2eeserver
            //DeploymentConfiguration dcFromDCP = dcProvider.getDeploymentConfiguration();
            GlassfishConfiguration dcFromCache = GlassfishConfiguration.getConfiguration(primaryConfigFile);
//            if(dcFromDCP == null) {
//                fail("DeploymentConfiguration instance from DeploymentConfigurationProvider is null.");
//            } else if(dcFromDCP == null) {
//                fail("DeploymentConfiguration instance from SunONEDeploymentConfiguration cache is null.");
//            } else if(dcFromDCP != dcFromCache) {
//                fail("DeploymentConfiguration instance for project does not match cached instance in SunONEDeploymentConfiguration");
//            }
            Util.closeProject(testProjectName);
            Util.sleep(5000);
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    
    public void changeSunWebConfigVersion() {
        changeConfiguration(Util.WEB_PROJECT_NAME, Util.WEB_PROJECT_PATH,
                "WEB-INF/sun-web.xml");
    }
    
    public void changeSunEjbJarConfigVersion() {
        changeConfiguration(Util.EJB_PROJECT_NAME, Util.EJB_PROJECT_PATH,
                "sun-ejb-jar.xml");
    }
    
    public void changeSunApplicationConfigVersion() {
        loadConfiguration(Util.STATEFUL_PROJECT_NAME, Util.STATEFUL_PROJECT_PATH, "sun-application.xml");
    }
    public void changeSunAppClientConfigVersion() {
        changeConfiguration(Util.CUSTOMER_CLIENT_PROJECT_NAME, Util.CUSTOMER_PROJECT_PATH+Util._SEP+Util.CUSTOMER_CLIENT_PROJECT_NAME,"sun-application-client.xml");
    }
    
    /** Generic method that performs version changes on the specified configuration file
     *  from the specified test project.
     */
    private void changeConfiguration(String testProjectName, String testProjectPath, String testConfigPath) {
        try {
            Project project = (Project)Util.openProject(new File(testProjectPath));
            J2eeModuleProvider provider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
            J2eeModule mod = provider.getJ2eeModule();
            File primaryConfigFile = mod.getDeploymentConfigurationFile(testConfigPath);
            // Test: Get deployment configuration object instance of type SunONEDeploymentConfiguration
            ConfigSupport support = provider.getConfigSupport();
            support.ensureConfigurationReady();
            Util.sleep(5000);
            GlassfishConfiguration dcFromCache = GlassfishConfiguration.getConfiguration(primaryConfigFile);
            // Test: change version to 8.1
            GlassfishConfiguration sunDC = dcFromCache;
            ASDDVersion asVersion = sunDC.getAppServerVersion();
            System.out.println("Current " + primaryConfigFile.getName() + " version is " + asVersion.toString());
            ASDDVersion oldVersion = asVersion;
            ASDDVersion newVersion = ASDDVersion.SUN_APPSERVER_8_1;
            if(oldVersion == ASDDVersion.SUN_APPSERVER_8_1) {
                newVersion = ASDDVersion.SUN_APPSERVER_9_0;
            }
            File configBackup= primaryConfigFile.createTempFile("backup",".xml",primaryConfigFile.getParentFile());
            System.out.println("backup file created is "+configBackup.getName()+"path is "+configBackup.toURL().toString());
            FileInputStream fin = new FileInputStream(primaryConfigFile);
            FileOutputStream fout= new FileOutputStream(configBackup);
            int b=fin.read();
            while (b!=-1) {
                fout.write(b);
                b=fin.read();
            }
            fin.close();
            fout.close();
            System.out.println("backup file created is "+configBackup.getName());
            System.out.println("Changing version to " + newVersion.toString());
            sunDC.setAppServerVersion(newVersion);
            Util.sleep(3000);
            ASDDVersion currentVersion = sunDC.getAppServerVersion();
            if(!newVersion.equals(currentVersion)) {
                fail("Failed to change configuration version.  Current version is still " + currentVersion.toString());
            }
            
            // Test: change version to 9.0
            System.out.println("Changing version back to " + oldVersion.toString());
            sunDC.setAppServerVersion(oldVersion);
            Util.sleep(3000);
            currentVersion = sunDC.getAppServerVersion();
            if(!oldVersion.equals(currentVersion)) {
                fail("Failed to change configuration version back to original.  Current version is still " + currentVersion.toString());
            }
            
            // Test: diff sun config file against original copy to make sure everything saved properly
            // -- need to be careful that fields that were dropped due to insufficient version support
            // remain dropped so either they can't be represented in the first place, or the backup copy
            // needs to be trimmed accordingly.
            
            //System.out.println("backup configpath is "+backupConfigPath);
            System.out.println("Comparing " + primaryConfigFile.getName() + " after version changes with backup of original.");
            if(!Util.compareFile(primaryConfigFile, configBackup)) {
                System.out.println(primaryConfigFile.getName() + " content altered by version change.\nReplacing the file with backup file");
                FileInputStream fIn = new FileInputStream(configBackup);
                FileOutputStream fOut= new FileOutputStream(primaryConfigFile);
                b=fIn.read();
                while (b!=-1) {
                    fOut.write(b);
                    b=fIn.read();
                }
                fIn.close();
                fOut.close();
                
            } else {
                System.out.println(primaryConfigFile.getName() + " matches " + configBackup.getName());
            }
            configBackup.delete();
            Util.closeProject(testProjectName);
            Util.sleep(5000);
        } catch(Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    
//    public static NbTestSuite suite() {
//        NbTestSuite suite = new NbTestSuite("SunConfigurationMethods");
////        suite.addTest(new AddRemoveSjsasInstanceTest("addSjsasInstance"));
////        suite.addTest(new StartStopServerTest("startServer"));
////        suite.addTest(new SunConfigurationMethods("loadSunWebConfigVersion"));
////        suite.addTest(new SunConfigurationMethods("loadSunEjbJarConfigVersion"));
////        suite.addTest(new SunConfigurationMethods("loadSunApplicationConfigVersion"));
////        suite.addTest(new SunConfigurationMethods("loadSunAppClientConfigVersion"));
////        suite.addTest(new SunConfigurationMethods("changeSunWebConfigVersion"));
////        suite.addTest(new SunConfigurationMethods("changeSunEjbJarConfigVersion"));
////        suite.addTest(new SunConfigurationMethods("changeSunApplicationConfigVersion"));
////        suite.addTest(new SunConfigurationMethods("changeSunAppClientConfigVersion"));
////        suite.addTest(new StartStopServerTest("stopServer"));
////        suite.addTest(new AddRemoveSjsasInstanceTest("removeSjsasInstance"));
//        return suite;
//    }
}



