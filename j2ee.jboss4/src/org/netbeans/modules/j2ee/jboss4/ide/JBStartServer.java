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
package org.netbeans.modules.j2ee.jboss4.ide;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
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
import org.openide.execution.NbProcessDescriptor;
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
        String serverName = dm.getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.RUNNING, NbBundle.getMessage(JBStartServer.class, "MSG_START_SERVER_IN_PROGRESS", serverName))); //NOI18N
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
        String serverName = dm.getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.RUNNING, NbBundle.getMessage(JBStartServer.class, "MSG_STOP_SERVER_IN_PROGRESS", serverName)));//NOI18N
        RequestProcessor.getDefault().post(new JBStopRunnable(), 0, Thread.NORM_PRIORITY);
        isDebugModeUri.remove(dm.getUrl());
        return this;
    }
    
    /**
     * Starts the server
     */
    public ProgressObject startDeploymentManager() {
        String serverName = dm.getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.RUNNING, NbBundle.getMessage(JBStartServer.class, "MSG_START_SERVER_IN_PROGRESS", serverName)));//NOI18N
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
        final InstanceProperties ip = dm.getInstanceProperties();
        if (ip == null) {
            return false; // finish, it looks like this server instance has been unregistered
        }
        // this should prevent the thread from getting stuck if the server is in suspended state 
        SafeTrueTest test = new SafeTrueTest() {
            public void run() {
                ClassLoader oldLoader = null;
                String checkingConfigName = ip.getProperty(JBPluginProperties.PROPERTY_SERVER);
                try{
                    oldLoader = Thread.currentThread().getContextClassLoader();
                    String serverRoot = ip.getProperty(JBPluginProperties.PROPERTY_ROOT_DIR);
                    JBDeploymentFactory df = (JBDeploymentFactory)JBDeploymentFactory.create();
                    URLClassLoader loader = df.getJBClassLoader(serverRoot);
                    Thread.currentThread().setContextClassLoader(loader);

                    Hashtable env = new Hashtable();
                    env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory"); // NOI18N
                    String serverDir = ip.getProperty(JBPluginProperties.PROPERTY_SERVER_DIR);
                    env.put(Context.PROVIDER_URL, "jnp://localhost:" + JBPluginUtils.getJnpPort(serverDir));
                    env.put(Context.OBJECT_FACTORIES, "org.jboss.naming");                      // NOI18N
                    env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");   // NOI18N
                    env.put("jnp.disableDiscovery", Boolean.TRUE);                              // NOI18N

                    InitialContext ctx = new InitialContext(env);
                    Object srv = ctx.lookup("/jmx/invoker/RMIAdaptor");                 // NOI18N
                    Class objectName = loader.loadClass("javax.management.ObjectName"); // NOI18N
                    Method getInstance = objectName.getMethod("getInstance", new Class[] {String.class} );          // NOI18N
                    Object target = getInstance.invoke(null, new Object[]{"jboss.system:type=ServerConfig"});       // NOI18N
                    Class[] params = new Class[]{loader.loadClass("javax.management.ObjectName"), String.class};    // NOI18N
                    Method getAttribute = srv.getClass().getMethod("getAttribute", params);                 // NOI18N
                    Object serverName = getAttribute.invoke(srv, new Object[]{target, "ServerName"});       // NOI18N
                    if (checkingConfigName.equals(serverName)) {
                        result = true;
                    }
                } catch(NameNotFoundException e){
                    if (checkingConfigName.equals("minimal")) { // NOI18N
                        result = true;
                    }
                } catch (IllegalAccessException ex) {
                    result = false; 
                } catch (ClassNotFoundException ex) {
                    result = false; 
                } catch (NamingException ex) {
                    result = false; 
                } catch (NoSuchMethodException ex) {
                    result = false; 
                } catch (InvocationTargetException ex) {
                    result = false; 
                } finally{
                    if (oldLoader != null) {
                        Thread.currentThread().setContextClassLoader(oldLoader);
                    }
                }
            }
        };
        return safeTrueTest(test, 10000);
    }
    
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
            // no op
        } finally {
            return test.result();
        }
    }
    
    public boolean isRunning() {
        //     String host = dm.getHost();
        //   int port = dm.getPort();
        
        String url = dm.getUrl();

        InstanceProperties ip = dm.getInstanceProperties();
        if (ip == null) {
            return false; // finish, it looks like this server instance has been unregistered
        }
        
        if (!isReallyRunning()){
            dm.setRunningLastCheck(ip, Boolean.FALSE);
            return false;
        }
        
        // Show log if server started and log not shown
        // create an output tab for the server output
        InputOutput io = UISupport.getServerIO(url);
        if (io != null) {
            io.select();
        }
        
        
        String logFileName = (String)ip.getProperty(JBPluginProperties.PROPERTY_SERVER_DIR) + File.separator+"log"+ File.separator+"server.log" ;//NOI18N
        File logFile = new File(logFileName);
        if (logFile.exists()){
            try{
                String serverName = (String)ip.getProperty(JBPluginProperties.PROPERTY_SERVER) ;
                JBLogWriter logWriter = JBLogWriter.createInstance(io, new FileInputStream(logFile),serverName,true);
                dm.setLogWriter(logWriter);
                if (!logWriter.isRunning())
                    RequestProcessor.getDefault().post(logWriter, 0, Thread.NORM_PRIORITY);
            }catch(Exception e){
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }

        }else{
            dm.setRunningLastCheck(ip, Boolean.FALSE);
            return false;
        }
        
        dm.setRunningLastCheck(ip, Boolean.TRUE);
        return true;
    }
    
    private class JBStartRunnable implements Runnable {
        
        private boolean debug;
        private  String JBOSS_INSTANCE ="";
        
        public JBStartRunnable(boolean debug) {
            JBOSS_INSTANCE = dm.getInstanceProperties().getProperty(JBPluginProperties.PROPERTY_SERVER);
            this.debug = debug;
        }
        
        public void run() {

            InstanceProperties ip = dm.getInstanceProperties();

            String serverName = ip.getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
            
            String strHTTPConnectorPort = ip.getProperty(JBPluginProperties.PROPERTY_PORT);
            try {
                int HTTPConnectorPort = new Integer(strHTTPConnectorPort).intValue();
                if (!JBPluginUtils.isPortFree(HTTPConnectorPort)) {
                    fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.FAILED, 
                            NbBundle.getMessage(JBStartServer.class, "MSG_START_SERVER_FAILED_HTTP_PORT_IN_USE", serverName, strHTTPConnectorPort)));//NOI18N
                    return;
                }

                String serverDir = ip.getProperty(JBPluginProperties.PROPERTY_SERVER_DIR);
                
                String strJNPServicePort = JBPluginUtils.getJnpPort(serverDir);
                int JNPServicePort = new Integer(strJNPServicePort).intValue();
                if (!JBPluginUtils.isPortFree(JNPServicePort)) {
                    fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.FAILED, 
                            NbBundle.getMessage(JBStartServer.class, "MSG_START_SERVER_FAILED_JNP_PORT_IN_USE", serverName, strJNPServicePort)));//NOI18N
                    return;
                }

                String strRMINamingServicePort = JBPluginUtils.getRMINamingServicePort(serverDir);
                int RMINamingServicePort = new Integer(strRMINamingServicePort).intValue();
                if (!JBPluginUtils.isPortFree(RMINamingServicePort)) {
                    fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.FAILED, 
                            NbBundle.getMessage(JBStartServer.class, "MSG_START_SERVER_FAILED_RMI_PORT_IN_USE", serverName, strRMINamingServicePort)));//NOI18N
                    return;
                }

                String server = ip.getProperty(JBPluginProperties.PROPERTY_SERVER);
                if (!"minimal".equals(server)) {
                    String strRMIInvokerPort = JBPluginUtils.getRMIInvokerPort(serverDir);
                    int RMIInvokerPort = new Integer(strRMIInvokerPort).intValue();
                    if (!JBPluginUtils.isPortFree(RMIInvokerPort)) {
                        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.FAILED, 
                                NbBundle.getMessage(JBStartServer.class, "MSG_START_SERVER_FAILED_INVOKER_PORT_IN_USE", serverName, strRMIInvokerPort)));//NOI18N
                        return;
                    }
                }

            }
            catch (NumberFormatException nfe) {} // noop


            Process serverProcess = null;
//            String serverLocation = JBPluginProperties.getInstance().getInstallLocation();
            String serverLocation = ip.getProperty(JBPluginProperties.PROPERTY_ROOT_DIR);

            String serverRunFileName = serverLocation + (Utilities.isWindows() ? STARTUP_BAT : STARTUP_SH); 

            File serverRunFile = new File(serverRunFileName);

            if (!serverRunFile.exists()){
                fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.FAILED, NbBundle.getMessage(JBStartServer.class, "MSG_START_SERVER_FAILED_FNF", serverName)));//NOI18N
                return;
            }

            String args = ("all".equals(JBOSS_INSTANCE) ? "-b 127.0.0.1 " : "") + "-c " + JBOSS_INSTANCE; // NOI18N
            org.openide.execution.NbProcessDescriptor pd = new org.openide.execution.NbProcessDescriptor(serverRunFileName, args);

            String envp[];

            if (debug) {
                envp = new String[]{"JAVA_OPTS=-classic -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address="+dm.getDebuggingPort()+ ",server=y,suspend=n"};
            } else {
                envp = new String[0];
            }

            try {
                serverProcess = pd.exec(null, envp, true, null );
            } catch (java.io.IOException ioe) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);

                fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.FAILED, 
                        NbBundle.getMessage(JBStartServer.class, "MSG_START_SERVER_FAILED_PD", serverName, serverRunFileName)));//NOI18N

                return;
            }

            fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.RUNNING, NbBundle.getMessage(JBStartServer.class, "MSG_START_SERVER_IN_PROGRESS", serverName)));//NOI18N

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

            LineNumberReader reader = null;
            try {
                // read the data from the server's output up
                reader = new LineNumberReader(new InputStreamReader(serverProcess.getInputStream()));

                int timeout = 900000;
                int elapsed = 0;
                while (elapsed < timeout) {
                    while (reader.ready()) {
                        String line = reader.readLine();
                        if (line == null) continue;

                        io.getOut().write(line + "\n"); //NOI18N

                        if (line.indexOf("Starting JBoss (MX MicroKernel)") > -1) {
                            fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.RUNNING, NbBundle.getMessage(JBStartServer.class, "MSG_START_SERVER_IN_PROGRESS", serverName)));//NOI18N
                        }

                        else if (line.indexOf("JBoss (MX MicroKernel)") > -1 && line.indexOf("Started in") > -1) {//NOI18N
                            fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.COMPLETED, NbBundle.getMessage(JBStartServer.class, "MSG_SERVER_STARTED", serverName)));//NOI18N
                            // start logging
                            JBLogWriter logWriter = JBLogWriter.updateInstance(io, serverProcess.getInputStream(),JBOSS_INSTANCE);
                            dm.setLogWriter(logWriter);
                            RequestProcessor.getDefault().post(logWriter, 0, Thread.NORM_PRIORITY);
                            return;
                        }

                        else if (line.indexOf("Shutdown complete") > -1) {
                            fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.FAILED, NbBundle.getMessage(JBStartServer.class, "MSG_START_SERVER_FAILED", serverName)));//NOI18N
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
            finally {
                try {
                    if (reader != null)
                        reader.close();
                } catch (IOException ex) {}
            }

            fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.FAILED, NbBundle.getMessage(JBStartServer.class, "MSG_START_SERVER_FAILED", serverName)));//NOI18N
                
        }
        private final static String STARTUP_SH = "/bin/run.sh";
        private final static String STARTUP_BAT = "/bin/run.bat";
    }
    
    private class JBStopRunnable implements Runnable {
        
        public void run() {
            
            InstanceProperties ip = dm.getInstanceProperties();
            
            String configName = ip.getProperty("server");
            if ("minimal".equals(configName)) {
                fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.FAILED, NbBundle.getMessage(JBStartServer.class, "MSG_STOP_SERVER_FAILED_MINIMAL")));//NOI18N
                return;
            }

            String serverName = ip.getProperty(InstanceProperties.DISPLAY_NAME_ATTR);

            String serverLocation = ip.getProperty(JBPluginProperties.PROPERTY_ROOT_DIR);
            String serverStopFileName = serverLocation + (Utilities.isWindows() ? SHUTDOWN_BAT : SHUTDOWN_SH);

            File serverStopFile = new File(serverStopFileName);
            if (!serverStopFile.exists()){
                fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.FAILED, NbBundle.getMessage(JBStartServer.class, "MSG_STOP_SERVER_FAILED_FNF", serverName)));//NOI18N
                return;
            }

            NbProcessDescriptor pd = new NbProcessDescriptor(serverStopFileName, "--shutdown");
            try {
                pd.exec();
            } catch (java.io.IOException ioe) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);

                fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.FAILED, 
                        NbBundle.getMessage(JBStartServer.class, "MSG_STOP_SERVER_FAILED_PD", serverName, serverStopFileName)));//NOI18N

                return;
            }

            fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.RUNNING,
                    NbBundle.getMessage(JBStartServer.class, "MSG_STOP_SERVER_IN_PROGRESS", serverName)));



            int timeout = 300000;
            int elapsed = 0;
            while (elapsed < timeout) {
                if (isRunning()) {
                    fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.RUNNING,
                            NbBundle.getMessage(JBStartServer.class, "MSG_STOP_SERVER_IN_PROGRESS", serverName)));//NOI18N
                } else {
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        // do nothing
                    }

                    fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.COMPLETED,
                            NbBundle.getMessage(JBStartServer.class, "MSG_SERVER_STOPPED", serverName)));//NOI18N
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
                    NbBundle.getMessage(JBStartServer.class, "MSG_STOP_SERVER_FAILED", serverName)));//NOI18N
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
