/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cloud.oracle;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import oracle.cloud.paas.api.ApplicationManager;
import oracle.cloud.paas.api.ApplicationManagerConnectionFactory;
import oracle.cloud.paas.exception.ManagerException;
import oracle.cloud.paas.model.Application;
import oracle.cloud.paas.model.ApplicationType;
import oracle.cloud.paas.model.Job;
import oracle.cloud.paas.model.JobStatus;
import oracle.cloud.paas.model.Log;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.cloud.common.spi.support.serverplugin.DeploymentStatus;
import org.netbeans.modules.cloud.common.spi.support.serverplugin.ProgressObjectImpl;
import org.netbeans.modules.cloud.oracle.serverplugin.OracleDeploymentFactory;
import org.netbeans.modules.cloud.oracle.serverplugin.OracleJ2EEInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.deploy.CommandBasedDeployer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * Describes single Amazon account.
 */
public class OracleInstance {

    private static final RequestProcessor ORACLE_RP = new RequestProcessor("oracle cloud", 10); // NOI18N
    
    private static final Logger LOG = Logger.getLogger(OracleInstance.class.getSimpleName());
    
    private final String name;
    private String user;
    private String password;
    private String adminURL;
    private String instanceURL;
    private String cloudURL;
    private String serviceGroup;
    private String serviceInstance;
    private String onPremiseServerInstanceId;
    
    private ServerInstance serverInstance;
    
    private ApplicationManager platform;
    
    /* GuardedBy(this) */
    private OracleJ2EEInstance j2eeInstance;
    
    public OracleInstance(String name, String tenantUserName, String tenantPassword, 
          String adminURL, String instanceURL, String cloudURL, String serviceGroup, String serviceInstance, String onPremiseServerInstanceId) {
        this.name = name;
        this.user = tenantUserName;
        this.password = tenantPassword;
        this.adminURL = adminURL;
        this.instanceURL = instanceURL;
        this.cloudURL = cloudURL;
        this.serviceGroup = serviceGroup;
        this.serviceInstance = serviceInstance;
        this.onPremiseServerInstanceId = onPremiseServerInstanceId;
    }

    public String getCloudURL() {
        return cloudURL;
    }

    public void setCloudURL(String cloudURL) {
        this.cloudURL = cloudURL;
    }

    public String getInstanceURL() {
        return instanceURL;
    }

    public void setInstanceURL(String instanceURL) {
        this.instanceURL = instanceURL;
        synchronized (this) {
            if (j2eeInstance != null) {
                j2eeInstance.getInstanceProperties().setProperty(OracleDeploymentFactory.IP_INSTANCE_URL, getInstanceURL());
            }
        }
    }

    void setServerInstance(ServerInstance serverInstance) {
        this.serverInstance = serverInstance;
    }

    public ServerInstance getServerInstance() {
        return serverInstance;
    }

    public String getName() {
        return name;
    }
    
    public String getPassword() {
        return password;
    }

    public String getUser() {
        return user;
    }

    public String getAdminURL() {
        return adminURL;
    }

    public String getServiceInstance() {
        return serviceInstance;
    }

    public String getServiceGroup() {
        return serviceGroup;
    }

    public void setPlatform(ApplicationManager platform) {
        this.platform = platform;
    }

    public void setServiceInstance(String serviceInstance) {
        this.serviceInstance = serviceInstance;
        resetCache();
    }
    
    public void setServiceGroup(String serviceGroup) {
        this.serviceGroup = serviceGroup;
        resetCache();
    }

    public void setPassword(String tenantPassword) {
        this.password = tenantPassword;
        synchronized (this) {
            if (j2eeInstance != null) {
                j2eeInstance.getInstanceProperties().setProperty(
                                    InstanceProperties.PASSWORD_ATTR, tenantPassword);
            }
        }
        resetCache();
    }

    public void setUser(String tenantUserName) {
        this.user = tenantUserName;
        synchronized (this) {
            if (j2eeInstance != null) {
                j2eeInstance.getInstanceProperties().setProperty(
                                    InstanceProperties.USERNAME_ATTR, tenantUserName);
            }
        }
        resetCache();
    }

    public void setAdminURL(String urlEndpoint) {
        this.adminURL = urlEndpoint;
        synchronized (this) {
            if (j2eeInstance != null) {
                j2eeInstance.getInstanceProperties().setProperty(OracleDeploymentFactory.IP_ADMIN_URL, getAdminURL());
            }
        }
        resetCache();
    }

    public String getOnPremiseServerInstanceId() {
        return onPremiseServerInstanceId;
    }

    public void setOnPremiseServerInstanceId(String onPremiseServerInstanceId) {
        this.onPremiseServerInstanceId = onPremiseServerInstanceId;
        synchronized (this) {
            if (j2eeInstance != null) {
                j2eeInstance.getInstanceProperties().setProperty(
                        OracleDeploymentFactory.IP_PREMISE_SERVICE_INSTANCE_ID, onPremiseServerInstanceId);
            }
        }
    }
    
    private synchronized void resetCache() {
        platform = null;
    }
    
    public synchronized ApplicationManager getApplicationManager() {
        if (platform == null) {
            platform = createApplicationManager(adminURL, user, password);
        }
        return platform;
    }
    
    public static ApplicationManager createApplicationManager(String adminUrl, String tenantUserName, String tenantPassword) {
        try {
            String url = adminUrl;
            if (!url.endsWith("/")) {
                url += "/";
            }
            url += "manager/rest"; // NOI18N
            return ApplicationManagerConnectionFactory.createServiceEndpoint(new URL(url), tenantUserName, tenantPassword);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    
    public void testConnection() throws ManagerException {
        assert !SwingUtilities.isEventDispatchThread();
        getApplicationManager().listJobs();
    }
    
    public OracleJ2EEInstance readJ2EEServerInstance() {
        assert !SwingUtilities.isEventDispatchThread();
        OracleJ2EEInstance inst = new OracleJ2EEInstance(this);
        synchronized (this) {
            j2eeInstance = inst;
            return j2eeInstance;
        }
    }
    
    public void deregisterJ2EEServerInstances() {
        OracleJ2EEInstance instance;
        synchronized (this) {
            instance = j2eeInstance;
        }
        if (instance != null) {
            instance.deregister();
        }
    }
    
    public static Future<DeploymentStatus> deployAsync(final String instanceUrl, final ApplicationManager pm, final File f, 
                         final String serviceGroup, 
                         final String serviceName, 
                         final ProgressObjectImpl po,
                         final String cloudInstanceName,
                         final String onPremiseServiceInstanceId) {
        return runAsynchronously(new Callable<DeploymentStatus>() {
            @Override
            public DeploymentStatus call() throws Exception {
                String url[] = new String[1];
                DeploymentStatus ds = deploy(instanceUrl, pm, f, serviceGroup, serviceName, po, url, cloudInstanceName, onPremiseServiceInstanceId);
                LOG.log(Level.INFO, "deployment result: "+ds); // NOI18N
                po.updateDepoymentResult(ds, url[0]);
                return ds;
            }
        });
    }
    
    public static DeploymentStatus deploy(String instanceURL, ApplicationManager am, File f, String serviceGroup, String serviceName, 
                          ProgressObjectImpl po, String[] url, String cloudInstanceName, String onPremiseServiceInstanceId) {
        assert !SwingUtilities.isEventDispatchThread();
        OutputWriter ow = null;
        OutputWriter owe = null;
        try {
            assert f.exists() : "archive does not exist: "+f;
            String name = "";
            FileObject fo = FileUtil.toFileObject(f);
            Project p = FileOwnerQuery.getOwner(fo);
            if (p != null) {
                name = ProjectUtils.getInformation(p).getDisplayName();
            }
            String tabName = NbBundle.getMessage(OracleInstance.class, "MSG_DeploymentOutput", cloudInstanceName);
            InputOutput io = IOProvider.getDefault().getIO(tabName, false);
            if (io.isClosed()) {
                io = IOProvider.getDefault().getIO(tabName, true);
            }
            ow = io.getOut();
            owe = io.getErr();
            if (po != null) {
                po.updateDepoymentStage(NbBundle.getMessage(OracleInstance.class, "MSG_UPLOADING_APP"));
                ow.println(NbBundle.getMessage(OracleInstance.class, "MSG_UPLOADING_APP"));
            }
            String appId = f.getName().substring(0, f.getName().lastIndexOf('.'));
            InputStream is = new FileInputStream(f);
            ApplicationType at = ApplicationType.WAR;
            if ("EAR".equalsIgnoreCase(fo.getExt())) { // NOI18N
                at = ApplicationType.EAR;
            }
            String ctx = CommandBasedDeployer.readWebContext(fo);
            boolean redeploy = false;
            List<Application> apps = am.listApplications(serviceGroup, serviceName);
            for (Application app : apps) {
                if (app.getApplicationName() != null && app.getApplicationName().equals(appId)) {
                    redeploy = true;
                    break;
                }
            }
            
            Job jt;
            if (redeploy) {
                LOG.log(Level.INFO, "redeploying: archive="+f); // NOI18N
                jt = am.redeployApplication(serviceGroup, serviceName, appId, is);
                LOG.log(Level.INFO, "redeployed as "+jt.getJobId()+" "+jt); // NOI18N
            } else {
                LOG.log(Level.INFO, "deploying: archive="+f); // NOI18N
                jt = am.deployApplication(serviceGroup, serviceName, appId, at, is);
                LOG.log(Level.INFO, "deployed as "+jt.getJobId()+" "+jt); // NOI18N
            }
            
            if (po != null) {
                po.updateDepoymentStage(NbBundle.getMessage(OracleInstance.class, redeploy ? "MSG_REDEPLOYING_APP" : "MSG_DEPLOYING_APP"));
                ow.print(NbBundle.getMessage(OracleInstance.class, redeploy ? "MSG_REDEPLOYING_APP" : "MSG_DEPLOYING_APP"));
            }
            
            int numberOfJobsToIgnore = -1;
            while (true) {
                try {
                    // let's wait
                    ow.print(".");
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                po.updateDepoymentStage(NbBundle.getMessage(OracleInstance.class, redeploy ? "MSG_REDEPLOYING_APP" : "MSG_DEPLOYING_APP"));
                Job latestJob = am.describeJob(jt.getJobId());
                JobStatus jobStatus = latestJob.getStatus();
                numberOfJobsToIgnore = dumpLog(am, ow, owe, latestJob, numberOfJobsToIgnore);
                if (JobStatus.COMPLETE.equals(jobStatus)) {
                    url[0] = instanceURL+(instanceURL.endsWith("/") ? ctx.substring(1) : ctx);
                    ow.println();
                    ow.println(NbBundle.getMessage(OracleInstance.class, "MSG_Deployment_OK", url[0]));
                    return DeploymentStatus.SUCCESS;
                } else if (JobStatus.SUBMITTED.equals(jobStatus)) {
                    // let's wait longer
                } else if (JobStatus.RUNNING.equals(jobStatus)) {
                    // let's wait longer
                } else if (JobStatus.FAILED.equals(jobStatus)) {
                    ow.println();
                    ow.println(NbBundle.getMessage(OracleInstance.class, "MSG_Deployment_FAILED"));
                    return DeploymentStatus.FAILED;
                }
            }
        } catch (IOException ex) {
            if (owe != null) {
                ex.printStackTrace(owe);
            }
            return DeploymentStatus.UNKNOWN;
        } catch (ManagerException ex) {
            if (owe != null) {
                ex.printStackTrace(owe);
            }
            return DeploymentStatus.UNKNOWN;
        } catch (Throwable t) {
            if (owe != null) {
                t.printStackTrace(owe);
            }
            return DeploymentStatus.UNKNOWN;
        } finally {
            if (ow != null) {
                ow.close();
            }
            if (owe != null) {
                owe.close();
            }
        }
    }
    
    private static int dumpLog(ApplicationManager am, OutputWriter ow, OutputWriter owe, Job latestJob, int numberOfJobsToIgnore) {
        int i = 0;
        for (Log lt : latestJob.getLogs()) {
            i++;
            if (numberOfJobsToIgnore > 0) {
                numberOfJobsToIgnore--;
                continue;
            }
            ow.println("\n==================== Log file: "+lt.getName()+"==========================\n");
            ByteArrayOutputStream os = new ByteArrayOutputStream(8000);
            try {
                am.fetchJobLog(latestJob.getJobId(), lt.getName(), os);
            } catch (Throwable t) {
                owe.println("Exception occured while retrieving the log:");
                t.printStackTrace(owe);
                continue;
            }
            try {
                ow.println(os.toString(Charset.defaultCharset().name()));
            } catch (UnsupportedEncodingException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return i;
    }
    
    public static <T> Future<T> runAsynchronously(Callable<T> callable) {
        return runAsynchronously(callable, null);
    }
    
    public static synchronized <T> Future<T> runAsynchronously(Callable<T> callable, OracleInstance ai) {
        Future<T> f = ORACLE_RP.submit(callable);
        return f;
    }
    
    public List<Application> getApplications() {
        assert !SwingUtilities.isEventDispatchThread();
        return getApplicationManager().listApplications(getServiceGroup(), getServiceInstance());
    }

    public void undeploy(Application app) {
        assert !SwingUtilities.isEventDispatchThread();
        getApplicationManager().undeployApplication(getServiceGroup(), getServiceInstance(), app.getApplicationName());
    }
    
    public void start(Application app) {
        assert !SwingUtilities.isEventDispatchThread();
        getApplicationManager().startApplication(getServiceGroup(), getServiceInstance(), app.getApplicationName());
    }
    
    public void stop(Application app) {
        assert !SwingUtilities.isEventDispatchThread();
        getApplicationManager().stopApplication(getServiceGroup(), getServiceInstance(), app.getApplicationName());
    }
    
    public static File findWeblogicJar(String onPremiseServerInstanceId) {
        if (onPremiseServerInstanceId == null) {
            return null;
        }
        try {
            File home = Deployment.getDefault().getServerInstance(onPremiseServerInstanceId).getJ2eePlatform().getServerHome();
            return WLPluginProperties.getWeblogicJar(home);
        } catch (InstanceRemovedException ex) {
            // ignore
        }
        return null;
    }
    
}
