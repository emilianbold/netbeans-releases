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
import org.netbeans.modules.db.dataview.meta.DBConnectionFactory;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.netbeans.modules.db.dataview.util.DBReadWriteHelper;
import org.netbeans.modules.db.dataview.util.DataViewUtils;

/**
 *
 * @author Ahimanikya Satapathy
 */
class SQLExecutionHelper {

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
                dvParent.rollback(conn);
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
            return error;
        }
    }
}
