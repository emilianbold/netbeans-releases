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
import net.java.hulp.i18n.Logger;
import com.sun.sql.framework.utils.StringUtil;
import java.awt.event.ActionEvent;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.Properties;
import javax.swing.AbstractAction;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.codegen.DB;
import org.netbeans.modules.sql.framework.codegen.DBFactory;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.codegen.Statements;
import org.netbeans.modules.sql.framework.codegen.axion.AxionDB;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.ui.SwingWorker;
import org.netbeans.modules.sql.framework.ui.utils.UIUtil;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;

/**
 * @author Ahimanikya Satapathy
 */
class TruncateTableAction extends AbstractAction {

    private static transient final Logger mLogger = Logger.getLogger(TruncateTableAction.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    DataOutputPanel outer;

    protected TruncateTableAction(DataOutputPanel outer) {
        super();
        this.outer = outer;
    }

    public void actionPerformed(ActionEvent e) {
        String nbBundle1 = mLoc.t("BUND357: Truncate contents of table {0}?", outer.table.getDisplayName());
        String confirmMsg = nbBundle1.substring(15);
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(confirmMsg, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.WARNING_MESSAGE);

        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.CANCEL_OPTION) {
            return;
        }
        String nbBundle2 = mLoc.t("BUND358: Truncating Table");
        String title = nbBundle2.substring(15);
        String nbBundle3 = mLoc.t("BUND359: Truncating Table from database, please wait...");
        String msg = nbBundle3.substring(15);
        UIUtil.startProgressDialog(title, msg);

        if (outer.truncateButton != null) {
            outer.truncateButton.setEnabled(false);
        }

        outer.refreshButton.setEnabled(false);
        outer.refreshField.setEnabled(false);
        TruncateTargetTableWorkerThread tThread = new TruncateTargetTableWorkerThread(outer.table);
        tThread.start();
    }

    protected class TruncateTargetTableWorkerThread extends SwingWorker {

        private ResultSet cntRs;
        private SQLPart defragStmtPart;
        private Throwable ex;
        private DBConnectionFactory factory = DBConnectionFactory.getInstance();
        private Statement stmtCount;
        private SQLObject tTable;
        private Connection conn;

        public TruncateTargetTableWorkerThread(SQLObject table) {
            this.tTable = table;
        }

        public Object construct() {
            SQLDBTable aTable = (SQLDBTable) tTable;
            try {
                DBConnectionDefinition connDef = ((SQLDBModel) aTable.getParentObject()).getETLDBConnectionDefinition();

                Properties connProps = connDef.getConnectionProperties();
                conn = factory.getConnection(connProps);
                conn.setAutoCommit(true);
                Statement stmt = conn.createStatement();

                // Generate the truncate command
                DB db = DBFactory.getInstance().getDatabase(factory.getDatabaseVersion(connProps));
                if (db instanceof AxionDB) {
                    outer.insert.setEnabled(false);
                } else {
                    outer.insert.setEnabled(true);
                }
                Statements stmts = db.getStatements();
                StatementContext context = new StatementContext();
                TargetTable tt = (TargetTable) aTable;

                //SQLPart sqlPart = stmts.getTruncateStatement(tt, context);
                SQLPart sqlPart = stmts.getTruncateStatement(aTable, context);
                String sqlList = sqlPart.getSQL();

                Iterator stmtIter = StringUtil.createStringListFrom(sqlList, SQLPart.STATEMENT_SEPARATOR).iterator();

                while (stmtIter.hasNext()) {
                    String sql = (String) stmtIter.next();
                    stmt.executeUpdate(sql);
                }
                stmt.close();

                // Get the count of all rows
                stmtCount = conn.createStatement();
                SQLPart cntSqlPart = db.getStatements().getRowCountStatement(tt, context);
                String countSql = cntSqlPart.getSQL();
                this.cntRs = stmtCount.executeQuery(countSql);
            } catch (Exception t) {
                this.ex = t;
                mLogger.errorNoloc(mLoc.t("EDIT149: Could not truncate output for target aTable{0}", DataOutputPanel.class.getName()), t);

            }

            return "";
        }

        //Runs on the event-dispatching thread.
        @Override
        public void finished() {
            UIUtil.stopProgressDialog();
            if (this.ex != null) {
                String nbBundle4 = mLoc.t("BUND360: Failed to truncate table {0}.Cause: {1}",tTable.getDisplayName(),ex.getMessage());
                String errorMsg = nbBundle4.substring(15);
                DialogDisplayer.getDefault().notify(new Message(errorMsg, NotifyDescriptor.ERROR_MESSAGE));
            } else {
                String nbBundle5 = mLoc.t("BUND361: Table {0} truncated.",tTable.getDisplayName());
                String informMsg = nbBundle5.substring(15);
                DialogDisplayer.getDefault().notify(new Message(informMsg, NotifyDescriptor.INFORMATION_MESSAGE));
            }

            if (outer.truncateButton != null) {
                outer.truncateButton.setEnabled(true);
            }
            outer.refreshButton.setEnabled(true);
            outer.refreshField.setEnabled(true);
            //set the count
            outer.setTotalCount(this.cntRs);
            outer.first.setEnabled(false);
            outer.previous.setEnabled(false);
            outer.next.setEnabled(false);
            outer.last.setEnabled(false);
            outer.deleteRow.setEnabled(false);
            outer.commit.setEnabled(false);
            if (outer.queryView != null) {
                outer.queryView.clearView();
            }

            if (conn != null) {
                try {
                    if (cntRs != null) {
                        cntRs.close();
                    }

                    if (stmtCount != null) {
                        stmtCount.close();
                    }

                    if (!conn.getAutoCommit()) {
                        conn.commit();
                    }

                    if (defragStmtPart != null) {
                        factory.shutdown(conn, true, defragStmtPart.getSQL());
                    } else {
                        factory.shutdown(conn, false, null);
                    }
                } catch (Exception t) {
                    mLogger.errorNoloc(mLoc.t("EDIT170: Could not commit truncate action for target aTable...{0}", DataOutputPanel.class.getName()), t);

                } finally {
                    outer.queryView.revalidate();
                    outer.queryView.repaint();
                }
            }
        }
    }
}
