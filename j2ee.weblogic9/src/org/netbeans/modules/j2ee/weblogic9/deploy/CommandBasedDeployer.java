/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.weblogic9.deploy;

import java.beans.PropertyVetoException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.dd.api.application.Module;
import org.netbeans.modules.j2ee.dd.api.application.Web;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.weblogic9.URLWait;
import org.netbeans.modules.j2ee.weblogic9.config.WLApplicationModule;
import org.netbeans.modules.j2ee.weblogic9.config.WLDatasource;
import org.netbeans.modules.j2ee.weblogic9.config.WLMessageDestination;
import org.netbeans.modules.j2ee.weblogic9.dd.model.WebApplicationModel;
import org.netbeans.modules.j2ee.weblogic9.optional.NonProxyHostsHelper;
import org.netbeans.modules.j2ee.weblogic9.ui.FailedAuthenticationSupport;
import org.netbeans.modules.weblogic.common.api.BatchDeployListener;
import org.netbeans.modules.weblogic.common.api.DeployListener;
import org.netbeans.modules.weblogic.common.api.WebLogicDeployer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 *
 * @author Petr Hejl
 */
public final class CommandBasedDeployer extends AbstractDeployer {

    private static final Logger LOGGER = Logger.getLogger(CommandBasedDeployer.class.getName());

    private static final RequestProcessor URL_WAIT_RP = new RequestProcessor("Weblogic URL Wait", 10); // NOI18N

    private static final Callable<String> NON_PROXY = new Callable<String>() {

        @Override
        public String call() throws Exception {
            return NonProxyHostsHelper.getNonProxyHosts();
        }
    };

    public CommandBasedDeployer(WLDeploymentManager deploymentManager) {
        super(deploymentManager);
    }

    public ProgressObject directoryDeploy(final Target target, String name,
            File file, String host, String port, boolean secured, J2eeModule.Type type) {
        return deploy(createModuleId(target, file, host, port, secured, name, type), file, name);
    }

    public ProgressObject directoryRedeploy(final TargetModuleID moduleId) {
        return redeploy(new TargetModuleID[] {moduleId}, null);
    }

    public ProgressObject deploy(Target[] target, final File file, final File plan,
            String host, String port, boolean secured) {
        // TODO is this correct only first server mentioned
        String name = file.getName();
        if (name.endsWith(".war") || name.endsWith(".ear")) { // NOI18N
            name = name.substring(0, name.length() - 4);
        }
        final TargetModuleID moduleId = createModuleId(target[0], file, host, port, secured, name, null);
        return deploy(moduleId, file, null);
    }

    public ProgressObject redeploy(TargetModuleID[] targetModuleID, File file, File file2) {
        return redeploy(targetModuleID, file);
    }

    public ProgressObject undeploy(final TargetModuleID[] targetModuleID) {
        final WLProgressObject progress = new WLProgressObject(targetModuleID);

        final Map<String, TargetModuleID> names = new LinkedHashMap<String, TargetModuleID>();
        for (TargetModuleID id : targetModuleID) {
            names.put(id.getModuleID(), id);
        }

        BatchDeployListener listener = new BatchDeployListener() {

            private TargetModuleID module;

            @Override
            public void onStepStart(String name) {
                module = names.get(name);
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.RUNNING,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Undeploying", name)));
            }

            @Override
            public void onStepFinish(String name) {
                // noop
            }

            @Override
            public void onStart() {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.RUNNING,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Undeployment_Started")));
            }

            @Override
            public void onFinish() {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.COMPLETED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Undeployment_Completed")));
            }

            @Override
            public void onFail(String line) {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Undeployment_Failed",
                                line)));
                FailedAuthenticationSupport.checkFailedAuthentication(getDeploymentManager(), line);
            }

            @Override
            public void onTimeout() {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Undeployment_Failed_Timeout")));
            }

            @Override
            public void onInterrupted() {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Undeployment_Failed_Interrupted")));
            }

            @Override
            public void onException(Exception ex) {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Undeployment_Failed_With_Message", ex.getMessage())));
            }
        };

        WebLogicDeployer deployer = WebLogicDeployer.getInstance(
                getDeploymentManager().getCommonConfiguration(), new File(getJavaBinary()), NON_PROXY);
        deployer.undeploy(names.keySet(), listener);

        return progress;
    }

    public ProgressObject start(final TargetModuleID[] targetModuleID) {
        final WLProgressObject progress = new WLProgressObject(targetModuleID);

        final Map<String, TargetModuleID> names = new LinkedHashMap<String, TargetModuleID>();
        for (TargetModuleID id : targetModuleID) {
            names.put(id.getModuleID(), id);
        }

        BatchDeployListener listener = new BatchDeployListener() {

            private TargetModuleID module;

            @Override
            public void onStepStart(String name) {
                module = names.get(name);
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.RUNNING,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Starting", name)));
            }

            @Override
            public void onStepFinish(String name) {
                try {
                    waitForUrlReady(getDeploymentManager(), module, progress);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (TimeoutException ex) {
                    // FIXME
                }
            }

            @Override
            public void onStart() {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.RUNNING,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Start_Started")));
            }

            @Override
            public void onFinish() {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.COMPLETED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Start_Completed")));
            }

            @Override
            public void onFail(String line) {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Start_Failed", line)));
                FailedAuthenticationSupport.checkFailedAuthentication(getDeploymentManager(), line);
            }

            @Override
            public void onTimeout() {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Start_Failed_Timeout")));
            }

            @Override
            public void onInterrupted() {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Start_Failed_Interrupted")));
            }

            @Override
            public void onException(Exception ex) {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Start_Failed_With_Message", ex.getMessage())));
            }
        };

        WebLogicDeployer deployer = WebLogicDeployer.getInstance(
                getDeploymentManager().getCommonConfiguration(), new File(getJavaBinary()), NON_PROXY);
        deployer.start(names.keySet(), listener);

        return progress;
    }

    public ProgressObject stop(final TargetModuleID[] targetModuleID) {
        final WLProgressObject progress = new WLProgressObject(targetModuleID);

        final Map<String, TargetModuleID> names = new LinkedHashMap<String, TargetModuleID>();
        for (TargetModuleID id : targetModuleID) {
            names.put(id.getModuleID(), id);
        }

        BatchDeployListener listener = new BatchDeployListener() {

            private TargetModuleID module;

            @Override
            public void onStepStart(String name) {
                module = names.get(name);
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.STOP, StateType.RUNNING,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Stopping", name)));
            }

            @Override
            public void onStepFinish(String name) {
                // noop
            }

            @Override
            public void onStart() {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.STOP, StateType.RUNNING,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Stop_Started")));
            }

            @Override
            public void onFinish() {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.STOP, StateType.COMPLETED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Stop_Completed")));
            }

            @Override
            public void onFail(String line) {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.STOP, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Stop_Failed", line)));
                FailedAuthenticationSupport.checkFailedAuthentication(getDeploymentManager(), line);
            }

            @Override
            public void onTimeout() {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.STOP, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Stop_Failed_Timeout")));
            }

            @Override
            public void onInterrupted() {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.STOP, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Stop_Failed_Interrupted")));
            }

            @Override
            public void onException(Exception ex) {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.STOP, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Stop_Failed_With_Message", ex.getMessage())));
            }
        };

        WebLogicDeployer deployer = WebLogicDeployer.getInstance(
                getDeploymentManager().getCommonConfiguration(), new File(getJavaBinary()), NON_PROXY);
        deployer.stop(names.keySet(), listener);

        return progress;
    }

    public ProgressObject deployDatasource(final Collection<WLDatasource> datasources) {
        return deployApplicationModules(datasources, NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Module_Datasource"));
    }

    public ProgressObject deployMessageDestinations(final Collection<WLMessageDestination> destinations) {
        return deployApplicationModules(destinations, NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Module_JMS"));
    }

    private ProgressObject deployApplicationModules(
            final Collection<? extends WLApplicationModule> modules, final String moduleDisplayName) {

        final String upperDisplayName = moduleDisplayName.length() <= 0 ? moduleDisplayName :
                Character.toUpperCase(moduleDisplayName.charAt(0)) + moduleDisplayName.substring(1);

        final WLProgressObject progress = new WLProgressObject(new TargetModuleID[0]);

        BatchDeployListener listener = new BatchDeployListener() {

            @Override
            public void onStepStart(String name) {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.RUNNING,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Module_Deploying",
                            new Object[] {moduleDisplayName, name})));
            }

            @Override
            public void onStepFinish(String name) {
                // noop
            }

            @Override
            public void onStart() {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.RUNNING,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Module_Started", upperDisplayName)));
            }

            @Override
            public void onFinish() {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.COMPLETED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Module_Completed", upperDisplayName)));
            }

            @Override
            public void onFail(String line) {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Module_Failed",
                            new Object[]{upperDisplayName, line})));
                FailedAuthenticationSupport.checkFailedAuthentication(getDeploymentManager(), line);
            }

            @Override
            public void onTimeout() {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Module_Failed_Timeout",
                            upperDisplayName)));
            }

            @Override
            public void onInterrupted() {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Module_Failed_Interrupted",
                            upperDisplayName)));
            }

            @Override
            public void onException(Exception ex) {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Module_Failed_With_Message",
                            new Object[]{upperDisplayName, ex.getLocalizedMessage()})));
            }
        };

        List<WebLogicDeployer.Artifact> artifacts = new ArrayList<WebLogicDeployer.Artifact>(modules.size());
        for (WLApplicationModule module : modules) {
            if (module.getOrigin() == null) {
                LOGGER.log(Level.INFO, "Could not deploy {0}", module.getName());
                continue;
            }
            artifacts.add(new WebLogicDeployer.Artifact(module.getOrigin(), module.getName(), false));
        }

        WebLogicDeployer deployer = WebLogicDeployer.getInstance(
                getDeploymentManager().getCommonConfiguration(), new File(getJavaBinary()), NON_PROXY);
        deployer.deploy(artifacts, listener);

        return progress;
    }

    public ProgressObject deployLibraries(final Set<File> libraries) {
        final WLProgressObject progress = new WLProgressObject(new TargetModuleID[0]);

        BatchDeployListener listener = new BatchDeployListener() {

            @Override
            public void onStepStart(String name) {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Library_Deploying", name)));
            }

            @Override
            public void onStepFinish(String name) {
                // noop
            }

            @Override
            public void onStart() {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Library_Started")));
            }

            @Override
            public void onFinish() {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.COMPLETED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Library_Completed")));
            }

            @Override
            public void onFail(String line) {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Library_Failed", line)));
                FailedAuthenticationSupport.checkFailedAuthentication(getDeploymentManager(), line);
            }

            @Override
            public void onTimeout() {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Library_Failed_Timeout")));
            }

            @Override
            public void onInterrupted() {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Library_Failed_Interrupted")));
            }

            @Override
            public void onException(Exception ex) {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Library_Failed_With_Message", ex.getMessage())));
            }
        };

        List<WebLogicDeployer.Artifact> artifacts = new ArrayList<WebLogicDeployer.Artifact>(libraries.size());
        for (File lib : libraries) {
            artifacts.add(new WebLogicDeployer.Artifact(lib, null, true));
        }

        WebLogicDeployer deployer = WebLogicDeployer.getInstance(
                getDeploymentManager().getCommonConfiguration(), new File(getJavaBinary()), NON_PROXY);
        deployer.deploy(artifacts, listener);

        return progress;
    }

    private ProgressObject deploy(final TargetModuleID moduleId, final File file, String name) {
        final WLProgressObject progress = new WLProgressObject(moduleId);

        DeployListener listener = new DeployListener() {

            @Override
            public void onStart() {
                progress.fireProgressEvent(moduleId, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Deploying", file.getAbsolutePath())));
            }

            @Override
            public void onFinish() {
                progress.fireProgressEvent(moduleId, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.COMPLETED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Deployment_Completed")));
            }

            @Override
            public void onFail(String line) {
                progress.fireProgressEvent(moduleId, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Deployment_Failed", line)));
                FailedAuthenticationSupport.checkFailedAuthentication(getDeploymentManager(), line);
            }

            @Override
            public void onTimeout() {
                progress.fireProgressEvent(moduleId, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Deployment_Failed_Timeout")));
            }

            @Override
            public void onInterrupted() {
                progress.fireProgressEvent(moduleId, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Deployment_Failed_Interrupted")));
            }

            @Override
            public void onException(Exception ex) {
                progress.fireProgressEvent(moduleId, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Deployment_Failed_With_Message", ex.getMessage())));
            }
        };

        WebLogicDeployer deployer = WebLogicDeployer.getInstance(
                getDeploymentManager().getCommonConfiguration(), new File(getJavaBinary()), NON_PROXY);
        deployer.deploy(file, listener, name);

        return progress;
    }

    // FIXME we should check the source of module if it differs this should do undeploy/deploy
    private ProgressObject redeploy(final TargetModuleID[] targetModuleID, final File file) {
        assert file == null || targetModuleID.length == 1;
        final WLProgressObject progress = new WLProgressObject(targetModuleID);

        final Map<String, TargetModuleID> names = new LinkedHashMap<String, TargetModuleID>();
        for (TargetModuleID id : targetModuleID) {
            names.put(id.getModuleID(), id);
        }

        BatchDeployListener listener = new BatchDeployListener() {

            private TargetModuleID module;

            @Override
            public void onStepStart(String name) {
                module = names.get(name);
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Redeploying", name)));
            }

            @Override
            public void onStepFinish(String name) {
                // noop
            }

            @Override
            public void onStart() {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.START, StateType.RUNNING,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Redeployment_Started")));
            }

            @Override
            public void onFinish() {
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.COMPLETED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Redeployment_Completed")));
            }

            @Override
            public void onFail(String line) {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Redeployment_Failed", line)));
                FailedAuthenticationSupport.checkFailedAuthentication(getDeploymentManager(), line);
            }

            @Override
            public void onTimeout() {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Redeployment_Failed_Timeout")));
            }

            @Override
            public void onInterrupted() {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Redeployment_Failed_Interrupted")));
            }

            @Override
            public void onException(Exception ex) {
                progress.fireProgressEvent(module, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                        NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Redeployment_Failed_With_Message", ex.getMessage())));
            }
        };

        WebLogicDeployer deployer = WebLogicDeployer.getInstance(
                getDeploymentManager().getCommonConfiguration(), new File(getJavaBinary()), NON_PROXY);
        if (file != null) {
            deployer.redeploy(targetModuleID[0].getModuleID(), file, listener);
        } else {
            deployer.redeploy(new ArrayList<String>(names.keySet()), listener);
        }

        return progress;
    }

    private String getJavaBinary() {
        // TODO configurable ? or use the jdk server is running on ?
        JavaPlatform platform = JavaPlatformManager.getDefault().getDefaultPlatform();
        Collection<FileObject> folders = platform.getInstallFolders();
        String javaBinary = Utilities.isWindows() ? "java.exe" : "java"; // NOI18N
        if (folders.size() > 0) {
            FileObject folder = folders.iterator().next();
            File file = FileUtil.toFile(folder);
            if (file != null) {
                javaBinary = file.getAbsolutePath() + File.separator
                        + "bin" + File.separator
                        + (Utilities.isWindows() ? "java.exe" : "java"); // NOI18N
            }
        }
        return javaBinary;
    }

    private static void waitForUrlReady(WLDeploymentManager dm, TargetModuleID moduleID,
            WLProgressObject progressObject) throws InterruptedException, TimeoutException {

        // prevent hitting the old content
        Thread.sleep(3000);
        long start = System.currentTimeMillis();
        String webUrl = moduleID.getWebURL();
        if (webUrl == null) {
            TargetModuleID[] ch = moduleID.getChildTargetModuleID();
            if (ch != null) {
                for (int i = 0; i < ch.length; i++) {
                    webUrl = ch[i].getWebURL();
                    if (webUrl != null) {
                        waitForUrlReady(dm, webUrl, progressObject, start);
                    }
                }
            }
        } else {
            waitForUrlReady(dm, webUrl, progressObject, start);
        }
    }

    private static void waitForUrlReady(WLDeploymentManager dm, String webUrl, WLProgressObject progressObject,
            long start) throws InterruptedException, TimeoutException {

        if (webUrl != null) {
            try {
                String realUrl = webUrl;

                // FIXME this is bit hacky
                if (dm.getCommonConfiguration().isSecured()
                        && realUrl.startsWith("http:") // NOI18N
                        && realUrl.contains(":" + dm.getPort() + "/")) { // NOI18N
                    realUrl = "https:" + realUrl.substring(5); // NOI18N
                }
                URL url = new URL(realUrl);
                String waitingMsg = NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Waiting_For_Url", url);

                progressObject.fireProgressEvent(null,
                        new WLDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, waitingMsg));

                int timeout = 1000;
                while (System.currentTimeMillis() - start < TIMEOUT) {
                    if (URLWait.waitForUrlReady(dm, URL_WAIT_RP, url, timeout)) {
                        break;
                    }
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }
                    if (timeout < TIMEOUT / 10) {
                        timeout = Math.min(TIMEOUT / 10, 2 * timeout);
                    }
                }
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }
    }

    private static WLTargetModuleID createModuleId(Target target, File file,
            String host, String port, boolean secured, String name, J2eeModule.Type type) {

        WLTargetModuleID moduleId = new WLTargetModuleID(target, name, file);

        try {
            String serverUrl = (secured ? "https://" : "http://") + host + ":" + port;

            // TODO in fact we should look to deployment plan for overrides
            // for now it is as good as previous solution
            if (J2eeModule.Type.WAR.equals(type) || (type == null && file.getName().endsWith(".war"))) { // NOI18N
                FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
                if (fo != null) {
                    configureWarModuleId(moduleId, fo, serverUrl);
                }
            } else if (J2eeModule.Type.EAR.equals(type) || (type == null && file.getName().endsWith(".ear"))) { // NOI18N
                configureEarModuleId(moduleId, file, serverUrl);
            }

        } catch (Exception e) {
            LOGGER.log(Level.INFO, null, e);
        }

        return moduleId;
    }

    private static void configureEarModuleId(WLTargetModuleID moduleId, File file, String serverUrl) {
        try {
            FileObject root = null;
            if (file.isDirectory()) {
                root = FileUtil.toFileObject(FileUtil.normalizeFile(file));
            } else {
                JarFileSystem jfs = new JarFileSystem();
                jfs.setJarFile(file);
                root = jfs.getRoot();
            }
            if (root == null) {
                return;
            }

            FileObject appXml = root.getFileObject("META-INF/application.xml"); // NOI18N
            if (appXml != null) {
                InputStream is = new BufferedInputStream(appXml.getInputStream());
                try {
                    // we used getDDRoot(FO), but the caching has been returning
                    // old model - see #194656
                    Application ear = DDProvider.getDefault().getDDRoot(new InputSource(is));
                    Module[] modules = ear.getModule();
                    for (int i = 0; i < modules.length; i++) {
                        WLTargetModuleID childModuleId = null;
                        Web web = modules[i].getWeb();
                        if (web != null) {
                            childModuleId = new WLTargetModuleID(moduleId.getTarget(), web.getWebUri());
                        } else {
                            childModuleId = new WLTargetModuleID(moduleId.getTarget());
                        }

                        if (modules[i].getWeb() != null) {
                            String context = modules[i].getWeb().getContextRoot();
                            String contextUrl = getContextUrl(serverUrl, context);
                            childModuleId.setContextURL(contextUrl);
                        }
                        moduleId.addChild(childModuleId);
                    }
                } finally {
                    is.close();
                }
            } else {
                // Java EE 5
                for (FileObject child : root.getChildren()) {
                    // this should work for exploded directory as well
                    if (child.hasExt("war") || child.hasExt("jar")) { // NOI18N
                        WLTargetModuleID childModuleId =
                                new WLTargetModuleID(moduleId.getTarget(), child.getNameExt());

                        if (child.hasExt("war")) { // NOI18N
                            configureWarModuleId(childModuleId, child, serverUrl);
                        }
                        moduleId.addChild(childModuleId);
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
        } catch (PropertyVetoException ex) {
            LOGGER.log(Level.INFO, null, ex);
        } catch (SAXException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }

    private static void configureWarModuleId(WLTargetModuleID moduleId, FileObject file, String serverUrl) {
        String contextUrl = getContextUrl(serverUrl, readWebContext(file));
        moduleId.setContextURL(contextUrl);
    }

    private static String getContextUrl(String serverUrl, String context) {
        StringBuilder builder = new StringBuilder(serverUrl);
        if (serverUrl.endsWith("/")) {
            builder.setLength(builder.length() - 1);
        }
        if (context != null) {
            if (!context.startsWith("/")) {
                LOGGER.log(Level.INFO, "Context path should start with forward slash while it is {0}", context);
                builder.append('/');
            }
            builder.append(context);
        }
        return builder.toString();
    }

    public static String readWebContext(FileObject file) {
        if (file.isFolder()) {
            FileObject weblogicXml = file.getFileObject("WEB-INF/weblogic.xml"); // NOI18N
            if (weblogicXml != null && weblogicXml.isData()) {
                try {
                    InputStream is = new BufferedInputStream(weblogicXml.getInputStream());
                    try {
                        return WebApplicationModel.forInputStream(is).getContextRoot();
                    } finally {
                        is.close();
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }
            return "/" + file.getNameExt(); // NOI18N
        } else {
            try {
                ZipInputStream zis = new ZipInputStream(file.getInputStream());
                try {
                    ZipEntry entry = null;
                    while ((entry = zis.getNextEntry()) != null) {
                        if ("WEB-INF/weblogic.xml".equals(entry.getName())) { // NOI18N
                            return WebApplicationModel.forInputStream(new ZipEntryInputStream(zis)).getContextRoot();
                        }
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, "Error reading context-root", ex); // NOI18N
                } finally {
                    zis.close();
                }
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
            return "/" + file.getName(); // NOI18N
        }
    }

    private static class ZipEntryInputStream extends InputStream {

        private final ZipInputStream zis;

        public ZipEntryInputStream(ZipInputStream zis) {
            this.zis = zis;
        }

        @Override
        public int available() throws IOException {
            return zis.available();
        }

        @Override
        public void close() throws IOException {
            zis.closeEntry();
        }

        @Override
        public int read() throws IOException {
            if (available() > 0) {
                return zis.read();
            }
            return -1;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return zis.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            return zis.skip(n);
        }
    }

}
