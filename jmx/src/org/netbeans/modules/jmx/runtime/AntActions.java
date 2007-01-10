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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.runtime;

import org.netbeans.api.project.Project;
import org.openide.filesystems.FileUtil;

import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;

import org.netbeans.spi.project.ui.support.MainProjectSensitiveActions;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

import org.openide.NotifyDescriptor;

import javax.swing.*;
import sun.jvmstat.monitor.*;
import sun.management.ConnectorAddressLink;

import org.openide.util.RequestProcessor;
import org.openide.execution.ExecutorTask;
import java.io.IOException;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;

import javax.management.remote.*;
import org.netbeans.modules.jmx.j2seproject.customizer.ManagementCompositePanelProvider;

public class AntActions {
    
    public static Action enableMM() {
        Action a = MainProjectSensitiveActions.mainProjectSensitiveAction(
                new ProjectActionPerformer() {
            public boolean enable(Project project) {
                if (project == null) return false;
                return J2SEProjectType.isProjectTypeSupported(project);
            }
            
            public void perform(Project project) {
                enableMM(project);
            }
        },
                NbBundle.getMessage(AntActions.class, "LBL_EnableMMAction"), // NOI18N
                null);
        a.putValue(
                Action.SHORT_DESCRIPTION,
                NbBundle.getMessage(AntActions.class,"HINT_EnableMMAction")); // NOI18N
        
        a.putValue(
                "iconBase", // NOI18N
                "org/netbeans/modules/jmx/resources/run_project.png" //NOI18N
                );
        
        //Needed in Tools|Options|...| ToolBars action icons
        a.putValue (
            Action.SMALL_ICON, 
            new ImageIcon (org.openide.util.Utilities.loadImage("org/netbeans/modules/jmx/resources/run_project.png")) // NOI18N        
                );
        return a;
    }
    
    public static Action debugMM() {
        Action a = MainProjectSensitiveActions.mainProjectSensitiveAction(
                new ProjectActionPerformer() {
            public boolean enable(Project project) {
                if (project == null) return false;
                return J2SEProjectType.isProjectTypeSupported(project);
            }
            
            public void perform(Project project) {
                debugMM(project);
            }
        },
                NbBundle.getMessage(AntActions.class, "LBL_DebugMMAction"), // NOI18N
                null);
        a.putValue(
                Action.SHORT_DESCRIPTION,
                NbBundle.getMessage(AntActions.class, "HINT_DebugMMAction")); // NOI18N

        a.putValue(
                "iconBase", // NOI18N
                "org/netbeans/modules/jmx/resources/debug_project.png" // NOI18N
                );
        //Needed in Tools|Options|...| ToolBars action icons
        a.putValue (
            Action.SMALL_ICON, 
            new ImageIcon (org.openide.util.Utilities.loadImage("org/netbeans/modules/jmx/resources/debug_project.png")) // NOI18N        
                );
        return a;
    }
    
    // -- Private implementation -----------------------------------------------------------------------------------------
    
    private static ExecutorTask runTarget(Project project, String target, Properties props) {
        FileObject buildFile = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        try {
            return ActionUtils.runTarget(buildFile, new String[] { target }, props);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private static void enableMM(Project project) {
       handleMM(project, "run-management");// NOI18N
    }
    private static void debugMM(Project project) {
       handleMM(project, "debug-management");// NOI18N
    }
   
    private static boolean isValidConfig(Integer port, String file) {
        if(file == null && port == null) return false;
        return true;
    }
    
    private static void handleMM(Project project, String target) {
        
        // JConsole properties
        Properties projectProperties = J2SEProjectType.getProjectProperties(project);
        
        // XXX Because we are unbundled, we enable management
        boolean localAttach =
                Boolean.valueOf(projectProperties.getProperty(ManagementCompositePanelProvider.ATTACH_JCONSOLE_KEY,"true")); // NOI18N
        boolean rmiConnect =
                Boolean.valueOf(projectProperties.getProperty(ManagementCompositePanelProvider.ENABLE_RMI_KEY,"false")); // NOI18N
        
        if(!rmiConnect && !localAttach) {
            ManagementDialogs.getDefault().notify(
                new NotifyDescriptor.Message(NbBundle.getMessage(J2SEProjectType.class, "ERR_MMNotEnabled"), NotifyDescriptor.WARNING_MESSAGE));// NOI18N
            return;
        }
        
        if(rmiConnect) {
            handleRemoteManagement(project, target, projectProperties);
        } else {
            handleLocalManagement(project, target, projectProperties);
        }
    }
    
    private static void handleRemoteManagement(Project project, String target, Properties properties) {
        // 2. check if the project has been modified for management
        if(!J2SEProjectType.checkProjectIsModifiedForManagement(project)||
           !J2SEProjectType.checkProjectCanBeManaged(project))
            return;
        
        String projectRootDir = FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath();
        
        boolean launchJConsole = 
                Boolean.valueOf(properties.
                getProperty(ManagementCompositePanelProvider.ATTACH_JCONSOLE_KEY, 
                "true"));
        boolean useRMIPort = 
                Boolean.valueOf(properties.
                getProperty(ManagementCompositePanelProvider.RMI_USE_PORT_KEY, 
                "true"));
        
        String rmiPort = null;
        String configFile = null;
        
        if(useRMIPort)
            rmiPort = properties.
                getProperty(ManagementCompositePanelProvider.RMI_PORT_KEY);
        if(!useRMIPort)
            configFile = properties.
                getProperty(ManagementCompositePanelProvider.CONFIG_FILE_KEY);
        
        try {
            //Add property to ant execution context
            Properties props = new Properties();
            if(rmiPort != null)
                props.setProperty("com.sun.management.jmxremote.port", rmiPort);// NOI18N
            if(configFile != null)
                props.setProperty("com.sun.management.config.file", configFile);// NOI18N
            
            //We need a port to poll the process
            if(rmiPort == null) {
               Properties p = new Properties();
               File f = new File(configFile);
               p.load(new FileInputStream(f));
               rmiPort = p.getProperty("com.sun.management.jmxremote.port");// NOI18N
            }
                
            //Should be set to "" if no auto connect
            String msg = "";// NOI18N
            String url = "localhost:" + rmiPort;// NOI18N
            
            if(launchJConsole)
                msg = NbBundle.getMessage(AntActions.class, "MSG_ConnectingJConsole");// NOI18N
            else
                msg = NbBundle.getMessage(AntActions.class, "MSG_EnablingRemoteManagement");// NOI18N
            
            String managementArgs = " -Dcom.sun.management.jmxremote.port=" + rmiPort + " " +   // NOI18N      
                                    (configFile == null ? "-Dcom.sun.management.jmxremote.authenticate=false  -Dcom.sun.management.jmxremote.ssl=false" : "-Dcom.sun.management.config.file=" + "\""+configFile+"\"");// NOI18N
            
            props.setProperty("management.jvmargs", managementArgs);// NOI18N
            
            props.setProperty("connecting.jconsole.msg", msg);// NOI18N
            props.setProperty("jconsole.managed.process.url", url);// NOI18N
            
            
            //Run the run-management target. Run the app with remote mgt enabled
            ExecutorTask t = runTarget(project, target, props);
            
            if(launchJConsole) {
                //Launch the JConsole task poller.
                //Such task will launch JCOnsole only if run target doesn't fail AND
                // A connector is found in shared memory.
                RequestProcessor rp = new RequestProcessor();
                JConsoleRemoteAction action = new JConsoleRemoteAction(Integer.valueOf(rmiPort), project, t, properties);
                rp.post(action);
            }
        
        }catch(Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
     
    private static void handleLocalManagement(Project project, String target, Properties properties) {
         // 2. check if the project has been modified for management
        if(!J2SEProjectType.checkProjectIsModifiedForManagement(project)||
           !J2SEProjectType.checkProjectCanBeManaged(project))
            return;
  
        // Compute a unic key
        String key = String.valueOf(System.currentTimeMillis());
        try {
            //Add property to ant execution context
            Properties props = new Properties();
            String managementArgs = "-Dcom.sun.management.jmxremote -Djmx.process.virtual.pid=" + key;// NOI18N
            props.setProperty("management.jvmargs", managementArgs);// NOI18N
            props.setProperty("connecting.jconsole.msg", NbBundle.getMessage(AntActions.class, "MSG_ConnectingJConsole"));// NOI18N
            props.setProperty("jconsole.managed.process.url", "");// NOI18N
            
            //Run the run-lcl-mgt target. Run the app with lsocal mgt enabled
            ExecutorTask t = runTarget(project, target, props);
            
            //Launch the JConsole task poller.
            //Such task will launch JCOnsole only if run target doesn't fail AND
            // A connector is found in shared memory.
            RequestProcessor rp = new RequestProcessor();
            JConsoleAction action = new JConsoleAction("jmx.process.virtual.pid=" + // NOI18N
                    key, project, t, properties);
            rp.post(action);
        }catch(Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
    
    
  /*
   * Handle JConsole / app process killing
   */
    static class Killer implements org.openide.util.TaskListener {
        ExecutorTask app;
        public Killer(ExecutorTask app) {
            this.app = app;
        }
        
        public void taskFinished(org.openide.util.Task task) {
            //Killing app
            if(!app.isFinished())
              app.stop();
        }
    }
    
    static class JConsoleCommonAction  {
        protected Object key;
        protected Project project;
        protected ExecutorTask t;
        protected Properties properties;
        public JConsoleCommonAction(Object key, Project project, ExecutorTask t, Properties properties) {
            this.t = t;
            this.key = key;
            this.project = project;
            this.properties = properties;
        }
        
        protected void handleApplicationDied(ExecutorTask t, String msg) {
            //If result != 0, means that compilation failed
            if(t.result() == 0)
                t.getInputOutput().getErr().print(msg);
        }
        
        protected void connectJConsole(String url) {
            //Access to settings
            String polling = 
                    properties.getProperty(ManagementCompositePanelProvider.POLLING_PERIOD_KEY,"4");
            Properties props = new Properties();
            t.getInputOutput().getErr().println(NbBundle.getMessage(AntActions.class, "MSG_FoundProcessToConnectTo"));// NOI18N
            
            props.setProperty("jconsole.settings.vmoptions", "");// NOI18N
            props.setProperty("jconsole.settings.polling", polling);// NOI18N
            //Peegyback in notile
            StringBuffer notileandothers = new StringBuffer("");// NOI18N
            if(J2SEProjectType.isPlatformGreaterThanJDK15(project)) {
                String pluginsPath = 
                    properties.getProperty(ManagementCompositePanelProvider.PLUGINS_PATH_KEY);
                boolean classpath = Boolean.valueOf(properties.getProperty(ManagementCompositePanelProvider.PLUGINS_CLASSPATH_KEY));
                if(pluginsPath != null || classpath)
                    notileandothers.append("-pluginpath ");// NOI18N
                if(pluginsPath != null)
                    notileandothers.append(pluginsPath);
                
                String runClasspath = (String) properties.get("javac.classpath") // NOI18N
                        + File.pathSeparator + properties.get("build.dir") + File.separator + "classes" + // NOI18N
                        File.pathSeparator + properties.get("file.reference.build-classes"); // NOI18N
                if(classpath)
                    notileandothers.append((pluginsPath != null ? File.pathSeparator : "") + runClasspath);// NOI18N
                
            }   
            props.setProperty("jconsole.settings.notile", notileandothers.toString());// NOI18N
            
            // Not resolving custom types
            boolean useClasspath = Boolean.valueOf(properties.getProperty(ManagementCompositePanelProvider.RESOLVE_CLASSPATH_KEY));
            if(!useClasspath)
                props.setProperty("run.classpath", "");// NOI18N
            
            props.setProperty("jconsole.managed.process.url", url);// NOI18N
            ExecutorTask jt = runTarget(project, "-connect-jconsole", props);// NOI18N
            t.getInputOutput().select();
            jt.getInputOutput().getErr().println(NbBundle.getMessage(AntActions.class, "MSG_DisplayingJConsole"));// NOI18N
            //jt.getInputOutput().closeInputOutput();
            
            //Killing both ways. First killed, kill the other
            
            //t.addTaskListener(new Killer(jt));
            jt.addTaskListener(new Killer(t));
            t.waitFinished();
            //Sometimes JConsole is not killed. 
            //See http://www.netbeans.org/issues/show_bug.cgi?id=52045
            jt.stop();
            
        }
    }
    
  /*
   * Poll and launch JConsole.
   */
    static class JConsoleAction extends JConsoleCommonAction implements Runnable {
        
        public JConsoleAction(String key, Project project, ExecutorTask t, Properties properties) {
            super(key, project, t, properties);
        }
    
        public void run() {
            try {
                if(t.isFinished()) {
                    handleApplicationDied(t, NbBundle.getMessage(AntActions.class, "MSG_ErrorConnectingJConsole"));// NOI18N
                }
                
                int pid = findPID((String) key, t);
                
                if(pid == -1) {
                    handleApplicationDied(t, NbBundle.getMessage(AntActions.class, "MSG_ErrorConnectingJConsole"));// NOI18N
                    return;
                }
                String url = String.valueOf(pid);
                connectJConsole(url);
               
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
  /*
   * Poll and launch JConsole.
   */
    static class JConsoleRemoteAction extends JConsoleCommonAction implements Runnable {
        private String host;
        public JConsoleRemoteAction(Integer rmiPort, Project project, ExecutorTask t, Properties properties) {
            super(rmiPort, project, t, properties);
            this.host = "localhost";// NOI18N
        }
        
        public void run() {
            try {
                if(t.isFinished()) {
                    handleApplicationDied(t, NbBundle.getMessage(AntActions.class, "MSG_ErrorConnectingRemoteJConsole"));// NOI18N
                }
                
                try {
                    tryConnect((Integer) key, host, t);
                }catch(Exception e) {
                    handleApplicationDied(t, NbBundle.getMessage(AntActions.class, "MSG_ErrorConnectingRemoteJConsole"));// NOI18N
                    return;
                }
               
                String url = host + ":" + key;// NOI18N
                connectJConsole(url);
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
  /*
   *
   * Use tools.jar to findout launched process.
   */  
    private static void tryConnect(Integer port, String host, ExecutorTask t) throws Exception {
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + // NOI18N
                                              port +  "/jmxrmi");// NOI18N
        while(true) {
            if(t.isFinished())
                throw new Exception("Process is already dead");// NOI18N
            
            try {
                JMXConnectorFactory.connect(url);
                return;
            }catch(IOException e) {
                //Not yet connected
                //continue
            }catch(SecurityException se) {
                //OK Connected.
                return;
            }
            
            Thread.sleep(1000);
        }
    }
  
  /*
   *
   * Use tools.jar to findout launched process.
   */  
  private static int findPID(String key, ExecutorTask t) throws Exception {
      MonitoredHost host; 
      host = MonitoredHost.getMonitoredHost(new HostIdentifier((String)null));
      while(true) {
        for (Object vm: host.activeVms()) {
            if(t.isFinished())
                return -1;
            
          try {
              int vmid = (Integer)vm;
              String address = ConnectorAddressLink.importFrom(vmid);
              
              if (address != null) {
                  VmIdentifier vmId = new VmIdentifier(Integer.toString(vmid));
                  String cmdLine =
                          MonitoredVmUtil.jvmArgs(host.getMonitoredVm(vmId));
                  if(cmdLine.contains(key)) {
                      //This is our JVM
                      return vmid;
                  }
              }
              Thread.sleep(1000);
          } catch (Exception x) {
              System.out.println("Error, you should clean <tmp dir>/hsperfdata_<yourname>/" + vm + " file");// NOI18N
              x.printStackTrace();
          }
        }
      }
  }
}