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
 * DesignTimeDatasourceManager.java
 *
 * Created on June 2, 2006, 9:39 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.visualweb.dataconnectivity.model;

import org.netbeans.modules.visualweb.api.designerapi.DesignTimeTransferDataCreator;
import org.netbeans.modules.visualweb.api.j2ee.common.RequestedJdbcResource;
import org.netbeans.modules.visualweb.dataconnectivity.explorer.RowSetBeanCreateInfoSet;

import org.netbeans.modules.visualweb.dataconnectivity.ui.JdbcDriverConfigUtil;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DisplayItem;
import com.sun.rave.designtime.Result;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.modules.db.api.explorer.DatabaseMetaDataTransfer;
import org.openide.ErrorManager;

/**
 * Manages the Design Time Data sources transferables
 * @author Winston   Prakash
 */
public class DatasourceTransferManager implements DesignTimeTransferDataCreator{

    protected static String dataProviderClassName = "com.sun.data.provider.impl.CachedRowSetDataProvider";
    public static String rowSetClassName = "com.sun.sql.rowset.CachedRowSetXImpl";

    public DisplayItem getDisplayItem(Transferable transferable) {
        Object transferData = null;
        try {
            DataFlavor supportedFlavor = DatabaseMetaDataTransfer.TABLE_FLAVOR;
            if (transferable.isDataFlavorSupported(supportedFlavor)){
                transferData = transferable.getTransferData(supportedFlavor);
                if(transferData != null){
                    if(transferData.getClass().isAssignableFrom(DatabaseMetaDataTransfer.Table.class)){
                        DatabaseMetaDataTransfer.Table tableInfo = (DatabaseMetaDataTransfer.Table) transferData;
                        String schemaName = tableInfo.getDatabaseConnection().getSchema();
                        String tableName =
                            ((schemaName == null) || (schemaName.equals(""))) ?
                            tableInfo.getTableName() :
                            schemaName + "." + tableInfo.getTableName();
                        // String tableName = tableInfo.getTableName();
                        DatabaseConnection dbConnection = (DatabaseConnection)tableInfo.getDatabaseConnection();
                        JDBCDriver jdbcDriver = (JDBCDriver) tableInfo.getJDBCDriver();

                        // Create the Bean Create Infoset and return
                        return new DatasourceBeanCreateInfoSet(dbConnection, jdbcDriver, tableName);
                    }
                }
            }
        }catch (Exception exc) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
        }
        return null;
    }

    private static final class DatasourceBeanCreateInfoSet extends RowSetBeanCreateInfoSet{
        DatabaseConnection dbConnection = null;
        JDBCDriver jdbcDriver = null;

        public DatasourceBeanCreateInfoSet(DatabaseConnection connection,  JDBCDriver driver, String tableName ) {
            super(tableName);
            dbConnection = connection;
            jdbcDriver = driver;
        }

        public Result beansCreatedSetup(DesignBean[] designBeans) {
            DesignBean designBean = designBeans[0];

            // Get the Database Connection information and add it to design time
            // Naming Context
            // Make sure duplicate datasources are not added
            String dsName = "dataSource";
            
            // I can not use the name returned by NB DatabaseConnection
            // It has some wierd format and exception is thrown from CachedRowset
            // Ex. the name look like jdbc:derby://localhost:1527/sample [travel on TRAVEL]
            //String dsName = dbConnection.getName();
            
            String driverClassName = dbConnection.getDriverClass();
            String url = dbConnection.getDatabaseURL();
            String validationQuery = null;
            String username = dbConnection.getUser();
            String password = dbConnection.getPassword();
            
            DataSourceInfo dataSourceInfo = new DataSourceInfo(dsName, driverClassName, url, validationQuery, username, password);
            /* XXX
            DataSourceInfo dsInfo = DataSourceInfoManager.getInstance().findDataSourceInfo(dataSourceInfo);
            if(dsInfo != null){
                dataSourceInfo = dsInfo;
            }else{
                DataSourceInfoManager.getInstance().addDataSourceInfo(dataSourceInfo);
                addJdbcDriver(jdbcDriver);
            }
            */
            
            // Logic to reuse the datasource exist in the project. No necessary to create new data source
            // First find if the datasource exists in the project
            ProjectDataSourceManager projectDataSourceManager = new ProjectDataSourceManager(designBean);
            Set<RequestedJdbcResource> projectDataResources = projectDataSourceManager.
                    findRequestedJdbcResources(dataSourceInfo);
            Set<DataSourceInfo> dsInfos = DataSourceInfoManager.getInstance().findDataSourceInfo(dataSourceInfo);
                                // OK, we don't have the datasource. Add it
            if ((dsInfos == null) || (dsInfos.size() == 0)) {
                boolean found = false;
                
                                // If Datasource exists in the project use its name
                if (projectDataResources != null && projectDataResources.size() > 0) {
                    Iterator<RequestedJdbcResource> it = projectDataResources.iterator();
                    
                    while (it.hasNext() && !found) {
                        RequestedJdbcResource r = it.next();
                        String projectDsInfoName = r.getResourceName().replaceFirst("jdbc/", ""); // NOI18N
                                // Make sure it doesn't exist in the DataSourceInfoManager
                        String lastUniqueName = DataSourceInfoManager.getInstance().
                                getUniqueDataSourceName(projectDsInfoName);
                        dataSourceInfo.setName(lastUniqueName);
                        
                        if (lastUniqueName.equals(projectDsInfoName)) {
                            found = true;
                        }
                    }
                }
                    
                if (!found) {
                   RequestedJdbcResource r = projectDataSourceManager.
                            getDataSourceWithName(dataSourceInfo.getName());
                        
                    if (r != null && !projectDataSourceManager.matchDataSourceInfo(r, dataSourceInfo)) {
                                // Too bad we have a different datasource in the DataSourceInfoManager
                                // with same name as project datasource
                        dataSourceInfo.setName(getUniqueName(dataSourceInfo.getName()));
                    }
                        
                    if (!projectDataSourceManager.addDataSource(dataSourceInfo)) {
                        return Result.FAILURE;
                    }
                }
                
                DataSourceInfoManager.getInstance().addDataSourceInfo(dataSourceInfo);
                addJdbcDriver(jdbcDriver);
            } else {
                if (projectDataResources != null && projectDataResources.size() > 0) {
                    Iterator<RequestedJdbcResource> it = projectDataResources.iterator();
                    boolean found = false;
                    String lastProjectDsInfoName = null;
                    
                    while (it.hasNext() && !found) {
                        RequestedJdbcResource r = it.next();
                        lastProjectDsInfoName = r.getResourceName().replaceFirst("jdbc/",""); // NOI18N
                        
                        Iterator<DataSourceInfo> it1 = dsInfos.iterator();
                        
                        while (it1.hasNext() && !found) {
                            DataSourceInfo dsInfo = it1.next();
                            if (dsInfo.getName().equals(lastProjectDsInfoName)) {
                                found = true;
                                dataSourceInfo = dsInfo;
                            }
                        }
                    }
                    
                    if (!found) {
                                // OK we have a problem, the JNDI name doesn't match, so create
                                // another datasource with that of project datasource in the
                                // Datasource manager
                        String uniqueName = DataSourceInfoManager.getInstance().
                                getUniqueDataSourceName(lastProjectDsInfoName);
                        
                        dataSourceInfo.setName(uniqueName);
                        
                        if (!uniqueName.equals(lastProjectDsInfoName)) {
                            RequestedJdbcResource r = projectDataSourceManager.
                                    getDataSourceWithName(uniqueName);
                        
                            if (r != null && !projectDataSourceManager.matchDataSourceInfo(r, dataSourceInfo)) {
                                dataSourceInfo.setName(getUniqueName(dataSourceInfo.getName()));
                            }

                            if (!projectDataSourceManager.addDataSource(dataSourceInfo)) {
                                return Result.FAILURE;
                            }
                        }

                        DataSourceInfoManager.getInstance().addDataSourceInfo(dataSourceInfo);
                    }
                } else {
                    dataSourceInfo = dsInfos.iterator().next();
                    
                    boolean needAdd = false;
                    
                    if (projectDataSourceManager.getDataSourceWithName(dataSourceInfo.getName()) != null) {
                        dataSourceInfo.setName(getUniqueName(dataSourceInfo.getName()));                       
                        needAdd = true;
                    }

                    if (!projectDataSourceManager.addDataSource(dataSourceInfo)) {
                        return Result.FAILURE;
                    }
                    
                    if (needAdd) {
                        DataSourceInfoManager.getInstance().addDataSourceInfo(dataSourceInfo);
                    }
                }
            }
            
            setDataSourceInfo(dataSourceInfo);
            return super.beansCreatedSetup(designBeans);
        }
        
       /* Get the JDBCDriver driver info and add the driver jar to Design time driver list
        * This is needed for design time datasource connection to work
        * TODO make sure duplicate drivers are not added
        * Possibly check against the driver jar names and its size
        */   
        private void addJdbcDriver(JDBCDriver jdbcDriver){
            // Get the driver jar urls copy them and then add the jar names
            URL[] driverJarUrls = jdbcDriver.getURLs();
            for(int i=0; i< driverJarUrls.length; i++){
                JdbcDriverConfigUtil driverUtil = new JdbcDriverConfigUtil();
                
                try {
                    String urlPath = driverJarUrls[i].toURI().getPath();
                    if (urlPath != null)  // test if urlPath is undefined
                        driverUtil.copyJarNoConfirm(urlPath);
                } catch (URISyntaxException urie) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, urie);
                }
            }
        }

        private String getUniqueName(String name){
           if(name.indexOf('_') != -1){
              String prefix = name.substring(0, name.indexOf('_'));
              return prefix + "_" + System.currentTimeMillis(); // NOI18N
           } else{
              return name + "_" + System.currentTimeMillis(); // NOI18N
           }
        } 
    }
}
