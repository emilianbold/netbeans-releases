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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.modules.db.dataview.meta.DBConnectionFactory;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.netbeans.modules.db.dataview.util.DBReadWriteHelper;
import org.netbeans.modules.db.dataview.util.DataViewUtils;

/**
 *
 * @author Ahimanikya Satapathy
 */
class SQLExecutionHelper {

    private static Logger mLogger = Logger.getLogger(SQLExecutionHelper.class.getName());
    private DataViewOutputPanel parent; 
    SQLExecutionHelper(DataViewOutputPanel parent){
        this.parent = parent;
    }
    
    boolean executeInsert(String[] insertSQL, Object[] insertedRow, DataViewOutputPanel dvParent) {
        PreparedStatement pstmt = null;
        Connection conn = null;
        boolean error = false;
        boolean autoCommit = true;
        String errorMsg = "";

        try {
            conn = DBConnectionFactory.getInstance().getConnection(dvParent.dbConn);
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(insertSQL[0]);
            int pos = 1;
            for (int i = 0; i < insertedRow.length; i++) {
                if (insertedRow[i] != null) {
                    DBReadWriteHelper.setAttributeValue(pstmt, pos++, dvParent.getDBTableWrapper().getColumnType(i), insertedRow[i]);
                }
            }
            int rows = pstmt.executeUpdate();

            if (rows != 1) {
                error = true;
            }
        } catch (Exception ex) {
            error = true;
            errorMsg = DBException.getMessage(ex);
        } finally {
            if (!error) {
                try {
                    conn.commit();
                } catch (SQLException ex) {
                    errorMsg = "Failure while commiting changes to database.\n";
                    errorMsg = errorMsg + DBException.getMessage(ex);
                    dvParent.printerrToOutputTab(errorMsg);
                }

                if (!error) {
                    errorMsg = "Record successfully inserted.\n";
                    dvParent.printinfoToOutputTab(errorMsg);
                }
            } else {
                dvParent.printerrToOutputTab("Insert command failed.\n");
                dvParent.printerrToOutputTab(errorMsg);
                dvParent.printinfoToOutputTab("\nUsing SQL:" + insertSQL[1]);
                rollback(conn);
            }

            DataViewUtils.closeResources(pstmt);
            if (dvParent.getResultSetPage().getTotalRows() <= 0) {
                dvParent.getResultSetPage().setTotalRows(1);
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(autoCommit);
                } catch (SQLException ex) {
                    //ignore
                }
            }
            return !error;
        }
    }

    int executeDeleteRow(int rowNum) {
        List<Object> values = new ArrayList<Object>();
        List<Integer> types = new ArrayList<Integer>();
        ResultSetUpdatedRowContext tblContext = parent.getResultSetUpdatedRowContext();

        SQLStatementGenerator generator = new SQLStatementGenerator(parent.getDBTableWrapper(), parent.getResulSetTable());
        String[] deleteStmt = generator.generateDeleteStatement(types, values, rowNum);
        String rawDeleteStmt = deleteStmt[1];
        mLogger.info("Statement: " + rawDeleteStmt);
        parent.printinfoToOutputTab("Statement: " + rawDeleteStmt);
        PreparedStatement pstmt = null;
        Connection conn = null;
        boolean error = false;
        String errorMsg = "";

        try {
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

            return rows;
        } catch (Exception ex) {
            error = true;
            errorMsg = errorMsg + DBException.getMessage(ex);
        } finally {
            DataViewUtils.closeResources(pstmt);
            if (!error) {
                try {
                    conn.commit();
                } catch (SQLException ex) {
                    parent.printerrToOutputTab("Commit Failed\n");
                    parent.printerrToOutputTab("Using SQL:" + rawDeleteStmt + "\n");
                    parent.printerrToOutputTab(DBException.getMessage(ex));
                }
            } else {
                parent.printerrToOutputTab("Delete command failed\n" + errorMsg);
                parent.printerrToOutputTab("Using SQL:" + rawDeleteStmt + "\n");
                parent.printerrToOutputTab(errorMsg);
            }
        }
        return 0;
    }

    protected void executeUpdate(String key) throws NumberFormatException, SQLException, DBException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String errorMsg = "";

        ResultSetUpdatedRowContext tblContext = parent.getResultSetUpdatedRowContext();
        int row = Integer.parseInt(key.substring(0, key.indexOf(";")));
        int col = Integer.parseInt(key.substring(key.indexOf(";") + 1, key.length()));
        String updateStmt = tblContext.getUpdateStmt(key);
        String rawUpdateStmt = tblContext.getRawUpdateStmt((key));
        List<Object> values = tblContext.getValueList(key);
        List<Integer> types = tblContext.getTypeList(key);

        int rowCount = 0;
        try {
            conn = DBConnectionFactory.getInstance().getConnection(parent.dbConn);
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(updateStmt);
            int pos = 1;
            for (Object val : values) {
                DBReadWriteHelper.setAttributeValue(pstmt, pos, types.get(pos - 1), val);
                pos++;
            }

            mLogger.info(rawUpdateStmt);
            parent.printinfoToOutputTab("Statement: " + rawUpdateStmt);
            rowCount = pstmt.executeUpdate();
        } catch (SQLException ex) {
            errorMsg = "Update failed at Row:" + row + ", Column:" + col;
            errorMsg += "\nErrorCode:" + ex.getErrorCode() + ", " + ex.getMessage();
        } finally {
            if (rowCount == 0) {
                if (DataViewUtils.isNullString(errorMsg)) {
                    errorMsg = "No rows updated using " + rawUpdateStmt;
                }
                parent.printerrToOutputTab(errorMsg);
                rollback(conn);
            } else if (rowCount > 1) {
                errorMsg = "Unable to find unique row using " + rawUpdateStmt;
                parent.printerrToOutputTab(errorMsg);
                rollback(conn);
            } else {
                conn.commit();
            }
            DataViewUtils.closeResources(pstmt);
        }
    }
    
    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ex) {
            String errorMsg = "Fail to rollback.\n";
            parent.printerrToOutputTab(errorMsg + DBException.getMessage(ex));
        }
    }
}
