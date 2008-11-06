/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
import org.netbeans.modules.visualweb.dataconnectivity.DataconnectivitySettings;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DisplayItem;
import com.sun.rave.designtime.Result;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseMetaDataTransfer;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.project.Project;
import org.netbeans.modules.visualweb.dataconnectivity.datasource.CurrentProject;
import org.netbeans.modules.visualweb.dataconnectivity.datasource.DataSourceResolver;
import org.netbeans.modules.visualweb.dataconnectivity.ui.DataSourceNamePanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;

/**
 * Manages the Design Time Data sources transferables
 * @author Winston Prakash, John Baker
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.visualweb.api.designerapi.DesignTimeTransferDataCreator.class)
public class DatasourceTransferManager implements DesignTimeTransferDataCreator{
    
    protected static String dataProviderClassName = "com.sun.data.provider.impl.CachedRowSetDataProvider";
    public static String rowSetClassName = "com.sun.sql.rowset.CachedRowSetXImpl";
    private static final Logger LOGGER = 
            Logger.getLogger(DatasourceTransferManager.class.getName());
    
    private static final String UNDERSCORE = "_";  // NOI18N
    private static final String SLASH = "/";  // NOI18N
    
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
                        Connection conn = dbConnection.getJDBCConnection();
                        DatabaseMetaData metaData = (conn == null) ? null : conn.getMetaData();
                        String tableName =
                                ((schemaName == null) || (schemaName.equals(""))) ?
                                 tableInfo.getTableName() :
                                 schemaName + "." + tableInfo.getTableName();
                        JDBCDriver jdbcDriver = tableInfo.getJDBCDriver();
                        
                        // Create the Bean Create Infoset and return
                        return new DatasourceBeanCreateInfoSet(dbConnection, jdbcDriver, tableName, metaData);
                    }
                }
            } else if (transferable.isDataFlavorSupported(viewFlavor)){
                transferData = transferable.getTransferData(viewFlavor);
                if(transferData != null){
                    if(transferData.getClass().isAssignableFrom(DatabaseMetaDataTransfer.View.class)){
                        DatabaseMetaDataTransfer.View viewInfo = (DatabaseMetaDataTransfer.View) transferData;
                        DatabaseConnection dbConnection = viewInfo.getDatabaseConnection();
                        String schemaName = dbConnection.getSchema();
                        Connection conn = dbConnection.getJDBCConnection();
                        DatabaseMetaData metaData = (conn == null) ? null : conn.getMetaData();
                        String viewName =
                                ((schemaName == null) || (schemaName.equals(""))) ?
                                  viewInfo.getViewName() :
                                  schemaName + "." + viewInfo.getViewName();
                        JDBCDriver jdbcDriver = viewInfo.getJDBCDriver();
                        
                        // Create the Bean Create Infoset and return
                        return new DatasourceBeanCreateInfoSet(dbConnection, jdbcDriver, viewName, metaData);
                    }
                }
            }
        } catch (Exception exc) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
        }
        return null;
    }
    
    private static final class DatasourceBeanCreateInfoSet extends RowSetBeanCreateInfoSet {
        
        DatabaseConnection dbConnection = null;
        JDBCDriver jdbcDriver = null;
        
        public DatasourceBeanCreateInfoSet(DatabaseConnection connection,  JDBCDriver driver, String tableName, DatabaseMetaData metaData ) {
            super(tableName, metaData);
            dbConnection = connection;
            jdbcDriver = driver;
        }
        @Override
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
            
            if (schema.equals("")) { // NOI18N
                if (databaseProductName.equals("MySQL") && url.lastIndexOf("?") == -1) // NOI18N
                    dsName = url.substring(url.lastIndexOf(SLASH) + 1, url.length()) + UNDERSCORE + databaseProductName; 
                else if (databaseProductName.contains("Firebird")) { // NOI18N
                    dsName = getTableName() + UNDERSCORE + "Firebird"; // NOI18N
                }
                else if (databaseProductName.contains(SLASH)) { 
                    int slashLoc = databaseProductName.indexOf(SLASH); 
                    String prefix = databaseProductName.substring(0, slashLoc);
                    String suffix = databaseProductName.substring(slashLoc+1);                   
                    dsName = prefix + UNDERSCORE + suffix;
                } else if (databaseProductName.equals("MySQL")) { // NOI18N
                    dsName = url.substring(url.lastIndexOf(SLASH) + 1, url.lastIndexOf("?")) + UNDERSCORE + databaseProductName; // NOI18N
                } else
                    dsName = getTableName() + UNDERSCORE + databaseProductName; 
            } else if (databaseProductName.contains(SLASH)) { 
                int slashLoc = databaseProductName.indexOf(SLASH); 
                String prefix = databaseProductName.substring(0, slashLoc);
                String suffix = databaseProductName.substring(slashLoc+1);
                dsName = prefix + UNDERSCORE + suffix; 
            } else {
                dsName = dbConnection.getSchema() + UNDERSCORE + databaseProductName;
            }
            
            // Check if target server supports data source creation.  If not then cancel drop
            Project currentProj = CurrentProject.getInstance().getCurrentProject(designBeans);
            if (!DataSourceResolver.getInstance().isDatasourceCreationSupported(currentProj)) {
                cancel = true;
                DataSourceResolver.getInstance().postUnsupportedDataSourceCreationDialog();
            }

            // Disallow drop of Oracle table when version of driver is less than 10.2
            if ("Oracle".equals(databaseProductName)) { // NOI18N
                try {
                    if ((dbConnection.getJDBCConnection().getMetaData().getDriverMajorVersion() < 10) && (dbConnection.getJDBCConnection().getMetaData().getDriverMinorVersion() < 2)) {
                        cancel = true;
                        LOGGER.log(Level.WARNING, "Oracle driver version 10 or later supported.  Drop cancelled."); // NOI18N
                        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(DatasourceBeanCreateInfoSet.class, "ORACLE_DRIVER_WARNING")); // NOI18N
                    }
                } catch (SQLException ex) {
                    Exceptions.printStackTrace(ex);
                } 
            }
            
            // ensure data source name is unique
            if (!DataSourceResolver.getInstance().isDataSourceUnique(currentProj, dsName, url)) {
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
