/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.jmx.jconsole;

import java.util.MissingResourceException;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
/**
 *
 * A test action
 *
 */
public class LaunchAction extends javax.swing.AbstractAction {

    class ErrReader implements Runnable {
        private BufferedReader reader;
        private OutputConsole console;
        
        ErrReader(InputStream err, OutputConsole console) {
            reader = new BufferedReader(new InputStreamReader(err));
            this.console = console;            
        }
        
        public void run() {
            try {
                String s = null;
                while((s = reader.readLine()) != null)
                    console.message(s);
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    class RunAction implements Runnable {
        OutputConsole console;
        public RunAction(OutputConsole console) {
            this.console = console;
        }
        
        /*      
        OutputConsole getConsole() {
            return console;
        }
        */
        public void run() {
            try {
                
                JConsoleSettings settings = JConsoleSettings.getDefault();
                String cp = settings.NETBEANS_CLASS_PATH;
                String settingsCP = settings.getClassPath();
                
                if(settingsCP != null)
                    cp = cp + File.pathSeparator + settingsCP;
                        
                String url = settings.getDefaultUrl() == null ? "" : settings.getDefaultUrl();// NOI18N
                String polling = String.valueOf(settings.getPolling());
                String vmOptions = settings.getVMOptions() == null ? "" : settings.getVMOptions();// NOI18N
                String tile = !settings.getTile() ? "-notile" : "";// NOI18N
                String javahome = System.getProperty("jdk.home");// NOI18N
                String pluginsPath = settings.getPluginsPath();
                String otherArgs = settings.getOtherArgs();
                String commonCmdLine = javahome + File.separator + "bin" + File.separator + "java" + " " + vmOptions + " " + "-classpath ";// NOI18N
                
                String classpath =  cp + File.pathSeparator + javahome + File.separator + "lib" + // NOI18N
                                               File.separator + "jconsole.jar";// NOI18N
                String args = "-interval=" + polling + " " + tile + (otherArgs == null ? "" : " " + otherArgs); // NOI18N
                boolean jdk6 = JConsoleSettings.isNetBeansJVMGreaterThanJDK15();
                List<String> arguments = new ArrayList<String>();
                arguments.add(javahome + File.separator + "bin" + File.separator + "java");// NOI18N
                if(vmOptions != null&& !vmOptions.equals(""))
                    arguments.add(vmOptions);
                arguments.add("-classpath");// NOI18N
                
                arguments.add(classpath);
                arguments.add("sun.tools.jconsole.JConsole");// NOI18N
                //String[] argsp = null;
                if(jdk6) {
                    if(pluginsPath != null && !pluginsPath.equals("")) { // NOI18N
                        arguments.add("-pluginpath");// NOI18N
                        arguments.add(pluginsPath);
                        args = "-pluginpath " + pluginsPath;// NOI18N
                    }
                }
                    
                arguments.add("-interval=" + polling);// NOI18N
                
                if(tile != null && !tile.equals(""))// NOI18N
                    arguments.add(tile);
                
                if(otherArgs != null && !otherArgs.equals(""))// NOI18N
                    arguments.add(otherArgs);
                
                if(url != null && !url.equals(""))// NOI18N
                    arguments.add(url);
                
                //String[] argsp = {javahome + File.separator + "bin" + File.separator + "java",
                //vmOptions, "-classpath", classpath, "-interval=" + polling, tile + (otherArgs == null ? "" : " " + otherArgs),
                //"-pluginpath", };
                
                
                //String enclosing = "";// NOI18N
                
                //String os = System.getProperty("os.name");// NOI18N
                //if(os.startsWith("Win"))// NOI18N
                //    enclosing = "\"";// NOI18N
                
                //String classpath = cp + File.pathSeparator + javahome + File.separator + "lib" + // NOI18N
                      //                         File.separator + "jconsole.jar";// NOI18N
                
                
                
                String cmdLine = commonCmdLine + classpath + 
                                 " sun.tools.jconsole.JConsole "+ args + " " + url;// NOI18N
                
                String msg1 = NbBundle.getMessage(LaunchAction.class,"LBL_ActionStartingMessage");// NOI18N
                
                console.message(msg1);
                
                console.message(cmdLine);
                
                String[] argsp = new String[arguments.size()];
                argsp = arguments.toArray(argsp);
                //for(int i = 0; i< argsp.length; i++)
                //    console.message(argsp[i]);
                Process p =
                        Runtime.getRuntime().exec(argsp, null);
                task.addTaskListener(new RuntimeProcessNodeActionListener(p, console));
                //Set err reader;
                RequestProcessor rp = new RequestProcessor();
                rp.post(new ErrReader(p.getErrorStream(), console));
                
                started();
                
                
                String msg2 = NbBundle.getMessage(LaunchAction.class,"LBL_ActionStartedMessage");// NOI18N
                //console.moveToFront();
                
                
                console.message(msg2);
                
                try {
                    p.waitFor();
                }catch(Exception e) { e.printStackTrace(); }
                
                
                
            }catch(Exception e) {
                console.message(e.toString());
                System.out.println(e.toString());
            } finally{
               stopped(console);
            }
        }
    }
    
    public LaunchAction() {
         putValue(Action.NAME,
                  NbBundle.getMessage(LaunchAction.class,"LBL_ActionName")); // NOI18N
         putValue(Action.SHORT_DESCRIPTION,
                  NbBundle.getMessage(LaunchAction.class,"HINT_StartJConsole")); // NOI18N
         putValue(
                "iconBase", // NOI18N
                "org/netbeans/modules/jmx/jconsole/resources/console.png" //NOI18N
                );
        
        //Needed in Tools|Options|...| ToolBars action icons
         putValue (
            Action.SMALL_ICON, 
            new javax.swing.ImageIcon (ImageUtilities.loadImage("org/netbeans/modules/jmx/jconsole/resources/console.png")) // NOI18N
                );
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return null;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    //public synchronized void performAction() {
    public synchronized void actionPerformed(java.awt.event.ActionEvent evt) {
        if(isStarted()) {
            String msg = NbBundle.getMessage(LaunchAction.class,"LBL_ActionAlreadyStartedMessage");// NOI18N
            console.message(msg);
            return;
        }
        
        started();
        try {
            console = new OutputConsole(NbBundle.getMessage(LaunchAction.class, "LBL_OutputName"));// NOI18N
        } catch (MissingResourceException ex) {
            ex.printStackTrace();
        }//NOI18N

        RunAction action = new RunAction(console);
        //console = action.getConsole();
        //rp.post(action);
        //In order to appear in Runtime|Processes list
        task = org.openide.execution.ExecutionEngine.getDefault().execute(
                NbBundle.getMessage(LaunchAction.class, "LBL_OutputName"), // NOI18N
                action, org.openide.windows.InputOutput.NULL);
    }
    
    synchronized boolean isStarted() {
        return started;
    }
    
    private void started() {
        started = true;
    }
    /**
     * We provide a console. On OSX, it seems that listeners are called in a 
     * very strange ways.
     */
    synchronized void stopped(OutputConsole console) {
        if(!started) return;
        started = false;
        String msg = NbBundle.getMessage(LaunchAction.class,"LBL_ActionStoppedMessage");// NOI18N
        console.message(msg);
        //console.close();
        //console = null;
    }
    
    class RuntimeProcessNodeActionListener implements org.openide.util.TaskListener {
        private Process p;
        private OutputConsole console;
        public RuntimeProcessNodeActionListener(Process p, OutputConsole console) {
            this.p = p;
        }
        
        public void taskFinished(org.openide.util.Task task) {
            try {
                // Check if process is dead
              p.exitValue();
            }catch(IllegalThreadStateException e) {
                //Not dead, kill it
                p.destroy();
                stopped(console);
            }
        }
    }
    private org.openide.execution.ExecutorTask task;
    private boolean started;
    private OutputConsole console;
}
