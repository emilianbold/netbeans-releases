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

import com.sun.sql.framework.jdbc.DBConnectionFactory;
import com.sun.sql.framework.jdbc.SQLPart;
import com.sun.sql.framework.utils.Logger;
import com.sun.sql.framework.utils.StringUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.netbeans.modules.etl.codegen.DBConnectionDefinitionTemplate;
import org.netbeans.modules.sql.framework.codegen.DB;
import org.netbeans.modules.sql.framework.codegen.DBFactory;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.common.jdbc.SQLDBConnectionDefinition;
import org.netbeans.modules.sql.framework.common.jdbc.SQLUtils;
import org.netbeans.modules.sql.framework.common.utils.MonitorUtil;
import org.netbeans.modules.sql.framework.model.RuntimeDatabaseModel;
import org.netbeans.modules.sql.framework.model.RuntimeInput;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.ui.SwingWorker;
import org.netbeans.modules.sql.framework.ui.utils.UIUtil;
import org.netbeans.modules.sql.framework.ui.view.BasicTopView;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;

/**
 * @author Ahimanikya Satapathy
 */
public class RejectedRowsDataPanel extends DataOutputPanel {

    public RejectedRowsDataPanel(SQLObject etlObject, SQLDefinition sqlDefinition) {
        super(etlObject, sqlDefinition, false, false);
    }

    public void generateResult() {
        generateResult(this.table);
    }

    public void generateResult(SQLObject aTable) {
        this.table = aTable;
        this.setName(NbBundle.getMessage(DataOutputPanel.class, "LBL_tab_rejected_data", table.getDisplayName()));
        String title = NbBundle.getMessage(BasicTopView.class, "MSG_LoadData");
        String msg = NbBundle.getMessage(BasicTopView.class, "MSG_LoadProgress");
        UIUtil.startProgressDialog(title, msg);
        generateRejectionTableData();
    }

    private void generateRejectionTableData() {
        refreshButton.setEnabled(false);
        refreshField.setEnabled(false);
        RejectionViewWorkerThread rejectionQueryThread = new RejectionViewWorkerThread(table);
        rejectionQueryThread.start();
    }

    class RejectionViewWorkerThread extends SwingWorker {

        DBConnectionFactory factory = DBConnectionFactory.getInstance();
        private SQLObject aTable;
        private Connection conn;
        private Throwable ex;
        private PreparedStatement pstmt;
        private ResultSet rs;
        private Statement stmt;

        public RejectionViewWorkerThread(SQLObject table) {
            this.aTable = table;
        }

        public Object construct() {
            try {
                SQLDBConnectionDefinition conDef;

                TargetTable outTable = (TargetTable) aTable;
                TargetTable clone = SQLModelObjectFactory.getInstance().createTargetTable(outTable);
                clone.setTablePrefix(MonitorUtil.LOG_DETAILS_TABLE_PREFIX);

                DBConnectionDefinitionTemplate connTemplate = new DBConnectionDefinitionTemplate();
                conDef = connTemplate.getDBConnectionDefinition("AXIONMEMORYDB");

                Map connParams = new HashMap();
                connParams.put(DBConnectionDefinitionTemplate.KEY_DATABASE_NAME, "MonitorDB");
                conDef.setConnectionURL(StringUtil.replace(conDef.getConnectionURL(), connParams));
                DB db = DBFactory.getInstance().getDatabase(DB.AXIONDB);

                Properties connProps = conDef.getConnectionProperties();
                conn = factory.getConnection(connProps);
                stmt = conn.createStatement();

                StatementContext context = new StatementContext();
                context.setUsingFullyQualifiedTablePrefix(false);
                context.setUsingUniqueTableName(true);
                Object limit = (recordToRefresh == 0) ? "" : Integer.toString(recordToRefresh);
                context.putClientProperty("limit", limit);

                SQLPart sqlPart = db.getStatements().getSelectStatement(clone, context);
                String sql = sqlPart.getSQL();

                // execute select and get result set
                List paramList = new ArrayList();
                Map attribMap = new HashMap();
                RuntimeDatabaseModel runtimeModel = getRuntimeDbModel();
                if (runtimeModel != null) {
                    RuntimeInput inputTable = runtimeModel.getRuntimeInput();
                    if (inputTable != null) {
                        attribMap = inputTable.getRuntimeAttributeMap();
                    }
                }

                String psSql = SQLUtils.createPreparedStatement(sql, attribMap, paramList);
                pstmt = conn.prepareStatement(psSql);
                SQLUtils.populatePreparedStatement(pstmt, attribMap, paramList);

                Logger.print(Logger.DEBUG, RejectedRowsDataPanel.class.getName(), "Select statement used for show data:" + NL + sql);
                this.rs = pstmt.executeQuery();
                queryView.setResultSet(rs, maxRows, 0);
                pstmt.close();
                queryView.setEditable(false);
                //get the count of all rows
                sqlPart = db.getStatements().getRowCountStatement(clone, context);
                String countSql = db.getStatements().normalizeSQLForExecution(sqlPart).getSQL();
                Logger.print(Logger.DEBUG, DataOutputPanel.class.getName(), "Select count(*) statement used for total rows:" + NL + countSql);

                paramList.clear();
                psSql = SQLUtils.createPreparedStatement(countSql, attribMap, paramList);
                pstmt = conn.prepareStatement(psSql);
                SQLUtils.populatePreparedStatement(pstmt, attribMap, paramList);
                ResultSet cntRs = pstmt.executeQuery();

                //set the count
                setTotalCount(cntRs);
                cntRs.close();
            } catch (Exception e) {
                this.ex = e;
                Logger.printThrowable(Logger.ERROR, DataOutputPanel.class.getName(), null, "Can't get contents for table " + ((aTable != null) ? aTable.getDisplayName() : ""), e);
                queryView.clearView();
                totalRowsLabel.setText("0");
            }

            return "";
        }

        //Runs on the event-dispatching thread.
        @Override
        public void finished() {
            if (this.ex != null) {
                String errorMsg = NbBundle.getMessage(DataOutputPanel.class, "MSG_error_fetch_failed", aTable.getDisplayName(), ex.getMessage());

                // If rejection table does not exist, show a brief user-friendly message
                // that doesn't include a stack trace.
                if (ex instanceof SQLException && "42704".equals(((SQLException) ex).getSQLState())) {
                    errorMsg = NbBundle.getMessage(DataOutputPanel.class, "MSG_no_rejection_data", aTable.getDisplayName());
                }

                StatusDisplayer.getDefault().setStatusText(errorMsg);
            }

            if (truncateButton != null) {
                truncateButton.setEnabled(true);
            }
            refreshButton.setEnabled(true);
            refreshField.setEnabled(true);
            if ((nowCount - maxRows) > 0) {
                first.setEnabled(true);
                previous.setEnabled(true);
            }
            if ((nowCount + maxRows) <= totalCount) {
                next.setEnabled(true);
                last.setEnabled(true);
            }
            if ((nowCount - maxRows) <= 0) {
                first.setEnabled(false);
                previous.setEnabled(false);
            }
            if ((nowCount + maxRows) > totalCount) {
                next.setEnabled(false);
                last.setEnabled(false);
            }
            insert.setEnabled(false);
            deleteRow.setEnabled(false);
            commit.setEnabled(false);
            int endCount = nowCount + maxRows - 1;
            if ((nowCount + maxRows - 1) > totalCount) {
                endCount = totalCount;
            }
            if (totalCount == 0) {
                nowCount = 0;
                endCount = 0;
            }
            refreshField.setText("" + maxRows);
            commit.setEnabled(false);
            queryView.repaint();
            try {
                if (pstmt != null) {
                    pstmt.close();
                }

                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
                Logger.printThrowable(Logger.ERROR, DataOutputPanel.class.getName(), null, "Could not close statement after retrieving aTable contents.", sqle);
            } finally {
                if (conn != null) {
                    factory.closeConnection(conn);
                    conn = null;
                    UIUtil.stopProgressDialog();
                }
            }
        }
    }
}
