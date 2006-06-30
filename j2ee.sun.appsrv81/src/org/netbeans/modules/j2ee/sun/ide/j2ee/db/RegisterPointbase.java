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

package org.netbeans.modules.j2ee.sun.ide.j2ee.db;

import java.io.FileNotFoundException;
import java.net.URL;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager ;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.sun.api.SunURIManager;
import org.netbeans.spi.db.explorer.DatabaseRuntime;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.j2ee.sun.ide.j2ee.PluginProperties;
import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.Util;
import org.netbeans.modules.derby.spi.support.DerbySupport;
import org.openide.util.RequestProcessor;
/**
 *
 * @author  ludo
 */
public class RegisterPointbase implements DatabaseRuntime {
    /** The name of the Pointbase driver to create the connection to sample database */
    public static final String DRIVER_DISPLAY_NAME = 
            NbBundle.getMessage(RegisterPointbase.class, "LBL_DriverName");     //NOI18N
    
    public static final String DRIVER_NAME = "pointbase";                       // NOI18N
    
    /** The driver to create the connection to sample database */
    public static final String DRIVER = 
            "com.pointbase.jdbc.jdbcUniversalDriver";                           //NOI18N
    
    /** The user name to create the connection to sample database */
    public static final String USER_NAME = "pbpublic";                          //NOI18N
    
    /** The schema name to create the connection to sample database */
    public static final String SCHEMA_NAME = "PBPUBLIC";                        //NOI18N
    
    /** The password to create the connection to sample database */
    public static final String PASSWORD = "pbpublic";                           //NOI18N
    
    private static final String RELATIVE_DRIVER_PATH = 
            "/pointbase/lib/pbembedded.jar";                                    //NOI18N

    private static RegisterPointbase reg = null;
    
    /** pointbase server process */
    protected static Process process = null;
    
    private File AppServerinstallationDirectory = null;
    
    
    /** Creates a new instance of RegisterPointbase */
    private RegisterPointbase() {
    }
    
    public static  RegisterPointbase getDefault(){
        if (reg==null)
            reg= new RegisterPointbase();
        return reg;
    }
    private static void copyFile(File file1, File file2) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file1));
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file2));
        int b;
        while((b=bis.read())!=-1)bos.write(b);
        bis.close();
        bos.close();
    }
    private static void unzip(InputStream source, File targetFolder) throws IOException {
        //installation
        ZipInputStream zip=new ZipInputStream(source);
        try {
            ZipEntry ent;
            while ((ent = zip.getNextEntry()) != null) {
                File f = new File(targetFolder, ent.getName());
                if (ent.isDirectory()) {
                    f.mkdirs();
                } else {
                    f.getParentFile().mkdirs();
                    FileOutputStream out = new FileOutputStream(f);
                    try {
                        FileUtil.copy(zip, out);
                    } finally {
                        out.close();
                    }
                }
            }
        } finally {
            zip.close();
        }
    }
    private void createLocalInstallation(){

        String installRoot = AppServerinstallationDirectory.getAbsolutePath(); 
        String dest = System.getProperty("netbeans.user");
        try{
            unzip(this.getClass().getClassLoader().getResourceAsStream("org/netbeans/modules/j2ee/sun/ide/j2ee/db/pointbasescripts.zip") , new File(dest));
            copyFile(new File(installRoot+"/pointbase/databases/sample.dbn"), new File(dest+"/pointbase/databases/sample.dbn"));
            copyFile(new File(installRoot+"/pointbase/databases/sun-appserv-samples.dbn"), new File(dest+"/pointbase/databases/sun-appserv-samples.dbn"));
            try {
                copyFile(new File(installRoot+"/pointbase/databases/sample$2.wal"), new File(dest+"/pointbase/databases/sample$2.wal"));
                copyFile(new File(installRoot+"/pointbase/databases/sun-appserv-samples$2.wal"), new File(dest+"/pointbase/databases/sun-appserv-samples$2.wal"));
            } catch(java.io.FileNotFoundException e){// UR 1 is different than UR2 there see bug 6309618
                try {
                    copyFile(new File(installRoot+"/pointbase/databases/sample$1.wal"), new File(dest+"/pointbase/databases/sample$1.wal"));
                    copyFile(new File(installRoot+"/pointbase/databases/sun-appserv-samples$1.wal"), new File(dest+"/pointbase/databases/sun-appserv-samples$1.wal"));
                } catch(java.io.FileNotFoundException ee){
                    //continue the logic.
                }
            }
        

            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dest+"/pointbase/tools/serveroption/pbenv.bat"));
            PrintStream ps = new PrintStream(bos);            
            ps.println("set AS_POINTBASE="+installRoot+"\\pointbase");            
            ps.println("set AS_POINTBASE_SAMPLESDB="+dest+"\\pointbase");
            ps.println("set PB_CONFIGURED_JAVA_HOME="+System.getProperty("java.home"));                        
            ps.println("exit /b 0");
            ps.close();
            BufferedOutputStream bos2 = new BufferedOutputStream(new FileOutputStream(dest+"/pointbase/tools/serveroption/pbenv.conf"));
            PrintStream ps2 = new PrintStream(bos2);
            
            ps2.println("AS_POINTBASE="+installRoot+"/pointbase");            
            ps2.println("AS_POINTBASE_SAMPLESDB="+dest+"/pointbase");
            ps2.println("PB_CONFIGURED_JAVA_HOME="+System.getProperty("java.home"));                        
            ps2.close();  
            
            if (File.separator.equals("/")) { // NOI18N   now change with the execute flag on Unix!!!
                String cmd[] ={"chmod","+x",dest+"/pointbase/tools/serveroption/startserver.sh"};
                
                Runtime.getRuntime().exec(cmd );
                cmd[2]=dest+"/pointbase/tools/serveroption/startconsole.sh";
                Runtime.getRuntime().exec(cmd );
                cmd[2]=dest+"/pointbase/tools/serveroption/startconsole.sh";
                Runtime.getRuntime().exec(cmd );
                cmd[2]=dest+"/pointbase/tools/serveroption/stopserver.sh";
                Runtime.getRuntime().exec(cmd );
                cmd[2]=dest+"/pointbase/tools/serveroption/pbenv.conf";
                Runtime.getRuntime().exec(cmd );
            }

        } catch(Exception e){
            e.printStackTrace();
        }
        
    }
    public void   register(File irf){
        if (null == irf || !irf.exists()) {
            return;
        }
        String installRoot = irf.getAbsolutePath(); 
        if (installRoot==null){
            return;
        }

        final FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        
        final File derbyInstall = new File(irf,"derby");//NOI18N
        if (derbyInstall.exists()){
         FileObject derb = fs.findResource("Databases/JDBCDrivers/org_apache_derby_jdbc_ClientDriver.xml"); //NOI18N
         final File dbsample = new File(DerbySupport.getDefaultSystemHome(),"sample");
         // create sample db if things are not initialized correctly
            if ((derb==null) || (!dbsample.exists()) || ("".equals(DerbySupport.getSystemHome())))    {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        try {
                            File dbdir = new File(DerbySupport.getDefaultSystemHome());
                            if (dbdir.exists()==false){
                                dbdir.mkdirs();
                            }
                            if ("".equals(DerbySupport.getLocation()))
                                DerbySupport.setLocation(derbyInstall.getAbsolutePath());
                            if ("".equals(DerbySupport.getSystemHome()))
                                DerbySupport.setSystemHome(dbdir.getAbsolutePath());
                            
                            //now register the sample db
                                DerbySupport.registerSampleDatabase();
                            
                            //now register the sun-appserv-samples default connexion
                            JDBCDriver[] newDriver = JDBCDriverManager.getDefault().getDrivers("org.apache.derby.jdbc.ClientDriver"); //NOI18N
                            if(newDriver.length>0){
                                FileObject sunSampleCon = fs.findResource("Databases/Connections/jdbc_derby___localhost_1527_sun_.xml"); //NOI18N
                                if (sunSampleCon==null){
                                    DatabaseConnection dbconn = DatabaseConnection.create(newDriver[0],
                                        "jdbc:derby://localhost:1527/sun-appserv-samples;create=true", //NOI18N
                                        "APP", "APP", "APP", true); //NOI18N
                                ConnectionManager.getDefault().addConnection(dbconn);
                                }
                            }

                        } catch (DatabaseException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        } 
        

        File localInstall = new File(irf,"pointbase");  //NOI18N

        if (!localInstall.exists()){
            return ;  
	}      
   ///     JDBCDriver[] drvs = JDBCDriverManager.getDefault().getDrivers(DRIVER);
	//now it is a good one.
        AppServerinstallationDirectory =irf;
        AddPointBaseMenus.execute();
        
        FileObject props = fs.findResource("Databases/JDBCDrivers/com_pointbase_jdbc_jdbcUniversalDriver.xml");
        if (props!=null)// 
  
            return; //already there
	
     
        // Go to the conf dir
        File dbFile = new File(installRoot+"/pointbase/databases/sample.dbn");  //NOI18N
        // if it is writable
        if (dbFile.exists()  && (dbFile.canWrite()==false)) {
            //no write access to the dbs. so we copy them in a location where the ide can RW them
            createLocalInstallation();
        }
        
        try{
            
            String driverName = installRoot + RELATIVE_DRIVER_PATH;
            //   System.out.println("Pointbase Driver: " + driverName); //NOI18N
            File f = new File(driverName);
            if(!f.exists()){
                return;
            }
            File dbDir = new File(localInstall,"databases");                    //NOI18N
            int portVal = getPort(dbDir);
            
            URL[] urls = new URL[1];
            urls[0]= f.toURI().toURL(); //NOI18N
            
            JDBCDriver newDriver = JDBCDriver.create(DRIVER_NAME, DRIVER_DISPLAY_NAME, DRIVER,urls);
            JDBCDriverManager.getDefault().addDriver(newDriver);
            
            
            File testFile = new File(dbDir,SAMPLE_NAME+DOT_DBN);
            if (testFile.exists()) {
                DatabaseConnection dbconn = DatabaseConnection.create(newDriver, 
                        LOCALHOST_URL_PREFIX+portVal+SLASH+SAMPLE_NAME,
                        USER_NAME, SCHEMA_NAME, PASSWORD, true);
                ConnectionManager.getDefault().addConnection(dbconn);
            }

            testFile = new File(dbDir,SUN_APPSERV_SAMPLES_NAME+DOT_DBN);
            if (testFile.exists()) {
                DatabaseConnection dbconn2 = DatabaseConnection.create(newDriver, 
                        LOCALHOST_URL_PREFIX+portVal+SLASH+SUN_APPSERV_SAMPLES_NAME,
                        USER_NAME, SCHEMA_NAME, PASSWORD, true);
                ConnectionManager.getDefault().addConnection(dbconn2);
            }
        } catch (Exception e){
            System.out.println(e);
        }
    }
    
    public String getJDBCDriverClass() {
        return DRIVER;
    }
    
    /**
     * Whether this runtime accepts this connection string.
     */
    public boolean acceptsDatabaseURL(String url){
        if (url.startsWith(LOCALHOST_URL_PREFIX))
            return true;
        else
            return false;
    }
    
    public boolean isRegisterable() {
        if (null == AppServerinstallationDirectory) {
            //System.out.println("PDB is null;");
            String instances[] = InstanceProperties.getInstanceList();
            if (null == instances) {
                // No hope here.
                return false;
            } else {
                for (int i = 0; i < instances.length; i++) {
                    if (instances[i].indexOf(SunURIManager.SUNSERVERSURI) > 0) {
                        int end = instances[i].indexOf (']');
                        if (end > 1) {
                            File irf = new File(instances[i].substring(1,end));
                            register(irf);
                            break;
                        }
                    }
                }
            }
            if (null == AppServerinstallationDirectory)
                return false;
        }
        if (!AppServerinstallationDirectory.exists()) {
            return false;
        }
        return true;
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
        
        // only can start if already installed
        return System.getProperty("com.sun.aas.installRoot") != null;
    }
    
    /**
     * Start the database server.
     */
    public void start(){
        start(5000);//wait 5 seconds
    }
    /* can return null
     * of the location of the pointbase scripts to be used
     *
     **/
    public File getScriptsLocation(){
        File irf = AppServerinstallationDirectory;
        if (null == irf || !irf.exists()) {
            return null;
        }
        String installRoot = irf.getAbsolutePath(); 
        if (installRoot == null) {
            Util.showInformation(NbBundle.getMessage(StartAction.class, "ERR_NotThere"));
            return null;
        }
        
        File localInstall = new File(System.getProperty("netbeans.user")+"/pointbase/tools/serveroption");
        if (localInstall.exists()){
            return localInstall;
        } else{
            return new File(installRoot+ "/pointbase/tools/serveroption");
            
        }
        
        
    }
    
    /**
     * Start the database server, and wait some time (in milliseconds) to make sure the server is active.
     */
    public void start(int waitTime){
        if (process!=null){// seems to be already running?
            stop();
        }
        String suffix;
        if (File.separator.equals("\\")) { // NOI18N
            suffix = ".bat"; // NOI18N
        } else {
            suffix = ".sh"; // NOI18N
        }
        File loc =  getScriptsLocation();
        if (loc==null){
            return;//nothing to start...
        }
        String script = loc.getAbsolutePath() +"/startserver";/// NOI18N
        if (File.separator.equals("\\")) { // NOI18N
            script = loc.getAbsolutePath() +"\\startserver"; // NOI18N
        } else {
            script = loc.getAbsolutePath() +"/startserver"; // NOI18N
        }
        try {
            
            ExecSupport ee= new ExecSupport();
            process= Runtime.getRuntime().exec(script + suffix,null,loc );
            ee.displayProcessOutputs(process,NbBundle.getMessage(StartAction.class, "LBL_outputtab"));
            if (waitTime>0){
                Thread.sleep(waitTime);// to make sure the server is up and running
            }
            
        } catch (Exception e) {
            Util.showInformation(e.getLocalizedMessage());
            
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
            BufferedWriter processIn = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            processIn.write("q\ny\n");
            processIn.flush();
            
            process.destroy();
            process=null;
        } catch (Exception e) {
            Util.showInformation(e.getMessage());
            process=null;
            
        }
    }
    
    private static final String POINTBASE_URL_PREFIX = "jdbc:pointbase:";       //NOI18N
    private static final String LOCALHOST_URL_PREFIX = 
            POINTBASE_URL_PREFIX + "//localhost:";                              //NOI18N
    private static final String SAMPLE_NAME = "sample";                         //NOI18N
    private static final String SUN_APPSERV_SAMPLES_NAME = 
            "sun-appserv-samples";                                              //NOI18N
    private static final String DOT_DBN = ".dbn";                               //NOI18N 
    private static final String SLASH = "/";                                    //NOI18N
    
    private int getPort(File databaseDir) throws IOException {
        File iniFile = new File(databaseDir,"pointbase.ini");                   //NOI18N
        
        // get the port info
        int port = 9092;
        Properties iniProps = new Properties();
        iniProps.load(new FileInputStream(iniFile));
        port = Integer.parseInt(iniProps.getProperty("server.port", "9092"));   //NOI18N
        return port;
    }
}

