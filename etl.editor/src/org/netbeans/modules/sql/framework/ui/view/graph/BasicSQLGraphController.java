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
package org.netbeans.modules.sql.framework.ui.view.graph;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.windows.WindowManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseMetaDataTransfer;
import org.netbeans.modules.etl.ui.DataObjectProvider;
import org.netbeans.modules.etl.ui.ETLDataObject;
import org.netbeans.modules.etl.ui.ETLEditorSupport;
import org.netbeans.modules.sql.framework.model.DBMetaDataFactory;
import org.netbeans.modules.sql.framework.common.utils.TagParserUtility;
import org.netbeans.modules.sql.framework.model.GUIInfo;
import org.netbeans.modules.sql.framework.model.RuntimeDatabaseModel;
import org.netbeans.modules.sql.framework.model.RuntimeInput;
import org.netbeans.modules.sql.framework.model.RuntimeOutput;
import org.netbeans.modules.sql.framework.model.SQLCanvasObject;
import org.netbeans.modules.sql.framework.model.SQLCastOperator;
import org.netbeans.modules.sql.framework.model.SQLConnectableObject;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLInputObject;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLOperator;
import org.netbeans.modules.sql.framework.model.SQLOperatorArg;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.SourceColumn;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetColumn;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.model.VisibleSQLLiteral;
import org.netbeans.modules.sql.framework.model.impl.RuntimeDatabaseModelImpl;
import org.netbeans.modules.sql.framework.model.impl.RuntimeOutputImpl;
import org.netbeans.modules.sql.framework.model.impl.SQLCustomOperatorImpl;
import org.netbeans.modules.sql.framework.model.impl.SourceTableImpl;
import org.netbeans.modules.sql.framework.model.impl.TargetTableImpl;
import org.netbeans.modules.sql.framework.model.utils.SQLObjectUtil;
import org.netbeans.modules.sql.framework.ui.event.SQLDataEvent;
import org.netbeans.modules.sql.framework.ui.graph.IGraphController;
import org.netbeans.modules.sql.framework.ui.graph.IGraphLink;
import org.netbeans.modules.sql.framework.ui.graph.IGraphNode;
import org.netbeans.modules.sql.framework.ui.graph.IGraphPort;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo;
import org.netbeans.modules.sql.framework.ui.graph.impl.CustomOperatorNode;
import org.netbeans.modules.sql.framework.ui.model.CollabSQLUIModel;
import org.netbeans.modules.sql.framework.ui.model.SQLUIModel;
import org.netbeans.modules.sql.framework.ui.view.join.JoinMainDialog;
import org.netbeans.modules.sql.framework.ui.view.join.JoinUtility;
import com.sun.etl.exception.BaseException;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.ui.view.ETLOutputWindowTopComponent;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;
import org.netbeans.modules.sql.framework.model.DBTable;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class BasicSQLGraphController implements IGraphController {

    private static final String NETBEANS_DBTABLE_MIMETYPE = "application/x-java-netbeans-dbexplorer-table;class=org.netbeans.api.db.explorer.DatabaseMetaDataTransfer$Table";
    private static final String LOG_CATEGORY = BasicSQLGraphController.class.getName();
    private static transient final Logger mLogger = Logger.getLogger(BasicSQLGraphController.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
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
                    //String url = dbConn.getDatabaseURL();
                    String catalog = null;
                    try {
                        catalog = conn.getCatalog();
                    } catch (Exception ex) {
                    }

                    String dlgTitle = null;
                    try {
                        String nbBundle1 = mLoc.t("BUND390: Add a table");
                        dlgTitle = nbBundle1.substring(15);
                    } catch (MissingResourceException mre) {
                        dlgTitle = "Add a table";
                    }

                    CollabSQLUIModel sqlModel = (CollabSQLUIModel) collabModel;
                    TypeSelectorPanel selectorPnl = new TypeSelectorPanel(tableTypeSelected);
                    DialogDescriptor dlgDesc = null;
                    DBMetaDataFactory dbMeta = new DBMetaDataFactory();
                    dlgDesc = new DialogDescriptor(selectorPnl, dlgTitle, true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, null, null);

                    Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);
                    dlg.getAccessibleContext().setAccessibleDescription("This dialog helps user to add a table");
                    dlg.setVisible(true);
                    SQLDBTable sTable = null;
                    if (NotifyDescriptor.OK_OPTION == dlgDesc.getValue()) {
                        e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        tableTypeSelected = selectorPnl.getSelectedType();
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
                            tbls = sqlModel.getSQLDefinition().getSourceTables();
                        } else {
                            tbls = sqlModel.getSQLDefinition().getTargetTables();
                        }
                        ((SQLDBTable) dbTable).setAliasUsed(true);
                        ((SQLDBTable) dbTable).setAliasName(generateTableAliasName(isSource, tbls));
                        DBConnectionDefinition def = null;
                        
                        String modelName = generateDBModelName(isSource);
                        List<SQLDBModel> dbmodels = null;
                        if(isSource){
                            dbmodels = sqlModel.getSQLDefinition().getSourceDatabaseModels();
                        } else {
                            dbmodels = sqlModel.getSQLDefinition().getTargetDatabaseModels();
                        }
                        
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
                        if (isSource) {
                            model = SQLModelObjectFactory.getInstance().createDBModel(SQLConstants.SOURCE_DBMODEL);
                        } else {
                            model = SQLModelObjectFactory.getInstance().createDBModel(SQLConstants.TARGET_DBMODEL);
                        }
                        model.setModelName(modelName);
                        model.setConnectionDefinition(def);
                        dbMeta.populateColumns((SQLDBTable) dbTable);
                        ((SQLDBTable) dbTable).setEditable(true);
                        ((SQLDBTable) dbTable).setSelected(true);
                        model.addTable((SQLDBTable) dbTable);

                        if (isSource) {
                            sTable = (SQLDBTable) collabModel.addSourceTable((SQLDBTable) dbTable, loc);
                        } else {
                            sTable = (SQLDBTable) collabModel.addTargetTable((SQLDBTable) dbTable, loc);
                            RuntimeDatabaseModel rtModel = sqlModel.getSQLDefinition().getRuntimeDbModel();
                            if (rtModel == null) {
                                rtModel = new RuntimeDatabaseModelImpl();
                            }
                            RuntimeOutput rtOut = rtModel.getRuntimeOutput();
                            SQLDBColumn column = null;
                            if (rtOut == null) {
                                rtOut = new RuntimeOutputImpl();
                                // add STATUS
                                column = SQLModelObjectFactory.getInstance().createTargetColumn("STATUS", Types.VARCHAR, 0, 0, true);
                                column.setEditable(false);
                                rtOut.addColumn(column);

                                // add STARTTIME
                                column = SQLModelObjectFactory.getInstance().createTargetColumn("STARTTIME", Types.TIMESTAMP, 0, 0, true);
                                column.setEditable(false);
                                rtOut.addColumn(column);

                                // add ENDTIME
                                column = SQLModelObjectFactory.getInstance().createTargetColumn("ENDTIME", Types.TIMESTAMP, 0, 0, true);
                                column.setEditable(false);
                                rtOut.addColumn(column);
                            }
                            String argName = SQLObjectUtil.getTargetTableCountRuntimeOutput((TargetTable) sTable);
                            column = SQLModelObjectFactory.getInstance().createTargetColumn(argName, Types.INTEGER, 0, 0, true);
                            column.setEditable(false);
                            rtOut.addColumn(column);
                            rtModel.addTable(rtOut);
                            sqlModel.getSQLDefinition().addObject(rtModel);
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
                                NotifyDescriptor d = new NotifyDescriptor.Confirmation("Do you want to create a join?", "Confirm join creation", NotifyDescriptor.YES_NO_OPTION);
                                if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.YES_OPTION) {
                                    JoinMainDialog.showJoinDialog(sqlModel.getSQLDefinition().getJoinSources(), null, this.viewC, true);
                                    if (JoinMainDialog.getClosingButtonState() == JoinMainDialog.OK_BUTTON) {
                                        SQLJoinView joinView = JoinMainDialog.getSQLJoinView();
                                        try {
                                            if (joinView != null) {
                                                JoinUtility.handleNewJoinCreation(joinView, JoinMainDialog.getTableColumnNodes(), this.viewC);
                                            }
                                        } catch (BaseException ex) {
                                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Error adding join view.", NotifyDescriptor.INFORMATION_MESSAGE));
                                            mLogger.errorNoloc(mLoc.t("EDIT025: error adding join view{0}", LOG_CATEGORY), ex);

                                        }
                                    }
                                }
                            }
                        }
                        collabModel.setDirty(true);
                        updateActions(collabModel);
                        dropStatus = true;
                    }
                } else {
                    e.rejectDrop();
                }
            } catch (IOException ex) {

                mLogger.errorNoloc(mLoc.t("EDIT161: Caught IOException while handling DnD.{0}", LOG_CATEGORY), ex);
                e.rejectDrop();
            } catch (UnsupportedFlavorException ex) {

                mLogger.errorNoloc(mLoc.t("EDIT161: Caught IOException while handling DnD.{0}", LOG_CATEGORY), ex);
                e.rejectDrop();
            } catch (BaseException ex) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ex.getLocalizedMessage(), NotifyDescriptor.WARNING_MESSAGE));
                mLogger.errorNoloc(mLoc.t("EDIT161: Caught IOException while handling DnD.{0}", LOG_CATEGORY), ex);

                e.rejectDrop();
            } catch (Exception ex) {
                mLogger.errorNoloc(mLoc.t("EDIT161: Caught IOException while handling DnD.{0}", LOG_CATEGORY), ex);

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
        if (!isEditAllowed()) {
            return;
        }

        IGraphNode srcGraphNode = null;
        IGraphNode destGraphNode = null;

        srcGraphNode = from.getDataNode();
        destGraphNode = to.getDataNode();

        if (srcGraphNode != null && destGraphNode != null && srcGraphNode.equals(destGraphNode)) {
            return;
        }

        setParameters(from, to, srcGraphNode, destGraphNode);

        SQLCanvasObject srcObj = (SQLCanvasObject) srcGraphNode.getDataObject();
        SQLConnectableObject destObj = (SQLConnectableObject) destGraphNode.getDataObject();

        if (srcObj == null && destObj == null) {
            return;
        }

        SQLInputObject inputObj = destObj.getInput(destParam);
        SQLObject existing = (inputObj != null) ? inputObj.getSQLObject() : null;
        if (existing instanceof TargetColumn) {
            existing = ((TargetColumn) existing).getValue();
        }

        if (existing != null) {
            return;
        }

        try {
            // do type checking
            boolean userResponse = doTypeChecking(srcObj, destObj, srcParam, destParam);

            if (!userResponse) {
                return;
            }

            CollabSQLUIModel sqlModel = (CollabSQLUIModel) this.collabModel;
            SourceTable s1 = SQLObjectUtil.getInputSourceTable(srcObj, sqlModel.getSQLDefinition().getAllObjects());
            SourceTable s2 = SQLObjectUtil.getInputSourceTable(destObj, sqlModel.getSQLDefinition().getAllObjects());

            TargetTable t1 = SQLObjectUtil.getMappedTargetTable(srcObj, sqlModel.getSQLDefinition().getTargetTables());
            TargetTable t2 = SQLObjectUtil.getMappedTargetTable(destObj, sqlModel.getSQLDefinition().getTargetTables());

            if (t1 == null && s1 != null) {
                t1 = SQLObjectUtil.getMappedTargetTable(s1, sqlModel.getSQLDefinition().getTargetTables());
            }

            if (t2 == null && s2 != null) {
                t2 = SQLObjectUtil.getMappedTargetTable(s2, sqlModel.getSQLDefinition().getTargetTables());
            }

            SQLJoinView jv1 = sqlModel.getJoinView(s1);
            SQLJoinView jv2 = sqlModel.getJoinView(s2);

            // join view is not null but a source table of join view which is not
            // directly linked to target is s1 so we need to find target based on join
            // view
            if (jv1 != null && t1 == null) {
                t1 = SQLObjectUtil.getMappedTargetTable(jv1, sqlModel.getSQLDefinition().getTargetTables());
            }

            if (jv2 != null && t2 == null) {
                t2 = SQLObjectUtil.getMappedTargetTable(jv2, sqlModel.getSQLDefinition().getTargetTables());
            }

            // we have target table but join view is still null, so check target
            // if it has a join view
            if (jv1 == null && t1 != null) {
                jv1 = t1.getJoinView();
            }

            if (jv2 == null && t2 != null) {
                jv2 = t2.getJoinView();
            }

            // can not map it as both source tables are mapped to different
            // target tables or can not map if both source tables belong to different
            // join view
            if (t1 != null && t2 != null && !t1.equals(t2)) {
                String nbBundle2 = mLoc.t("BUND391: Link is not allowed. ''{0}'' is already mapped to target table ''{1}''.", srcObj.getDisplayName(), t1.getDisplayName());
                String msg = nbBundle2.substring(15);

                if (!(srcObj instanceof SourceTable) && s1 != null) {
                    String nbBundle3 = mLoc.t("BUND392: Link is not allowed. ''{0}'' is mapped to source table ''{1}'' which is already mapped to target table ''{2}''.", srcObj.getDisplayName(), s1.getName(), t1.getDisplayName());
                    msg = nbBundle3.substring(15);
                }

                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);

                DialogDisplayer.getDefault().notify(d);

                return;
            } else if (jv1 != null && jv2 != null && !jv1.equals(jv2)) {
                String nbBundle4 = mLoc.t("BUND393: Link is not allowed between join view ''{0}'' and ''{1}''.", jv1.getAliasName(), jv2.getAliasName());
                NotifyDescriptor d = new NotifyDescriptor.Message(nbBundle4.substring(15), NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                return;
            }

            if (s1 != null && s2 != null && !s1.equals(s2)) {
                // if both source tables are not part of a join view then prompt user for
                // a join view
                if (jv1 == null && jv2 == null) {
                    ArrayList<DBTable> sTables = new ArrayList<DBTable>();
                    sTables.add(s1);
                    sTables.add(s2);
                    this.promptForNewJoinView(srcObj, destObj, sTables);
                    return;
                } else if (jv1 == null && jv2 != null) {
                    this.promptForAddToExistingJoinView(srcObj, destObj, s1, jv2);
                    return;
                } else if (jv1 != null && jv2 == null) {
                    this.promptForAddToExistingJoinView(srcObj, destObj, s2, jv1);
                    return;
                }
            }

            // create actual link
            createLink(srcObj, destObj);

            if (destObj instanceof TargetTable) {
                SQLTargetTableArea tt = (SQLTargetTableArea) destGraphNode;
                TargetColumn tCol = (TargetColumn) inputObj.getSQLObject();
                if ((tCol != null) && (tCol.isPrimaryKey())) {
                    tt.setConditionIcons();
                }
            }
            updateActions(collabModel);
        } catch (Exception sqle) {
            NotifyDescriptor d = new NotifyDescriptor.Message(sqle.toString(), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
    }

    private void createLink(SQLCanvasObject srcObj, SQLConnectableObject destObj) throws BaseException {
        if (srcObj != null && destObj != null) {
            collabModel.createLink(srcObj, srcParam, destObj, destParam);
        }
    }

    private void promptForNewJoinView(final SQLCanvasObject srcObj, final SQLConnectableObject destObj, final List<DBTable> sTables) {

        Runnable runDialog = new Runnable() {

            public void run() {
                try {
                    if (promptForNewJoinView(sTables)) {
                        // create actual link
                        createLink(srcObj, destObj);
                    }
                } catch (Exception ex) {
                    NotifyDescriptor d = new NotifyDescriptor.Message(ex.toString(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(d);
                }
            }
        };

        SwingUtilities.invokeLater(runDialog);
    }

    private boolean promptForNewJoinView(List<DBTable> sTables) throws BaseException {
        boolean userResponse = false;
        String nbBundle5 = mLoc.t("BUND394: You need to create a join view if you want to map columns of two or more tables.  Create a join view?");
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(nbBundle5.substring(15), NotifyDescriptor.WARNING_MESSAGE);
        Object response = DialogDisplayer.getDefault().notify(d);

        if (response.equals(NotifyDescriptor.OK_OPTION)) {
            CollabSQLUIModel sqlModel = (CollabSQLUIModel) this.collabModel;
            List<DBTable> joinSources = sqlModel.getSQLDefinition().getJoinSources();

            joinSources.removeAll(sTables);
            JoinMainDialog.showJoinDialog(joinSources, sTables, this.viewC, false);

            if (JoinMainDialog.getClosingButtonState() == JoinMainDialog.OK_BUTTON) {
                SQLJoinView joinView = JoinMainDialog.getSQLJoinView();
                if (joinView != null) {
                    sqlModel.setDirty(true);
                    JoinUtility.handleNewJoinCreation(joinView, JoinMainDialog.getTableColumnNodes(), this.viewC);
                    userResponse = true;
                }
            }
        }

        return userResponse;
    }

    private void promptForAddToExistingJoinView(final SQLCanvasObject srcObj, final SQLConnectableObject destObj, final SourceTable sTable, final SQLJoinView initJoinView) {
        Runnable runDialog = new Runnable() {

            public void run() {
                try {
                    if (promptForAddToExistingJoinView(sTable, initJoinView)) {
                        // create actual link
                        createLink(srcObj, destObj);
                    }
                } catch (Exception ex) {
                    NotifyDescriptor d = new NotifyDescriptor.Message(ex.toString(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(d);
                }
            }
        };

        SwingUtilities.invokeLater(runDialog);
    }

    private boolean promptForAddToExistingJoinView(SourceTable sTable, SQLJoinView initJoinView) throws BaseException {
        boolean userResponse = false;
        String nbBundle6 = mLoc.t("BUND395: You need to add table ''{0}'' to the already existing join view in order to map columns.  Add table to existing join view?", sTable.getName());
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(nbBundle6.substring(15), NotifyDescriptor.WARNING_MESSAGE);
        Object response = DialogDisplayer.getDefault().notify(d);

        if (response.equals(NotifyDescriptor.OK_OPTION)) {
            CollabSQLUIModel sqlModel = (CollabSQLUIModel) this.collabModel;
            List<DBTable> joinSources = sqlModel.getSQLDefinition().getJoinSources();
            joinSources.remove(sTable);
            JoinMainDialog.showJoinDialog(joinSources, sTable, initJoinView, this.viewC);
            if (JoinMainDialog.getClosingButtonState() == JoinMainDialog.OK_BUTTON) {
                SQLJoinView joinView = JoinMainDialog.getSQLJoinView();

                if (!initJoinView.equals(joinView)) {
                    sqlModel.setDirty(true);
                }
                // join sources
                List<DBTable> jSources = initJoinView.getSourceTables();
                jSources.add(sTable);
                // call this
                JoinUtility.editJoinView(initJoinView, joinView, jSources, JoinMainDialog.getTableColumnNodes(), this.viewC);
                userResponse = true;
            }
        }

        return userResponse;
    }

    /** Creates a new instance of SQLGraphController */
    public BasicSQLGraphController() {
    }

    private boolean doTypeChecking(SQLCanvasObject srcObj, SQLConnectableObject destObj, String srcParam1, String destParam1) throws BaseException {

        String msg = null;
        SQLObject input = srcObj;

        // get the specific sub object from srcObj which we are trying to link
        input = srcObj.getOutput(srcParam1);

        if (!destObj.isInputValid(destParam1, input)) {
            try {
                String srcObjType = TagParserUtility.getDisplayStringFor(input.getObjectType());
                String destObjType = TagParserUtility.getDisplayStringFor(destObj.getObjectType());
                String srcName = destObj.getDisplayName();

                if (srcName != null && destParam1 != null) {
                    String nbBundle7 = mLoc.t("BUND396: Cannot connect {0} to {1}-{2} at input {3}.", srcObjType, destObjType, destObj.getDisplayName(), destParam1);
                    msg = nbBundle7.substring(15);
                } else {
                    String nbBundle8 = mLoc.t("BUND397: Cannot connect {0} to {1}", srcObjType, destObjType);
                    msg = nbBundle8.substring(15);
                }
            } catch (Exception e) {
                mLogger.errorNoloc(mLoc.t("EDIT153: Caught Exception while resolving error message{0}", LOG_CATEGORY), e);

                msg = "Cannot link these objects together.";
            }

            NotifyDescriptor.Message m = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);

            DialogDisplayer.getDefault().notify(m);
            return false;
        }

        switch (destObj.isInputCompatible(destParam1, input)) {
            case SQLConstants.TYPE_CHECK_INCOMPATIBLE:
                try {
                    String nbBundle9 = mLoc.t("BUND398: Incompatible source and target datatypes.");
                    msg = nbBundle9.substring(15);
                } catch (MissingResourceException e) {
                    msg = "Incompatible source and target datatypes.";
                }

                NotifyDescriptor.Message m = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);

                DialogDisplayer.getDefault().notify(m);
                return false;
            case SQLConstants.TYPE_CHECK_DOWNCAST_WARNING:
                try {
                    String nbBundle11 = mLoc.t("BUND399: Connecting these types may result in a loss of precision or data truncation.  Continue?");
                    msg = nbBundle11.substring(15);
                } catch (MissingResourceException e) {
                    msg = "Connecting these datatypes may result in a loss of " + "precision or data truncation in the target.  Continue?";
                }

                String title = null;
                try {
                    String nbBundle12 = mLoc.t("BUND400: Datatype conversion");
                    title = nbBundle12.substring(15);
                } catch (MissingResourceException e) {
                    title = "Datatype conversion";
                }

                NotifyDescriptor.Confirmation d = new NotifyDescriptor.Confirmation(msg, title, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE);

                return DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION;
            case SQLConstants.TYPE_CHECK_COMPATIBLE:
            default:
                return true;
        }
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

            if (destObj instanceof TargetTable) {
                SQLTargetTableArea tt = (SQLTargetTableArea) destGraphNode;
                SQLInputObject inputObj = destObj.getInput(destParam);
                if (inputObj != null) {
                    TargetColumn tCol = (TargetColumn) inputObj.getSQLObject();
                    if ((tCol != null) && (tCol.isPrimaryKey())) {
                        tt.setConditionIcons();
                    }
                }
            }
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
                    String nbBundle13 = mLoc.t("BUND401: New Cast-As Operator");
                    CastAsDialog castDlg = new CastAsDialog(WindowManager.getDefault().getMainWindow(), nbBundle13.substring(15), true);
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
                    String nbBundle14 = mLoc.t("BUND402: User Function");
                    String title = nbBundle14.substring(15);
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
                    String nbBundle15 = mLoc.t("BUND403: New Literal Object");
                    LiteralDialog dlg = new LiteralDialog(WindowManager.getDefault().getMainWindow(), nbBundle15.substring(15), true);
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
        } catch (BaseException e) {
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
            if (sqlObj.getObjectType() == SQLConstants.SOURCE_TABLE || sqlObj.getObjectType() == SQLConstants.TARGET_TABLE) {
                SourceColumn col = SQLObjectUtil.removeRuntimeInput((SQLDBTable) sqlObj, (CollabSQLUIModel) collabModel);
                if (col != null) {
                    SQLDataEvent evt = new SQLDataEvent(collabModel, (RuntimeInput) col.getParent(), col);
                    collabModel.fireChildObjectDeletedEvent(evt);
                }
                ETLOutputWindowTopComponent.getDefault().findAndRemoveComponent(sqlObj);
            }
            updateActions(collabModel);
        } catch (Exception e) {

            mLogger.errorNoloc(mLoc.t("EDIT166: Caught exception while removing object{0}", LOG_CATEGORY), e);
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
        String connNamePrefix = isSource ? "SourceConnection" : "TargetConnection";
        String aName = connNamePrefix + cnt;
       
        while (isDBModelNameExist(isSource, aName)) {
            cnt++;
            aName = connNamePrefix + cnt;
        }

        return aName;
    }

    private boolean isDBModelNameExist(boolean isSource, String aName) {
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

    class TypeSelectorPanel extends JPanel {

        private ButtonGroup bg;
        private JRadioButton source;
        private JRadioButton target;

        public TypeSelectorPanel() {
            this(SQLConstants.SOURCE_TABLE);
        }

        public TypeSelectorPanel(int newType) {
            super();
            setLayout(new BorderLayout());

            JPanel insetPanel = new JPanel();
            insetPanel.setLayout(new BoxLayout(insetPanel, BoxLayout.PAGE_AXIS));
            insetPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 15));

            String title = "";
            try {
                String nbBundle16 = mLoc.t("BUND404: Select table type:");
                title = nbBundle16.substring(15);
            } catch (MissingResourceException mre) {
                title = "Specify table type:";
            }

            insetPanel.add(new JLabel(title));

            String sourceLabel = "";
            try {
                String nbBundle15 = mLoc.t("BUND405: Source table");
                sourceLabel = nbBundle15.substring(15);
            } catch (MissingResourceException mre) {
                sourceLabel = "Source table";
            }

            String targetLabel = "";
            try {
                String nbBundle18 = mLoc.t("BUND406: Target table");
                targetLabel = nbBundle18.substring(15);
            } catch (MissingResourceException mre) {
                targetLabel = "Target table";
            }

            source = new JRadioButton(sourceLabel);
            target = new JRadioButton(targetLabel);

            insetPanel.add(source);
            insetPanel.add(target);

            add(insetPanel, BorderLayout.CENTER);

            bg = new ButtonGroup();
            bg.add(source);
            bg.add(target);

            setSelectedType(newType);
        }

        public void setSelectedType(int type) {
            switch (type) {
                case SQLConstants.TARGET_TABLE:
                    bg.setSelected(target.getModel(), true);
                    break;
                case SQLConstants.SOURCE_TABLE:
                default:
                    bg.setSelected(source.getModel(), true);
            }
        }

        public int getSelectedType() {
            return target.isSelected() ? SQLConstants.TARGET_TABLE : SQLConstants.SOURCE_TABLE;
        }

        @Override
        public void addNotify() {
            super.addNotify();

            switch (getSelectedType()) {
                case SQLConstants.TARGET_TABLE:
                    target.requestFocusInWindow();
                    break;
                case SQLConstants.SOURCE_TABLE:
                default:
                    source.requestFocusInWindow();
                    break;
            }
        }
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
        try {
            ETLDataObject etlDataObject = DataObjectProvider.getProvider().getActiveDataObject();
            if (model.isDirty()) {
                ETLEditorSupport editor = etlDataObject.getETLEditorSupport();
                editor.synchDocument();
            }
        } catch (Exception e) {
            //ignore
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
                    } else {
                        dbTable = new TargetTableImpl(currTable[DBMetaDataFactory.NAME], currTable[DBMetaDataFactory.SCHEMA], currTable[DBMetaDataFactory.CATALOG]);
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
