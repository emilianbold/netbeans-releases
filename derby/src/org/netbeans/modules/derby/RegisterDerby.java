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

package org.netbeans.modules.derby;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import org.netbeans.modules.db.runtime.DatabaseRuntime;


/**
 *
 * @author  ludo
 */
public class RegisterDerby implements DatabaseRuntime {
    
    public static final String INST_DIR = "db-derby-10.1.1.0"; // NOI18N
    
    private static RegisterDerby reg=null;
    
    /** Derby server process */
    protected static Process process  =null;
    
    /** Creates a new instance of RegisterDerby */
    private RegisterDerby() {
    }
    
    public static  RegisterDerby getDefault(){
        if (reg==null)
            reg= new RegisterDerby();
        return reg;
    }
    
 
    /**
     * Whether this runtime accepts this connection string.
     */
    public boolean acceptsConnectionUrl(String url){
        if (url.startsWith("jdbc:pointbase"))
            return true;
        else
            return false;
    }
    
    /**
     * Is database server up and running.
     */
    public boolean isRunning(){
        if (process!=null){
            try{
                int e = process.exitValue();
                process=null;
            } catch (IllegalThreadStateException e){
                //not exited yet...it's ok
                
            }
        }
        return (process!=null);
        
    }
    
    /**
     * Can the database be started from inside the IDE?
     */
    public boolean canStart(){
        //System.out.println("can start!!!");
        return true;
    }
    
    /**
     * Start the database server.
     */
    public void start(){
        start(5000);//wait 5 seconds
    }

    private File getInstallLocation() {
        return InstalledFileLocator.getDefault().locate(INST_DIR, null, false);
    }
    
    /* can return null
     * of the location of the derby scripts to be used
     *
     **/
    private File getScriptsLocation() {
        File installRoot = getInstallLocation();
        if (installRoot == null) {
            showInformation(NbBundle.getMessage(StartAction.class, "ERR_NotThere"));
            return null;
        }
        return new File(installRoot, 
            "frameworks" + File.separator + "NetworkServer" + File.separator + "bin");
    }
    
    /** Returns a file suffix for shell files, either ".ksh" or ".bat"
     */
    private String getFileSuffix() {
        if (Utilities.isWindows()) {
            return ".bat"; // NOI18N
        } else {
            return ".ksh"; // NOI18N
        }
    }
    
    private String[] getEnvironment() {
        /*
        Map env = System.getenv();
        String[] result = new String[env.size() + 1];
        int index = 0;
        Iterator it = env.keySet().iterator();
        while (it.hasNext()) {
            String key = (String)it.next();
            result[index] = key + "=" + env.get(key);
            System.out.println("currentEnv " + result[index]);
            index++;
        }
        result[result.length - 1] = "DERBY_INSTALL=" + installLoc.getAbsolutePath();
         */
        String javaHome = System.getProperty("java.home");
        File installLoc = getInstallLocation();
        if (installLoc == null)
            return null;
        return new String[] {"DERBY_INSTALL=" + installLoc.getAbsolutePath(), 
                             "SystemRoot=" + System.getProperty("Env-SystemRoot"), // needed on Windows
                             "JAVA_HOME=" + javaHome};
    }
    
    /**
     * Start the database server, and wait some time (in milliseconds) to make sure the server is active.
     */
    public void start(int waitTime){
        if (process!=null){// seems to be already running?
            stop();
        }
        String suffix = getFileSuffix();
        File loc =  getScriptsLocation();
        if (loc==null){
            return;//nothing to start...
        }
        File script = new File(loc, "startNetworkServer" + suffix);
        try {
            ExecSupport ee= new ExecSupport();
            String[] environment = getEnvironment();
            process= Runtime.getRuntime().exec(script.getAbsolutePath(), environment, loc);
            ee.displayProcessOutputs(process,NbBundle.getMessage(StartAction.class, "LBL_outputtab"));
            if (waitTime>0){
                Thread.sleep(waitTime);// to make sure the server is up and running
            }
            
        } catch (Exception e) {
            showInformation(e.getLocalizedMessage());
        }
    }
    
    
    
    
    /**
     * Stop the database server.
     */
    public void stop(){
        try {
            if (process==null){//nothing to stop...
                return;
            }
            //BufferedWriter processIn = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            //processIn.write("q\ny\n");
            //processIn.flush();
            String suffix = getFileSuffix();
            File loc =  getScriptsLocation();
            if (loc==null){
                return;//nothing to start...
            }
            File script = new File(loc, "stopNetworkServer" + suffix);
            ExecSupport ee= new ExecSupport();
            Runtime.getRuntime().exec(script.getAbsolutePath(), getEnvironment(), loc);
            
            process.destroy();
        } 
        catch (Exception e) {
            showInformation(e.getMessage());
        }
        finally {
            process=null;
        }
    }
    
    public static void showInformation(final String msg){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        });
        
    }
    
}
