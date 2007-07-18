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
import org.netbeans.modules.visualweb.dataconnectivity.model.ProjectDataSourceManager;
import org.netbeans.modules.visualweb.dataconnectivity.project.datasource.ProjectDataSourceTracker;
import org.netbeans.modules.visualweb.project.jsf.services.DesignTimeDataSourceService;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.project.Project;
import org.netbeans.modules.visualweb.dataconnectivity.model.ProjectDataSourceManager;
import org.openide.ErrorManager;
import org.openide.util.Lookup;

/**
 *
 * @author John Baker
 */
public class DataSourceResolver implements DataSourceInfoListener {
    private static final int TRUE = 1;
    private static final int FALSE = 0;
    private static final int RESET = -1;
    private static DataSourceResolver dataSourceResolver;
    private String dataSourceInfo = null;

    /** Creates a new instance of DataSourceResolver */
    private DataSourceResolver() {
    }

    public static DataSourceResolver getInstance() {
        if (dataSourceResolver == null){
            dataSourceResolver = new DataSourceResolver();
        }
        return dataSourceResolver;
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
    
    // if a project data source does not have a corresponding connetion then return true
    public boolean isDataSourceMissing(Project project, String prjDsName) {
        DesignTimeDataSourceService dataSourceService = Lookup.getDefault().lookup(DesignTimeDataSourceService.class);
        Set <RequestedJdbcResource> problemDatasources =  dataSourceService.getBrokenDatasources(project);

        boolean missing = false;
        Iterator it = problemDatasources.iterator();
        while (it.hasNext()) {          
            RequestedJdbcResource reqRes = (RequestedJdbcResource)it.next();
            if (("jdbc/" + prjDsName).equals(reqRes.getJndiName())) {
                missing = true;
                break; // data source match made, stop checking
            }
        }        
        
        return missing;
    }
    
    
    
    
    
    // Find a matching driver registered with the IDE
    public JDBCDriver findMatchingDriver(String driverClass) {
      int i = 0;
        JDBCDriver[] newDrivers;
        newDrivers = JDBCDriverManager.getDefault().getDrivers();
        
        for (i = 0; i <newDrivers.length; i++) {
            if (newDrivers[i].getClassName().equals(driverClass)) {
                return newDrivers[i];
            }
        }
        
        return null;
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
    
   
    
 
}
