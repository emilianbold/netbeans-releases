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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.db.dataview.meta.DBConnectionFactory;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.netbeans.modules.db.dataview.util.DataViewUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * @author Ahimanikya Satapathy
 */
class DataViewWorkerThread extends SwingWorker {

    private volatile String errMsg;
    private volatile DataViewOutputPanel dataOutputPanel;

    public DataViewWorkerThread(DataViewOutputPanel dataOutputPanel) {
        super();
        this.dataOutputPanel = dataOutputPanel;
    }

    public Object construct() {
        try {
            dataOutputPanel.disableButtons();
            showDataForDBTable();
        } catch (Exception ex) {
            errMsg = "Error fetching data:\n" + DBException.getMessage(ex);
        }
        return "";
    }

    @Override
    public void finished() {
        if (this.errMsg != null) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(errMsg);
            DialogDisplayer.getDefault().notify(nd);
        }
        DataViewUtils.stopProgressDialog();
        dataOutputPanel.resetToolbar(this.errMsg != null);
    }

    public void showDataForDBTable() throws SQLException, DBException {
        Connection conn = null;
        ResultSet rs = null;
        ResultSet crs = null;
        Statement stmt = null;
        try {
            // Execute the Select statement
            conn = DBConnectionFactory.getInstance().getConnection(dataOutputPanel.dbConn);
            stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(dataOutputPanel.getResultSetPage().getPageSize());
            rs = stmt.executeQuery(dataOutputPanel.queryString);
            ResultSet genKeysRS = stmt.getGeneratedKeys();
            List<String> genKeys = new ArrayList<String>();
            while(genKeysRS!= null && genKeysRS.next()){
                genKeys.add(rs.getString(0));
            }
            dataOutputPanel.setResultSet(rs, genKeys);

            // Get total row count
            String countSql = getCountSQLQuery(dataOutputPanel.queryString);
            crs = stmt.executeQuery(countSql);
            dataOutputPanel.setTotalCount(crs);
        } catch (SQLException ex) {
            throw ex;
        } finally {
            DataViewUtils.closeResources(stmt);
            DataViewUtils.closeResources(rs);
            DataViewUtils.closeResources(crs);
        }
    }

    private String getCountSQLQuery(String queryString) {
        // User may type "FROM" in either lower, upper or mixed case
        String[] splitByFrom = queryString.toUpperCase().split("FROM");
        return "SELECT COUNT(*) " + queryString.substring(splitByFrom[0].length());
    }
}
