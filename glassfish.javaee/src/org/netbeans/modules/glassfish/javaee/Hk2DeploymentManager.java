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

package org.netbeans.modules.glassfish.javaee;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.glassfish.eecommon.api.HttpMonitorHelper;
import org.netbeans.modules.glassfish.javaee.ide.MonitorProgressObject;
import org.netbeans.modules.glassfish.javaee.ide.DummyProgressObject;
import org.netbeans.modules.glassfish.javaee.ide.Hk2PluginProperties;
import org.netbeans.modules.glassfish.javaee.ide.Hk2Target;
import org.netbeans.modules.glassfish.javaee.ide.Hk2TargetModuleID;
import org.netbeans.modules.glassfish.javaee.ide.UpdateContextRoot;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.glassfish.spi.AppDesc;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.openide.util.NbBundle;
import org.xml.sax.SAXException;


/**
 *
 * @author Ludovic Champenois
 * @author Peter Williams
 */
public class Hk2DeploymentManager implements DeploymentManager {

    private volatile ServerInstance serverInstance;
    private volatile InstanceProperties instanceProperties;
    private Hk2PluginProperties pluginProperties;
    private String uri;
    private ServerUtilities su;
    
    /**
     * 
     * @param uri 
     * @param uname 
     * @param passwd 
     */
    public Hk2DeploymentManager(String uri, String uname, String passwd, ServerUtilities su) {
        this.uri = uri;
        this.su = su;
        pluginProperties = new Hk2PluginProperties(this,su);
    }
        
    
    /**
     * 
     * @param deployableObject 
     * @return 
     * @throws javax.enterprise.deploy.spi.exceptions.InvalidModuleException 
     */
    public DeploymentConfiguration createConfiguration(DeployableObject deployableObject) 
            throws InvalidModuleException {
        return new Hk2Configuration(deployableObject);
    }

    /**
     * 
     * @param targetList
     * @param moduleArchive
     * @param deploymentPlan
     * @return
     * @throws java.lang.IllegalStateException
     */
    public ProgressObject distribute(Target [] targetList, File moduleArchive, File deploymentPlan) 
            throws IllegalStateException {
        // compute the ModuleID
        String t =  moduleArchive.getName();
        final String moduleName = t.substring(0, t.length() - 4);
        Hk2TargetModuleID moduleId = Hk2TargetModuleID.get((Hk2Target) targetList[0], moduleName,
                null, moduleArchive.getAbsolutePath());
        MonitorProgressObject deployProgress = new MonitorProgressObject(this, moduleId, false);
        MonitorProgressObject returnProgress = new MonitorProgressObject(this, moduleId, false);
        GlassfishModule commonSupport = this.getCommonServerSupport();
        // FIXME -- broken for remote deploy of web apps
        deployProgress.addProgressListener(new UpdateContextRoot(returnProgress,moduleId, getServerInstance(), false));

        try {
            boolean restart = HttpMonitorHelper.synchronizeMonitor(
                    commonSupport.getInstanceProperties().get(GlassfishModule.DOMAINS_FOLDER_ATTR),
                    commonSupport.getInstanceProperties().get(GlassfishModule.DOMAIN_NAME_ATTR),
                    Boolean.parseBoolean(commonSupport.getInstanceProperties().get(GlassfishModule.HTTP_MONITOR_FLAG)),
                    "modules/org-netbeans-modules-schema2beans.jar");
            if (restart) {
                commonSupport.restartServer(deployProgress);
            }
        } catch (IOException ex) {
            Logger.getLogger("glassfish-javaee").log(Level.WARNING, "http monitor state", ex);
        } catch (SAXException ex) {
            Logger.getLogger("glassfish-javaee").log(Level.WARNING, "http monitor state", ex);
        }
        commonSupport.deploy(deployProgress, moduleArchive, moduleName);

        return returnProgress;
    }

    /**
     * 
     * @param targetList
     * @param moduleArchive
     * @param deploymentPlan
     * @return
     * @throws java.lang.IllegalStateException
     */
    public ProgressObject distribute(Target [] targetList, InputStream moduleArchive, InputStream deploymentPlan) 
            throws IllegalStateException {
        throw new UnsupportedOperationException(
                "Hk2DeploymentManager.distribute(target [], stream, stream) not supported yet.");
    }

    /**
     * 
     * @param targetList
     * @param type
     * @param moduleArchive
     * @param deploymentPlan
     * @return
     * @throws java.lang.IllegalStateException
     */
    public ProgressObject distribute(Target [] targetList, ModuleType type, InputStream moduleArchive, InputStream deploymentPlan) 
            throws IllegalStateException {
        throw new UnsupportedOperationException(
                "Hk2DeploymentManager.distribute(target [], module_type, stream, stream) not supported yet.");
    }

    /**
     * 
     * @param moduleIDList
     * @param moduleArchive
     * @param deploymentPlan
     * @return
     * @throws java.lang.UnsupportedOperationException
     * @throws java.lang.IllegalStateException
     */
    public ProgressObject redeploy(TargetModuleID [] moduleIDList, File moduleArchive, File deploymentPlan) 
            throws UnsupportedOperationException, IllegalStateException {
        final Hk2TargetModuleID moduleId = (Hk2TargetModuleID) moduleIDList[0];
        final String moduleName = moduleId.getModuleID();
        MonitorProgressObject deployProgress = new MonitorProgressObject(this, moduleId, false);
        MonitorProgressObject returnProgress = new MonitorProgressObject(this, moduleId, false);
        GlassfishModule commonSupport = this.getCommonServerSupport();
        // FIXME -- broken for remote deploy of web apps
        deployProgress.addProgressListener(new UpdateContextRoot(returnProgress,moduleId,getServerInstance(), false));
        try {
            boolean restart = HttpMonitorHelper.synchronizeMonitor(
                    commonSupport.getInstanceProperties().get(GlassfishModule.DOMAINS_FOLDER_ATTR),
                    commonSupport.getInstanceProperties().get(GlassfishModule.DOMAIN_NAME_ATTR),
                    Boolean.parseBoolean(commonSupport.getInstanceProperties().get(GlassfishModule.HTTP_MONITOR_FLAG)),
                    "modules/org-netbeans-modules-schema2beans.jar");
            if (restart) {
                commonSupport.restartServer(deployProgress);
            }
        } catch (IOException ex) {
            Logger.getLogger("glassfish-javaee").log(Level.WARNING, "http monitor state", ex);
        } catch (SAXException ex) {
            Logger.getLogger("glassfish-javaee").log(Level.WARNING, "http monitor state", ex);
        }
        commonSupport.deploy(deployProgress, moduleArchive, moduleName);

        return returnProgress;
    }

    /**
     * 
     * @param moduleIDList
     * @param moduleArchive
     * @param deploymentPlan
     * @return
     * @throws java.lang.UnsupportedOperationException
     * @throws java.lang.IllegalStateException
     */
    public ProgressObject redeploy(TargetModuleID [] moduleIDList, InputStream moduleArchive, InputStream deploymentPlan) 
            throws UnsupportedOperationException, IllegalStateException {
        throw new UnsupportedOperationException(
                "Hk2DeploymentManager.redeploy(target_module [], stream, stream) not supported yet.");
    }

    /**
     * 
     * @param targetModuleID 
     * @return 
     * @throws java.lang.IllegalStateException 
     */
    public ProgressObject undeploy(TargetModuleID [] targetModuleIDs) 
            throws IllegalStateException {
        // !PW FIXME handle arrays with length > 1 (EARs?)
        if(targetModuleIDs != null && targetModuleIDs.length > 0) {
            GlassfishModule commonSupport = getCommonServerSupport();
            MonitorProgressObject progressObject = new MonitorProgressObject(
                    this, (Hk2TargetModuleID) targetModuleIDs[0], CommandType.UNDEPLOY, false);
            commonSupport.undeploy(progressObject, targetModuleIDs[0].getModuleID());
            return progressObject;
        } else {
            throw new IllegalArgumentException("No TargetModuleID's specified.");
        }
    }

    /**
     * 
     * @param targetModuleID 
     * @return 
     * @throws java.lang.IllegalStateException 
     */
    public ProgressObject stop(TargetModuleID[] moduleIDList) throws IllegalStateException {
        return new DummyProgressObject(moduleIDList[0]);
    }

    /**
     * 
     * @param targetModuleID 
     * @return 
     * @throws java.lang.IllegalStateException 
     */
    public ProgressObject start(TargetModuleID [] moduleIDList) throws IllegalStateException {
        return new DummyProgressObject(moduleIDList[0]);
    }

    /**
     * 
     * @param locale 
     * @throws java.lang.UnsupportedOperationException 
     */
    public void setLocale(java.util.Locale locale) throws UnsupportedOperationException {
    }

    /**
     * 
     * @param locale 
     * @return 
     */
    public boolean isLocaleSupported(java.util.Locale locale) {
        return false;
    }

    /**
     * 
     * @param moduleType 
     * @param target 
     * @return 
     * @throws javax.enterprise.deploy.spi.exceptions.TargetException 
     * @throws java.lang.IllegalStateException 
     */
    public TargetModuleID [] getAvailableModules(ModuleType moduleType, Target [] targetList) 
            throws TargetException, IllegalStateException {
        return getDeployedModules(moduleType, targetList);
    }
        

    /**
     * 
     * @param moduleType 
     * @param target 
     * @return 
     * @throws javax.enterprise.deploy.spi.exceptions.TargetException 
     * @throws java.lang.IllegalStateException 
     */
    public TargetModuleID [] getNonRunningModules(ModuleType moduleType, Target [] targetList) 
            throws TargetException, IllegalStateException {
        Logger.getLogger("glassfish-javaee").log(Level.WARNING,
                "Hk2DeploymentManager.getNonRunningModules() not supported yet.");
        return new TargetModuleID[0];
    }

    /**
     * 
     * @param moduleType 
     * @param target 
     * @return 
     * @throws javax.enterprise.deploy.spi.exceptions.TargetException 
     * @throws java.lang.IllegalStateException 
     */
    public TargetModuleID [] getRunningModules(ModuleType moduleType, Target [] targetList) 
            throws TargetException, IllegalStateException {
        return getDeployedModules(moduleType, targetList);
    }
    
    private TargetModuleID [] getDeployedModules(ModuleType moduleType, Target [] targetList) 
            throws TargetException, IllegalStateException {
        List<TargetModuleID> moduleList = new ArrayList<TargetModuleID>();
        GlassfishModule commonSupport = getCommonServerSupport();
        if(commonSupport != null) {
            AppDesc [] appList = commonSupport.getModuleList(GlassfishModule.WEB_CONTAINER);
            if(appList != null && appList.length > 0) {
                if(targetList[0] instanceof Hk2Target) {
                    Hk2Target target = (Hk2Target) targetList[0];
                    for(AppDesc app: appList) {
                        moduleList.add(Hk2TargetModuleID.get(target, app.getName(),
                                "".equals(app.getContextRoot()) ? null : app.getContextRoot(),
                                app.getPath()));
                    }
                } else {
                    String targetDesc = targetList[0] != null ? targetList[0].toString() : "(null)";
                    throw new TargetException(NbBundle.getMessage(
                            Hk2DeploymentManager.class, "ERR_WrongTarget", targetDesc));
                }
            }
        }
        return moduleList.size() > 0 ? moduleList.toArray(new TargetModuleID[moduleList.size()]) :
            new TargetModuleID[0];
    }

    /**
     * 
     * @param dConfigBeanVersionType 
     * @throws javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException 
     */
    public void setDConfigBeanVersion(DConfigBeanVersionType version) throws DConfigBeanVersionUnsupportedException {
    }

    /**
     * 
     * @param dConfigBeanVersionType 
     * @return 
     */
    public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType version) {
        return false;
    }

    /**
     * 
     */
    public void release() {
    }

    /**
     * 
     * @return 
     */
    public boolean isRedeploySupported() {
        return isLocal();
    }

    /**
     * 
     * @return 
     */
    public java.util.Locale getCurrentLocale() {
        return null;
    }

    /**
     * 
     * @return 
     */
    public DConfigBeanVersionType getDConfigBeanVersion() {
        return null;
    }

    /**
     * 
     * @return 
     */
    public java.util.Locale getDefaultLocale() {
        return null;
    }

    /**
     * 
     * @return 
     */
    public java.util.Locale[] getSupportedLocales() {
        return new java.util.Locale[] { java.util.Locale.getDefault() };
    }

    /**
     * 
     * @return 
     * @throws java.lang.IllegalStateException 
     */
    public Target[] getTargets() throws IllegalStateException {
        InstanceProperties ip = getInstanceProperties();
        String serverUri = constructServerUri(ip.getProperty(GlassfishModule.HOSTNAME_ATTR),
                ip.getProperty(GlassfishModule.HTTPPORT_ATTR), null);
        Hk2Target target = new Hk2Target(serverUri);
        Hk2Target targets[] = {target};
        return targets;
    }

    /**
     * 
     * @return 
     */
    public String getUri() {
        return uri;
    }
    
    /**
     * 
     * @return 
     */
    public Hk2PluginProperties getProperties() {
        return pluginProperties;
    }
    
    /**
     * 
     * @return 
     */
    public InstanceProperties getInstanceProperties() {
        // !PW FIXME synchronization - using volatile for now, could do a little better
        if(instanceProperties == null) {
            instanceProperties = InstanceProperties.getInstanceProperties(getUri());
        }
        return instanceProperties;
    }
    
    /**
     * Get the GlassfishInstance associated with this deployment manager.
     *  
     * @return
     */
    public ServerInstance getServerInstance() {
        // !PW FIXME synchronization - using volatile for now, could do a little better
        if(serverInstance == null) {
            serverInstance = su.getServerInstance(uri);
            if(serverInstance == null) {
                String warning = "Common server instance not found for " + uri;
                Logger.getLogger("glassfish-javaee").log(Level.WARNING, warning);
                throw new IllegalStateException(warning);
            }
        }
        return serverInstance;
    }

    /**
     * Get a reference to the common support module for the server instance
     * associated with this deployment manager.
     * 
     * @return Reference to common server support impl.
     */
    public GlassfishModule getCommonServerSupport() {
        ServerInstance si = getServerInstance();
        return si.getBasicNode().getLookup().lookup(GlassfishModule.class);
    }
    
    /** 
     * Returns URI of GF (manager application).
     * 
     * @return URI without home and base specification
     */
    public String getPlainUri() {
        InstanceProperties ip = getInstanceProperties();
        return constructServerUri(ip.getProperty(GlassfishModule.HOSTNAME_ATTR),
                ip.getProperty(GlassfishModule.HTTPPORT_ATTR), "/__asadmin/");
    }
    
    /** 
     * Returns URI of hk2.
     * 
     * @return URI without home and base specification
     */
    public String getServerUri() {
        InstanceProperties ip = getInstanceProperties();
        return constructServerUri(ip.getProperty(GlassfishModule.HOSTNAME_ATTR),
                ip.getProperty(GlassfishModule.ADMINPORT_ATTR), null);
    }

    private final String constructServerUri(String host, String port, String path) {
        StringBuilder builder = new StringBuilder(128);
        builder.append("http://"); // NOI18N
        builder.append(host);
        builder.append(":"); // NOI18N
        builder.append(port);
        if(path != null && path.length() > 0) {
            builder.append(path);
        }
        return builder.toString();
    }

    public boolean isLocal() {
        boolean result = true;
        GlassfishModule commonSupport = getCommonServerSupport();
        if(commonSupport != null && commonSupport.isRemote()) {
            result = false;
        }
        return result;
    }

//    class UpdateContextRoot implements ProgressListener {
//        private MonitorProgressObject returnProgress;
//        private Hk2TargetModuleID moduleId;
//
//        UpdateContextRoot(MonitorProgressObject returnProgress,Hk2TargetModuleID moduleId) {
//            this.returnProgress = returnProgress;
//            this.moduleId = moduleId;
//        }
//
//            public void handleProgressEvent(ProgressEvent event) {
//                if (event.getDeploymentStatus().isCompleted()) {
//                    returnProgress.operationStateChanged(OperationState.RUNNING, event.getDeploymentStatus().getMessage());
//                    // let's update the context-root
//                    //
//                    RequestProcessor.getDefault().post(new Runnable() {
//                        public void run() {
//                            GetPropertyCommand gpc = new GetPropertyCommand("*." + moduleId.getModuleID() + ".context-root");
//                            Future<OperationState> result = getCommonServerSupport().execute(gpc);
//                            try {
//                                //result.get()
//                                if (result.get(60, TimeUnit.SECONDS) == OperationState.COMPLETED) {
//                                    long end = System.nanoTime();
//                                    //String installRoot = getGlassfishRoot();
//                                    //String targetInstallRoot = gpc.propertyValue();
//                                    Map<String, String> retVal = gpc.getData();
//                                    if (retVal.size() == 1) {
//                                        returnProgress.operationStateChanged(OperationState.COMPLETED, "updated the moduleid");
//                                        moduleId.setPath(retVal.entrySet().iterator().next().getValue());
//                                    }
//                                }
//                            } catch (InterruptedException ex) {
//                                returnProgress.operationStateChanged(OperationState.FAILED, "failed updating the moduleid");
//                                Exceptions.printStackTrace(ex);
//                            } catch (ExecutionException ex) {
//                                returnProgress.operationStateChanged(OperationState.FAILED, "failed updating the moduleid");
//                                Exceptions.printStackTrace(ex);
//                            } catch (TimeoutException ex) {
//                                returnProgress.operationStateChanged(OperationState.FAILED, "failed updating the moduleid");
//                                Exceptions.printStackTrace(ex);
//                            }
//                        }
//                    });
//                } else if (event.getDeploymentStatus().isFailed()) {
//                    returnProgress.operationStateChanged(OperationState.FAILED, "failed to update the moduleid");
//                } else {
//                    returnProgress.operationStateChanged(OperationState.RUNNING, event.getDeploymentStatus().getMessage());
//                }
//            }
//
//    }
}
