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
package org.netbeans.modules.sql.framework.ui.output.dataview;

import com.sun.sql.framework.jdbc.DBConstants;
import com.sun.sql.framework.jdbc.SQLPart;
import com.sun.sql.framework.utils.StringUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.java.hulp.i18n.Logger;
import org.axiondb.ExternalConnectionProvider;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.codegen.DB;
import org.netbeans.modules.sql.framework.codegen.DBFactory;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.codegen.Statements;
import org.netbeans.modules.sql.framework.codegen.axion.AxionDB;
import org.netbeans.modules.sql.framework.codegen.axion.AxionPipelineStatements;
import org.netbeans.modules.sql.framework.common.jdbc.SQLUtils;
import org.netbeans.modules.sql.framework.common.utils.DBExplorerUtil;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;
import org.netbeans.modules.sql.framework.model.RuntimeDatabaseModel;
import org.netbeans.modules.sql.framework.model.RuntimeInput;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLJoinOperator;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.ui.SwingWorker;
import org.netbeans.modules.sql.framework.ui.utils.AxionExternalConnectionProvider;
import org.netbeans.modules.sql.framework.ui.utils.UIUtil;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;

/**
 * @author Ahimanikya Satapathy
 */
class DataViewWorkerThread extends SwingWorker {

    private SQLObject dbTable;
    private String errMsg;
    DataOutputPanel dataOutputPanel;
    private static transient final Logger mLogger = Logger.getLogger(DataViewWorkerThread.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    public DataViewWorkerThread(SQLObject table, DataOutputPanel dataOutputPanel) {
        super();
        this.dataOutputPanel = dataOutputPanel;
        this.dbTable = table; // May be a SQLDBTable or a JoinView

    }

    public Object construct() {
        if (dbTable instanceof SQLDBTable) {
            showDataForDBTable();
        } else if (dbTable instanceof SQLJoinView) {
            showDataForJoinView();
        } else if (dbTable instanceof SQLJoinOperator) {
            showDataForJoinOperator();
        }
        return "";
    }

    @Override
    public void finished() {

        if (this.errMsg != null) {
            String nbBundle = mLoc.t("BUND349: Error fetching data for table {0}.Cause:{1}", dbTable.getDisplayName(), this.errMsg);
            String errorMsg = nbBundle.substring(15);
            DialogDisplayer.getDefault().notify(new Message(errorMsg, NotifyDescriptor.ERROR_MESSAGE));
        }
        UIUtil.stopProgressDialog();
        if (dataOutputPanel.truncateButton != null) {
            dataOutputPanel.truncateButton.setEnabled(true);
        }
        dataOutputPanel.refreshButton.setEnabled(true);
        dataOutputPanel.refreshField.setEnabled(true);
        if (this.errMsg == null && (this.dbTable instanceof SQLDBTable || dbTable instanceof SQLJoinView || dbTable instanceof SQLJoinOperator)) {
            if (((dataOutputPanel.nowCount - dataOutputPanel.maxRows) > 0) && (dataOutputPanel.totalCount != 0)) {
                dataOutputPanel.first.setEnabled(true);
                dataOutputPanel.previous.setEnabled(true);
            }
            if (((dataOutputPanel.nowCount + dataOutputPanel.maxRows) <= dataOutputPanel.totalCount) && (dataOutputPanel.totalCount != 0)) {
                dataOutputPanel.next.setEnabled(true);
                dataOutputPanel.last.setEnabled(true);
            }
            if ((dataOutputPanel.nowCount - dataOutputPanel.maxRows) <= 0) {
                dataOutputPanel.first.setEnabled(false);
                dataOutputPanel.previous.setEnabled(false);
            }
            if ((dataOutputPanel.nowCount + dataOutputPanel.maxRows) > dataOutputPanel.totalCount) {
                dataOutputPanel.next.setEnabled(false);
                dataOutputPanel.last.setEnabled(false);
            }
            if (dataOutputPanel.maxRows > dataOutputPanel.totalCount) {
                dataOutputPanel.maxRows = dataOutputPanel.totalCount;
                dataOutputPanel.refreshField.setText(String.valueOf(dataOutputPanel.maxRows));
            }
            
            // editing controls
            if(this.dbTable instanceof SQLDBTable) {
                if (dataOutputPanel.totalCount != 0 && dataOutputPanel.maxRows != 0) {
                    dataOutputPanel.deleteRow.setEnabled(true);
                } else {
                    dataOutputPanel.deleteRow.setEnabled(false);
                }
                dataOutputPanel.insert.setEnabled(true);
            }
        } else {
            dataOutputPanel.first.setEnabled(false);
            dataOutputPanel.next.setEnabled(false);
            dataOutputPanel.last.setEnabled(false);
            dataOutputPanel.previous.setEnabled(false);
            dataOutputPanel.commit.setEnabled(false);
            dataOutputPanel.deleteRow.setEnabled(false);
            dataOutputPanel.insert.setEnabled(false);
        }

        if (dataOutputPanel.totalCount == 0) {
            dataOutputPanel.nowCount = 0;
        }
        
        dataOutputPanel.refreshField.setText("" + dataOutputPanel.maxRows);
        dataOutputPanel.queryView.revalidate();
        dataOutputPanel.queryView.repaint();
    }

    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
            }
        }
    }

    private void handleException() {
        dataOutputPanel.queryView.clearView();
        dataOutputPanel.totalRowsLabel.setText("0");
    }

    private void shutdownConnection(Connection conn) {
        if (conn != null) {
            try {
                if (conn.getMetaData().getDriverName().contains("Axion")) {
                    conn.createStatement().execute("shutdown");
                }
                conn.close();
            } catch (SQLException e) {
                conn = null;
            }
        }
    }

    private void showDataForDBTable() {
        Statement stmt = null;
        Connection conn = null;

        try {
            DBTableMetadata meta = dataOutputPanel.meta;
            DB db = DBFactory.getInstance().getDatabase(meta.getDBType());
            if (dbTable.getObjectType() == SQLConstants.TARGET_TABLE) {
                meta.shutdownIfAxion();
            }
            conn = meta.createConnection();
            if (conn != null) {
                String resetFetchSizeSQL = null;
                if (meta.isDBType(DBConstants.SYBASE)) {
                    conn.setAutoCommit(false);
                    stmt = conn.createStatement();
                    stmt.execute("SET ROWCOUNT " + dataOutputPanel.recordToRefresh);
                    resetFetchSizeSQL = "SET ROWCOUNT 0";
                }

                StatementContext context = new StatementContext();
                Object limit = (dataOutputPanel.recordToRefresh == 0) ? "" : dataOutputPanel.recordToRefresh;
                context.putClientProperty("limit", limit);
                Statements stmts = db.getStatements();

                String sql = null;
                if (dbTable.getObjectType() == SQLConstants.SOURCE_TABLE) {
                    SQLPart sqlPart = stmts.getSelectStatement((SourceTable) dbTable, context);
                    sql = sqlPart.getSQL();
                } else {
                    SQLPart sqlPart = stmts.getSelectStatement((TargetTable) dbTable, context);
                    sql = sqlPart.getSQL();
                }

                List paramList = new ArrayList();
                Map attribMap = new HashMap();
                RuntimeDatabaseModel runtimeModel = dataOutputPanel.getRuntimeDbModel();
                if (runtimeModel != null) {
                    RuntimeInput inputTable = runtimeModel.getRuntimeInput();
                    if (inputTable != null) {
                        attribMap = inputTable.getRuntimeAttributeMap();
                    }
                }

                String psSql = SQLUtils.createPreparedStatement(sql, attribMap, paramList);
                PreparedStatement pstmt = conn.prepareStatement(psSql);
                SQLUtils.populatePreparedStatement(pstmt, attribMap, paramList);
                mLogger.infoNoloc(mLoc.t("EDIT175: Select statement used for show data:{0}for {1}", DataOutputPanel.NL, sql));
                ResultSet rs = pstmt.executeQuery();

                dataOutputPanel.queryView.setEditable(true);
                dataOutputPanel.queryView.setResultSet(rs, dataOutputPanel.maxRows, dataOutputPanel.nowCount - 1);

                rs.close();
                pstmt.close();

                context.putClientProperty("limit", "");
                SQLPart sqlPart = db.getStatements().getRowCountStatement(meta.getTable(), context);
                String countSql = db.getStatements().normalizeSQLForExecution(sqlPart).getSQL();
                mLogger.infoNoloc(mLoc.t("EDIT176: Select count(*) statement used for total rows:{0}for {1}", DataOutputPanel.NL, countSql));
                paramList.clear();
                psSql = SQLUtils.createPreparedStatement(countSql, attribMap, paramList);
                pstmt = conn.prepareStatement(psSql);
                SQLUtils.populatePreparedStatement(pstmt, attribMap, paramList);
                ResultSet cntRs = pstmt.executeQuery();
                dataOutputPanel.setTotalCount(cntRs);

                cntRs.close();
                pstmt.close();

                if (resetFetchSizeSQL != null) {
                    stmt.execute(resetFetchSizeSQL);
                }
            }
        } catch (Exception e) {
            this.errMsg = e.getMessage();
            mLogger.errorNoloc(mLoc.t("EDIT177: Cannot get contents for table{0}", ((dbTable != null) ? dbTable.getDisplayName() : "")), e);
            handleException();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
            }
            closeConnection(conn);
        }
    }

    private void showDataForJoinOperator() {
        ResultSet rs = null;
        Statement stmt = null;
        Connection conn = null;
        int dbType;

        try {
            SQLJoinOperator joinOperator = (SQLJoinOperator) dbTable;
            Iterator it = joinOperator.getAllSourceTables().iterator();
            DBConnectionDefinition connDef = null;
            DBConnectionDefinition tempDef = null;
            boolean isSameDB = true;
            while (it.hasNext()) {
                SourceTable tbl = (SourceTable) it.next();
                connDef = tbl.getParent().getConnectionDefinition();
                if (tempDef == null) {
                    tempDef = connDef;
                }
                if (!connDef.getConnectionURL().equals(tempDef.getConnectionURL())) {
                    isSameDB = false;
                    break;
                }
            }
            String joinSql = "";
            StringBuilder buf = null;
            if (isSameDB) {
                conn = DBExplorerUtil.createConnection(connDef.getDriverClass(), connDef.getConnectionURL(), connDef.getUserName(), connDef.getPassword());
                StatementContext context = new StatementContext();
                DB db = DBFactory.getInstance().getDatabase(SQLUtils.getSupportedDBType(connDef.getDBType()));
                context.setUseSourceTableAliasName(true);
                buf = new StringBuilder(db.getStatements().getSelectStatement(joinOperator, context).getSQL());
                joinSql = joinSql + db.getGeneratorFactory().generate(joinOperator, context);
            } else {
                try {
                    Thread.currentThread().getContextClassLoader().loadClass(AxionExternalConnectionProvider.class.getName());
                    System.setProperty(ExternalConnectionProvider.EXTERNAL_CONNECTION_PROVIDER_PROPERTY_NAME, AxionExternalConnectionProvider.class.getName());
                } catch (ClassNotFoundException e) {
                }
                conn = DBExplorerUtil.createConnection("org.axiondb.jdbc.AxionDriver", "jdbc:axiondb:joinview", "sa", "sa");
                stmt = conn.createStatement();
                it = joinOperator.getAllSourceTables().iterator();
                StatementContext joinContext = new StatementContext();
                while (it.hasNext()) {
                    SourceTable table = (SourceTable) it.next();
                    AxionDB db = (AxionDB) DBFactory.getInstance().getDatabase(DBConstants.AXION);
                    DBConnectionDefinition connDefn = table.getParent().getConnectionDefinition();
                    AxionPipelineStatements stmts = db.getAxionPipelineStatements();
                    String linkName = StringUtil.createSQLIdentifier(connDefn.getName());
                    String dbLinkSql = stmts.getCreateDBLinkStatement(connDefn, linkName).getSQL();
                    String dropDBLinkSql = stmts.getDropDBLinkStatement(linkName).getSQL();
                    try {
                        stmt.execute(dropDBLinkSql);
                    } catch (SQLException e) {
                    }
                    stmt.execute(dbLinkSql);
                    StatementContext context = new StatementContext();
                    context.setUsingUniqueTableName(table, true);
                    context.setUsingFullyQualifiedTablePrefix(false);
                    String localName = db.getUnescapedName(db.getGeneratorFactory().generate(table, context));
                    String remoteTableSql = stmts.getCreateRemoteTableStatement(table, localName, linkName).getSQL();
                    try {
                        stmt.execute(remoteTableSql);
                    } catch (SQLException e) {
                    }

                    // add table properties to statementcontext.
                    joinContext.setUsingFullyQualifiedTablePrefix(false);
                    joinContext.setUsingUniqueTableName(table, true);
                }
                DB db = DBFactory.getInstance().getDatabase(DBConstants.AXION);

                joinContext.setUsingUniqueTableName(true);
                buf = new StringBuilder(db.getStatements().getSelectStatement(joinOperator, joinContext).getSQL());
                joinSql = joinSql + db.getGeneratorFactory().generate(joinOperator, joinContext);
            }

            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString().trim());
            dataOutputPanel.queryView.setEditable(false);
            dataOutputPanel.queryView.setResultSet(rs, dataOutputPanel.recordToRefresh, 0);
            rs.close();
            stmt.close();
            try {
                // set total count
                ResultSet cntRs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM " + joinSql.trim());
                dataOutputPanel.setTotalCount(cntRs);
                cntRs.close();
            } catch (SQLException e) {
            }
        } catch (Exception ex1) {
            mLogger.errorNoloc(mLoc.t("EDIT177: Cannot get contents for table{0}", ((dbTable != null) ? dbTable.getDisplayName() : "")), ex1);
            handleException();
        } finally {
            shutdownConnection(conn);
        }
    }

    private void showDataForJoinView() {
        ResultSet rs = null;
        Statement stmt = null;
        Connection conn = null;

        try {
            SQLJoinView joinView = (SQLJoinView) dbTable;
            Iterator it = joinView.getSourceTables().iterator();
            DBConnectionDefinition connDef = null;
            DBConnectionDefinition tempDef = null;
            boolean isSameDB = true;
            while (it.hasNext()) {
                SourceTable tbl = (SourceTable) it.next();
                connDef = tbl.getParent().getConnectionDefinition();
                if (tempDef == null) {
                    tempDef = connDef;
                }
                if (!connDef.getConnectionURL().equals(tempDef.getConnectionURL())) {
                    isSameDB = false;
                    break;
                }
            }
            String joinSql = "";
            String srcFiltersStr = "";
            StringBuilder buf = null;
            if (isSameDB) {
                conn = DBExplorerUtil.createConnection(connDef.getDriverClass(), connDef.getConnectionURL(), connDef.getUserName(), connDef.getPassword());
                StatementContext context = new StatementContext();
                DB db = DBFactory.getInstance().getDatabase(SQLUtils.getSupportedDBType(connDef.getDBType()));
                context.setUseSourceTableAliasName(true);
                buf = new StringBuilder(db.getStatements().getSelectStatement(joinView, context).getSQL());
                joinSql = joinSql + db.getGeneratorFactory().generate(joinView.getRootJoin(), context);
                srcFiltersStr = getSourceWhereCondition(db, joinView.getSourceTables(), context);
            } else {
                try {
                    Thread.currentThread().getContextClassLoader().loadClass(AxionExternalConnectionProvider.class.getName());
                    System.setProperty(ExternalConnectionProvider.EXTERNAL_CONNECTION_PROVIDER_PROPERTY_NAME, AxionExternalConnectionProvider.class.getName());
                } catch (ClassNotFoundException e) {
                }
                conn = DBExplorerUtil.createConnection("org.axiondb.jdbc.AxionDriver", "jdbc:axiondb:joinview", "sa", "sa");
                stmt = conn.createStatement();
                it = joinView.getSourceTables().iterator();
                StatementContext joinContext = new StatementContext();
                while (it.hasNext()) {
                    SourceTable table = (SourceTable) it.next();
                    AxionDB db = (AxionDB) DBFactory.getInstance().getDatabase(DBConstants.AXION);
                    DBConnectionDefinition connDefn = table.getParent().getConnectionDefinition();
                    AxionPipelineStatements stmts = db.getAxionPipelineStatements();
                    String linkName = StringUtil.createSQLIdentifier(connDefn.getName());
                    String dbLinkSql = stmts.getCreateDBLinkStatement(connDefn, linkName).getSQL();
                    String dropDBLinkSql = stmts.getDropDBLinkStatement(linkName).getSQL();
                    try {
                        stmt.execute(dropDBLinkSql);
                    } catch (SQLException e) {
                    }
                    stmt.execute(dbLinkSql);
                    StatementContext context = new StatementContext();
                    context.setUsingUniqueTableName(table, true);
                    context.setUsingFullyQualifiedTablePrefix(false);
                    String localName = db.getUnescapedName(db.getGeneratorFactory().generate(table, context));
                    String remoteTableSql = stmts.getCreateRemoteTableStatement(table, localName, linkName).getSQL();
                    try {
                        stmt.execute(remoteTableSql);
                    } catch (SQLException e) {
                    }
                    joinContext.setUsingFullyQualifiedTablePrefix(false);
                    joinContext.setUsingUniqueTableName(table, true);
                }
                DB db = DBFactory.getInstance().getDatabase(DBConstants.AXION);
                joinContext.setUsingUniqueTableName(true);
                buf = new StringBuilder(db.getStatements().getSelectStatement(joinView, joinContext).getSQL());
                joinSql = joinSql + db.getGeneratorFactory().generate(joinView.getRootJoin(), joinContext);
                srcFiltersStr = getSourceWhereCondition(db, joinView.getSourceTables(), joinContext);
            }
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString().trim());
            dataOutputPanel.queryView.setEditable(false);
            dataOutputPanel.queryView.setResultSet(rs, dataOutputPanel.maxRows, dataOutputPanel.nowCount - 1);
            rs.close();
            stmt.close();
            try {
                ResultSet cntRs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM " + joinSql.trim() + srcFiltersStr.trim());
                dataOutputPanel.setTotalCount(cntRs);
                cntRs.close();
            } catch (SQLException e) {
            }
        } catch (Exception e) {
            mLogger.errorNoloc(mLoc.t("EDIT177: Cannot get contents for table{0}", ((dbTable != null) ? dbTable.getDisplayName() : "")), e);
            handleException();
        } finally {
            shutdownConnection(conn);
        }
    }
    
    private String getSourceWhereCondition(DB db, List sTables, StatementContext context) throws Exception {
        StringBuilder sourceCondition = new StringBuilder(50);
        Iterator it = sTables.iterator();
        int cnt = 0;

        while (it.hasNext()) {
            SourceTable sTable = (SourceTable) it.next();
            SQLCondition condition = sTable.getExtractionCondition();
            SQLPredicate predicate = condition.getRootPredicate();

            if (predicate != null && !"full".equalsIgnoreCase(sTable.getExtractionType())) {
                if (cnt != 0) {
                    sourceCondition.append(" AND ");
                }
                sourceCondition.append(db.getGeneratorFactory().generate(predicate, context));
                cnt++;
            }
        }

        return StringUtil.isNullString(sourceCondition.toString()) ? "" : " WHERE " + sourceCondition.toString();
    }
}
