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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.j2ee;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.apache.maven.model.Build;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.ExecutionResultChecker;
import org.netbeans.modules.maven.api.execute.PrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.j2ee.web.WebModuleProviderImpl;
import org.netbeans.modules.maven.spi.debug.MavenDebugger;
import org.netbeans.modules.maven.j2ee.web.WebRunCustomizerPanel;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.modules.maven.spi.customizer.ModelHandleUtils;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.OutputWriter;

/**
 *
 * @author mkleint
 */
public class ExecutionChecker implements ExecutionResultChecker, PrerequisitesChecker {

    private final Project project;
    public static final String DEV_NULL = "DEV-NULL"; //NOI18N
    public static final String MODULEURI = "netbeans.deploy.clientModuleUri"; //NOI18N
    public static final String CLIENTURLPART = "netbeans.deploy.clientUrlPart"; //NOI18N

    private static final String NB_COS = ".netbeans_automatic_build"; //NOI18N


    ExecutionChecker(Project prj) {
        project = prj;
    }

    public void executionResult(RunConfig config, ExecutionContext res, int resultCode) {
        boolean depl = Boolean.parseBoolean(config.getProperties().getProperty(MavenJavaEEConstants.ACTION_PROPERTY_DEPLOY));
        if (depl && resultCode == 0) {
            if (RunUtils.hasApplicationCompileOnSaveEnabled(config)) {
                //dump the nb java support's timestamp fil in output directory..
                touchCoSTimeStamp(config, System.currentTimeMillis());
            }
            String moduleUri = config.getProperties().getProperty(MODULEURI);
            String clientUrl = config.getProperties().getProperty(CLIENTURLPART, ""); //NOI18N
            boolean redeploy = Boolean.parseBoolean(config.getProperties().getProperty(MavenJavaEEConstants.ACTION_PROPERTY_DEPLOY_REDEPLOY, "true")); //NOI18N
            boolean debugmode = Boolean.parseBoolean(config.getProperties().getProperty(MavenJavaEEConstants.ACTION_PROPERTY_DEPLOY_DEBUG_MODE)); //NOI18N
            boolean profilemode = Boolean.parseBoolean(config.getProperties().getProperty("netbeans.deploy.profilemode")); //NOI18N
            String openInBrowser = config.getProperties().getProperty(MavenJavaEEConstants.ACTION_PROPERTY_DEPLOY_OPEN);
            boolean showInBrowser = openInBrowser == null ? true : Boolean.parseBoolean( openInBrowser );

            performDeploy(res, debugmode, profilemode, moduleUri, clientUrl, 
                    redeploy, showInBrowser );
        }
    }

    private void performDeploy(ExecutionContext res, boolean debugmode, 
            boolean profilemode, String clientModuleUri, String clientUrlPart, 
            boolean forceRedeploy, boolean showInBrowser ) 
    {
        FileUtil.refreshFor(FileUtil.toFile(project.getProjectDirectory()));
        OutputWriter err = res.getInputOutput().getErr();
        OutputWriter out = res.getInputOutput().getOut();
        J2eeModuleProvider jmp = project.getLookup().lookup(J2eeModuleProvider.class);
        if (jmp == null) {
            err.println();
            err.println();
            err.println("NetBeans: Application Server deployment not available for Maven project '" + ProjectUtils.getInformation(project).getDisplayName() + "'");//NOI18N - no localization in maven build
            return;
        }
        String serverInstanceID = jmp.getServerInstanceID();
        if (DEV_NULL.equals(serverInstanceID)) {
            err.println();
            err.println();
            err.println("NetBeans: No suitable Deployment Server is defined for the project or globally.");//NOI18N - no localization in maven build now.
            //TODO - click here to setup..
            return;
        }
        ServerInstance si = Deployment.getDefault().getServerInstance(serverInstanceID);
        try {
            out.println("NetBeans: Deploying on " + (si != null ? si.getDisplayName() : serverInstanceID)); //NOI18N - no localization in maven build now.
        } catch (InstanceRemovedException ex) {
            out.println("NetBeans: Deploying on " + serverInstanceID); //NOI18N - no localization in maven build now.
        }
        try {
            out.println("    profile mode: " + profilemode); //NOI18N - no localization in maven build now.
            out.println("    debug mode: " + debugmode);//NOI18N - no localization in maven build now.
//                log.info("    clientModuleUri: " + clientModuleUri);//NOI18N - no localization in maven build now.
//                log.info("    clientUrlPart: " + clientUrlPart);//NOI18N - no localization in maven build now.
            out.println("    force redeploy: " + forceRedeploy);//NOI18N - no localization in maven build now.


            Deployment.Mode mode = Deployment.Mode.RUN;
            if (debugmode) {
                mode = Deployment.Mode.DEBUG;
            } else if (profilemode) {
                mode = Deployment.Mode.PROFILE;
            }

            String clientUrl = Deployment.getDefault().deploy(jmp, mode, clientModuleUri, clientUrlPart, forceRedeploy, new DLogger(out));
            if (clientUrl != null) {
                FileObject fo = project.getProjectDirectory();
                boolean show = showInBrowser;
                if (fo != null && show ) {
                    String browser = (String) fo.getAttribute(WebRunCustomizerPanel.PROP_SHOW_IN_BROWSER);
                    show = browser != null ? Boolean.parseBoolean(browser) : true;
                }
                if (show) {
//                        log.info("Executing browser to show " + clientUrl);//NOI18N - no localization in maven build now.
                    HtmlBrowser.URLDisplayer.getDefault().showURL(new URL(clientUrl));
                }
            }
            if (debugmode) {
                ServerDebugInfo sdi = jmp.getServerDebugInfo();

                if (sdi != null) { //fix for bug 57854, this can be null
                    String h = sdi.getHost();
                    String transport = sdi.getTransport();
                    String address = "";   //NOI18N

                    if (transport.equals(ServerDebugInfo.TRANSPORT_SHMEM)) {
                        address = sdi.getShmemName();
                    } else {
                        address = Integer.toString(sdi.getPort());
                    }
                    MavenDebugger deb = project.getLookup().lookup(MavenDebugger.class);
                    deb.attachDebugger(res.getInputOutput(), "Debug Deployed app", transport, h, address);//NOI18N - no localization in maven build now.
                }
            }
        } catch (Deployment.DeploymentException ex) {
            ex.printStackTrace(err);
            Logger.getLogger(ExecutionChecker.class.getName()).log(Level.FINE, "Exception occured wile deploying to Application Server.", ex); //NOI18N
        } catch (Exception ex) {
            Logger.getLogger(ExecutionChecker.class.getName()).log(Level.FINE, "Exception occured wile deploying to Application Server.", ex); //NOI18N
        }
    }

    public static boolean showServerSelectionDialog(Project project, J2eeModuleProvider provider, RunConfig config) {
        if (ExecutionChecker.DEV_NULL.equals(provider.getServerInstanceID())) {
            boolean isDefaultGoal = config == null ? true : neitherJettyNorCargo(config.getGoals()); //TODO how to figure if really default or overridden by user?
            SelectAppServerPanel panel = new SelectAppServerPanel(!isDefaultGoal, project);
            DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(ExecutionChecker.class, "TIT_Select"));
            panel.setNLS(dd.createNotificationLineSupport());
            Object obj = DialogDisplayer.getDefault().notify(dd);
            if (obj == NotifyDescriptor.OK_OPTION) {
                String instanceId = panel.getSelectedServerInstance();
                String serverId = panel.getSelectedServerType();
                if (!ExecutionChecker.DEV_NULL.equals(instanceId)) {
                    boolean permanent = panel.isPermanent();
                    if (permanent) {
                        persistServer(project, instanceId, serverId, panel.getChosenProject());
                    } else {
                        SessionContent sc = project.getLookup().lookup(SessionContent.class);
                        sc.setServerInstanceId(instanceId);
                        WebModuleProviderImpl prv = project.getLookup().lookup(WebModuleProviderImpl.class);
                        POHImpl poh = project.getLookup().lookup(POHImpl.class);
                        poh.hackModuleServerChange();
                        //provider instance not relevant from here
                        provider = null;
                    }

                    // USG logging
                    LogRecord record = new LogRecord(Level.INFO, "USG_PROJECT_CONFIG_MAVEN_SERVER");  //NOI18N
                    record.setLoggerName(POHImpl.USG_LOGGER_NAME);
                    record.setParameters(new Object[] { POHImpl.obtainServerName(project) });
                    POHImpl.USG_LOGGER.log(record);

                    return true;
                } else {
                    //ignored used now..
                    if (panel.isIgnored() && config != null) {
                        removeNetbeansDeployFromActionMappings(project, config.getActionName());
                        return true;
                    }
                }
            }
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(ExecutionChecker.class, "ERR_Action_without_deployment_server"));
            return false;
        }
        return true;
    }

    @Override
    public boolean checkRunConfig(RunConfig config) {
        boolean depl = Boolean.parseBoolean(config.getProperties().getProperty(MavenJavaEEConstants.ACTION_PROPERTY_DEPLOY));
        if (depl) {
            J2eeModuleProvider provider = config.getProject().getLookup().lookup(J2eeModuleProvider.class);
            if (provider != null) {
                return showServerSelectionDialog(project, provider, config);
            }
        }
        return true;
    }

    private boolean touchCoSTimeStamp(RunConfig rc, long stamp) {
        if (rc.getProject() == null) {
            return false;
        }
        Build build = rc.getMavenProject().getBuild();
        if (build == null || build.getOutputDirectory() == null) {
            return false;
        }
        File fl = new File(build.getOutputDirectory());
        fl = FileUtil.normalizeFile(fl);
        if (!fl.exists()) {
            //the project was not built
            return false;
        }
        File check = new File(fl, NB_COS);
        if (!check.exists()) {
            try {
                return check.createNewFile();
            } catch (IOException ex) {
                return false;
            }
        }
        return check.setLastModified(stamp);
    }

    public static boolean hasCoSTimeStamp(Project prj) {
        NbMavenProject nbprj = prj.getLookup().lookup(NbMavenProject.class);
        if (nbprj == null) {
            return false;
        }
        return new File(nbprj.getOutputDirectory(false), NB_COS).exists();
    }

    private static void removeNetbeansDeployFromActionMappings(Project project, String actionName) {
        try {
            ModelHandle handle = ModelHandleUtils.createModelHandle(project);
            NetbeansActionMapping mapp = ModelHandle.getActiveMapping(actionName, project);
            if (mapp != null) {
                java.util.Properties props = mapp.getProperties();
                if (props != null) {
                    props.remove(MavenJavaEEConstants.ACTION_PROPERTY_DEPLOY);
                    ModelHandle.setUserActionMapping(mapp, handle.getActionMappings());
                    handle.markAsModified(handle.getActionMappings());
                    ModelHandleUtils.writeModelHandle(handle, project);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (XmlPullParserException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static boolean neitherJettyNorCargo(List<String> goals) {
        for (String goal : goals) {
            if (goal.contains("jetty") || goal.contains("cargo")) {
                return false;
            }
        }
        return true;
    }


    static class DLogger implements Deployment.Logger {

        private OutputWriter logger;

        public DLogger(OutputWriter log) {
            logger = log;
        }

        public void log(String string) {
            logger.println(string);
        }
    }

    private static void persistServer(Project project, final String iID, final String sID, final Project targetPrj) {
        project.getLookup().lookup(AuxiliaryProperties.class).put(MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER_ID, iID, false);
        final ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
            public void performOperation(POMModel model) {
                Properties props = model.getProject().getProperties();
                if (props == null) {
                    props = model.getFactory().createProperties();
                    model.getProject().setProperties(props);
                }
                props.setProperty(MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER, sID);
            }
        };
        final FileObject projDir = targetPrj.getProjectDirectory();
        final FileObject fo = projDir.getFileObject("pom.xml"); //NOI18N
        try {
            fo.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    Utilities.performPOMModelOperations(fo, Collections.singletonList(operation));
                }
            });
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        //#109507 workaround
        POHImpl poh = project.getLookup().lookup(POHImpl.class);
        poh.hackModuleServerChange();

        // refresh all subprojects
        SubprojectProvider spp = targetPrj.getLookup().lookup(SubprojectProvider.class);
        Set<? extends Project> childrenProjs = spp.getSubprojects();
        if (!childrenProjs.contains(project)) {
            NbMavenProject.fireMavenProjectReload(project);
        }
        for (Project curPrj : childrenProjs) {
            NbMavenProject.fireMavenProjectReload(curPrj);
        }

    }
}
