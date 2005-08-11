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
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.FileUtil;
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
        if (url.startsWith("jdbc:derby://"))
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
    
    private String getNetworkServerClasspath() {
        return 
            getDerbyFile("lib/derby.jar").getAbsolutePath() + File.pathSeparator + 
            getDerbyFile("lib/derbytools.jar").getAbsolutePath() + File.pathSeparator + 
            getDerbyFile("lib/derbynet.jar").getAbsolutePath();
    }
    
    private File getDerbyFile(String relPath) {
        return InstalledFileLocator.getDefault().locate(INST_DIR + "/" + relPath, null, false);
    }
    
    /* can return null
     * of the location of the derby scripts to be used
     *
     **/
    private File getScriptsLocation() {
        return getDerbyFile("frameworks/NetworkServer/bin");
    }
    
    private String[] getEnvironment() {
        //String javaHome = System.getProperty("java.home");
        File installLoc = getInstallLocation();
        if (installLoc == null)
            return null;
        // PENDING - set derby.system.home to $userdir/derby, so the log file is written there
        return new String[] {"DERBY_INSTALL=" + installLoc.getAbsolutePath()};
        //                     "SystemRoot=" + System.getProperty("Env-SystemRoot"), // needed on Windows
        //                     "JAVA_HOME=" + javaHome};
    }
    
    private JavaPlatform getJavaPlatform() {
        JavaPlatformManager jpm = JavaPlatformManager.getDefault();
        return jpm.getDefaultPlatform(); 
    }
    
    /**
     * Start the database server, and wait some time (in milliseconds) to make sure the server is active.
     */
    public void start(int waitTime){
        if (process!=null){// seems to be already running?
            stop();
        }
        try {
            ExecSupport ee= new ExecSupport();
            String java = FileUtil.toFile(getJavaPlatform().findTool("java")).getAbsolutePath();
            if (java == null)
                throw new Exception (NbBundle.getMessage(RegisterDerby.class, "EXC_JavaExecutableNotFound"));
            // java -Dderby.system.home=<userdir/derbytemp> -classpath  
            //     <DERBY_INSTALL>/lib/derby.jar:<DERBY_INSTALL>/lib/derbytools.jar:<DERBY_INSTALL>/lib/derbynet.jar
            //     org.apache.derby.drda.NetworkServerControl start
            NbProcessDescriptor desc = new NbProcessDescriptor(
              java,
              "-Dderby.system.home=" + System.getProperty("netbeans.user") + File.separator + "derbytemp " +
              "-classpath " + getNetworkServerClasspath() +
              " org.apache.derby.drda.NetworkServerControl start"
            );
            process = desc.exec (
                null,
                getEnvironment(),
                true,
                getScriptsLocation()
            );

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
            String java = FileUtil.toFile(getJavaPlatform().findTool("java")).getAbsolutePath();
            if (java == null)
                throw new Exception (NbBundle.getMessage(RegisterDerby.class, "EXC_JavaExecutableNotFound"));
            // java -Dderby.system.home=<userdir/derbytemp> -classpath  
            //     <DERBY_INSTALL>/lib/derby.jar:<DERBY_INSTALL>/lib/derbytools.jar:<DERBY_INSTALL>/lib/derbynet.jar
            //     org.apache.derby.drda.NetworkServerControl shutdown
            NbProcessDescriptor desc = new NbProcessDescriptor(
              java,
              "-Dderby.system.home=" + System.getProperty("netbeans.user") + File.separator + "derbytemp " +
              "-classpath " + getNetworkServerClasspath() +
              " org.apache.derby.drda.NetworkServerControl shutdown"
            );
            Process shutwownProcess = desc.exec (
                null,
                getEnvironment(),
                true,
                getScriptsLocation()
            );
            shutwownProcess.waitFor();

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
