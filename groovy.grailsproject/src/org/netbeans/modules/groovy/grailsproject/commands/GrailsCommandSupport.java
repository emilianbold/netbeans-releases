/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grailsproject.commands;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.groovy.grails.api.ExecutionSupport;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grails.api.GrailsPlatform;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.GrailsServerState;
import org.netbeans.modules.groovy.grailsproject.actions.RefreshProjectRunnable;
import org.netbeans.modules.groovy.support.api.GroovySettings;
import org.netbeans.modules.web.client.tools.api.JSToNbJSLocationMapper;
import org.netbeans.modules.web.client.tools.api.LocationMappersFactory;
import org.netbeans.modules.web.client.tools.api.NbJSToJSLocationMapper;
import org.netbeans.modules.web.client.tools.api.WebClientToolsProjectUtils;
import org.netbeans.modules.web.client.tools.api.WebClientToolsSessionException;
import org.netbeans.modules.web.client.tools.api.WebClientToolsSessionStarterService;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.windows.InputOutput;

/**
 *
 * @author Petr Hejl
 */
public final class GrailsCommandSupport {

    private static final ExecutionDescriptor GRAILS_DESCRIPTOR = new ExecutionDescriptor()
            .controllable(true).frontWindow(true).inputVisible(true)
                .showProgress(true).optionsPath(GroovySettings.GROOVY_OPTIONS_CATEGORY);

    private static final ExecutionDescriptor RUN_DESCRIPTOR = GRAILS_DESCRIPTOR.showSuspended(true);

    private static final InputProcessorFactory ANSI_STRIPPING = new AnsiStrippingInputProcessorFactory();

    private static final Logger LOGGER = Logger.getLogger(GrailsCommandSupport.class.getName());

    private static final String WEB_APP_DIR = "web-app"; // NOI18N

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private final GrailsProject project;

    private PluginListener pluginListener;

    private List<GrailsCommand> commands;

    public GrailsCommandSupport(GrailsProject project) {
        this.project = project;
    }

    public synchronized List<GrailsCommand> getGrailsCommands() {
        return commands;
    }

    public ExecutionDescriptor getRunDescriptor() {
        return getDescriptor(GrailsPlatform.IDE_RUN_COMMAND);
    }

    public ExecutionDescriptor getDescriptor(String command) {
        return getDescriptor(command, null, false);
    }

    public ExecutionDescriptor getDescriptor(String command, InputProcessorFactory outFactory) {
        return getDescriptor(command, outFactory, false);
    }

    public ExecutionDescriptor getDescriptor(String command, InputProcessorFactory outFactory, final boolean debug) {
        if (GrailsPlatform.IDE_RUN_COMMAND.equals(command)) {

            ExecutionDescriptor descriptor = RUN_DESCRIPTOR;
            InputProcessorFactory urlFactory = new InputProcessorFactory() {
                public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                    return InputProcessors.proxy(defaultProcessor,
                            InputProcessors.bridge(new ServerURLProcessor(project, debug)));
                }
            };

            if (outFactory != null) {
                descriptor = descriptor.outProcessorFactory(new ProxyInputProcessorFactory(urlFactory, outFactory));
            } else {
                descriptor = descriptor.outProcessorFactory(urlFactory);
            }
            return descriptor;
        } else if ("shell".equals(command)) { // NOI18N
            ExecutionDescriptor descriptor = RUN_DESCRIPTOR.postExecution(new RefreshProjectRunnable(project))
                    .errProcessorFactory(ANSI_STRIPPING);
            if (outFactory != null) {
                descriptor = descriptor.outProcessorFactory(new ProxyInputProcessorFactory(ANSI_STRIPPING, outFactory));
            } else {
                descriptor = descriptor.outProcessorFactory(ANSI_STRIPPING);
            }
            return descriptor;
        } else {
            ExecutionDescriptor descriptor = GRAILS_DESCRIPTOR.postExecution(new RefreshProjectRunnable(project))
                    .errProcessorFactory(ANSI_STRIPPING);
            if (outFactory != null) {
                descriptor = descriptor.outProcessorFactory(new ProxyInputProcessorFactory(ANSI_STRIPPING, outFactory));
            } else {
                descriptor = descriptor.outProcessorFactory(ANSI_STRIPPING);
            }
            return descriptor;
        }
    }

    public void refreshGrailsCommands() {
        Callable<Process> callable = ExecutionSupport.getInstance().createSimpleCommand("help", // NOI18N
                GrailsProjectConfig.forProject(project));
        final HelpLineProcessor lineProcessor = new HelpLineProcessor();

        ExecutionDescriptor descriptor = new ExecutionDescriptor().inputOutput(InputOutput.NULL)
                .outProcessorFactory(new ExecutionDescriptor.InputProcessorFactory() {

            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                // we are sure this will be invoked at most once
                return InputProcessors.bridge(lineProcessor);
            }
        });

        List<GrailsCommand> freshCommands = Collections.emptyList();
        ExecutionService service = ExecutionService.newService(callable, descriptor, "help"); // NOI18N
        Future<Integer> task = service.run();
        try {
            if (task.get().intValue() == 0) {
                freshCommands = new ArrayList<GrailsCommand>();
                for (String command : lineProcessor.getCommands()) {
                    freshCommands.add(new GrailsCommand(command, null, command)); // NOI18N
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }

        synchronized (this) {
            // FIXME this is 1.0 code only
            if (pluginListener == null) {
                pluginListener = new PluginListener();
                File folder = FileUtil.toFile(project.getProjectDirectory());
                // weakly referenced
                FileUtil.addFileChangeListener(pluginListener, new File(folder, "plugins")); // NOI18N
            }

            this.commands = freshCommands;
        }
    }

    public void refreshGrailsCommandsLater(final Runnable post) {
        EXECUTOR.submit(new Runnable() {

            public void run() {
                refreshGrailsCommands();
                if (post != null) {
                    post.run();
                }
            }
        });
    }

    public static final void showURL(URL url, boolean debug, GrailsProject project) {
        boolean debuggerAvailable = WebClientToolsSessionStarterService.isAvailable();

        if (!debug || !debuggerAvailable) {
            if (GrailsProjectConfig.forProject(project).getDisplayBrowser()) {
                HtmlBrowser.URLDisplayer.getDefault().showURL(url);
            }
        } else {
            FileObject webAppDir = project.getProjectDirectory().getFileObject(WEB_APP_DIR);
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

    private static class HelpLineProcessor implements LineProcessor {

        private static final Pattern COMMAND_PATTERN = Pattern.compile("grails\\s(.*)"); // NOI18N

        private static final Pattern EXCLUDE_PATTERN = Pattern.compile("Usage.*|Examples.*"); // NOI18N

        private List<String> commands = Collections.synchronizedList(new ArrayList<String>());

        private boolean excluded;

        public void processLine(String line) {
            Matcher matcher = COMMAND_PATTERN.matcher(line);
            if (matcher.matches()) {
                if (!excluded) {
                    commands.add(matcher.group(1));
                }
            } else {
                excluded = EXCLUDE_PATTERN.matcher(line).matches();
            }
        }

        public List<String> getCommands() {
            return commands;
        }

        public void close() {
        }

        public void reset() {
        }
    }

    private static class AnsiStrippingInputProcessorFactory implements InputProcessorFactory {

        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            return InputProcessors.ansiStripping(defaultProcessor);
        }
    }

    private static class ServerURLProcessor implements LineProcessor {

        private final GrailsProject project;
        private final boolean debug;

        private boolean running;

        public ServerURLProcessor(GrailsProject project, boolean debug) {
            this.project = project;
            this.debug = debug;
        }

        public void processLine(String line) {
            if (!running && line.contains("Browse to http://")) {
                running = true;

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

                GrailsCommandSupport.showURL(url, debug, project);
            }
        }

        public void reset() {
            // noop
        }

        public void close() {
            // noop
        }
    }

    private static class ProxyInputProcessorFactory implements InputProcessorFactory {

        private final List<InputProcessorFactory> factories = new ArrayList<InputProcessorFactory>();

        public ProxyInputProcessorFactory(InputProcessorFactory... proxied) {
            for (InputProcessorFactory factory : proxied) {
                if (factory != null) {
                    factories.add(factory);
                }
            }
        }

        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            InputProcessor[] processors = new InputProcessor[factories.size()];
            for (int i = 0; i < processors.length; i++) {
                processors[i] = factories.get(i).newInputProcessor(defaultProcessor);
            }
            return InputProcessors.proxy(processors);
        }
    }

    private class PluginListener implements FileChangeListener {

        public void fileAttributeChanged(FileAttributeEvent fe) {
            // noop
        }

        public void fileChanged(FileEvent fe) {
            changed();
        }

        public void fileDataCreated(FileEvent fe) {
            changed();
        }

        public void fileDeleted(FileEvent fe) {
            changed();
        }

        public void fileFolderCreated(FileEvent fe) {
            changed();
        }

        public void fileRenamed(FileRenameEvent fe) {
            changed();
        }

        private void changed() {
            // FIXME check only script changes

            synchronized (GrailsCommandSupport.this) {
                commands = null;
            }
        }
    }
}
