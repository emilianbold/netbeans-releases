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

import org.netbeans.modules.j2ee.jboss4.JBDeploymentFactory;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBInstantiatingIterator;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.plugins.api.StartServer;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.IOProvider;
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
import javax.management.*;
import java.net.URLClassLoader;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginUtils;

/**
 *
 * @author Kirill Sorokin <Kirill.Sorokin@Sun.COM>
 */
public class JBStartServer extends StartServer implements ProgressObject{
    
    private JBDeploymentManager dm;
    
    public JBStartServer(DeploymentManager dm) {
        if (!(dm instanceof JBDeploymentManager)) {
            throw new IllegalArgumentException("");
        }
        this.dm = (JBDeploymentManager) dm;
    }
    
    public ProgressObject startDebugging(Target target) {
        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.RUNNING, NbBundle.getMessage(JBStartServer.class, "MSG_START_SERVER_IN_PROGRESS"))); //NOI18N
        RequestProcessor.getDefault().post(new JBStartRunnable(true), 0, Thread.NORM_PRIORITY);
        return this;
    }
    
    public boolean isDebuggable(Target target) {
        return false;
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
        return this;
    }
    
    /**
     * Starts the server
     */
    public ProgressObject startDeploymentManager() {
        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.RUNNING, NbBundle.getMessage(JBStartServer.class, "MSG_START_SERVER_IN_PROGRESS")));//NOI18N
        RequestProcessor.getDefault().post(new JBStartRunnable(false), 0, Thread.NORM_PRIORITY);
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
        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        URLClassLoader loader = ((JBDeploymentFactory)JBDeploymentFactory.create()).getJBClassLoader();
        
        Thread.currentThread().setContextClassLoader(loader);
        java.util.Hashtable env = new java.util.Hashtable();
        
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
        env.put(Context.PROVIDER_URL, "jnp://localhost:"+JBPluginUtils.getJnpPort( InstanceProperties.getInstanceProperties(dm.getUrl()).getProperty(JBInstantiatingIterator.PROPERTY_SERVER_DIR)));
        env.put(Context.OBJECT_FACTORIES, "org.jboss.naming");
        env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces" );
        env.put("jnp.disableDiscovery", Boolean.TRUE); // NOI18N
        
        String checkingConfigName = InstanceProperties.getInstanceProperties(dm.getUrl()).getProperty("server");
        
        try{
            InitialContext ctx = new InitialContext(env);
            ctx.lookup("/jmx/invoker/RMIAdaptor");
            
            MBeanServerConnection srv = (MBeanServerConnection)ctx.lookup("/jmx/invoker/RMIAdaptor");
            ObjectName target = new ObjectName("jboss.system:type=ServerConfig");
            String configName = (String)srv.getAttribute(target, "ServerName");
            
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
        String serverName = (String)InstanceProperties.getInstanceProperties((dm).getUrl()).getProperty(JBInstantiatingIterator.PROPERTY_SERVER) ;
        
        
        
        if (!isReallyRunning()){
            return false;
        }
        
        
        // Show log if server started and log not shown
        // create an output tab for the server output
        InputOutput io = IOProvider.getDefault().getIO( getIOTabName(), false);
        io.select();
        
        
        String logFileName = (String)InstanceProperties.getInstanceProperties((dm).getUrl()).getProperty(JBInstantiatingIterator.PROPERTY_SERVER_DIR) + File.separator+"log"+ File.separator+"server.log" ;//NOI18N
        File logFile = new File(logFileName);
        if (logFile.exists()){
            try{
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
    
    private String getIOTabName(){
        return (String)InstanceProperties.getInstanceProperties((dm).getUrl()).getProperty(JBInstantiatingIterator.PROPERTY_DISPLAY_NAME);
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
                String serverLocation = JBPluginProperties.getInstance().getInstallLocation();
                
                ProcessBuilder processBuilder = new ProcessBuilder(new String[] {serverLocation + (Utilities.isWindows() ? STARTUP_BAT : STARTUP_SH), "-c", JBOSS_INSTANCE});
                
                if (debug) {
                    processBuilder.environment().put("JAVA_OPTS", "-classic -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address="+dm.getDebuggingPort()+ ",server=y,suspend=n");//NOI18N
                } else {
                    processBuilder.environment().put("JAVA_OPTS", "");
                }
                
                Process serverProcess = null;
                
                serverProcess = processBuilder.start();
                
//                Process serverProcess = Runtime.getRuntime().exec(JBOSS_HOME + (Utilities.isWindows() ? STARTUP_BAT : STARTUP_SH));
                
                fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.RUNNING, NbBundle.getMessage(JBStartServer.class, "MSG_START_SERVER_IN_PROGRESS")));//NOI18N
                
                // create an output tab for the server output
                InputOutput io = IOProvider.getDefault().getIO(getIOTabName(), false);
                io.select();
                
                // read the data from the server's output up
                LineNumberReader reader = new LineNumberReader(new InputStreamReader(serverProcess.getInputStream()));
                
                try {
                    int timeout = 300000;
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
            try {
                String serverLocation = JBPluginProperties.getInstance().getInstallLocation();
                Process serverProcess = Runtime.getRuntime().exec(serverLocation + (Utilities.isWindows() ? SHUTDOWN_BAT : SHUTDOWN_SH));
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
                        elapsed += 5000;
                        Thread.sleep(5000);
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
        private final static String SHUTDOWN_SH = "/bin/shutdown.sh --shutdown";//NOI18N
        private final static String SHUTDOWN_BAT = "/bin/shutdown.bat --shutdown";//NOI18N
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
