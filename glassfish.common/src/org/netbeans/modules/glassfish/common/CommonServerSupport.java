/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.keyring.Keyring;
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
import org.netbeans.modules.glassfish.spi.ServerCommand.GetPropertyCommand;
import org.netbeans.modules.glassfish.spi.CommandFactory;
import org.netbeans.modules.glassfish.spi.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
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
    private transient boolean isRemote = false;
    private GlassfishInstanceProvider instanceProvider;
    // prevent j2eeserver from stopping an authenticated domain that
    // the IDE did not start.
    private boolean stopDisabled = false;
    
    CommonServerSupport(Lookup lookup, Map<String, String> ip, GlassfishInstanceProvider instanceProvider) {
        this.lookup = lookup;
        this.instanceProvider = instanceProvider;
        this.isRemote = ip.get(GlassfishModule.DOMAINS_FOLDER_ATTR) == null;
        String hostName = updateString(ip, GlassfishModule.HOSTNAME_ATTR, GlassfishInstance.DEFAULT_HOST_NAME);
        String glassfishRoot = updateString(ip, GlassfishModule.GLASSFISH_FOLDER_ATTR, ""); // NOI18N
        int httpPort = updateInt(ip, GlassfishModule.HTTPPORT_ATTR, GlassfishInstance.DEFAULT_HTTP_PORT);
        updateString(ip, GlassfishModule.DISPLAY_NAME_ATTR, "Bogus display name"); // NOI18N GlassfishInstance.GLASSFISH_PRELUDE_SERVER_NAME);
        int adminPort = updateInt(ip, GlassfishModule.ADMINPORT_ATTR, GlassfishInstance.DEFAULT_ADMIN_PORT);
        
        updateString(ip,GlassfishModule.SESSION_PRESERVATION_FLAG,"true"); // NOI18N
        updateString(ip,GlassfishModule.START_DERBY_FLAG, isRemote ? "false" : "true"); // NOI18N
        updateString(ip,GlassfishModule.USE_IDE_PROXY_FLAG, "true");  // NOI18N
        updateString(ip,GlassfishModule.DRIVER_DEPLOY_FLAG, "true");  // NOI18N

        if(ip.get(GlassfishModule.URL_ATTR) == null) {
            String deployerUrl = instanceProvider.formatUri(glassfishRoot, hostName, adminPort);
            ip.put(URL_ATTR, deployerUrl);
        }

        ip.put(JVM_MODE, isRemote ? DEBUG_MODE : NORMAL_MODE);
        properties.putAll(ip);
        
        // XXX username/password handling at some point.
        properties.put(USERNAME_ATTR, GlassfishInstance.DEFAULT_ADMIN_NAME);
        properties.put(PASSWORD_ATTR, GlassfishInstance.DEFAULT_ADMIN_PASSWORD);

        // !PW FIXME hopefully temporary patch for JavaONE 2008 to make it easier
        // to persist per-instance property changes made by the user.
        instanceFO = getInstanceFileObject();
        
            refresh();
    }
    
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
    
    @Override
    public String getPassword() {
        String retVal = properties.get(PASSWORD_ATTR);
        String key = properties.get(URL_ATTR);
        char[] retChars = Keyring.read(key);
        if (null == retChars || retChars.length < 1 || !GlassfishModule.PASSWORD_CONVERTED_FLAG.equals(retVal)) {
            retChars = retVal.toCharArray();
            Keyring.save(key, retChars, "a Glassfish/SJSAS passord");
            properties.put(PASSWORD_ATTR, GlassfishModule.PASSWORD_CONVERTED_FLAG) ;
        } else {
            retVal = String.copyValueOf(retChars);
        }
        return retVal;
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
    
    public int getAdminPortNumber() {
        int adminPort = -1;
        try {
            adminPort = Integer.parseInt(properties.get(ADMINPORT_ATTR));
        } catch(NumberFormatException ex) {
            Logger.getLogger("glassfish").log(Level.WARNING, ex.getLocalizedMessage(), ex);  // NOI18N
        }
        return adminPort;
    }

    public String getHostName() {
        return properties.get(HOSTNAME_ATTR);
    }
    
    public synchronized String getDomainsRoot() {
        String retVal = properties.get(DOMAINS_FOLDER_ATTR);
        File candidate = new File(retVal);
        if (candidate.exists() && !Utils.canWrite(candidate)) {
            // we need to do some surgury here...
            String foldername = FileUtil.findFreeFolderName(FileUtil.getConfigRoot(), "GF3");
            FileObject destdir = null;
            try {
                destdir = FileUtil.createFolder(FileUtil.getConfigRoot(),foldername);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (null != destdir) {
                candidate = new File(candidate, properties.get(DOMAIN_NAME_ATTR));
                FileObject source = FileUtil.toFileObject(FileUtil.normalizeFile(candidate));
                try {
                    Utils.doCopy(source, destdir);

                    retVal = FileUtil.toFile(destdir).getAbsolutePath();
                    properties.put(DOMAINS_FOLDER_ATTR,retVal);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return retVal;
    }
    
    public String getDomainName() {
        String retVal = properties.get(DOMAIN_NAME_ATTR);
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
    @Override
    public Map<String, String> getInstanceProperties() {
        // force the domains conversion
        getDomainsRoot();
        return Collections.unmodifiableMap(properties);
    }
    
    @Override
    public GlassfishInstanceProvider getInstanceProvider() {
        return instanceProvider;
    }

    @Override
    public boolean isRemote() {
        return isRemote;
    }

    @Override
    public Future<OperationState> startServer(final OperationStateListener stateListener) {
        Logger.getLogger("glassfish").log(Level.FINEST, "CSS.startServer called on thread \"" + Thread.currentThread().getName() + "\""); // NOI18N
        OperationStateListener startServerListener = new StartOperationStateListener(GlassfishModule.ServerState.RUNNING);
        FutureTask<OperationState> task = new FutureTask<OperationState>(
                new StartTask(this, getRecognizers(), startServerListener, stateListener));
        RequestProcessor.getDefault().post(task);
        return task;
    }

    @Override
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
    
    @Override
    public Future<OperationState> stopServer(final OperationStateListener stateListener) {
        Logger.getLogger("glassfish").log(Level.FINEST, "CSS.stopServer called on thread \"" + Thread.currentThread().getName() + "\""); // NOI18N
        OperationStateListener stopServerListener = new OperationStateListener() {
            @Override
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
        // prevent j2eeserver from stopping a server it did not start.
        if (stopDisabled) {
            stopServerListener.operationStateChanged(OperationState.COMPLETED, "");
            if (null != stateListener) {
                stateListener.operationStateChanged(OperationState.COMPLETED, "");
            }
            return task;
        }
        RequestProcessor.getDefault().post(task);
        return task;
    }

    @Override
    public Future<OperationState> restartServer(OperationStateListener stateListener) {
        Logger.getLogger("glassfish").log(Level.FINEST, "CSS.restartServer called on thread \"" + Thread.currentThread().getName() + "\""); // NOI18N
        FutureTask<OperationState> task = new FutureTask<OperationState>(
                new RestartTask(this, stateListener));
        RequestProcessor.getDefault().post(task);
        return task;
    }
    
    @Override
    public Future<OperationState> deploy(final OperationStateListener stateListener, 
            final File application, final String name) {
        return deploy(stateListener, application, name, null);
    }

    @Override
    public Future<OperationState> deploy(final OperationStateListener stateListener, 
            final File application, final String name, final String contextRoot) {
        return deploy(stateListener, application, name, contextRoot, null);
    }

    @Override
    public Future<OperationState> deploy(final OperationStateListener stateListener,
            final File application, final String name, final String contextRoot, Map<String,String> properties) {
        CommandRunner mgr = new CommandRunner(isReallyRunning(), getCommandFactory(), getInstanceProperties(), stateListener);
        
        return mgr.deploy(application, name, contextRoot, properties);
    }
    
    @Override
    public Future<OperationState> redeploy(final OperationStateListener stateListener, 
            final String name) {
        return redeploy(stateListener, name, null);
    }
        
    @Override
    public Future<OperationState> redeploy(final OperationStateListener stateListener, 
            final String name, final String contextRoot) {
        CommandRunner mgr = new CommandRunner(isReallyRunning(), getCommandFactory(), getInstanceProperties(), stateListener);
        return mgr.redeploy(name, contextRoot);
    }

    @Override
    public Future<OperationState> undeploy(final OperationStateListener stateListener, final String name) {
        CommandRunner mgr = new CommandRunner(isReallyRunning(), getCommandFactory(), getInstanceProperties(), stateListener);
        return mgr.undeploy(name);
    }
    
    @Override
    public Future<OperationState> execute(ServerCommand command) {
        CommandRunner mgr = new CommandRunner(isReallyRunning(), getCommandFactory(), getInstanceProperties());
        return mgr.execute(command);
    }
    
    private Future<OperationState> execute(boolean irr, ServerCommand command) {
        CommandRunner mgr = new CommandRunner(irr, getCommandFactory(), getInstanceProperties());
        return mgr.execute(command);
    }
    @Override
    public AppDesc [] getModuleList(String container) {
        CommandRunner mgr = new CommandRunner(isReallyRunning(),getCommandFactory(), getInstanceProperties());
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

    @Override
    public Map<String, ResourceDesc> getResourcesMap(String type) {
        CommandRunner mgr = new CommandRunner(isReallyRunning(),getCommandFactory(), getInstanceProperties());
        Map<String, ResourceDesc> resourcesMap = new HashMap<String, ResourceDesc>();
        List<ResourceDesc> resourcesList = mgr.getResources(type);
        for (ResourceDesc resource : resourcesList) {
            resourcesMap.put(resource.getName(), resource);
        }
        return resourcesMap;
    }

    @Override
    public ServerState getServerState() {
        return serverState;
    }
    
    @Override
    public void addChangeListener(final ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(final ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }
    
    @Override
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
        return isRunning(getHostName(), getAdminPortNumber()) && isReady(false,30,TimeUnit.SECONDS);
    }

    public boolean isReady(boolean retry, int timeout, TimeUnit units) {
        boolean isReady = false;
        int maxtries = retry ? 3 : 1;
        int tries = 0;

        while(!isReady && tries++ < maxtries) {
            if (tries > 1) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                     Logger.getLogger("glassfish").log(Level.INFO, null,ex);
                }
            }
            long start = System.nanoTime();
            Commands.LocationCommand command = new Commands.LocationCommand();
            try {
                Future<OperationState> result = execute(true,command);
                if(result.get(timeout, units) == OperationState.COMPLETED) {
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
                        if (isReady) {
                            // make sure the http port info is corrected
                            updateHttpPort();
                        }
                    }
                    break;
                } else if(!command.retry()) {
                    // !PW temporary while some server versions support __locations
                    // and some do not but are still V3 and might the ones the user
                    // is using.
                    result = execute(true, new Commands.VersionCommand());
                    isReady = result.get(timeout, units) == OperationState.COMPLETED;
                    break;
                } else {
                    long end = System.nanoTime();
                    Logger.getLogger("glassfish").log(Level.FINE, command.getCommand() + " timed out inside server after " + (end - start)/1000000 + "ms"); // NOI18N
                }
            } catch(Exception ex) {
                Logger.getLogger("glassfish").log(Level.INFO, command.getCommand() + " timed out.", ex); // NOI18N
                isReady = false;
                break;
            }
        }

        return isReady;
    }

    // ------------------------------------------------------------------------
    //  RefreshModulesCookie implementation (for refreshing server state)
    // ------------------------------------------------------------------------
    private final AtomicBoolean refreshRunning = new AtomicBoolean(false);

    @Override
    public void refresh() {
        refresh(null,null);
    }

    @Override
    public void refresh(String expected, String unexpected) {
        // !PW FIXME we can do better here, but for now, make sure we only change
        // server state from stopped or running states -- leave stopping or starting
        // states alone.
        if(refreshRunning.compareAndSet(false, true)) {
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
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

    void disableStop() {
        stopDisabled = true;
    }

    @Override
    public CommandFactory getCommandFactory() {
        return instanceProvider.getCommandFactory();
    }

    class StartOperationStateListener implements OperationStateListener {
        private ServerState endState;

        StartOperationStateListener(ServerState endState) {
            this.endState = endState;
        }

        @Override
        public void operationStateChanged(OperationState newState, String message) {
            if(newState == OperationState.RUNNING) {
                setServerState(ServerState.STARTING);
            } else if(newState == OperationState.COMPLETED) {
                startedByIde = isReady(false,300,TimeUnit.MILLISECONDS);
                setServerState(endState);
            } else if(newState == OperationState.FAILED) {
                setServerState(ServerState.STOPPED);
                // Open a warning dialog here...
                NotifyDescriptor nd = new NotifyDescriptor.Message(message);
                DialogDisplayer.getDefault().notifyLater(nd);
            }
        }
    }

    private void updateHttpPort() {
        GetPropertyCommand gpc = new GetPropertyCommand("*.http-listener-1.port"); // NOI18N
        Future<OperationState> result2 = execute(true, gpc);
        try {
            if (result2.get(10, TimeUnit.SECONDS) == OperationState.COMPLETED) {
                Map<String, String> retVal = gpc.getData();
                for (Entry<String, String> entry : retVal.entrySet()) {
                    String val = entry.getValue();
                    if (null != val && val.trim().length() > 0) {
                        setEnvironmentProperty(GlassfishModule.HTTPPORT_ATTR, val, true);
                    }
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, null, ex); // NOI18N
        } catch (ExecutionException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, null, ex); // NOI18N
        } catch (TimeoutException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, null, ex); // NOI18N
        }

    }
}
