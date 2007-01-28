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
/*
 * DataSourceResolver.java
 *
 * Created on September 6, 2006, 10:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.visualweb.dataconnectivity.datasource;

import org.netbeans.modules.visualweb.api.j2ee.common.RequestedJdbcResource;
import org.netbeans.modules.visualweb.dataconnectivity.model.DataSourceInfo;
import org.netbeans.modules.visualweb.dataconnectivity.model.DataSourceInfoListener;
import org.netbeans.modules.visualweb.dataconnectivity.model.DataSourceInfoManager;
import org.netbeans.modules.visualweb.dataconnectivity.model.JdbcDriverInfo;
import org.netbeans.modules.visualweb.dataconnectivity.model.JdbcDriverInfoListener;
import org.netbeans.modules.visualweb.dataconnectivity.model.JdbcDriverInfoManager;
import org.netbeans.modules.visualweb.dataconnectivity.model.ProjectDataSourceManager;
import org.netbeans.modules.visualweb.dataconnectivity.project.datasource.ProjectDataSourceTracker;
import org.netbeans.modules.visualweb.dataconnectivity.ui.AddDataSourceDialog;
import org.netbeans.modules.visualweb.project.jsf.services.DesignTimeDataSourceService;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.project.Project;
import org.openide.ErrorManager;
import org.openide.util.Lookup;

/**
 *
 * @author John Baker
 */
public class DataSourceResolver implements JdbcDriverInfoListener, DataSourceInfoListener {
    private static final int TRUE = 1;
    private static final int FALSE = 0;
    private static final int RESET = -1;
    private static DataSourceResolver dataSourceResolver;
    private JdbcDriverInfo jdbcDriverInfo = null;
    private String dataSourceInfo = null;
    private final static JDBCDriver[] NULL_JDBC_DRIVER_ARRAY = new JDBCDriver[0];

    /** Creates a new instance of DataSourceResolver */
    private DataSourceResolver() {
    }

    public static DataSourceResolver getInstance() {
        if (dataSourceResolver == null){
            dataSourceResolver = new DataSourceResolver();
        }
        return dataSourceResolver;
    }

    public void jdbcDriverInfoModified(org.netbeans.modules.visualweb.dataconnectivity.model.JdbcDriverInfoEvent modelEvent) {
        jdbcDriverInfo = modelEvent.getJdbcDriverInfoDs();
    }

    public void jdbcDriverInfoAdded(org.netbeans.modules.visualweb.dataconnectivity.model.JdbcDriverInfoEvent modelEvent) {
        jdbcDriverInfo = modelEvent.getJdbcDriverInfoDs();
    }

    public void jdbcDriverInfoRemoved(org.netbeans.modules.visualweb.dataconnectivity.model.JdbcDriverInfoEvent modelEvent) {
        ; // no-op
    }

    public void dataSourceInfoModified(org.netbeans.modules.visualweb.dataconnectivity.model.DataSourceInfoEvent modelEvent) {
        dataSourceInfo = modelEvent.getDataSourceInfoId();
    }

    public void datasourceInfoAdded(org.netbeans.modules.visualweb.dataconnectivity.model.DataSourceInfoEvent modelEvent) {
        dataSourceInfo = modelEvent.getDataSourceInfoId();
    }

    public void dataSourceInfoRemoved(org.netbeans.modules.visualweb.dataconnectivity.model.DataSourceInfoEvent modelEvent) {
        ; // no-op
    }

    public Set getProblemDataSources(Project project) {
        int match; // TRUE if data source in context matches data source in project
        Set<String> problemDataSources = null;

        //***** CHECK IDE STARTUP
//        System.err.println("jb: BEFORE DATASOURCE CHECK - " + Calendar.getInstance().getTime());

        // collect the datasources JNDI names from DataSourceInfoManager
        Set ideDataSources = DataSourceInfoManager.getInstance().getSortedDataSourceNames();
        
        // collect all the JNDI names used in the project
        String[] dynamicDataSources = ProjectDataSourceTracker.getDynamicDataSources(project );
        String[] hardCodedDataSources = ProjectDataSourceTracker.getHardcodedDataSources(project);
        
        // If both project and IDE don't have any then match is TRUE
        if (ideDataSources.isEmpty() && (dynamicDataSources.length == 0 && hardCodedDataSources.length == 0))
            match = TRUE;
        // Test for match if both project and IDE have data sources
        else {
            //  Check if dynamicDataSources  & hardCodedDataSources  are available in the ideDataSources
            //  If not found, add them to the Set problemDataSources
            problemDataSources = new HashSet<String>();
            Iterator ideDs = ideDataSources.iterator();
            String ideDsName;
            
            // Check for any missing hardcoded data sources, add if any by comparing the IDE data sources with the project's
            for (int i=0; i < hardCodedDataSources.length; i++) {
                // parse connection string, java:/comp/env/jdbc/XXX, to retrieve just the JNDI /jdbc/XXXX
                String projectDsName = hardCodedDataSources[i].substring(hardCodedDataSources[i].indexOf("jdbc")+"jdbc/".length());
                match = RESET; // reset flag
                
                while (ideDs.hasNext()) {
                    ideDsName = (String)ideDs.next();
                    
                    if (ideDsName.equals(projectDsName)) {
                        match = TRUE;
                        break; // data source match made, stop checking
                    } else
                        match = FALSE;
                }
                
                if (match == FALSE)
                    if (!problemDataSources.contains(projectDsName))
                        problemDataSources.add(projectDsName);
                
                // reset iterator
                ideDs = ideDataSources.iterator();
            }
            
            // Check for any missing dynamic data sources, add if any
            for (int i=0; i < dynamicDataSources.length; i++) {
                // parse connection string, java:/comp/env/jdbc/XXX, to retrieve just the JNDI /jdbc/XXXX
                String projectDsName = dynamicDataSources[i].substring(dynamicDataSources[i].indexOf("jdbc")+"jdbc/".length());
                match = RESET;  //reset
                
                while (ideDs.hasNext()) {
                    ideDsName = (String)ideDs.next();
                    
                    if (ideDsName.equals(projectDsName)) {
                        match = TRUE;
                        break; // data source match made, stop checking
                    } else
                        match = FALSE;
                }
                
                // handle case if context.xml has no entries
                if (ideDataSources.isEmpty())
                    match = FALSE;
                
                // Add any projects that have problem data sources (no data sources in context)
                if (match == FALSE)
                    if (!problemDataSources.contains(projectDsName))
                        problemDataSources.add(projectDsName);
                
                // reset iterator
                ideDs = ideDataSources.iterator();
            }
            
            // *** for testing purposes only
//            Iterator testProb = problemDataSources.iterator();
//            while (testProb.hasNext()) {
//                System.err.println(" Project = " + project.toString() + "  ## missing datasource = " + (String)testProb.next());
//            }
        }
        
//        System.err.println("jb: AFTER DATASOURCE CHECK - " + Calendar.getInstance().getTime());
        
        return problemDataSources;
    }
    
    
    // Checks if datasource already exists.
    public boolean dataSourceExists(Project project, String item) {
        ProjectDataSourceManager projectDataSourceManager = new ProjectDataSourceManager(project);
        DataSourceInfo dsInfo = DataSourceInfoManager.getInstance().getDataSourceInfoByName(item);
        if (dsInfo == null)
            return false;
        else
            return projectDataSourceManager.matchDataSourceInfo(projectDataSourceManager.getDataSourceWithName(item), dsInfo);
    }
    
    // if a project data source is not known to the IDE (data source is missing) then return true
    public boolean isDataSourceMissing(Project project, String prjDsName) {
        Set ideDataSources = DataSourceInfoManager.getInstance().getSortedDataSourceNames();
        boolean missing = true;
        
        if (!ideDataSources.isEmpty()) {
            Iterator ideDs = ideDataSources.iterator();
            String ideDsName;
            
            while (ideDs.hasNext()) {
                ideDsName = (String)ideDs.next();
                
                if (ideDsName.equals(prjDsName)) {
                    missing = false;
                    break; // data source match made, stop checking
                }
            }
        }
        
        return missing;
    }
    
    
    
    
    
    // Find a matching driver registered with the IDE
    private JDBCDriver findMatchingDriver(DataSourceInfo dsInfo) {
        int i = 0;
        
        JDBCDriver[] newDrivers;
        List theDriver = new ArrayList();
        newDrivers = JDBCDriverManager.getDefault().getDrivers();
        boolean found = false;
        
        if (dsInfo != null) {
            if (newDrivers.length == 0)
                newDrivers = (JDBCDriver[]) theDriver.toArray(NULL_JDBC_DRIVER_ARRAY);
            
            if (newDrivers != null) {
                for (i=0; i<newDrivers.length; i++) {
                    if (newDrivers[i].getClassName().equals(dsInfo.getDriverClassName())) {
                        found = true;
                        break;
                    }
                }
            }
        }
        
        if (!found)  {
            i = 0;
            //newDrivers = (JDBCDriver[]) theDriver.toArray(NULL_JDBC_DRIVER_ARRAY);
            return null;
        } else
            return newDrivers[i];
    }
    
    
    
    // Add a Data Source using the Add Data Source dialog;
    // Add a driver using the Add Driver dialog
    // Add a connection through the DataConnection APIs
    public boolean addDataSource(Project project, String itemSelected)  {
        
        if (!dataSourceExists(project, itemSelected)) {
            // Show the Add Data Source dialog
            new AddDataSourceDialog(false, itemSelected).showDialog();
            DataSourceInfo dsInfo = DataSourceInfoManager.getInstance().getDataSourceInfoByName(itemSelected);
        }
        
        DataSourceInfo dsInfo = DataSourceInfoManager.getInstance().getDataSourceInfoByName(itemSelected);
        String jdbcDriverName = "";
        
        // If no data source has been registered then add the driver
        if (dsInfo == null) {
            if (dataSourceResolved(project, itemSelected))
                addDriver(dsInfo);
        } else {
            // data source has been added
            NoSelectedServerWarning.getSelectedServerDialog().dispose();
            
            // next check to see if the driver has been added
            JDBCDriver matchingDriver = findMatchingDriver(dsInfo);
            if (matchingDriver != null)
                jdbcDriverName = matchingDriver.getName();
            
            // if the driver hasn't been added yet
            if (matchingDriver == null) {
                // if the data source has been added then proceed and add the driver
                if (dataSourceResolved(project, itemSelected))
                    addDriver(dsInfo);
            }
        }
        
        // if user cancels the Add Data Connection then do nothing else add a connection
//        if (dsInfo == null) {
//            new AddDataSourceDialog(false, itemSelected).showDialog();
//            dsInfo = DataSourceInfoManager.getInstance().getDataSourceInfoByName(itemSelected);
//        } else {
        if (dsInfo != null) {
            // make sure the driver has been added before adding the connection
            if (findMatchingDriver(dsInfo) == null)
                addDriver(dsInfo);
            else {
                
                // Add the connection
                try {
                    JdbcDriverInfo jdbcInfo = JdbcDriverInfoManager.getInstance().getCurrentJdbcDriverInfo();
                    dsInfo = DataSourceInfoManager.getInstance().getDataSourceInfoByName(itemSelected);
                    String username = dsInfo.getUsername();
                    String password = dsInfo.getPassword();
                    JDBCDriver drvs = findMatchingDriver(dsInfo);
                    DatabaseConnection dbconn = DatabaseConnection.create(drvs, dsInfo.getUrl(), username,  username.toUpperCase(), password,  true); // NOI18N
                    ConnectionManager.getDefault().addConnection(dbconn);
                    updateProject(project, dsInfo);
                    ProjectDataSourceTracker.refreshDataSources(project);
                    ProjectDataSourceTracker.refreshDataSourceReferences(project);
                } catch (DatabaseException de) {
                    de.printStackTrace();
                }
            }
        }
        
        
        return true;
    }
    
    public JDBCDriver addDriver(DataSourceInfo dsInfo) {
        JDBCDriver driver = null ;
        
        try {
            // Add the driver
            JarFile jf;
            String drv;
            String jarName = "";
            
            JdbcDriverInfo jdbcInfo = JdbcDriverInfoManager.getInstance().getCurrentJdbcDriverInfo();
            List jars  = jdbcInfo.getJarNames();
            Iterator jar = jars.iterator();
            while (jar.hasNext())
                jarName = (String) jar.next();
            
            String userDir = System.getProperty("netbeans.user"); // NOI18N
            URL url = (new File(userDir + System.getProperty("file.separator") + "jdbc-drivers" + System.getProperty("file.separator") + jarName)).toURL();
            driver = JDBCDriver.create(dsInfo.getName(), jdbcInfo.getDisplayName(), jdbcInfo.getDriverClassName(), new URL[] {url});
            JDBCDriverManager.getDefault().addDriver(driver);
            
        } catch (DatabaseException de) {
            de.printStackTrace();
        } catch(MalformedURLException mue) {
            ErrorManager.getDefault().notify(mue);
        }
        
        return driver;
    }
    
    // check to see if the Data Source has been resolved
    private boolean dataSourceResolved(Project currentProject, String item) {
        String[] remainingDataSources = getProblemDataSourceInstances(currentProject);
        boolean resolved = true;
        
        for (int i=0; i<remainingDataSources.length; i++) {
            if (remainingDataSources[i].equals(item)) {
                resolved = false;
                break;
            }
        }
        
        return resolved;
    }
    
    
// return String array of the missing data sources
    public String[] getProblemDataSourceInstances(Project currentProject) {
        String[] instances;
        
        Set problemDataSources = getProblemDataSources(currentProject);
        instances = new String[problemDataSources.size()];
        Iterator dataSourceItem = problemDataSources.iterator();
        
        int i = 0;
        while (dataSourceItem.hasNext()) {
            instances[i++] = (String)dataSourceItem.next();
        }
        
        return instances;
    }
    
    private boolean updateProject(Project project, DataSourceInfo dsInfo) {
        boolean needAdd = false;
        ProjectDataSourceManager projectDataSourceManager = new ProjectDataSourceManager(project);
        
        // if project's datasource hasn't been added, then add it
        if (projectDataSourceManager.getDataSourceWithName(dsInfo.getName()) == null) {
            projectDataSourceManager.addDataSource(dsInfo);
            needAdd = true;
        } else
            needAdd = false;
        
        return needAdd;
        
    }
    
    // Check if any project resources are missing.  If any are missing then add to the project
    public void updateProjectDataSources(Project project) {                
        // get list of all data sources used in the project
        String[] dynamicDataSources = ProjectDataSourceTracker.getDynamicDataSources(project );
        String[] hardCodedDataSources = ProjectDataSourceTracker.getHardcodedDataSources(project);
        boolean missing = false;
        DataSourceInfo dsInfo = null;
        Set<RequestedJdbcResource> ret = new HashSet<RequestedJdbcResource>();
        DesignTimeDataSourceService dataSourceService = null;
        RequestedJdbcResource reqResource = null;
        Iterator<RequestedJdbcResource> prjDataResources = null;
        
        // make sure project doesn't have any dynamic data sources
        if (dynamicDataSources != null && hardCodedDataSources != null) {
            dataSourceService = (DesignTimeDataSourceService)Lookup.getDefault().
                    lookup(DesignTimeDataSourceService.class);
            
            // Update project's resources if server resources not found, based on a project's dynamic data sources
            for (int i=0; i<dynamicDataSources.length; i++) {
                dsInfo = DataSourceInfoManager.getInstance().getDataSourceInfoByName(dynamicDataSources[i].substring(dynamicDataSources[i].indexOf("jdbc")+"jdbc/".length()));
                
               // see if the server has the project's data sources'
                ProjectDataSourceManager projectDataSourceManager = new ProjectDataSourceManager(project);
                
                // retrieve a list of server data sources
                Set<RequestedJdbcResource> serverDataSources = dataSourceService.getServerDataSources(project);
                Iterator<RequestedJdbcResource> serverDataSourcesIterator = serverDataSources.iterator();
                
                // if (!serverDataSources.isEmpty()) {  // may not be needed if resolve data sources code executed previously has taken care of this use case
                
                // iterate through the app server datasources
                while (serverDataSourcesIterator.hasNext()) {
                    RequestedJdbcResource requestedJdbcResource = serverDataSourcesIterator.next();
                    
                    // if a project datasource doesn't match a server datasource then add a resource to the project
                    if (!projectDataSourceManager.matchDataSourceInfo(requestedJdbcResource, dsInfo)) {
                        missing = true;
                    }                    
                }
                
                // if the project's resources are missing (the setup folder plus resource files) then add 
                if (missing) {
                    reqResource = new RequestedJdbcResource("jdbc/" + // NOI18N
                            dsInfo.getName(),
                            dsInfo.getDriverClassName(), dsInfo.getUrl(), null, dsInfo.getUsername(),
                            dsInfo.getPassword(), null);
                    dataSourceService.updateProjectDataSource(project, reqResource);
                }
                
                missing = false;
            }
        
        
        // need to repeat the above for hardcoded datasources            
        // Update project's resources if server resources not found, based on a project's hardcoded data sources
            for (int i=0; i<hardCodedDataSources.length; i++) {
                dsInfo = DataSourceInfoManager.getInstance().getDataSourceInfoByName(hardCodedDataSources[i].substring(hardCodedDataSources[i].indexOf("jdbc")+"jdbc/".length()));
                
               // see if the server has the project's data sources'
                ProjectDataSourceManager projectDataSourceManager = new ProjectDataSourceManager(project);
                
                // retrieve a list of server data sources
                Set<RequestedJdbcResource> serverDataSources = dataSourceService.getServerDataSources(project);
                Iterator<RequestedJdbcResource> serverDataSourcesIterator = serverDataSources.iterator();
                
                // if (!serverDataSources.isEmpty()) {  // may not be needed if resolve data sources code executed previously has taken care of this use case
                
                // iterate through the app server datasources
                while (serverDataSourcesIterator.hasNext()) {
                    RequestedJdbcResource requestedJdbcResource = serverDataSourcesIterator.next();
                    
                    // if a project datasource doesn't match a server datasource then add a resource to the project
                    if (!projectDataSourceManager.matchDataSourceInfo(requestedJdbcResource, dsInfo)) {
                        missing = true;
                    }                    
                }
                
                // if the project's resources are missing (the setup folder plus resource files) then add 
                if (missing) {
                    reqResource = new RequestedJdbcResource("jdbc/" + // NOI18N
                            dsInfo.getName(),
                            dsInfo.getDriverClassName(), dsInfo.getUrl(), null, dsInfo.getUsername(),
                            dsInfo.getPassword(), null);
                    dataSourceService.updateProjectDataSource(project, reqResource);
                }
                
                missing = false;
            } // end, iterate through the data sources
        } // end, make sure project doesn't have any dynamic data sources
        
    } 
 
}
