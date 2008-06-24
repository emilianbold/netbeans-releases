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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.dataview.meta.DBConnectionFactory;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.netbeans.modules.db.dataview.meta.DBMetaDataFactory;
import org.netbeans.modules.db.dataview.meta.DBTable;
import org.netbeans.modules.db.dataview.util.DataViewUtils;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * DataView to show data of a given sql query string
 *
 * @author Ahimanikya Satapathy
 */
public class DataView extends JPanel {

    public static final int DEFAULT_TOOLBAR = 0;
    public static final int HORIZONTALONLY_TOOLBAR = 1;
    DatabaseConnection dbConn;
    private String queryString;
    private DataViewDBTable tblMeta;
    private DataViewPageContext dataPage;
    private SQLExecutionHelper execHelper;
    private SQLStatementGenerator stmtGenerator;
    private List<String> errMessages = new ArrayList<String>();
    private DataViewUI dataViewUI;
    private int toolbarType;
    private ResultSet resultSet;
    private ResultSet countresultSet;
    // the RequestProcessor used for executing statements.
    private final RequestProcessor rp = new RequestProcessor("SQLQueryExecution", 1, true); // NOI18N

    private static Logger mLogger = Logger.getLogger(DataView.class.getName());

    public static DataView create(final DatabaseConnection dbConn, String qs) {
        DataView dataView = new DataView();
        dataView.dbConn = dbConn;

        dataView.queryString = qs.trim();
        dataView.toolbarType = HORIZONTALONLY_TOOLBAR;

        dataView.dataPage = new DataViewPageContext();
        dataView.execHelper = new SQLExecutionHelper(dataView, dbConn);

        dataView.executeQuery();
        return dataView;
    }

    public static JComponent createComponent(final DataView dataView) throws SQLException {
        if (dataView.dataViewUI == null) {
            dataView.dataViewUI = new DataViewUI(dataView, dataView.toolbarType, dataView.queryString);
        }
        return dataView;
    }

    public void setToolbarType(int toolbarType) {
        this.toolbarType = toolbarType;
    }

    public boolean hasException() {
        return !errMessages.isEmpty();
    }

    public Iterator<String> getExceptions() {
        return errMessages.iterator();
    }

    public JButton[] getVerticalToolBar() {
        return dataViewUI.getVerticalToolBar();
    }

    DataViewDBTable getDataViewDBTable() {
        return tblMeta;
    }

    DataViewPageContext getDataViewPageContext() {
        return dataPage;
    }

    void disableButtons() {
        if (dataViewUI != null) {
            dataViewUI.disableButtons();
        }
    }

    UpdatedRowContext getResultSetUpdatedRowContext() {
        return dataViewUI.getResultSetRowContext();
    }

    SQLExecutionHelper getSQLExecutionHelper() {
        return execHelper;
    }

    SQLStatementGenerator getSQLStatementGenerator() {
        return stmtGenerator;
    }

    protected void clearDataViewPanel() {
        dataViewUI.clearPanel();
    }

    void setInfoStatusText(String statusText) {
        StatusDisplayer.getDefault().setStatusText("INFO: " + statusText);
    }

    void setErrorStatusText(String errorMsg) {
        errMessages.add(errorMsg);
        StatusDisplayer.getDefault().setStatusText("ERROR: " + errorMsg);
    }

    void resetToolbar(boolean wasError) {
        if (dataViewUI != null) {
            dataViewUI.resetToolbar(wasError);
        }
    }

    void executeQuery() {
        SQLStatementExecutor executor = new SQLStatementExecutor(this, "Executing Query", queryString) {
            ResultSet rs = null;
            ResultSet crs = null;
            Statement stmt = null;

            // Execute the Select statement
            public void execute() throws SQLException, DBException {
                parent.disableButtons();
                
                Connection conn = DBConnectionFactory.getInstance().getConnection(parent.dbConn);
                if(conn == null) {
                    this.ex = new DBException("Unable to Connect to database");
                }
                
                stmt = conn.createStatement(
                        ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                stmt.setFetchSize(parent.getDataViewPageContext().getPageSize());

                rs = stmt.executeQuery(parent.queryString);
                parent.setResultSet(rs);

                // Get total row count
                crs = stmt.executeQuery(
                        SQLStatementGenerator.getCountSQLQuery(
                        parent.queryString));
                parent.setTotalCount(crs);
            }

            public void finished() {
                DataViewUtils.closeResources(stmt);
                DataViewUtils.closeResources(rs);
                DataViewUtils.closeResources(crs);
                parent.resetToolbar(this.ex != null);
                if (this.ex != null) {
                    parent.setErrorStatusText(ex.getMessage());
                } else {
                    parent.setInfoStatusText("Query Executed Successfully");
                }
            }
        };
        RequestProcessor.Task task = rp.create(executor);
        executor.setTask(task);
        task.schedule(0);
    }

    void setResultSet(ResultSet resultSet) throws DBException, SQLException {
        if (resultSet == null) {
            return;
        }

        this.resultSet = resultSet;
        if (dataViewUI == null) {
            dataViewUI = new DataViewUI(this, toolbarType, queryString);
        }

        if (tblMeta == null) {
            try {
                Connection conn = DBConnectionFactory.getInstance().getConnection(dbConn);
                DBMetaDataFactory dbMeta = new DBMetaDataFactory(conn);
                Collection<DBTable> tables = dbMeta.generateDBTables(resultSet);
                this.tblMeta = new DataViewDBTable(tables);
                this.stmtGenerator = new SQLStatementGenerator(tblMeta);
                if (!tables.isEmpty()) {
                    for (DBTable tbl : tables) {
                        tbl.setEditable(tables.size() == 1 && !tbl.getName().equals(""));
                    }
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        dataViewUI.setResultSet(resultSet, dataPage.getPageSize(), dataPage.getCurrentPos() - 1);
        dataViewUI.setEditable(tblMeta.geTableCount() == 1 && !tblMeta.geTable(0).equals(""));
    }

    void setTotalCount(ResultSet countresultSet) {
        this.countresultSet = countresultSet;
        try {
            if (countresultSet == null) {
                dataViewUI.setTotalCount(0);
            } else {
                if (countresultSet.next()) {
                    int count = countresultSet.getInt(1);
                    dataViewUI.setTotalCount(count);
                    dataPage.setTotalRows(count);
                }
            }
        } catch (SQLException ex) {
            mLogger.info("Could not get total row count " + ex);
        }
    }

    private DataView() {
    }
}
