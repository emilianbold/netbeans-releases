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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import oracle.nuviaq.api.PlatformManager;
import oracle.nuviaq.api.PlatformManagerConnectionFactory;
import oracle.nuviaq.api.PlatformManagerException;
import oracle.nuviaq.model.xml.ApplicationDeploymentType;
import oracle.nuviaq.model.xml.JobType;
import oracle.nuviaq.model.xml.PlatformDeploymentType;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.cloud.common.spi.support.serverplugin.DeploymentStatus;
import org.netbeans.modules.cloud.common.spi.support.serverplugin.ProgressObjectImpl;
import org.netbeans.modules.cloud.oracle.serverplugin.OracleJ2EEInstance;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Describes single Amazon account.
 */
public class OracleInstance {

    private static final RequestProcessor ORACLE_RP = new RequestProcessor("oracle cloud 9", 10); // NOI18N
    
    private static final Logger LOG = Logger.getLogger(OracleInstance.class.getSimpleName());
    
    private final String name;
    private final String tenantUserName;
    private final String tenantPassword;
    private final String urlEndpoint;
    
    private ServerInstance serverInstance;
    
    private PlatformManager platform;

    public OracleInstance(String name, String tenantUserName, String tenantPassword, String urlEndpoint) {
        this.name = name;
        this.tenantUserName = tenantUserName;
        this.tenantPassword = tenantPassword;
        this.urlEndpoint = urlEndpoint;
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

    public synchronized PlatformManager getPlatformManager() {
        if (platform == null) {
            try {
                platform = PlatformManagerConnectionFactory.createServiceEndpoint(new URL(urlEndpoint), tenantUserName, tenantPassword);
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return platform;
    }
    
    public void testConnection() throws PlatformManagerException {
        assert !SwingUtilities.isEventDispatchThread();
        getPlatformManager().listJobs();
    }
    
    public List<OracleJ2EEInstance> readJ2EEServerInstances() {
        assert !SwingUtilities.isEventDispatchThread();
        List<OracleJ2EEInstance> res = new ArrayList<OracleJ2EEInstance>();

        for (PlatformDeploymentType p : getPlatformManager().listPlatformInstances()) {
            OracleJ2EEInstance inst = new OracleJ2EEInstance(this, p.getInstanceId());
            //inst.updateState(?????);
            res.add(inst);
        }
        return res;
    }
    public static Future<DeploymentStatus> deployAsync(final PlatformManager pm, final File f, 
                         final String instanceId, 
                         final ProgressObjectImpl po) {
        return runAsynchronously(new Callable<DeploymentStatus>() {
            @Override
            public DeploymentStatus call() throws Exception {
                String url[] = new String[1];
                DeploymentStatus ds = deploy(pm, f, instanceId, po, url);
                LOG.log(Level.INFO, "deployment result: "+ds); // NOI18N
                po.updateDepoymentResult(ds, url[0]);
                return ds;
            }
        });
    }
    
    public static DeploymentStatus deploy(PlatformManager pm, File f, String instanceId, 
                          ProgressObjectImpl po, String[] url) {
        assert !SwingUtilities.isEventDispatchThread();
        try {
            if (po != null) {
                po.updateDepoymentStage(NbBundle.getMessage(OracleInstance.class, "MSG_UPLOADING_APP"));
            }
            assert f.exists() : "archive does not exist: "+f;
            String appContext = f.getName().substring(0, f.getName().lastIndexOf('.'));
            InputStream is = new FileInputStream(f);
            ApplicationDeploymentType adt =new ApplicationDeploymentType();
            adt.setInstanceId(instanceId);
            
            // XXX: what should this be???
            //adt.setApplicationId("id"+System.currentTimeMillis());
            adt.setApplicationId(appContext);
            
            adt.setArchiveUrl(f.getName());
            
            boolean redeploy = false;
            List<ApplicationDeploymentType> apps = pm.listApplications(instanceId);
            for (ApplicationDeploymentType app : apps) {
                if (app.getApplicationId().equals(appContext)) {
                    redeploy = true;
                    adt = app;
                    break;
                }
            }
            
            JobType jt;
            if (redeploy) {
                LOG.log(Level.INFO, "redeploying: archive="+f+" "+adt); // NOI18N
                jt = pm.redeployApplication(is, adt);
                LOG.log(Level.INFO, "redeployed as "+jt.getJobId()+" "+jt); // NOI18N
            } else {
                LOG.log(Level.INFO, "deploying: archive="+f+" "+adt); // NOI18N
                jt = pm.deployApplication(is, adt);
                LOG.log(Level.INFO, "deployed as "+jt.getJobId()+" "+jt); // NOI18N
            }
            
            if (po != null) {
                po.updateDepoymentStage(NbBundle.getMessage(OracleInstance.class, redeploy ? "MSG_REDEPLOYING_APP" : "MSG_DEPLOYING_APP"));
            }
            
            while (true) {
                try {
                    // let's wait
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                po.updateDepoymentStage(NbBundle.getMessage(OracleInstance.class, redeploy ? "MSG_REDEPLOYING_APP" : "MSG_DEPLOYING_APP"));
                JobType latestJob = pm.describeJob(jt.getJobId());
                String jobStatus = latestJob.getStatus();
                if ("Complete".equals(jobStatus)) {
                    
                    // XXX: how do I get this one:
                    
                    url[0] = "http://localhost:7001/"+appContext+"/";
                    
                    return DeploymentStatus.SUCCESS;
                } else if ("submitted".equalsIgnoreCase(jobStatus)) {
                    // let's wait longer
                } else if ("running".equalsIgnoreCase(jobStatus)) {
                    // let's wait longer
                } else if ("failed".equalsIgnoreCase(jobStatus)) {
                    return DeploymentStatus.FAILED;
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return DeploymentStatus.UNKNOWN;
        } catch (PlatformManagerException ex) {
            Exceptions.printStackTrace(ex);
            return DeploymentStatus.UNKNOWN;
        } catch (Throwable t) {
            Exceptions.printStackTrace(t);
            return DeploymentStatus.UNKNOWN;
        }
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

    public List<ApplicationDeploymentType> getApplications(String instanceID) {
        assert !SwingUtilities.isEventDispatchThread();
        return getPlatformManager().listApplications(instanceID);
    }

    public void undeploy(ApplicationDeploymentType app) {
        assert !SwingUtilities.isEventDispatchThread();
        getPlatformManager().undeployApplication(app.getInstanceId(), app.getApplicationId());
    }
    
}
