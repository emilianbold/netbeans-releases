/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.jmx.jconsole;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;

/**
 *
 * A test action
 *
 */
public class LaunchAction extends CallableSystemAction {
    
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
        public RunAction() {
            String str = NbBundle.getMessage(LaunchAction.class, "LBL_ActionName");// NOI18N
            console = new OutputConsole(str);
        }
        
        OutputConsole getConsole() {
            return console;
        }
        
        public void run() {
            try {
                JConsoleSettings settings = JConsoleSettings.getDefault();
                String cp = settings.getClassPath();
                String url = settings.getDefaultUrl() == null ? "" : settings.getDefaultUrl();// NOI18N
                String polling = String.valueOf(settings.getPolling());
                String vmOptions = settings.getVMOptions() == null ? "" : settings.getVMOptions();// NOI18N
                String tile = !settings.getTile() ? "-notile" : "";// NOI18N
                String javahome = System.getProperty("jdk.home");// NOI18N
                String fileSep = System.getProperty("file.separator");// NOI18N
                String cpSep = System.getProperty("path.separator");// NOI18N
                
                String commonCmdLine  = javahome + fileSep + "bin" + fileSep +"java" + " "+ vmOptions + " " + // NOI18N
                        "-classpath ";// NOI18N
                
                String enclosing = "";// NOI18N
                
                String os = System.getProperty("os.name");// NOI18N
                
                if(os.startsWith("Win"))// NOI18N
                    enclosing = "\"";// NOI18N
                
                String classpath = enclosing + cp + cpSep + javahome + fileSep + "lib" + // NOI18N
                                               fileSep + "jconsole.jar" + enclosing;// NOI18N
                    
                String cmdLine = commonCmdLine + classpath + 
                                 " sun.tools.jconsole.JConsole -interval="  + // NOI18N
                                 polling + " " + tile + " " + url;// NOI18N
                
                String msg1 = NbBundle.getMessage(LaunchAction.class,"LBL_ActionStartingMessage");// NOI18N
                console.message(msg1);
                console.message(cmdLine);
                
                Process p =
                        Runtime.getRuntime().exec(cmdLine, null);
                
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
                System.out.println(e.toString());
            } finally{
                stopped();
            }
        }
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return null;
    }
    
    // action caption
    public String getName() {
        return (String)NbBundle.getMessage(LaunchAction.class,"LBL_ActionName");// NOI18N
    }
    
    
    protected String iconResource() {
        return "org/netbeans/modules/jmx/resources/console.png";// NOI18N
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    public synchronized void performAction() {
        if(isStarted()) {
            String msg = NbBundle.getMessage(LaunchAction.class,"LBL_ActionAlreadyStartedMessage");// NOI18N
            console.message(msg);
            return;
        }
        
        started();
        
        RequestProcessor rp = new RequestProcessor();
        RunAction action = new RunAction();
        console = action.getConsole();
        rp.post(action);
    }
    
    synchronized boolean isStarted() {
        return started;
    }
    
    private void started() {
        started = true;
    }
    
    synchronized void stopped() {
        started = false;
        String msg = NbBundle.getMessage(LaunchAction.class,"LBL_ActionStoppedMessage");// NOI18N
        console.message(msg);
        console = null;
    }
    
    private boolean started;
    private OutputConsole console;
}
