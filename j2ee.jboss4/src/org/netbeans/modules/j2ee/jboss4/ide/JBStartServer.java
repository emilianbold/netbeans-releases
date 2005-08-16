/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.jboss4.ide;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentFactory;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBInstantiatingIterator;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.File;
import java.io.FileInputStream;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.plugins.api.StartServer;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;
import java.util.Vector;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import org.openide.util.NbBundle;
import javax.naming.*;
import java.net.URLClassLoader;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginUtils;

/**
 *
 * @author Kirill Sorokin <Kirill.Sorokin@Sun.COM>
 */
public class JBStartServer extends StartServer implements ProgressObject{
    
    private JBDeploymentManager dm;
    private static Map isDebugModeUri = Collections.synchronizedMap((Map)new HashMap(2,1));
    
    public JBStartServer(DeploymentManager dm) {
        if (!(dm instanceof JBDeploymentManager)) {
            throw new IllegalArgumentException("");
        }
        this.dm = (JBDeploymentManager) dm;
    }
    
    public ProgressObject startDebugging(Target target) {
        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.RUNNING, NbBundle.getMessage(JBStartServer.class, "MSG_START_SERVER_IN_PROGRESS"))); //NOI18N
        RequestProcessor.getDefault().post(new JBStartRunnable(true), 0, Thread.NORM_PRIORITY);
        isDebugModeUri.put(dm.getUrl(), new Object());
        return this;
    }
    
    public boolean isDebuggable(Target target) {
        if (!isDebugModeUri.containsKey(dm.getUrl())) {
            return false;
        }
        if (!isRunning()) {
            isDebugModeUri.remove(dm.getUrl());
            return false;
        }
        return true;
    }
    
    public boolean supportsStartDebugging(Target target) {
        return true;
    }
    
    public boolean isAlsoTargetServer(Target target) {
        return true;
    }
    
    public ServerDebugInfo getDebugInfo(Target target) {
        return new ServerDebugInfo("localhost", dm.getDebuggingPort());
    }
    
    
    
    /**
     * Indicates whether this server supports start/stop.
     *
     * @return true/false - supports/does not support
     */
    public boolean supportsStartDeploymentManager() {
        return true;
    }
    
    /**
     * Stops the server.
     */
    public ProgressObject stopDeploymentManager() {
        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.RUNNING, NbBundle.getMessage(JBStartServer.class, "MSG_STOP_SERVER_IN_PROGRESS")));//NOI18N
        RequestProcessor.getDefault().post(new JBStopRunnable(), 0, Thread.NORM_PRIORITY);
        isDebugModeUri.remove(dm.getUrl());
        return this;
    }
    
    /**
     * Starts the server
     */
    public ProgressObject startDeploymentManager() {
        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.RUNNING, NbBundle.getMessage(JBStartServer.class, "MSG_START_SERVER_IN_PROGRESS")));//NOI18N
        RequestProcessor.getDefault().post(new JBStartRunnable(false), 0, Thread.NORM_PRIORITY);
        isDebugModeUri.remove(dm.getUrl());
        return this;
    }
    
    
    public boolean needsStartForTargetList() {
        return false;
    }
    
    public boolean needsStartForConfigure() {
        return false;
    }
    
    public boolean needsStartForAdminConfig() {
        return false;
    }
    
    private boolean isReallyRunning(){
        InstanceProperties ip = InstanceProperties.getInstanceProperties(dm.getUrl());
        if (ip == null) {
            return false; // finish, it looks like this server instance has been unregistered
        }
        
        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        URLClassLoader loader = ((JBDeploymentFactory)JBDeploymentFactory.create()).getJBClassLoader();
        
        Thread.currentThread().setContextClassLoader(loader);
        java.util.Hashtable env = new java.util.Hashtable();
        
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
        env.put(Context.PROVIDER_URL, "jnp://localhost:"+JBPluginUtils.getJnpPort(ip.getProperty(JBInstantiatingIterator.PROPERTY_SERVER_DIR)));
        env.put(Context.OBJECT_FACTORIES, "org.jboss.naming");
        env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces" );
        env.put("jnp.disableDiscovery", Boolean.TRUE); // NOI18N
        
        String checkingConfigName = ip.getProperty("server");
        
        try{
            InitialContext ctx = new InitialContext(env);
            Object srv = ctx.lookup("/jmx/invoker/RMIAdaptor");
            java.lang.reflect.Method  method = loader.loadClass("javax.management.ObjectName").getMethod("getInstance", new Class[] {String.class} );
            Object target = method.invoke(null, new Object[]{"jboss.system:type=ServerConfig"});
            Object serverName = srv.getClass().getMethod("getAttribute", new Class[]{loader.loadClass("javax.management.ObjectName"),String.class}).invoke(srv, new Object[]{target, "ServerName"});
            String configName = (String)serverName;
            //temporary debug message
            if (checkingConfigName.equals(configName)){
                return true;
            }else{
                return false;
            }
        }catch(NameNotFoundException e){
            if (checkingConfigName.equals("minimal"))
                return true;
            else
                return false;
        }catch(Exception e){
            return false;
        } finally{
            Thread.currentThread().setContextClassLoader(oldLoader);
        }
        // return true;
    }
    
    public boolean isRunning() {
        //     String host = dm.getHost();
        //   int port = dm.getPort();
        
        if (!isReallyRunning()){
            return false;
        }
        
        String url = dm.getUrl();
        
        // Show log if server started and log not shown
        // create an output tab for the server output
        InputOutput io = UISupport.getServerIO(url);
        if (io != null) {
            io.select();
        }
        
        InstanceProperties ip = InstanceProperties.getInstanceProperties(url);
        if (ip == null) {
            return false; // finish, it looks like this server instance has been unregistered
        }
        
        String logFileName = (String)ip.getProperty(JBInstantiatingIterator.PROPERTY_SERVER_DIR) + File.separator+"log"+ File.separator+"server.log" ;//NOI18N
        File logFile = new File(logFileName);
        if (logFile.exists()){
            try{
                String serverName = (String)ip.getProperty(JBInstantiatingIterator.PROPERTY_SERVER) ;
                JBLogWriter logWriter = JBLogWriter.createInstance(io, new FileInputStream(logFile),serverName,true);
                dm.setLogWriter(logWriter);
                if (!logWriter.isRunning())
                    RequestProcessor.getDefault().post(logWriter, 0, Thread.NORM_PRIORITY);
            }catch(Exception e){
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }

        }else{
            return false;
        }
        
        return true;
    }
    
    private class JBStartRunnable implements Runnable {
        
        private boolean debug;
        private  String JBOSS_INSTANCE ="";
        
        public JBStartRunnable(boolean debug) {
            JBOSS_INSTANCE = InstanceProperties.getInstanceProperties(dm.getUrl()).getProperty("server");
            this.debug = debug;
        }
        
        public void run() {
            try {
                Process serverProcess = null;
                String serverLocation = JBPluginProperties.getInstance().getInstallLocation();
                
                String serverRunFileName = serverLocation + (Utilities.isWindows() ? STARTUP_BAT : STARTUP_SH); 
                
                File serverRunFile = new File(serverRunFileName);
                
                if (!serverRunFile.exists()){
                    fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.FAILED, NbBundle.getMessage(JBStartServer.class, "MSG_START_SERVER_FAILED_FNF")));//NOI18N
                    return;
                }
                
                org.openide.execution.NbProcessDescriptor pd = new org.openide.execution.NbProcessDescriptor(serverRunFileName, "-c "+JBOSS_INSTANCE);
                
                String envp[];
                
                if (debug) {
                    envp = new String[]{"JAVA_OPTS=-classic -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address="+dm.getDebuggingPort()+ ",server=y,suspend=n"};
                } else {
                    envp = new String[0];
                }
                
                serverProcess = pd.exec(null, envp, true, null );
                
                fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.RUNNING, NbBundle.getMessage(JBStartServer.class, "MSG_START_SERVER_IN_PROGRESS")));//NOI18N
                
                // create an output tab for the server output
                InputOutput io = UISupport.getServerIO(dm.getUrl());                
                if (io == null) {
                    return; // finish, it looks like this server instance has been unregistered
                }
                
                // clear the old output
                try {
                    io.getOut().reset();
                } catch (IOException ioe) {
                    // no op
                }
                io.select();
                
                // read the data from the server's output up
                LineNumberReader reader = new LineNumberReader(new InputStreamReader(serverProcess.getInputStream()));
                
                try {
                    int timeout = 900000;
                    int elapsed = 0;
                    while (elapsed < timeout) {
                        while (reader.ready()) {
                            String line = reader.readLine();
                            
                            io.getOut().write(line + "\n"); //NOI18N
                            
                            if (line.matches("\\d\\d:\\d\\d:\\d\\d,\\d\\d\\d INFO  \\[Server\\] Starting JBoss \\(MX MicroKernel\\)\\.\\.\\.")) {
                                fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.RUNNING, NbBundle.getMessage(JBStartServer.class, "MSG_START_SERVER_IN_PROGRESS")));//NOI18N
                            }
                            
                            if (line.matches("\\d\\d:\\d\\d:\\d\\d,\\d\\d\\d INFO  \\[Server\\] Core system initialized")) {
                                fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.RUNNING, NbBundle.getMessage(JBStartServer.class, "MSG_START_SERVER_IN_PROGRESS")));//NOI18N
                            }
                            
                            if (line.matches("\\d\\d:\\d\\d:\\d\\d,\\d\\d\\d INFO  \\[Catalina\\] Server startup in [0-9]+ ms")) {
                                fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.RUNNING, NbBundle.getMessage(JBStartServer.class, "MSG_START_SERVER_IN_PROGRESS")));//NOI18N
                            }
                            
                            if (line.matches("\\d\\d:\\d\\d:\\d\\d,\\d\\d\\d INFO  \\[Server\\] JBoss \\(MX MicroKernel\\) \\[4\\.0[\\S]+ \\(build: CVSTag=[\\S]+ date=[\\d]+\\)\\] Started in [\\d]+s:[\\d]+ms")) {//NOI18N
                                fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.COMPLETED, NbBundle.getMessage(JBStartServer.class, "MSG_SERVER_STARTED")));//NOI18N
                                // start logging
                                JBLogWriter logWriter = JBLogWriter.updateInstance(io, serverProcess.getInputStream(),JBOSS_INSTANCE);
                                dm.setLogWriter(logWriter);
                                RequestProcessor.getDefault().post(logWriter, 0, Thread.NORM_PRIORITY);
                                return;
                            }
                            
                            if (line.indexOf("Shutdown complete")>-1) {
                                fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.FAILED, NbBundle.getMessage(JBStartServer.class, "MSG_START_SERVER_FAILED")));//NOI18N
                                return;
                            }

                        }
                        
                        try {
                            elapsed += 500;
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            // do nothing
                        }
                    }
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
                
                fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.FAILED, NbBundle.getMessage(JBStartServer.class, "MSG_START_SERVER_FAILED")));//NOI18N
                
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        private final static String STARTUP_SH = "/bin/run.sh";
        private final static String STARTUP_BAT = "/bin/run.bat";
    }
    
    private class JBStopRunnable implements Runnable {
        
        public void run() {
            String configName = InstanceProperties.getInstanceProperties(dm.getUrl()).getProperty("server");
            if ("minimal".equals(configName)) {
                fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.FAILED, NbBundle.getMessage(JBStartServer.class, "MSG_STOP_SERVER_FAILED_MINIMAL")));//NOI18N
                return;
            }
            try {
                String serverLocation = JBPluginProperties.getInstance().getInstallLocation();
                String serverStopFileName = serverLocation + (Utilities.isWindows() ? SHUTDOWN_BAT : SHUTDOWN_SH);
                
                File serverStopFile = new File(serverStopFileName);
                if (!serverStopFile.exists()){
                    fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.FAILED, NbBundle.getMessage(JBStartServer.class, "MSG_STOP_SERVER_FAILED_FNF")));//NOI18N
                    return;
                }
                
                Process serverProcess = Runtime.getRuntime().exec(serverStopFileName+" --shutdown");
                fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.RUNNING,
                        NbBundle.getMessage(JBStartServer.class, "MSG_STOP_SERVER_IN_PROGRESS")));
                
                
                
                int timeout = 300000;
                int elapsed = 0;
                while (elapsed < timeout) {
                    if (isRunning()) {
                        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.RUNNING,
                                NbBundle.getMessage(JBStartServer.class, "MSG_STOP_SERVER_IN_PROGRESS")));//NOI18N
                    } else {
                        try {
                            Thread.sleep(30000);
                        } catch (InterruptedException e) {
                            // do nothing
                        }
                        
                        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.COMPLETED,
                                NbBundle.getMessage(JBStartServer.class, "MSG_SERVER_STOPPED")));//NOI18N
                        return;
                    }
                    
                    try {
                        elapsed += 500;
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                }
                fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.FAILED,
                        NbBundle.getMessage(JBStartServer.class, "MSG_STOP_SERVER_FAILED")));//NOI18N
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        private final static String SHUTDOWN_SH = "/bin/shutdown.sh";//NOI18N
        private final static String SHUTDOWN_BAT = "/bin/shutdown.bat";//NOI18N
    }
    
    
    
    // ----------  Implementation of ProgressObject interface
    private Vector listeners = new Vector();
    private DeploymentStatus deploymentStatus;
    
    public void addProgressListener(ProgressListener pl) {
        listeners.add(pl);
    }
    
    public void removeProgressListener(ProgressListener pl) {
        listeners.remove(pl);
    }
    
    public void stop() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("");
    }
    
    public boolean isStopSupported() {
        return false;
    }
    
    public void cancel() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("");
    }
    
    public boolean isCancelSupported() {
        return false;
    }
    
    public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
        return null;
    }
    
    public TargetModuleID[] getResultTargetModuleIDs() {
        return new TargetModuleID[]{};
    }
    
    public DeploymentStatus getDeploymentStatus() {
        return deploymentStatus;
    }
    
    /** Report event to any registered listeners. */
    public void fireHandleProgressEvent(TargetModuleID targetModuleID, DeploymentStatus deploymentStatus) {
        ProgressEvent evt = new ProgressEvent(this, targetModuleID, deploymentStatus);
        
        this.deploymentStatus = deploymentStatus;
        
        java.util.Vector targets = null;
        synchronized (this) {
            if (listeners != null) {
                targets = (java.util.Vector) listeners.clone();
            }
        }
        
        if (targets != null) {
            for (int i = 0; i < targets.size(); i++) {
                ProgressListener target = (ProgressListener)targets.elementAt(i);
                target.handleProgressEvent(evt);
            }
        }
    }
    
    
}
