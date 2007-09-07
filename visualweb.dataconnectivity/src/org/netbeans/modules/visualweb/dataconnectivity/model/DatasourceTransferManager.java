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

import org.netbeans.modules.visualweb.dataconnectivity.DataconnectivitySettings;

import org.netbeans.modules.visualweb.api.designerapi.DesignTimeTransferDataCreator;
import org.netbeans.modules.visualweb.dataconnectivity.explorer.RowSetBeanCreateInfoSet;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DisplayItem;
import com.sun.rave.designtime.Result;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.sql.SQLException;

import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseMetaDataTransfer;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.modules.visualweb.dataconnectivity.datasource.CurrentProject;
import org.netbeans.modules.visualweb.dataconnectivity.datasource.DataSourceResolver;
import org.netbeans.modules.visualweb.dataconnectivity.ui.DataSourceNamePanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;

/**
 * Manages the Design Time Data sources transferables
 * @author Winston Prakash, John Baker
 */
public class DatasourceTransferManager implements DesignTimeTransferDataCreator{
    
    protected static String dataProviderClassName = "com.sun.data.provider.impl.CachedRowSetDataProvider";
    public static String rowSetClassName = "com.sun.sql.rowset.CachedRowSetXImpl";
    
    public DisplayItem getDisplayItem(Transferable transferable) {
        Object transferData = null;
        try {
            DataFlavor tableFlavor = DatabaseMetaDataTransfer.TABLE_FLAVOR;
            DataFlavor viewFlavor = DatabaseMetaDataTransfer.VIEW_FLAVOR;
            
            if (transferable.isDataFlavorSupported(tableFlavor)){
                transferData = transferable.getTransferData(tableFlavor);
                if(transferData != null){
                    if(transferData.getClass().isAssignableFrom(DatabaseMetaDataTransfer.Table.class)){
                        DatabaseMetaDataTransfer.Table tableInfo = (DatabaseMetaDataTransfer.Table) transferData;
                        DatabaseConnection dbConnection = tableInfo.getDatabaseConnection();
                        String schemaName = dbConnection.getSchema();
                        String tableName =
                                ((schemaName == null) || (schemaName.equals(""))) ?
                                    tableInfo.getTableName() :
                                    schemaName + "." + tableInfo.getTableName();
                        // String tableName = tableInfo.getTableName();
                        JDBCDriver jdbcDriver = tableInfo.getJDBCDriver();     
                        // Create the Bean Create Infoset and return
                        return new DatasourceBeanCreateInfoSet(dbConnection, jdbcDriver, tableName);
                    }
                }
            } else if (transferable.isDataFlavorSupported(viewFlavor)){
                transferData = transferable.getTransferData(viewFlavor);
                if(transferData != null){
                    if(transferData.getClass().isAssignableFrom(DatabaseMetaDataTransfer.View.class)){
                        DatabaseMetaDataTransfer.View viewInfo = (DatabaseMetaDataTransfer.View) transferData;
                        DatabaseConnection dbConnection = viewInfo.getDatabaseConnection();
                        String schemaName = dbConnection.getSchema();
                        String viewName =
                                ((schemaName == null) || (schemaName.equals(""))) ?
                                    viewInfo.getViewName() :
                                    schemaName + "." + viewInfo.getViewName();
                        JDBCDriver jdbcDriver = viewInfo.getJDBCDriver();
                        
                        // Create the Bean Create Infoset and return
                        return new DatasourceBeanCreateInfoSet(dbConnection, jdbcDriver, viewName);
                    }
                }
            }
        } catch (Exception exc) {
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
            boolean cancel = false;
            String databaseProductName = null;
            try {
                databaseProductName = dbConnection.getJDBCConnection().getMetaData().getDatabaseProductName().replaceAll("\\s{1}", "");
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
            
            String dsName = "dataSource";
            String driverClassName = dbConnection.getDriverClass();
            String url = dbConnection.getDatabaseURL();
            String validationQuery = null;
            String username = dbConnection.getUser();
            String password = dbConnection.getPassword();
            String schema = dbConnection.getSchema();
            
            if (schema.equals("")) {
                if (databaseProductName.equals("MySQL") && url.lastIndexOf("?") == -1)
                    dsName = url.substring(url.lastIndexOf("/") + 1, url.length()) + "_" + databaseProductName;
                else if (databaseProductName.equals("MySQL")) {
                    dsName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("?")) + "_" + databaseProductName;;
                } else
                    dsName = getTableName() + "_" + databaseProductName;
            } else
                dsName = dbConnection.getSchema() + "_" + databaseProductName;
            
            // Check if target server supports data source creation.  If not then cancel drop
            if (!DataSourceResolver.getInstance().isDatasourceCreationSupported(CurrentProject.getInstance().getProject())) {
                cancel = true;
            }
            
            // ensure data source name is unique
            if (!DataSourceResolver.getInstance().isDataSourceUnique(CurrentProject.getInstance().getProject(), dsName, url)) {
                dsName = getUniqueName(dsName);
            }
            
            if (DataconnectivitySettings.promptForName()) {
                DataSourceNamePanel dataSourceNamePanel = new DataSourceNamePanel(dsName);
                final DialogDescriptor dialogDescriptor = new DialogDescriptor(
                        dataSourceNamePanel,
                        NbBundle.getMessage(DataSourceNamePanel.class, "LBL_SpecifyDataSourceName"), //NOI18N
                        true,
                        DialogDescriptor.DEFAULT_OPTION,
                        DialogDescriptor.OK_OPTION,
                        DialogDescriptor.DEFAULT_ALIGN,
                        HelpCtx.DEFAULT_HELP,
                        null);
                
                // initial invalidation
                dialogDescriptor.setValid(true);
                // show and eventually save
                Object option = DialogDisplayer.getDefault().notify(dialogDescriptor);
                if (option == NotifyDescriptor.OK_OPTION) {
                    dsName = dataSourceNamePanel.getDataSourceName();
                } else if (option == NotifyDescriptor.CANCEL_OPTION) {
                    cancel = true;
                }
            }
            
            // if user has enabled Prompt for Data Source name and clicks cancel then don't create and bind the data source
            if (!cancel) {
                DataSourceInfo dataSourceInfo = new DataSourceInfo(dsName, driverClassName, url, validationQuery, username, password);

                // Logic to reuse the datasource exist in the project. No necessary to create new data source
                ProjectDataSourceManager projectDataSourceManager = new ProjectDataSourceManager(designBean);

                // Add the data sources to the project
                projectDataSourceManager.addDataSource(dataSourceInfo);

                // create the rowset
                setDataSourceInfo(dataSourceInfo);
                return super.beansCreatedSetup(designBeans);
            }
            
             // remove unused data provider that was created
            final DesignBean deleteMeBean = designBeans[0];
            DesignContext dc = deleteMeBean.getDesignContext();
            dc.deleteBean(deleteMeBean);
            return null;
        }
        
         private int getIndex(String name) {
            int index = -1;

            if (name.indexOf('_') != -1) {
                try {
                    index = Integer.parseInt(name.substring(name.lastIndexOf('_') + 1, name.length() - 1));
                } catch (NumberFormatException nfe) {
                    // do nothing
                }
            }

            return index;
        }
         
        private String getUniqueName(String name){
            int index = getIndex(name);
            if (index != -1) {
                index++;
                String prefix = name.substring(0, name.indexOf('_'));
                return prefix  + "_" + Integer.toString(index); // NOI18N
            } else{
                return name + "_" + 1; // NOI18N
            }
        }
    }
}
