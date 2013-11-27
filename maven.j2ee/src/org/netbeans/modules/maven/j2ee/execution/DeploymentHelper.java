/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee.execution;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.model.Build;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import static org.netbeans.modules.maven.j2ee.execution.ExecutionChecker.DEV_NULL;
import org.netbeans.modules.maven.j2ee.MavenJavaEEConstants;
import org.netbeans.modules.maven.j2ee.OneTimeDeployment;
import static org.netbeans.modules.maven.j2ee.execution.Bundle.Choose_server;
import org.netbeans.modules.maven.j2ee.ui.customizer.impl.CustomizerRunWeb;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;
import org.netbeans.modules.maven.spi.debug.MavenDebugger;
import org.netbeans.modules.web.browser.spi.URLDisplayerImplementation;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.OutputWriter;

/**
 * General helper class encapsulating behavior needed to perform deployment execution.
 *
 * <p>
 * This class is <i>immutable</i> and thus <i>thread safe</i>.
 * </p>
 *
 * @author Martin Janicek <mjanicek@netbeans.org>
 */
public final class DeploymentHelper {

    private static final String MODULEURI = "netbeans.deploy.clientModuleUri"; //NOI18N
    private static final String NB_COS = ".netbeans_automatic_build"; //NOI18N
    private static final Logger LOGGER = Logger.getLogger(DeploymentHelper.class.getName());

    public static final String CLIENTURLPART = "netbeans.deploy.clientUrlPart"; //NOI18N

    /**
     * Mapping between Maven goals and Server IDs.
     *
     * This allows us to find correct server based on the goal used in nbaction.xml
     * <p>
     *
     * Key = Prefix of the Maven goal. For example all goals from maven tomcat plugin are starting with "tomcat".
     * <br/><br/>
     * Value = Server ID used across the IDE. For example name of the Glassfish server in the IDE is "gfv3ee6".
     */
    private static final Map<String, String> serverPrefixes = new HashMap<>();
    static {
        serverPrefixes.put("glassfish", "gfv3ee6"); // https://maven-glassfish-plugin.java.net/
        serverPrefixes.put("weblogic", "WebLogic"); // http://mojo.codehaus.org/weblogic-maven-plugin/plugin-info.html
        serverPrefixes.put("tomcat", "Tomcat"); // http://tomcat.apache.org/maven-plugin-2.0/tomcat7-maven-plugin/plugin-info.html
        serverPrefixes.put("jboss", "JBoss4"); // http://docs.jboss.org/jbossas/7/plugins/maven/latest/
    }


    private DeploymentHelper() {
    }


    /**
     * Performs deploy based on the given arguments.
     *
     * @param config configuration
     * @param executionContext execution context
     * @return {@literal true} if the execution was successful, {@literal false} otherwise
     */
    public static boolean perform(final RunConfig config, final ExecutionContext executionContext) {
        final Project project = config.getProject();

        if (RunUtils.isCompileOnSaveEnabled(config)) {
            //dump the nb java support's timestamp fil in output directory..
            touchCoSTimeStamp(config, System.currentTimeMillis());
        }
        String moduleUri = config.getProperties().get(MODULEURI);
        String clientUrlPart = config.getProperties().get(CLIENTURLPART);
        if (clientUrlPart == null) {
            clientUrlPart = ""; // NOI18N
        }
        boolean redeploy = isRedeploy(config);
        boolean debugmode = isDebugMode(config);
        boolean profilemode = isProfileMode(config);
        boolean showInBrowser = showInBrowser(config);

        FileUtil.refreshFor(FileUtil.toFile(project.getProjectDirectory()));
        OutputWriter err = executionContext.getInputOutput().getErr();
        OutputWriter out = executionContext.getInputOutput().getOut();
        final J2eeModuleProvider jmp = project.getLookup().lookup(J2eeModuleProvider.class);
        if (jmp == null) {
            err.println();
            err.println();
            err.println("NetBeans: Application Server deployment not available for Maven project '" + ProjectUtils.getInformation(project).getDisplayName() + "'"); // NOI18N
            return false;
        }

        String serverInstanceID = null;

        // First check if the one-time deployment server is set
        OneTimeDeployment oneTimeDeployment = project.getLookup().lookup(OneTimeDeployment.class);
        if (oneTimeDeployment != null) {
            serverInstanceID = oneTimeDeployment.getServerInstanceId();
        }

        Deployment.Mode mode = Deployment.Mode.RUN;
        if (debugmode) {
            mode = Deployment.Mode.DEBUG;
        } else if (profilemode) {
            mode = Deployment.Mode.PROFILE;
        }

        if (serverInstanceID == null) {
            serverInstanceID = jmp.getServerInstanceID();
            if (DEV_NULL.equals(serverInstanceID) || serverInstanceID == null) {
                // No server set within the IDE --> Try to check nbactions.xml for server goals
                // See issue #237618
                String[] serverInstances = findActionMappingServer(project, mode);
                if (serverInstances.length != 0) {
                    // If only one server instance of searching type is registered, use that one
                    if (serverInstances.length == 1) {
                        serverInstanceID = serverInstances[0];
                        jmp.setServerInstanceID(serverInstanceID);

                    // If there is more than one server of searching type, ask user which one should be used
                    } else {
                        String chosenServer = chooseServer(serverInstances);
                        if (chosenServer != null) {
                            serverInstanceID = chosenServer;
                            jmp.setServerInstanceID(chosenServer);
                        } else {
                            // User clicked on cancel button --> End the execution
                            return true;
                        }
                    }
                } else {
                    err.println("NetBeans: No suitable Deployment Server is defined for the project or globally."); // NOI18N
                    return false;
                }
            }
        }
        ServerInstance si = Deployment.getDefault().getServerInstance(serverInstanceID);
        try {
            out.println("NetBeans: Deploying on " + (si != null ? si.getDisplayName() : serverInstanceID)); //NOI18N - no localization in maven build now.
        } catch (InstanceRemovedException ex) {
            out.println("NetBeans: Deploying on " + serverInstanceID); // NOI18N
        }
        try {
            out.println("    profile mode: " + profilemode); // NOI18N
            out.println("    debug mode: " + debugmode); // NOI18N
            out.println("    force redeploy: " + redeploy); //NOI18N

            Callable<Void> debuggerHook = null;
            if (debugmode) {
                debuggerHook = new Callable<Void>() {

                    @Override
                    public Void call() throws Exception {
                        ServerDebugInfo sdi = jmp.getServerDebugInfo();

                        if (sdi != null) { //fix for bug 57854, this can be null
                            String h = sdi.getHost();
                            String transport = sdi.getTransport();
                            String address;

                            if (transport.equals(ServerDebugInfo.TRANSPORT_SHMEM)) {
                                address = sdi.getShmemName();
                            } else {
                                address = Integer.toString(sdi.getPort());
                            }
                            MavenDebugger deb = project.getLookup().lookup(MavenDebugger.class);
                            try {
                                deb.attachDebugger(executionContext.getInputOutput(), "Debug Deployed app", transport, h, address); // NOI18N
                            } catch (Exception ex) {
                                // See issue #235796 --> We were not able to attach debugger because
                                // it's already attached, BUT we still want to deploy the application
                                LOGGER.log(Level.FINE, "Exception occured while trying to attach debugger", ex); //NOI18N
                            }
                        }
                        return null;
                    }
                };

            }

            String clientUrl = Deployment.getDefault().deploy(jmp, mode, moduleUri, clientUrlPart, redeploy, new DeploymentLogger(out), debuggerHook);
            if (clientUrl != null) {
                if (showInBrowser) {
                    URL url = new URL(clientUrl);
                    URLDisplayerImplementation urlDisplayer = project.getLookup().lookup(URLDisplayerImplementation.class);
                    if (urlDisplayer != null) {
                        URL appRoot = url;
                        if (clientUrlPart.length() > 0 && clientUrl.endsWith(clientUrlPart)) {
                            appRoot = new URL(clientUrl.substring(0, clientUrl.length() - clientUrlPart.length()));
                        }
                        urlDisplayer.showURL(appRoot, url, project.getProjectDirectory());
                    } else {
                        HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                    }
                }
            }
        } catch (Deployment.DeploymentException | MalformedURLException ex) {
            LOGGER.log(Level.FINE, "Exception occured wile deploying to Application Server.", ex); //NOI18N
        }

        // Reset the value of the one-time server
        if (oneTimeDeployment != null) {
            oneTimeDeployment.reset();
            MavenProjectSupport.changeServer(project, false);
        }
        return true;
    }

    private static boolean isRedeploy(RunConfig config) {
        return readBooleanValue(config, MavenJavaEEConstants.ACTION_PROPERTY_DEPLOY_REDEPLOY, true);
    }

    private static boolean isDebugMode(RunConfig config) {
        return readBooleanValue(config, MavenJavaEEConstants.ACTION_PROPERTY_DEPLOY_DEBUG_MODE, false);
    }

    private static boolean isProfileMode(RunConfig config) {
        return readBooleanValue(config, "netbeans.deploy.profilemode", false); //NOI18N
    }

    private static boolean showInBrowser(RunConfig config) {
        if (!readBooleanValue(config, MavenJavaEEConstants.ACTION_PROPERTY_DEPLOY_OPEN, true)) {
            return false;
        }

        FileObject projectDir = config.getProject().getProjectDirectory();
        if (projectDir != null) {
            String browser = (String) projectDir.getAttribute(CustomizerRunWeb.PROP_SHOW_IN_BROWSER);
            if (browser != null && Boolean.parseBoolean(browser)) {
                return false;
            }
        }
        return true;
    }

    private static boolean readBooleanValue(RunConfig config, String key, boolean defaultValue) {
        String value = config.getProperties().get(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    private static boolean touchCoSTimeStamp(RunConfig rc, long stamp) {
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

    /**
     * Tries to guess server based on the goal's defined in the nbaction.xml file.
     *
     * If there is a goal tomcat7:run defined as an action for standard run action,
     * then we should probably deploy/run application on Tomcat even if the server
     * is not set for the project.
     *
     * See issue #237618 for more details.
     *
     * @param project project
     * @param mode information about deployment mode (Run/Debug/Profile)
     * @return server instance ID if we have positive guess, {@literal null} otherwise
     */
    private static String[] findActionMappingServer(Project project, Deployment.Mode mode) {
        ProjectConfigurationProvider configProvider = project.getLookup().lookup(ProjectConfigurationProvider.class);
        if (configProvider != null) {
            ProjectConfiguration projectConfig = configProvider.getActiveConfiguration();
            NetbeansActionMapping actionMapping = ModelHandle2.getMapping(getActionName(mode), project, projectConfig);

            if (actionMapping != null) {
                // Iterates over goals and check if some of them is starting with server prefix.
                // If it is, then we should try to find such server registered in the IDE and use
                // it for the deployment.
                for (String goal : actionMapping.getGoals()) {
                    for (Map.Entry<String, String> entry : serverPrefixes.entrySet()) {
                        if (goal.startsWith(entry.getKey())) {
                            for (String serverID : Deployment.getDefault().getServerIDs()) {
                                if (serverID.equals(entry.getValue())) {
                                    return Deployment.getDefault().getInstancesOfServer(serverID);
                                }
                            }
                        }
                    }
                }
            }
        }

        return new String[0];
    }

    private static String getActionName(Deployment.Mode mode) {
        switch (mode) {
            case RUN: return "run";         // NOI18N
            case DEBUG: return "debug";     // NOI18N
            case PROFILE: return "profile"; // NOI18N
        }
        return "run"; // NOI18N
    }

    @Messages("Choose_server=Choose server")
    private static String chooseServer(String[] serverInstances) {
        ServerInstanceChooserPanel panel = new ServerInstanceChooserPanel(serverInstances);
        DialogDescriptor dd = new DialogDescriptor(panel, Choose_server());
        Object result = DialogDisplayer.getDefault().notify(dd);
        if (result == NotifyDescriptor.OK_OPTION) {
            return panel.getChosenServerInstance();
        }
        return null;
    }
}
