/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.jmx.jconsole.runtime;

import org.netbeans.api.project.Project;
import org.openide.filesystems.FileUtil;

import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;

import org.netbeans.spi.project.ui.support.MainProjectSensitiveActions;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;

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
import org.netbeans.modules.jmx.common.runtime.J2SEProjectType;
import org.netbeans.modules.jmx.common.runtime.ManagementDialogs;
import static org.netbeans.modules.jmx.jconsole.runtime.Bundle.*;
import org.openide.util.NbBundle.Messages;

public class AntActions {
    
    @Messages({"LBL_EnableMMAction=Run Main Project with Monitoring and Management...", "HINT_EnableMMAction=Run Main Project with Monitoring and Management"})
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
                LBL_EnableMMAction(),
                null);
        a.putValue(
                Action.SHORT_DESCRIPTION,
                HINT_EnableMMAction());
        
        a.putValue(
                "iconBase", // NOI18N
                "org/netbeans/modules/jmx/jconsole/resources/run_project.png" //NOI18N
                );
        
        //Needed in Tools|Options|...| ToolBars action icons
        a.putValue (
            Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/jmx/jconsole/resources/run_project.png", false));
        return a;
    }
    
    @Messages({"LBL_DebugMMAction=Debug Main Project with Monitoring and Management...", "HINT_DebugMMAction=Debug Main Project with Monitoring and Management"})
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
                LBL_DebugMMAction(),
                null);
        a.putValue(
                Action.SHORT_DESCRIPTION,
                HINT_DebugMMAction()); // NOI18N

        a.putValue(
                "iconBase", // NOI18N
                "org/netbeans/modules/jmx/jconsole/resources/debug_project.png" // NOI18N
                );
        //Needed in Tools|Options|...| ToolBars action icons
        a.putValue (
            Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/jmx/jconsole/resources/debug_project.png", false));
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
    
    @Messages("ERR_MMNotEnabled=No Management features enabled. Please check Project properties to enable Monitoring And Management.")
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
                new NotifyDescriptor.Message(ERR_MMNotEnabled(), NotifyDescriptor.WARNING_MESSAGE));
            return;
        }
        Properties p = null;
        if(rmiConnect) //{
            p = createRemoteManagementProperties(project, target, projectProperties);
        //} else {
        handleLocalManagement(project, target, projectProperties, p, localAttach);
        //}
    }
    
    @Messages({"MSG_ConnectingJConsole=Connecting JConsole...", "MSG_EnablingRemoteManagement=Enabling Remote Management..."})
    private static Properties createRemoteManagementProperties(Project project, String target, Properties properties) {
        // 2. check if the project has been modified for management
        if(!ManagementChecker.checkProjectIsModifiedForManagement(project)||
           !ManagementChecker.checkProjectCanBeManaged(project))
            return null;
        
        String projectRootDir = FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath();
        
        boolean launchJConsole = 
                Boolean.valueOf(properties.
                getProperty(ManagementCompositePanelProvider.ATTACH_JCONSOLE_KEY, 
                "true"));// NOI18N
        boolean useRMIPort = 
                Boolean.valueOf(properties.
                getProperty(ManagementCompositePanelProvider.RMI_USE_PORT_KEY, 
                "true"));// NOI18N
        
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
               FileInputStream fis = new FileInputStream(f);
               try {
                   p.load(fis);
               } finally {
                   fis.close();
               }
               rmiPort = p.getProperty("com.sun.management.jmxremote.port");// NOI18N
            }
                
            //Should be set to "" if no auto connect
            String msg = "";// NOI18N
            String url = "localhost:" + rmiPort;// NOI18N
            
            if(launchJConsole)
                msg = MSG_ConnectingJConsole();
            else
                msg = MSG_EnablingRemoteManagement();
            
            String managementArgs = " -Dcom.sun.management.jmxremote.port=" + rmiPort + " " +   // NOI18N      
                                    (configFile == null ? "-Dcom.sun.management.jmxremote.authenticate=false  -Dcom.sun.management.jmxremote.ssl=false" : "-Dcom.sun.management.config.file=" + "\""+configFile+"\"");// NOI18N
            
            props.setProperty("management.jvmargs", managementArgs);// NOI18N
            
            props.setProperty("connecting.jconsole.msg", msg);// NOI18N
            props.setProperty("jconsole.managed.process.url", "RMI access enabled on " + url);// NOI18N
            
            
            //Run the run-management target. Run the app with remote mgt enabled
            //ExecutorTask t = runTarget(project, target, props);
            
            /*if(launchJConsole) {
                //Launch the JConsole task poller.
                //Such task will launch JCOnsole only if run target doesn't fail AND
                // A connector is found in shared memory.
                RequestProcessor rp = new RequestProcessor();
                JConsoleRemoteAction action = new JConsoleRemoteAction(Integer.valueOf(rmiPort), project, t, properties);
                rp.post(action);
            }
            */
            return props;
        }catch(Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
        return null;
    }
     
    private static void handleLocalManagement(Project project, String target, 
            Properties properties, Properties remoteProperties, boolean launchJConsole) {
         // 2. check if the project has been modified for management
        if(!ManagementChecker.checkProjectIsModifiedForManagement(project)||
           !ManagementChecker.checkProjectCanBeManaged(project))
            return;
  
        // Compute a unic key
        String key = String.valueOf(System.currentTimeMillis());
        try {
            //Add property to ant execution context
            Properties props = remoteProperties == null ? new Properties() : remoteProperties;
            String remoteArgs = props.getProperty("management.jvmargs");// NOI18N
           
            String managementArgs = "-Dcom.sun.management.jmxremote -Djmx.process.virtual.pid=" + key;// NOI18N
            if(remoteArgs != null)
                managementArgs = managementArgs + " " + remoteArgs;
            
            props.setProperty("management.jvmargs", managementArgs);// NOI18N
            props.setProperty("connecting.jconsole.msg", MSG_ConnectingJConsole());// NOI18N
            if(remoteProperties == null)
                props.setProperty("jconsole.managed.process.url", "");// NOI18N
            
            //Run the run-lcl-mgt target. Run the app with lsocal mgt enabled
            ExecutorTask t = runTarget(project, target, props);
            
            //Launch the JConsole task poller.
            //Such task will launch JCOnsole only if run target doesn't fail AND
            // A connector is found in shared memory.
            if(launchJConsole) {
                RequestProcessor rp = new RequestProcessor();
                JConsoleAction action = new JConsoleAction("jmx.process.virtual.pid=" + // NOI18N
                    key, project, t, properties);
                rp.post(action);
            }
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
        
        @Messages({"MSG_FoundProcessToConnectTo=Found manageable process, connecting JConsole to process...", "MSG_DisplayingJConsole=Displaying JConsole..."})
        protected void connectJConsole(String url) {
            //Access to settings
            String polling = 
                    properties.getProperty(ManagementCompositePanelProvider.POLLING_PERIOD_KEY,"4");
            Properties props = new Properties();
            t.getInputOutput().getErr().println(MSG_FoundProcessToConnectTo());
            
            props.setProperty("jconsole.settings.vmoptions", "");// NOI18N
            props.setProperty("jconsole.settings.polling", polling);// NOI18N
            //Peegyback in notile
            StringBuffer notileandothers = new StringBuffer("");// NOI18N
            if(J2SEProjectType.isPlatformGreaterThanJDK15(project)) {
                String pluginsPath = 
                    properties.getProperty(ManagementCompositePanelProvider.PLUGINS_PATH_KEY);
                boolean classpath = Boolean.valueOf(properties.getProperty(ManagementCompositePanelProvider.PLUGINS_CLASSPATH_KEY));
                if((pluginsPath != null && !pluginsPath.equals(""))|| classpath)
                    notileandothers.append("-pluginpath ");// NOI18N
                if(pluginsPath != null && !pluginsPath.equals(""))
                    notileandothers.append(pluginsPath);
                
                Object javacPath = properties.get("javac.classpath"); // NOI18N
                Object buildDir = properties.get("build.dir"); // NOI18N
                Object classesDir = properties.get("classes"); // NOI18N
                Object filePath = properties.get("file.reference.build-classes"); // NOI18N
                StringBuffer runClassPath = new StringBuffer();
                if(javacPath != null)
                    runClassPath.append(javacPath.toString()+ File.pathSeparator);
                if(buildDir != null)
                    runClassPath.append(buildDir.toString()+ File.pathSeparator);
                if(classesDir != null)
                    runClassPath.append(classesDir.toString()+ File.pathSeparator);
                if(filePath != null)
                    runClassPath.append(filePath.toString()+ File.pathSeparator);
                
                /*String runClasspath = (String) properties.get("javac.classpath") // NOI18N
                        + File.pathSeparator + properties.get("build.dir") + File.separator + "classes" + // NOI18N
                        File.pathSeparator + properties.get("file.reference.build-classes"); // NOI18N
                 */ 
                if(classpath)
                    notileandothers.append((pluginsPath != null ? File.pathSeparator : "") + runClassPath.toString());// NOI18N
                
            }   
            props.setProperty("jconsole.settings.notile", notileandothers.toString());// NOI18N
            
            // Not resolving custom types
            boolean useClasspath = Boolean.valueOf(properties.getProperty(ManagementCompositePanelProvider.RESOLVE_CLASSPATH_KEY));
            if(!useClasspath)
                props.setProperty("run.classpath", "");// NOI18N
            
            props.setProperty("jconsole.managed.process.url", url);// NOI18N
            ExecutorTask jt = runTarget(project, "-connect-jconsole", props);// NOI18N
            t.getInputOutput().select();
            jt.getInputOutput().getErr().println(MSG_DisplayingJConsole());
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
    
        @Messages("MSG_ErrorConnectingJConsole=Unable to connect jconsole to process. CAUSE : process is not waiting (check main method) or process is dead.")
        public void run() {
            try {
                if(t.isFinished()) {
                    handleApplicationDied(t, MSG_ErrorConnectingJConsole());
                }
                
                int pid = findPID((String) key, t);
                
                if(pid == -1) {
                    handleApplicationDied(t, MSG_ErrorConnectingJConsole());
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
        
        @Messages("MSG_ErrorConnectingRemoteJConsole=Unable to connect jconsole to process. CAUSE : process is not waiting (check main method), RMI port is already used or process is dead.")
        public void run() {
            try {
                if(t.isFinished()) {
                    handleApplicationDied(t, MSG_ErrorConnectingRemoteJConsole());
                }
                
                try {
                    tryConnect((Integer) key, host, t);
                }catch(Exception e) {
                    handleApplicationDied(t, MSG_ErrorConnectingRemoteJConsole());
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
                e.printStackTrace();
                //Not yet connected
                //continue
            }catch(SecurityException se) {
                se.printStackTrace();
                return;
            }catch(Exception ex) {
                ex.printStackTrace();
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
