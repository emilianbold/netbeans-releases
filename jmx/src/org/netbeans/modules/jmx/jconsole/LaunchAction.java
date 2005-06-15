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
            String str = NbBundle.getMessage(LaunchAction.class,
                    "LBL_ActionName");
            console = new OutputConsole(str);
        }
        
        OutputConsole getConsole() {
            return console;
        }
        
        public void run() {
            try {
                JConsoleSettings settings = JConsoleSettings.getDefault();
                String cp = settings.getClassPath();
                String url = settings.getDefaultUrl() == null ? "" : settings.getDefaultUrl();
                String polling = String.valueOf(settings.getPolling());
                String vmOptions = settings.getVMOptions() == null ? "" : settings.getVMOptions();
                String tile = !settings.getTile() ? "-notile" : "";
                String javahome = System.getProperty("jdk.home");
                String fileSep = System.getProperty("file.separator");
                String cpSep = System.getProperty("path.separator");
                
                String commonCmdLine  = javahome + fileSep + "bin" + fileSep +"java" + " "+ vmOptions + " " + 
                        "-classpath ";
                
                String enclosing = "";
                
                String os = System.getProperty("os.name");
                
                if(os.startsWith("Win"))
                    enclosing = "\"";
                
                String classpath = enclosing + cp + cpSep + javahome + fileSep + "lib" + 
                                               fileSep + "jconsole.jar" + enclosing;
                    
                String cmdLine = commonCmdLine + classpath + 
                                 " sun.tools.jconsole.JConsole -interval="  + 
                                 polling + " " + tile + " " + url;
                
                String msg1 = NbBundle.getMessage(LaunchAction.class,
                        "LBL_ActionStartingMessage");
                console.message(msg1);
                console.message(cmdLine);
                
                Process p =
                        Runtime.getRuntime().exec(cmdLine, null);
                
                //Set err reader;
                RequestProcessor rp = new RequestProcessor();
                rp.post(new ErrReader(p.getErrorStream(), console));
                
                started();
                
                
                String msg2 = NbBundle.getMessage(LaunchAction.class,
                        "LBL_ActionStartedMessage");
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
        return (String)NbBundle.getMessage(LaunchAction.class,
                "LBL_ActionName");
    }
    
    
    protected String iconResource() {
        return "org/netbeans/modules/jmx/resources/console.png";
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    public synchronized void performAction() {
        if(isStarted()) {
            String msg = NbBundle.getMessage(LaunchAction.class,
                              "LBL_ActionAlreadyStartedMessage");
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
        String msg = NbBundle.getMessage(LaunchAction.class,
                     "LBL_ActionStoppedMessage");
        console.message(msg);
        console = null;
    }
    
    private boolean started;
    private OutputConsole console;
}
