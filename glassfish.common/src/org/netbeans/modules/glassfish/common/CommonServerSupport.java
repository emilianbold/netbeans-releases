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
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.glassfish.tools.ide.admin.*;
import org.glassfish.tools.ide.data.IdeContext;
import org.netbeans.modules.glassfish.common.nodes.actions.RefreshModulesCookie;
import org.netbeans.modules.glassfish.spi.GlassfishModule.OperationState;
import org.netbeans.modules.glassfish.spi.GlassfishModule.ServerState;
import org.netbeans.modules.glassfish.spi.ServerCommand.GetPropertyCommand;
import org.netbeans.modules.glassfish.spi.*;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;


/**
 *
 * @author Peter Williams
 */
public class CommonServerSupport implements GlassfishModule3, RefreshModulesCookie {


    ////////////////////////////////////////////////////////////////////////////
    // Inner methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Task state listener watching __locations command execution.
     */
    private static class LocationsTaskStateListener
            implements TaskStateListener {

        /** GlassFish server support object instance. */
        final CommonServerSupport css;

        /**
         * Creates an instance of task state listener watching __locations
         * command execution.
         * <p/>
         * @param css GlassFish server support object instance.
         */
        LocationsTaskStateListener(CommonServerSupport css) {
            this.css = css;
        }

        /**
         * Callback to notify about GlassFish __locations command execution
         * state change.
         * <p/>
         * @param newState New command execution state.
         * @param event    Event related to execution state change.
         * @param args     Additional String arguments.
         */
        @Override
        public void operationStateChanged(
                TaskState newState, TaskEvent event,
                String[] args) {
            String message = args.length > 0 ? args[0] : null;
            synchronized (css) {
                long lastDisplayed = css.getLatestWarningDisplayTime();
                long currentTime = System.currentTimeMillis();
                if (TaskState.FAILED == newState
                        && !"".equals(message)
                        && currentTime - lastDisplayed > 5000) {
                    NotifyDescriptor nd = new NotifyDescriptor.Message(message);
                    DialogDisplayer.getDefault().notifyLater(nd);
                    css.setLatestWarningDisplayTime(currentTime);
                    Logger.getLogger("glassfish").log(Level.INFO, message);
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Managed GlassFish instance. */
    private final GlassfishInstance instance;

    private volatile ServerState serverState = ServerState.UNKNOWN;
    private final Object stateMonitor = new Object();

    private ChangeSupport changeSupport = new ChangeSupport(this);

    private FileObject instanceFO;

    private volatile boolean startedByIde = false;
    private transient boolean isRemote = false;
    // prevent j2eeserver from stopping an authenticated domain that
    // the IDE did not start.
    private boolean stopDisabled = false;
    
    private Process localStartProcess;

    CommonServerSupport(GlassfishInstance instance) {
        this.instance = instance;
        this.isRemote = instance.getProperties().get(
                GlassfishModule.DOMAINS_FOLDER_ATTR) == null;
        // !PW FIXME hopefully temporary patch for JavaONE 2008 to make it easier
        // to persist per-instance property changes made by the user.
        instanceFO = getInstanceFileObject();
        if (!isRemote) {
            refresh();
        }
    }

    /**
     * Get <code>GlassfishInstance</code> object associated with this object.
     * <p/>
     * @return <code>GlassfishInstance</code> object associated with this object.
     */
    public GlassfishInstance getInstance() {
        return this.instance;
    }

    private FileObject getInstanceFileObject() {
        FileObject dir = FileUtil.getConfigFile(
                instance.getInstanceProvider().getInstancesDirName());
        if(dir != null) {
            String instanceFN = instance.getProperty(GlassfishInstanceProvider.INSTANCE_FO_ATTR);
            if(instanceFN != null) {
                return dir.getFileObject(instanceFN);
            }
        }
        return null;
    }

    @Override
    public String getPassword() {
        return instance.getPassword();
    }

    /** @deprecated Use in <code>GlassfishInstance</code> context. */
    @Deprecated
    public String getInstallRoot() {
        return instance.getInstallRoot();
    }

    /** @deprecated Use in <code>GlassfishInstance</code> context. */
    @Deprecated
    public String getGlassfishRoot() {
        return instance.getGlassfishRoot();
    }

    /** @deprecated Use in <code>GlassfishInstance</code> context. */
    @Deprecated
    public String getDisplayName() {
        return instance.getDisplayName();
    }

    /** @deprecated Use in <code>GlassfishInstance</code> context. */
    @Deprecated
    public String getDeployerUri() {
        return instance.getDeployerUri();
    }

    /** @deprecated Use in <code>GlassfishInstance</code> context. */
    @Deprecated
    public String getUserName() {
        return instance.getUserName();
    }

    /** @deprecated Use in <code>GlassfishInstance</code> context. */
    @Deprecated
    public String getAdminPort() {
        return instance.getHttpAdminPort();
    }

    /** @deprecated Use in <code>GlassfishInstance</code> context. */
    @Deprecated
    public String getHttpPort() {
        return instance.getHttpPort();
    }

    /** @deprecated Use in <code>GlassfishInstance</code> context. */
    @Deprecated
    public int getHttpPortNumber() {
        return instance.getPort();
    }

    /** @deprecated Use in <code>GlassfishInstance</code> context. */
    @Deprecated
    public int getAdminPortNumber() {
        return instance.getAdminPort();
    }

    /** @deprecated Use in <code>GlassfishInstance</code> context. */
    @Deprecated
    public String getHostName() {
        return instance.getProperty(HOSTNAME_ATTR);
    }

   /** @deprecated Use in <code>GlassfishInstance</code> context. */
   @Deprecated
   public String getDomainsRoot() {
        return instance.getDomainsRoot();
    }

    /** @deprecated Use in <code>GlassfishInstance</code> context. */
    @Deprecated
    public String getDomainName() {
        return instance.getDomainName();
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
        return Collections.unmodifiableMap(instance.getProperties());
    }

    @Override
    public GlassfishInstanceProvider getInstanceProvider() {
        return instance.getInstanceProvider();
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
        Collection<? extends RecognizerCookie> cookies = 
                instance.localLookup().lookupAll(RecognizerCookie.class);
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
        FutureTask<OperationState> task;
        if (!isRemote() || !Util.isDefaultOrServerTarget(instance.getProperties())) {
            if (getServerState() == ServerState.STOPPED_JVM_PROFILER) {
                task = new FutureTask<OperationState>(
                        new StopProfilingTask(this, stateListener));
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
    public Future<OperationState> deploy(OperationStateListener stateListener,
            File application, String name, String contextRoot,
            Map<String, String> properties, File[] libraries) {
        CommandRunner mgr = new CommandRunner(isReallyRunning(),
                getCommandFactory(), instance, stateListener);
        return mgr.deploy(application, name, contextRoot, properties,
                libraries);
    }

    @Override
    public Future<OperationState> redeploy(
            final OperationStateListener stateListener,
            final String name, boolean resourcesChanged) {
        return redeploy(stateListener, name, null, resourcesChanged);
    }

    @Override
    public Future<OperationState> redeploy(
            final OperationStateListener stateListener,
            final String name, final String contextRoot,
            boolean resourcesChanged) {
        return redeploy(stateListener, name, contextRoot, new File[0],
                resourcesChanged);
    }

    @Override
    public Future<OperationState> redeploy(OperationStateListener stateListener,
    String name, String contextRoot, File[] libraries,
    boolean resourcesChanged) {
        CommandRunner mgr = new CommandRunner(isReallyRunning(),
                getCommandFactory(), instance, stateListener);
        return mgr.redeploy(name, contextRoot, libraries, resourcesChanged);
    }

    @Override
    public Future<OperationState> undeploy(
            final OperationStateListener stateListener, final String name) {
        CommandRunner mgr = new CommandRunner(isReallyRunning(),
                getCommandFactory(), instance, stateListener);
        return mgr.undeploy(name);
    }

    @Override
    public Future<OperationState> enable(
            final OperationStateListener stateListener, final String name) {
        CommandRunner mgr = new CommandRunner(isReallyRunning(),
                getCommandFactory(), instance, stateListener);
        return mgr.enable(name);
    }
    @Override
    public Future<OperationState> disable(
            final OperationStateListener stateListener, final String name) {
        CommandRunner mgr = new CommandRunner(isReallyRunning(),
                getCommandFactory(), instance, stateListener);
        return mgr.disable(name);
    }

    @Override
    public Future<OperationState> execute(ServerCommand command) {
        CommandRunner mgr = new CommandRunner(isReallyRunning(),
                getCommandFactory(), instance);
        return mgr.execute(command);
    }

    private Future<OperationState> execute(boolean irr, ServerCommand command) {
        CommandRunner mgr = new CommandRunner(irr, getCommandFactory(),
                instance);
        return mgr.execute(command);
    }
    private Future<OperationState> execute(boolean irr, ServerCommand command,
            OperationStateListener... osl) {
        CommandRunner mgr = new CommandRunner(irr, getCommandFactory(),
                instance, osl);
        return mgr.execute(command);
    }

    @Override
    public AppDesc [] getModuleList(String container) {
        CommandRunner mgr = new CommandRunner(isReallyRunning(),
                getCommandFactory(), instance);
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
        CommandRunner mgr = new CommandRunner(isReallyRunning(),
                getCommandFactory(), instance);
        Map<String, ResourceDesc> resourcesMap
                = new HashMap<String, ResourceDesc>();
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
        String result;

        synchronized (instance.getProperties()) {
            result = instance.getProperty(name);
            if(result == null || overwrite == true) {
                instance.putProperty(name, value);
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
        instance.putProperty(key, value);
    }

    void getProperty(String key) {
        instance.getProperty(key);
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

    public static boolean isRunning(final String host, final int port,
            String name) {
        if(null == host)
            return false;

        try {
            InetSocketAddress isa = new InetSocketAddress(host, port);
            Socket socket = new Socket();
            int timeout = 2000;
            if ("localhost".equals(host) || "127.0.0.1".equals(host)) {
                timeout = 2000;
            }
            Logger.getLogger("glassfish-socket-connect-diagnostic").log(
                    Level.FINE, "Using socket.connect", new Exception());
            socket.connect(isa, timeout);
            socket.setSoTimeout(timeout);
            try { socket.close(); } catch (IOException ioe) {
                Logger.getLogger("glassfish").log(
                        Level.INFO, "closing after test", ioe);
            }
            return true;
        } catch (java.net.ConnectException ex) {
            return false;
        } catch (java.net.SocketTimeoutException ste) {
            return false;
        } catch (IOException ioe) {
            String message;
            if (name == null || "".equals(name.trim())) {
                message = NbBundle.getMessage(CommonServerSupport.class,
                        "MSG_FLAKEY_NETWORK", host, ""+port,
                        ioe.getLocalizedMessage());
            } else {
                message = NbBundle.getMessage(CommonServerSupport.class,
                        "MSG_FLAKEY_NETWORK2", host, ""+port,
                        ioe.getLocalizedMessage(), name);
            }
            NotifyDescriptor nd = new NotifyDescriptor.Message(message);
            DialogDisplayer.getDefault().notifyLater(nd);
            Logger.getLogger("glassfish").log(Level.INFO,
                    "evidence of network flakiness", ioe);
            return false;
        }
    }

    public boolean isReallyRunning() {
        return isReady(false,30,TimeUnit.SECONDS);
    }

    @SuppressWarnings("SleepWhileInLoop")
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
            CommandLocation commandLocation = new CommandLocation();
            try {
                //Future<OperationState> result;
                Future<ResultMap<String, String>> futureLocation;
                if (isRemote) {
                    TaskStateListener[] listenersLocation
                            = new TaskStateListener[]{
                        new LocationsTaskStateListener(this)};
                    futureLocation
                            = ServerAdmin.<ResultMap<String, String>>exec(
                            instance, commandLocation, new IdeContext(),
                            listenersLocation);
                } else {
                    futureLocation
                            = ServerAdmin.<ResultMap<String, String>>exec(
                            instance, commandLocation, new IdeContext());
                }
                ResultMap<String, String> resultLocation
                        = futureLocation.get(timeout, units);
                if (resultLocation.getState() == TaskState.COMPLETED) {
                    long end = System.nanoTime();
                    Logger.getLogger("glassfish").log(Level.FINE,
                            "{0} responded in {1}ms",
                            new Object[]{commandLocation.getCommand(),
                                (end - start) / 1000000});
                    String domainRoot = getDomainsRoot() + File.separator
                            + getDomainName();
                    String targetDomainRoot
                            = resultLocation.getValue().get(
                            "Domain-Root_value");
                    if (getDomainsRoot() != null && targetDomainRoot != null) {
                        File installDir
                                = FileUtil.normalizeFile(new File(domainRoot));
                        File targetInstallDir
                                = FileUtil.normalizeFile(
                                new File(targetDomainRoot));
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
                } else if (!commandLocation.retry()) {
                    // !PW temporary while some server versions support
                    // __locationsband some do not but are still V3 and might
                    // the ones the user is using.
                    Future<ResultString> future = 
                            ServerAdmin.<ResultString>exec(instance,
                            new CommandVersion(), new IdeContext());
                    isReady = future.get().getState() == TaskState.COMPLETED;
                    break;
                } else {
                    // keep trying for 10 minutes if the server is stuck between
                    // httpLive and server ready state. We have to give up
                    // sometime, though.
                    VMIntrospector vmi = Lookups.forPath(Util.GF_LOOKUP_PATH)
                            .lookup(VMIntrospector.class);
                    boolean suspended = null == vmi
                            ? false
                            : vmi.isSuspended(getHostName(),
                            (String) instance.getProperty(
                            GlassfishModule.DEBUG_PORT));
                    if (suspended) {
                        tries--;
                    } else if (maxtries < 20) {
                        maxtries++;
                    }
                    long end = System.nanoTime();
                    Logger.getLogger("glassfish").log(Level.INFO,
                            "{0} returned from server after {1}ms."
                            + " The server is still getting ready",
                            new Object[]{commandLocation.getCommand(),
                                (end - start) / 1000000});
                }
            } catch(TimeoutException ex) {
                Logger.getLogger("glassfish").log(Level.INFO,
                        commandLocation.getCommand() + " timed out. "
                        +tries+" of "+maxtries, ex);
                isReady = false;
            } catch (Exception ex) {
                Logger.getLogger("glassfish").log(Level.INFO,
                        commandLocation.getCommand() + " failed at  "
                        +tries+" of "+maxtries, ex);
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
    public final void refresh() {
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
                    if (isRunning && !Util.isDefaultOrServerTarget(
                            instance.getProperties())) {
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
        return instance.getInstanceProvider().getCommandFactory();
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
        String target = Util.computeTarget(instance.getProperties());
        GetPropertyCommand gpc;
        if (Util.isDefaultOrServerTarget(instance.getProperties())) {
            gpc = new GetPropertyCommand("*.server-config.*.http-listener-1.port"); // NOI18N
            setEnvironmentProperty(GlassfishModule.HTTPHOST_ATTR, 
                    instance.getProperty(GlassfishModule.HOSTNAME_ATTR), true); // NOI18N
        } else {
            String server = getServerFromTarget(target);
            String adminHost = instance.getProperty(GlassfishModule.HOSTNAME_ATTR);
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
            if (!didSet && !Util.isDefaultOrServerTarget(instance.getProperties())) {
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

    @SuppressWarnings("SleepWhileInLoop")
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
