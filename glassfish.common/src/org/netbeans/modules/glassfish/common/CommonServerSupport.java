/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.glassfish.common;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
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
import org.netbeans.modules.glassfish.spi.GlassfishModule3;
import org.netbeans.modules.glassfish.spi.Utils;
import org.netbeans.modules.glassfish.spi.VMIntrospector;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.*;
import org.openide.util.lookup.Lookups;


/**
 *
 * @author Peter Williams
 */
public class CommonServerSupport implements GlassfishModule3, RefreshModulesCookie {

    private final transient Lookup lookup;
    private final Map<String, String> properties =
            Collections.synchronizedMap(new HashMap<String, String>(37));

    private volatile ServerState serverState = ServerState.UNKNOWN;
    private final Object stateMonitor = new Object();

    private ChangeSupport changeSupport = new ChangeSupport(this);

    private FileObject instanceFO;

    private volatile boolean startedByIde = false;
    private transient boolean isRemote = false;
    private GlassfishInstanceProvider instanceProvider;
    // prevent j2eeserver from stopping an authenticated domain that
    // the IDE did not start.
    private boolean stopDisabled = false;
    
    private Process localStartProcess;

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
        updateString(ip,GlassfishModule.HTTPHOST_ATTR, "localhost"); // NOI18N
        String deployerUri = ip.get(GlassfishModule.URL_ATTR);

        // Asume a local instance is in NORMAL_MODE
        // Assume remote Prelude and 3.0 instances are in DEBUG (we cannot change them)
        // Assume a remote 3.1 instance is in NORMAL_MODE... we can restart it into debug mode
        ip.put(JVM_MODE, isRemote && !deployerUri.contains("deployer:gfv3ee6wc") ? DEBUG_MODE : NORMAL_MODE);
        properties.putAll(ip);

        // XXX username/password handling at some point.
        properties.put(USERNAME_ATTR, GlassfishInstance.DEFAULT_ADMIN_NAME);
        properties.put(PASSWORD_ATTR, GlassfishInstance.DEFAULT_ADMIN_PASSWORD);

        // !PW FIXME hopefully temporary patch for JavaONE 2008 to make it easier
        // to persist per-instance property changes made by the user.
        instanceFO = getInstanceFileObject();
        if (!isRemote) {
            refresh();
        }
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
            if (null != key) {
                Keyring.save(key, retChars, "a Glassfish/SJSAS passord");
                properties.put(PASSWORD_ATTR, GlassfishModule.PASSWORD_CONVERTED_FLAG) ;
            }
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
        if (null == retVal) {
            return null;
        }
        File candidate = new File(retVal);
        if (candidate.exists() && !Utils.canWrite(candidate)) {
            // we need to do some surgury here...
            String foldername = FileUtil.findFreeFolderName(FileUtil.getConfigRoot(), "GF3");
            FileObject destdir = null;
            try {
                destdir = FileUtil.createFolder(FileUtil.getConfigRoot(),foldername);
            } catch (IOException ex) {
                Logger.getLogger("glassfish").log(Level.INFO,"could not create a writable domain dir",ex); // NOI18N
            }
            if (null != destdir) {
                candidate = new File(candidate, properties.get(DOMAIN_NAME_ATTR));
                FileObject source = FileUtil.toFileObject(FileUtil.normalizeFile(candidate));
                try {
                    Utils.doCopy(source, destdir);

                    retVal = FileUtil.toFile(destdir).getAbsolutePath();
                    properties.put(DOMAINS_FOLDER_ATTR,retVal);
                } catch (IOException ex) {
                    // need to try again... since the domain is probably unreadable.
                    foldername = FileUtil.findFreeFolderName(FileUtil.getConfigRoot(), "GF3"); // NOI18N
                    destdir = null;
                    try {
                        destdir = FileUtil.createFolder(FileUtil.getConfigRoot(), foldername);
                    } catch (IOException ioe) {
                        Logger.getLogger("glassfish").log(Level.INFO,"could not create a writable second domain dir",ioe); // NOI18N
                        return retVal;
                    }
                    File destdirFile = FileUtil.toFile(destdir);
                    properties.put(DOMAINS_FOLDER_ATTR, destdirFile.getAbsolutePath());
                    retVal = destdirFile.getAbsolutePath();
                    // getEe6() eventually creates a call to getDomainsRoot()... which can lead to a deadlock
                    //  forcing the call to happen after getDomainsRoot returns will 
                    // prevent the deadlock.
                    RequestProcessor.getDefault().post(new Runnable() {

                        @Override
                        public void run() {
                            CreateDomain cd = new CreateDomain("anonymous", "", // NOI18N
                                    new File(properties.get(GlassfishModule.GLASSFISH_FOLDER_ATTR)),
                                    properties, GlassfishInstanceProvider.getEe6(), false, true, "INSTALL_ROOT_KEY"); // NOI18N
                            cd.start();
                        }
                    }, 100);
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

    private static final RequestProcessor RP = new RequestProcessor("CommonServerSupport - start/stop/refresh",5); // NOI18N

    @Override
    public Future<OperationState> startServer(final OperationStateListener stateListener, ServerState endState) {
        Logger.getLogger("glassfish").log(Level.FINEST, "CSS.startServer called on thread \"{0}\"", Thread.currentThread().getName()); // NOI18N
        OperationStateListener startServerListener = new StartOperationStateListener(endState);
        VMIntrospector vmi = Lookups.forPath(Util.GF_LOOKUP_PATH).lookup(VMIntrospector.class);
        FutureTask<OperationState> task = new FutureTask<OperationState>(
                new StartTask(this, getRecognizers(), vmi,
                              (FileObject)null,
                              (String[])(endState == ServerState.STOPPED_JVM_PROFILER ? new String[]{""} : null),
                              startServerListener, stateListener));
        RP.post(task);
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
        Logger.getLogger("glassfish").log(Level.FINEST, "CSS.stopServer called on thread \"{0}\"", Thread.currentThread().getName()); // NOI18N
        OperationStateListener stopServerListener = new OperationStateListener() {
            @Override
            public void operationStateChanged(OperationState newState, String message) {
                if(newState == OperationState.RUNNING) {
                    setServerState(ServerState.STOPPING);
                } else if(newState == OperationState.COMPLETED) {
                    setServerState(ServerState.STOPPED);
                } else if(newState == OperationState.FAILED) {
                    // possible bug - what if server was started in other mode than RUNNING
                    setServerState(ServerState.RUNNING);
                }
            }
        };
        FutureTask<OperationState> task = null;
        if (!isRemote() || !Util.isDefaultOrServerTarget(properties)) {
            if (getServerState() == ServerState.STOPPED_JVM_PROFILER) {
                task = new FutureTask<OperationState>(new StopProfilingTask(this, stateListener));
            } else {
                task = new FutureTask<OperationState>(
                    new StopTask(this, stopServerListener, stateListener));
            }
        // prevent j2eeserver from stopping a server it did not start.
        } else {
            task = new FutureTask<OperationState>(new NoopTask(this,stopServerListener,stateListener));
        }
        if (stopDisabled) {
            stopServerListener.operationStateChanged(OperationState.COMPLETED, "");
            if (null != stateListener) {
                stateListener.operationStateChanged(OperationState.COMPLETED, "");
            }
            return task;
        }
        RP.post(task);
        return task;
    }

    
    @Override
    public Future<OperationState> restartServer(OperationStateListener stateListener) {
        Logger.getLogger("glassfish").log(Level.FINEST, "CSS.restartServer called on thread \"{0}\"", Thread.currentThread().getName()); // NOI18N
        FutureTask<OperationState> task = new FutureTask<OperationState>(
                new RestartTask(this, stateListener));
        RP.post(task);
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
        return deploy(stateListener, application, name, contextRoot, null, new File[0]);
    }

    @Override
    public Future<OperationState> deploy(OperationStateListener stateListener, File application, String name, String contextRoot, Map<String, String> properties, File[] libraries) {
        CommandRunner mgr = new CommandRunner(isReallyRunning(), getCommandFactory(), getInstanceProperties(), stateListener);

        return mgr.deploy(application, name, contextRoot, properties, libraries);
    }

    @Override
    public Future<OperationState> redeploy(final OperationStateListener stateListener,
            final String name, boolean resourcesChanged) {
        return redeploy(stateListener, name, null, resourcesChanged);
    }

    @Override
    public Future<OperationState> redeploy(final OperationStateListener stateListener,
            final String name, final String contextRoot, boolean resourcesChanged) {
        return redeploy(stateListener, name, contextRoot, new File[0], resourcesChanged);
    }

    @Override
    public Future<OperationState> redeploy(OperationStateListener stateListener, String name, String contextRoot, File[] libraries, boolean resourcesChanged) {
        CommandRunner mgr = new CommandRunner(isReallyRunning(), getCommandFactory(), getInstanceProperties(), stateListener);
        return mgr.redeploy(name, contextRoot, libraries, resourcesChanged);
    }

    @Override
    public Future<OperationState> undeploy(final OperationStateListener stateListener, final String name) {
        CommandRunner mgr = new CommandRunner(isReallyRunning(), getCommandFactory(), getInstanceProperties(), stateListener);
        return mgr.undeploy(name);
    }

    @Override
    public Future<OperationState> enable(final OperationStateListener stateListener, final String name) {
        CommandRunner mgr = new CommandRunner(isReallyRunning(), getCommandFactory(), getInstanceProperties(), stateListener);
        return mgr.enable(name);
    }
    @Override
    public Future<OperationState> disable(final OperationStateListener stateListener, final String name) {
        CommandRunner mgr = new CommandRunner(isReallyRunning(), getCommandFactory(), getInstanceProperties(), stateListener);
        return mgr.disable(name);
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
    private Future<OperationState> execute(boolean irr, ServerCommand command, OperationStateListener... osl) {
        CommandRunner mgr = new CommandRunner(irr, getCommandFactory(), getInstanceProperties(), osl);
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
        if (serverState == ServerState.UNKNOWN) {
            refresh();
        }
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

    boolean setInstanceAttr(String name, String value) {
        boolean retVal = false;
        if(instanceFO == null || !instanceFO.isValid()) {
            instanceFO = getInstanceFileObject();
        }
        if(instanceFO != null && instanceFO.canWrite()) {
            try {
                Object currentValue = instanceFO.getAttribute(name);
                if (null != currentValue && currentValue.equals(value)) {
                    // do nothing
                } else {
                    instanceFO.setAttribute(name, value);
                }
                retVal = true;
            } catch(IOException ex) {
                Logger.getLogger("glassfish").log(Level.WARNING,
                        "Unable to save attribute " + name + " in " + instanceFO.getPath() + " for " + getDeployerUri(), ex); // NOI18N
            }
        } else {
            if (null == instanceFO)
                Logger.getLogger("glassfish").log(Level.WARNING,
                        "Unable to save attribute {0} for {1} in {3}. Instance file is writable? {2}",
                        new Object[]{name, getDeployerUri(), false, "null"}); // NOI18N
            else
                Logger.getLogger("glassfish").log(Level.WARNING,
                        "Unable to save attribute {0} for {1} in {3}. Instance file is writable? {2}",
                        new Object[]{name, getDeployerUri(), instanceFO.canWrite(), instanceFO.getPath()}); // NOI18N
        }
        return retVal;
    }

    void setFileObject(FileObject fo) {
        instanceFO = fo;
    }

    public static boolean isRunning(final String host, final int port, String name) {
        if(null == host)
            return false;

        try {
            InetSocketAddress isa = new InetSocketAddress(host, port);
            Socket socket = new Socket();
            int timeout = 2000;
            if ("localhost".equals(host) || "127.0.0.1".equals(host)) {
                timeout = 2000;
            }
            Logger.getLogger("glassfish-socket-connect-diagnostic").log(Level.FINE, "Using socket.connect", new Exception());
            socket.connect(isa, timeout);
            socket.setSoTimeout(timeout);
            try { socket.close(); } catch (IOException ioe) {
                Logger.getLogger("glassfish").log(Level.INFO, "closing after test", ioe);
            }
            return true;
        } catch (java.net.ConnectException ex) {
            return false;
        } catch (java.net.SocketTimeoutException ste) {
            return false;
        } catch (IOException ioe) {
            String message = null;
            if (name == null || "".equals(name.trim())) {
                message = NbBundle.getMessage(CommonServerSupport.class,
                        "MSG_FLAKEY_NETWORK", host, ""+port, ioe.getLocalizedMessage()); // NOI18N
            } else {
                message = NbBundle.getMessage(CommonServerSupport.class,
                        "MSG_FLAKEY_NETWORK2", host, ""+port, ioe.getLocalizedMessage(), name); // NOI18N
            }
            NotifyDescriptor nd = new NotifyDescriptor.Message(message);
            DialogDisplayer.getDefault().notifyLater(nd);
            Logger.getLogger("glassfish").log(Level.INFO, "evidence of network flakiness", ioe); // NOI18N
            return false;
        }
    }

    public boolean isReallyRunning() {
        return //isRunning(getHostName(), getAdminPortNumber(), properties.get(DISPLAY_NAME_ATTR)) &&
                isReady(false,30,TimeUnit.SECONDS);
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
                Future<OperationState> result = null;

                if (isRemote) {
                    final CommonServerSupport css = this;
                    result = execute(true, command, new OperationStateListener() {
                        @Override
                        public void operationStateChanged(OperationState newState, String message) {
                            synchronized (css) {
                                long lastDisplayed = css.getLatestWarningDisplayTime();
                                long currentTime = System.currentTimeMillis();
                                if (OperationState.FAILED == newState && !"".equals(message)
                                        && currentTime - lastDisplayed > 5000) { // NOI18N
                                    NotifyDescriptor nd = new NotifyDescriptor.Message(message);
                                    DialogDisplayer.getDefault().notifyLater(nd);
                                    css.setLatestWarningDisplayTime(currentTime);
                                    Logger.getLogger("glassfish").log(Level.INFO, message); // NOI18N
                                }
                            }
                        }
                    });
                } else {
                    result = execute(true, command);
                }
                if(result.get(timeout, units) == OperationState.COMPLETED) {
                    long end = System.nanoTime();
                    Logger.getLogger("glassfish").log(Level.FINE, "{0} responded in {1}ms", new Object[]{command.getCommand(), (end - start) / 1000000});  // NOI18N
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
                    if (isReady) {
                        // make sure the http port info is corrected
                        updateHttpPort();
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
                    // keep trying for 10 minutes if the server is stuck between
                    // httpLive and server ready state. We have to give up sometime, though.
                    VMIntrospector vmi = Lookups.forPath(Util.GF_LOOKUP_PATH).lookup(VMIntrospector.class);
                    boolean suspended = null == vmi ? false : vmi.isSuspended(getHostName(), (String) properties.get(GlassfishModule.DEBUG_PORT));
                    if (suspended) {
                        tries--;
                    } else if (maxtries < 20) {
                        maxtries++;
                    }
                    long end = System.nanoTime();
                    Logger.getLogger("glassfish").log(Level.INFO, "{0} returned from server after {1}ms. The server is still getting ready", new Object[]{command.getCommand(), (end - start) / 1000000}); // NOI18N
                }
            } catch(TimeoutException ex) {
                Logger.getLogger("glassfish").log(Level.INFO, command.getCommand() + " timed out. "+tries+" of "+maxtries, ex); // NOI18N
                isReady = false;
            } catch (Exception ex) {
                Logger.getLogger("glassfish").log(Level.INFO, command.getCommand() + " failed at  "+tries+" of "+maxtries, ex); // NOI18N
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
            RP.post(new Runnable() {
                @Override
                public void run() {
                    // Can block for up to a few seconds...
                    boolean isRunning = isReallyRunning();
                    if (isRunning && !Util.isDefaultOrServerTarget(properties)) {
                        isRunning = pingHttp(1);
                    }
                    ServerState currentState = getServerState();
                    
                    if((currentState == ServerState.STOPPED || currentState == ServerState.UNKNOWN) && isRunning) {
                        setServerState(ServerState.RUNNING);
                    } else if((currentState == ServerState.RUNNING || currentState == ServerState.UNKNOWN) && !isRunning) {
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
    
    void setLocalStartProcess(Process process) {
        this.localStartProcess = process;
    }
    
    Process getLocalStartProcess() {
        return localStartProcess;
    }
    
    void stopLocalStartProcess() {
        localStartProcess.destroy();
        localStartProcess = null;
    }

    @Override
    public CommandFactory getCommandFactory() {
        return instanceProvider.getCommandFactory();
    }

    @Override
    public String getResourcesXmlName() {
        return Utils.useGlassfishPrefix(getDeployerUri()) ?
                "glassfish-resources" : "sun-resources"; // NOI18N
    }

    @Override
    public boolean supportsRestartInDebug() {
        return getDeployerUri().contains(GlassfishInstanceProvider.EE6WC_DEPLOYER_FRAGMENT);
    }

    @Override
    public boolean isRestfulLogAccessSupported() {
        return getDeployerUri().contains(GlassfishInstanceProvider.EE6WC_DEPLOYER_FRAGMENT);
    }

    @Override
    public boolean isWritable() {
        return (null == instanceFO) ? false : instanceFO.canWrite();
    }

    private long latestWarningDisplayTime = System.currentTimeMillis();
    
    private long getLatestWarningDisplayTime() {
        return latestWarningDisplayTime;
    }

    private void setLatestWarningDisplayTime(long currentTime) {
        latestWarningDisplayTime = currentTime;
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
                startedByIde = isRemote ? false : isReady(false,300,TimeUnit.MILLISECONDS);
                setServerState(endState);
            } else if(newState == OperationState.FAILED) {
                setServerState(ServerState.STOPPED);
                // Open a warning dialog here...
                NotifyDescriptor nd = new NotifyDescriptor.Message(message);
                DialogDisplayer.getDefault().notifyLater(nd);
            }
        }
    }

    void updateHttpPort() {
        String target = Util.computeTarget(properties);
        GetPropertyCommand gpc = null;
        if (Util.isDefaultOrServerTarget(properties)) {
            gpc = new GetPropertyCommand("*.server-config.*.http-listener-1.port"); // NOI18N
            setEnvironmentProperty(GlassfishModule.HTTPHOST_ATTR, 
                    properties.get(GlassfishModule.HOSTNAME_ATTR), true); // NOI18N
        } else {
            String server = getServerFromTarget(target);
            String adminHost = properties.get(GlassfishModule.HOSTNAME_ATTR);
            setEnvironmentProperty(GlassfishModule.HTTPHOST_ATTR,
                    getHttpHostFromServer(server,adminHost), true);
            gpc = new GetPropertyCommand("servers.server."+server+".system-property.HTTP_LISTENER_PORT.value", true); // NOI18N
        }
        Future<OperationState> result2 = execute(true, gpc);
        try {
            boolean didSet = false;
            if (result2.get(10, TimeUnit.SECONDS) == OperationState.COMPLETED) {
                Map<String, String> retVal = gpc.getData();
                for (Entry<String, String> entry : retVal.entrySet()) {
                    String val = entry.getValue();
                    try {
                        if (null != val && val.trim().length() > 0) {
                            Integer.parseInt(val);
                            setEnvironmentProperty(GlassfishModule.HTTPPORT_ATTR, val, true);
                            didSet = true;
                        }
                    } catch (NumberFormatException nfe) {
                        // skip it quietly..
                    }
                }
            }
            if (!didSet && !Util.isDefaultOrServerTarget(properties)) {
                setEnvironmentProperty(GlassfishModule.HTTPPORT_ATTR, "28080", true); // NOI18N
            }
        } catch (InterruptedException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, null, ex); // NOI18N
        } catch (ExecutionException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, null, ex); // NOI18N
        } catch (TimeoutException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, "could not get http port value in 10 seconds from the server", ex); // NOI18N
        }
    }

    private String getServerFromTarget(String target) {
        String retVal = target; // NOI18N
        GetPropertyCommand  gpc = new GetPropertyCommand("clusters.cluster."+target+".server-ref.*.ref", true); // NOI18N

        Future<OperationState> result2 = execute(true, gpc);
        try {
            if (result2.get(10, TimeUnit.SECONDS) == OperationState.COMPLETED) {
                Map<String, String> data = gpc.getData();
                for (Entry<String, String> entry : data.entrySet()) {
                    String val = entry.getValue();
                        if (null != val && val.trim().length() > 0) {
                            retVal = val;
                            break;
                        }
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, null, ex); // NOI18N
        } catch (ExecutionException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, null, ex); // NOI18N
        } catch (TimeoutException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, "could not get http port value in 10 seconds from the server", ex); // NOI18N
        }

        return retVal;
    }
    private String getHttpHostFromServer(String server, String nameOfLocalhost) {
        String retVal = "localhostFAIL"; // NOI18N
        GetPropertyCommand  gpc = new GetPropertyCommand("servers.server."+server+".node-ref"); // NOI18N
        String refVal = null;
        Future<OperationState> result2 = execute(true, gpc);
        try {
            if (result2.get(10, TimeUnit.SECONDS) == OperationState.COMPLETED) {
                Map<String, String> data = gpc.getData();
                for (Entry<String, String> entry : data.entrySet()) {
                    String val = entry.getValue();
                        if (null != val && val.trim().length() > 0) {
                            refVal = val;
                            break;
                        }
                }
            }
            gpc = new GetPropertyCommand("nodes.node."+refVal+".node-host"); // NOI18N
            result2 = execute(true,gpc);
            if (result2.get(10, TimeUnit.SECONDS) == OperationState.COMPLETED) {
                Map<String, String> data = gpc.getData();
                for (Entry<String, String> entry : data.entrySet()) {
                    String val = entry.getValue();
                        if (null != val && val.trim().length() > 0) {
                            retVal = val;
                            break;
                        }
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, null, ex); // NOI18N
        } catch (ExecutionException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, null, ex); // NOI18N
        } catch (TimeoutException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, "could not get http port value in 10 seconds from the server", ex); // NOI18N
        }

        return "localhost".equals(retVal) ? nameOfLocalhost : retVal; // NOI18N
    }

    private boolean pingHttp(int maxTries) {
        boolean retVal = false;
        URL url = null;
        int tries = 0;
        while (false == retVal && tries < maxTries) {
            tries++;
            HttpURLConnection httpConn = null;
            try {
                url = new URL("http://" + getInstanceProperties().get(GlassfishModule.HTTPHOST_ATTR)
                        + ":" + getInstanceProperties().get(GlassfishModule.HTTPPORT_ATTR) + "/"); // NOI18N
                httpConn = (HttpURLConnection) url.openConnection();
                retVal = httpConn.getResponseCode() > 0;
            } catch (java.net.MalformedURLException mue) {
                Logger.getLogger("glassfish").log(Level.INFO, null, mue); // NOI18N
            } catch (java.net.ConnectException ce) {
                // we expect this...
                Logger.getLogger("glassfish").log(Level.FINE, url.toString(), ce); // NOI18N
            } catch (java.io.IOException ioe) {
                Logger.getLogger("glassfish").log(Level.INFO, url.toString(), ioe); // NOI18N
            } finally {
                if (null != httpConn) {
                    httpConn.disconnect();
                }
            }
            try {
                if (tries < maxTries) Thread.sleep(300);
            } catch (InterruptedException ex) {
            }
        }
        Logger.getLogger("glassfish").log(Level.FINE, "pingHttp returns {0}", retVal); // NOI18N
        return retVal;
    }
}
