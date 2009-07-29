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

package org.netbeans.modules.glassfish.common;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.glassfish.common.nodes.actions.RefreshModulesCookie;
import org.netbeans.modules.glassfish.spi.AppDesc;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.GlassfishModule.OperationState;
import org.netbeans.modules.glassfish.spi.GlassfishModule.ServerState;
import org.netbeans.modules.glassfish.spi.OperationStateListener;
import org.netbeans.modules.glassfish.spi.Recognizer;
import org.netbeans.modules.glassfish.spi.RecognizerCookie;
import org.netbeans.modules.glassfish.spi.ResourceDesc;
import org.netbeans.modules.glassfish.spi.ServerCommand;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;


/**
 *
 * @author Peter Williams
 */
public class CommonServerSupport implements GlassfishModule, RefreshModulesCookie {

    private final transient Lookup lookup;
    private final Map<String, String> properties =
            Collections.synchronizedMap(new HashMap<String, String>(37));
    
    private volatile ServerState serverState = ServerState.STOPPED;
    private final Object stateMonitor = new Object();
    
    private ChangeSupport changeSupport = new ChangeSupport(this);
    
    private FileObject instanceFO;

    private volatile boolean startedByIde = false;
    private GlassfishInstanceProvider instanceProvider;
    
    CommonServerSupport(Lookup lookup, Map<String, String> ip, GlassfishInstanceProvider instanceProvider) {
        this.lookup = lookup;
        this.instanceProvider = instanceProvider;
        String hostName = updateString(ip, GlassfishModule.HOSTNAME_ATTR, GlassfishInstance.DEFAULT_HOST_NAME);
        String glassfishRoot = updateString(ip, GlassfishModule.GLASSFISH_FOLDER_ATTR, ""); // NOI18N
        int httpPort = updateInt(ip, GlassfishModule.HTTPPORT_ATTR, GlassfishInstance.DEFAULT_HTTP_PORT);
        updateString(ip, GlassfishModule.DISPLAY_NAME_ATTR, "Bogus display name"); // NOI18N GlassfishInstance.GLASSFISH_PRELUDE_SERVER_NAME);
        updateInt(ip, GlassfishModule.ADMINPORT_ATTR, GlassfishInstance.DEFAULT_ADMIN_PORT);
        
        updateString(ip,GlassfishModule.SESSION_PRESERVATION_FLAG,"true"); // NOI18N
        updateString(ip,GlassfishModule.START_DERBY_FLAG,
                ip.get(GlassfishModule.DOMAINS_FOLDER_ATTR) == null ? "false" : "true");  // NOI18N
        updateString(ip,GlassfishModule.USE_IDE_PROXY_FLAG,"true");  // NOI18N

        if(ip.get(GlassfishModule.URL_ATTR) == null) {
            String deployerUrl = instanceProvider.formatUri(glassfishRoot, hostName, httpPort);
            ip.put(URL_ATTR, deployerUrl);
        }

        ip.put(JVM_MODE, ip.get(GlassfishModule.DOMAINS_FOLDER_ATTR) == null ? DEBUG_MODE : NORMAL_MODE);
        properties.putAll(ip);
        
        // XXX username/password handling at some point.
        properties.put(USERNAME_ATTR, GlassfishInstance.DEFAULT_ADMIN_NAME);
        properties.put(PASSWORD_ATTR, GlassfishInstance.DEFAULT_ADMIN_PASSWORD);

        // !PW FIXME hopefully temporary patch for JavaONE 2008 to make it easier
        // to persist per-instance property changes made by the user.
        instanceFO = getInstanceFileObject();
        
        if(isRunning(hostName, httpPort)) {
            refresh();
        }
    }
    
//<<<<<<< local
//    private static String formatUri(String glassfishRoot, String host, int port, String uriFragment) {
//        return "[" + glassfishRoot + "]" + uriFragment + ":" + host + ":" + port;
//    }
//
//=======
//>>>>>>> other
    private static String updateString(Map<String, String> map, String key, String defaultValue) {
        String result = map.get(key);
        if(result == null) {
            map.put(key, defaultValue);
            result = defaultValue;
        }
        return result;
    }

    private static int updateInt(Map<String, String> map, String key, int defaultValue) {
        int result;
        String value = map.get(key);
        try {
            result = Integer.parseInt(value);
        } catch(NumberFormatException ex) {
            map.put(key, Integer.toString(defaultValue));
            result = defaultValue;
        }
        return result;
    }
    
    private FileObject getInstanceFileObject() {
        FileObject dir = FileUtil.getConfigFile(instanceProvider.getInstancesDirName());
        if(dir != null) {
            String instanceFN = properties.get(GlassfishInstanceProvider.INSTANCE_FO_ATTR);
            if(instanceFN != null) {
                return dir.getFileObject(instanceFN);
            }
        }
        return null;
    }
    
    public String getInstallRoot() {
        return properties.get(INSTALL_FOLDER_ATTR);
    }
    
    public String getGlassfishRoot() {
        return properties.get(GLASSFISH_FOLDER_ATTR);
    }
    
    public String getDisplayName() {
        return properties.get(DISPLAY_NAME_ATTR);
    }
    
    public String getDeployerUri() {
        return properties.get(URL_ATTR);
    }
    
    public String getUserName() {
        return properties.get(USERNAME_ATTR);
    }
    
    public String getPassword() {
        return properties.get(PASSWORD_ATTR);
    }
    
    public String getAdminPort() {
        return properties.get(ADMINPORT_ATTR);
    }
    
    public String getHttpPort() {
        return properties.get(HTTPPORT_ATTR);
    }
    
    public int getHttpPortNumber() {
        int httpPort = -1;
        try {
            httpPort = Integer.parseInt(properties.get(HTTPPORT_ATTR));
        } catch(NumberFormatException ex) {
            Logger.getLogger("glassfish").log(Level.WARNING, ex.getLocalizedMessage(), ex);  // NOI18N
        }
        return httpPort;
    }
    
    public String getHostName() {
        return properties.get(HOSTNAME_ATTR);
    }
    
    public String getDomainsRoot() {
        String retVal = properties.get(DOMAINS_FOLDER_ATTR);
//        if (null == retVal) {
//            retVal = properties.get(GLASSFISH_FOLDER_ATTR) + File.separator +
//                    GlassfishInstance.DEFAULT_DOMAINS_FOLDER; // NOI18N
//        }
        return retVal;
    }
    
    public String getDomainName() {
        String retVal = properties.get(DOMAIN_NAME_ATTR);
//        if (null == retVal) {
//            retVal = GlassfishInstance.DEFAULT_DOMAIN_NAME;
//        }
        return retVal;
    }
    
    public void setServerState(final ServerState newState) {
        // Synchronized on private monitor to serialize changes in state.
        // Storage of serverState is volatile to facilitate readability of
        // current state regardless of lock status.
        boolean fireChange = false;
        
        synchronized (stateMonitor) {
            if(serverState != newState) {
                serverState = newState;
                fireChange = true;
            }
        }
        
        if(fireChange) {
            changeSupport.fireChange();
        }
    }

    boolean isStartedByIde() {
        return startedByIde;
    }
    
    // ------------------------------------------------------------------------
    // GlassfishModule interface implementation
    // ------------------------------------------------------------------------
    public Map<String, String> getInstanceProperties() {
        return Collections.unmodifiableMap(properties);
    }
    
    public Future<OperationState> startServer(final OperationStateListener stateListener) {
        Logger.getLogger("glassfish").log(Level.FINEST, "CSS.startServer called on thread \"" + Thread.currentThread().getName() + "\""); // NOI18N
        OperationStateListener startServerListener = new StartOperationStateListener(GlassfishModule.ServerState.RUNNING);
        FutureTask<OperationState> task = new FutureTask<OperationState>(
                new StartTask(this, getRecognizers(), startServerListener, stateListener));
        RequestProcessor.getDefault().post(task);
        return task;
    }

    public Future<OperationState> startServer(final OperationStateListener stateListener, FileObject jdkRoot, String[] jvmArgs) {
        Logger.getLogger("glassfish").log(Level.FINEST, "CSS.startServer called on thread \"" + Thread.currentThread().getName() + "\""); // NOI18N
        OperationStateListener startServerListener = new StartOperationStateListener(GlassfishModule.ServerState.STOPPED_JVM_PROFILER);
        FutureTask<OperationState> task = new FutureTask<OperationState>(
                new StartTask(this, getRecognizers(), jdkRoot, jvmArgs, startServerListener, stateListener));
        RequestProcessor.getDefault().post(task);
        return task;
    }
    
    private List<Recognizer> getRecognizers() {
        List<Recognizer> recognizers;
        Collection<? extends RecognizerCookie> cookies = lookup.lookupAll(RecognizerCookie.class);
        if(!cookies.isEmpty()) {
            recognizers = new LinkedList<Recognizer>();
            for(RecognizerCookie cookie: cookies) {
                recognizers.addAll(cookie.getRecognizers());
            }
            recognizers = Collections.unmodifiableList(recognizers);
        } else {
            recognizers = Collections.emptyList();
        }
        return recognizers;
    }
    
    public Future<OperationState> stopServer(final OperationStateListener stateListener) {
        Logger.getLogger("glassfish").log(Level.FINEST, "CSS.stopServer called on thread \"" + Thread.currentThread().getName() + "\""); // NOI18N
        OperationStateListener stopServerListener = new OperationStateListener() {
            public void operationStateChanged(OperationState newState, String message) {
                if(newState == OperationState.RUNNING) {
                    setServerState(ServerState.STOPPING);
                } else if(newState == OperationState.COMPLETED) {
                    setServerState(ServerState.STOPPED);
                } else if(newState == OperationState.FAILED) {
                    setServerState(ServerState.RUNNING);
                }
            }
        };
        FutureTask<OperationState> task = new FutureTask<OperationState>(
                new StopTask(this, stopServerListener, stateListener));
        RequestProcessor.getDefault().post(task);
        return task;
    }

    public Future<OperationState> restartServer(OperationStateListener stateListener) {
        Logger.getLogger("glassfish").log(Level.FINEST, "CSS.restartServer called on thread \"" + Thread.currentThread().getName() + "\""); // NOI18N
        FutureTask<OperationState> task = new FutureTask<OperationState>(
                new RestartTask(this, stateListener));
        RequestProcessor.getDefault().post(task);
        return task;
    }
    
    public Future<OperationState> deploy(final OperationStateListener stateListener, 
            final File application, final String name) {
        return deploy(stateListener, application, name, null);
    }

    public Future<OperationState> deploy(final OperationStateListener stateListener, 
            final File application, final String name, final String contextRoot) {
        CommandRunner mgr = new CommandRunner(getInstanceProperties(), stateListener);
        return mgr.deploy(application, name, contextRoot);
    }
    
    public Future<OperationState> redeploy(final OperationStateListener stateListener, 
            final String name) {
        return redeploy(stateListener, name, null);
    }
        
    public Future<OperationState> redeploy(final OperationStateListener stateListener, 
            final String name, final String contextRoot) {
        CommandRunner mgr = new CommandRunner(getInstanceProperties(), stateListener);
        return mgr.redeploy(name, contextRoot);
    }

    public Future<OperationState> undeploy(final OperationStateListener stateListener, final String name) {
        CommandRunner mgr = new CommandRunner(getInstanceProperties(), stateListener);
        return mgr.undeploy(name);
    }
    
    public Future<OperationState> execute(ServerCommand command) {
        CommandRunner mgr = new CommandRunner(getInstanceProperties());
        return mgr.execute(command);
    }
    
    public AppDesc [] getModuleList(String container) {
        CommandRunner mgr = new CommandRunner(getInstanceProperties());
        int total = 0;
        Map<String, List<AppDesc>> appMap = mgr.getApplications(container);
        Collection<List<AppDesc>> appLists = appMap.values();
        for(List<AppDesc> appList: appLists) {
            total += appList.size();
        }
        AppDesc [] result = new AppDesc[total];
        int index = 0;
        for(List<AppDesc> appList: appLists) {
            for(AppDesc app: appList) {
                result[index++] = app;
            }
        }
        return result;
    }

    public Map<String, ResourceDesc> getResourcesMap(String type) {
        CommandRunner mgr = new CommandRunner(getInstanceProperties());
        Map<String, ResourceDesc> resourcesMap = new HashMap<String, ResourceDesc>();
        List<ResourceDesc> resourcesList = mgr.getResources(type);
        for (ResourceDesc resource : resourcesList) {
            resourcesMap.put(resource.getName(), resource);
        }
        return resourcesMap;
    }

    public ServerState getServerState() {
        return serverState;
    }
    
    public void addChangeListener(final ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(final ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }
    
    public String setEnvironmentProperty(final String name, final String value, 
            final boolean overwrite) {    
        String result = null;

        synchronized (properties) {
            result = properties.get(name);
            if(result == null || overwrite == true) {
                properties.put(name, value);
                setInstanceAttr(name, value);
                result = value;
            }
        }
        
        return result;
    }

    // ------------------------------------------------------------------------
    // bookkeeping & impl managment, not exposed via interface.
    // ------------------------------------------------------------------------
    void setProperty(final String key, final String value) {
        properties.put(key, value);
    }
    
    void getProperty(String key) {
        properties.get(key);
    }
    
    void setInstanceAttr(String name, String value) {
        if(instanceFO == null || !instanceFO.isValid()) {
            instanceFO = getInstanceFileObject();
        }
        if(instanceFO != null) {
            try {
                instanceFO.setAttribute(name, value);
            } catch(IOException ex) {
                Logger.getLogger("glassfish").log(Level.WARNING, "Unable to save attribute " + name + " for " + getDeployerUri(), ex); // NOI18N
            }
        } else {
            Logger.getLogger("glassfish").log(Level.WARNING, "Unable to save attribute " + name + " for " + getDeployerUri()); // NOI18N
        }
    }
    
    void setFileObject(FileObject fo) {
        instanceFO = fo;
    }

    public static boolean isRunning(final String host, final int port) {
        if(null == host)
            return false;
        
        try {
            InetSocketAddress isa = new InetSocketAddress(host, port);
            Socket socket = new Socket();
            socket.connect(isa, 100);
            socket.close();
            return true;
        } catch(IOException ex) {
            return false;
        }
    }
    
    public boolean isReallyRunning() {
        return isRunning(getHostName(), getHttpPortNumber()) && isReady(false);
    }

    public boolean isReady(boolean retry) {
        boolean isReady = false;
        int maxtries = retry ? 3 : 1;
        int tries = 0;

        while(!isReady && tries++ < maxtries) {
            long start = System.nanoTime();
            Commands.LocationCommand command = new Commands.LocationCommand();
            try {
                Future<OperationState> result = execute(command);
                if(result.get(30, TimeUnit.SECONDS) == OperationState.COMPLETED) {
                    long end = System.nanoTime();
                    Logger.getLogger("glassfish").log(Level.FINE, command.getCommand() + " responded in " + (end - start)/1000000 + "ms");  // NOI18N
                    String domainRoot = getDomainsRoot() + File.separator + getDomainName();
                    String targetDomainRoot = command.getDomainRoot();
                    if(getDomainsRoot() != null && targetDomainRoot != null) {
                        File installDir = FileUtil.normalizeFile(new File(domainRoot));
                        File targetInstallDir = FileUtil.normalizeFile(new File(targetDomainRoot));
                        isReady = installDir.equals(targetInstallDir);
                    } else {
                        // if we got a response from the server... we are going 
                        // to trust that it is the 'right one'
                        // TODO -- better edge case detection/protection
                        isReady = null != targetDomainRoot;
                    }
                    break;
                } else if(!command.retry()) {
                    // !PW temporary while some server versions support __locations
                    // and some do not but are still V3 and might the ones the user
                    // is using.
                    result = execute(new Commands.VersionCommand());
                    isReady = result.get(30, TimeUnit.SECONDS) == OperationState.COMPLETED;
                    break;
                } else {
                    long end = System.nanoTime();
                    Logger.getLogger("glassfish").log(Level.FINE, command.getCommand() + " timed out inside server after " + (end - start)/1000000 + "ms"); // NOI18N
                }
            } catch(Exception ex) {
                Logger.getLogger("glassfish").log(Level.FINE, command.getCommand() + " timed out.", ex); // NOI18N
                isReady = false;
                break;
            }
        }

        return isReady;
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
     * !PW XXX Is there a more efficient way to implement a successful future object?
     * 
     * @return Future object that represents an immediate successful operation
     */
    private static Future<OperationState> successfulOperation() {
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
                return OperationState.COMPLETED;
            }

            public OperationState get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return OperationState.COMPLETED;
            }
        };
    }

    // ------------------------------------------------------------------------
    //  RefreshModulesCookie implementation (for refreshing server state)
    // ------------------------------------------------------------------------
    private final AtomicBoolean refreshRunning = new AtomicBoolean(false);

    public void refresh() {
        // !PW FIXME we can do better here, but for now, make sure we only change
        // server state from stopped or running states -- leave stopping or starting
        // states alone.
        if(refreshRunning.compareAndSet(false, true)) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    // Can block for up to a few seconds...
                    boolean isRunning = isReallyRunning();
                    ServerState currentState = getServerState();
                    
                    if(currentState == ServerState.STOPPED && isRunning) {
                        setServerState(ServerState.RUNNING);
                    } else if(currentState == ServerState.RUNNING && !isRunning) {
                        setServerState(ServerState.STOPPED);
                    } else if(currentState == ServerState.STOPPED_JVM_PROFILER && isRunning) {
                        setServerState(ServerState.RUNNING);
                    }
                    
                    refreshRunning.set(false);
                }
            });
        }
    }

    public GlassfishInstanceProvider getInstanceProvider() {
        return instanceProvider;
    }


    class StartOperationStateListener implements OperationStateListener {
        private ServerState endState;

        StartOperationStateListener(ServerState endState) {
            this.endState = endState;
        }

        public void operationStateChanged(OperationState newState, String message) {
            if(newState == OperationState.RUNNING) {
                setServerState(ServerState.STARTING);
            } else if(newState == OperationState.COMPLETED) {
                startedByIde = true;
                setServerState(endState);
            } else if(newState == OperationState.FAILED) {
                setServerState(ServerState.STOPPED);
                // Open a warning dialog here...
                NotifyDescriptor nd = new NotifyDescriptor.Message(message);
                DialogDisplayer.getDefault().notifyLater(nd);
            }
        }
    }
}
