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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import oracle.nuviaq.api.ApplicationManager;
import oracle.nuviaq.api.ApplicationManagerConnectionFactory;
import oracle.nuviaq.exception.ManagerException;
import oracle.nuviaq.model.xml.ApplicationDeployment;
import oracle.nuviaq.model.xml.Job;
import oracle.nuviaq.model.xml.Log;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.cloud.common.spi.support.serverplugin.DeploymentStatus;
import org.netbeans.modules.cloud.common.spi.support.serverplugin.ProgressObjectImpl;
import org.netbeans.modules.cloud.oracle.serverplugin.OracleJ2EEInstance;
import org.netbeans.modules.cloud.oracle.whitelist.WhiteListAction;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.cloud.WhiteListTool;
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

    private static final RequestProcessor ORACLE_RP = new RequestProcessor("oracle cloud 9", 10); // NOI18N
    
    private static final Logger LOG = Logger.getLogger(OracleInstance.class.getSimpleName());
    
    private final String name;
    private String tenantUserName;
    private String tenantPassword;
    private String urlEndpoint;
    private String tenantId;
    private String serviceName;
    private String onPremiseServerInstanceId;
    
    private ServerInstance serverInstance;
    
    private ApplicationManager platform;
    
    public OracleInstance(String name, String tenantUserName, String tenantPassword, 
          String urlEndpoint, String tenantId, String serviceName, String onPremiseServerInstanceId) {
        this.name = name;
        this.tenantUserName = tenantUserName;
        this.tenantPassword = tenantPassword;
        this.urlEndpoint = urlEndpoint;
        this.tenantId = tenantId;
        this.serviceName = serviceName;
        this.onPremiseServerInstanceId = onPremiseServerInstanceId;
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
    
    public String getTenantPassword() {
        return tenantPassword;
    }

    public String getTenantUserName() {
        return tenantUserName;
    }

    public String getUrlEndpoint() {
        return urlEndpoint;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setPlatform(ApplicationManager platform) {
        this.platform = platform;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
        resetCache();
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
        resetCache();
    }

    public void setTenantPassword(String tenantPassword) {
        this.tenantPassword = tenantPassword;
        resetCache();
    }

    public void setTenantUserName(String tenantUserName) {
        this.tenantUserName = tenantUserName;
        resetCache();
    }

    public void setUrlEndpoint(String urlEndpoint) {
        this.urlEndpoint = urlEndpoint;
        resetCache();
    }

    public String getOnPremiseServerInstanceId() {
        return onPremiseServerInstanceId;
    }

    private synchronized void resetCache() {
        platform = null;
    }
    
    public synchronized ApplicationManager getApplicationManager() {
        if (platform == null) {
            platform = createApplicationManager(urlEndpoint, tenantUserName, tenantPassword);
        }
        return platform;
    }
    
    public static ApplicationManager createApplicationManager(String urlEndpoint, String tenantUserName, String tenantPassword) {
        try {
            String url = urlEndpoint;
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
    
    public List<OracleJ2EEInstance> readJ2EEServerInstances() {
        assert !SwingUtilities.isEventDispatchThread();
        List<OracleJ2EEInstance> res = new ArrayList<OracleJ2EEInstance>();

        // used to be dynamic list; keeping as list for now:
        OracleJ2EEInstance inst = new OracleJ2EEInstance(this, getTenantId(), getServiceName());
        res.add(inst);
        
        return res;
    }
    public static Future<DeploymentStatus> deployAsync(final String urlEndpoint, final ApplicationManager pm, final File f, 
                         final String tenantId, 
                         final String serviceName, 
                         final ProgressObjectImpl po,
                         final String cloudInstanceName,
                         final String onPremiseServiceInstanceId) {
        return runAsynchronously(new Callable<DeploymentStatus>() {
            @Override
            public DeploymentStatus call() throws Exception {
                String url[] = new String[1];
                DeploymentStatus ds = deploy(urlEndpoint, pm, f, tenantId, serviceName, po, url, cloudInstanceName, onPremiseServiceInstanceId);
                LOG.log(Level.INFO, "deployment result: "+ds); // NOI18N
                po.updateDepoymentResult(ds, url[0]);
                return ds;
            }
        });
    }
    
    public static DeploymentStatus deploy(String urlEndpoint, ApplicationManager am, File f, String tenantId, String serviceName, 
                          ProgressObjectImpl po, String[] url, String cloudInstanceName, String onPremiseServiceInstanceId) {
        assert !SwingUtilities.isEventDispatchThread();
        OutputWriter ow = null;
        OutputWriter owe = null;
        try {
            assert f.exists() : "archive does not exist: "+f;
            if (po != null) {
                po.updateDepoymentStage(NbBundle.getMessage(OracleInstance.class, "MSG_WHITELIST_APP"));
            }
            String name = "";
            Project p = FileOwnerQuery.getOwner(FileUtil.toFileObject(f));
            if (p != null) {
                name = ProjectUtils.getInformation(p).getDisplayName();
            }
            String tabName = NbBundle.getMessage(OracleInstance.class, "MSG_DeploymentOutput", cloudInstanceName, name);
            File weblogic = findWeblogicJar(onPremiseServiceInstanceId);
            if (weblogic != null) {
                if (!WhiteListTool.execute(f, tabName, weblogic)) {
    //                return DeploymentStatus.FAILED;
                }
            }
            
            InputOutput io = IOProvider.getDefault().getIO(tabName, false);
            ow = io.getOut();
            owe = io.getErr();
            if (weblogic == null) {
                owe.println(NbBundle.getMessage(OracleInstance.class, "MSG_NO_WEBLOGIC"));
            }
            if (po != null) {
                po.updateDepoymentStage(NbBundle.getMessage(OracleInstance.class, "MSG_UPLOADING_APP"));
                ow.println(NbBundle.getMessage(OracleInstance.class, "MSG_UPLOADING_APP"));
            }
            String appContext = f.getName().substring(0, f.getName().lastIndexOf('.'));
            InputStream is = new FileInputStream(f);
            ApplicationDeployment adt =new ApplicationDeployment();
            adt.setInstanceId(serviceName);
            adt.setApplicationId(appContext);
            adt.setArchiveUrl(f.getName());
            boolean redeploy = false;
            List<ApplicationDeployment> apps = am.listApplications(tenantId, serviceName);
            for (ApplicationDeployment app : apps) {
                if (app.getApplicationId().equals(appContext)) {
                    redeploy = true;
                    adt = app;
                    break;
                }
            }
            
            Job jt;
            if (redeploy) {
                LOG.log(Level.INFO, "redeploying: archive="+f+" "+adt); // NOI18N
                jt = am.redeployApplication(is, tenantId, serviceName, adt);
                LOG.log(Level.INFO, "redeployed as "+jt.getJobId()+" "+jt); // NOI18N
            } else {
                LOG.log(Level.INFO, "deploying: archive="+f+" "+adt); // NOI18N
                jt = am.deployApplication(is, tenantId, serviceName, adt);
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
                String jobStatus = latestJob.getStatus();
                numberOfJobsToIgnore = dumpLog(am, ow, owe, latestJob, numberOfJobsToIgnore);
                if ("Complete".equals(jobStatus)) {
                    
                    // XXX: how do I get this one:
                    
                    url[0] = urlEndpoint+appContext+"/";

                    ow.println();
                    ow.println(NbBundle.getMessage(OracleInstance.class, "MSG_Deployment_OK", url[0]));
                    return DeploymentStatus.SUCCESS;
                } else if ("submitted".equalsIgnoreCase(jobStatus)) {
                    // let's wait longer
                } else if ("running".equalsIgnoreCase(jobStatus)) {
                    // let's wait longer
                } else if ("failed".equalsIgnoreCase(jobStatus)) {
                    ow.println();
                    ow.println(NbBundle.getMessage(OracleInstance.class, "MSG_Deployment_FAILED"));
                    return DeploymentStatus.FAILED;
                }
            }
        } catch (IOException ex) {
            if (owe != null) {
                owe.print(ex.toString());
            }
            Exceptions.printStackTrace(ex);
            return DeploymentStatus.UNKNOWN;
        } catch (ManagerException ex) {
            if (owe != null) {
                owe.print(ex.toString());
            }
            Exceptions.printStackTrace(ex);
            return DeploymentStatus.UNKNOWN;
        } catch (Throwable t) {
            if (owe != null) {
                owe.print(t.toString());
            }
            Exceptions.printStackTrace(t);
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
                owe.println("Exception occured while retrieving the log:\n"+t.toString());
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
//        tasks.add(f);
        return f;
    }
    
//    private static List<Future> tasks = new ArrayList<Future>();

    public List<ApplicationDeployment> getApplications() {
        assert !SwingUtilities.isEventDispatchThread();
        return getApplicationManager().listApplications(getTenantId(), getServiceName());
    }

    public void undeploy(ApplicationDeployment app) {
        assert !SwingUtilities.isEventDispatchThread();
        getApplicationManager().undeployApplication(getTenantId(), getServiceName(), app.getApplicationId());
    }
    
    public void start(ApplicationDeployment app) {
        assert !SwingUtilities.isEventDispatchThread();
        getApplicationManager().startApplication(getTenantId(), getServiceName(), app.getApplicationId());
    }
    
    public void stop(ApplicationDeployment app) {
        assert !SwingUtilities.isEventDispatchThread();
        getApplicationManager().stopApplication(getTenantId(), getServiceName(), app.getApplicationId());
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
