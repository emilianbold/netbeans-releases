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

import org.netbeans.modules.visualweb.dataconnectivity.project.datasource.ProjectDataSourceTracker;
import org.netbeans.modules.visualweb.dataconnectivity.naming.DesignTimeInitialContextFactory;
import java.beans.Introspector;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.modules.visualweb.dataconnectivity.datasource.CurrentProject;
import org.netbeans.modules.visualweb.dataconnectivity.naming.DatabaseSettingsImporter;
import org.netbeans.modules.visualweb.dataconnectivity.naming.DerbyWaiter;
import org.openide.modules.ModuleInstall;
import org.openide.windows.WindowManager;

/**
 * Initialization code for dataconnectivity module.
 * Copies various files from application bundle into userdir.
 * Defines initial drivers.
 */
public class DataconnectivityModuleInstaller extends ModuleInstall {

    private static String JSFCL_DATA_BEANINFO_PATH = "com.sun.jsfcl.data"; //NOI18N
    private static String DATACONNECTIVITY_BEANINFO_PATH 
        = "org.netbeans.modules.visualweb.dataconnectivity.designtime"; // NOI18N

    public void restored() {
        // initialize settings for data source naming option
        DataconnectivitySettings.getInstance() ;
        
        //!JK Temporary place to register the rowset customizer.  This call may change.
        //!JK See Carl for details.
        //!JK Also, temporary place to add JSFCL_DATA_BEANINFO_PATH to the beanInfoSearchPath
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

        // Register the designtime directory for dataconnectivity in the search path
        if (!bisp.contains(DATACONNECTIVITY_BEANINFO_PATH)) {
            bisp = new ArrayList(bisp);
            bisp.add(this.DATACONNECTIVITY_BEANINFO_PATH);
            Introspector.setBeanInfoSearchPath((String[])bisp.toArray(new String[0]));
        }                        
       
        //!JK temporary place to set InitialContextFactoryBuilder because the context is being
        //!JK polluted by jars being loaded by deployment
        DesignTimeInitialContextFactory.setInitialContextFactoryBuilder();
        
        // database registration for sample databases and legacy projects
        init();
                
       // Won't include other databases yet
       // SampleDatabaseCreator.createAll("vir", "vir", "vir", "VIR", "modules/ext/vir.zip", true, "localhost", 1527);                
    }
       
    // Wait for IDE to start before taking care of database registration
    public static void init() {
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            public void run() {
                // code to be invoked when system UI is ready
                CurrentProject.getInstance().setup();
                
                // Dataconnectivity implementation to support Project migration of previous releases projects
                // For previous release userdir migration, if no context file then settings haven't been migrated
                File contextFile =  DatabaseSettingsImporter.getInstance().retrieveMigratedSettingsAtStartup();
                if (contextFile != null)
                    new DerbyWaiter(contextFile.exists());  // waits for Derby drivers to be registered before migrating userdir settings
                else {
                    // Create sample database
                    if (ConnectionManager.getDefault().getConnection("jdbc:derby://localhost:1527/travel [travel on TRAVEL]") == null)
                        new DerbyWaiter(false);
                }
                
            }
        }  );
    }       
}
