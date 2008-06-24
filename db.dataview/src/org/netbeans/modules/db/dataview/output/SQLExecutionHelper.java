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
package org.netbeans.modules.db.dataview.output;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.table.TableModel;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.dataview.meta.DBConnectionFactory;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.netbeans.modules.db.dataview.meta.DBTable;
import org.netbeans.modules.db.dataview.util.DBReadWriteHelper;
import org.netbeans.modules.db.dataview.util.DataViewUtils;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Ahimanikya Satapathy
 */
class SQLExecutionHelper {

    private static Logger mLogger = Logger.getLogger(SQLExecutionHelper.class.getName());
    private final DataView parent;
    private final DatabaseConnection dbConn;
    // the RequestProcessor used for executing statements.
    private final RequestProcessor rp = new RequestProcessor("SQLStatementExecution", 1, true); // NOI18N


    SQLExecutionHelper(DataView parent, DatabaseConnection dbConn) {
        this.parent = parent;
        this.dbConn = dbConn;
    }

    void executeInsert(final String[] insertSQL, final Object[] insertedRow) {

        SQLStatementExecutor executor = new SQLStatementExecutor(parent, "Executing Insert", "") {

            private PreparedStatement pstmt = null;
            private Connection conn = null;
            private boolean error = false;
            private boolean autoCommit = true;
            private String errorMsg = "";

            @Override
            public void finished() {
                if (!error && ex == null) {
                    try {
                        conn.commit();
                    } catch (SQLException e) {
                        errorMsg = "Failure while commiting changes to database.\n";
                        errorMsg = errorMsg + DBException.getMessage(e);
                        parent.setErrorStatusText(errorMsg);
                        error = true;
                    }

                    if (!error) {
                        errorMsg = "Record successfully inserted.\n";
                        parent.setInfoStatusText(errorMsg);
                    }
                } else {
                    parent.setErrorStatusText("Insert command failed.\n");
                    parent.setErrorStatusText(errorMsg);
                    parent.setInfoStatusText("\nUsing SQL:" + insertSQL[1]);
                    rollback(conn);
                }

                DataViewUtils.closeResources(pstmt);
                if (parent.getDataViewPageContext().getTotalRows() <= 0) {
                    parent.getDataViewPageContext().setTotalRows(1);
                }
                if (conn != null) {
                    try {
                        conn.setAutoCommit(autoCommit);
                    } catch (SQLException e) {
                        //ignore
                    }
                }
                parent.executeQuery();
            }

            @Override
            public void execute() throws SQLException, DBException {
                conn = DBConnectionFactory.getInstance().getConnection(parent.dbConn);
                autoCommit = conn.getAutoCommit();
                conn.setAutoCommit(false);
                pstmt = conn.prepareStatement(insertSQL[0]);
                int pos = 1;
                for (int i = 0; i < insertedRow.length; i++) {
                    if (insertedRow[i] != null) {
                        DBReadWriteHelper.setAttributeValue(pstmt, pos++, parent.getDataViewDBTable().getColumnType(i), insertedRow[i]);
                    }
                }
                int rows = pstmt.executeUpdate();

                if (rows != 1) {
                    error = true;
                }
            }
        };
        RequestProcessor.Task task = rp.create(executor);
        executor.setTask(task);
        task.schedule(0);

    }

    void executeDeleteRow(int rowNum, TableModel tblModel) {
        final List<Object> values = new ArrayList<Object>();
        final List<Integer> types = new ArrayList<Integer>();

        SQLStatementGenerator generator = parent.getSQLStatementGenerator();
        final String[] deleteStmt = generator.generateDeleteStatement(types, values, rowNum, tblModel);
        final String rawDeleteStmt = deleteStmt[1];
        mLogger.info("Statement: " + rawDeleteStmt);
        parent.setInfoStatusText("Statement: " + rawDeleteStmt);

        SQLStatementExecutor executor = new SQLStatementExecutor(parent, "Executing Delete", "") {

            private PreparedStatement pstmt = null;
            private Connection conn = null;
            private boolean error = false;
            private String errorMsg = "";

            @Override
            public void execute() throws SQLException, DBException {

                conn = DBConnectionFactory.getInstance().getConnection(parent.dbConn);
                conn.setAutoCommit(false);
                pstmt = conn.prepareStatement(deleteStmt[0]);
                int pos = 1;
                for (Object val : values) {
                    DBReadWriteHelper.setAttributeValue(pstmt, pos, types.get(pos - 1), val);
                    pos++;
                }
                int rows = pstmt.executeUpdate();
                if (rows == 0) {
                    error = true;
                    errorMsg = errorMsg + " No rows deleted.\n";
                } else if (rows > 1) {
                    error = true;
                    errorMsg = errorMsg + "No unique row.\n";
                }
            }

            @Override
            public void finished() {
                DataViewUtils.closeResources(pstmt);
                if (!error) {
                    try {
                        conn.commit();
                    } catch (SQLException e) {
                        parent.setErrorStatusText("Commit Failed\n");
                        parent.setErrorStatusText("Using SQL:" + rawDeleteStmt + "\n");
                        parent.setErrorStatusText(DBException.getMessage(e));
                    }
                } else {
                    parent.setErrorStatusText("Delete command failed\n" + errorMsg);
                    parent.setErrorStatusText("Using SQL:" + rawDeleteStmt + "\n");
                    parent.setErrorStatusText(errorMsg);
                }
            }
        };

        RequestProcessor.Task task = rp.create(executor);
        executor.setTask(task);
        task.schedule(0);
    }

    void executeUpdate(String key) throws NumberFormatException, SQLException, DBException {


        UpdatedRowContext tblContext = parent.getResultSetUpdatedRowContext();
        final int row = Integer.parseInt(key.substring(0, key.indexOf(";")));
        final int col = Integer.parseInt(key.substring(key.indexOf(";") + 1, key.length()));
        final String updateStmt = tblContext.getUpdateStmt(key);
        final String rawUpdateStmt = tblContext.getRawUpdateStmt((key));
        final List<Object> values = tblContext.getValueList(key);
        final List<Integer> types = tblContext.getTypeList(key);



        SQLStatementExecutor executor = new SQLStatementExecutor(parent, "Executing Update", "") {

            private Connection conn;
            private PreparedStatement pstmt;
            private String errorMsg = "";
            private int rowCount = 0;

            @Override
            public void finished() {
                if (ex != null) {
                    errorMsg = "Update failed at Row:" + row + ", Column:" + col;
                    errorMsg += ex.getMessage();
                }

                if (rowCount == 0) {
                    if (DataViewUtils.isNullString(errorMsg)) {
                        errorMsg = "No rows updated using " + rawUpdateStmt;
                    }
                    parent.setErrorStatusText(errorMsg);
                    rollback(conn);
                } else if (rowCount > 1) {
                    errorMsg = "Unable to find unique row using " + rawUpdateStmt;
                    parent.setErrorStatusText(errorMsg);
                    rollback(conn);
                } else {
                    try {
                        conn.commit();
                    } catch (SQLException e) {
                        errorMsg = "Failed to commit " + e.getMessage();
                        parent.setErrorStatusText(errorMsg);
                    }
                }
                DataViewUtils.closeResources(pstmt);
            }

            @Override
            public void execute() throws SQLException, DBException {
                conn = DBConnectionFactory.getInstance().getConnection(parent.dbConn);
                conn.setAutoCommit(false);
                pstmt = conn.prepareStatement(updateStmt);
                int pos = 1;
                for (Object val : values) {
                    DBReadWriteHelper.setAttributeValue(pstmt, pos, types.get(pos - 1), val);
                    pos++;
                }

                mLogger.info(rawUpdateStmt);
                parent.setInfoStatusText("Statement: " + rawUpdateStmt);
                rowCount = pstmt.executeUpdate();
            }
        };
    }

    // Truncate is allowed only when there is single table used in the query.
    void truncateDBTable() throws DBException {
        Connection conn = null;
        Statement stmt = null;
        ResultSet cntRs = null;
        String truncateSql = "";

        try {
            conn = DBConnectionFactory.getInstance().getConnection(this.dbConn);
            conn.setAutoCommit(true);
            stmt = conn.createStatement();

            DBTable dbTable = parent.getDataViewDBTable().geTable(0);
            truncateSql = "Truncate table " + dbTable.getFullyQualifiedName();
            try {
                mLogger.info("Trncating Table Using: " + truncateSql);
                stmt.executeUpdate(truncateSql);

            } catch (SQLException sqe) {
                truncateSql = "Delete from " + dbTable.getFullyQualifiedName();
                mLogger.info("Trncating Table Using: " + truncateSql);
                stmt.executeUpdate(truncateSql);
            }

            if (!conn.getAutoCommit()) {
                conn.commit();
            }

            //set the total count
            stmt = conn.createStatement();
            String countSql = "Select Count(*) from " + dbTable.getFullyQualifiedName();
            mLogger.info(countSql);
            cntRs = stmt.executeQuery(countSql);
            parent.setTotalCount(cntRs);
        } catch (Exception t) {
            mLogger.info("Could not truncate data using " + truncateSql);
            throw new DBException("Could not truncate data using " + truncateSql, t);
        } finally {
            DataViewUtils.closeResources(stmt);
            DataViewUtils.closeResources(cntRs);
        }
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ex) {
            String errorMsg = "Fail to rollback.\n";
            parent.setErrorStatusText(errorMsg + DBException.getMessage(ex));
        }
    }
}
