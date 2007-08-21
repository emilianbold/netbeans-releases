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

import java.net.MalformedURLException;
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
import java.io.Writer;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager ;
import org.netbeans.modules.derby.api.DerbyDatabases;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.sun.api.SunURIManager;
import org.netbeans.spi.db.explorer.DatabaseRuntime;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.Util;
import org.netbeans.modules.derby.spi.support.DerbySupport;
import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Lookup;
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
    private static Process process = null;
    
    private File appServerInstallationDirectory = null;
    
    
    /** Creates a new instance of RegisterPointbase */
    private RegisterPointbase() {
    }
    
    public static  RegisterPointbase getDefault(){
        if (reg==null) {
            reg= new RegisterPointbase();
        }
        return reg;
    }
    private static void copyFile(File file1, File file2) throws IOException {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            fis = new FileInputStream(file1);
            bis = new BufferedInputStream(fis);
            fos = new FileOutputStream(file2);
            bos = new BufferedOutputStream(fos);
            int b;
            while((b=bis.read())!=-1)bos.write(b);
        } finally {
            if (null != bis) {
                try {
                    bis.close();
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                }
            }
            if (null != bos) {
                try {
                    bos.close();
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                }
            }
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                }
            }
            if (null != fos) {
                try {
                    fos.close();
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                }
            }
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
    private void createLocalInstallation(){
        
        String installRoot = appServerInstallationDirectory.getAbsolutePath();
        String dest = System.getProperty("netbeans.user");
        try {
            unzip(this.getClass().getClassLoader().getResourceAsStream("org/netbeans/modules/j2ee/sun/ide/j2ee/db/pointbasescripts.zip") , new File(dest));
            copyFile(new File(installRoot+"/pointbase/databases/sample.dbn"), new File(dest+"/pointbase/databases/sample.dbn"));
            copyFile(new File(installRoot+"/pointbase/databases/sun-appserv-samples.dbn"), new File(dest+"/pointbase/databases/sun-appserv-samples.dbn"));
            try {
                copyFile(new File(installRoot+"/pointbase/databases/sample$2.wal"), new File(dest+"/pointbase/databases/sample$2.wal"));
                copyFile(new File(installRoot+"/pointbase/databases/sun-appserv-samples$2.wal"), new File(dest+"/pointbase/databases/sun-appserv-samples$2.wal"));
            } catch(java.io.FileNotFoundException e){// UR 1 is different than UR2 there see bug 6309618
                //continue the logic.
                try {
                    copyFile(new File(installRoot+"/pointbase/databases/sample$1.wal"), new File(dest+"/pointbase/databases/sample$1.wal"));
                    copyFile(new File(installRoot+"/pointbase/databases/sun-appserv-samples$1.wal"), new File(dest+"/pointbase/databases/sun-appserv-samples$1.wal"));
                } catch(java.io.FileNotFoundException ee){
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ee);
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
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }        
    }
    
    public void   register(File irf){
        if (null != irf  && irf.exists()) {
            String installRoot = irf.getAbsolutePath();
            if (installRoot!=null) {
                
                final FileSystem fs = Repository.getDefault().getDefaultFileSystem();
                
                File derbyInstall = new File(irf,"derby");//NOI18N
                if (!derbyInstall.exists()){
                    derbyInstall = new File(irf,"javadb");//NOI18N for latest Glassfish
                }
                // help support JPA use in J2SE projects, where the user will 
                // need this library... [hence GlassFish only]
                if (ServerLocationManager.isGlassFish(irf)) {
                    registerDerbyLibrary(derbyInstall);
                }
                if (derbyInstall.exists()){
                    final FileObject derb = fs.findResource("Databases/JDBCDrivers/org_apache_derby_jdbc_ClientDriver.xml"); //NOI18N
                    final File installDir = derbyInstall;
                    // create sample db if things are not initialized correctly
                    RequestProcessor.getDefault().post(new ConfigureJavaDBSamples(installDir,derb));
                }
                                
                File localInstall = new File(irf,"pointbase");  //NOI18N
                
                if (localInstall.exists()){
                    configureForPointbaseSamples(installRoot, irf, localInstall, fs);
                } // stop here
            }
        }
    }
    
    static class ConfigureJavaDBSamples implements Runnable {
        
        private Object derb;
        
        private File installDir;
        
        ConfigureJavaDBSamples(File installDir, Object derb) {
            this.installDir = installDir;
            this.derb = derb;
        }
        
        public void run() {
            try {
                String loc = DerbySupport.getLocation();
                File locFile = null;
                if (null != loc) {
                    locFile = new File(loc);
                }
                if ("".equals(loc)) {
                    DerbySupport.setLocation(installDir.getAbsolutePath());
                } else if (null != locFile && (!locFile.exists() || locFile.isFile())) {
                    DerbySupport.setLocation(installDir.getAbsolutePath());
                } else if (null == derb) {
                    //  The user has an incorrect value in here
                    // fix it quietly.
                    DerbySupport.setLocation(installDir.getAbsolutePath());
                }
                if ("".equals(DerbySupport.getSystemHome())) {
                    File dbdir = new File(DerbySupport.getDefaultSystemHome());
                    if (dbdir.exists()==false){
                        dbdir.mkdirs();
                    }
                    DerbySupport.setSystemHome(dbdir.getAbsolutePath());
                }
                
                //now register the sample db
                DerbyDatabases.createSampleDatabase();
            } catch (DatabaseException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
    }

    private void configureForPointbaseSamples(final String installRoot, final File irf, final File localInstall, final FileSystem fs) {
        appServerInstallationDirectory =irf;
        AddPointBaseMenus.execute();
        
        FileObject props = fs.findResource("Databases/JDBCDrivers/com_pointbase_jdbc_jdbcUniversalDriver.xml");
        if (props==null) {
            // Go to the conf dir
            File dbFile = new File(installRoot+"/pointbase/databases/sample.dbn");  //NOI18N
            // if it is writable
            if (dbFile.exists()  && (dbFile.canWrite()==false)) {
                //no write access to the dbs. so we copy them in a location where the ide can RW them
                createLocalInstallation();
            }
            try {
                String driverName = installRoot + RELATIVE_DRIVER_PATH;
                File f = new File(driverName);
                if(f.exists()){
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
                }
            } catch (MalformedURLException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            } catch (DatabaseException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            } catch (RuntimeException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
    }
    
    public String getJDBCDriverClass() {
        return DRIVER;
    }
    
    /**
     * Whether this runtime accepts this connection string.
     */
    public boolean acceptsDatabaseURL(String url){
        return url.startsWith(LOCALHOST_URL_PREFIX);
    }
    
    public boolean isRegisterable() {
        boolean retVal = false;
        if (null == appServerInstallationDirectory) {
            //System.out.println("PDB is null;");
            String instances[] = InstanceProperties.getInstanceList();
            if (null != instances) {
                for (int i = 0; i < instances.length; i++) {
                    int end = instances[i].indexOf(']');
                    if (instances[i].indexOf(SunURIManager.SUNSERVERSURI) > -1 &&
                            end > -1) {
                        File irf = new File(instances[i].substring(1,end));
                        register(irf);
                        break;
                    }
                }
            }
        }
        if (null != appServerInstallationDirectory && appServerInstallationDirectory.exists()) {
            retVal = true;
        }
        return retVal;
    }
    
    /**
     * Is database server up and running.
     */
    public boolean isRunning(){
        if (process!=null){
            try{
                process.exitValue();
                process=null;
            } catch (IllegalThreadStateException e){
                //not exited yet...it's ok
                // TODO -- need a better method to do this test?
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
        File irf = appServerInstallationDirectory;
        File localInstall = null;
        if (null != irf && irf.exists()) {
            String installRoot = irf.getAbsolutePath();
            if (installRoot != null) {
                localInstall = new File(System.getProperty("netbeans.user")+"/pointbase/tools/serveroption");
                if (!localInstall.exists()){
                    localInstall = new File(installRoot+ "/pointbase/tools/serveroption");
                }
            } else {
                Util.showInformation(NbBundle.getMessage(StartAction.class, "ERR_NotThere"));
            }
        }
        return localInstall;
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
        String script = null; //loc.getAbsolutePath() +"/startserver";/// NOI18N
        if (File.separator.equals("\\")) { // NOI18N
            script = loc.getAbsolutePath() +"\\startserver"; // NOI18N
        } else {
            script = loc.getAbsolutePath() +"/startserver"; // NOI18N
        }
        try {
            ExecSupport ee= new ExecSupport();
            process= Runtime.getRuntime().exec(script + suffix,null,loc );
            ee.displayProcessOutputs(process,NbBundle.getMessage(StartAction.class, "LBL_outputtab"),"");
            if (waitTime>0){
                Thread.sleep(waitTime);// to make sure the server is up and running
            }
        } catch (MissingResourceException ex) {
            Util.showInformation(ex.getLocalizedMessage());
        } catch (IOException ex) {
            Util.showInformation(ex.getLocalizedMessage());
        } catch (InterruptedException ex) {
            Util.showInformation(ex.getLocalizedMessage());
        } catch (RuntimeException ex) {
            Util.showInformation(ex.getLocalizedMessage());
        }
    }
    
    
    
    
    /**
     * Stop the database server.
     */
    public void stop(){
        BufferedWriter processIn = null;
        try {
            if (process!=null) {//something to stop...
                processIn = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                processIn.write("q\ny\n");
                processIn.flush();
                process.destroy();
                process=null;
            }
            
        } catch (IOException e) {
            Util.showInformation(e.getMessage());
            process=null;
        } catch (RuntimeException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
            Util.showInformation(ex.getMessage());
            process=null;            
        } finally {
            if (null != processIn) {
                try {
                    processIn.close();
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                            ioe);
                }
            }
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
    /* register the derby driver to the library manager of netbeans
     *
     **/
    private void registerDerbyLibrary(final File location) { // , final String name){
        final Repository rep = (Repository) Lookup.getDefault().lookup(Repository.class);
        final FileObject libsFolder = rep.getDefaultFileSystem().findResource("/org-netbeans-api-project-libraries/Libraries"); //NOI18N
        if (libsFolder!=null){
            try {
                libsFolder.getFileSystem().runAtomicAction(
                        new DerbyLibraryRegistrar(location,libsFolder));
            } catch (FileStateInvalidException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
    }
    
    static class DerbyLibraryRegistrar implements FileSystem.AtomicAction {
        
        private File location;
        
        private FileObject libsFolder;
        
        DerbyLibraryRegistrar(File location, FileObject libsFolder) {
            this.location = location;
            this.libsFolder = libsFolder;
        }
        
        public void run() throws IOException {
            FileLock ld = null;
            java.io.OutputStream outStreamd = null;
            Writer outd = null;
            OutputStreamWriter osw = null;
            try {                
                //  the derby lib driver:
                FileObject derbyLib =null;
                derbyLib = libsFolder.getFileObject("JavaDB" ,"xml");//NOI18N
                if (null == derbyLib) {
                    derbyLib = libsFolder.createData("JavaDB" ,"xml");//NOI18N
                    ld = derbyLib.lock();
                    outStreamd = derbyLib.getOutputStream(ld);
                    osw = new OutputStreamWriter(outStreamd);
                    outd = new BufferedWriter(osw);
                    outd.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE library PUBLIC \"-//NetBeans//DTD Library Declaration 1.0//EN\" \"http://www.netbeans.org/dtds/library-declaration-1_0.dtd\">\n");//NOI18N
                    outd.write("<library version=\"1.0\">\n<name>JAVADB_DRIVER_LABEL</name>\n");//NOI18N
                    outd.write("<type>j2se</type>\n");//NOI18N
                    outd.write("<localizing-bundle>org.netbeans.modules.j2ee.sun.ide.j2ee.db.Bundle</localizing-bundle>\n");//NOI18N
                    outd.write("<volume>\n<type>classpath</type>\n"); //NOI18N
                    outd.write("<resource>jar:"+new File(location.getAbsolutePath()+"/lib/derby.jar").toURI().toURL()+"!/</resource>\n"); //NOI18N
                    outd.write("<resource>jar:"+new File(location.getAbsolutePath()+"/lib/derbyclient.jar").toURI().toURL()+"!/</resource>\n"); //NOI18N
                    outd.write("<resource>jar:"+new File(location.getAbsolutePath()+"/lib/derbynet.jar").toURI().toURL()+"!/</resource>\n"); //NOI18N
                    
                    
                    outd.write("</volume>\n<volume>\n<type>src</type>\n</volume>\n"); //NOI18N
                    outd.write("<volume>\n<type>javadoc</type>\n");  //NOI18N
                    outd.write("</volume>\n</library>"); //NOI18N
                }
            } finally {
                if (null != outd) {
                    try {
                        outd.close();
                    } catch (IOException ioe) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                    }
                }
                if (null != outStreamd) {
                    try {
                        outStreamd.close();
                    } catch (IOException ioe) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                    }
                }
                if (null != ld) {
                    ld.releaseLock();
                }
            }
            
        }
    }    
}

