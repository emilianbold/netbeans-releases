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

package org.netbeans.modules.compapp.projects.jbi.anttasks;

import com.sun.esb.management.api.deployment.DeploymentService;
import com.sun.esb.management.common.ManagementRemoteException;
import com.sun.jbi.ui.common.ServiceAssemblyInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;

import java.nio.channels.FileChannel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.netbeans.modules.compapp.jbiserver.JbiManager;
import org.netbeans.modules.compapp.projects.jbi.AdministrationServiceHelper;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.netbeans.modules.sun.manager.jbi.GenericConstants;
import org.netbeans.modules.sun.manager.jbi.management.JBIMBeanTaskResultHandler;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentStatus;
import org.netbeans.modules.sun.manager.jbi.management.wrapper.api.RuntimeManagementServiceWrapper;
import org.netbeans.modules.sun.manager.jbi.util.ServerInstance;

/**
 * Ant task to deploy/undeploy Service Assembly to/from the target JBI server.
 *
 */
public class DeployServiceAssembly extends Task {
    
    private static final String SERVER_TARGET = "server";
    
    /**
     * DOCUMENT ME!
     */
    private String serviceAssemblyID;
    
    /**
     * DOCUMENT ME!
     */
    private String serviceAssemblyLocation;
    
    private String undeployServiceAssembly = "false";
    
    private String hostName;
    
    private String port;
    
    private String userName;
    
    private String password;
    
    // REMOVE ME
    private String serverInstanceLocation;
    
    private String netBeansUserDir;
    
    // If not defined, then the first server instance in the setting file
    // will be used.
    // REMOVE ME
    private String j2eeServerInstance;
    
    private boolean FORCE = true; //??
    
    
    /**
     * DOCUMENT ME!
     *
     * @return Returns the serviceAssemblyID.
     */
    public String getServiceAssemblyID() {
        return this.serviceAssemblyID;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param serviceAssemblyID
     *            The ServiceAssembly ID to set.
     */
    public void setServiceAssemblyID(String serviceAssemblyID) {
        this.serviceAssemblyID = serviceAssemblyID;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return Returns the serviceAssemblyID.
     */
    public String getServiceAssemblyLocation() {
        return this.serviceAssemblyLocation;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param serviceAssemblyLocation
     *            The ServiceAssembly location to set.
     */
    public void setServiceAssemblyLocation(String serviceAssemblyLocation) {
        this.serviceAssemblyLocation = serviceAssemblyLocation;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return Returns the undeployServiceAssembly.
     */
    public String getUndeployServiceAssembly() {
        return this.undeployServiceAssembly;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param undeployServiceAssembly
     *            The undeployServiceAssembly command.
     */
    public void setUndeployServiceAssembly(String undeployServiceAssembly) {
        this.undeployServiceAssembly = undeployServiceAssembly;
    }
    
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
    
    public String getHostName() {
        return hostName;
    }
    
    public void setPort(String port) {
        this.port = port;
    }
    
    public String getPort() {
        return port;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setServerInstanceLocation(String serverInstanceLocation) {
        this.serverInstanceLocation = serverInstanceLocation;
    }
    
    public String getServerInstanceLocation() {
        return serverInstanceLocation;
    }
    
    public void setNetBeansUserDir(String netBeansUserDir) {
        this.netBeansUserDir = netBeansUserDir;
    }
    
    public String getNetBeansUserDir() {
        return netBeansUserDir;
    }
    
    public void setJ2eeServerInstance(String j2eeServerInstance) {
        this.j2eeServerInstance = j2eeServerInstance;
    }
    
    public String getJ2eeServerInstance() {
        return j2eeServerInstance;
    }
        
    /**
     * DOCUMENT ME!
     *
     * @throws BuildException
     *             DOCUMENT ME!
     */
    @Override
    public void execute() throws BuildException {
        
        // 4/11/08, copy SA over to autodeploy directory if OSGi is enabled
        Project p = this.getProject();
        String osgisupport = p.getProperty(JbiProjectProperties.OSGI_SUPPORT);

        // 02/04/09, IZ#153580, disable fuji deployment
        /*
        if ((osgisupport != null) && osgisupport.equalsIgnoreCase("true")) {
            
            String osgiDirPath = 
                p.getProperty(JbiProjectProperties.OSGI_CONTAINER_DIR);
            if (osgiDirPath == null || osgiDirPath.trim().length() == 0) {
                throw new BuildException("OSGi container directory is not specified.");
            }
            
            File osgiDir = new File(osgiDirPath);
            if (!osgiDir.exists() || osgiDir.isFile()) {
                throw new BuildException("Invalid OSGi container directory: " + osgiDirPath);                
            }
            
            File autoDeployDir = new File(osgiDirPath + "/jbi/autodeploy/");
            File srcFile = new File(serviceAssemblyLocation);
            File targetFile = new File(autoDeployDir, srcFile.getName());
            
            if (undeployServiceAssembly.equalsIgnoreCase("true")) { // NOI18N
                log("  remove " + targetFile.getAbsolutePath());
                targetFile.delete();
            } else {
                try {
                    log("  copy " + srcFile.getAbsolutePath() + " to " + 
                            autoDeployDir.getAbsolutePath());
                    copyFile(srcFile, targetFile);
                } catch (IOException ex) {
                    throw new BuildException(ex.getMessage());
                }
            }
            return;
        }
        */
            
        if (serviceAssemblyID != null && 
                serviceAssemblyID.equals("${org.netbeans.modules.compapp.projects.jbi.descriptor.uuid.assembly-unit}")) {
            String msg = "Unknown Service Assembly ID: " + serviceAssemblyID + 
                    System.getProperty("line.separator") +
                    "Please re-open your CompApp project using the latest NetBeans to refresh your CompApp project." +
                    System.getProperty("line.separator") +
                    "See http://www.netbeans.org/issues/show_bug.cgi?id=108702 for more info."; 
            throw new BuildException(msg);
        }
                
        String nbUserDir = getNetBeansUserDir();
        String serverInstanceID = getJ2eeServerInstance();

        try {
            // Make sure the app server is running.
            JbiManager.startServer(serverInstanceID, true);            
        } catch (Exception e) {
            // NPE from command line because of missing repository in the 
            // default lookup. The server needs to be started explicitly
            // in this case.
        }
        
        ServerInstance serverInstance = AdministrationServiceHelper.getServerInstance(
                nbUserDir, serverInstanceID);
               
        try {
            RuntimeManagementServiceWrapper mgmtServiceWrapper = 
                    AdministrationServiceHelper.
                    getRuntimeManagementServiceWrapper(serverInstance);
            DeploymentService deploymentService = 
                    AdministrationServiceHelper.
                    getDeploymentService(serverInstance);
        
            hostName = serverInstance.getHostName();
            port = serverInstance.getAdminPort();
            userName = serverInstance.getUserName();
            password = serverInstance.getPassword();

            mgmtServiceWrapper.clearServiceAssemblyStatusCache();
            ServiceAssemblyInfo assembly = mgmtServiceWrapper.getServiceAssembly(
                    serviceAssemblyID,SERVER_TARGET);  
            
            String status = assembly == null ? null : assembly.getState();

            if (JBIComponentStatus.UNKNOWN_STATE.equals(status)) {
                String msg = "Unknown status for Service Assembly "
                        + serviceAssemblyID;
                throw new BuildException(msg);
            }

            if (undeployServiceAssembly.equalsIgnoreCase("true")) { // NOI18N
                if (JBIComponentStatus.STARTED_STATE.equals(status)) {
                    stopServiceAssembly(mgmtServiceWrapper);
                    shutdownServiceAssembly(mgmtServiceWrapper);
                    undeployServiceAssembly(deploymentService);
                } else if (JBIComponentStatus.STOPPED_STATE.equals(status)) {
                    shutdownServiceAssembly(mgmtServiceWrapper);
                    undeployServiceAssembly(deploymentService);
                } else if (JBIComponentStatus.SHUTDOWN_STATE.equals(status)) {
                    undeployServiceAssembly(deploymentService);
                }
            } else { // deploy action...
                if (JBIComponentStatus.STARTED_STATE.equals(status)) {
                    stopServiceAssembly(mgmtServiceWrapper);
                    shutdownServiceAssembly(mgmtServiceWrapper);
                    undeployServiceAssembly(deploymentService);
                } else if (JBIComponentStatus.STOPPED_STATE.equals(status)) {
                    shutdownServiceAssembly(mgmtServiceWrapper);
                    undeployServiceAssembly(deploymentService);
                } else if (JBIComponentStatus.SHUTDOWN_STATE.equals(status)) {
                    undeployServiceAssembly(deploymentService);
                } 
                
                try {
                    deployServiceAssembly(deploymentService);
                } catch (BuildException e) {
                                        
                    Object[] processResult = JBIMBeanTaskResultHandler.getProcessResult(
                            GenericConstants.DEPLOY_SERVICE_ASSEMBLY_OPERATION_NAME,
                            serviceAssemblyID, e.getMessage(), false);                    
                    log("ERROR: " + processResult[0], Project.MSG_ERR);
                    
                    ServiceAssemblyInfo saInfo = mgmtServiceWrapper.getServiceAssembly(
                            serviceAssemblyID,SERVER_TARGET);
                    if (saInfo != null) {
                        log("Cleaning up...");
                        try {
                            undeployServiceAssembly(deploymentService);
                        } catch (BuildException ex) {
                            log("ERROR: " + ex.getMessage(), Project.MSG_ERR);
                        }
                    }
                    
                    throw new BuildException("Deployment failure.");                    
                } 
                
                try {
                    startServiceAssembly(mgmtServiceWrapper);
                } catch (BuildException e) {
                    
                    Object[] processResult = JBIMBeanTaskResultHandler.getProcessResult(
                            GenericConstants.START_SERVICE_ASSEMBLY_OPERATION_NAME,
                            serviceAssemblyID, e.getMessage(), false);
                    log("ERROR: " +  processResult[0], Project.MSG_ERR);
                    log("Cleaning up...");
                    
                    boolean rollbackFailure = false;
                    
                    try{
                        stopServiceAssembly(mgmtServiceWrapper);
                    } catch (BuildException ex) {
                        rollbackFailure = true;
                        log("ERROR: " + ex.getMessage(), Project.MSG_ERR);
                    }
                    
                    if (!rollbackFailure) {
                        try {
                            shutdownServiceAssembly(mgmtServiceWrapper);
                        } catch (BuildException ex) {
                            rollbackFailure = true;
                            log("ERROR: " + ex.getMessage(), Project.MSG_ERR);
                        }
                    }
                    
                    if (!rollbackFailure) {
                        try {
                            undeployServiceAssembly(deploymentService);
                        } catch (BuildException ex) {
                            rollbackFailure = true;
                            log("ERROR: " + ex.getMessage(), Project.MSG_ERR);
                        }
                    }
                    
                    throw new BuildException("Start failure.");    
                }
            }
        } catch (ManagementRemoteException e) {
            Object[] processResult = JBIMBeanTaskResultHandler.getProcessResult(
                    GenericConstants.DEPLOY_SERVICE_ASSEMBLY_OPERATION_NAME,
                    serviceAssemblyID, e.getMessage(), false);
            throw new BuildException((String) processResult[0]);
        }             
    }
        
    private void deployServiceAssembly(DeploymentService adminService) 
            throws BuildException {
        log("[deploy-service-assembly]");
        log("    Deploying a service assembly...");
        log("        host=" + hostName);
        log("        port=" + port);
        log("        file=" + serviceAssemblyLocation);
        
        String result = null;
        try {
            result = adminService.deployServiceAssembly(serviceAssemblyLocation, SERVER_TARGET);
        } catch (ManagementRemoteException e) {
            result = e.getMessage();
        } finally {
            Object[] value = JBIMBeanTaskResultHandler.getProcessResult(
                "Deploy", serviceAssemblyLocation, result, false);
            if (value[0] != null) {
                throw new BuildException((String)value[0]);
            }
        }
    }
    
    private void startServiceAssembly(
            RuntimeManagementServiceWrapper adminService) 
            throws BuildException {
        log("[start-service-assembly]");
        log("    Starting a service assembly...");
        log("        host=" + hostName);
        log("        port=" + port);
        log("        name=" + serviceAssemblyID);
          
        String result = null;
        try {
            result = adminService.startServiceAssembly(serviceAssemblyID, SERVER_TARGET);
        } catch (ManagementRemoteException e) {
             result = e.getMessage();
        } finally {
            Object[] value = JBIMBeanTaskResultHandler.getProcessResult(
                "Start", serviceAssemblyID, result, false);
            if (value[0] != null) {
                throw new BuildException((String)value[0]);
            }
        }
    }
    
    private void stopServiceAssembly(RuntimeManagementServiceWrapper adminService) 
            throws BuildException {
        log("[stop-service-assembly]");
        log("    Stopping a service assembly...");
        log("        host=" + hostName);
        log("        port=" + port);
        log("        name=" + serviceAssemblyID);
        
        String result = null;
        try {
            result = adminService.stopServiceAssembly(serviceAssemblyID, SERVER_TARGET);
        } catch (ManagementRemoteException e) {
             result = e.getMessage();
        } finally {
            Object[] value = JBIMBeanTaskResultHandler.getProcessResult(
                "Stop", serviceAssemblyID, result, false);
            if (value[0] != null) {
                throw new BuildException((String)value[0]);
            }
        }
    }
    
    private void shutdownServiceAssembly(RuntimeManagementServiceWrapper adminService) 
            throws BuildException {
        log("[shutdown-service-assembly]");
        log("    Shutting down a service assembly...");
        log("        host=" + hostName);
        log("        port=" + port);
        log("        name=" + serviceAssemblyID);
        
        String result = null;
        try {
            result = adminService.shutdownServiceAssembly(serviceAssemblyID, FORCE, SERVER_TARGET);
        } catch (ManagementRemoteException e) {
             result = e.getMessage();
        } finally {
            Object[] value = JBIMBeanTaskResultHandler.getProcessResult(
                "Shutdown", serviceAssemblyID, result, false);
            if (value[0] != null) {
                throw new BuildException((String)value[0]);
            }
        }
    }
    
    private void undeployServiceAssembly(DeploymentService adminService) 
            throws BuildException {
        log("[undeploy-service-assembly]");
        log("    Undeploying a service assembly...");
        log("        host=" + hostName);
        log("        port=" + port);
        log("        name=" + serviceAssemblyID);
        
        String result = null;
        try {
            result = adminService.undeployServiceAssembly(serviceAssemblyID, FORCE, SERVER_TARGET);
        } catch (ManagementRemoteException e) {
             result = e.getMessage();
        } finally {
            Object[] value = JBIMBeanTaskResultHandler.getProcessResult(
                "Undeploy", serviceAssemblyID, result, false);
            if (value[0] != null) {
                throw new BuildException((String)value[0]);
            }
        }
    }
    
    static Document loadXML(String xmlSource) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            // documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory
                    .newDocumentBuilder();
            return documentBuilder.parse(new InputSource(new StringReader(
                    xmlSource)));
        } catch (Exception e) {
            System.out.println("Error parsing XML: " + e);
            return null;
        }
    }
        
    private static void copyFile(File src, File target)
            throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(target).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            throw e;
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param args
     *            DOCUMENT ME!
     */
    public static void main(String[] args) {
        System.out.println("here");
        DeployServiceAssembly deploy = new DeployServiceAssembly();
        deploy.setServiceAssemblyID("01000000-C40493EE0B0100-8199A774-01"); 
        deploy.setServiceAssemblyLocation("C:\\Documents and Settings\\jqian\\CompositeApp10\\dist\\CompositeApp10.zip"); 
        deploy.setUserName("admin");
        deploy.setPassword("adminadmin");
        deploy.setHostName("localhost");
        deploy.setPort("4848");
        deploy.setServerInstanceLocation("C:\\Alaska\\Sun\\AppServer");
        deploy.execute();
    }
    
}
