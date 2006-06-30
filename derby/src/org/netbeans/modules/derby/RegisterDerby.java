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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import javax.swing.SwingUtilities;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.spi.db.explorer.DatabaseRuntime;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor.Message;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;


/**
 *
 * @author  Ludo, Petr Jiricka
 */
public class RegisterDerby implements DatabaseRuntime {
    
    // XXX this class does too much. Should maybe be split into 
    // DatabaseRuntimeImpl and the rest.
    
    private static final ErrorManager LOGGER = ErrorManager.getDefault().getInstance(RegisterDerby.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(ErrorManager.INFORMATIONAL);
    
    
    public static final String NET_DRIVER_CLASS_NAME = "org.apache.derby.jdbc.ClientDriver"; // NOI18N
    
    
    private static RegisterDerby reg=null;
    
    /** Derby server process */
    protected static Process process  =null;
    
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
    
    public String getJDBCDriverClass() {
        return NET_DRIVER_CLASS_NAME;
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

    private String getNetworkServerClasspath() {
        return 
            getDerbyFile("lib/derby.jar").getAbsolutePath() + File.pathSeparator + 
            getDerbyFile("lib/derbytools.jar").getAbsolutePath() + File.pathSeparator + 
            getDerbyFile("lib/derbynet.jar").getAbsolutePath();
    }
    
    private File getInstallLocation() {
        return new File(DerbyOptions.getDefault().getLocation());
    }
    
    boolean hasInstallLocation() {
        File location = getInstallLocation();
        return location.isAbsolute() && location.isDirectory() && location.exists();
    }
    
    private File getDerbyFile(String relPath) {
        return new File(getInstallLocation(), relPath);
    }
    
    private URL[] getDerbyNetDriverURLs() throws MalformedURLException {
        URL[] driverURLs = new URL[1];
        driverURLs[0] = getDerbyFile("lib/derbyclient.jar").toURI().toURL();
        return driverURLs;
    }
    
    private Driver getDerbyNetDriver() throws Exception {
        URL[] driverURLs = getDerbyNetDriverURLs();
        DbURLClassLoader l = new DbURLClassLoader(driverURLs);
        Class c = Class.forName(NET_DRIVER_CLASS_NAME, true, l);
        return (Driver)c.newInstance();
    }
    
    /* Returns the registered Derby driver.
     */
    private JDBCDriver getRegisteredDerbyDriver() throws IOException {
      
        JDBCDriverManager dm = JDBCDriverManager.getDefault();
        JDBCDriver[] drvs = dm.getDrivers(NET_DRIVER_CLASS_NAME);
        
        if (drvs.length > 0) {
            return drvs[0];
        }
        return null;
    }
    
    private int getPort() {
        return 1527;
    }
    
    /** Posts the creation of the new database to request processor.
     */
    void postCreateNewDatabase(final File location, final String user, final String password) throws Exception {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run () {
                try {
                    createNewDatabase(location, user, password);
                }
                catch (Exception e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    String message = NbBundle.getMessage(RegisterDerby.class, "ERR_CreateDatabase", e.getMessage());
                    NotifyDescriptor d  = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(d);
                }
            }
        });
    }
    
    private void createNewDatabase(final File location, String user, String password) throws Exception {
        ProgressHandle ph = ProgressHandleFactory.createHandle(NbBundle.getMessage(
                RegisterDerby.class, "MSG_CreatingDBProgressLabel", location));
        ph.start();
        try {
            Driver driver = getDerbyNetDriver();
            Properties props = new Properties();
            
            String url = "jdbc:derby://localhost:" + getPort() + "/" + location; // NOI18N
            String urlForCreation = url + ";create=true"; // NOI18N
            Connection connection = driver.connect(urlForCreation, props);
            
            boolean setupAuthentication = (user != null && user.length() >= 0);
            
            if (setupAuthentication) {
                setupDatabaseAuthentication(connection, user, password);
            }
            
            connection.close();
            
            if (setupAuthentication) {
                // we have to reboot the database for the authentication properties
                // to take effect
                try {
                    connection = driver.connect(url + ";shutdown=true", props); // NOI18N
                } catch (SQLException e) {
                    // OK, will always occur
                }
            }

            JDBCDriver jdbcDriver = getRegisteredDerbyDriver();
            DatabaseConnection cinfo = DatabaseConnection.create(jdbcDriver, url, user, 
                    setupAuthentication ? user.toUpperCase() : "APP", // NOI18N
                    setupAuthentication ? password : null, setupAuthentication);

            ConnectionManager cm = ConnectionManager.getDefault();
            cm.addConnection(cinfo);
        }
        finally {
            ph.finish();
        }
    }
    
    private void setupDatabaseAuthentication(Connection conn, String user, String password) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("{call SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(?, ?)}"); // NOI18N
        
        stmt.setString(1, "derby.connection.requireAuthentication"); // NOI18N
        stmt.setString(2, "true"); // NOI18N
        stmt.execute();
        
        stmt.clearParameters();
        stmt.setString(1, "derby.authentication.provider"); // NOI18N
        stmt.setString(2, "BUILTIN"); // NOI18N
        stmt.execute();
        
        stmt.clearParameters();
        stmt.setString(1, "derby.user." + user); // NOI18N
        stmt.setString(2, password); // NOI18N
        stmt.execute();
        
        stmt.close();
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
        if (Utilities.OS_MAC == Utilities.getOperatingSystem()) {
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
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } finally {
            if (fileos != null) {
                try {
                    fileos.close();
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
        }
        
    }
            
    private String[] getEnvironment() {
        File installLoc = getInstallLocation();
        if (installLoc == null)
            return null;
        return new String[] {"DERBY_INSTALL=" + installLoc.getAbsolutePath()};
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
        if (!hasInstallLocation()) {
            showInformation(NbBundle.getMessage(RegisterDerby.class, "MSG_DerbyLocationIncorrect"));
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
                LOGGER.log(ErrorManager.INFORMATIONAL, "Running " + desc.getProcessName() + " " + desc.getArguments());
            }
            process = desc.exec (
                null,
                getEnvironment(),
                true,
                getInstallLocation()
            );

            ee.displayProcessOutputs(process,NbBundle.getMessage(StartAction.class, "LBL_outputtab"));
            if (waitTime > 0){
                ee.waitForMessage(NbBundle.getMessage(RegisterDerby.class, "MSG_StartingDerby"),
                    waitTime); // to make sure the server is up and running
                //Thread.currentThread().sleep(waitTime); // to make sure the server is up and running
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
                LOGGER.log(ErrorManager.INFORMATIONAL, "Running " + desc.getProcessName() + " " + desc.getArguments());
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
            showInformation(e.getMessage());
        }
        finally {
            process=null;
        }
    }
    
    static void showInformation(final String msg){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        });
        
    }
    
}
