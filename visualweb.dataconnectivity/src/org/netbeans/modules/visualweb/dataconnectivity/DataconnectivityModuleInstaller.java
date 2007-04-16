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
import org.netbeans.modules.visualweb.dataconnectivity.datasource.CurrentProject;
import org.netbeans.modules.visualweb.dataconnectivity.naming.DatabaseSettingsImporter;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInstall;
import org.openide.windows.WindowManager;

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

    public void restored() {

        Log.err.log("Entering DataconnectivityModuleInstaller.restored()");
//         long start = System.TimeMillis();
//         long time1 = start, time2;

        setBundledDBPort() ;

        DataconnectivitySettings.getInstance() ;

        // Load the bundled database info
        String bundledDBFile = System.getProperty("rave.bundled.database") ;
        BundledDatabaseHelper.getInfo(bundledDBFile) ;

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
        
//         time2 = System.currentTimeMillis();
//         System.err.println("DataconnectivityModuleInstaller.restored() t1 dt=" + (time2 - time1));
//         time1 = time2;
        
        //!JK temporary place to set InitialContextFactoryBuilder because the context is being
        //!JK polluted by jars being loaded by deployment
        DesignTimeInitialContextFactory.setInitialContextFactoryBuilder();
        
//         time2 = System.currentTimeMillis();
//         System.err.println("DataconnectivityModuleInstaller.restored() t2 dt=" + (time2 - time1));
//        time1 = time2;
        
        // Create sample database in Shortfin 
        SampleDatabaseCreator.createAll("travel", "travel", "travel", "TRAVEL", "modules/ext/travel.zip", false, "localhost", 1527);       
        init();
        
        // Dataconnectivity implementation to support Project migration of previous releases projects
        DatabaseSettingsImporter.getInstance().locateAndRegisterDrivers();
        DatabaseSettingsImporter.getInstance().locateAndRegisterConnections(); 
        
  
       // Won't include other databases yet
       // SampleDatabaseCreator.createAll("jsc", "jsc", "jsc", "JSC", "modules/ext/jsc.zip", true, "localhost", 1527);                
       // SampleDatabaseCreator.createAll("vir", "vir", "vir", "VIR", "modules/ext/vir.zip", true, "localhost", 1527);                

//         time2 = System.currentTimeMillis();
//         System.err.println("DataconnectivityModuleInstaller.restored() t3 dt=" + (time2 - time1) + "  Total: " + (time2 - start));
    }
       
    
    public static void init() {
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            public void run() {
                // code to be invoked when system UI is ready
                CurrentProject.getInstance().setup();
            }
        }  );
    }
    
    // public static String compString = "jdbc:pointbase:server://localhost:9092/"; //NOI18N
    public static String compString = "jdbc:derby://localhost:1527/"; //NOI18N   
    
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
}
