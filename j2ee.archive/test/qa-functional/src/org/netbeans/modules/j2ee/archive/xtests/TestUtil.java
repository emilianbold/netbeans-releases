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
package org.netbeans.modules.j2ee.archive.xtests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.archive.ui.JavaEePlatformUiSupport;
import org.netbeans.modules.j2ee.archive.wizard.*;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;

/**
 *
 * @author Michal Mocnak
 * @author vince kraemer
 */
public class TestUtil {
    
    // TEST PROPERTIES
    public static final String EJB_PROJECT_NAME = "SjsasTestEjb";
    public static final String WEB_PROJECT_NAME = "SjsasTestWeb";
    public static final String EJB_PROJECT_PATH = System.getProperty("xtest.tmpdir") + File.separator + EJB_PROJECT_NAME;
    public static final String WEB_PROJECT_PATH = System.getProperty("xtest.tmpdir") + File.separator + WEB_PROJECT_NAME;
    public static final int SLEEP = 10000;
    
    // SERVER PROPERTIES FOR TESTS
    public static final String _SEP = System.getProperty("file.separator");
    public static final String _DISPLAY_NAME = "Sun Java System Application Server";
    public static final String _PLATFORM_LOCATION = System.getProperty("sjsas.server.path");
    public static final String _INSTALL_LOCATION = _PLATFORM_LOCATION+_SEP+"domains";
    public static final String _DOMAIN = "domain1";
    public static final String _HOST = "localhost";
    public static final String _PORT = getPort(new File(_INSTALL_LOCATION+_SEP+_DOMAIN+_SEP+"config"+_SEP+"domain.xml"));
    public static final String _USER_NAME = "admin";
    public static final String _PASSWORD = "adminadmin";
    public static final String _URL = "["+_PLATFORM_LOCATION+"]deployer:Sun:AppServer::"+_HOST+":"+_PORT;
    
    // SERVER PROPERTIES FOR APP SERVER REGISTRATION
    public static final String USER_NAME = "username";
    public static final String PASSWORD = "password";
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String PLATFORM_LOCATION = "platform_location";
    public static final String INSTALL_LOCATION = "install_location";
    public static final String DOMAIN = "domain";
    public static final String TYPE = "type";
    public static final String PROP_DISPLAY_NAME = "ServInstWizard_displayName";
    
    /**
     * It returns admin port number if the server.
     */
    public static String getPort(File domainXml){
        String adminPort = null;
        String buffer = null;
        
        try {
            FileReader reader = new FileReader(domainXml);
            BufferedReader br = new BufferedReader(reader);
            
            while((buffer = br.readLine()) != null) {
                if(buffer.indexOf("admin-listener") > -1) {
                    int x = buffer.indexOf(34, buffer.indexOf("port"));
                    int y = buffer.indexOf(34, ++x);
                    adminPort = buffer.substring(x, y);
                    break;
                }
            }
            
            br.close();
            reader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return adminPort;
    }
    
    public static void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch(Exception e) {
            // Nothing to do
        }
    }
    
//    public static TargetModuleID deployModule(final ModuleType moduleType, final String modulePath, final String moduleName) throws Exception {
//        Project project = (Project)openProject(new File(modulePath));
//        ActionProvider ap = (ActionProvider)project.getLookup().lookup(ActionProvider.class);
//        J2eeModuleProvider jmp = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
//        final ServerInstance si = ServerRegistry.getInstance().getServerInstance(_URL);
//        
//        Runnable startCondition = new Runnable() {
//            public void run() {
//                while(!si.isConnected()) {
//                    try {
//                        Thread.sleep(5000);
//                    } catch(Exception e) {}
//                }
//            }
//        };
//        
//        Runnable deployCondition = new Runnable() {
//            public void run() {
//                while(getModuleID(moduleType, moduleName, si) == null) {
//                    try {
//                        Thread.sleep(5000);
//                    } catch(Exception e) {}
//                }
//            }
//        };
//        
//        Task t = RequestProcessor.getDefault().create(startCondition);
//        ap.invokeAction(EjbProjectConstants.COMMAND_REDEPLOY, project.getLookup());
//        t.run();
//        if(!t.waitFinished(300000))
//            throw new Exception("Server start timeout");
//        
//        sleep(SLEEP);
//        
//        t = RequestProcessor.getDefault().create(deployCondition);
//        t.run();
//        if(!t.waitFinished(300000))
//            throw new Exception("WEB Application deploy timeout");
//        
//        sleep(SLEEP);
//        
//        closeProject(moduleName);
//        
//        sleep(SLEEP);
//        
//        return null;
//    }
    
//    public static void undeployModule(final ModuleType moduleType, final String modulePath, final String moduleName, final TargetModuleID moduleID) throws Exception {
//        ServerInstance si = ServerRegistry.getInstance().getServerInstance(_URL);
//        si.getDeploymentManager().undeploy(new TargetModuleID[] {moduleID});
//        
//        sleep(SLEEP);
//        
//        if(getModuleID(moduleType, moduleName, si) != null)
//            throw new Exception("Undeploy failed");
//    }
    
//    public static TargetModuleID getModuleID(ModuleType moduleType, String moduleName, ServerInstance si) {
//        try {
//            Target target = si.getTargets()[0].getTarget();
//            TargetModuleID[] modules = si.getDeploymentManager().getRunningModules(moduleType, new Target[] {target});
//            
//            for(int i=0;i<modules.length;i++) {
//                if(modules[i].getModuleID().equals(moduleName))
//                    return modules[i];
//            }
//            
//            return null;
//        } catch(Exception e) {
//            return null;
//        }
//        
//    }
    
//    public static Object openProject(File projectDir) {
//        return ProjectSupport.openProject(projectDir);
//    }
//    
//    public static void closeProject(String projectName) {
//        ProjectSupport.closeProject(projectName);
//    }
    
    static void createProjectFromArchive(String archiveName, String type, int sleepTime) {
        WizardDescriptor wizDesc = new WizardDescriptor(new WizardDescriptor.Panel[0]);
        File oldVal = ProjectChooser.getProjectsFolder();
        File projDest = new File(System.getProperty("xtest.tmpdir") + File.separator + archiveName);
        wizDesc.putProperty(DeployableWizardIterator.PROJECT_DIR_PROP, projDest);
        File archive = new File(System.getProperty("xtest.data")+File.separator + archiveName);
        wizDesc.putProperty(DeployableWizardIterator.PROJECT_ARCHIVE_PROP, archive);
                //FileUtil.toFile(archive));
        String subprojkey = archiveName;
        wizDesc.putProperty(DeployableWizardIterator.PROJECT_NAME_PROP,
                subprojkey);
        
        wizDesc.putProperty(DeployableWizardIterator.PROJECT_TARGET_PROP,
                JavaEePlatformUiSupport.getServerInstanceID(TestUtil._URL));
        wizDesc.putProperty(DeployableWizardIterator.PROJECT_TYPE_PROP,
                type);
        DeployableWizardIterator dwi = new DeployableWizardIterator();
        dwi.initialize(wizDesc);
        try {
            dwi.instantiate();
        } catch (IOException ioe) {
            NbTestCase.fail();// fail();
        }
        if (null != oldVal) {
            ProjectChooser.setProjectsFolder(oldVal);
        }
        try {
            Thread.currentThread().sleep(sleepTime);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

}
