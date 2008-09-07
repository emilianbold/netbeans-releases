/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.groovy.grailsproject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.extexecution.api.ExecutionDescriptor.InputProcessorFactory;
import org.netbeans.modules.extexecution.api.ExecutionDescriptor;
import org.netbeans.modules.extexecution.api.ExecutionService;
import org.netbeans.modules.extexecution.api.input.InputProcessor;
import org.netbeans.modules.extexecution.api.input.InputProcessors;
import org.netbeans.modules.extexecution.api.input.LineProcessor;
import org.netbeans.modules.groovy.grails.api.ExecutionSupport;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grails.api.GrailsRuntime;
import org.netbeans.modules.groovy.grailsproject.actions.ConfigSupport;
import org.netbeans.modules.groovy.grailsproject.actions.RefreshProjectRunnable;
import org.netbeans.modules.groovy.support.api.GroovySettings;
import org.netbeans.modules.web.client.tools.api.JSToNbJSLocationMapper;
import org.netbeans.modules.web.client.tools.api.LocationMappersFactory;
import org.netbeans.modules.web.client.tools.api.NbJSToJSLocationMapper;
import org.netbeans.modules.web.client.tools.api.WebClientToolsProjectUtils;
import org.netbeans.modules.web.client.tools.api.WebClientToolsSessionException;
import org.netbeans.modules.web.client.tools.api.WebClientToolsSessionStarterService;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.LifecycleManager;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Petr Hejl
 */
public class GrailsActionProvider implements ActionProvider {

    public static final String COMMAND_GRAILS_SHELL = "grails-shell"; // NOI18N
    public static final String COMMAND_COMPILE = "compile"; // NOI18N
    public static final String COMMAND_STATS = "stats"; // NOI18N
    public static final String COMMAND_UPGRADE = "upgrade"; // NOI18N
    public static final String COMMAND_WAR = "war"; // NOI18N

    private static final ExecutionDescriptor GRAILS_DESCRIPTOR = new ExecutionDescriptor()
            .controllable(true).frontWindow(true).inputVisible(true)
                .showProgress(true).optionsPath(GroovySettings.GROOVY_OPTIONS_CATEGORY);

    private static final ExecutionDescriptor RUN_DESCRIPTOR = GRAILS_DESCRIPTOR.showSuspended(true);

    private static final Logger LOGGER = Logger.getLogger(GrailsActionProvider.class.getName());

    private static final String[] supportedActions = {
        COMMAND_RUN,
        COMMAND_TEST,
        COMMAND_CLEAN,
        COMMAND_DELETE,
        COMMAND_GRAILS_SHELL,
        COMMAND_COMPILE,
        COMMAND_STATS,
        COMMAND_UPGRADE,
        COMMAND_WAR
    };

    private final GrailsProject project;

    public GrailsActionProvider(GrailsProject project) {
        this.project = project;
    }


    public String[] getSupportedActions() {
        if (WebClientToolsSessionStarterService.isAvailable()) {
            String[] debugSupportedActions = new String[supportedActions.length + 1];
            for (int i = 0; i < supportedActions.length; i++) {
                debugSupportedActions[i] = supportedActions[i];
            }
            debugSupportedActions[supportedActions.length] = COMMAND_DEBUG;
            return debugSupportedActions;
        } else {
            return supportedActions.clone();
        }
    }

    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        final GrailsRuntime runtime = GrailsRuntime.getInstance();
        if (!runtime.isConfigured()) {
            ConfigSupport.showConfigurationWarning(runtime);
            return;
        }

        if (COMMAND_RUN.equals(command)) {
            LifecycleManager.getDefault().saveAll();
            executeRunAction();
        } else if (COMMAND_DEBUG.equals(command)) {
            LifecycleManager.getDefault().saveAll();
            executeRunAction(true);
        } else if (COMMAND_GRAILS_SHELL.equals(command)) {
            executeShellAction();
        } else if (COMMAND_TEST.equals(command)) {
            executeSimpleAction("test-app"); // NOI18N
        } else if (COMMAND_CLEAN.equals(command)) {
            executeSimpleAction("clean"); // NOI18N
        } else if (COMMAND_COMPILE.equals(command)) {
            executeSimpleAction("compile"); // NOI18N
        } else if (COMMAND_STATS.equals(command)) {
            executeSimpleAction("stats"); // NOI18N
        } else if (COMMAND_UPGRADE.equals(command)) {
            executeSimpleAction("upgrade"); // NOI18N
        } else if (COMMAND_DELETE.equals(command)) {
            DefaultProjectOperations.performDefaultDeleteOperation(project);
        }
    }

    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        return true;
    }

    private void executeRunAction() {
        executeRunAction(false);
    }
    
    private void executeRunAction(final boolean debug) {
        final GrailsServerState serverState = project.getLookup().lookup(GrailsServerState.class);
        if (serverState != null && serverState.isRunning()) {
            URL url = serverState.getRunningUrl();
            if (url != null) {
                showURL(url, debug, project);
            }
            return;
        }

        Callable<Process> callable = new Callable<Process>() {

            public Process call() throws Exception {
                Callable<Process> inner = ExecutionSupport.getInstance().createRunApp(GrailsProjectConfig.forProject(project));
                Process process = inner.call();
                final GrailsServerState serverState = project.getLookup().lookup(GrailsServerState.class);
                if (serverState != null) {
                    serverState.setProcess(process);
                }
                return process;
            }
        };

        ProjectInformation inf = project.getLookup().lookup(ProjectInformation.class);
        String displayName = inf.getDisplayName() + " (run-app)"; // NOI18N
        Runnable runnable = new Runnable() {
            public void run() {
                final GrailsServerState serverState = project.getLookup().lookup(GrailsServerState.class);
                if (serverState != null) {
                    serverState.setProcess(null);
                    serverState.setRunningUrl(null);
                }
            }
        };

        ExecutionDescriptor descriptor = RUN_DESCRIPTOR;
        descriptor = descriptor.outProcessorFactory(new InputProcessorFactory() {
            public InputProcessor newInputProcessor() {
                return InputProcessors.bridge(new ServerURLProcessor(project, debug));
            }
        });
        descriptor = descriptor.postExecution(runnable);

        ExecutionService service = ExecutionService.newService(callable, descriptor, displayName);
        service.run();
    }

    private void executeShellAction() {
        String command = "shell"; // NOI18N

        GrailsProjectConfig config = GrailsProjectConfig.forProject(project);
        File directory = FileUtil.toFile(config.getProject().getProjectDirectory());

        // XXX this is workaround for jline bug (native access to console on windows) used by grails
        Properties props = new Properties();
        props.setProperty("jline.WindowsTerminal.directConsole", "false"); // NOI18N

        GrailsRuntime.CommandDescriptor descriptor = new GrailsRuntime.CommandDescriptor(
                        command, directory, config.getEnvironment(), new String[] {}, props);
        Callable<Process> callable = GrailsRuntime.getInstance().createCommand(descriptor);

        ProjectInformation inf = project.getLookup().lookup(ProjectInformation.class);
        String displayName = inf.getDisplayName() + " (shell)"; // NOI18N

        ExecutionDescriptor execDescriptor = RUN_DESCRIPTOR.postExecution(
                new RefreshProjectRunnable(project));

        ExecutionService service = ExecutionService.newService(callable, execDescriptor, displayName);
        service.run();
    }

    private void executeSimpleAction(String command) {
        ProjectInformation inf = project.getLookup().lookup(ProjectInformation.class);
        String displayName = inf.getDisplayName() + " (" + command + ")"; // NOI18N

        Callable<Process> callable = ExecutionSupport.getInstance().createSimpleCommand(
                command, GrailsProjectConfig.forProject(project));

        ExecutionDescriptor descriptor = GRAILS_DESCRIPTOR.postExecution(
                new RefreshProjectRunnable(project));

        ExecutionService service = ExecutionService.newService(callable, descriptor, displayName);
        service.run();
    }

    private static final void showURL(URL url, boolean debug, GrailsProject project) {
        boolean debuggerAvailable = WebClientToolsSessionStarterService.isAvailable();
        
        if (!debug || !debuggerAvailable) {
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        } else {
            FileObject webAppDir = project.getProjectDirectory().getFileObject(GrailsProjectFactory.WEB_APP_DIR);
            GrailsProjectConfig config = GrailsProjectConfig.forProject(project);
            
            String port = config.getPort();
            String prefix = url.getProtocol() + "://" + url.getHost() + ":" + port + "/" + project.getProjectDirectory().getName();
            String actualURL = url.toExternalForm();
            
            Lookup debugLookup;
            if (!actualURL.startsWith(prefix)) {
                LOGGER.warning("Could not construct URL mapper for JavaScript debugger.");
                debugLookup = Lookups.fixed(project);
            } else {
                LocationMappersFactory factory = Lookup.getDefault().lookup(LocationMappersFactory.class);
                
                if (factory == null) {
                    debugLookup = Lookups.fixed(project);
                } else {
                    try {
                        URI prefixURI = new URI(prefix);

                        JSToNbJSLocationMapper forwardMapper = factory.getJSToNbJSLocationMapper(webAppDir, prefixURI, null);
                        NbJSToJSLocationMapper reverseMapper = factory.getNbJSToJSLocationMapper(webAppDir, prefixURI, null);

                        debugLookup = Lookups.fixed(forwardMapper, reverseMapper, project);
                    } catch (URISyntaxException ex) {
                        LOGGER.log(Level.WARNING, "Server URI could not be constructed from displayed URL", ex);
                        debugLookup = Lookups.fixed(project);
                    }
                }
            }
            
            try {
                URI launchURI = url.toURI();
                HtmlBrowser.Factory browser = WebClientToolsProjectUtils.getFirefoxBrowser();

                String browserString = config.getDebugBrowser();
                if (browserString == null) {
                    browserString = WebClientToolsProjectUtils.Browser.FIREFOX.name();
                }
                if (WebClientToolsProjectUtils.Browser.valueOf(browserString) == WebClientToolsProjectUtils.Browser.INTERNET_EXPLORER) {
                     browser = WebClientToolsProjectUtils.getInternetExplorerBrowser();
                }
                WebClientToolsSessionStarterService.startSession(launchURI, browser, debugLookup);
            } catch (URISyntaxException ex) {
                LOGGER.log(Level.SEVERE, "Unable to obtain URI for URL", ex);
            } catch (WebClientToolsSessionException ex) {
                LOGGER.log(Level.SEVERE, "Unexpected exception launching javascript debugger", ex);
            }
        }
    }
    
    private static class ServerURLProcessor implements LineProcessor {

        private final GrailsProject project;
        private final boolean debug;

        public ServerURLProcessor(GrailsProject project, boolean debug) {
            this.project = project;
            this.debug = debug;
        }

        public void processLine(String line) {
            if (line.contains("Browse to http:/")) {
                String urlString = line.substring(line.indexOf("http://"));

                URL url;
                try {
                    url = new URL(urlString);
                } catch (MalformedURLException ex) {
                    LOGGER.log(Level.WARNING, "Could not start browser", ex);
                    return;
                }

                GrailsServerState state = project.getLookup().lookup(GrailsServerState.class);
                if (state != null) {
                    state.setRunningUrl(url);
                }

                showURL(url, debug, project);
            }
        }

        public void reset() {
            // noop
        }

        public void close() {
            // noop
        }
    }
}
