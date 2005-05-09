/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL.
 * Use is subject to license terms.
 */

package org.netbeans.modules.jmx.runtime;

import org.netbeans.api.project.Project;
import org.openide.filesystems.FileUtil;

import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;

import org.netbeans.spi.project.ui.support.MainProjectSensitiveActions;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import org.openide.NotifyDescriptor;

import javax.swing.*;
import java.util.Properties;
import java.util.Set;
import sun.jvmstat.monitor.*;
import sun.management.ConnectorAddressLink;

import org.openide.util.RequestProcessor;
import org.openide.execution.ExecutorTask;
import org.openide.windows.InputOutput;
import org.openide.DialogDescriptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;

import org.netbeans.modules.jmx.jconsole.JConsoleSettings;


import javax.management.remote.*;
/**
 * 4.0 Ant-style actions
 *
 * @author Ian Formanek
 */
public class AntActions {
    /**
     * @return An Action to invoke profiling of the main project in the IDE
     */
    public static Action enableLocalManagement() {
        Action a = MainProjectSensitiveActions.mainProjectSensitiveAction(
                new ProjectActionPerformer() {
            public boolean enable(Project project) {
                if (project == null) return false;
                return J2SEProjectType.isProjectTypeSupported(project);
            }
            
            public void perform(Project project) {
                enableLocalManagement(project);
            }
        },
                NbBundle.getMessage(AntActions.class, "LBL_EnableLocalManagementAction"), // NOI18N
                null);
        a.putValue(
                Action.SHORT_DESCRIPTION,
                NbBundle.getMessage(
                AntActions.class,
                "HINT_EnableLocalManagementAction" // NOI18N
                )
                );
        a.putValue(
                "iconBase", // NOI18N
                "org/netbeans/modules/jmx/resources/run_project.png" //NOI18N
                );
        return a;
    }
    
    public static Action enableRemoteManagement() {
        Action a = MainProjectSensitiveActions.mainProjectSensitiveAction(
                new ProjectActionPerformer() {
            public boolean enable(Project project) {
                if (project == null) return false;
                return J2SEProjectType.isProjectTypeSupported(project);
            }
            
            public void perform(Project project) {
                enableRemoteManagement(project);
            }
        },
                NbBundle.getMessage(AntActions.class, "LBL_EnableRemoteManagementAction"), // NOI18N
                null);
        a.putValue(
                Action.SHORT_DESCRIPTION,
                NbBundle.getMessage(
                AntActions.class,
                "HINT_EnableRemoteManagementAction" // NOI18N
                )
                );
        a.putValue(
                "iconBase", // NOI18N
                "org/netbeans/modules/jmx/resources/run_project.png" // NOI18N
                );
        return a;
    }
    
    public static Action debugLocalManagement() {
        Action a = MainProjectSensitiveActions.mainProjectSensitiveAction(
                new ProjectActionPerformer() {
            public boolean enable(Project project) {
                if (project == null) return false;
                return J2SEProjectType.isProjectTypeSupported(project);
            }
            
            public void perform(Project project) {
                debugLocalManagement(project);
            }
        },
                NbBundle.getMessage(AntActions.class, "LBL_DebugLocalManagementAction"), // NOI18N
                null);
        a.putValue(
                Action.SHORT_DESCRIPTION,
                NbBundle.getMessage(
                AntActions.class,
                "HINT_DebugLocalManagementAction" // NOI18N
                )
                );
        a.putValue(
                "iconBase", // NOI18N
                "org/netbeans/modules/jmx/resources/debug_project.png" // NOI18N
                );
        return a;
    }
    
    public static Action debugRemoteManagement() {
        Action a = MainProjectSensitiveActions.mainProjectSensitiveAction(
                new ProjectActionPerformer() {
            public boolean enable(Project project) {
                if (project == null) return false;
                return J2SEProjectType.isProjectTypeSupported(project);
            }
            
            public void perform(Project project) {
                debugRemoteManagement(project);
            }
        },
                NbBundle.getMessage(AntActions.class, "LBL_DebugRemoteManagementAction"), // NOI18N
                null);
        a.putValue(
                Action.SHORT_DESCRIPTION,
                NbBundle.getMessage(
                AntActions.class,
                "HINT_DebugRemoteManagementAction" // NOI18N
                )
                );
        a.putValue(
                "iconBase", // NOI18N
                "org/netbeans/modules/jmx/resources/debug_project.png" // NOI18N
                );
        return a;
    }
    
    // -- Private implementation -----------------------------------------------------------------------------------------
    
    private static ExecutorTask runTarget(Project project, String target, Properties props) {
        FileObject buildFile = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        try {
            return ActionUtils.runTarget(buildFile, new String[] { target }, props);
        } catch (IOException e) {
            //Profiler.getDefault().notifyException(Profiler.EXCEPTION, e);
            //XXX REVISIT Exception handling
        }
        return null;
    }
    
    private static void enableLocalManagement(Project project) {
       handleLocalManagement(project, "run-management");
    }
    
    private static void debugLocalManagement(Project project) {
       handleLocalManagement(project, "debug-management");
    }
    
    private static void enableRemoteManagement(Project project) {
        handleRemoteManagement(project, "run-management");
    }
    
    private static void debugRemoteManagement(Project project) {
        handleRemoteManagement(project, "debug-management");
    }
    
   
    private static boolean isValidConfig(Integer port, String file) {
        if(file == null && port == null) return false;
        return true;
    }
    
    private static void handleRemoteManagement(Project project, String target) {
        // 2. check if the project has been modified for management
        if(!J2SEProjectType.checkProjectIsModifiedForManagement(project)||
           !J2SEProjectType.checkProjectCanBeManaged(project))
            return;
        
        //Access Settings
        //TODO!!!
        ConfigOptions options = ConfigOptions.getDefault();
        
        
        
        Integer rmiPort = null;
        String configFile = null;
        ConfigurationPanel dialog;
        String projectRootDir = FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath();
        do{
          dialog = new ConfigurationPanel(options.isPortSelectedChoice(),
                options.getRMIPort(), 
                options.getConfigurationFile(), 
                options.isJConsoleAutoConnected(),
                projectRootDir);
           
           if(dialog.isClosed()) return;
           rmiPort = dialog.isPortSelected() ? dialog.getRMIPort() : null;
           configFile = dialog.isFileSelected() ? dialog.getFilePath() : null;
        }while(!isValidConfig(rmiPort, configFile));
        
        boolean launchJConsole = dialog.isJConsoleToConnect();
       
        //Persist options
        options.setPortSelectedChoice(Boolean.valueOf(dialog.isPortSelected()));
        options.setConfigurationFile(dialog.getFilePath());
        options.setRMIPort(dialog.getRMIPort());
        options.setJConsoleAutoConnected(Boolean.valueOf(launchJConsole));
        
        try {
            //Add property to ant execution context
            Properties props = new Properties();
            if(rmiPort != null)
                props.setProperty("com.sun.management.jmxremote.port", rmiPort.toString());
            if(configFile != null)
                props.setProperty("com.sun.management.config.file", configFile);
            
            //We need a port to poll the process
            if(rmiPort == null) {
               Properties p = new Properties();
               File f = new File(configFile);
               p.load(new FileInputStream(f));
               rmiPort = new Integer(p.getProperty("com.sun.management.jmxremote.port"));
            }
                
            //Should be set to "" if no auto connect
            String msg = "";
            String url = "localhost:" + rmiPort;
            
            if(launchJConsole)
                msg = NbBundle.getMessage(AntActions.class, "MSG_ConnectingJConsole");
            else
                msg = NbBundle.getMessage(AntActions.class, "MSG_EnablingRemoteManagement");
            
            String managementArgs = " -Dcom.sun.management.jmxremote.port=" + rmiPort + " " +         
                                    (configFile == null ? "-Dcom.sun.management.jmxremote.authenticate=false  -Dcom.sun.management.jmxremote.ssl=false" : "-Dcom.sun.management.config.file=" + "\""+configFile+"\"");
            
            props.setProperty("management.jvmargs", managementArgs);
            
            props.setProperty("connecting.jconsole.msg", msg);
            props.setProperty("jconsole.managed.process.url", url);
            
            
            //Run the run-management target. Run the app with remote mgt enabled
            ExecutorTask t = runTarget(project, target, props);
            
            if(launchJConsole) {
                //Launch the JConsole task poller.
                //Such task will launch JCOnsole only if run target doesn't fail AND
                // A connector is found in shared memory.
                RequestProcessor rp = new RequestProcessor();
                JConsoleRemoteAction action = new JConsoleRemoteAction(rmiPort, project, t);
                rp.post(action);
            }
        
        }catch(Exception e) {
            System.out.println("EXCEPTION "+ e.toString());
            e.printStackTrace();
        }
    }
     
    private static void handleLocalManagement(Project project, String target) {
         // 2. check if the project has been modified for management
        if(!J2SEProjectType.checkProjectIsModifiedForManagement(project)||
           !J2SEProjectType.checkProjectCanBeManaged(project))
            return;
  
        // Compute a unic key
        String key = String.valueOf(System.currentTimeMillis());
        try {
            //Add property to ant execution context
            Properties props = new Properties();
            String managementArgs = "-Dcom.sun.management.jmxremote -Djmx.process.virtual.pid=" + key;
            props.setProperty("management.jvmargs", managementArgs);
            props.setProperty("connecting.jconsole.msg", NbBundle.getMessage(AntActions.class, "MSG_ConnectingJConsole"));
            props.setProperty("jconsole.managed.process.url", "");
            
            //Run the run-lcl-mgt target. Run the app with lsocal mgt enabled
            ExecutorTask t = runTarget(project, target, props);
            
            //Launch the JConsole task poller.
            //Such task will launch JCOnsole only if run target doesn't fail AND
            // A connector is found in shared memory.
            RequestProcessor rp = new RequestProcessor();
            JConsoleAction action = new JConsoleAction("jmx.process.virtual.pid=" +
                    key, project, t);
            rp.post(action);
        }catch(Exception e) {
            System.out.println("EXCEPTION "+ e.toString());
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
        public JConsoleCommonAction(Object key, Project project, ExecutorTask t) {
            this.t = t;
            this.key = key;
            this.project = project;
        }
        
        protected void handleApplicationDied(ExecutorTask t, String msg) {
            //If result != 0, means that compilation failed
            if(t.result() == 0)
                t.getInputOutput().getErr().print(msg);
        }
        
        protected void connectJConsole(String url) {
            //Access to settings
            JConsoleSettings settings = JConsoleSettings.getDefault();
            String polling = String.valueOf(settings.getPolling());
            String tile = !settings.getTile() ? "-notile" : "";
            String vmOptions = settings.getVMOptions() == null ? "" : settings.getVMOptions();
            Properties props = new Properties();
            t.getInputOutput().getErr().println(NbBundle.getMessage(AntActions.class, "MSG_FoundProcessToConnectTo"));
            
            props.setProperty("jconsole.settings.vmoptions", vmOptions);
            props.setProperty("jconsole.settings.polling", polling);
            props.setProperty("jconsole.settings.notile", tile);
            
            props.setProperty("jconsole.managed.process.url", url);
            ExecutorTask jt = runTarget(project, "-connect-jconsole", props);
            t.getInputOutput().select();
            jt.getInputOutput().getErr().println(NbBundle.getMessage(AntActions.class, "MSG_DisplayingJConsole"));
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
        
        public JConsoleAction(String key, Project project, ExecutorTask t) {
            super(key, project, t);
        }
    
        public void run() {
            try {
                if(t.isFinished()) {
                    handleApplicationDied(t, NbBundle.getMessage(AntActions.class, "MSG_ErrorConnectingJConsole"));
                }
                
                int pid = findPID((String) key, t);
                
                if(pid == -1) {
                    handleApplicationDied(t, NbBundle.getMessage(AntActions.class, "MSG_ErrorConnectingJConsole"));
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
        public JConsoleRemoteAction(Integer rmiPort, Project project, ExecutorTask t) {
            super(rmiPort, project, t);
            this.host = "localhost";
        }
        
        public void run() {
            try {
                if(t.isFinished()) {
                    handleApplicationDied(t, NbBundle.getMessage(AntActions.class, "MSG_ErrorConnectingRemoteJConsole"));
                }
                
                try {
                    tryConnect((Integer) key, host, t);
                }catch(Exception e) {
                    handleApplicationDied(t, NbBundle.getMessage(AntActions.class, "MSG_ErrorConnectingRemoteJConsole"));
                    return;
                }
               
                String url = host + ":" + key;
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
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + 
                                              port +  "/jmxrmi");
        while(true) {
            if(t.isFinished())
                throw new Exception("Process is already dead");
            
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
              System.out.println("Error, you should clean <tmp dir>/hsperfdata_<yourname>/" + vm + " file");
              x.printStackTrace();
          }
        }
      }
  }
}