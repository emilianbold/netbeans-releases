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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/**
 *
 * @author  Winston Prakash
 */

package org.netbeans.modules.visualweb.dataconnectivity;

import org.netbeans.modules.visualweb.dataconnectivity.model.JdbcDriverInfoManager;
import org.netbeans.modules.visualweb.dataconnectivity.model.XmlUtil;
import org.netbeans.modules.visualweb.dataconnectivity.project.datasource.ProjectDataSourceTracker;
import org.netbeans.modules.visualweb.dataconnectivity.explorer.ideDb.BundledDatabaseHelper;
import org.netbeans.modules.visualweb.dataconnectivity.model.DataSourceInfoManager;
import org.netbeans.modules.visualweb.dataconnectivity.utils.DbPortUtilities;
import org.netbeans.modules.visualweb.dataconnectivity.utils.SampleDatabaseCreator;


import org.netbeans.modules.visualweb.dataconnectivity.naming.DesignTimeInitialContextFactory;

import java.beans.Introspector;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInstall;

// Comment out the code that was added to create Driver entries for DataDirect drivers
// import java.net.URL;
// import java.net.MalformedURLException;
// import org.netbeans.api.db.explorer.JDBCDriver;
// import org.netbeans.api.db.explorer.JDBCDriverManager;
// import org.netbeans.api.db.explorer.DatabaseException;

/**
 * Initialization code for dataconnectivity module.
 * Copies various files from application bundle into userdir.
 * Defines initial drivers.
 */
public class DataconnectivityModuleInstaller extends ModuleInstall {

    private static String JSFCL_DATA_BEANINFO_PATH = "com.sun.jsfcl.data"; //NOI18N

    public static final String DBPORT_property = "derbyPort" ; // NOI18N

    // String ideHome = System.getProperty("netbeans.home"); //NOI18N

    private String userDir = System.getProperty("netbeans.user"); //NOI18N

    private static final String installPropsReef = "system/install.properties"; //NOI18N
    private static final String installPropsThresher = "config/com-sun-rave-install.properties"; //NOI18N

    public String dsConfPath = userDir + "/jdbc-drivers/" + XmlUtil.DS_CONFIG_FILE; //NOI18N

    // String origDsConfPath = ideHome + "/startup/" + XmlUtil.DS_CONFIG_FILE; //NOI18N
    String origDsConfPath = "startup/" + XmlUtil.DS_CONFIG_FILE; //NOI18N
    String oldDsConfPath = userDir + "/jdbc-drivers/" + XmlUtil.DS_CONFIG_FILE.replaceFirst("[.]", "-2_0.");; //NOI18N

    public void restored() {

        Log.err.log("Entering DataconnectivityModuleInstaller.restored()");
//         long start = System.currentTimeMillis();
//         long time1 = start, time2;

        setBundledDBPort() ;

        DataconnectivitySettings.getInstance() ;

        // Load the bundled database info
        String bundledDBFile = System.getProperty("rave.bundled.database") ;
        BundledDatabaseHelper.getInfo(bundledDBFile) ;

        // Load the Data Source Config information
        File dsInfoFile = new File(dsConfPath);
        if ( !dsInfoFile.exists() )
            copyJdbcDriverInfoFile(dsInfoFile) ;

        //!JK Temporary place to register the rowset customizer.  This call may change.
        //!JK See Carl for details.
        //!JK Also, temporary place to add JSFCL_DATA_BEANINFO_PATH to the beanInfoSearchPath
        org.netbeans.modules.visualweb.insync.live.LiveUnit.registerCustomizer(
                com.sun.sql.rowset.JdbcRowSetXImpl.class.getName(),
                new org.netbeans.modules.visualweb.dataconnectivity.customizers.SqlCommandCustomizer(com.sun.sql.rowset.JdbcRowSetXImpl.class.getName()));
        org.netbeans.modules.visualweb.insync.live.LiveUnit.registerCustomizer(
                com.sun.sql.rowset.CachedRowSetXImpl.class.getName(),
                new org.netbeans.modules.visualweb.dataconnectivity.customizers.SqlCommandCustomizer(com.sun.sql.rowset.CachedRowSetXImpl.class.getName()));
              
        // data source tracking for NB4 JsfProjects - do prelim setup.
        ProjectDataSourceTracker.getInstance() ;
        
        List bisp = Arrays.asList(Introspector.getBeanInfoSearchPath());
        if (!bisp.contains(JSFCL_DATA_BEANINFO_PATH)) {
            bisp = new ArrayList(bisp);
            bisp.add(JSFCL_DATA_BEANINFO_PATH);
            Introspector.setBeanInfoSearchPath((String[])bisp.toArray(new String[0]));
        }

        // create a new context.xml in the userdir (if it doesn't exist).
        checkUserContextFile() ;
        
//         time2 = System.currentTimeMillis();
//         System.err.println("DataconnectivityModuleInstaller.restored() t1 dt=" + (time2 - time1));
//         time1 = time2;
        
        //!JK temporary place to set InitialContextFactoryBuilder because the context is being
        //!JK polluted by jars being loaded by deployment
        DesignTimeInitialContextFactory.setInitialContextFactoryBuilder();
        
        // Disabled, because we defer this until the information is needed
        // Load and fill the DataSource
        // DataSourceInfoManager.getInstance().initModel();

        // Disabled, since we no longer bundle the DataDirect drivers
        // create definitions for the bundled JDBC drivers, if they don't already exist
        // checkDrivers();

//         time2 = System.currentTimeMillis();
//         System.err.println("DataconnectivityModuleInstaller.restored() t2 dt=" + (time2 - time1));
//        time1 = time2;
        
        // Create sample database in Shortfin 
        SampleDatabaseCreator.createAll("travel", "travel", "travel", "TRAVEL", "modules/ext/travel.zip", false, "localhost", 1527);                
       // Won't include other databases yet
       // SampleDatabaseCreator.createAll("jsc", "jsc", "jsc", "JSC", "modules/ext/jsc.zip", true, "localhost", 1527);                
       // SampleDatabaseCreator.createAll("vir", "vir", "vir", "VIR", "modules/ext/vir.zip", true, "localhost", 1527);                

//         time2 = System.currentTimeMillis();
//         System.err.println("DataconnectivityModuleInstaller.restored() t3 dt=" + (time2 - time1) + "  Total: " + (time2 - start));
    }
    
    public void close(){
        JdbcDriverInfoManager.getInstance().save();
        BundledDatabaseHelper.save() ;
    }
    
    
    // public static String compString = "jdbc:pointbase:server://localhost:9092/"; //NOI18N
    public static String compString = "jdbc:derby://localhost:1527/"; //NOI18N
    public void copyJdbcDriverInfoFile(File dsConfFile){
        
        String derbyPort = System.getProperty(DBPORT_property);

        if(!dsConfFile.getParentFile().exists())
            dsConfFile.getParentFile().mkdirs();
        
        // Location of datasourceconfig in application bundle
        File originalDsConfFile = InstalledFileLocator.getDefault().locate( origDsConfPath, null, false );
        
        //System.out.println("Original Config File - " + originalDsConfFile.getAbsolutePath());
        //System.out.println("User Config File - " + dsConfFile.getAbsolutePath());
        try{
            
            //System.out.println("PB Port Number has Changed");
            Log.log("Copying datasourceconfig.xml from application");
            BufferedReader in = new BufferedReader(new FileReader(originalDsConfFile));
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(dsConfFile)));
            String line = null;
            while ( (line = in.readLine()) != null ) {
                // Check if the string has the pointbase urltemplate string
                if((derbyPort != null) && (!derbyPort.equals("1527"))){ //NOI18N
                    if(line.indexOf(compString) != -1){
                        line = line.replaceAll("1527", derbyPort); //NOI18N
                    }
                }
                if ( line.indexOf("</DataSourceConfigList>") != -1)
                    break;
                out.println(line);
            }
            in.close();

            // Copy any user-defined Database Server types from the previous version, if there is one
            File oldDsConfFile = new File (oldDsConfPath);
            if ( oldDsConfFile.exists() ) {
                Log.log("Merging user-defined Database Server types from Creator 2.0");
                in = new BufferedReader(new FileReader(oldDsConfFile));
                while( (line = in.readLine()) != null) {
                    if ( line.indexOf("UserDefined") != -1 ) {
                        // Found a user-defined DB Server type; copy the definition over 
                        out.println(line);
                        while ( (line = in.readLine()).indexOf("</DataSourceConfig>") == -1 ) {
                            out.println(line);
                        }
                        out.println(line);
                    }
                }
                in.close();
                oldDsConfFile.delete();
            }

            out.println("</DataSourceConfigList>");
            out.close();

        }catch(IOException exc){
            exc.printStackTrace();
        }

    }
    
    /***
     * determine the bundled database port.
     * Users:  check rave-install.properties.  
     * Developers:  overruled if RAVE_J2EE_HOME is set (look in pointbase.ini).
     */
    public void setBundledDBPort() {
        // get the properties written by the installer.
        // first look in the new thresher location.  If not found, check the old location.
        File installPropsFile = InstalledFileLocator.getDefault().locate( installPropsThresher ,null,false ) ;
        if ( installPropsFile == null ) {
            installPropsFile = InstalledFileLocator.getDefault().locate( installPropsReef ,null,false ) ;
        }

        String port = null ; // default
        
        String derbyPropertyPort = DbPortUtilities.getDerbyPortFromDerbyProperties() ;
        if ( derbyPropertyPort != null ) {
            port = derbyPropertyPort ;
        } else {
                // read the port from the config file
            String fPort = DbPortUtilities.getPropFromFile( installPropsFile, "derbyPort" ) ;
            if ( fPort != null) port = fPort ;
        } 
        
        if ( port == null)
           port = "1527" ; // default
 
        System.setProperty(DBPORT_property, port); //NOI18N
    }
    
    /***
     * This checks for <userdir>/context.xml
     * The context.xml file has the definitions of Order, Travel, JumpStartCycles and VIR datasources.
     * If <userdir>/context.xml is found, do nothing.
     * If not found, copy the master file from the installation into the userdir,
     * adjusting the derbyPort if necessary.
     */
    public static final String contextFileName = "/context.xml" ; // NOI18N
    public  String ctxPathName = userDir + contextFileName ; //NOI18N
    public  String oldCtxPathName = userDir + "/context-2_0.xml"; //NOI18N
    private String masterFilePath= "startup/samples" + contextFileName ; //NOI18N
    public void checkUserContextFile() {
        
        File ctxFile = new File(ctxPathName) ;
        if ( ctxFile.exists() ) {
            return ;
        }
        File srcFile = InstalledFileLocator.getDefault().locate( masterFilePath ,null,false ) ;
     
        // Copy the context file to the user dir.
        if ( srcFile.exists()) {

            try{
                Log.log("Copying new context.xml") ; //NOI18N
                String derbyPort = System.getProperty(DBPORT_property) ;
                BufferedReader in = new BufferedReader(new FileReader(srcFile));
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(ctxPathName)));
                String line = null;
                while( (line = in.readLine()) != null){
                    // Check if the string has the pointbase urltemplate string
                    if((derbyPort != null) && (!derbyPort.equals("1527"))){ //NOI18N
                        if(line.indexOf(compString) != -1){
                            // Log.log("Found line with pointbase template URL and changing");
                            line = line.replaceAll("1527", derbyPort); //NOI18N
                        }
                    }
                    if ( line.indexOf("</context>") != -1)
                        break;
                    out.println(line);
                }
                in.close();

                // Merge in any user-defined datasources from the previous version, if there are any
                File oldContextFile = new File (oldCtxPathName);
                if ( oldContextFile.exists() ) {

                    Log.log("Merging user-defined datasources from Creator 2.0");
                    BufferedReader inOld = new BufferedReader(new FileReader(oldContextFile));
                    boolean print = true;

                    while( (line = inOld.readLine()) != null) {

                        // Find the start of a datasource
                        if ( line.indexOf("<object ") != -1) {

                            // Don't copy any of the built-in datasources
                            if ( (line.indexOf("\"Order\"") != -1 )                 ||
                                 (line.indexOf("\"JumpStartCycles\"") != -1 )       ||
                                 (line.indexOf("\"Travel\"") != -1 )                ||
                                 (line.indexOf("\"VIR\"") != -1 ))
                            {
                                print = false;
                            }
                    
                            // loop until the end of the datasource
                            while (line.indexOf("</object>") == -1) {
                                if (print) {
                                    out.println(line);
                                }
                                line=inOld.readLine();
                            }
                            if (print) {
                                out.println(line);
                            }
                            print = true;
                        }   
                    }

                    inOld.close();
                    oldContextFile.delete();
                }

                out.println("            </context>");
                out.println("        </context>");
                out.println("    </context>");
                out.println("</rootContext>");
            
                out.close();
            }catch(IOException exc){
                exc.printStackTrace();
            }            
        }
    }

//     /**
//      * Define entries for the bundled (DataDirect) JDBC drivers
//      */
//     void checkDrivers() {
//         try {
//             URL baseURL = InstalledFileLocator.getDefault().locate( "modules/ext/smbase.jar", null, false).toURL();
//             URL utilURL = InstalledFileLocator.getDefault().locate( "modules/ext/smutil.jar", null, false).toURL();

//             /* Get the current list of drivers, so we can check if it's already there */
//             JDBCDriver[] drivers = JDBCDriverManager.getDefault().getDrivers();

//             maybeAddDriver("Oracle Driver", "com.sun.sql.jdbc.oracle.OracleDriver",
//                            "modules/ext/smoracle.jar", baseURL, utilURL, drivers);
//             maybeAddDriver("Microsoft SQL Server Driver", "com.sun.sql.jdbc.sqlserver.SQLServerDriver",
//                            "modules/ext/smsqlserver.jar", baseURL, utilURL, drivers);
//             maybeAddDriver("DB2 Driver", "com.sun.sql.jdbc.db2.DB2Driver",
//                            "modules/ext/smdb2.jar", baseURL, utilURL, drivers);
//             maybeAddDriver("Sybase Driver", "com.sun.sql.jdbc.sybase.SybaseDriver",
//                            "modules/ext/smsybase.jar", baseURL, utilURL, drivers);

//         } catch (IOException exc) {
//             System.out.println(exc.getMessage());
//         } catch (DatabaseException exc) {
//             System.out.println(exc.getMessage());
//         }
//     }

//     /*
//      * Create an entry for a particular driver, if it doesn't already exist
//      */
//     void maybeAddDriver(String name, String clazz, String jarFileName, URL baseURL, URL utilURL, JDBCDriver[] drivers)
//         throws DatabaseException, MalformedURLException
//     {
//         for (int i=0; i<drivers.length; i++) {
//             /* if we have a match on name and class, just return */
//             if (drivers[i].getName().equals(name) &&
//                 drivers[i].getClassName().equals(clazz))
//                 return;
//         }

//         JDBCDriver drv = JDBCDriver.create(
//             name,
//             name,
//             clazz,
//             new URL[] {
//                 InstalledFileLocator.getDefault().locate( jarFileName, null, false).toURL(),
//                 baseURL,
//                 utilURL });
//         JDBCDriverManager.getDefault().addDriver(drv);
//     }

}
