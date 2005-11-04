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

package org.netbeans.modules.j2ee.sun.ide.j2ee.db;

import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
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
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager ;
import org.netbeans.spi.db.explorer.DatabaseRuntime;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.j2ee.sun.ide.j2ee.PluginProperties;
import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.Util;

import org.openide.ErrorManager;

/**
 *
 * @author  ludo
 * @author  vkraemer
 */
public class RegisterPointbase implements DatabaseRuntime {
    
    /** The driver to create the connection to sample database */
    static final String DRIVER = "com.pointbase.jdbc.jdbcUniversalDriver";
    
    /** The user name to create the connection to sample database */
    private static final String USER_NAME = "pbpublic";                         //NOI18N
    
    /** The schema name to create the connection to sample database */
    private static final String SCHEMA_NAME = "PBPUBLIC";                       //NOI18N
    
    /** The password to create the connection to sample database */
    private static final String PASSWORD = "pbpublic";                          //NOI18N
    
    private static final String RELATIVE_DRIVER_PATH = 
            "/lib/pbembedded.jar";                                              //NOI18N
    
    private static RegisterPointbase reg=null;
    
    /** pointbase server process */
    private static Process process = null;
    
    private File PointbaseInstallationDirectory = null;

    private static String POINTBASE_PREFIX = "jdbc:pointbase://localhost:";     //NOI18N
    private static String DBHOME_TEXT = ",database.home=";                      //NOI18N
    
    
    /** Creates a new instance of RegisterPointbase */
    private RegisterPointbase() {
    }
    
    public static  RegisterPointbase getDefault(){
        if (reg==null)
            reg= new RegisterPointbase();
        return reg;
    }
    private static void copyFile(File file1, File file2) throws IOException {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(file1));
            bos = new BufferedOutputStream(new FileOutputStream(file2));
            int b;
            while((b=bis.read())!=-1)bos.write(b);
        } finally {
            if (null != bis)
                bis.close();
            if (null != bos)
                bos.close();
        }
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

    private File createLocalInstallation(String installRoot) throws IOException {
        String dest = System.getProperty("netbeans.user")+"/pointbase";         //NOI18N
        String windest = System.getProperty("netbeans.user")+"\\pointbase";     //NOI18N
        unzip(this.getClass().getClassLoader().getResourceAsStream("org/netbeans/modules/j2ee/sun/ide/j2ee/db/pointbasescripts.zip") , new File(System.getProperty("netbeans.user")));
        copyFile(new File(installRoot+"/databases/sample.dbn"),             //NOI18N
                new File(dest+"/databases/sample.dbn"));                    //NOI18N
        copyFile(new File(installRoot+"/databases/sun-appserv-samples.dbn"),//NOI18N
                new File(dest+"/databases/sun-appserv-samples.dbn"));       //NOI18N
        try {
            copyFile(new File(installRoot+"/databases/sample$2.wal"),       //NOI18N
                    new File(dest+"/databases/sample$2.wal"));              //NOI18N
            copyFile(new File(installRoot+"/databases/sun-appserv-samples$2.wal"), //NOI18N
                    new File(dest+"/databases/sun-appserv-samples$2.wal")); //NOI18N
        } catch(java.io.FileNotFoundException e){// UR 1 is different than UR2 there see bug 6309618
            try {
                copyFile(new File(installRoot+"/databases/sample$1.wal"),   //NOI18N
                        new File(dest+"/databases/sample$1.wal"));          //NOI18N
                copyFile(new File(installRoot+"/databases/sun-appserv-samples$1.wal"), //NOI18N
                        new File(dest+"/databases/sun-appserv-samples$1.wal")); //NOI18N
            } catch(java.io.FileNotFoundException ee){
                //continue the logic.
                // If we don't copy something
            }
        }
        
        
        PrintStream ps = null;
        try {
            ps = new PrintStream(new BufferedOutputStream(
                    new FileOutputStream(dest+"/tools/serveroption/pbenv.bat")));
            ps.println("set AS_POINTBASE="+installRoot);                        //NOI18N
            ps.println("set AS_POINTBASE_SAMPLESDB="+windest);                  //NOI18N
            ps.println("set PB_CONFIGURED_JAVA_HOME="+System.getProperty("java.home")); //NOI18N
            ps.println("exit /b 0");                                            //NOI18N
        } finally {
            if (null != ps)
                ps.close();
        }
        
        PrintStream ps2 = null;
        try {
            ps2 = new PrintStream(new BufferedOutputStream(
                    new FileOutputStream(dest+"/tools/serveroption/pbenv.conf")));
            
            ps2.println("AS_POINTBASE="+installRoot);                           //NOI18N
            ps2.println("AS_POINTBASE_SAMPLESDB="+dest);                        //NOI18N
            ps2.println("PB_CONFIGURED_JAVA_HOME="+System.getProperty("java.home"));   //NOI18N
        } finally {
            if (null != ps2)
                ps2.close();
        }
        
        if (File.separator.equals("/")) { // NOI18N   now change with the execute flag on Unix!!!
            String cmd[] ={"chmod","+x",
                    dest+"/tools/serveroption/startserver.sh"};                 //NOI18N
                    
            Runtime.getRuntime().exec(cmd );
            cmd[2]=dest+"/tools/serveroption/startconsole.sh";                  //NOI18N
            Runtime.getRuntime().exec(cmd );
            cmd[2]=dest+"/tools/serveroption/startconsole.sh";                  //NOI18N
            Runtime.getRuntime().exec(cmd );
            cmd[2]=dest+"/tools/serveroption/stopserver.sh";                    //NOI18N
            Runtime.getRuntime().exec(cmd );
            cmd[2]=dest+"/tools/serveroption/pbenv.conf";                       //NOI18N
            Runtime.getRuntime().exec(cmd );
        }        
        return new File(dest);        
    }
    
    public void   register(File irf){
        if (null == irf || !irf.exists()) {
            return;
        }
        PointbaseInstallationDirectory = new File(irf,"pointbase");
        if (!PointbaseInstallationDirectory.exists()){
            return ;  
	}      
	
        File localDatabaseInstall = PointbaseInstallationDirectory;
        try{
            File dbFile = new File(PointbaseInstallationDirectory+
                    "/databases/sample.dbn");//NOI18N
            // if it isn't writable make a copy in the someplace the user "owns"
            if (dbFile.exists()  && (dbFile.canWrite()==false)) {
                // no write access to the dbs. so we copy them in a location where
                // the ide can RW them
                localDatabaseInstall = createLocalInstallation(localDatabaseInstall.getAbsolutePath());
            }
            
            String driverName = PointbaseInstallationDirectory + RELATIVE_DRIVER_PATH;
            File f = new File(driverName);
            if(!f.exists()){
                return;
            }
            
            URL[] urls = new URL[1];
            urls[0]= f.toURI().toURL();
            
            String[] connectionUrls = getConnectionUrls(localDatabaseInstall);
                        
            JDBCDriver newDriver = null;
            if (connectionUrls.length > 0) {
                newDriver = JDBCDriver.create(
                        NbBundle.getMessage(RegisterPointbase.class,
                        "LBL_DriverName",localDatabaseInstall.getAbsolutePath()),       //NOI18N
                        DRIVER,urls);
                JDBCDriverManager.getDefault().addDriver(newDriver);
            }
            for (int i = 0; i < connectionUrls.length; i++) {
                DatabaseConnection dbconn = DatabaseConnection.create(newDriver,
                        connectionUrls[i], USER_NAME, SCHEMA_NAME, PASSWORD, true);
                ConnectionManager.getDefault().addConnection(dbconn);
            }
            
        } catch (Exception e){
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,e);
        }
    }
    
    public String getJDBCDriverClass() {
        return DRIVER;
    }
    
    /**
     * Whether this runtime accepts this connection string.
     */
    public boolean acceptsDatabaseURL(String url){
        String databaseHome = getDatabaseHome(url);
        if (url.startsWith(POINTBASE_PREFIX) && databaseHome != null) {
            PointbaseInstallationDirectory = new File(databaseHome);
            if (PointbaseInstallationDirectory.exists() && 
                    PointbaseInstallationDirectory.canWrite()) {
                return true;
            } else {
                PointbaseInstallationDirectory = null;
                return false;
            }
        }
        else
            return false;
    }
    
    private String getDatabaseHome(String url) {
        int dbhomeDex = url.lastIndexOf(DBHOME_TEXT);
        if (dbhomeDex < 0)
            return null;
        int nextCommaDex = url.indexOf(',',dbhomeDex+1);
        if (nextCommaDex < 0) 
            return url.substring(dbhomeDex+15);
        else
            return url.substring(dbhomeDex+15,nextCommaDex);
    }
    
    public boolean isRegisterable() {
        if (null == PointbaseInstallationDirectory || !PointbaseInstallationDirectory.exists()) {
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
        return true;
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
    private /*public*/ File getScriptsLocation(){
        File irf = PointbaseInstallationDirectory;
        if (null == irf || !irf.exists()) {
            return null;
        }
        String installRoot = irf.getAbsolutePath(); 
//        if (installRoot == null) {
//            Util.showInformation(NbBundle.getMessage(StartAction.class, "ERR_NotThere"));
//            return null;
//        }
        
        File localInstall = new File(System.getProperty("netbeans.user")+"/pointbase/tools/serveroption");
        if (localInstall.exists()){
            return localInstall;
        } else{
            return new File(installRoot+ "/../tools/serveroption");
            
        }
        
        
    }
    
    /**
     * Start the database server, and wait some time (in milliseconds) to make sure the server is active.
     */
    void start(int waitTime){
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
    
    private String[] getConnectionUrls(File localInstall) {
        // sanity check
        String[] errVal = new String[0];
        File databaseDir = new File(localInstall,"databases");                  // NOI18N
        if (!databaseDir.exists()) {            
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL,
                    NbBundle.getMessage(RegisterPointbase.class, 
                    "INFO_NO_DATABASES", databaseDir.getAbsolutePath()));       //NOI18N
            return errVal;
        }
        if (!databaseDir.canWrite()) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL,
                    NbBundle.getMessage(RegisterPointbase.class, 
                    "INFO_WRITE_PROT_DATABASES",databaseDir.getAbsolutePath()));//NOI18N
            return errVal;
        }
        File iniFile = new File(databaseDir,"pointbase.ini");

        // get the port info
        int port = 9092;
        if (iniFile.exists() && iniFile.canRead()) {
            Properties iniProps = new Properties();
            try {
                iniProps.load(new FileInputStream(iniFile));
                port = Integer.parseInt(iniProps.getProperty("server.port", "9092"));
            } catch (FileNotFoundException ex) {
                // this is bad because we just checked this!!!
                ErrorManager.getDefault().log(ErrorManager.ERROR,
                        NbBundle.getMessage(RegisterPointbase.class, 
                        "ERR_PROP_FILE_NOT_FOUND", iniFile.getAbsolutePath())); //NOI18N
                return errVal;
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.ERROR,
                        ErrorManager.getDefault().annotate(ex,
                                NbBundle.getMessage(RegisterPointbase.class, 
                                "ERR_PROP_FILE_IOEX", iniFile.getAbsolutePath())));
                return errVal;
            }
        }
        // now to find the databases themselves...
        File[] dbnFiles = databaseDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".dbn");                                   //NOI18N
            }
        });
        if (null == dbnFiles) {
                ErrorManager.getDefault().log(ErrorManager.ERROR,
                        NbBundle.getMessage(RegisterPointbase.class, 
                        "ERR_DBN_FILE_SEARCH", databaseDir.getAbsolutePath())); //NOI18N
                return errVal;            
        }
        String [] retVal = new String[dbnFiles.length];
        for (int i = 0; i < dbnFiles.length; i++) {
            String dbFileName = dbnFiles[i].getName();
            String dbName = dbFileName.substring(0,dbFileName.length()-4);
            retVal[i] = POINTBASE_PREFIX + port + "/" + dbName +
                    DBHOME_TEXT + databaseDir.getAbsolutePath();
        }
        return retVal;
    }

}
