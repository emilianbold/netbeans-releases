/*
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). You may not use this
 * file except in compliance with the License.  You can obtain a copy of the
 *  License at http://www.netbeans.org/cddl.html
 *
 * When distributing Covered Code, include this CDDL Header Notice in each
 * file and include the License. If applicable, add the following below the
 * CDDL Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved
 *
 */
package org.netbeans.modules.edm.editor.utils;

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import javax.swing.table.DefaultTableModel;

import org.netbeans.modules.edm.model.MashupCollaborationModel;
import org.netbeans.modules.edm.model.DBConnectionDefinition;
import org.netbeans.modules.edm.model.DBMetaDataFactory;
import org.netbeans.modules.edm.editor.utils.DBExplorerUtil;
import org.netbeans.modules.edm.model.SQLConstants;
import org.netbeans.modules.edm.model.SQLDBModel;
import org.netbeans.modules.edm.model.SQLDefinition;
import org.netbeans.modules.edm.model.SQLModelObjectFactory;
import org.netbeans.modules.edm.model.SourceTable;
import org.netbeans.modules.edm.model.impl.SourceTableImpl;
import org.netbeans.modules.edm.editor.ui.view.MashupDataObjectProvider;
import org.netbeans.modules.edm.model.SQLDBTable;

/**
 *
 * @author karthikeyan s
 */
public class MashupModelHelper {

    /** Creates a new instance of MashupModelHelper */
    public MashupModelHelper() {
    }

    private static String generateDBModelName(SQLDefinition sqldef) {
        int cnt = 1;
        String connNamePrefix = "SourceConnection";
        String aName = connNamePrefix + cnt;
        while (isDBModelNameExist(aName, sqldef)) {
            cnt++;
            aName = connNamePrefix + cnt;
        }

        return aName;
    }

    private static boolean isDBModelNameExist(String aName, SQLDefinition sqldef) {
        
        Iterator<SQLDBModel> it1 = sqldef.getAllDatabases().iterator();
        while (it1.hasNext()) {
            SQLDBModel dbModel = it1.next();
            String dbName = dbModel.getModelName();
            if (dbName != null && dbName.equals(aName)) {
                return true;
            }
        }

        return false;
    }

    private static void setAliasForModelTables(List sqldefTables) {
        Iterator it = sqldefTables.iterator();
        while (it.hasNext()) {
            SQLDBTable tTable = (SQLDBTable) it.next();
            String tAlias = generateTableAliasName(sqldefTables);
            tTable.setAliasUsed(true);
            tTable.setAliasName(tAlias);
        }
    }
    private static String generateTableAliasName(List sqldefTables) {
        int cnt = 1;
        String aliasPrefix = "S";
        String aName = aliasPrefix + cnt;
        while (isTableAliasNameExist(aName, sqldefTables)) {
            cnt++;
            aName = aliasPrefix + cnt;
        }

        return aName;
    }

    private static boolean isTableAliasNameExist(String aName, List sTables) {

        Iterator it = sTables.iterator();
        while (it.hasNext()) {
            SQLDBTable tTable = (SQLDBTable) it.next();
            String tAlias = tTable.getAliasName();
            if (tAlias != null && tAlias.equals(aName)) {
                return true;
            }
        }

        return false;
    }
    
    public static MashupCollaborationModel getModel(MashupCollaborationModel model, DefaultTableModel tblModel) {
        SQLDefinition sqlDefn = model.getSQLDefinition();
        DBMetaDataFactory meta = new DBMetaDataFactory();
        try {
            for (int i = 0; i < tblModel.getRowCount(); i++) {
                String table = (String) tblModel.getValueAt(i, 0);
                String schema = (String) tblModel.getValueAt(i, 1);
                String connectionUrl = (String) tblModel.getValueAt(i, 2);
                String user = (String) tblModel.getValueAt(i, 3);
                String pass = (String) tblModel.getValueAt(i, 4);
                String driver = (String) tblModel.getValueAt(i, 5);
                Connection conn = DBExplorerUtil.createConnection(driver, connectionUrl, user, pass);
                meta.connectDB(conn);
                //Create the Table
                SourceTable srcTable = new SourceTableImpl(table, schema, "");
                srcTable.setAliasName(generateTableAliasName(sqlDefn.getSourceTables()));
                meta.populateColumns(srcTable);
                
                // We should check if there exists a DBModel for this URL (connection from where
                // the table is selected and dropped on to collaboration editor.
                List<SQLDBModel> sqlDefDBModels = sqlDefn.getAllDatabases();
                Iterator modelIt = sqlDefDBModels.iterator();
                boolean isNewModelRequired = true;
                while (modelIt.hasNext()) {
                    SQLDBModel aModel = (SQLDBModel) modelIt.next();
                    if (connectionUrl.equals(aModel.getConnectionDefinition().getConnectionURL()) &&
                           user.equals(aModel.getConnectionDefinition().getUserName()) ) {
                        //Add the table to this SQLDBModel, instead of creatign a new SQLDBModel.
                        aModel.addTable((SQLDBTable)srcTable);              
                        sqlDefn.addObject(aModel);
                        //SQLObjectUtil.setOrgProperties((SQLDBTable)srcTable);
                        isNewModelRequired = false;
                        break;
                    }
                } //end while
                
                if (isNewModelRequired) {
                    SQLDBModel dbModel = SQLModelObjectFactory.getInstance().createDBModel(
                            SQLConstants.SOURCE_DBMODEL);
                    String modelName = generateDBModelName(sqlDefn);
                    populateModel(dbModel, driver, user, pass, connectionUrl, meta, modelName);
                    dbModel.addTable(srcTable);
                    sqlDefn.addObject(dbModel);
                    try {
                        meta.disconnectDB();
                    } catch (Exception ex) {
                        //ignore
                    }
                }
            } //end for

            //setAliasForModelTables(sqlDefn.getSourceTables());
            model.setDirty(true);
            MashupDataObjectProvider.getProvider().getActiveDataObject().getMashupDataEditorSupport().synchDocument();            
            model.setSQLDefinition(sqlDefn);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return model;
    }

    private static SQLDBModel populateModel(SQLDBModel model, String driver,
            String user, String pass, String url, DBMetaDataFactory meta, String modelName) {
        DBConnectionDefinition def = null;
        try {
            def = SQLModelObjectFactory.getInstance().createDBConnectionDefinition(modelName,
                    meta.getDBType(), driver, url, user, pass, "Descriptive info here");
        } catch (Exception ex) {
            // ignore
        }
        model.setModelName(modelName);
        model.setConnectionDefinition(def);
        return model;
    }
}