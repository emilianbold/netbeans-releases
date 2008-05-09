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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.db.sql.execute.ui;

import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JButton;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.db.core.SQLOptions;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.netbeans.modules.db.sql.execute.ColumnDef;
import org.netbeans.modules.db.sql.execute.FetchLimitHandler;
import org.netbeans.modules.db.sql.execute.ResultSetTableModelSupport;
import org.openide.awt.Mnemonics;

/**
 * TableModel based on a ResultSet. 
 *
 * @author Andrei Badea
 */
public class ResultSetTableModel extends AbstractTableModel {
        
    // TODO: be conservative for editing: no editing for sensitive result sets
    // TODO: maybe the fetch handler should be received as a param
    
    private final List/*<ColumnDef>*/ columnDefs;
    private final List/*<List>*/ rows;
    
    /**
     * @throws SQLException if a database error occurred
     * @throws IOException if an error occurred while reading data from a column
     *
     * @return a TableModel for the ResultSet or null if the calling thread was
     *         interrupted
     */
    public static ResultSetTableModel create(DatabaseMetaData dbmd, 
            ResultSet rs) throws SQLException, IOException {
        ResultSetMetaData rsmd = rs.getMetaData();
        List columnDefs = ResultSetTableModelSupport.createColumnDefs(dbmd, rsmd);
        if (columnDefs == null) { // thread interrupted
            return null;
        }
        List rows = ResultSetTableModelSupport.retrieveRows(dbmd, rs, rsmd, new FetchLimitHandlerImpl());
        if (rows == null) { // thread interrupted
            return null;
        }
        return new ResultSetTableModel(columnDefs, rows);
    }
    
    private ResultSetTableModel(List/*<ColumnDef>*/ columnDefs, List/*<List>*/ rows) {
        this.columnDefs = columnDefs;
        this.rows = rows;
    }

    public int getRowCount() {
        return rows.size();
    }

    public int getColumnCount() {
        return columnDefs.size();
    }

    public Object getValueAt(int row, int column) {
        List rowData = (List)rows.get(row);
        return rowData.get(column);
    }

    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public String getColumnName(int column) {
        return getColumnDef(column).getLabel();
    }

    public Class getColumnClass(int column) {
        return getColumnDef(column).getDisplayClass();
    }

    private ColumnDef getColumnDef(int column) {
        return (ColumnDef)columnDefs.get(column);
    }
    
    private static final class FetchLimitHandlerImpl implements FetchLimitHandler {
        
        public int fetchLimitReached(int fetchCount) {
            JButton fetchYes = new JButton();
            Mnemonics.setLocalizedText(fetchYes, NbBundle.getMessage(ResultSetTableModel.class, "LBL_FetchYes"));
            fetchYes.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ResultSetTableModel.class, "ACSD_FetchYes"));
            
            JButton fetchAll = new JButton();
            Mnemonics.setLocalizedText(fetchAll, NbBundle.getMessage(ResultSetTableModel.class, "LBL_FetchAll"));
            fetchAll.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ResultSetTableModel.class, "ACSD_FetchAll"));
            
            JButton fetchNo = new JButton();
            Mnemonics.setLocalizedText(fetchNo, NbBundle.getMessage(ResultSetTableModel.class, "LBL_FetchNo"));
            fetchNo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ResultSetTableModel.class, "ACSD_FetchNo"));
            
            String title = NbBundle.getMessage(ResultSetTableModel.class, "LBL_FetchNextTitle");
            String message = NbBundle.getMessage(ResultSetTableModel.class, "LBL_FetchNextMessage", 
                    new Object[] { new Integer(fetchCount), new Integer(SQLOptions.getDefault().getFetchStep()) });
            
            NotifyDescriptor desc = new NotifyDescriptor(message, title, NotifyDescriptor.YES_NO_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE, new Object[] { fetchYes, fetchAll , fetchNo }, NotifyDescriptor.CANCEL_OPTION);
            Object ret = DialogDisplayer.getDefault().notify(desc);
            
            if (ret instanceof JButton) {
                if (ret == fetchYes) {
                    return fetchCount + SQLOptions.getDefault().getFetchStep();
                } else if (ret == fetchAll) {
                    return 0;
                } else {
                    return fetchCount;
                }
            } else {
                // dialog closed using the close button or the Esc key
                return fetchCount;
            }
        }

        public int getFetchLimit() {
            return SQLOptions.getDefault().getFetchStep();
        }
    }
}
