/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.sql.execute.ui;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Collections;
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

/**
 * TableModel based on a ResultSet. 
 *
 * @author Andrei Badea
 */
public class ResultSetTableModel extends AbstractTableModel {
        
    // TODO: be conservative for editing: no editing for sensitive result sets
    // TODO: maybe the fetch handler should be received as a param
    
    private List/*<ColumnDef>*/ columnDefs;
    private List/*<List>*/ rows;

    /**
     * @throws SQLException if a database error has occured
     * @throws IOException if an error occured while reading data from a column
     */
    public ResultSetTableModel(ResultSet rs) throws SQLException, IOException {
        ResultSetMetaData rsmd = rs.getMetaData();
        columnDefs = ResultSetTableModelSupport.createColumnDefs(rsmd);
        rows = ResultSetTableModelSupport.retrieveRows(rs, rsmd, new FetchLimitHandlerImpl());
    }
    
    private ResultSetTableModel() {
        columnDefs = Collections.EMPTY_LIST;
        rows = Collections.EMPTY_LIST;
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
        return getColumnDef(column).getName();
    }

    public Class getColumnClass(int column) {
        return getColumnDef(column).getDisplayClass();
    }

    private ColumnDef getColumnDef(int column) {
        return (ColumnDef)columnDefs.get(column);
    }
    
    private static final class FetchLimitHandlerImpl implements FetchLimitHandler {
        
        public int fetchLimitReached(int fetchCount) {
            JButton fetchYes = new JButton(NbBundle.getMessage(SQLResultPanel.class, "LBL_FetchYes"));
            fetchYes.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SQLResultPanel.class, "ACSD_FetchYes"));
            fetchYes.setMnemonic(NbBundle.getMessage(SQLResultPanel.class, "MNE_FetchYes").charAt(0));
            
            JButton fetchAll = new JButton(NbBundle.getMessage(SQLResultPanel.class, "LBL_FetchAll"));
            fetchAll.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SQLResultPanel.class, "ACSD_FetchAll"));
            fetchAll.setMnemonic(NbBundle.getMessage(SQLResultPanel.class, "MNE_FetchAll").charAt(0));
            
            JButton fetchNo = new JButton(NbBundle.getMessage(SQLResultPanel.class, "LBL_FetchNo"));
            fetchNo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SQLResultPanel.class, "ACSD_FetchNo"));
            fetchNo.setMnemonic(NbBundle.getMessage(SQLResultPanel.class, "MNE_FetchNo").charAt(0));
            
            String title = NbBundle.getMessage(SQLResultPanel.class, "LBL_FetchNextTitle");
            Integer[] args = new Integer[] { new Integer(fetchCount), new Integer(SQLOptions.getDefault().getFetchStep()) };
            String message = MessageFormat.format(NbBundle.getMessage(SQLResultPanel.class, "LBL_FetchNextMessage"), args);
            
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
    
    public static class Empty extends ResultSetTableModel {
    }
}
