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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.netbeans.modules.db.explorer.driver.JDBCDriver;
import org.netbeans.modules.db.explorer.driver.JDBCDriverManager ;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DatabaseOption;
import org.netbeans.modules.db.explorer.nodes.RootNode;
import org.netbeans.modules.db.explorer.infos.RootNodeInfo;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.db.runtime.DatabaseRuntime;
import org.netbeans.modules.db.runtime.DatabaseRuntimeManager;

import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.Util;
/**
 *
 * @author  ludo
 */
public class RegisterPointbase implements DatabaseRuntime {
    /** The name of the Pointbase driver to create the connection to sample database */
    public static final String DRIVER_NAME = NbBundle.getMessage(RegisterPointbase.class, "LBL_DriverName");
    
    /** The driver to create the connection to sample database */
    public static final String DRIVER = "com.pointbase.jdbc.jdbcUniversalDriver";
    
    /** The database URL to create the connection to sample database */
    public static final String DATABASE_URL  = "jdbc:pointbase://localhost:9092/sample";
    public static final String DATABASE_URL2 = "jdbc:pointbase://localhost:9092/sun-appserv-samples";
    
    /** The user name to create the connection to sample database */
    public static final String USER_NAME = "pbpublic";
    
    /** The schema name to create the connection to sample database */
    public static final String SCHEMA_NAME = "PBPUBLIC";
    
    /** The password to create the connection to sample database */
    public static final String PASSWORD = "pbpublic";
    
    private static final String RELATIVE_DRIVER_PATH = "/pointbase/lib/pbembedded.jar";  //NOI18N
    private static RegisterPointbase reg=null;
    
    /** pointbase server process */
    protected static Process process  =null;
    
    
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
        String installRoot = System.getProperty("com.sun.aas.installRoot");
        String dest = System.getProperty("netbeans.user");
        try{
            unzip(this.getClass().getClassLoader().getResourceAsStream("org/netbeans/modules/j2ee/sun/ide/j2ee/db/pointbasescripts.zip") , new File(dest));
            copyFile(new File(installRoot+"/pointbase/databases/sample.dbn"), new File(dest+"/pointbase/databases/sample.dbn"));
            copyFile(new File(installRoot+"/pointbase/databases/sample$1.wal"), new File(dest+"/pointbase/databases/sample$1.wal"));
            copyFile(new File(installRoot+"/pointbase/databases/sun-appserv-samples.dbn"), new File(dest+"/pointbase/databases/sun-appserv-samples.dbn"));
            copyFile(new File(installRoot+"/pointbase/databases/sun-appserv-samples$1.wal"), new File(dest+"/pointbase/databases/sun-appserv-samples$1.wal"));

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
    public void   register(){
        String installRoot = System.getProperty("com.sun.aas.installRoot");
        if (installRoot==null){
            return;
        }
        
        
        DatabaseRuntimeManager.getDefault(). register(DRIVER,this);
        
        JDBCDriverManager dm= JDBCDriverManager.getDefault();
        JDBCDriver[] drvs = dm.getDriver(DRIVER);
        
        if (drvs.length>0)
            return; //already there
        
        // Go to the conf dir
        File dbFile = new File(installRoot+"/pointbase/databases/sample.dbn");//NOI18N
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
            
            URL[] urls = new URL[1];
            urls[0]= f.toURI().toURL(); //NOI18N
            
            JDBCDriver newDriver = new JDBCDriver(DRIVER_NAME, DRIVER,urls);
            dm.addDriver(newDriver);
            
            DatabaseConnection cinfo = new DatabaseConnection();
            cinfo.setDriverName( DRIVER_NAME );
            cinfo.setDriver( DRIVER );
            cinfo.setDatabase( DATABASE_URL );
            cinfo.setUser( USER_NAME );
            cinfo.setSchema( SCHEMA_NAME );
            cinfo.setPassword( PASSWORD );
            cinfo.setRememberPassword( true );
            
            
            DatabaseConnection cinfo2 = new DatabaseConnection();
            cinfo2.setDriverName( DRIVER_NAME );
            cinfo2.setDriver( DRIVER );
            cinfo2.setDatabase( DATABASE_URL2 );
            cinfo2.setUser( USER_NAME );
            cinfo2.setSchema( SCHEMA_NAME );
            cinfo2.setPassword( PASSWORD );
            cinfo2.setRememberPassword( true );
            DatabaseOption option = RootNode.getOption();
            
            DatabaseRuntimeManager drtm = DatabaseRuntimeManager.getDefault();
            RootNodeInfo rni = drtm.getRootNodeInfo();
            rni.addDatabaseConnection(cinfo);
            rni.addDatabaseConnection(cinfo2);
            
        } catch (Exception e){
            System.out.println(e);
        }
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
    /* can return null
     * of the location of the pointbase scripts to be used
     *
     **/
    public File getScriptsLocation(){
        String installRoot = System.getProperty("com.sun.aas.installRoot");
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
}
