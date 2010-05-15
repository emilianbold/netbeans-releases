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
package org.netbeans.modules.edm.editor.graph.actions;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseMetaDataTransfer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.netbeans.api.visual.action.AcceptProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Widget;

import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;
import org.netbeans.modules.edm.editor.graph.MashupGraphManager;
import org.netbeans.modules.edm.editor.palette.Operator;
import org.netbeans.modules.edm.editor.widgets.EDMGraphScene;
import org.netbeans.modules.edm.model.DBConnectionDefinition;
import org.netbeans.modules.edm.model.DBMetaDataFactory;
import org.netbeans.modules.edm.model.SQLConstants;
import org.netbeans.modules.edm.model.SQLDBModel;
import org.netbeans.modules.edm.model.SQLDBTable;
import org.netbeans.modules.edm.model.SQLJoinView;
import org.netbeans.modules.edm.model.SQLModelObjectFactory;
import org.netbeans.modules.edm.model.impl.SourceTableImpl;
import org.netbeans.modules.edm.editor.utils.SQLObjectUtil;
import org.netbeans.modules.edm.editor.ui.model.CollabSQLUIModel;
import org.netbeans.modules.edm.editor.ui.view.join.JoinMainDialog;
import org.netbeans.modules.edm.editor.ui.view.join.JoinUtility;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * This class implements the accept provider.
 * This can accept the palette items and act accordingly.
 *
 * @author karthikeyan s
 */
public class SceneAcceptProvider implements AcceptProvider {

    private MashupDataObject mObj;
    private MashupGraphManager manager;
    private static final String NETBEANS_DBTABLE_MIMETYPE = "application/x-java-netbeans-dbexplorer-table;class=org.netbeans.api.db.explorer.DatabaseMetaDataTransfer$Table";
    private transient int tableTypeSelected = SQLConstants.SOURCE_TABLE;
    protected static DataFlavor[] mDataFlavorArray = new DataFlavor[2];
    

    static {
        try {
            mDataFlavorArray[0] = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);

            mDataFlavorArray[1] = new DataFlavor(NETBEANS_DBTABLE_MIMETYPE);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public SceneAcceptProvider(MashupDataObject dObj, MashupGraphManager manager) {
        this.mObj = dObj;
        this.manager = manager;
    }

    public ConnectorState isAcceptable(Widget widget, Point point, Transferable transferable) {
        boolean accept = true;
        String type = null;
        try {
            Object node1 = transferable.getTransferData(mDataFlavorArray[1]);
            if (node1 instanceof DatabaseMetaDataTransfer.Table) {
                return ConnectorState.ACCEPT;
            }
        } catch (UnsupportedFlavorException uex) {
            Object node;
            try {
                node = transferable.getTransferData(mDataFlavorArray[0]);
                Operator op = (Operator) node;
                type = op.getName();
            } catch (UnsupportedFlavorException ex) {
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (type.equals("Join")) {
                if (mObj.getModel().getSQLDefinition().getJoinSources().size() == 0) {
                    accept = false;
                }
            }
        } catch (Exception ex) {
            accept = false;
        }

        if (!accept) {
            return ConnectorState.REJECT_AND_STOP;
        }
        return ConnectorState.ACCEPT;
    }

    public void accept(Widget widget, Point point, Transferable transferable) {
        try {
            Connection conn = null;
            Object node1 = transferable.getTransferData(new DataFlavor(NETBEANS_DBTABLE_MIMETYPE));
            if (node1 instanceof DatabaseMetaDataTransfer.Table) {
                DatabaseMetaDataTransfer.Table tbl = (DatabaseMetaDataTransfer.Table) node1;
                DatabaseConnection dbConn = tbl.getDatabaseConnection();
                conn = dbConn.getJDBCConnection();
                String tableName = tbl.getTableName();
                String schema = tbl.getDatabaseConnection().getSchema();
                String catalog = null;
                try {
                    catalog = conn.getCatalog();
                } catch (Exception ex) {
                }
                DBMetaDataFactory dbMeta = new DBMetaDataFactory();
                SQLDBTable sTable = null;
                tableTypeSelected = SQLConstants.SOURCE_TABLE;
                boolean isSource = false;
                if (SQLConstants.SOURCE_TABLE == tableTypeSelected) {
                    isSource = true;
                }

                String[][] tableList = null;
                try {
                    dbMeta.connectDB(conn);
                    schema = (schema == null) ? "" : schema;
                    catalog = (catalog == null) ? "" : catalog;
                    tableList = dbMeta.getTablesAndViews(catalog, schema, "", false);
                } catch (Exception ex) {
                }

                Object dbTable = createTable(tableList, tableName, isSource);
                List tbls = null;
                if (isSource) {
                    tbls = mObj.getModel().getSQLDefinition().getSourceTables();
                }
                ((SQLDBTable) dbTable).setAliasUsed(true);
                ((SQLDBTable) dbTable).setAliasName(generateTableAliasName(isSource, tbls));
                dbMeta.populateColumns((SQLDBTable) dbTable);
                ((SQLDBTable) dbTable).setEditable(true);
                ((SQLDBTable) dbTable).setSelected(true);
                DBConnectionDefinition def = null;

                // We should check if there exists a DBModel for this URL (connection from where
                // the table is selected and dropped on to collaboration editor.
                List<SQLDBModel> sqlDefDBModels = mObj.getModel().getSQLDefinition().getAllDatabases();
                Iterator modelIt = sqlDefDBModels.iterator();
                boolean isNewModelRequired = true;
                while (modelIt.hasNext()) {
                    SQLDBModel aModel = (SQLDBModel) modelIt.next();
                    if (dbConn.getDatabaseURL().equals(aModel.getConnectionDefinition().getConnectionURL()) &&
                            dbConn.getUser().equals(aModel.getConnectionDefinition().getUserName())) {
                        //Add the table to this SQLDBModel, instead of creatign a new SQLDBModel.
                        aModel.addTable((SQLDBTable) dbTable);
                        mObj.getModel().getSQLDefinition().addObject(aModel);
                        SQLObjectUtil.setOrgProperties((SQLDBTable) dbTable);
                        isNewModelRequired = false;
                        break;
                    }
                } //end while
                if (isNewModelRequired) {
                    String modelName = generateDBModelName();
                    try {
                        def = SQLModelObjectFactory.getInstance().createDBConnectionDefinition(modelName, dbMeta.getDBType(), dbConn.getDriverClass(), dbConn.getDatabaseURL(), dbConn.getUser(), dbConn.getPassword(), "Descriptive info here");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    SQLDBModel model = null;
                    if (isSource) {
                        model = SQLModelObjectFactory.getInstance().createDBModel(SQLConstants.SOURCE_DBMODEL);
                    }
                    model.setModelName(modelName);
                    model.setConnectionDefinition(def);
                    model.addTable((SQLDBTable) dbTable);

                    if (isSource) {
                        sTable = (SQLDBTable) mObj.getModel().addSourceTable((SQLDBTable) dbTable, point);
                    }
                    SQLObjectUtil.setOrgProperties(sTable);
                }
                mObj.getGraphManager().addTable((SQLDBTable) dbTable, point);//refreshGraph();
                mObj.getMashupDataEditorSupport().synchDocument();
                mObj.getMashupDataEditorSupport().syncModel();
                mObj.getModel().setDirty(true);
                mObj.setModified(true);
            }
        } catch (UnsupportedFlavorException ex) {
            acceptOperators(widget, point, transferable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void acceptOperators(Widget widget, Point point, Transferable transferable) {
        try {
            Object nd = transferable.getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType));
            Operator node = (Operator) nd;
            String type = node.getName();
            if (type.equals("Join")) {
                // create join widget on the canvas & create join operator and add to the model.
                if (mObj.getModel().getSQLDefinition().getJoinSources().size() != 0) {
                    SQLJoinView[] joinViews = (SQLJoinView[]) mObj.getModel().getSQLDefinition().getObjectsOfType(
                            SQLConstants.JOIN_VIEW).toArray(new SQLJoinView[0]);
                    if (joinViews == null || joinViews.length == 0) {
                        JoinMainDialog.showJoinDialog(
                                mObj.getModel().getSQLDefinition().getJoinSources(), null,
                                null, false);
                    } else {
                        JoinMainDialog.showJoinDialog(
                                mObj.getModel().getSQLDefinition().getJoinSources(), joinViews[0],
                                null);
                    }
                    if (JoinMainDialog.getClosingButtonState() == JoinMainDialog.OK_BUTTON) {
                        SQLJoinView joinView = JoinMainDialog.getSQLJoinView();
                        try {
                            if (joinView != null) {
                                JoinUtility.handleNewJoinCreation(joinView,
                                        JoinMainDialog.getTableColumnNodes(),
                                        mObj.getEditorView().getCollabSQLUIModel());
                                manager.refreshGraph();
                                manager.layoutGraph();
                                //mObj.getMashupDataEditorSupport().synchDocument();
                                //mObj.getMashupDataEditorSupport().syncModel();
                                mObj.getModel().setDirty(true);
                                mObj.setModified(true);
                            }
                        } catch (Exception exc) {
                            manager.setLog(NbBundle.getMessage(SceneAcceptProvider.class, "LOG_Error_adding_Join_view"));
                        }
                    }
                }
            } else if (type.equals("Group By")) {
                // Add group by operator.
                if (mObj.getGraphManager().addGroupby(((EDMGraphScene) widget).convertLocalToScene(point))) {
                    manager.refreshGraph();
                    manager.layoutGraph();
                    mObj.getModel().setDirty(true);
                    mObj.setModified(true);
                }
            } else if (type.equals("Materialized View") ||
                    type.equals("Union") || type.equals("Intersect")) {
                // create view widget on the canvas.
                NotifyDescriptor d =
                        new NotifyDescriptor.Message(NbBundle.getMessage(SceneAcceptProvider.class, "MSG_Operator_not_supported"),
                        NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        } catch (Exception e) {
        }
    }


    private Object createTable(String[][] tableList, String tableName, boolean isSource) {
        String[] currTable = null;
        Object dbTable = null;
        if (tableList != null) {
            for (int i = 0; i < tableList.length; i++) {
                currTable = tableList[i];
                if (currTable[DBMetaDataFactory.NAME].equals(tableName)) {
                    if (isSource) {
                        dbTable = new SourceTableImpl(currTable[DBMetaDataFactory.NAME], currTable[DBMetaDataFactory.SCHEMA], currTable[DBMetaDataFactory.CATALOG]);
                    }
                    break;
                }
            }
        }
        return dbTable;
    }

    private String generateTableAliasName(boolean isSource, List sTables) {
        int cnt = 1;
        String aliasPrefix = isSource ? "S" : "T";
        String aName = aliasPrefix + cnt;
        while (isTableAliasNameExist(aName, sTables)) {
            cnt++;
            aName = aliasPrefix + cnt;
        }

        return aName;
    }

    private boolean isTableAliasNameExist(String aName, List sTables) {

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

    private boolean isDBModelNameExist(String aName) {
        CollabSQLUIModel sqlModel = (CollabSQLUIModel) mObj.getModel();
        Iterator<SQLDBModel> it = sqlModel.getSQLDefinition().getAllDatabases().iterator();
        while (it.hasNext()) {
            SQLDBModel dbModel = it.next();
            String dbName = dbModel.getModelName();
            if (dbName != null && dbName.equals(aName)) {
                return true;
            }
        }
        return false;
    }

    private String generateDBModelName() {
        int cnt = 1;
        String connNamePrefix = "SourceConnection";
        String aName = connNamePrefix + cnt;

        while (isDBModelNameExist(aName)) {
            cnt++;
            aName = connNamePrefix + cnt;
        }

        return aName;
    }
}