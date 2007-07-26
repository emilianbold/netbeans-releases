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

package org.netbeans.modules.derby;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.derby.api.DerbyDatabases;
import org.netbeans.spi.db.explorer.DatabaseRuntime;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;


/**
 *
 * @author  Ludo, Petr Jiricka
 */
public class RegisterDerby implements DatabaseRuntime {
    
    // XXX this class does too much. Should maybe be split into 
    // DatabaseRuntimeImpl and the rest.
    
    private static final Logger LOGGER = Logger.getLogger(RegisterDerby.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    
    private static final int START_TIMEOUT = 5; // seconds
    
    private static RegisterDerby reg=null;
    
    /** Derby server process */
    static Process process = null;
    
    /** Creates a new instance of RegisterDerby */
    private RegisterDerby() {
    }
    
    public static synchronized RegisterDerby getDefault(){
        if (reg==null)
            reg= new RegisterDerby();
        return reg;
    }
    
    /**
     * Whether this runtime accepts this connection string.
     */
    public boolean acceptsDatabaseURL(String url){
        return url.trim().startsWith("jdbc:derby://localhost"); // NOI18N
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
    
    public String getJDBCDriverClass() {
        return DerbyOptions.DRIVER_CLASS_NET;
    }
    
    /**
     * Can the database be started from inside the IDE?
     */
    public boolean canStart(){
        // issue 81619: should only try to start if the location is set
        return DerbyOptions.getDefault().getLocation().length() > 0;
    }
    
    /**
     * Start the database server.
     */
    public void start(){
        start(START_TIMEOUT); // wait for Derby to start
    }
    
    /**
     * Starts the database server and does not wait until it has started.
     */
    public void startNoWait() {
        start(0);
    }

    private String getNetworkServerClasspath() {
        return 
            Util.getDerbyFile("lib/derby.jar").getAbsolutePath() + File.pathSeparator +
            Util.getDerbyFile("lib/derbytools.jar").getAbsolutePath() + File.pathSeparator +
            Util.getDerbyFile("lib/derbynet.jar").getAbsolutePath(); // NOI18N
    }
    
    /**
     * Returns the registered Derby driver.
     */
    private JDBCDriver getRegisteredDerbyDriver() {
        JDBCDriver[] drvs = JDBCDriverManager.getDefault().getDrivers(DerbyOptions.DRIVER_CLASS_NET);
        if (drvs.length > 0) {
            return drvs[0];
        }
        return null;
    }
    
    public int getPort() {
        return 1527;
    }
    
    /** Posts the creation of the new database to request processor.
     */
    void postCreateNewDatabase(final String databaseName, final String user, final String password) throws Exception {
        // DerbyDatabases.createDatabase would start the database too, but
        // doing it beforehand to avoid having two progress bars running
        ensureStarted();
        
        RequestProcessor.getDefault().post(new Runnable() {
            public void run () {
                try {
                    ProgressHandle ph = ProgressHandleFactory.createHandle(NbBundle.getMessage(
                        RegisterDerby.class, "MSG_CreatingDBProgressLabel", databaseName));
                    ph.start();
                    try {
                        DerbyDatabases.createDatabase(databaseName, user, password);
                    } finally {
                        ph.finish();
                    }
               } catch (Exception e) {
                    LOGGER.log(Level.WARNING, null, e);
                    String message = NbBundle.getMessage(RegisterDerby.class, "ERR_CreateDatabase", e.getMessage());
                    Util.showInformation(message);
               }
           }
        });
    }
    
    private String getDerbySystemHome() {
        // return System.getProperty("netbeans.user") + File.separator + "derby";
        return DerbyOptions.getDefault().getSystemHome();
    }
    
    private void createDerbyPropertiesFile() {
        File derbyProperties = new File(getDerbySystemHome(), "derby.properties");
        if (derbyProperties.exists())
            return;
        Properties derbyProps = new Properties();
        // fill it
        if (Utilities.isMac()) {
            derbyProps.setProperty("derby.storage.fileSyncTransactionLog", "true");
        }

        // write it out
        OutputStream fileos = null; 
        try {
            File derbyPropertiesParent = derbyProperties.getParentFile();
            derbyPropertiesParent.mkdirs();
            fileos = new FileOutputStream(derbyProperties);
            derbyProps.store(fileos, NbBundle.getMessage(RegisterDerby.class, "MSG_DerbyPropsFile"));
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } finally {
            if (fileos != null) {
                try {
                    fileos.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
            }
        }
        
    }
    
    private File getInstallLocation() {
        String location = DerbyOptions.getDefault().getLocation();
        if (location.equals("")) { // NOI18N
            return null;
        }
        return new File(location);
    }
            
    private String[] getEnvironment() {
        String location = DerbyOptions.getDefault().getLocation();
        if (location.equals("")) { // NOI18N
            return null;
        }
        return new String[] { "DERBY_INSTALL=" + location }; // NOI18N
    }
    
    private JavaPlatform getJavaPlatform() {
        JavaPlatformManager jpm = JavaPlatformManager.getDefault();
        return jpm.getDefaultPlatform(); 
    }
    
    /**
     * Start the database server, and wait some time (in milliseconds) to make sure the server is active.
     */
    private void start(int waitTime){
        if (process!=null){// seems to be already running?
            stop();
        }
        if (!Util.checkInstallLocation()) {
            return;
        }
        try {
            ExecSupport ee= new ExecSupport();
            ee.setStringToLookFor("" + getPort());
            FileObject javaFO = getJavaPlatform().findTool("java");
            if (javaFO == null)
                throw new Exception (NbBundle.getMessage(RegisterDerby.class, "EXC_JavaExecutableNotFound"));
            String java = FileUtil.toFile(javaFO).getAbsolutePath();
            
            // create the derby.properties file
            createDerbyPropertiesFile();
            
            // java -Dderby.system.home="<userdir/derby>" -classpath  
            //     "<DERBY_INSTALL>/lib/derby.jar:<DERBY_INSTALL>/lib/derbytools.jar:<DERBY_INSTALL>/lib/derbynet.jar"
            //     org.apache.derby.drda.NetworkServerControl start
            NbProcessDescriptor desc = new NbProcessDescriptor(
              java,
              "-Dderby.system.home=\"" + getDerbySystemHome() + "\" " +
              "-classpath \"" + getNetworkServerClasspath() + "\"" + 
              " org.apache.derby.drda.NetworkServerControl start"
            );
            if (LOG) {
                LOGGER.log(Level.FINE, "Running " + desc.getProcessName() + " " + desc.getArguments());
            }
            process = desc.exec (
                null,
                getEnvironment(),
                true,
                getInstallLocation()
            );

            ee.displayProcessOutputs(process,NbBundle.getMessage(StartAction.class, "LBL_outputtab"));
            if (waitTime > 0) {
                // to make sure the server is up and running
                waitStart(ee, waitTime);
            }
            
        } catch (Exception e) {
            Util.showInformation(e.getLocalizedMessage());
        }
    }
    
    private void waitStart(ExecSupport execSupport, int waitTime) {
        String waitMessage = NbBundle.getMessage(RegisterDerby.class, "MSG_StartingDerby");
        ProgressHandle progress = ProgressHandleFactory.createHandle(waitMessage);
        progress.start();
        try {
            boolean started = false;
            while (!started) {
                started = execSupport.waitForMessage(waitTime * 1000);
                if (!started) {
                    String title = NbBundle.getMessage(RegisterDerby.class, "LBL_DerbyDatabase");
                    String message = NbBundle.getMessage(RegisterDerby.class, "MSG_WaitStart", waitTime);
                    NotifyDescriptor waitConfirmation = new NotifyDescriptor.Confirmation(message, title, NotifyDescriptor.YES_NO_OPTION);
                    if (DialogDisplayer.getDefault().notify(waitConfirmation) != NotifyDescriptor.YES_OPTION) {
                        break;
                    }
                }
            }
            if (!started) {
                LOGGER.log(Level.WARNING, "Derby server failed to start"); // NOI18N
            }
        } finally {
            progress.finish();
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
            // java -Dderby.system.home="<userdir/derby>" -classpath  
            //     "<DERBY_INSTALL>/lib/derby.jar:<DERBY_INSTALL>/lib/derbytools.jar:<DERBY_INSTALL>/lib/derbynet.jar"
            //     org.apache.derby.drda.NetworkServerControl shutdown
            NbProcessDescriptor desc = new NbProcessDescriptor(
              java,
              "-Dderby.system.home=\"" + getDerbySystemHome() + "\" " +
              "-classpath \"" + getNetworkServerClasspath() + "\"" + 
              " org.apache.derby.drda.NetworkServerControl shutdown"
            );
            if (LOG) {
                LOGGER.log(Level.FINE, "Running " + desc.getProcessName() + " " + desc.getArguments());
            }
            Process shutwownProcess = desc.exec (
                null,
                getEnvironment(),
                true,
                getInstallLocation()
            );
            shutwownProcess.waitFor();

            process.destroy();
        } 
        catch (Exception e) {
            Util.showInformation(e.getMessage());
        }
        finally {
            process=null;
        }
    }
    
    public void ensureStarted() {
        if (!isRunning() && canStart()) {
            startNoWait();
        }
    }
    
    
}
