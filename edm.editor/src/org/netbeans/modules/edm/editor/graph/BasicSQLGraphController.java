/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.edm.editor.graph;

import java.awt.Dialog;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.MissingResourceException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.windows.WindowManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseMetaDataTransfer;
import org.netbeans.modules.edm.model.DBMetaDataFactory;
import org.netbeans.modules.edm.model.GUIInfo;
import org.netbeans.modules.edm.model.RuntimeInput;
import org.netbeans.modules.edm.model.SQLCanvasObject;
import org.netbeans.modules.edm.model.SQLCastOperator;
import org.netbeans.modules.edm.model.SQLConnectableObject;
import org.netbeans.modules.edm.model.SQLConstants;
import org.netbeans.modules.edm.model.SQLDBModel;
import org.netbeans.modules.edm.model.SQLDBTable;
import org.netbeans.modules.edm.model.SQLModelObjectFactory;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.model.SQLOperator;
import org.netbeans.modules.edm.model.SQLOperatorArg;
import org.netbeans.modules.edm.model.SQLPredicate;
import org.netbeans.modules.edm.model.SourceColumn;
import org.netbeans.modules.edm.model.VisibleSQLLiteral;
import org.netbeans.modules.edm.model.impl.SQLCustomOperatorImpl;
import org.netbeans.modules.edm.model.impl.SourceTableImpl;
import org.netbeans.modules.edm.editor.utils.SQLObjectUtil;
import org.netbeans.modules.edm.editor.ui.event.SQLDataEvent;
import org.netbeans.modules.edm.editor.graph.jgo.IGraphController;
import org.netbeans.modules.edm.editor.graph.jgo.IGraphLink;
import org.netbeans.modules.edm.editor.graph.jgo.IGraphNode;
import org.netbeans.modules.edm.editor.graph.jgo.IGraphPort;
import org.netbeans.modules.edm.editor.graph.jgo.IGraphView;
import org.netbeans.modules.edm.editor.graph.jgo.IOperatorXmlInfo;
import org.netbeans.modules.edm.editor.graph.jgo.CustomOperatorNode;
import org.netbeans.modules.edm.editor.ui.model.CollabSQLUIModel;
import org.netbeans.modules.edm.editor.ui.model.SQLUIModel;
import org.netbeans.modules.edm.model.EDMException;
import org.netbeans.modules.edm.model.DBConnectionDefinition;
import org.openide.util.NbBundle;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class BasicSQLGraphController implements IGraphController {

    private static final String NETBEANS_DBTABLE_MIMETYPE = "application/x-java-netbeans-dbexplorer-table;class=org.netbeans.api.db.explorer.DatabaseMetaDataTransfer$Table";
    private static final String LOG_CATEGORY = BasicSQLGraphController.class.getName();
    private static transient final Logger mLogger = Logger.getLogger(BasicSQLGraphController.class.getName());
    private static DataFlavor[] mDataFlavorArray = new DataFlavor[1];
    

    static {
        try {
            mDataFlavorArray[0] = new DataFlavor(NETBEANS_DBTABLE_MIMETYPE);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    protected SQLUIModel collabModel;
    protected IGraphView viewC;
    private String srcParam = null;
    private String destParam = null;
    private transient int tableTypeSelected = SQLConstants.SOURCE_TABLE;

    /**
     * Handle drop.
     *
     * @param e DropTargetDropEvent
     */
    public void handleDrop(java.awt.dnd.DropTargetDropEvent e) {
        if (!isEditAllowed()) {
            return;
        }

        boolean dropStatus = false;
        Point loc = e.getLocation();
        if (e.isDataFlavorSupported(mDataFlavorArray[0])) {
            Connection conn = null;
            try {
                Transferable t = e.getTransferable();
                Object o = t.getTransferData(mDataFlavorArray[0]);
                if (o instanceof DatabaseMetaDataTransfer.Table) {
                    DatabaseMetaDataTransfer.Table tbl = (DatabaseMetaDataTransfer.Table) o;
                    DatabaseConnection dbConn = tbl.getDatabaseConnection();
                    conn = dbConn.getJDBCConnection();
                    String tableName = tbl.getTableName();
                    String schema = tbl.getDatabaseConnection().getSchema();
                    String catalog = null;
                    try {
                        catalog = conn.getCatalog();
                    } catch (Exception ex) {
                    }

                    String dlgTitle = null;
                    try {
                        dlgTitle = NbBundle.getMessage(BasicSQLGraphController.class, "TITLE_dlg_table_type");
                    } catch (MissingResourceException mre) {
                        dlgTitle = "Add a table";
                    }

                    CollabSQLUIModel sqlModel = (CollabSQLUIModel) collabModel;
                    DBMetaDataFactory dbMeta = new DBMetaDataFactory();

                    SQLDBTable sTable = null;

                        boolean isSource = true;
                        String[][] tableList = null;
                        try {
                            dbMeta.connectDB(conn);
                            schema = (schema == null) ? "" : schema;
                            catalog = (catalog == null) ? "" : catalog;
                            tableList = dbMeta.getTablesAndViews(catalog, schema, "", false);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        Object dbTable = createTable(tableList, tableName, isSource);
                        List tbls = null;
                        tbls = sqlModel.getSQLDefinition().getSourceTables();
                        ((SQLDBTable) dbTable).setAliasUsed(true);
                        ((SQLDBTable) dbTable).setAliasName(generateTableAliasName(isSource, tbls));
                        DBConnectionDefinition def = null;
                        
                        String modelName = generateDBModelName(isSource);
                        List<SQLDBModel> dbmodels = null;
                        dbmodels = sqlModel.getSQLDefinition().getSourceDatabaseModels();
                        
                        for(SQLDBModel dbm: dbmodels){
                            if(dbm.getConnectionDefinition().getConnectionURL().equals(dbConn.getDatabaseURL())){
                                modelName = dbm.getModelName();
                            } 
                        }
                        
                        try {
                            def = SQLModelObjectFactory.getInstance().createDBConnectionDefinition(modelName, dbMeta.getDBType(), dbConn.getDriverClass(), dbConn.getDatabaseURL(), dbConn.getUser(), dbConn.getPassword(), "Descriptive info here");
                        } catch (Exception ex) {
                            //ignore
                        }
                        SQLDBModel model = null;
                        model = SQLModelObjectFactory.getInstance().createDBModel(SQLConstants.SOURCE_DBMODEL);
                        model.setModelName(modelName);
                        model.setConnectionDefinition(def);
                        dbMeta.populateColumns((SQLDBTable) dbTable);
                        ((SQLDBTable) dbTable).setEditable(true);
                        ((SQLDBTable) dbTable).setSelected(true);
                        model.addTable((SQLDBTable) dbTable);

                        if (isSource) {
                            sTable = (SQLDBTable) collabModel.addSourceTable((SQLDBTable) dbTable, loc);
                        }

                        SourceColumn runtimeArg = SQLObjectUtil.createRuntimeInput(sTable, sqlModel.getSQLDefinition());
                        SQLObjectUtil.setOrgProperties(sTable);

                        if (runtimeArg != null && (RuntimeInput) runtimeArg.getParent() != null) {
                            RuntimeInput runtimeInput = (RuntimeInput) runtimeArg.getParent();
                            // if runtime input is not in SQL definition then add it
                            if ((sqlModel.getSQLDefinition().isTableExists(runtimeInput)) == null) {
                                sqlModel.getSQLDefinition().addObject((SQLObject) runtimeInput);
                                collabModel.addObject(runtimeInput);
                                SQLDataEvent evt = new SQLDataEvent(collabModel, runtimeInput, runtimeArg);
                                collabModel.fireChildObjectCreatedEvent(evt);
                            }
                        }
                        if (sqlModel.getSQLDefinition().getSourceTables().size() > 1) {
                            if (dbTable instanceof SourceTableImpl) {
                                e.dropComplete(true);
                            }
                        }
                        collabModel.setDirty(true);
                        updateActions(collabModel);
                        dropStatus = true;
                } else {
                    e.rejectDrop();
                }
            } catch (IOException ex) {

                mLogger.log(Level.INFO,NbBundle.getMessage(BasicSQLGraphController.class, "LOG.INFO_Caught_IOException",new Object[] {LOG_CATEGORY}),ex);
                e.rejectDrop();
            } catch (UnsupportedFlavorException ex) {

                mLogger.log(Level.INFO,NbBundle.getMessage(BasicSQLGraphController.class, "LOG.INFO_Caught_IOException",new Object[] {LOG_CATEGORY}),ex);
                e.rejectDrop();
            } catch (EDMException ex) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ex.getLocalizedMessage(), NotifyDescriptor.WARNING_MESSAGE));
                mLogger.log(Level.INFO,NbBundle.getMessage(BasicSQLGraphController.class, "LOG.INFO_Caught_IOException",new Object[] {LOG_CATEGORY}),ex);

                e.rejectDrop();
            } catch (Exception ex) {
                mLogger.log(Level.INFO,NbBundle.getMessage(BasicSQLGraphController.class, "LOG.INFO_Caught_IOException",new Object[] {LOG_CATEGORY}),ex);

                e.rejectDrop();
            } finally {
                e.dropComplete(dropStatus);
                conn = null;
            }
        } else {
            e.rejectDrop();
        }
    }

    /**
     * Handle drop of arbitrary object.
     *
     * @param obj Object dropped onto canvas
     */
    public void handleObjectDrop(Object obj) {
        if (!isEditAllowed()) {
            return;
        }
    }

    /**
     * handle new link
     *
     * @param from IGraphPort
     * @param to IGraphPort
     */
    public void handleLinkAdded(IGraphPort from, IGraphPort to) {
    }



    public BasicSQLGraphController() {
    }

    private void setParameters(IGraphPort from, IGraphPort to, IGraphNode srcGraphNode, IGraphNode destGraphNode) {

        if (srcGraphNode != null && destGraphNode != null) {
            srcParam = srcGraphNode.getFieldName(from);
            destParam = destGraphNode.getFieldName(to);
        }
    }

    /**
     * handle link deletion
     *
     * @param link IGraphLink
     */
    public void handleLinkDeleted(IGraphLink link) {
        if (!isEditAllowed()) {
            return;
        }

        IGraphPort from = link.getFromGraphPort();
        IGraphPort to = link.getToGraphPort();
        IGraphNode srcGraphNode = from.getDataNode();
        IGraphNode destGraphNode = to.getDataNode();

        setParameters(from, to, srcGraphNode, destGraphNode);

        // source is always canvas object and destination is always expression object
        SQLCanvasObject srcObj = (SQLCanvasObject) srcGraphNode.getDataObject();
        SQLConnectableObject destObj = (SQLConnectableObject) destGraphNode.getDataObject();

        if (srcObj == null && destObj == null) {
            return;
        }

        try {
            collabModel.removeLink(srcObj, srcParam, destObj, destParam);
        } catch (Exception e) {
            NotifyDescriptor d = new NotifyDescriptor.Message(e.toString(), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
    }

    /**
     * handle node add
     *
     * @param xmlInfo IOperatorXmlInfo
     * @param dropLocation dropLocation
     */
    @SuppressWarnings(value = "fallthrough")
    public void handleNodeAdded(IOperatorXmlInfo xmlInfo, Point dropLocation) {
        if (!isEditAllowed()) {
            return;
        }

        // what object type is dropped
        String className = xmlInfo.getObjectClassName();

        try {
            // create object
            SQLCanvasObject sqlObj = collabModel.createObject(className);
            sqlObj.setDisplayName(xmlInfo.getName());

            GUIInfo guiInfo = sqlObj.getGUIInfo();

            if (dropLocation.x == 0 && dropLocation.y == 0) {
                guiInfo.setX(guiInfo.getX());
                guiInfo.setY(guiInfo.getY());
            } else {
                guiInfo.setX(dropLocation.x);
                guiInfo.setY(dropLocation.y);
            }

            collabModel.setDirty(true);
            // do special processing for following objects
            switch (sqlObj.getObjectType()) {
                case SQLConstants.CAST_OPERATOR:
                    CastAsDialog castDlg = new CastAsDialog(WindowManager.getDefault().getMainWindow(), NbBundle.getMessage(BasicSQLGraphController.class, "TITLE_new_castas"), true);
                    castDlg.show();
                    if (castDlg.isCanceled()) {
                        return;
                    }

                    SQLCastOperator castOp = (SQLCastOperator) sqlObj;
                    castOp.setOperatorXmlInfo(xmlInfo);

                    castOp.setJdbcType(castDlg.getJdbcType());

                    int precision = castDlg.getPrecision();
                    castOp.setPrecision(precision);

                    int scale = castDlg.getScale();
                    castOp.setScale(scale);

                    break;
                case SQLConstants.CUSTOM_OPERATOR:
                    CustomOperatorPane customOptPane = new CustomOperatorPane(new ArrayList());
                    String title = NbBundle.getMessage(BasicSQLGraphController.class, "TITLE_user_function");
                    DialogDescriptor dlgDesc = new DialogDescriptor(customOptPane, title, true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, null, null);
                    Dialog customOptDialog = DialogDisplayer.getDefault().createDialog(dlgDesc);
                    customOptDialog.getAccessibleContext().setAccessibleDescription("This dialog hepls user configure user-defined functions");
                    customOptDialog.setVisible(true);
                    if (NotifyDescriptor.CANCEL_OPTION == dlgDesc.getValue()) {
                        return;
                    }
                    List inputArgs = customOptPane.getArgList();
                    SQLOperatorArg retType = customOptPane.getReturnType();
                    CustomOperatorNode customOptNode = new CustomOperatorNode(xmlInfo, inputArgs, retType);
                    SQLCustomOperatorImpl custOp = (SQLCustomOperatorImpl) sqlObj;
                    custOp.setOperatorXmlInfo(customOptNode);
                    custOp.setCustomOperatorName(customOptPane.getFunctionName());
                    custOp.getOperatorDefinition().setArgList(inputArgs);
                    custOp.initializeInputs(inputArgs.size());
                    break;
                case SQLConstants.VISIBLE_PREDICATE:
                    ((SQLPredicate) sqlObj).setOperatorXmlInfo(xmlInfo);
                // fall through to set XML info (using common SQLOperator interface)
                case SQLConstants.GENERIC_OPERATOR:
                case SQLConstants.DATE_ARITHMETIC_OPERATOR:
                    // for operator we need to set the type of operator
                    // ((SQLGenericOperator) sqlObj).setOperatorType(xmlInfo.getName());
                    ((SQLOperator) sqlObj).setOperatorXmlInfo(xmlInfo);
                    sqlObj.setDisplayName(xmlInfo.getDisplayName());
                    break;
                case SQLConstants.VISIBLE_LITERAL:
                    LiteralDialog dlg = new LiteralDialog(WindowManager.getDefault().getMainWindow(), NbBundle.getMessage(BasicSQLGraphController.class, "TITLE_new_literal"), true);
                    dlg.show();

                    // OK button is not pressed so return
                    if (dlg.isCanceled()) {
                        return;
                    }

                    String value = dlg.getLiteral();
                    VisibleSQLLiteral lit = (VisibleSQLLiteral) sqlObj;
                    lit.setJdbcType(dlg.getType());
                    lit.setValue(value);
                    lit.setDisplayName(xmlInfo.getDisplayName());

                    break;
            }

            // now add the object
            collabModel.addObject(sqlObj);
            collabModel.setDirty(true);
        } catch (EDMException e) {
            NotifyDescriptor d = new NotifyDescriptor.Message(e.toString(), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
    }

    /**
     * handle node deletion
     *
     * @param node IGraphNode
     */
    public void handleNodeRemoved(IGraphNode node) {
        if (!isEditAllowed()) {
            return;
        }

        try {
            IGraphNode pNode = node.getParentGraphNode();
            // if node has a parent then we should delete it from parent and return
            // we do not need to go to collaboration as node is contained within
            // its parent and deleting it from its parent should remove it
            if (pNode != null) {
                pNode.removeChildNode(node);
                return;
            }

            SQLCanvasObject sqlObj = (SQLCanvasObject) node.getDataObject();
            if (sqlObj != null) {
                collabModel.removeObject(sqlObj);
            }

            // if a source table is deleted, check if it is flatfile and try to remove
            // its auto generated runtime input argument for file location
            if (sqlObj.getObjectType() == SQLConstants.SOURCE_TABLE) {
                SourceColumn col = SQLObjectUtil.removeRuntimeInput((SQLDBTable) sqlObj, (CollabSQLUIModel) collabModel);
                if (col != null) {
                    SQLDataEvent evt = new SQLDataEvent(collabModel, (RuntimeInput) col.getParent(), col);
                    collabModel.fireChildObjectDeletedEvent(evt);
                }
            }
            updateActions(collabModel);
        } catch (Exception e) {

            mLogger.log(Level.INFO,NbBundle.getMessage(BasicSQLGraphController.class, "LOG.INFO_Caught_exception",new Object[] {LOG_CATEGORY}),e);
            NotifyDescriptor d = new NotifyDescriptor.Message(e.toString(), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
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
    
    private String generateDBModelName(boolean isSource) {
        int cnt = 1;
        //String connNamePrefix = isSource ? "SourceConnection" : "TargetConnection";
        String connNamePrefix = "SourceConnection";
        String aName = connNamePrefix + cnt;
       
        while (isDBModelNameExist(aName)) {
            cnt++;
            aName = connNamePrefix + cnt;
        }

        return aName;
    }

    private boolean isDBModelNameExist(String aName) {
        CollabSQLUIModel sqlModel = (CollabSQLUIModel) collabModel;
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

    /**
     * Sets the data model which this controller modifies
     *
     * @param newModel new data model
     */
    public void setDataModel(Object newModel) {
        collabModel = (SQLUIModel) newModel;
    }

    public Object getDataModel() {
        return collabModel;
    }

    protected boolean isEditAllowed() {
        /*  if (viewC != null) {
        return viewC.canEdit();
        }*/

        return true;
    }

    /**
     * set the view from which this controller interacts
     *
     * @param view view
     */
    public void setView(Object view) {
        viewC = (IGraphView) view;
    }

    private void updateActions(SQLUIModel model) {
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

    class LinkInfo {

        private SQLCanvasObject sObj;
        private SQLConnectableObject eObj;
        private String sParam;
        private String dParam;

        LinkInfo(SQLCanvasObject srcObj, SQLConnectableObject expObj, String srcParam, String destParam) {
            this.sObj = srcObj;
            this.eObj = expObj;
            this.sParam = srcParam;
            this.dParam = destParam;
        }

        public SQLCanvasObject getSource() {
            return this.sObj;
        }

        public SQLConnectableObject getTarget() {
            return this.eObj;
        }

        public String getSourceParam() {
            return this.sParam;
        }

        public String getTargetParam() {
            return this.dParam;
        }
    }
}
