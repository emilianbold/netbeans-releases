/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.j2ee.deployment.impl;

import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.shared.*;
import javax.enterprise.deploy.spi.status.*;
import javax.swing.JButton;

import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.j2ee.deployment.impl.ui.DeployProgressUI;
import org.netbeans.modules.j2ee.deployment.impl.ui.DeployProgressMonitor;
import org.openide.filesystems.*;
import java.util.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.openide.nodes.Node;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.netbeans.modules.j2ee.deployment.impl.ui.ServerStatusBar;
import org.openide.util.RequestProcessor;

public class ServerInstance implements Node.Cookie {
    
    private static final String EMPTY_STRING = ""; //NOI18N
    private String state = EMPTY_STRING; 

    private final String url;
    private final Server server;
    private DeploymentManager manager;
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
    private ServerStatusBar serverStatusBar = null;
    
    private static class ConflictData {
        private String transport;
        private ServerInstance si;
        public ConflictData(ServerInstance si, String transport) {
            this.si = si;
            assert transport.equals(ServerDebugInfo.TRANSPORT_SOCKET) ||
                   transport.equals(ServerDebugInfo.TRANSPORT_SHMEM);
            this.transport = transport;
        }
        public String getTransport() { return transport; }
        public ServerInstance getServerInstance() { return si; }
    };
        
    // PENDING how to manage connected/disconnected servers with the same manager?
    // maybe concept of 'default unconnected instance' is broken?
    public ServerInstance(Server server, String url, DeploymentManager manager) {
        this.server = server; 
        this.url = url; 
        this.manager = manager;
        instanceProperties = new InstancePropertiesImpl(url);
    }
    
    /**
     * Return this server instance <code>InstanceProperties</code>.
     * 
     * @return this server instance <code>InstanceProperties</code>.
     */
    public InstancePropertiesImpl getInstanceProperties() {
        return instanceProperties;
    }
    
    /**
     * Set a new display name for this server instance.
     *
     * @param name new display name.
     */
    public void setDisplayName(String name) {
        instanceProperties.setProperty(InstanceProperties.DISPLAY_NAME_ATTR, name);
    }
    
    /**
     * Return display name which represents this server instance.
     *
     * @return display name which represents this server instance.
     */
    public String getDisplayName() {
        return instanceProperties.getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
    }
    
    /**
     * Return display name which represents this server instance followed by its
     * state [running/stopped].
     *
     * @return display name which represents this server instance followed by its
     *         state [running/stopped].
     */
    public String getDisplayNameWithState() {
        return getDisplayName() + " " + state; // NOI18N
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
        }
        catch(javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException e) {
            throw new RuntimeException(e);
        }
        return manager;
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
                j2eePlatformImpl = fact.getJ2eePlatformImpl(getDeploymentManager());
            }
        }
        return j2eePlatformImpl;
    }
    
    public void refresh(ServerState serverState) {
        state = serverState.getMessage();
        if (serverState == ServerState.RUNNING) {
            initCoTarget();
        } else if (serverState == ServerState.STOPPED) {
            reset();
        }
        fireInstanceRefreshed(serverState);
    }
    
    public void reset() {
        if (manager != null) {
            manager.release();
            manager = null;
        }
        incrementalDeployment = null;
        tmidResolver = null;
        startServer = null;
        findJSPServlet = null;
        coTarget = null;
        targets = null;
    }
    
    public void remove() {
        String displayName = getDisplayName();
        String title = NbBundle.getMessage(ServerInstance.class, "LBL_StopServerProgressMonitor", displayName);
        DeployProgressUI ui = new DeployProgressMonitor(title, false, true);  // modeless with stop/cancel buttons
        
        for (Iterator i=targetsStartedByIde.iterator(); i.hasNext(); ){
            String targetName = (String) i.next();
            ServerTarget serverTarget = getServerTarget(targetName);
            if (serverTarget != null) {
                _stop(serverTarget.getTarget(), ui);
            }
        }
        
        stop(ui);
        ServerRegistry.getInstance().removeServerInstance(getUrl());
    }
    
    /**
     * Is it forbidden to remove this server instance from the server registry?
     *
     * @return <code>true</code> if this server instance is not allowed to be removed
     *         from the server registry, <code>false</code> otherwise.
     */
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
            if(targs == null)
                targs = new Target[0];
            
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
            startServer = server.getOptionalFactory ().getStartServer (getDeploymentManager ());
        }
        return startServer;
    }
    
    public IncrementalDeployment getIncrementalDeployment() {
        if (incrementalDeployment == null) {
            incrementalDeployment = server.getOptionalFactory ().getIncrementalDeployment (getDeploymentManager ());
        }
        return incrementalDeployment;
    }
    
    public TargetModuleIDResolver getTargetModuleIDResolver() {
        if (tmidResolver == null) {
            tmidResolver = server.getOptionalFactory ().getTargetModuleIDResolver(getDeploymentManager ());
        }
        return tmidResolver;
    }

    public FindJSPServlet getFindJSPServlet() {
        if (findJSPServlet == null) {
            findJSPServlet = server.getOptionalFactory().getFindJSPServlet (getDeploymentManager ());
        }
        return findJSPServlet;
    }

    //---------- State API's:  running, debuggable, startedByIDE -----------
    long lastCheck = 0;
    boolean isRunning = false;
    public boolean isRunningLastCheck() {
        if (lastCheck > 0)
            return isRunning;
        else
            return false;
    }
    public boolean isReallyRunning() {
        return isRunningWithinMillis(0);
    }
    public boolean isRunning() {
        return isRunningWithinMillis(2000);
    }
    public boolean isRunningWithinMillis(long millisecs) {
        if (System.currentTimeMillis() - lastCheck < millisecs) 
            return isRunning;
        StartServer ss = getStartServer();
        try {
            boolean state = (ss != null && ss.isRunning());
            if (isRunning != state) {
                isRunning = state;
                refresh(state ? ServerState.RUNNING 
                              : ServerState.STOPPED);
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            isRunning = false;
        }
        lastCheck = System.currentTimeMillis();
        return isRunning;
    }
    
    public boolean isDebuggable() {
        StartServer ss = getStartServer();
        Target target = getDeploymentManager().getTargets()[0];
        return ss != null && ss.isAlsoTargetServer(target) && ss.isDebuggable(target);
    }
    
    public boolean isDebuggable(Target target) {
        StartServer ss = getStartServer();
        return ss != null && ss.isDebuggable(target);
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
            return cd; //attempt to start server (serverInstance remains null)
        ServerDebugInfo thisSDI = thisSS.getDebugInfo(target);
        //should not occur -> workaround for issue #56714
        if (thisSDI == null)
            return cd;
        
        //get all server instances
        ServerInstance[] serverInstances = ServerRegistry.getInstance().getServerInstances();
        //check existence of a server instance running in debug mode with the same parameters
        for (int i = 0; cd == null && i < serverInstances.length; i++) {
            ServerInstance si = serverInstances[i];
            if (url.equalsIgnoreCase(si.getUrl())) continue;
            if (si.isDebuggable(target)) { //running in debug mode
                ServerDebugInfo sdi = si.getStartServer().getDebugInfo(target);
                if (thisSDI.getTransport().equals(sdi.getTransport())) { //transport matches
                    if (thisSDI.getTransport() == ServerDebugInfo.TRANSPORT_SOCKET) {
                        if (thisSDI.getHost().equalsIgnoreCase(sdi.getHost())) //host matches
                            if (thisSDI.getPort() == sdi.getPort()) //port matches
                                cd = new ConflictData(si, ServerDebugInfo.TRANSPORT_SOCKET);
                    }
                    else if (thisSDI.getShmemName().equalsIgnoreCase(sdi.getShmemName()))
                        cd = new ConflictData(si, ServerDebugInfo.TRANSPORT_SHMEM);
                }
            }
        }
        
        return cd;
    }
    
    public boolean startedByIde() {
        return managerStartedByIde;
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
    // Note: configuration needs
    /**
     * Return a connected DeploymentManager if needed by server platform for configuration
     * @return DeploymentManager object for configuration.
     */
    public DeploymentManager getDeploymentManagerForConfiguration() {
        StartServer ss = getStartServer();
        if (ss != null && ss.needsStartForConfigure())
            start();
        
        return getDeploymentManager();
    }
    
    // Note: execution only need these 3 state transition APIs
    
    /**
     * Start the admin server.
     * Note: for debug mode, always use startDebugTarget() calls because
     * it is sure then the target need to be started.
     * @return true if successful.
     */
    public boolean start(DeployProgressUI ui) {
        return startTarget(null, ui);
    }

    /**
     * Start specified target server.  If it is also admin server only make sure
     * admin server is running.
     * @param target target server to be started
     * @param ui DeployProgressUI to display start progress
     * @return true when server is started successfully; else return false.
     */
    public boolean startTarget(Target target, DeployProgressUI ui) {
        return startTarget(target, ui, false);
    }
    
    /**
     * Start specified target server in debug mode.  If target is also admin
     * server only make sure admin server is running.
     * @param target target server to be started
     * @param ui DeployProgressUI to display start progress
     * @return true when server is started successfully; else return false.
     */
    public boolean startDebugTarget(Target target, DeployProgressUI ui) {
        return startTarget(target, ui, true);
    }
    
    /**
     * Start admin server, mainly for registry actions with no existing progress UI
     */
    private void start() {
        if (isRunning())
            return;
        
        if (! RequestProcessor.getDefault().isRequestProcessorThread()) {
            //PENDING maybe a modal dialog instead of async is needed here
            RequestProcessor.Task t = RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    start(null);
                }
            }, 0, Thread.MAX_PRIORITY);
        } else {            
            String title = NbBundle.getMessage(ServerInstance.class, "LBL_StartServerProgressMonitor", getDisplayName());
            final DeployProgressUI ui = new DeployProgressMonitor(title, false, true);  // modeless with stop/cancel buttons
            ui.startProgressUI(5);
            if (start(ui)) {
                ui.recordWork(5);
            }
        }
    }
    
    /**
     * Stop admin server.
     */
    public void stop(DeployProgressUI ui) {
        if (isReallyRunning()) {
            _stop(ui);
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
     * @return true if conflict was resolved successfully, i.e. current server was stopped.
     */
    private boolean _resolveServerConflict(Target target, DeployProgressUI ui, ConflictData cd) {
        
        StartServer ss = getStartServer();
        assert(ss != null);
        
        ServerInstance si = cd.getServerInstance();
        //inform a user and allow him to stop the running instance
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(
                            ServerInstance.class, 
                            "MSG_AnotherServerRunning",
                            new Object[] {
                                si.getDisplayName(),
                                ss.getDebugInfo(target).getHost(),
                                cd.getTransport().equals(ServerDebugInfo.TRANSPORT_SOCKET) ?
                                    "socket" : "shared memory",
                                cd.getTransport().equals(ServerDebugInfo.TRANSPORT_SOCKET) ?
                                    new Integer(ss.getDebugInfo(target).getPort()).toString() : ss.getDebugInfo(target).getShmemName()
                            }),
                NotifyDescriptor.QUESTION_MESSAGE
                );
        nd.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
        JButton yes = new JButton(NbBundle.getMessage(ServerInstance.class, "MSG_AnotherServerStopYes"));
        JButton no = new JButton(NbBundle.getMessage(ServerInstance.class, "MSG_AnotherServerStopNo"));
        yes.setDefaultCapable(true);
        nd.setOptions(new Object[] { yes, no });
        Object option = DialogDisplayer.getDefault().notify(nd);
        if (option != yes) //user pressed No
            return false;
        //try to stop running server
        if (si.getStartServer().supportsStartDeploymentManager()) {
            if (!si._stop(ui)) //stopping unsuccessful =>
                return false;  //starting target server impossible
        } else {
            return si.errorCannotControlAdmin(ui);
        }
        
        return true;
    }
            
    // multiplexor state-machine core
    private boolean startTarget(Target target, DeployProgressUI ui, boolean debugMode) {
        
        StartServer ss = getStartServer();
        
        // No StartServer, have to assume manually started
        if (ss == null) {
            ui.addMessage(NbBundle.getMessage(ServerInstance.class, "MSG_PluginHasNoStartServerClass", getServer()));
            return true;
        }
        
        boolean canControlAdmin = ss.supportsStartDeploymentManager();
        boolean needsRestart = ss.needsRestart(target);
        
        if (ss.isAlsoTargetServer(target)) {
            if (debugMode) { 
                if (ss.isDebuggable(target)) { // implies ss.isRunning() true
                    if (! needsRestart) {
                        return true;
                    }
                    if (! canControlAdmin || ! _stop(ui)) {
                        return errorCannotControlAdmin(ui);
                    }
                    
                } else if (isReallyRunning()) { // running but not debuggable
                    if (canControlAdmin) {
                        if (! _stop(ui))
                            return false;
                    } else {
                        return errorCannotControlAdmin(ui);
                    }
                }
                ConflictData cd = anotherServerDebuggable(target);
                if (cd != null) //another server instance with the same parameters
                    if (!_resolveServerConflict(target, ui, cd))
                        return false;
                
                if (canControlAdmin) {
                    return _startDebug(target, ui);
                } else {
                    return errorCannotControlAdmin(ui);
                }
            } else {
                if (isReallyRunning()) {
                    if (! needsRestart) {
                        return true;
                    } 
                    if (! canControlAdmin || ! _stop(ui)) {
                        return errorCannotControlAdmin(ui);
                    }
                }
                if (canControlAdmin) {
                    return _start(ui);
                } else {
                    return errorCannotControlAdmin(ui);
                }
            }
        } else { // not also target server
            // this block ensure a running admin server to control other targets
            if (! isReallyRunning()) {  
                if (canControlAdmin) {
                    if (! _start(ui)) {
                        return false;
                    }
                } else {
                    return errorCannotControlAdmin(ui);
                }
            }
            if (debugMode) {
                if (ss.isDebuggable(target)) {
                    if ( ! needsRestart) {
                        return true;
                    }
                    if (! _stop(target, ui)) {
                        return false;
                    }
                } else if (ss.isRunning(target)) {
                    if (! _stop(target, ui)) {
                        return false;
                    }
                }
                ConflictData cd = anotherServerDebuggable(target);
                if (cd != null) //another server instance with the same parameters
                    if (!_resolveServerConflict(target, ui, cd))
                        return false;
                
                return _startDebug(target, ui);
            } else {
                if (ss.isRunning(target)) {
                    if (! needsRestart) {
                        return true;
                    }
                    if ( ! _stop(target, ui)) {
                        return false;
                    }
                }
                return _start(target, ui);
            }
        }
    }
    
    //------------------------------------------------------------
    // state-transition atomic operations (always do-it w/o checking state)
    //------------------------------------------------------------
    // startDeploymentManager
    private synchronized boolean _start(DeployProgressUI ui) {
        String displayName = getDisplayName();
        output(ui, NbBundle.getMessage(ServerInstance.class, "MSG_StartingServer", displayName));

        DeployProgressUI.CancelHandler ch = getCancelHandler();
        ProgressObject po = null;
        StartProgressHandler handler = new StartProgressHandler();
        if (ui != null) {
            ui.addCancelHandler(ch);
        }
        
        try {
            setCommandSucceeded(false);
            po = getStartServer().startDeploymentManager();
            if (ui != null) {
                ui.setProgressObject(po);
            }
            po.addProgressListener(handler);
            
            String error = null;
            if (isProgressing(po)) {
                // wait until done or cancelled
                boolean done = sleep();
                if (! done) {
                    error = NbBundle.getMessage(ServerInstance.class, "MSG_StartServerTimeout", displayName);
                } else if (! hasCommandSucceeded()) {
                    return false;
                } else if (ui != null && ui.checkCancelled()) {
                    error = NbBundle.getMessage(ServerInstance.class, "MSG_StartServerCanceled");
                }
            } else if (hasFailed(po)) {
                return false;
            }
            
            if (error != null) {
                outputError(ui, error);
                return false;
            }

            managerStartedByIde = true;
            refresh(ServerState.RUNNING);
            return true;
            
        } finally {
            if (ui != null) {
                ui.removeCancelHandler(ch);
                ui.setProgressObject(null);
            }
            if (po != null) {
                po.removeProgressListener(handler);
            }
        }
    }

    // startDebugging
    private synchronized boolean _startDebug(Target target, DeployProgressUI ui) {
        String displayName = getDisplayName();
        output(ui, NbBundle.getMessage(ServerInstance.class, "MSG_StartingDebugServer", displayName));
        
        DeployProgressUI.CancelHandler ch = getCancelHandler();
        ProgressObject po = null;
        StartProgressHandler handler = new StartProgressHandler();
        if (ui != null) {
            ui.addCancelHandler(ch);
        }
        
        try {
            setCommandSucceeded(false);
            po = getStartServer().startDebugging(target);
            if (ui != null) {
                ui.setProgressObject(po);
            }
            po.addProgressListener(handler);
            
            String error = null;
            if (isProgressing(po)) {
                // wait until done or cancelled
                boolean done = sleep();
                if (! done) {
                    error = NbBundle.getMessage(ServerInstance.class, "MSG_StartDebugTimeout", displayName);
                } else if (! hasCommandSucceeded()) {
                    return false;
                } else if (ui != null && ui.checkCancelled()) {
                    error = NbBundle.getMessage(ServerInstance.class, "MSG_StartDebugCancelled");
                }
            } else if (hasFailed(po)) {
                return false;
            }

            if (error != null) {
                outputError(ui, error);
                return false;
            }

            managerStartedByIde = true;
            refresh(ServerState.RUNNING);
            return true;
            
        } finally {
            if (ui != null) {
                ui.removeCancelHandler(ch);
                ui.setProgressObject(null);
            }
            if (po != null) {
                po.removeProgressListener(handler);
            }
        }
    }
    // stopDeploymentManager
    private synchronized boolean _stop(DeployProgressUI ui) {
        String displayName = getDisplayName();
        output(ui, NbBundle.getMessage(ServerInstance.class, "MSG_StoppingServer", displayName));
        
        DeployProgressUI.CancelHandler ch = getCancelHandler();
        StartProgressHandler handler = new StartProgressHandler();
        ProgressObject po = null;
        if (ui != null) {
            ui.addCancelHandler(ch);
        }
        
        try {
            setCommandSucceeded(false);
            po = getStartServer().stopDeploymentManager();
            if (ui != null) {
                ui.setProgressObject(po);
            }
            po.addProgressListener(handler);
            
            String error = null;
            if (isProgressing(po)) {
                // wait until done or cancelled
                boolean done = sleep();
                if (! done) {
                    error = NbBundle.getMessage(ServerInstance.class, "MSG_StopServerTimeout", displayName);
                } else if (ui != null && ui.checkCancelled()) {
                    error = NbBundle.getMessage(ServerInstance.class, "MSG_StopServerCancelled", displayName);
                } else if (! hasCommandSucceeded()) {
                    return false;
                }
            } else if (hasFailed(po)) {
                return false;
            }
            
            if (error != null) {
                outputError(ui, error);
                return false;
            }
            
            managerStartedByIde = false;
            refresh(ServerState.STOPPED);
            return true;
            
        } finally {
            if (ui != null) {
                ui.removeCancelHandler(ch);
                ui.setProgressObject(null);
            }
            if (po != null) {
                po.removeProgressListener(handler);
            }
        }
    }
    
    private boolean _start(Target target, DeployProgressUI ui) {
        ServerTarget serverTarget = getServerTarget(target.getName());
        if (serverTarget.isRunning())
            return true;
        
        String displayName = target.getName();
        output(ui, NbBundle.getMessage(ServerInstance.class, "MSG_StartingServer", displayName));
        DeployProgressUI.CancelHandler ch = getCancelHandler();
        StartProgressHandler handler = new StartProgressHandler();
        ProgressObject po = null;
        if (ui != null) {
            ui.addCancelHandler(ch);
        }
        
        try {
            setCommandSucceeded(false);
            po = serverTarget.start();
            if (ui != null) {
                ui.setProgressObject(po);
            }
            po.addProgressListener(handler);
            
            String error = null;
            if (isProgressing(po)) {
                // wait until done or cancelled
                boolean done = sleep();
                if (! done) {
                    error = NbBundle.getMessage(ServerInstance.class, "MSG_StartServerTimeout", displayName);
                } else if (ui != null && ui.checkCancelled()) {
                    error = NbBundle.getMessage(ServerInstance.class, "MSG_StartServerCancelled", displayName);
                } else if (! hasCommandSucceeded()) {
                    return false;
                }
            } else if (hasFailed(po)) {
                return false;
            }
            
            if (error != null) {
                outputError(ui, error);
                return false;
            } else {
                targetsStartedByIde.add(serverTarget.getName());
                return true;
            }
            
        } finally {
            if (ui != null) {
                ui.removeCancelHandler(ch);
                ui.setProgressObject(null);
            }
            if (po != null) {
                po.removeProgressListener(handler);
            }
        }
    }
    
    private boolean _stop(Target target, DeployProgressUI ui) {
        ServerTarget serverTarget = getServerTarget(target.getName());
        if (serverTarget.isRunning())
            return true;
        
        String displayName = target.getName();
        output(ui, NbBundle.getMessage(ServerInstance.class, "MSG_StoppingServer", displayName));
        DeployProgressUI.CancelHandler ch = getCancelHandler();
        StartProgressHandler handler = new StartProgressHandler();
        ProgressObject po = null;
        if (ui != null) {
            ui.addCancelHandler(ch);
        }
        
        try {
            setCommandSucceeded(false);
            po = serverTarget.stop();
            if (ui != null) {
                ui.setProgressObject(po);
            }
            po.addProgressListener(handler);
            
            String error = null;
            if (isProgressing(po)) {
                // wait until done or cancelled
                boolean done = sleep();
                if (! done) {
                    error = NbBundle.getMessage(ServerInstance.class, "MSG_StopServerTimeout", displayName);
                } else if (ui != null && ui.checkCancelled()) {
                    error = NbBundle.getMessage(ServerInstance.class, "MSG_StopServerCancelled", displayName);
                } else if (! hasCommandSucceeded()) {
                    return false;
                }
            } else if (hasFailed(po)) {
                return false;
            }
            
            if (error != null) {
                outputError(ui, error);
                return false;
            } else {
                targetsStartedByIde.remove(serverTarget.getName());
                return true;
            }
            
        } finally {
            if (ui != null) {
                ui.removeCancelHandler(ch);
                ui.setProgressObject(null);
            }
            if (po != null) {
                po.removeProgressListener(handler);
            }
        }
    }
    
    //-------------- End state-machine operations -------------------
    public void reportError(String errorText) {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errorText));
    }
    private void output(DeployProgressUI ui, String msg) {
        if (ui != null) {
            ui.addMessage(msg);
        } else {
            showStatusText(msg);
        }
    }
    private void outputError(DeployProgressUI ui, String msg) {
        if (ui != null) {
            ui.addError(msg);
        } else {
            showStatusText(msg);
        }
    }
    private String checkStartServer(StartServer ss) {
        if (ss == null)
            return NbBundle.getMessage(ServerInstance.class, "MSG_PluginHasNoStartServerClass", getServer());
        return null;
    }
    private boolean errorCannotControlAdmin(DeployProgressUI ui) {
        outputError(ui, NbBundle.getMessage(ServerInstance.class, "MSG_StartingThisServerNotSupported", getDisplayName()));
        return false;
    }
    
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
    private DeployProgressUI.CancelHandler getCancelHandler() {
        DeployProgressUI.CancelHandler ch = new DeployProgressUI.CancelHandler() {
            public void handle() {
                ServerInstance.this.wakeUp();
            }
        };
        return ch;
    }
    private synchronized void wakeUp() {
        notify();
    }
    private synchronized void sleepTillCancel() {
        try {
            wait();
        } catch (Exception e) { }
    }
    private static long TIMEOUT = 180000;
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
    
    public static interface RefreshListener {
        public void handleRefresh(ServerState serverState) ;
    }
    
    Vector rListeners = new Vector();
    public void addRefreshListener(RefreshListener rl) {
        rListeners.add(rl);
    }
    public void removeRefreshListener(RefreshListener rl) {
        rListeners.remove(rl);
    }
    private void fireInstanceRefreshed(ServerState serverState) {
        for (Iterator i=rListeners.iterator(); i.hasNext();) {
            RefreshListener rl = (RefreshListener) i.next();
            rl.handleRefresh(serverState);
        }
    }
    private void showStatusText(String msg) {
        org.openide.awt.StatusDisplayer.getDefault().setStatusText(msg);
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
    
    public String toString () {
        return getDisplayName ();
    }
    
    public static boolean isProgressing(ProgressObject po) {
        StateType state = po.getDeploymentStatus().getState();
        return (state == StateType.RUNNING || state == StateType.RELEASED);
    }
    public static boolean hasFailed(ProgressObject po) {
        StateType state = po.getDeploymentStatus().getState();
        return (state == StateType.FAILED);
    }
    
    private synchronized boolean hasCommandSucceeded() {
        return commandSucceed;
    }
    private synchronized void setCommandSucceeded(boolean val) {
        commandSucceed = val;
    }
    
    public void setServerStatusBar(ServerStatusBar serverStatusBar) {
        this.serverStatusBar = serverStatusBar;
    }
    
    public ServerStatusBar getServerStatusBar() {
        return serverStatusBar;
    }
    
}
