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
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.codegen.DBConnectionDefinitionTemplate;
import org.netbeans.modules.etl.codegen.ETLCodegenUtil;
import org.netbeans.modules.etl.logger.Localizer;
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
import org.openide.awt.StatusDisplayer;

/**
 * @author Ahimanikya Satapathy
 */
public class RejectedRowsDataPanel extends DataOutputPanel {

    private static transient final Logger mLogger = Logger.getLogger(RejectedRowsDataPanel.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    public RejectedRowsDataPanel(SQLObject etlObject, SQLDefinition sqlDefinition) {
        super(etlObject, sqlDefinition, false, false);
    }

    public void generateResult() {
        generateResult(this.table);
    }

    public void generateResult(SQLObject aTable) {
        this.table = aTable;
        String nbBundle1 = mLoc.t("BUND353: Rejected Data: {0} ",table.getDisplayName());
        this.setName(nbBundle1.substring(15));
        String nbBundle2 = mLoc.t("BUND351: Loading Data");
        String title = nbBundle2.substring(15);
        String nbBundle3 = mLoc.t("BUND352: Loading from database, please wait...");
        String msg = nbBundle3.substring(15);
        UIUtil.startProgressDialog(title, msg);
        generateRejectionTableData();
    }

    private void generateRejectionTableData() {
        refreshButton.setEnabled(false);
        refreshField.setEnabled(false);
        RejectionViewWorkerThread rejectionQueryThread = new RejectionViewWorkerThread(table,super.sqlDefinition);
        rejectionQueryThread.start();
    }

    class RejectionViewWorkerThread extends SwingWorker {

        DBConnectionFactory factory = DBConnectionFactory.getInstance();
        private SQLObject aTable;
        private SQLDefinition sqlDef;
        private Connection conn;
        private Throwable ex;
        private PreparedStatement pstmt;
        private ResultSet rs;
        private Statement stmt;

        public RejectionViewWorkerThread(SQLObject table, SQLDefinition sqlDef) {
            this.aTable = table;
            this.sqlDef = sqlDef;
        }

        public Object construct() {
            try {
                SQLDBConnectionDefinition conDef;

                TargetTable outTable = (TargetTable) aTable;
                TargetTable clone = SQLModelObjectFactory.getInstance().createTargetTable(outTable);
                clone.setTablePrefix(MonitorUtil.LOG_DETAILS_TABLE_PREFIX);

                DBConnectionDefinitionTemplate connTemplate = new DBConnectionDefinitionTemplate();
                //conDef = connTemplate.getDBConnectionDefinition("AXIONMEMORYDB");
                conDef = connTemplate.getDBConnectionDefinition("STCDBADAPTER");
                Map connParams = new HashMap();
                connParams.put(DBConnectionDefinitionTemplate.KEY_DATABASE_NAME, this.sqlDef.getDisplayName());
                connParams.put(DBConnectionDefinitionTemplate.KEY_METADATA_DIR, ETLCodegenUtil.getMonitorDBDir(sqlDef.getDisplayName(), sqlDef.getAxiondbWorkingDirectory()));
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
                mLogger.infoNoloc(mLoc.t("EDIT155: Select statement used for show data:{0}is{1}",NL,sql));
                this.rs = pstmt.executeQuery();
                queryView.setResultSet(rs, maxRows, 0);
                pstmt.close();
                queryView.setEditable(false);
                //get the count of all rows
                sqlPart = db.getStatements().getRowCountStatement(clone, context);
                String countSql = db.getStatements().normalizeSQLForExecution(sqlPart).getSQL();
                mLogger.infoNoloc(mLoc.t("EDIT156: Select count(*) statement used for total rows:{0}is{1}",NL,countSql));
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
                mLogger.errorNoloc(mLoc.t("EDIT157: Can't get contents for table{0}in{1}",((aTable != null) ? aTable.getDisplayName() : ""),DataOutputPanel.class.getName()),e);
                queryView.clearView();
                totalRowsLabel.setText("0");
            }
            return "";
        }

        //Runs on the event-dispatching thread.
        @Override
        public void finished() {
            if (this.ex != null) {
                String nbBundle1 = mLoc.t("BUND354: Error fetching data for table {0}.\nCause: {1}",aTable.getDisplayName(),ex.getMessage());
                String errorMsg = nbBundle1.substring(15);

                // If rejection table does not exist, show a brief user-friendly message
                // that doesn't include a stack trace.
                if (ex instanceof SQLException && "42704".equals(((SQLException) ex).getSQLState())) {
                    String nbBundle4 = mLoc.t("BUND355: No rejection rows available for table {0}.",aTable.getDisplayName());
                    errorMsg = nbBundle4.substring(15);
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
                mLogger.errorNoloc(mLoc.t("EDIT701: Could not close statement after retrieving aTable contents."),sqle);
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
