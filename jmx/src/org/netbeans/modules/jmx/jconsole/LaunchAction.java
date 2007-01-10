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
package org.netbeans.modules.jmx.jconsole;

import java.util.MissingResourceException;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
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
                String cp = settings.NETBEANS_CLASS_PATH + File.pathSeparator + 
                        settings.getClassPath();
                String url = settings.getDefaultUrl() == null ? "" : settings.getDefaultUrl();// NOI18N
                String polling = String.valueOf(settings.getPolling());
                String vmOptions = settings.getVMOptions() == null ? "" : settings.getVMOptions();// NOI18N
                String tile = !settings.getTile() ? "-notile" : "";// NOI18N
                String javahome = System.getProperty("jdk.home");// NOI18N
                String pluginsPath = settings.getPluginsPath();
                String otherArgs = settings.getOtherArgs();
                String commonCmdLine = javahome + File.separator + "bin" + File.separator + "java" + " " + vmOptions + " " + "-classpath ";// NOI18N
                
                String enclosing = "";// NOI18N
                
                String os = System.getProperty("os.name");// NOI18N
                
                if(os.startsWith("Win"))// NOI18N
                    enclosing = "\"";// NOI18N
                
                String classpath = enclosing + cp + File.pathSeparator + javahome + File.separator + "lib" + // NOI18N
                                               File.separator + "jconsole.jar" + enclosing;// NOI18N
                String args = "-interval=" + polling + " " + tile + (otherArgs == null ? "" : " " + otherArgs); // NOI18N
                if(JConsoleSettings.isNetBeansJVMGreaterThanJDK15()) {
                    if(pluginsPath != null && !pluginsPath.equals("")) {
                        args = args + " -pluginpath " + pluginsPath;
                    }
                }
                String cmdLine = commonCmdLine + classpath + 
                                 " sun.tools.jconsole.JConsole "+ args + " " + url;// NOI18N
                
                String msg1 = NbBundle.getMessage(LaunchAction.class,"LBL_ActionStartingMessage");// NOI18N
                
                console.message(msg1);
                
                console.message(cmdLine);
                
                Process p =
                        Runtime.getRuntime().exec(cmdLine, null);
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
                "org/netbeans/modules/jmx/resources/console.png" //NOI18N
                );
        
        //Needed in Tools|Options|...| ToolBars action icons
         putValue (
            Action.SMALL_ICON, 
            new javax.swing.ImageIcon (org.openide.util.Utilities.loadImage("org/netbeans/modules/jmx/resources/console.png")) // NOI18N        
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
