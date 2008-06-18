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

import org.netbeans.modules.db.dataview.util.SwingWorker;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.netbeans.modules.db.dataview.util.DataViewUtils;
import org.netbeans.modules.db.dataview.meta.DBConnectionFactory;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.netbeans.modules.db.dataview.meta.DBTable;
import org.netbeans.modules.db.dataview.meta.DBObject;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * @author Ahimanikya Satapathy
 */
class TruncateTableAction extends AbstractAction {

    DataViewOutputPanel dataViewPanel;
    private Logger mLogger = Logger.getLogger(TruncateTableAction.class.getName());

    protected TruncateTableAction(DataViewOutputPanel panel) {
        super();
        this.dataViewPanel = panel;
    }

    public void actionPerformed(ActionEvent e) {
        String nbBundle1 = "Truncate contents of table " + dataViewPanel.getDBTableWrapper().geTable(0).getDisplayName();
        String confirmMsg = nbBundle1;
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(confirmMsg, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.WARNING_MESSAGE);

        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.CANCEL_OPTION) {
            return;
        }
        String nbBundle2 = "Truncating Table";
        String title = nbBundle2;
        String nbBundle3 = "Truncating Table from database, please wait...";
        String msg = nbBundle3;
        DataViewUtils.startProgressDialog(title, msg);

        dataViewPanel.disableButtons();
        TruncateTableWorkerThread tThread = new TruncateTableWorkerThread(dataViewPanel.getDBTableWrapper().geTable(0));
        tThread.start();
    }

    protected class TruncateTableWorkerThread extends SwingWorker {

        private Throwable ex;
        private DBObject tTable;

        public TruncateTableWorkerThread(DBObject table) {
            this.tTable = table;
        }

        public Object construct() {
            truncateDBTable();
            return "";
        }

        @Override
        public void finished() {
            DataViewUtils.stopProgressDialog();

            if (this.ex != null) {
                String errorMsg = "Failed to truncate table" + tTable.getDisplayName() + "\n";
                errorMsg += new DBException(ex).getMessage();
                NotifyDescriptor nd = new NotifyDescriptor.Message(errorMsg);
                DialogDisplayer.getDefault().notify(nd);
            } else {
                String informMsg = "Table " + tTable.getDisplayName() + " truncated.";
                NotifyDescriptor nd = new NotifyDescriptor.Message(informMsg);
                DialogDisplayer.getDefault().notify(nd);

            }
            dataViewPanel.clearPanel();
        }

        private void truncateDBTable() {
            Connection conn = null;
            Statement stmt = null;
            ResultSet cntRs = null;
            String truncateSql = "";

            try {
                conn = DBConnectionFactory.getInstance().getConnection(dataViewPanel.dbConn);
                conn.setAutoCommit(true);
                stmt = conn.createStatement();

                DBTable aTable = (DBTable) tTable;
                truncateSql = "Truncate table " + aTable.getFullyQualifiedName();
                try {
                    mLogger.info("Trncating Table Using: " + truncateSql);
                    stmt.executeUpdate(truncateSql);

                } catch (SQLException sqe) {
                    truncateSql = "Delete from " + aTable.getFullyQualifiedName();
                    mLogger.info("Trncating Table Using: " + truncateSql);
                    stmt.executeUpdate(truncateSql);
                }

                if (!conn.getAutoCommit()) {
                    conn.commit();
                }

                //set the total count
                stmt = conn.createStatement();
                String countSql = "Select Count(*) from " + aTable.getFullyQualifiedName();
                mLogger.info(countSql);
                cntRs = stmt.executeQuery(countSql);
                dataViewPanel.setTotalCount(cntRs);
            } catch (Exception t) {
                this.ex = t;
                mLogger.info("Could not truncate data using " + truncateSql);
            } finally {
                DataViewUtils.closeResources(stmt);
                DataViewUtils.closeResources(cntRs);
            }
        }
    }
}
