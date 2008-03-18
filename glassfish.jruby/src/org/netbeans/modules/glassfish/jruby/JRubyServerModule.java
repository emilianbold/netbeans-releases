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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.railsprojects.server.spi.RubyInstance;
import org.netbeans.spi.glassfish.GlassfishModule;
import org.netbeans.spi.glassfish.OperationStateListener;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;


/**
 *
 * @author Peter Williams
 */
public class JRubyServerModule implements RubyInstance {

    private Lookup lookup;
    
    JRubyServerModule(Lookup instanceLookup) {
        this.lookup = instanceLookup;
    }
    
    @Override
    public String toString() {
        return "GlassFish V3 / JRuby Support"; // NOI18N
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
                    platform.getHome().getAbsolutePath());
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
                    GlassfishModule.JRUBY_HOME, requestedPlatformDir);
            
            if(!requestedPlatformDir.equals(currentPlatformDir)) {
                // !PW XXX Server using different platform, fail until platform
                // restart query implemented.
                return failedOperation();
            }

            GlassfishModule.ServerState state = commonModule.getServerState();
            if(state == GlassfishModule.ServerState.STOPPED) {
                FutureTask<OperationState> task = new FutureTask<OperationState>(
                        new RunAppTask(commonModule, applicationName, applicationDir));
                RequestProcessor.getDefault().post(task);
                return task;
            } else if(state == GlassfishModule.ServerState.RUNNING) {
                return deploy(applicationName, applicationDir);
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
        private String step;
                
        public RunAppTask(final GlassfishModule module, final String appname, final File appdir) {
            commonModule = module;
            applicationName = appname;
            applicationDir = appdir;
            step = "start";
        }
        
        public OperationState call() throws Exception {
            final Future<GlassfishModule.OperationState> startFuture = 
                    commonModule.startServer(this);
            GlassfishModule.OperationState result = startFuture.get();
            if(result == GlassfishModule.OperationState.COMPLETED) {
                step = "deploy";
                final Future<GlassfishModule.OperationState> deployFuture = 
                        commonModule.deploy(this, applicationDir, applicationName, "/");
                result = deployFuture.get();
            }
            return translateOperationState(result);
        }

        public void operationStateChanged(final GlassfishModule.OperationState newState, final String message) {
            Logger.getLogger("glassfish-jruby").log(Level.FINEST, 
                    "runApplication/" + step + " V3/JRuby: " + newState + " - " + message);
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
            return wrapTask(commonModule.deploy(new OperationStateListener() {
                public void operationStateChanged(final GlassfishModule.OperationState newState, final String message) {
                    Logger.getLogger("glassfish-jruby").log(Level.FINEST, 
                            "deploy V3/JRuby: " + newState + " - " + message);
                }
            }, applicationDir, applicationName, "/"));
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
            }, applicationName));
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
//        return applicationName;
        return "";
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

}
