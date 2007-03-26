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
import org.netbeans.modules.visualweb.dataconnectivity.explorer.RowSetBeanCreateInfoSet;

import org.netbeans.modules.visualweb.dataconnectivity.ui.JdbcDriverConfigUtil;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DisplayItem;
import com.sun.rave.designtime.Result;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.logging.ErrorManager;
import javax.naming.NamingException;
//import javax.xml.transform.Result;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.modules.db.api.explorer.DatabaseMetaDataTransfer;
import org.netbeans.modules.visualweb.dataconnectivity.sql.DesignTimeDataSource;
import org.netbeans.modules.visualweb.dataconnectivity.sql.DesignTimeDataSourceHelper;
//import org.openide.ErrorManager;

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
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
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
            
            String databaseProductName = null;
            try {
                databaseProductName = dbConnection.getJDBCConnection().getMetaData().getDatabaseProductName();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
            
            String dsName = dbConnection.getSchema() + "_" + databaseProductName;
            if (dbConnection.getSchema() == "")
                dsName = dbConnection.getUser()  + dsName;
            
            String driverClassName = dbConnection.getDriverClass();
            String url = dbConnection.getDatabaseURL();
            String validationQuery = null;
            String username = dbConnection.getUser();
            String password = dbConnection.getPassword();
            
            DataSourceInfo dataSourceInfo = new DataSourceInfo(dsName, driverClassName, url, validationQuery, username, password);
                                    
            // Logic to reuse the datasource exist in the project. No necessary to create new data source
            ProjectDataSourceManager projectDataSourceManager = new ProjectDataSourceManager(designBean);            
            
            // If Datasource exists in the project use its name
            try {
                DesignTimeDataSourceHelper dsHelper = new DesignTimeDataSourceHelper();
                DesignTimeDataSource dtDs = dsHelper.getDataSource(dataSourceInfo.getName());
            } catch (NamingException ne) {
                // XXX Swallow exception for now - clean up later
            }
            
            // Add the data sources to the project
            projectDataSourceManager.addDataSource(dataSourceInfo);            
            try {
                DesignTimeDataSourceHelper dsHelper = new DesignTimeDataSourceHelper();
                dsHelper.addDataSource( dsName, driverClassName, url, null,  username, password);
            } catch (NamingException ne) {
                ne.printStackTrace();
            }
            
            
            // no need to do this once the switch is made to use NetBeans connections
            addJdbcDriver(jdbcDriver);
                        
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
//                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, urie);
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
