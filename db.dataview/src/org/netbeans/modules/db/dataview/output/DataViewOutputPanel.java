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

import org.netbeans.modules.db.dataview.util.DBReadWriteHelper;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import org.netbeans.modules.db.dataview.meta.DBException;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.dataview.meta.DBConnectionFactory;
import org.netbeans.modules.db.dataview.meta.DBMetaDataFactory;
import org.netbeans.modules.db.dataview.meta.DBTable;
import org.netbeans.modules.db.dataview.util.DataViewUtils;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.WindowManager;

/**
 * TopComponent hosting display of design-level SQL test output.
 *
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 */
public class DataViewOutputPanel extends JPanel {

    DatabaseConnection dbConn;
    String queryString;
    private DBMetaDataFactory dbMeta;
    private DBTableWrapper tblMeta;
    private ResultSetPageContext dataPage;
    
    private DataViewOutputPanelUI dataViewUI;
    private static Logger mLogger = Logger.getLogger(DataViewOutputPanel.class.getName());
    private static volatile InputOutput out = IOProvider.getDefault().getIO("DataView", true);
    public static int DEFAULT_TOOLBAR = 0;
    public static int HORIZONTALONLY_TOOLBAR = 1;
    private int toolbarType;

    public DataViewOutputPanel(DatabaseConnection dbConn, String qs, int toolbarType) throws Exception {
        this.dbConn = dbConn;
        this.queryString = qs.trim();
        this.toolbarType = toolbarType;
        this.setName("Data:" + queryString);

        dataPage = new ResultSetPageContext();
        Connection conn = DBConnectionFactory.getInstance().getConnection(dbConn);
        dbMeta = new DBMetaDataFactory(conn);
    }

    DBTableWrapper getDBTableWrapper() {
        return tblMeta;
    }

    ResultSetPageContext getResultSetPage() {
        return dataPage;
    }

    public void generateResult() {
        this.setName("Data:" + queryString);
        String title = "Loading Data";
        String msg = "Loading from database, please wait...";
        DataViewUtils.startProgressDialog(title, msg);

        DataViewWorkerThread queryThread = new DataViewWorkerThread(this);
        queryThread.start();
    }

    public JButton[] getVerticalToolBar() {
        return dataViewUI.getVerticalToolBar();
    }

    protected void disableButtons() {
        if (dataViewUI != null) {
            dataViewUI.disableButtons();
        }
    }

    protected void executeUpdate(String key) throws NumberFormatException, SQLException, DBException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String errorMsg = "";

        ResultSetUpdatedRowContext tblContext = dataViewUI.getResultSetRowContext();
        int row = Integer.parseInt(key.substring(0, key.indexOf(";")));
        int col = Integer.parseInt(key.substring(key.indexOf(";") + 1, key.length()));
        String updateStmt = tblContext.getUpdateStmt(key);
        String rawUpdateStmt = tblContext.getRawUpdateStmt((key));
        List<Object> values = tblContext.getValueList(key);
        List<Integer> types = tblContext.getTypeList(key);

        int rowCount = 0;
        try {
            conn = DBConnectionFactory.getInstance().getConnection(dbConn);
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(updateStmt);
            int pos = 1;
            for (Object val : values) {
                DBReadWriteHelper.setAttributeValue(pstmt, pos, types.get(pos - 1), val);
                pos++;
            }

            mLogger.info(rawUpdateStmt);
            rowCount = pstmt.executeUpdate();
        } catch (SQLException ex) {
            errorMsg = "Update failed at Row:" + row + ", Column:" + col;
            errorMsg += "\nErrorCode:" + ex.getErrorCode() + ", " + ex.getMessage();
        } finally {
            if (rowCount == 0) {
                if (DataViewUtils.isNullString(errorMsg)) {
                    errorMsg = "No rows updated using " + rawUpdateStmt;
                }
                printerrToOutputTab(errorMsg);
                rollback(conn);
            } else if (rowCount > 1) {
                errorMsg = "Unable to find unique row using " + rawUpdateStmt;
                printerrToOutputTab(errorMsg);
                rollback(conn);
            } else {
                conn.commit();
            }
            DataViewUtils.closeResources(pstmt);
        }
    }

    protected void refreshActionPerformed() {
        int intVal = dataPage.getTotalRows();
        if (intVal < 0) {
            return;
        }
        dataPage.setRecordToRefresh(intVal);
        generateResult();
    }

    protected void setCommitEnabled(boolean flag) {
        dataViewUI.setCommitEnabled(flag);
    }

    protected void clearPanel() {
        dataViewUI.clearPanel();
    }

    private boolean rejectModifications() {
        boolean doCalculation = true;
        if (dataViewUI.isCommitEnabled()) {
            String msg = "You have uncommited Changes in this page. If you continue, you changes will be lost. Do you still want to continue?";
            NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, "Confirm navigation", NotifyDescriptor.YES_NO_OPTION);
            if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.NO_OPTION) {
                doCalculation = false;
            }
        }
        return doCalculation;
    }

    protected void setMaxActionPerformed() {
        if (rejectModifications()) {
            int pageSize = dataViewUI.getPageSize(dataPage.getTotalRows());
            dataPage.setPageSize(pageSize);
            dataPage.first();
            generateResult();
        }
    }

    protected void firstActionPerformed() {
        if (rejectModifications()) {
            dataPage.first();
            generateResult();
        }
    }

    protected void previousActionPerformed() {
        if (rejectModifications()) {
            dataPage.previous();
            generateResult();
        }
    }

    protected void nextActionPerformed() {
        if (rejectModifications()) {
            dataPage.next();
            generateResult();
        }
    }

    protected void lastActionPerformed() {
        if (rejectModifications()) {
            dataPage.last();
            generateResult();
        }
    }

    protected void commitActionPerformed() {
        if (dataViewUI.isDirty()) {
            try {
                ResultSetUpdatedRowContext tblContext = dataViewUI.getResultSetRowContext();
                for (String key : tblContext.getUpdateKeys()) {
                    executeUpdate(key);
                }
            } catch (Exception ex) {
                String errorMsg = DBException.getMessage(ex);
                printerrToOutputTab(errorMsg);
            } finally {
                generateResult();
            }
        }
    }

    // TODO: should support generated, default and make null
    void insertActionPerformed() {
        InsertRecordDialog dialog = new InsertRecordDialog(this);
        dialog.setVisible(true);
    }

    void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ex) {
            String errorMsg = "Fail to rollback.\n";
            printerrToOutputTab(errorMsg + DBException.getMessage(ex));
        }
    }

    void resetToolbar(boolean wasError) {
        if (dataViewUI != null) {
            dataViewUI.resetToolbar(wasError);
        }
    }

    void setResultSet(ResultSet rs, List<String> genKeys) throws DBException, SQLException {

        if (dataViewUI == null) {
            Collection<DBTable> tables = dbMeta.generateDBTables(rs);
            this.tblMeta = new DBTableWrapper(tables);
            if (!tables.isEmpty()) {

                for (DBTable tbl : tables) {
                    tbl.setEditable(tables.size() == 1 && !tbl.getName().equals(""));
                }
                dataViewUI = new DataViewOutputPanelUI(this, toolbarType);
                dataViewUI.setEditable(tables.size() == 1 && !tblMeta.geTable(0).equals(""));
            }
        }
        dataViewUI.setResultSet(rs, dataPage.getPageSize(), dataPage.getCurrentPos() - 1);

    }

    void deleteRecordActionPerformed() {
        ResulSetTable rsTable = dataViewUI.getResulSetTable();
        if (rsTable.getSelectedRowCount() == 0) {
            String msg = "Please select a row to delete.";
            printerrToOutputTab(msg);
        } else {
            try {
                String msg = "Permanently delete record(s) from the database?";
                NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, "Confirm delete", NotifyDescriptor.OK_CANCEL_OPTION);
                if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
                    int[] rows = rsTable.getSelectedRows();
                    for (int j = 0; j < rows.length; j++) {
                        executeDeleteRow(rows[j]);
                    }
                    generateResult();
                }
            } catch (Exception ex) {
                String msg = "Error Deleting Row(s): " + ex.getMessage();
                printerrToOutputTab(msg);
            }
        }
    }

    int executeDeleteRow(int rowNum) {
        List<Object> values = new ArrayList<Object>();
        List<Integer> types = new ArrayList<Integer>();
        ResultSetUpdatedRowContext tblcContext = dataViewUI.getResultSetRowContext();
        String[] deleteStmt = tblcContext.cteateDeleteStatement(types, values, rowNum);
        String rawDeleteStmt = deleteStmt[1];
        mLogger.info("Delete Statement: " + rawDeleteStmt);
        PreparedStatement pstmt = null;
        Connection conn = null;
        boolean error = false;
        String errorMsg = "";

        try {
            conn = DBConnectionFactory.getInstance().getConnection(dbConn);
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
                    printerrToOutputTab("Commit Failed\n");
                    printerrToOutputTab("Using SQL:" + rawDeleteStmt + "\n");
                    printerrToOutputTab(DBException.getMessage(ex));
                }
            } else {
                printerrToOutputTab("Delete command failed\n" + errorMsg);
                printerrToOutputTab("Using SQL:" + rawDeleteStmt + "\n");
                printerrToOutputTab(errorMsg);
            }
        }
        return 0;
    }

    void setTotalCount(ResultSet rs) {
        try {
            if (rs == null) {
                dataViewUI.setTotalCount(0);
            } else {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    dataViewUI.setTotalCount(count);
                    dataPage.setTotalRows(count);
                }
            }
        } catch (SQLException ex) {
            mLogger.info("Could not get total row count " + ex);
        }
    }

    public void setIOProvider(InputOutput inOut) {
        DataViewOutputPanel.out = inOut;
    }

    void printerrToOutputTab(String errMsg) {
        if (!WindowManager.getDefault().findTopComponent("output").isShowing()) {
            WindowManager.getDefault().findTopComponent("output").open();
        }
        out.getErr().println(errMsg);  //this text should appear in red

        out.getErr().close();
    }

    void printinfoToOutputTab(String infoMsg) {
        if (!WindowManager.getDefault().findTopComponent("output").isShowing()) {
            WindowManager.getDefault().findTopComponent("output").open();
        }
        out.getOut().println(infoMsg);
        out.getOut().close();
    }
}
