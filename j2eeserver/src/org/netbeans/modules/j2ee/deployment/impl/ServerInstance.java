/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.j2ee.deployment.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.shared.*;
import javax.enterprise.deploy.spi.status.*;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.jpda.AttachingDICookie;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.openide.filesystems.*;
import java.util.*;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.impl.ui.ProgressUI;
import org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerServerSettings;
import org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerSupport;
import org.netbeans.modules.j2ee.deployment.profiler.spi.Profiler;
import org.openide.nodes.Node;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;


public class ServerInstance implements Node.Cookie, Comparable {
    
    /** Server state is being checked or state changes is in progress */
    public static final int STATE_WAITING   = 1;
    /** Server is stopped */
    public static final int STATE_STOPPED   = 2;
    /** Server is running in normal mode */
    public static final int STATE_RUNNING   = 3;
    /** Server is running in debug mode */
    public static final int STATE_DEBUGGING = 4;
    /** Server is suspended on a break point (in debug mode and not responding) */
    public static final int STATE_SUSPENDED = 5;
    /** Server is running in profile mode */
    public static final int STATE_PROFILING = 6;
    /** Server is ready for the profiler to connect, server JVM is blocked. */
    public static final int STATE_PROFILER_BLOCKING = 7;
    /** Server is starting in profile mode. */
    public static final int STATE_PROFILER_STARTING = 8;
    
    /** For how long should plugins be allowed to block in the isRunning method */
    private static final int RUNNING_CHECK_TIMEOUT = 10000; // in millis
    /** For how long should plugins be allowed to block in the isDebuggable method */
    private static final int DEBUGGING_CHECK_TIMEOUT = 10000; // in millis
    
    /** Maximum amount of time the server should finish starting/stopping in */
    private static final long TIMEOUT = 1200000; // in millis
    
    private final String url;
    private final Server server;
    private DeploymentManager manager;
    private DeploymentManager disconnectedManager;
    private IncrementalDeployment incrementalDeployment;
    private TargetModuleIDResolver tmidResolver;
    private J2eePlatform j2eePlatform;
    private J2eePlatformImpl j2eePlatformImpl;
    private StartServer startServer;
    private FindJSPServlet findJSPServlet;
    private final Set targetsStartedByIde = new HashSet(); // valued by target name
    private Map targets; // keyed by target name, valued by ServerTarget
    private boolean managerStartedByIde = false;
    private ServerTarget coTarget = null;
    private boolean commandSucceed = false;
    private InstancePropertiesImpl instanceProperties;
    private HashMap/*<Target, ServerDebugInfo>*/ debugInfo = new HashMap();
    
    // last known server state, the initial value is stopped
    private int serverState = STATE_STOPPED;
    // server state listeners
    private List stateListeners = new ArrayList();
    
    // running check helpers
    private long lastCheck = 0;
    private boolean isRunning = false;
    
    
    private static ServerInstance   profiledServerInstance;
    private ProfilerServerSettings  profilerSettings;
    
    // PENDING how to manage connected/disconnected servers with the same manager?
    // maybe concept of 'default unconnected instance' is broken?
    public ServerInstance(Server server, String url) {
        this.server = server;
        this.url = url;
        instanceProperties = new InstancePropertiesImpl(url);
        // listen to debugger changes so that we can update server status accordingly
        DebuggerManager.getDebuggerManager().addDebuggerListener(new DebuggerStateListener());
    }
    
    /** Return this server instance InstanceProperties. */
    public InstancePropertiesImpl getInstanceProperties() {
        return instanceProperties;
    }
    
    /** Return display name of this server instance.*/
    public String getDisplayName() {
        return instanceProperties.getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
    }
    
    public Server getServer() {
        return server;
    }
    
    public String getUrl() {
        return url;
    }
    
    public DeploymentManager getDeploymentManager() {
        if (manager != null) return manager;
        
        try {
            FileObject fo = ServerRegistry.getInstanceFileObject(url);
            if (fo == null) {
                String msg = NbBundle.getMessage(ServerInstance.class, "MSG_NullInstanceFileObject", url);
                throw new IllegalStateException(msg);
            }
            String username = (String) fo.getAttribute(ServerRegistry.USERNAME_ATTR);
            String password = (String) fo.getAttribute(ServerRegistry.PASSWORD_ATTR);
            manager = server.getDeploymentManager(url, username, password);
        } catch(javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException e) {
            throw new RuntimeException(e);
        }
        return manager;
    }
    
    public boolean isConnected () {
        return manager != null;
    }
    
    public DeploymentManager getDisconnectedDeploymentManager() throws DeploymentManagerCreationException {
        if (disconnectedManager != null) return disconnectedManager;
        
        FileObject fo = ServerRegistry.getInstanceFileObject(url);
        if (fo == null) {
            String msg = NbBundle.getMessage(ServerInstance.class, "MSG_NullInstanceFileObject", url);
            throw new IllegalStateException(msg);
        }
        disconnectedManager = server.getDisconnectedDeploymentManager(url);
        return disconnectedManager;
    }
    
    public J2eePlatform getJ2eePlatform() {
        return j2eePlatform;
    }
    
    public void setJ2eePlatform(J2eePlatform aJ2eePlatform ) {
        j2eePlatform = aJ2eePlatform;
    }
    
    public J2eePlatformImpl getJ2eePlatformImpl() {
        if (j2eePlatformImpl == null) {
            J2eePlatformFactory fact = server.getJ2eePlatformFactory();
            // TODO this will be removed, implementation of J2EEPlatformFactory will be mandatory
            if (fact != null) {
                try {
                    j2eePlatformImpl = fact.getJ2eePlatformImpl(isConnected() ? getDeploymentManager() : getDisconnectedDeploymentManager());
                }  catch (DeploymentManagerCreationException dmce) {
                    ErrorManager.getDefault().notify(dmce);
                }
            }
        }
        return j2eePlatformImpl;
    }
    
    public ServerDebugInfo getServerDebugInfo(Target target) {
        assert debugInfo != null;
        ServerDebugInfo sdi = null;
        if (target == null) { //performance: treat as special simple case
            sdi = (ServerDebugInfo) debugInfo.get(null);
        } else {
            for (Iterator it = debugInfo.keySet().iterator(); sdi == null && it.hasNext(); ) {
                Target t = (Target) it.next();
                if (t == target || (t != null && t.getName().equals(target.getName()))) {
                    sdi = (ServerDebugInfo) debugInfo.get(t);
                }
            }
        }
        
        return sdi;
    }
    
    public void refresh() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    setServerState(STATE_WAITING);
                    if (ServerInstance.this == profiledServerInstance) {
                        int profState = ProfilerSupport.getState();
                        if (profState == ProfilerSupport.STATE_STARTING) {
                            setServerState(ServerInstance.STATE_PROFILER_STARTING);
                            return;
                        } else if (profState == ProfilerSupport.STATE_BLOCKING) {
                            setServerState(ServerInstance.STATE_PROFILER_BLOCKING);
                            return;
                        } else if (profState == ProfilerSupport.STATE_PROFILING
                                   || profState == ProfilerSupport.STATE_RUNNING) {
                            initCoTarget();
                            setServerState(ServerInstance.STATE_PROFILING);
                            return;
                        } else {
                            //  profiler is inactive - has been shutdown
                            profiledServerInstance = null;
                        }
                    }
                    if (isSuspended()) {
                        setServerState(ServerInstance.STATE_SUSPENDED);
                    } else if (isDebuggable(null)) {
                        initCoTarget();
                        setServerState(ServerInstance.STATE_DEBUGGING);
                    } else if (isReallyRunning()) {
                        initCoTarget();
                        setServerState(ServerInstance.STATE_RUNNING);
                    } else {
                        reset();
                        setServerState(ServerInstance.STATE_STOPPED);
                    }
                } finally {
                    // safety catch - make sure that we are not still waiting
                    if (getServerState() == STATE_WAITING) {
                        setServerState(ServerInstance.STATE_STOPPED);
                    }
                }
            }
        });
    }
    
    public void reset() {
        if (manager != null) {
            manager.release();
            manager = null;
        }
        if (disconnectedManager != null) {
            disconnectedManager = null;
        }
        incrementalDeployment = null;
        tmidResolver = null;
        startServer = null;
        findJSPServlet = null;
        coTarget = null;
        targets = null;
    }
    
    /** Remove this server instance and stop it if it has been started from within the IDE */
    public void remove() {
        stopIfStartedByIde();        
        // close the server io window
        InputOutput io = UISupport.getServerIO(url);
        if (!io.isClosed()) {
            io.closeInputOutput();
        }
        ServerRegistry.getInstance().removeServerInstance(getUrl());
    }
    
    /** Stop the server if it has been started from within the IDE, do nothing otherwise */
    public void stopIfStartedByIde() {
        if (managerStartedByIde) {
            if (canStopDontWait()) {
                stopDontWait();
            } else {
                String title = NbBundle.getMessage(ServerInstance.class, "LBL_ShutDownServer", getDisplayName());
                final ProgressUI progressUI = new ProgressUI(title, true, null);
                progressUI.start();
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        try {
                            for (Iterator it = targetsStartedByIde.iterator(); it.hasNext();) {
                                ServerTarget serverTarget = getServerTarget((String)it.next());
                                if (serverTarget != null) {
                                    try {
                                        _stop(serverTarget.getTarget(), progressUI);
                                    } catch (ServerException ex) {
                                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                                    }
                                }
                            }
                            if (isReallyRunning() || isSuspended()) {
                                try {
                                    _stop(progressUI);
                                } catch (ServerException ex) {
                                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                                }
                            }
                        } finally {
                            progressUI.finish();
                        }
                    }
                });
                progressUI.showProgressDialog();
            }
        }
    }
    
    /** Set the server state and notify all listeners */
    public void setServerState(int newState) {
        int oldState = serverState;
        serverState = newState;
        fireStateChanged(oldState, newState);
    }
    
    /** Return the current server state */
    public int getServerState() {
        return serverState;
    }
    
    /** Is it forbidden to remove this server instance from the server registry? */
    public boolean isRemoveForbidden() {
        String removeForbid = instanceProperties.getProperty(InstanceProperties.REMOVE_FORBIDDEN);
        return Boolean.valueOf(removeForbid).booleanValue();
    }
    
    public ServerTarget[] getTargets() {
        getTargetMap();
        return (ServerTarget[]) targets.values().toArray(new ServerTarget[targets.size()]);
    }
    
    public Collection getTargetList() {
        getTargetMap();
        return  targets.values();
    }
    
    // PENDING use targets final variable?
    private Map getTargetMap() {
        if (targets == null) {
            Target[] targs = null;
            StartServer startServer = getStartServer();
            try {
                if (! isRunning() && startServer != null && startServer.needsStartForTargetList()) {
                    start();
                }
                
                targs = getDeploymentManager().getTargets();
            } catch(IllegalStateException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            if(targs == null) {
                targs = new Target[0];
            }
            
            targets = new HashMap();
            for (int i = 0; i < targs.length; i++) {
                //System.out.println("getTargetMap: targ["+i+"]="+targs[i]);
                targets.put(targs[i].getName(), new ServerTarget(this, targs[i]));
            }
        }
        return targets;
    }
    
    public ServerTarget getServerTarget(String targetName) {
        return (ServerTarget) getTargetMap().get(targetName);
    }
    
    public Target getTarget(String targetName) {
        return getServerTarget(targetName).getTarget();
    }
    
    public StartServer getStartServer() {
        if (startServer == null) {
            try {
                startServer = server.getOptionalFactory ().getStartServer(getDisconnectedDeploymentManager());
            }  catch (DeploymentManagerCreationException dmce) {
                ErrorManager.getDefault().notify(dmce);
            }
        }
        return startServer;
    }
    
    public IncrementalDeployment getIncrementalDeployment() {
        if (incrementalDeployment == null) {
            incrementalDeployment = server.getOptionalFactory().getIncrementalDeployment(getDeploymentManager());
        }
        return incrementalDeployment;
    }
    
    public TargetModuleIDResolver getTargetModuleIDResolver() {
        if (tmidResolver == null) {
            tmidResolver = server.getOptionalFactory().getTargetModuleIDResolver(getDeploymentManager());
        }
        return tmidResolver;
    }
    
    public FindJSPServlet getFindJSPServlet() {
        if (findJSPServlet == null) {
            try {
                findJSPServlet = server.getOptionalFactory().getFindJSPServlet (getDisconnectedDeploymentManager());
            }  catch (DeploymentManagerCreationException dmce) {
                ErrorManager.getDefault().notify(dmce);
            }
        }
        return findJSPServlet;
    }
    
    //---------- State API's:  running, debuggable, startedByIDE -----------
    
    public boolean isRunningLastCheck() {
        if (lastCheck > 0) {
            return isRunning;
        } else {
            return false;
        }
    }
    
    public boolean isReallyRunning() {
        return isRunningWithinMillis(0);
    }
    
    public boolean isRunning() {
        return isRunningWithinMillis(2000);
    }
    
    public boolean isRunningWithinMillis(long millisecs) {
        if (System.currentTimeMillis() - lastCheck < millisecs) {
            return isRunning;
        }
        final StartServer ss = getStartServer();
        if (ss != null) {
            isRunning = safeTrueTest(new SafeTrueTest() {
                                         public void run() {
                                             result = ss.isRunning();
                                         }
                                     }, 
                                     RUNNING_CHECK_TIMEOUT);
        } else {
            isRunning = false;
        }
        lastCheck = System.currentTimeMillis();
        return isRunning;
    }
    
    public boolean isDebuggable(final Target target) {
        final StartServer ss = getStartServer();
        if (ss != null) {
            return safeTrueTest(new SafeTrueTest() {
                                    public void run() {
                                        result = ss.isDebuggable(target);
                                    }
                                }, 
                                DEBUGGING_CHECK_TIMEOUT);
        } else {
            return false;
        }
    }
    
    /**
     * @return conflict data instance for server instance running in debug mode with the same socket number
     * or shared memory id. If no such server instance exists then null is returned.
     */
    public ConflictData anotherServerDebuggable(Target target) {
        
        ConflictData cd = null;
        //get debug info for this instance
        StartServer thisSS = getStartServer();
        if (thisSS == null) //this case should not occur =>
            return null; //attempt to start server (serverInstance remains null)
        ServerDebugInfo thisSDI = getServerDebugInfo(target);
        if (thisSDI == null) {
            Target t = _retrieveTarget(target);
            thisSDI = thisSS.getDebugInfo(t);
            if (thisSDI == null) {
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "DebuggerInfo cannot be found for: " + this.toString());
                return null;
            }
        }
        
        //get all server instances
        ServerInstance[] serverInstances = ServerRegistry.getInstance().getServerInstances();
        //check existence of a server instance running in debug mode with the same parameters
        for (int i = 0; cd == null && i < serverInstances.length; i++) {
            ServerInstance si = serverInstances[i];
            if (url.equalsIgnoreCase(si.getUrl())) continue;
            if (si.isDebuggable(null)) { //running in debug mode
                Target t = si._retrieveTarget(null);
                ServerDebugInfo sdi = si.getServerDebugInfo(t);
                if (sdi == null) continue; //should not occur -> workaround for issue #56714
                if (thisSDI.getTransport().equals(sdi.getTransport())) { //transport matches
                    if (thisSDI.getTransport() == ServerDebugInfo.TRANSPORT_SOCKET) {
                        if (thisSDI.getHost().equalsIgnoreCase(sdi.getHost())) //host matches
                            if (thisSDI.getPort() == sdi.getPort()) //port matches
                                cd = new ConflictData(si, thisSDI);
                    } else if (thisSDI.getShmemName().equalsIgnoreCase(sdi.getShmemName()))
                        cd = new ConflictData(si, thisSDI);
                }
            }
        }
        
        return cd;
    }
    
    /**
     * Returns true if this server is started in debug mode AND debugger is attached to it
     * AND threads are suspended (e.g. debugger stopped on breakpoint)
     */
    public boolean isSuspended() {
        
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        
        Target target = _retrieveTarget(null);
        ServerDebugInfo sdi = getServerDebugInfo(target);
        if (sdi == null) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "DebuggerInfo cannot be found for: " + this.toString());
            return false; // give user a chance to start server even if we don't know whether she will success
        }
        
        for (int i = 0; i < sessions.length; i++) {
            Session s = sessions[i];
            if (s == null) continue;
            Object o = s.lookupFirst(null, AttachingDICookie.class);
            if (o == null) continue;
            AttachingDICookie attCookie = (AttachingDICookie)o;
            if (sdi.getTransport().equals(ServerDebugInfo.TRANSPORT_SHMEM)) {
                String shmem = attCookie.getSharedMemoryName();
                if (shmem == null) continue;
                if (shmem.equalsIgnoreCase(sdi.getShmemName())) {
                    Object d = s.lookupFirst(null, JPDADebugger.class);
                    if (d != null) {
                        JPDADebugger jpda = (JPDADebugger)d;
                        if (jpda.getState() == JPDADebugger.STATE_STOPPED) {
                            return true;
                        }
                    }
                }
            } else {
                String host = stripHostName(attCookie.getHostName());
                if (host == null) continue;
                if (host.equalsIgnoreCase(stripHostName(sdi.getHost()))) {
                    if (attCookie.getPortNumber() == sdi.getPort()) {
                        Object d = s.lookupFirst(null, JPDADebugger.class);
                        if (d != null) {
                            JPDADebugger jpda = (JPDADebugger)d;
                            if (jpda.getState() == JPDADebugger.STATE_STOPPED) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Can be this server started in the debug mode? Currently the only case when
     * the server cannot be started in the debugged is when the admin server is
     * not also the target server.
     *
     * @return <code>true</code> if the server can be started in the debug mode,
     *         <code>false/code> otherwise.
     */
    public boolean isDebugSupported() {
        StartServer ss = getStartServer();
        return ss.supportsStartDebugging(null);
    }
    
    /** 
     * Can be this server started in profile mode? Currently the only case when
     * the server cannot be started in the debugged is when the admin server is
     * not also the target server.
     */
    public boolean isProfileSupported() {
        Profiler profiler = ServerRegistry.getProfiler();
        if (profiler == null) {
            return false;
        }
        StartServer ss = getStartServer();
        return ss.supportsStartProfiling(null);
    }
    
    /**
     * Return set of ServerTarget's that have been started from inside IDE.
     * @return set of ServerTarget objects.
     */
    public Set getTargetsStartedByIde() {
        Set ret = new HashSet();
        for (Iterator i=targetsStartedByIde.iterator(); i.hasNext(); ) {
            String targetName = (String) i.next();
            ret.add(getServerTarget(targetName));
        }
        return ret;
    }
    
    //----------- State Transistion API's: ----------------------
    
    /**
     * Start the admin server. Show UI feedback.
     * Note: for debug mode, always use startDebugTarget() calls because
     * it is sure then the target need to be started.
     *
     * @throws ServerException if the server cannot be started.
     */
    public void start(ProgressUI ui) throws ServerException {
        try {
            setServerState(STATE_WAITING);
            startTarget(null, ui);
        } finally {
            refresh();
        }
    }
    
    /** Start the admin server in the debug mode. Show UI feedback. 
     *
     * @throws ServerException if the server cannot be started.
     */
    public void startDebug(ProgressUI ui) throws ServerException {
        try {
            setServerState(STATE_WAITING);
            startTarget(null, ui, true);
            _retrieveDebugInfo(null);
        } finally {
            refresh();
        }
    }
    
    /** Start the admin server in the profile mode. Show UI feedback. 
     * @param settings settings that will be used to start the server
     *
     * @throws ServerException if the server cannot be started.
     */
    public void startProfile(ProfilerServerSettings settings, boolean forceRestart, ProgressUI ui) 
    throws ServerException {
        // check whether another server not already running in profile mode
        // and ask whether it is ok to stop it
        if (profiledServerInstance != null && profiledServerInstance != this) {
            String msg = NbBundle.getMessage(
                                    ServerInstance.class,
                                    "MSG_AnotherServerProfiling",
                                    profiledServerInstance.getDisplayName());
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.OK_CANCEL_OPTION);
            if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.CANCEL_OPTION) {
                // start in profile mode has been cancelled
                String err = NbBundle.getMessage(ServerInstance.class, "MSG_ProfilingCancelled", getDisplayName());
                throw new ServerException(err);
            }
        }
        try {
            setServerState(STATE_WAITING);
            // target == null - admin server
            startProfileImpl(null, settings, forceRestart, ui);
        } finally {
            refresh();
        }
    }
    
    /** Restart the admin server in the mode the server was running in before. 
     * Show UI feedback. 
     *
     * @throws ServerException if the server cannot be restarted.
     */
    public void restart(ProgressUI ui) throws ServerException {
        try {
            setServerState(STATE_WAITING);
            boolean inDebug = isDebuggable(null);
            boolean inProfile = profiledServerInstance == this;
            boolean stopped = true;
            
            if (inProfile || isReallyRunning() || isSuspended()) {
                _stop(ui);
            }
            if (stopped) {
                // restart in the mode the server was running in before
                if (inProfile) {
                    startProfileImpl(null, profilerSettings, true, ui);
                } else if (inDebug) {
                    startDebugTarget(null, ui);
                } else {
                    startTarget(null, ui);
                }
            }
        } finally {
            refresh();
        }
    }
    
    /** Stop admin server. Show UI feedback. 
     *
     * @throws ServerException if the server cannot be stopped.
     */
    public void stop(ProgressUI ui) throws ServerException {
        try {
            setServerState(STATE_WAITING);
            if (profiledServerInstance == this || isReallyRunning() || isSuspended()) {
                _stop(ui);
            }
            debugInfo.clear();
        } finally {
            refresh();
        }
    }
    
    // Note: configuration needs
    /**
     * Return a connected DeploymentManager if needed by server platform for configuration
     * @return DeploymentManager object for configuration.
     */
    public DeploymentManager getDeploymentManagerForConfiguration() throws DeploymentManagerCreationException {
        StartServer ss = getStartServer();
        if (ss != null && ss.needsStartForConfigure()) {
            start();
            return getDeploymentManager();
        } else {
            return getDisconnectedDeploymentManager();
        }
    }
    
    // Note: execution only need these 3 state transition APIs
    
    /**
     * Start specified target server.  If it is also admin server only make sure
     * admin server is running.
     * @param target target server to be started
     * @param ui DeployProgressUI to display start progress
     *
     * @throws ServerException if the target cannot be started.
     */
    public void startTarget(Target target, ProgressUI ui) throws ServerException {
        startTarget(target, ui, false);
    }
    
    /**
     * Start specified target server in debug mode.  If target is also admin
     * server only make sure admin server is running.
     * @param target target server to be started
     * @param ui DeployProgressUI to display start progress
     *
     * @throws ServerException if the server cannot be started.
     */
    public void startDebugTarget(Target target, ProgressUI ui) throws ServerException {
        startTarget(target, ui, true);
        _retrieveDebugInfo(target);
    }
    
    /**
     * Start admin server, mainly for registry actions with no existing progress UI
     *
     * @throws ServerException if the server cannot be started.
     */
    private void start() {
        if (SwingUtilities.isEventDispatchThread()) {
            //PENDING maybe a modal dialog instead of async is needed here
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    start();
                }
            });
        }
        if (isRunning()) {
            return;
        }
        String title = NbBundle.getMessage(ServerInstance.class, "LBL_StartServerProgressMonitor", getDisplayName());
        ProgressUI ui = new ProgressUI(title, false);
        try {
            ui.start();
            start(ui);
        } catch (ServerException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } finally {
            ui.finish();
        }
    }
    
    /**
     * Start admin server for profiling, mainly for registry actions with no existing progress UI
     * @param settings settings that will be used to start the server
     *
     * @throws ServerException if the server cannot be started.
     */
    public boolean startProfile(final ProfilerServerSettings settings, boolean forceRestart, Deployment.Logger logger) {
        String title = NbBundle.getMessage(ServerInstance.class, "LBL_StartServerInProfileMode", getDisplayName());
        ProgressUI ui = new ProgressUI(title, false, logger);
        try {
            ui.start();
            startProfile(settings, forceRestart, ui);
            return true;
        } catch (ServerException ex) {
            return false;
        } finally {
            ui.finish();
        }
    }
    
    /** Stop the server and do not wait for response.
     * This will be used at IDE exit.
     */
    public void stopDontWait() {
        if (isReallyRunning()) {
            assert startServer.canStopDeploymentManagerSilently() : "server does not support silent stop of deployment manager";
            startServer.stopDeploymentManagerSilently();
        }
    }
    
    /** see stopDontWait */
    public boolean canStopDontWait() {
        return startServer.canStopDeploymentManagerSilently();
    }
    
    //------------------------------------------------------------
    /**
     * @throws ServerException if the conflict has not been resolved.
     */
    private void resolveServerConflict(Target target, ProgressUI ui, ConflictData cd) throws ServerException {
        
        ServerInstance si = cd.getServerInstance();
        //inform a user and allow him to stop the running instance
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(
                ServerInstance.class,
                "MSG_AnotherServerRunning",
                new Object[] {
            si.getDisplayName(),
                    cd.getServerDebugInfo().getHost(),
                    cd.getServerDebugInfo().getTransport().equals(ServerDebugInfo.TRANSPORT_SOCKET) ?
                        "socket" : "shared memory",
                    cd.getServerDebugInfo().getTransport().equals(ServerDebugInfo.TRANSPORT_SOCKET) ?
                        new Integer(cd.getServerDebugInfo().getPort()).toString() : cd.getServerDebugInfo().getShmemName()
        }),
                NotifyDescriptor.QUESTION_MESSAGE
                );
        nd.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
        JButton yes = new JButton(NbBundle.getMessage(ServerInstance.class, "MSG_AnotherServerStopYes"));
        JButton no = new JButton(NbBundle.getMessage(ServerInstance.class, "MSG_AnotherServerStopNo"));
        yes.setDefaultCapable(true);
        nd.setOptions(new Object[] { yes, no });
        Object option = DialogDisplayer.getDefault().notify(nd);
        if (option != yes) { //user pressed No
            String msg = NbBundle.getMessage(ServerInstance.class, "MSG_ServerStartupCancelled", getDisplayName());
            throw new ServerException(msg);
        }
        //try to stop running server
        if (si.getStartServer().supportsStartDeploymentManager()) {
            si.stop(ui);
        } else {
            String msg = NbBundle.getMessage(ServerInstance.class, "MSG_StartingThisServerNotSupported", getDisplayName());
            throw new ServerException(msg);
        }
    }
    
    // multiplexor state-machine core
    /**
     * @throws ServerException if the server cannot be started.
     */
    private void startTarget(Target target, ProgressUI ui, boolean debugMode) throws ServerException {
        StartServer ss = getStartServer();
        
        // No StartServer, have to assume manually started
        if (ss == null) {
            ui.progress(NbBundle.getMessage(ServerInstance.class, "MSG_PluginHasNoStartServerClass", getServer()));
            return;
        }
        
        boolean canControlAdmin = ss.supportsStartDeploymentManager();
        boolean canDebug = ss.supportsStartDebugging(target);
        boolean needsRestart = ss.needsRestart(target);
        
        if (ss.isAlsoTargetServer(target)) {
            if (debugMode) {
                if (ss.isDebuggable(target)) { // already running in debug mode
                    if (! needsRestart) {
                        return;
                    }
                    if (!canControlAdmin || !canDebug) {
                        String msg = NbBundle.getMessage(ServerInstance.class, "MSG_RestartingThisServerNotSupported", getDisplayName());
                        throw new ServerException(msg);
                    }
                    _stop(ui);
                } else if (isReallyRunning()) { // running but not debuggable
                    if (!canControlAdmin || !canDebug) {
                        String msg = NbBundle.getMessage(ServerInstance.class, "MSG_DebugginThisServerNotSupported", getDisplayName());
                        throw new ServerException(msg);
                    }
                    _stop(ui);
                }                
                // the server is stopped now
                if (!canDebug) {
                    String msg = NbBundle.getMessage(ServerInstance.class, "MSG_DebugginThisServerNotSupported", getDisplayName());
                    throw new ServerException(msg);
                }
                // resolve conflicts with other servers
                ConflictData cd = anotherServerDebuggable(target);
                if (cd != null) { // another server instance with the same parameters
                    resolveServerConflict(target, ui, cd);
                }
                _startDebug(target, ui);
            } else {
                if (isReallyRunning()) { // already running 
                    if (! needsRestart) {
                        return;
                    }
                    if (!canControlAdmin) {
                        String msg = NbBundle.getMessage(ServerInstance.class, "MSG_RestartingThisServerNotSupported", getDisplayName());
                        throw new ServerException(msg);
                    }
                    _stop(ui);
                }
                if (!canControlAdmin) {
                    String msg = NbBundle.getMessage(ServerInstance.class, "MSG_StartingThisServerNotSupported", getDisplayName());
                    throw new ServerException(msg);
                }
                _start(ui);
            }
        } else { // not also target server
            // this block ensure a running admin server to control other targets
            if (! isReallyRunning()) {
                if (!canControlAdmin) {
                    String msg = NbBundle.getMessage(ServerInstance.class, "MSG_StartingThisServerNotSupported", getDisplayName());
                    throw new ServerException(msg);
                }
                _start(ui);
            }
            if (debugMode) {
                if (ss.isDebuggable(target)) {
                    if ( ! needsRestart) {
                        return;
                    }
                    _stop(target, ui);
                } else if (ss.isRunning(target)) {
                    _stop(target, ui);
                }
                ConflictData cd = anotherServerDebuggable(target);
                if (cd != null) { //another server instance with the same parameters
                    resolveServerConflict(target, ui, cd);
                }
                _startDebug(target, ui);
            } else {
                if (ss.isRunning(target)) {
                    if (! needsRestart) {
                        return;
                    }
                    _stop(target, ui);
                }
                _start(target, ui);
            }
        }
    }
    
    //------------------------------------------------------------
    // state-transition atomic operations (always do-it w/o checking state)
    //------------------------------------------------------------
    // startDeploymentManager
    private synchronized void _start(ProgressUI ui) throws ServerException {
        
        String displayName = getDisplayName();
        
        ui.progress(NbBundle.getMessage(ServerInstance.class, "MSG_StartingServer", displayName));
        
        ProgressObject po = null;
        StartProgressHandler handler = new StartProgressHandler();
        
        try {
            setCommandSucceeded(false);
            po = getStartServer().startDeploymentManager();
            if (ui != null) {
                ui.setProgressObject(po);
            }
            po.addProgressListener(handler);
            
            if (isProgressing(po)) {
                // wait until done or cancelled
                boolean done = sleep();
                if (! done) {
                    String msg = NbBundle.getMessage(ServerInstance.class, "MSG_StartServerTimeout", displayName);
                    throw new ServerException(msg);
                } else if (! hasCommandSucceeded()) {
                    DeploymentStatus status = po.getDeploymentStatus();
                    throw new ServerException(status.getMessage());
                }
            } else if (hasFailed(po)) {
                DeploymentStatus status = po.getDeploymentStatus();
                throw new ServerException(status.getMessage());
            }
            managerStartedByIde = true;
            coTarget = null;
            targets = null;
            initCoTarget();
        } finally {
            if (ui != null) {
                ui.setProgressObject(null);
            }
            if (po != null) {
                po.removeProgressListener(handler);
            }
        }
    }
    
    // startDebugging
    private synchronized void _startDebug(Target target, ProgressUI ui) throws ServerException {
        
        String displayName = getDisplayName();
        ui.progress(NbBundle.getMessage(ServerInstance.class, "MSG_StartingDebugServer", displayName));
        
        ProgressObject po = null;
        StartProgressHandler handler = new StartProgressHandler();
        
        try {
            setCommandSucceeded(false);
            po = getStartServer().startDebugging(target);
            if (ui != null) {
                ui.setProgressObject(po);
            }
            po.addProgressListener(handler);
            
            if (isProgressing(po)) {
                // wait until done or cancelled
                boolean done = sleep();
                if (! done) {
                    String msg = NbBundle.getMessage(ServerInstance.class, "MSG_StartDebugTimeout", displayName);
                    throw new ServerException(msg);
                } else if (! hasCommandSucceeded()) {
                    DeploymentStatus status = po.getDeploymentStatus();
                    throw new ServerException(status.getMessage());
                }
            } else if (hasFailed(po)) {
                DeploymentStatus status = po.getDeploymentStatus();
                throw new ServerException(status.getMessage());
            }
            managerStartedByIde = true;
            coTarget = null;
            targets = null;
            initCoTarget();
        } finally {
            if (ui != null) {
                ui.setProgressObject(null);
            }
            if (po != null) {
                po.removeProgressListener(handler);
            }
        }
    }
    
    /** start server in the profile mode */
    private synchronized void startProfileImpl(
                                    Target target, 
                                    ProfilerServerSettings settings,
                                    boolean forceRestart,
                                    ProgressUI ui) throws ServerException {
        if (profiledServerInstance == this && !forceRestart && settings.equals(profilerSettings)) {
            return; // server is already runnning in profile mode, no need to restart the server
        }
        if (profiledServerInstance != null && profiledServerInstance != this) {
            // another server currently running in profiler mode
            profiledServerInstance.stop(ui);
            profiledServerInstance = null;
        }
        if (profiledServerInstance == this || isReallyRunning() || isDebuggable(target)) {
            _stop(ui);
            debugInfo.clear();
            profiledServerInstance = null;
        }
        
        String displayName = getDisplayName();
        ui.progress(NbBundle.getMessage(ServerInstance.class, "MSG_StartingProfileServer", displayName));
        
        ProgressObject po = null;
        StartProgressHandler handler = new StartProgressHandler();
        
        try {
            setCommandSucceeded(false);
            Profiler profiler = ServerRegistry.getProfiler();
            if (profiler == null) {
                // this should not occur, but it is safer this way
                String msg = NbBundle.getMessage(ServerInstance.class, "MSG_ProfilerNotRegistered");
                throw new ServerException(msg);
            }
            profiler.notifyStarting();
            po = getStartServer().startProfiling(target, settings);
            ui.setProgressObject(po);
            po.addProgressListener(handler);
            
            if (isProgressing(po)) {
                // wait until done or cancelled
                boolean done = sleep();
                if (! done) {
                    String msg = NbBundle.getMessage(ServerInstance.class, "MSG_StartProfileTimeout", displayName);
                    throw new ServerException(msg);
                } else if (! hasCommandSucceeded()) {
                    DeploymentStatus status = po.getDeploymentStatus();
                    throw new ServerException(status.getMessage());
                }
            } else if (hasFailed(po)) {
                DeploymentStatus status = po.getDeploymentStatus();
                throw new ServerException(status.getMessage());
            }
            profiledServerInstance = this;
            profilerSettings = settings;
            managerStartedByIde = true;
        } finally {
            if (ui != null) {
                ui.setProgressObject(null);
            }
            if (po != null) {
                po.removeProgressListener(handler);
            }
        }
    }
    
    /** Tell the profiler to shutdown */
    private synchronized void shutdownProfiler(ProgressUI ui) throws ServerException {
        ui.progress(NbBundle.getMessage(ServerInstance.class, "MSG_StoppingProfiler"));
        StartProgressHandler handler = new StartProgressHandler();
        ProgressObject po = null;
        try {
            Profiler profiler = ServerRegistry.getProfiler();
            if (profiler != null) {
                po = profiler.shutdown();
                ui.setProgressObject(po);
                po.addProgressListener(handler);
                if (isProgressing(po)) {
                    // wait until done or cancelled
                    boolean done = sleep();
                    if (!done) {
                        String msg = NbBundle.getMessage(ServerInstance.class, "MSG_ProfilerShutdownTimeout");
                        throw new ServerException(msg);
                    } else if (! hasCommandSucceeded()) {
                        DeploymentStatus status = po.getDeploymentStatus();
                        throw new ServerException(status.getMessage());
                    }
                } else if (hasFailed(po)) {
                    DeploymentStatus status = po.getDeploymentStatus();
                    throw new ServerException(status.getMessage());
                }
            }
        } finally {
            if (ui != null) {
                ui.setProgressObject(null);
            }
            if (po != null) {
                po.removeProgressListener(handler);
            }
        }
    }
    
    // stopDeploymentManager
    private synchronized void _stop(ProgressUI ui) throws ServerException {
        // if the server is started in profile mode, deattach profiler first
        if (profiledServerInstance == this) {
            shutdownProfiler(ui);
            profiledServerInstance = null;
        }
        // if the server is suspended, the debug session has to be terminated first
        if (isSuspended()) {
            Target target = _retrieveTarget(null);
            ServerDebugInfo sdi = getServerDebugInfo(target);
            Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
            for (int i = 0; i < sessions.length; i++) {
                Session s = sessions[i];
                if (s != null) {
                    AttachingDICookie attCookie = (AttachingDICookie)s.lookupFirst(null, AttachingDICookie.class);
                    if (attCookie != null) {
                        if (sdi.getTransport().equals(ServerDebugInfo.TRANSPORT_SHMEM)) {
                            String shmem = attCookie.getSharedMemoryName();
                            if (shmem != null && shmem.equalsIgnoreCase(sdi.getShmemName())) {
                                s.kill();
                            }
                        } else {
                            String host = stripHostName(attCookie.getHostName());
                            if (host != null && host.equalsIgnoreCase(stripHostName(sdi.getHost())) 
                                && attCookie.getPortNumber() == sdi.getPort()) {
                                s.kill();
                            }
                        }
                    }
                }
            }
        }
        
        String displayName = getDisplayName();
        ui.progress(NbBundle.getMessage(ServerInstance.class, "MSG_StoppingServer", displayName));
        
        StartProgressHandler handler = new StartProgressHandler();
        ProgressObject po = null;
        try {
            po = getStartServer().stopDeploymentManager();
            ui.setProgressObject(po);
            po.addProgressListener(handler);
            
            if (isProgressing(po)) {
                // wait until done or cancelled
                boolean done = sleep();
                if (! done) {
                    String msg = NbBundle.getMessage(ServerInstance.class, "MSG_StopServerTimeout", displayName);
                    throw new ServerException(msg);
                } else if (! hasCommandSucceeded()) {
                    DeploymentStatus status = po.getDeploymentStatus();
                    throw new ServerException(status.getMessage());
                }
            } else if (hasFailed(po)) {
                DeploymentStatus status = po.getDeploymentStatus();
                throw new ServerException(status.getMessage());
            }
            managerStartedByIde = false;
            reset();
        } finally {
            if (ui != null) {
                ui.setProgressObject(null);
            }
            if (po != null) {
                po.removeProgressListener(handler);
            }
        }
    }
    
    private void _start(Target target, ProgressUI ui) throws ServerException {
        ServerTarget serverTarget = getServerTarget(target.getName());
        if (serverTarget.isRunning()) {
            return;
        }
        
        String displayName = target.getName();
        ui.progress(NbBundle.getMessage(ServerInstance.class, "MSG_StartingServer", displayName));
        StartProgressHandler handler = new StartProgressHandler();
        ProgressObject po = null;
        
        try {
            setCommandSucceeded(false);
            po = serverTarget.start();
            if (ui != null) {
                ui.setProgressObject(po);
            }
            po.addProgressListener(handler);
            
            if (isProgressing(po)) {
                // wait until done or cancelled
                boolean done = sleep();
                if (! done) {
                    String msg = NbBundle.getMessage(ServerInstance.class, "MSG_StartServerTimeout", displayName);
                    throw new ServerException(msg);
                } else if (! hasCommandSucceeded()) {
                    DeploymentStatus status = po.getDeploymentStatus();
                    throw new ServerException(status.getMessage());
                }
            } else if (hasFailed(po)) {
                DeploymentStatus status = po.getDeploymentStatus();
                throw new ServerException(status.getMessage());
            }
            targetsStartedByIde.add(serverTarget.getName());
        } finally {
            if (ui != null) {
                ui.setProgressObject(null);
            }
            if (po != null) {
                po.removeProgressListener(handler);
            }
        }
    }
    
    private void _stop(Target target, ProgressUI ui) throws ServerException {
        ServerTarget serverTarget = getServerTarget(target.getName());
        if (serverTarget.isRunning()) {
            return;
        }
        
        String displayName = target.getName();
        ui.progress(NbBundle.getMessage(ServerInstance.class, "MSG_StoppingServer", displayName));
        StartProgressHandler handler = new StartProgressHandler();
        ProgressObject po = null;
        
        try {
            setCommandSucceeded(false);
            po = serverTarget.stop();
            if (ui != null) {
                ui.setProgressObject(po);
            }
            po.addProgressListener(handler);
            
            if (isProgressing(po)) {
                // wait until done or cancelled
                boolean done = sleep();
                if (! done) {
                    String msg = NbBundle.getMessage(ServerInstance.class, "MSG_StopServerTimeout", displayName);
                    throw new ServerException(msg);
                } else if (! hasCommandSucceeded()) {
                    DeploymentStatus status = po.getDeploymentStatus();
                    throw new ServerException(status.getMessage());
                }
            } else if (hasFailed(po)) {
                DeploymentStatus status = po.getDeploymentStatus();
                throw new ServerException(status.getMessage());
            }
            targetsStartedByIde.remove(serverTarget.getName());
        } finally {
            if (ui != null) {
                ui.setProgressObject(null);
            }
            if (po != null) {
                po.removeProgressListener(handler);
            }
        }
    }
    
    //-------------- End state-machine operations -------------------
    
    public boolean canStartServer() {
        return this.getStartServer() != null && getStartServer().supportsStartDeploymentManager();
    }
    
    private class StartProgressHandler implements ProgressListener {
        Boolean completed = null;
        public StartProgressHandler() {
        }
        public void handleProgressEvent(ProgressEvent progressEvent) {
            if (completed != null)
                return;
            DeploymentStatus status = progressEvent.getDeploymentStatus();
            if (status.isCompleted()) {
                completed = Boolean.TRUE;
                ServerInstance.this.setCommandSucceeded(true);
                ServerInstance.this.wakeUp();
            } else if (status.isFailed()) {
                completed = Boolean.FALSE;
                ServerInstance.this.wakeUp();
            }
        }
        public boolean isCompleted() {
            if (completed == null)
                return false;
            return completed.booleanValue();
        }
    }
    
    private synchronized void wakeUp() {
        notify();
    }
    
    //return false when timeout or interrupted
    private synchronized boolean sleep() {
        try {
            long t0 = System.currentTimeMillis();
            wait(TIMEOUT);
            return (System.currentTimeMillis() - t0) < TIMEOUT;
        } catch (Exception e) { return false; }
    }
    
    public boolean isManagerOf(Target target) {
        return getTargetMap().keySet().contains(target.getName());
    }
    
    public ServerTarget getCoTarget() {
        return coTarget;
    }
    
    private void initCoTarget() {
        ServerTarget[] childs = getTargets();
        for (int i=0; i<childs.length; i++) {
            if (getStartServer().isAlsoTargetServer(childs[i].getTarget()))
                coTarget = childs[i];
        }
    }
    
    public boolean isDefault() {
        return url.equals(ServerRegistry.getInstance().getDefaultInstance().getUrl());
    }
    
    public String toString() {
        return getDisplayName();
    }
    
    public static boolean isProgressing(ProgressObject po) {
        StateType state = po.getDeploymentStatus().getState();
        return (state == StateType.RUNNING || state == StateType.RELEASED);
    }
    public static boolean hasFailed(ProgressObject po) {
        StateType state = po.getDeploymentStatus().getState();
        return (state == StateType.FAILED);
    }
    
    // StateListener ----------------------------------------------------------
    
    /** Listener that allows to listen to server state changes */
    public static interface StateListener {
        void stateChanged(int oldState, int newState);
    }
    
    public void addStateListener(StateListener sl) {
        synchronized (stateListeners) {
            stateListeners.add(sl);
        }
    }
    
    public void removeStateListener(StateListener sl) {
        synchronized (stateListeners) {
            stateListeners.remove(sl);
        }
    }
    
    private void fireStateChanged(int oldState, int newState) {
        StateListener[] listeners;
        synchronized (stateListeners) {
            listeners = (StateListener[])stateListeners.toArray(new StateListener[stateListeners.size()]);
        }
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].stateChanged(oldState, newState);
        }
    }
    
    // private helper classes & methods ---------------------------------------
    
    private static class ConflictData {
        private ServerInstance si;
        private ServerDebugInfo sdi;
        
        public ConflictData(ServerInstance si, ServerDebugInfo sdi) {
            this.si = si;
            this.sdi = sdi;
        }
        
        public ServerInstance getServerInstance() { 
            return si; 
        }
        
        public ServerDebugInfo getServerDebugInfo() { 
            return sdi; 
        }
    };
    
    /** Safe true/false test useful. */
    private abstract static class SafeTrueTest implements Runnable {
        protected boolean result = false;
        
        public abstract void run();
        
        public final boolean result() {
            return result;
        }
    };
    
    /** Return the result of the test or false if the given time-out ran out. */
    private boolean safeTrueTest(SafeTrueTest test, int timeout) {
        try {
           new RequestProcessor().post(test).waitFinished(timeout);
        } catch (InterruptedException ie) {
            ErrorManager.getDefault().notify(ie);
        } finally {
            return test.result();
        }
    }
    
    private synchronized boolean hasCommandSucceeded() {
        return commandSucceed;
    }
    private synchronized void setCommandSucceeded(boolean val) {
        commandSucceed = val;
    }
    
    private ServerDebugInfo _retrieveDebugInfo(Target target) {
        StartServer ss = getStartServer();
        if (ss == null) {
            return null;
        }
        
        Target t = _retrieveTarget(target);
        ServerDebugInfo sdi = ss.getDebugInfo(t);
        
        if (sdi != null || t != null) {
            debugInfo.remove(t);
            debugInfo.put(t, sdi);//cache debug info for given target
        }
        
        return sdi;
    }
    
    private Target _retrieveTarget(Target target) {
        StartServer ss = getStartServer();
        if (ss == null) {
            return null;
        }
        
        Target t = null;
        
        // Getting targets from AS8.1 requires start server which would hang UI, so avoid start server
        if (! isReallyRunning() && ss.needsStartForTargetList()) {
            if (t == null) {
                for (Iterator it = debugInfo.keySet().iterator(); t == null && it.hasNext(); ) {
                    Target cachedTarget = (Target) it.next();
                    if (ss.isAlsoTargetServer(cachedTarget)) {
                        t = cachedTarget;
                    }
                }
            } else {
                if (ss.isAlsoTargetServer(target))
                    t = target;
            }
        } else {
            ServerTarget[] targets = getTargets();
            for (int i = 0; t == null && i < targets.length; i++) {
                if (ss.isAlsoTargetServer(targets[i].getTarget())) {
                    t = targets[i].getTarget();
                }
            }
            
            if (t == null && targets.length > 0) {
                t = targets[0].getTarget();
            }
        }
        
        return t;
    }

    public int compareTo(Object other) {
        if (!(other instanceof ServerInstance)) {
            throw new IllegalArgumentException();
        }
        return getDisplayName().compareTo(((ServerInstance)other).getDisplayName());
    }
    
    /** Take for example myhost.xyz.org and return myhost */
    private String stripHostName(String host) {
        if (host == null) {
            return null;
        }
        int idx = host.indexOf('.');
        return idx != -1 ? host.substring(0, idx) : host;
    }
    
    /** DebugStatusListener listens to debugger state changes and calls refresh() 
     *  if needed. If the debugger stops at a breakpoint, the server status will
     *  thus change to suspended, etc. */
    private class DebuggerStateListener extends DebuggerManagerAdapter {
            
            private RequestProcessor.Task refreshTask;
            
            public void sessionAdded(Session session) {
                Target target = _retrieveTarget(null);
                ServerDebugInfo sdi = getServerDebugInfo(target);
                if (sdi == null) {
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "DebuggerInfo cannot be found for: " + ServerInstance.this);
                    return; // give it up
                }
                AttachingDICookie attCookie = (AttachingDICookie)session.lookupFirst(null, AttachingDICookie.class);
                if (attCookie == null) {
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "AttachingDICookie cannot be found for: " + ServerInstance.this);
                    return; // give it up
                }
                if (ServerDebugInfo.TRANSPORT_SHMEM.equals(sdi.getTransport())) {
                    String shmem = attCookie.getSharedMemoryName();
                    if (shmem != null && shmem.equalsIgnoreCase(sdi.getShmemName())) {
                        registerListener(session);
                    }
                } else {
                    String host = stripHostName(attCookie.getHostName());                    
                    if (host != null && host.equalsIgnoreCase(stripHostName(sdi.getHost()))) {
                        if (attCookie.getPortNumber() == sdi.getPort()) {
                            registerListener(session);
                        }
                    }
                }
            }
            
            public synchronized void sessionRemoved(Session session) {
                refreshTask = null;
            }
            
            private void registerListener(Session session) {
                final JPDADebugger jpda = (JPDADebugger)session.lookupFirst(null, JPDADebugger.class);
                if (jpda != null) {
                    jpda.addPropertyChangeListener(JPDADebugger.PROP_STATE, new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt) {
                            RequestProcessor.Task task; 
                            synchronized (DebuggerStateListener.this) {
                                if (refreshTask == null) {
                                    refreshTask = RequestProcessor.getDefault().create(new Runnable() {
                                        public void run() {
                                            if (jpda.getState() == JPDADebugger.STATE_STOPPED) {
                                                setServerState(ServerInstance.STATE_SUSPENDED);
                                            } else {
                                                setServerState(ServerInstance.STATE_DEBUGGING);
                                            }
                                        }
                                    });
                                }
                                task = refreshTask;
                            }
                            // group fast arriving refresh calls
                            task.schedule(500);
                        }
                    });
                }
            }
        }
}
