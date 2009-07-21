/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.db.dataview.output;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.TableModel;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.dataview.meta.DBConnectionFactory;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.netbeans.modules.db.dataview.meta.DBMetaDataFactory;
import org.netbeans.modules.db.dataview.meta.DBTable;
import org.netbeans.modules.db.dataview.util.DBReadWriteHelper;
import org.netbeans.modules.db.dataview.util.DataViewUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * This class assumes there will be only one connection which can't be closed.
 *
 * @author Ahimanikya Satapathy
 */
class SQLExecutionHelper {

    private final DataView dataView;
    private static Logger mLogger = Logger.getLogger(SQLExecutionHelper.class.getName());
    // the RequestProcessor used for executing statements.
    private final RequestProcessor rp = new RequestProcessor("SQLStatementExecution", 20, true); // NOI18N
    private static final String LIMIT_CLAUSE = " LIMIT "; // NOI18N
    public static final String OFFSET_CLAUSE = " OFFSET "; // NOI18N
    private static Logger LOGGER = Logger.getLogger(SQLExecutionHelper.class.getName());

    SQLExecutionHelper(DataView dataView) {
        this.dataView = dataView;
    }

    static void initialDataLoad(DataView dv, DatabaseConnection dbConn, SQLExecutionHelper execHelper) throws SQLException {
        Statement stmt = null;
        try {
            Connection conn = DBConnectionFactory.getInstance().getConnection(dbConn);
            String msg = "";
            if (conn == null) {
                Throwable ex = DBConnectionFactory.getInstance().getLastException();
                if (ex != null) {
                    msg = ex.getMessage();
                } else {
                    msg = NbBundle.getMessage(SQLExecutionHelper.class, "MSG_connection_failure", dv.getDatabaseConnection());
                }
                NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(nd);
                return;
            }

            DBMetaDataFactory dbMeta = new DBMetaDataFactory(conn);
            dv.setLimitSupported(dbMeta.supportsLimit());
            String sql = dv.getSQLString();
            boolean isSelect = execHelper.isSelectStatement(sql);

            stmt = execHelper.prepareSQLStatement(conn, sql);
            execHelper.executeSQLStatement(stmt, sql);

            if (dv.getUpdateCount() != -1) {
                if (!conn.getAutoCommit()) {
                    conn.commit();
                }
                return;
            }

            ResultSet resultSet = null;
            try {
                resultSet = stmt.getResultSet();
                if (resultSet == null) {
                    if (!conn.getAutoCommit()) {
                        conn.commit();
                    }
                    return;
                }
                Collection<DBTable> tables = dbMeta.generateDBTables(resultSet, sql, isSelect);
                DataViewDBTable dvTable = new DataViewDBTable(tables);
                dv.setDataViewDBTable(dvTable);
                execHelper.loadDataFrom(resultSet);
            } finally {
                DataViewUtils.closeResources(resultSet);
            }
            execHelper.getTotalCount(isSelect, sql, stmt);
        } finally {
            DataViewUtils.closeResources(stmt);
        }
    }

    RequestProcessor.Task executeInsertRow(final String insertSQL, final Object[] insertedRow) {
        String title = NbBundle.getMessage(SQLExecutionHelper.class, "LBL_sql_insert");
        SQLStatementExecutor executor = new SQLStatementExecutor(dataView, title, "") {

            @Override
            public void execute() throws SQLException, DBException {
                dataView.setEditable(false);
                PreparedStatement pstmt = conn.prepareStatement(insertSQL);
                try {
                    int pos = 1;
                    for (int i = 0; i < insertedRow.length; i++) {
                        Object val = insertedRow[i];

                        // Check for Constant e.g <NULL>, <DEFAULT>, <CURRENT_TIMESTAMP> etc
                        if (DataViewUtils.isSQLConstantString(val)) {
                            continue;
                        }

                        // literals
                        int colType = dataView.getDataViewDBTable().getColumnType(i);
                        DBReadWriteHelper.setAttributeValue(pstmt, pos++, colType, val);
                    }

                    executePreparedStatement(pstmt);
                    int rows = dataView.getUpdateCount();
                    if (rows != 1) {
                        error = true;
                        errorMsg = NbBundle.getMessage(SQLExecutionHelper.class, "MSG_failure_insert_rows");
                    }
                } finally {
                    DataViewUtils.closeResources(pstmt);
                }
            }

            @Override
            public void finished() {
                dataView.setEditable(true);
                commitOrRollback(NbBundle.getMessage(SQLExecutionHelper.class, "LBL_insert_command"));
            }

            @Override
            protected void executeOnSucess() {
                if (dataView.getDataViewPageContext().getTotalRows() < 0) {
                    dataView.getDataViewPageContext().setTotalRows(0);
                    dataView.getDataViewPageContext().first();
                }
                dataView.incrementRowSize(1);

                // refresh when required
                if (dataView.getDataViewPageContext().refreshRequiredOnInsert()) {
                    SQLExecutionHelper.this.executeQuery();
                } else {
                    reinstateToolbar();
                }
            }
        };
        RequestProcessor.Task task = rp.create(executor);
        executor.setTask(task);
        task.schedule(0);
        return task;
    }

    void executeDeleteRow(final DataViewTableUI rsTable) {
        String title = NbBundle.getMessage(SQLExecutionHelper.class, "LBL_sql_delete");
        final int[] rows = rsTable.getSelectedRows();
        SQLStatementExecutor executor = new SQLStatementExecutor(dataView, title, "") {

            @Override
            public void execute() throws SQLException, DBException {
                dataView.setEditable(false);

                for (int j = 0; j < rows.length && !error; j++) {
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    deleteARow(rows[j], rsTable.getModel());
                }
            }

            private void deleteARow(int rowNum, TableModel tblModel) throws SQLException, DBException {
                final List<Object> values = new ArrayList<Object>();
                final List<Integer> types = new ArrayList<Integer>();

                SQLStatementGenerator generator = dataView.getSQLStatementGenerator();
                final String deleteStmt = generator.generateDeleteStatement(types, values, rowNum, tblModel);
                PreparedStatement pstmt = conn.prepareStatement(deleteStmt);
                try {
                    int pos = 1;
                    for (Object val : values) {
                        DBReadWriteHelper.setAttributeValue(pstmt, pos, types.get(pos - 1), val);
                        pos++;
                    }

                    executePreparedStatement(pstmt);
                    int rows = dataView.getUpdateCount();
                    if (rows == 0) {
                        error = true;
                        errorMsg = errorMsg + NbBundle.getMessage(SQLExecutionHelper.class, "MSG_no_match_to_delete");
                    } else if (rows > 1) {
                        error = true;
                        errorMsg = errorMsg + NbBundle.getMessage(SQLExecutionHelper.class, "MSG_no_unique_row_for_match");
                    }
                } finally {
                    DataViewUtils.closeResources(pstmt);
                }
            }

            @Override
            public void finished() {
                dataView.setEditable(true);
                commitOrRollback(NbBundle.getMessage(SQLExecutionHelper.class, "LBL_delete_command"));
            }

            @Override
            protected void executeOnSucess() {
                dataView.decrementRowSize(rows.length);
                SQLExecutionHelper.this.executeQuery();
            }
        };

        RequestProcessor.Task task = rp.create(executor);
        executor.setTask(task);
        task.schedule(0);
    }

    void executeUpdateRow(final DataViewTableUI rsTable, final boolean selectedOnly) {
        String title = NbBundle.getMessage(SQLExecutionHelper.class, "LBL_sql_update");
        SQLStatementExecutor executor = new SQLStatementExecutor(dataView, title, "") {

            private PreparedStatement pstmt;
            Set<Integer> keysToRemove = new HashSet<Integer>();

            @Override
            public void execute() throws SQLException, DBException {
                dataView.setEditable(false);
                if (selectedOnly) {
                    updateSelected();
                } else {
                    for (Integer key : dataView.getUpdatedRowContext().getUpdateKeys()) {
                        if (Thread.currentThread().isInterrupted()) {
                            break;
                        } else {
                            updateARow(key);
                            keysToRemove.add(key);
                        }
                    }
                }
            }

            private void updateSelected() throws SQLException, DBException {
                int[] rows = rsTable.getSelectedRows();
                UpdatedRowContext tblContext = dataView.getUpdatedRowContext();
                for (int j = 0; j < rows.length && !error; j++) {
                    Set<Integer> keys = tblContext.getUpdateKeys();
                    for (Integer key : keys) {
                        if (Thread.currentThread().isInterrupted()) {
                            break;
                        } else if (key == rows[j]) {
                            updateARow(key);
                            keysToRemove.add(key);
                        }
                    }
                }
            }

            private void updateARow(Integer key) throws SQLException, DBException {
                UpdatedRowContext updatedRowCtx = dataView.getUpdatedRowContext();
                SQLStatementGenerator generator = dataView.getSQLStatementGenerator();

                List<Object> values = new ArrayList<Object>();
                List<Integer> types = new ArrayList<Integer>();
                String updateStmt = generator.generateUpdateStatement(key, updatedRowCtx.getChangedData(key), values, types, rsTable.getModel());

                pstmt = conn.prepareStatement(updateStmt);
                int pos = 1;
                for (Object val : values) {
                    // Check for Constant e.g <NULL>, <DEFAULT>, <CURRENT_TIMESTAMP> etc
                    if (DataViewUtils.isSQLConstantString(val)) {
                        continue;
                    }

                    DBReadWriteHelper.setAttributeValue(pstmt, pos, types.get(pos - 1), val);
                    pos++;
                }

                try {
                    executePreparedStatement(pstmt);
                    int rows = dataView.getUpdateCount();
                    if (rows == 0) {
                        error = true;
                        errorMsg = errorMsg + NbBundle.getMessage(SQLExecutionHelper.class, "MSG_no_match_to_update");
                    } else if (rows > 1) {
                        error = true;
                        errorMsg = errorMsg + NbBundle.getMessage(SQLExecutionHelper.class, "MSG_no_unique_row_for_match");
                    }
                } finally {
                    DataViewUtils.closeResources(pstmt);
                }
            }

            @Override
            public void finished() {
                dataView.setEditable(true);
                commitOrRollback(NbBundle.getMessage(SQLExecutionHelper.class, "LBL_update_command"));
            }

            @Override
            protected void executeOnSucess() {
                UpdatedRowContext tblContext = dataView.getUpdatedRowContext();
                for (Integer key : keysToRemove) {
                    tblContext.removeUpdateForSelectedRow(key);
                }
                dataView.syncPageWithTableModel();
                reinstateToolbar();
            }
        };
        RequestProcessor.Task task = rp.create(executor);
        executor.setTask(task);
        task.schedule(0);
    }

    // Truncate is allowed only when there is single table used in the query.
    void executeTruncate() {
        String msg = NbBundle.getMessage(SQLExecutionHelper.class, "MSG_truncate_table_progress");
        String title = NbBundle.getMessage(SQLExecutionHelper.class, "LBL_sql_truncate");
        SQLStatementExecutor executor = new SQLStatementExecutor(dataView, title, msg) {

            private PreparedStatement stmt = null;

            @Override
            public void execute() throws SQLException, DBException {


                DBTable dbTable = dataView.getDataViewDBTable().geTable(0);
                String truncateSql = "TRUNCATE TABLE " + dbTable.getFullyQualifiedName(true); // NOI18N

                try {
                    stmt = conn.prepareStatement(truncateSql);
                    executePreparedStatement(stmt);
                } catch (SQLException sqe) {
                    mLogger.log(Level.FINE, "TRUNCATE Not supported...will try DELETE * \n"); // NOI18N
                    truncateSql = "DELETE FROM " + dbTable.getFullyQualifiedName(true); // NOI18N
                    stmt = conn.prepareStatement(truncateSql);
                    executePreparedStatement(stmt);
                } finally {
                    DataViewUtils.closeResources(stmt);
                }
            }

            @Override
            public void finished() {
                commitOrRollback(NbBundle.getMessage(SQLExecutionHelper.class, "LBL_truncate_command"));
            }

            @Override
            protected void executeOnSucess() {
                dataView.getDataViewPageContext().setTotalRows(0);
                dataView.getDataViewPageContext().first();
                SQLExecutionHelper.this.executeQuery();
            }
        };

        RequestProcessor.Task task = rp.create(executor);
        executor.setTask(task);
        task.schedule(0);
    }

    // Once Data View is created the it assumes the query never changes.
    void executeQuery() {
        String title = NbBundle.getMessage(SQLExecutionHelper.class, "LBL_sql_executequery");
        SQLStatementExecutor executor = new SQLStatementExecutor(dataView, title, dataView.getSQLString()) {

            private ResultSet rs = null;
            private Statement stmt = null;
            boolean lastEditState = dataView.isEditable();

            // Execute the Select statement
            public void execute() throws SQLException, DBException {
                dataView.setEditable(false);
                String sql = dataView.getSQLString();
                stmt = prepareSQLStatement(conn, sql);

                // Execute the query
                try {
                    executeSQLStatement(stmt, sql);
                    if (dataView.hasResultSet()) {
                        rs = stmt.getResultSet();
                        loadDataFrom(rs);
                    } else {
                        return;
                    }
                } catch (SQLException sqlEx) {
                    String title = NbBundle.getMessage(SQLExecutionHelper.class, "MSG_error");
                    String msg = NbBundle.getMessage(SQLExecutionHelper.class, "Confirm_Close");
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(sqlEx.getMessage() + "\n" + msg, title,
                            NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                    if (nd.getValue().equals(NotifyDescriptor.YES_OPTION)) {
                        dataView.removeComponents();
                    }
                    throw sqlEx;

                } finally {
                    DataViewUtils.closeResources(rs);
                }

                // Get total row count
                if (dataView.getDataViewPageContext().getTotalRows() == -1) {
                    getTotalCount(isSelectStatement(sql), sql, stmt);
                }
            }

            public void finished() {
                DataViewUtils.closeResources(stmt);
                dataView.setEditable(lastEditState);
                synchronized (dataView) {
                    if (error) {
                        dataView.setErrorStatusText(ex);
                    }
                    dataView.getUpdatedRowContext().removeAllUpdates();
                    dataView.resetToolbar(error);
                    dataView.setRowsInTableModel();
                }
            }
        };
        RequestProcessor.Task task = rp.create(executor);
        executor.setTask(task);
        task.schedule(0);
    }

    void loadDataFrom(ResultSet rs) throws SQLException {
        if (rs == null) {
            return;
        }

        int pageSize = dataView.getDataViewPageContext().getPageSize();
        int startFrom = 0;
        if (!dataView.isLimitSupported() || isLimitUsedInSelect(dataView.getSQLString())) {
            startFrom = dataView.getDataViewPageContext().getCurrentPos() - 1;
        }

        DataViewDBTable tblMeta = dataView.getDataViewDBTable();
        List<Object[]> rows = new ArrayList<Object[]>();
        int colCnt = tblMeta.getColumnCount();
        try {
            // Skip till current position
            boolean lastRowPicked = rs.next();
            int curRowPos = 1;
            while (lastRowPicked && curRowPos < (startFrom + 1)) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                lastRowPicked = rs.next();
                curRowPos++;
            }

            // Get next page
            int rowCnt = 0;
            while (((pageSize == -1) || (pageSize > rowCnt)) && (lastRowPicked || rs.next())) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }

                Object[] row = new Object[colCnt];
                for (int i = 0; i < colCnt; i++) {
                    row[i] = DBReadWriteHelper.readResultSet(rs, tblMeta.getColumn(i), i + 1);
                }
                rows.add(row);
                rowCnt++;
                if (lastRowPicked) {
                    lastRowPicked = false;
                }
            }
        } catch (SQLException e) {
            mLogger.log(Level.SEVERE, "Failed to set up table model" + e); // NOI18N
            throw e;
        } finally {
            dataView.getDataViewPageContext().setCurrentRows(rows);
        }
    }

    void setTotalCount(ResultSet countresultSet) {
        try {
            if (countresultSet == null) {
                dataView.getDataViewPageContext().setTotalRows(-1);
            } else {
                if (countresultSet.next()) {
                    int count = countresultSet.getInt(1);
                    dataView.getDataViewPageContext().setTotalRows(count);
                }
            }
        } catch (SQLException ex) {
            mLogger.log(Level.SEVERE, "Could not get total row count " + ex); // NOI18N
        }
    }

    private String appendLimitIfRequired(String sql) {
        if (dataView.isLimitSupported() && isSelectStatement(sql)) {
            if (!isLimitUsedInSelect(sql)) {
                sql += LIMIT_CLAUSE + dataView.getDataViewPageContext().getPageSize();
                sql += OFFSET_CLAUSE + (dataView.getDataViewPageContext().getCurrentPos() - 1);
            }
        }

        return sql;
    }

    private Statement prepareSQLStatement(Connection conn, String sql) throws SQLException {
        Statement stmt = null;
        if (sql.startsWith("{")) { // NOI18N

            stmt = conn.prepareCall(sql);
        } else if (isSelectStatement(sql)) {
            stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            int pageSize = dataView.getDataViewPageContext().getPageSize();

            try {
                stmt.setFetchSize(pageSize);
            } catch (SQLException e) {
                // ignore -  used only as a hint to the driver to optimize
                LOGGER.log(Level.WARNING, "Unable to set Fetch size" + e); // NOI18N
            }

            try {
                if (dataView.isLimitSupported() && sql.toUpperCase().indexOf(LIMIT_CLAUSE) == -1) {
                    stmt.setMaxRows(pageSize);
                } else {
                    stmt.setMaxRows(dataView.getDataViewPageContext().getCurrentPos() + pageSize);
                }
            } catch (SQLException exc) {
                mLogger.log(Level.WARNING, "Unable to set Max row size" + exc); // NOI18N
            }
        } else {
            stmt = conn.createStatement();
        }
        return stmt;
    }

    private void executeSQLStatement(Statement stmt, String sql) throws SQLException {
        mLogger.log(Level.FINE, "Statement: " + sql); // NOI18N
        dataView.setInfoStatusText(NbBundle.getMessage(SQLExecutionHelper.class, "LBL_sql_executestmt") + sql);

        long startTime = System.currentTimeMillis();
        boolean isResultSet = false;
        if (stmt instanceof PreparedStatement) {
            isResultSet = ((PreparedStatement) stmt).execute();
        } else {
            try {
                isResultSet = stmt.execute(appendLimitIfRequired(sql));
            } catch (SQLException sqlExc) {
                if (sqlExc.getErrorCode() == 1064 && sqlExc.getSQLState().equals("37000")) {
                    isResultSet = stmt.execute(sql);
                } else {
                    mLogger.log(Level.SEVERE, "Failed to execute SQL Statement" + sqlExc);
                    throw sqlExc;
                }
            }
        }

        long executionTime = System.currentTimeMillis() - startTime;
        synchronized (dataView) {
            dataView.setHasResultSet(isResultSet);
            dataView.setUpdateCount(stmt.getUpdateCount());
            dataView.setExecutionTime(executionTime);
        }
    }

    private void executePreparedStatement(PreparedStatement stmt) throws SQLException {
        long startTime = System.currentTimeMillis();
        boolean isResultSet = stmt.execute();

        long executionTime = System.currentTimeMillis() - startTime;
        String execTimeStr = SQLExecutionHelper.millisecondsToSeconds(executionTime);
        dataView.setInfoStatusText(NbBundle.getMessage(SQLExecutionHelper.class, "MSG_execution_success", execTimeStr));

        synchronized (dataView) {
            dataView.setHasResultSet(isResultSet);
            dataView.setUpdateCount(stmt.getUpdateCount());
            dataView.setExecutionTime(executionTime);
        }
    }

    private void getTotalCount(boolean isSelect, String sql, Statement stmt) {

        // Case for LIMIT n OFFSET m
        if (isSelect && isLimitUsedInSelect(sql)) {
            try {
                String lmtStr = sql.toUpperCase().split(LIMIT_CLAUSE)[1].trim();
                int rCnt = Integer.parseInt(lmtStr.split(" ")[0]);
                dataView.getDataViewPageContext().setTotalRows(rCnt);
                return;
            } catch (NumberFormatException nex) {
            }
        }

        // SELECT COUNT(*) FROM (sqlquery) alias
        ResultSet cntResultSet = null;
        if (isSelect) {
            try {
                cntResultSet = stmt.executeQuery(SQLStatementGenerator.getCountAsSubQuery(sql));
                setTotalCount(cntResultSet);
                return;
            } catch (SQLException e) {
            } finally {
                DataViewUtils.closeResources(cntResultSet);
            }
        }

        // Try spliting the query by FROM and use "SELECT COUNT(*) FROM"  + "2nd part sql"
        if (isSelect && !isGroupByUsedInSelect(sql)) {
            cntResultSet = null;
            try {
                cntResultSet = stmt.executeQuery(SQLStatementGenerator.getCountSQLQuery(sql));
                setTotalCount(cntResultSet);
                return;
            } catch (SQLException e) {
            } finally {
                DataViewUtils.closeResources(cntResultSet);
            }
        }

        // In worse case, get the count from resultset
        cntResultSet = null;
        int totalRows = 0;
        try {
            // reset fetch size
            int fetchSize = dataView.getDataViewPageContext().getPageSize();
            try {
                fetchSize = stmt.getFetchSize();
                stmt.setFetchSize(20000);
            } catch (SQLException sqe) {
                // ignore
            }

            cntResultSet = stmt.executeQuery(sql);
            while (cntResultSet.next()) {
                totalRows++;
            }
            dataView.getDataViewPageContext().setTotalRows(totalRows);

            // set to old value
            try {
                stmt.setFetchSize(fetchSize);
            } catch (SQLException sqe) {
                // ignore
            }
            return;
        } catch (SQLException e) {
        } finally {
            DataViewUtils.closeResources(cntResultSet);
        }

        // Unable to compute the total rows
        setTotalCount(null);

    }

    private boolean isSelectStatement(String queryString) {
        return queryString.trim().toUpperCase().startsWith("SELECT") && queryString.trim().toUpperCase().indexOf("INTO") == -1; // NOI18N
    }

    private boolean isLimitUsedInSelect(String sql) {
        return sql.toUpperCase().indexOf(LIMIT_CLAUSE) != -1;
    }

    private boolean isGroupByUsedInSelect(String sql) {
        return sql.toUpperCase().indexOf(" GROUP BY ") != -1 || sql.toUpperCase().indexOf(" COUNT(*) ") != -1; // NOI18N
    }

    static String millisecondsToSeconds(long ms) {
        NumberFormat fmt = NumberFormat.getInstance();
        fmt.setMaximumFractionDigits(3);
        return fmt.format(ms / 1000.0);
    }
}
