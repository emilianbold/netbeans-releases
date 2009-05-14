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

package org.netbeans.modules.glassfish.jruby;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.extexecution.print.LineConvertors;
import org.netbeans.api.extexecution.print.LineConvertors.FileLocator;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.glassfish.jruby.ui.JRubyServerCustomizer;
import org.netbeans.modules.glassfish.spi.Recognizer;
import org.netbeans.modules.ruby.railsprojects.server.spi.RubyInstance;
import org.netbeans.modules.glassfish.spi.CustomizerCookie;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.OperationStateListener;
import org.netbeans.modules.glassfish.spi.RecognizerCookie;
import org.netbeans.modules.glassfish.spi.ServerCommand;
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.netbeans.modules.ruby.platform.execution.DirectoryFileLocator;
import org.netbeans.modules.ruby.platform.execution.RubyLineConvertorFactory;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.windows.OutputListener;


/**
 *
 * @author Peter Williams
 */
public class JRubyServerModule implements RubyInstance, CustomizerCookie, RecognizerCookie {

    public static final String USE_ROOT_CONTEXT_ATTR = "jruby.useRootContext"; // NOI18N
    
    private Lookup lookup;
    
    JRubyServerModule(Lookup instanceLookup) {
        this.lookup = instanceLookup;
    }
    
    @Override
    public String toString() {
        return "GlassFish v3 Prelude / JRuby Support"; // NOI18N
    }
    
    // ------------------------------------------------------------------------
    // RubyInstance implementation
    // ------------------------------------------------------------------------
    public String getServerUri() {
        String serverUri = null;
        GlassfishModule commonModule = lookup.lookup(GlassfishModule.class);
        if(commonModule != null) {
            serverUri = commonModule.getInstanceProperties().get(GlassfishModule.URL_ATTR);
        } else {
            Logger.getLogger("glassfish-jruby").log(Level.INFO, 
                    "No V3 Common Server support found for V3/Ruby server instance");
        }
        return serverUri;
    }
    
    public String getDisplayName() {
        String displayName = null;
        GlassfishModule commonModule = lookup.lookup(GlassfishModule.class);
        if(commonModule != null) {
            displayName = commonModule.getInstanceProperties().get(GlassfishModule.DISPLAY_NAME_ATTR);
        } else {
            Logger.getLogger("glassfish-jruby").log(Level.INFO, 
                    "No V3 Common Server support found for V3/Ruby server instance");
        }
        return displayName;
    }

    public ServerState getServerState() {
        ServerState state = null;
        GlassfishModule commonModule = lookup.lookup(GlassfishModule.class);
        if(commonModule != null) {
            state = translateServerState(commonModule.getServerState());
        } else {
            Logger.getLogger("glassfish-jruby").log(Level.INFO, 
                    "No V3 Common Server support found for V3/Ruby server instance");
        }
        return state;
    }
    
    public Future<OperationState> startServer(final RubyPlatform platform) {
        GlassfishModule commonModule = lookup.lookup(GlassfishModule.class);
        if(commonModule != null) {
            // !PW XXX check for pre-existing different platform
            commonModule.setEnvironmentProperty(GlassfishModule.JRUBY_HOME, 
                    platform.getHome().getAbsolutePath(), false);
            return wrapTask(commonModule.startServer(new OperationStateListener() {
                public void operationStateChanged(final GlassfishModule.OperationState newState, final String message) {
                    Logger.getLogger("glassfish-jruby").log(Level.FINEST, 
                            "startServer V3/JRuby: " + newState + " - " + message);
                }
            }));
        } else {
            throw new IllegalStateException("No V3 Common Server support found for V3/Ruby server instance");
        }
                
    }
    
    public Future<OperationState> runApplication(final RubyPlatform platform, 
            final String applicationName, final File applicationDir) {
        GlassfishModule commonModule = lookup.lookup(GlassfishModule.class);
        if(commonModule != null) {
            // !PW XXX check for pre-existing different platform
            String requestedPlatformDir = platform.getHome().getAbsolutePath();
            String currentPlatformDir = commonModule.setEnvironmentProperty(
                    GlassfishModule.JRUBY_HOME, requestedPlatformDir, false);
            
            if(!requestedPlatformDir.equals(currentPlatformDir)) {
                // !PW XXX Server using different platform, fail until platform
                // restart query implemented.
                Logger.getLogger("glassfish-jruby").log(Level.WARNING, 
                        "Running project with current V3 Rails platform " + currentPlatformDir + 
                        " rather than requested platform " + requestedPlatformDir);
            }
            RubyPlatform.Info info = platform.getInfo();
            commonModule.setEnvironmentProperty(GlassfishModule.GEM_HOME, info.getGemHome(), false);
            commonModule.setEnvironmentProperty(GlassfishModule.GEM_PATH, info.getGemPath(), false);

            GlassfishModule.ServerState state = commonModule.getServerState();
            if(state == GlassfishModule.ServerState.STOPPED || 
                    state == GlassfishModule.ServerState.RUNNING) {
                FutureTask<OperationState> task = new FutureTask<OperationState>(
                        new RunAppTask(commonModule, applicationName, applicationDir, 
                                state == GlassfishModule.ServerState.STOPPED));
                RequestProcessor.getDefault().post(task);
                return task;
            } else {
                // !PW XXX server state indeterminate - starting or stopping, 
                // fail action until wait capability installed.
                return failedOperation();
            }
        } else {
            throw new IllegalStateException("No V3 Common Server support found for V3/Ruby server instance");
        }
    }

    private static class RunAppTask implements 
            Callable<OperationState>,
            OperationStateListener 
    {
        private final GlassfishModule commonModule;
        private final String applicationName;
        private final File applicationDir;
        private final boolean doStart;
        private String contextRoot;
        private String step;
                
        public RunAppTask(final GlassfishModule module, final String appname, final File appdir, boolean startRequired) {
            commonModule = module;
            applicationName = appname.replaceAll("[ \t]", "_");
            applicationDir = appdir;
            contextRoot = calculateContextRoot(module, appname);
            doStart = startRequired;
            step = "start";
        }
        
        public OperationState call() throws Exception {
            GlassfishModule.OperationState result = GlassfishModule.OperationState.COMPLETED;
            if(doStart) {
                final Future<GlassfishModule.OperationState> startFuture = 
                        commonModule.startServer(this);
                result = startFuture.get();
            }
            if(result == GlassfishModule.OperationState.COMPLETED) {
                if(isDeployed()) {
                    result = GlassfishModule.OperationState.COMPLETED;
                } else {
                    step = "deploy";
                    final Future<GlassfishModule.OperationState> deployFuture = 
                            commonModule.deploy(this, applicationDir, applicationName, contextRoot);
                    result = deployFuture.get();
                }
            }
            return translateOperationState(result);
        }

        public void operationStateChanged(final GlassfishModule.OperationState newState, final String message) {
            Logger.getLogger("glassfish-jruby").log(Level.FINEST, 
                    "runApplication/" + step + " V3/JRuby: " + newState + " - " + message);
        }

        private boolean isDeployed() {
            step = "checkdeployed";
            String propertyBase = "applications.application." + applicationName;
            ServerCommand.GetPropertyCommand getCmd = new ServerCommand.GetPropertyCommand(propertyBase);
            Future<GlassfishModule.OperationState> cmdOp = commonModule.execute(getCmd);
            try {
                GlassfishModule.OperationState result = cmdOp.get(15000, TimeUnit.MILLISECONDS);
                if(result != GlassfishModule.OperationState.COMPLETED) {
                    return false;
                }
                
                Map<String, String> properties = getCmd.getData();
                
                String name = properties.get(propertyBase + ".name");
                if(name == null || !name.equals(applicationName)) {
                    return false;
                }
                
                String location = properties.get(propertyBase + ".location");
                if(location == null || !location.startsWith("file:") || 
                        !match(applicationDir, location.substring(5))) {
                    return false;
                }
                
                String contextRootProperty = propertyBase + ".context-root";
                String deployedContextRoot = properties.get(contextRootProperty);
                if(deployedContextRoot == null) {
                    return false;
                } else if(!deployedContextRoot.equals(contextRoot)) {
                    ServerCommand.SetPropertyCommand setCmd = new ServerCommand.SetPropertyCommand(
                            contextRootProperty, contextRoot);
                    result = commonModule.execute(setCmd).get(15000, TimeUnit.MILLISECONDS);
                    if(result != GlassfishModule.OperationState.COMPLETED) {
                        return false;
                    }
                }
            } catch(Exception ex) {
                // Assume application is not deployed correctly.  Not expected.
                Logger.getLogger("glassfish-jruby").log(Level.FINE, ex.getLocalizedMessage(), ex);
                return false;
            }
            return true;
        }

        private boolean match(File dir, String path) {
            String dirpath = dir.getAbsolutePath().replaceAll("[ \t]", "%20");
            if(!dirpath.endsWith("/")) {
                dirpath = dirpath + "/";
            }
            if(!path.endsWith("/")) {
                path = path + "/";
            }
            return dirpath.equals(path);
        }
        
    }
    
    public Future<OperationState> stopServer() {
        GlassfishModule commonModule = lookup.lookup(GlassfishModule.class);
        if(commonModule != null) {
            return wrapTask(commonModule.stopServer(new OperationStateListener() {
                public void operationStateChanged(final GlassfishModule.OperationState newState, final String message) {
                    Logger.getLogger("glassfish-jruby").log(Level.FINEST, 
                            "stopServer V3/JRuby: " + newState + " - " + message);
                }
            }));
        } else {
            throw new IllegalStateException("No V3 Common Server support found for V3/Ruby server instance");
        }
    }

    public Future<OperationState> deploy(final String applicationName, final File applicationDir) {
        GlassfishModule commonModule = lookup.lookup(GlassfishModule.class);
        if(commonModule != null) {
            FutureTask<OperationState> task = new FutureTask<OperationState>(
                    new RunAppTask(commonModule, applicationName, applicationDir, false));
            RequestProcessor.getDefault().post(task);
            return task;
        } else {
            throw new IllegalStateException("No V3 Common Server support found for V3/Ruby server instance");
        }
    }

    public Future<OperationState> stop(final String applicationName) {
        GlassfishModule commonModule = lookup.lookup(GlassfishModule.class);
        if(commonModule != null) {
            return wrapTask(
                    commonModule.undeploy(new OperationStateListener() {
                public void operationStateChanged(final GlassfishModule.OperationState newState, final String message) {
                    Logger.getLogger("glassfish-jruby").log(Level.FINEST, 
                            "undeploy V3/JRuby: " + newState + " - " + message);
                }
            }, applicationName.replaceAll("[ \t]", "_")));
        } else {
            throw new IllegalStateException("No V3 Common Server support found for V3/Ruby server instance");
        }
    }

    public boolean isPlatformSupported(RubyPlatform platform) {
        // Only JRuby platforms (but all of them, regardless of version, for now)
        // are supported.
        return platform.isJRuby();
    }
    
    public void addChangeListener(ChangeListener listener) {
        GlassfishModule commonModule = lookup.lookup(GlassfishModule.class);
        if(commonModule != null) {
            commonModule.addChangeListener(listener);
        } else {
            Logger.getLogger("glassfish-jruby").log(Level.WARNING, 
                    "No V3 Common Server support found for V3/Ruby server instance");
        }
    }

    public void removeChangeListener(ChangeListener listener) {
        GlassfishModule commonModule = lookup.lookup(GlassfishModule.class);
        if(commonModule != null) {
            commonModule.removeChangeListener(listener);
        } else {
            Logger.getLogger("glassfish-jruby").log(Level.WARNING, 
                    "No V3 Common Server support found for V3/Ruby server instance");
        }
    }

    public String getContextRoot(String applicationName) {
        String result = applicationName;
        GlassfishModule commonModule = lookup.lookup(GlassfishModule.class);
        if(commonModule != null) {
            result = calculateContextRoot(commonModule, applicationName);
        }
        return result;
    }

    public int getRailsPort() {
        int httpPort = 8080; // defaults should probably be public in gfcommon spi
        GlassfishModule commonModule = lookup.lookup(GlassfishModule.class);
        if(commonModule != null) {
            try {
                String httpPortStr = commonModule.getInstanceProperties().get(GlassfishModule.HTTPPORT_ATTR);
                httpPort = Integer.parseInt(httpPortStr);
            } catch(NumberFormatException ex) {
                Logger.getLogger("glassfish-jruby").log(Level.WARNING, 
                        "Server's HTTP port value is not a valid integer.");
            }
        } else {
            Logger.getLogger("glassfish-jruby").log(Level.WARNING, 
                    "No V3 Common Server support found for V3/Ruby server instance");
        }
        return httpPort;
    }

    public String getServerCommand(final RubyPlatform platform, final String classpath, 
            final File applicationDir, final int httpPort, final boolean debug) {
        
        /**
         *  -cp
         *    ${server}/modules/grizzly-jruby-1.7.8-SNAPSHOT.jar:
         *    ${server}/modules/grizzly-module-1.8.2.jar:
         *    ${jruby.home}/lib/jruby.jar
         *  -Djruby.home=${jruby.home}
         *  -client
         *  ${grizzly.jruby.vm.params}
         *  -Xdebug
         *  -Xrunjdwp:transport=dt_socket,address=${grizzly.jruby.vm.debugport},server=y,suspend=n
         *  -Dglassfish.rdebug=${rdebug.path}
         *  -Dglassfish.rdebug.port=${rdebug.port}
         *  -Dglassfish.rdebug.version=${rdebug.version}
         *  -Dglassfish.rdebug.iosynch=${rdebug.iosynch}
         *  com.sun.grizzly.standalone.JRuby
         *    -p <Rails HTTP Port> -n 1 <Rails Application Folder>
         */
        StringBuilder builder = new StringBuilder(1000);

        // JVM classpath
        builder.append("-cp ");
        List<String> serverJars = getServerJars();
        for(String jarPath: serverJars) {
            builder.append(jarPath);
            builder.append(File.pathSeparatorChar);
        }

        String jrubyJar = platform.getHome().getAbsolutePath() +
                File.separatorChar + "lib" + File.separatorChar + "jruby.jar";
        builder.append(ServerUtilities.quote(jrubyJar));

        if(classpath != null && classpath.length() > 0) {
            String[] subpaths = classpath.split(File.pathSeparator);
            for(String subpath: subpaths) {
                builder.append(File.pathSeparatorChar);
                builder.append(ServerUtilities.quote(subpath));
            }
        }

        // JVM flags
        builder.append(" -client");

        // JVM properties
        builder.append(" -Djruby.home=");
        builder.append(ServerUtilities.quote(platform.getHome().getAbsolutePath()));
        builder.append(" -Djruby.runtime.max=1");

        String grizzlyVMParams = System.getProperty("grizzly.jruby.vm.params");
        if(grizzlyVMParams != null) {
            builder.append(' ');
            builder.append(grizzlyVMParams);
        }

        // Enables debugging of the Grizzly JVM, if this system property is set.
        Integer grizzlyVMDebugPort = Integer.getInteger("grizzly.jruby.vm.debugport");
        if(grizzlyVMDebugPort != null) {
            builder.append(" -Xdebug -Xrunjdwp:transport=dt_socket,address=" +
                     grizzlyVMDebugPort + ",server=y,suspend=y");
        }

        // Define properties to enable rdebug inside Grizzly/JRuby adapter if
        // debugging is enabled.
        if(debug) {
            builder.append(" -Djruby.reflection=true -Djruby.compile.mode=OFF");
            builder.append(
                    " -Dglassfish.rdebug=${rdebug.path}" +
                    " -Dglassfish.rdebug.port=${rdebug.port}" +
                    " -Dglassfish.rdebug.version=${rdebug.version}" +
                    " -Dglassfish.rdebug.iosynch=${rdebug.iosynch}");
            if("true".equals(System.getProperty("glassfish.rdebug.verbose"))) {
                builder.append(" -Dglassfish.rdebug.verbose=true");
            }
        }

        // Arguments to Grizzly/JRuby standalone server
        builder.append(" com.sun.grizzly.standalone.JRuby");
        builder.append(" -p ");
        builder.append(httpPort);
        builder.append(" -n 1 ");
        builder.append(ServerUtilities.quote(applicationDir.getAbsolutePath()));
        
        return builder.toString();
    }
    
    private List<String> getServerJars() {
        List<String> serverJars = new ArrayList<String>();
        GlassfishModule commonModule = lookup.lookup(GlassfishModule.class);
        if(commonModule != null) {
            String glassfishRoot = commonModule.getInstanceProperties().get(GlassfishModule.GLASSFISH_FOLDER_ATTR);
            File modulesDir = new File(glassfishRoot, "modules");

            // !PW FIXME cache results so we don't keep looking this up.
            //
            // !PW FIXME could have more robust jar searching.  We need to
            // locate the following jars:
            //   grizzly-jruby-<version>.jar        // grizzly jruby adapter
            //   grizzly-module-<version>.jar       // grizzly http connector
            //   grizzly-optionals-<version>.jar    // comet, etc.
            //
            File [] grizzlyJars = modulesDir.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    String name = pathname.getName();
                    return name.startsWith("grizzly") && name.endsWith(".jar") && !name.contains("jruby-module");
                }
            });

            if(grizzlyJars != null) {
                for(File jar: grizzlyJars) {
                    Logger.getLogger("glassfish-jruby").log(Level.FINER,
                            "Found jar for grizzly path: " + jar.getAbsolutePath());
                    serverJars.add(ServerUtilities.quote(jar.getAbsolutePath()));
                }
            } else {
                Logger.getLogger("glassfish-jruby").log(Level.WARNING,
                        "Problem accessing " + modulesDir.getAbsolutePath() +
                        " when searching for Grizzly Jars.");
            }
        } else {
            Logger.getLogger("glassfish-jruby").log(Level.WARNING, 
                    "No V3 Common Server support found for V3/Ruby server instance");
        }
        
        return serverJars;
    }

    private static String calculateContextRoot(GlassfishModule commonModule, String applicationName) {
        boolean useRootContext = Boolean.valueOf(
                commonModule.getInstanceProperties().get(USE_ROOT_CONTEXT_ATTR));
        return useRootContext ? "/" : "/" + applicationName.replaceAll("[ \t]", "_");
    }

    /**
     * !PW XXX Is there a more efficient way to implement a failed future object? 
     * 
     * @return Future object that represents an immediate failed operation
     */
    private static Future<OperationState> failedOperation() {
        return new Future<OperationState>() {

            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            public boolean isCancelled() {
                return false;
            }

            public boolean isDone() {
                return true;
            }

            public OperationState get() throws InterruptedException, ExecutionException {
                return OperationState.FAILED;
            }

            public OperationState get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return OperationState.FAILED;
            }
        };
    }
    
    /**
     * @param Future object from glassfish common operations
     * 
     * @return Future object using ruby module state constants.
     */
    private static Future<OperationState> wrapTask(
            final Future<GlassfishModule.OperationState> task) {
        return new Future<OperationState>() {

            public boolean cancel(boolean mayInterruptIfRunning) {
                return task.cancel(mayInterruptIfRunning);
            }

            public boolean isCancelled() {
                return task.isCancelled();
            }

            public boolean isDone() {
                return task.isDone();
            }

            public OperationState get() throws InterruptedException, ExecutionException {
                GlassfishModule.OperationState state = task.get();
                return translateOperationState(state);
            }

            public OperationState get(long timeout, TimeUnit unit) 
                    throws InterruptedException, ExecutionException, TimeoutException {
                GlassfishModule.OperationState state = task.get(timeout, unit);
                return translateOperationState(state);
            }
            
        };
    }

    private static ServerState translateServerState(GlassfishModule.ServerState state) {
        switch(state) {
            case STARTING:
                return ServerState.STARTING;
            case RUNNING:
            case RUNNING_JVM_DEBUG:
                return ServerState.RUNNING;
            case STOPPING:
                return ServerState.STOPPING;
            case STOPPED:
            case STOPPED_JVM_BP:
                return ServerState.STOPPED;
        }
        
        Logger.getLogger("glassfish-jruby").log(Level.INFO, "Invalid server state: " + state);
        return ServerState.STOPPED;
    }
    
    private static OperationState translateOperationState(GlassfishModule.OperationState state) {
        switch(state) {
            case RUNNING:
                return OperationState.RUNNING;
            case COMPLETED:
                return OperationState.COMPLETED;
            case FAILED:
                return OperationState.FAILED;
        }

        Logger.getLogger("glassfish-jruby").log(Level.INFO, "Invalid operation state: " + state);
        return OperationState.FAILED;
    }

    // ------------------------------------------------------------------------
    //  CustomizerCookie implementation
    // ------------------------------------------------------------------------
    public Collection<JPanel> getCustomizerPages() {
        Collection<JPanel> result = new LinkedList<JPanel>();
        result.add(new JRubyServerCustomizer(lookup.lookup(GlassfishModule.class)));
        return result;
    }

    // ------------------------------------------------------------------------
    // RecognizerCookie support
    // ------------------------------------------------------------------------

    private static final String PREFIX = "(?:\\s+|(?:[^:\\s\\d]{1,20}:\\s*))?"; // NOI18N
    private static final String DRIVE = "(?:\\S{1}:)"; // NOI18N
    private static final String FILE_CHAR = "[^\\s\\[\\]\\:\\\"]"; // NOI18N
    private static final String FILE = "(?:" + FILE_CHAR + "*)"; // NOI18N
    private static final String LINE = "([1-9][0-9]*)"; // NOI18N
    private static final String ROL = ".*\\s?"; // NOI18N
    private static final String FILE_SEP = "[\\\\/]"; // NOI18N
    private static final String LINE_SEP = "\\:"; // NOI18N

    // DRIVE?(FILE_SEP FILE)+ LINE_SEP LINE ROL
    private static final Pattern PATH_RECOGNIZER = Pattern.compile(
            PREFIX + DRIVE + "?(" + FILE_SEP + FILE + ")+" + LINE_SEP + LINE + ROL); // NOI18N

    public Collection<? extends Recognizer> getRecognizers() {
        FileLocator locator = new DirectoryFileLocator(FileUtil.toFileObject(FileUtil.normalizeFile(new File("/")))); //NOI18N
        LineConvertor convertor = LineConvertors.filePattern(locator, PATH_RECOGNIZER, RubyLineConvertorFactory.EXT_RE, 1, 2);
        return Collections.singleton(wrapRubyRecognizer(convertor));
    }

    private Recognizer wrapRubyRecognizer(final LineConvertor convertor) {
        return new Recognizer() {
            public OutputListener processLine(String text) {
                OutputListener result = null;
                List<ConvertedLine> match = convertor.convert(text);
                if (match != null && !match.isEmpty()) {
                    // relies on an implementation detail of FilePatternConvertor in that
                    // this assumes the returned listener to be an instance of FindFileListener
                    result = match.get(0).getListener();
                }
                return result;
            }
        };
    }
    
}
