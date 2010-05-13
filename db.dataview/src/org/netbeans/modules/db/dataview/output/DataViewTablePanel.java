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
package org.netbeans.modules.db.dataview.output;

import org.netbeans.modules.db.dataview.table.JXTableRowHeader;
import java.awt.BorderLayout;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.JScrollPane;
import org.jdesktop.swingx.JXPanel;

/**
 * Renders rows and columns of a given ResultSet via JTable.
 *
 * @author Ahimanikya Satapathy
 */
class DataViewTablePanel extends JXPanel {

    private final DataViewTableUI tableUI;
    private DataViewUI dataViewUI;
    private DataView dataView;
    private boolean isDirty = false;
    private UpdatedRowContext updatedRowCtx;
    private JXTableRowHeader rowHeader;
    private boolean editable;

    public DataViewTablePanel(DataView dataView, DataViewUI dataViewUI, DataViewActionHandler actionHandler) {
        this.setLayout(new BorderLayout());
        
        this.dataView = dataView;
        this.dataViewUI = dataViewUI;
        
        tableUI = new DataViewTableUI(this, actionHandler, dataView);
        updatedRowCtx = new UpdatedRowContext();
        
        rowHeader = new JXTableRowHeader(tableUI);
        JScrollPane sp = new JScrollPane(tableUI);
        sp.setRowHeaderView(rowHeader);
        
        sp.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowHeader.getTableHeader());
        this.add(sp, BorderLayout.CENTER);
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    protected boolean isEditable() {
        return editable;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        isDirty = dirty;
        if (!isDirty) {
            updatedRowCtx.removeAllUpdates();
        }
    }

    DataViewTableUI getDataViewTableUI() {
        return tableUI;
    }
    
    UpdatedRowContext getUpdatedRowContext() {
        return updatedRowCtx;
    }

    boolean isCommitEnabled() {
        return dataViewUI.isCommitEnabled();
    }

    public void createTableModel(List<Object[]> rows) {
        setDirty(false);
        tableUI.createTableModel(rows, rowHeader);
    }

    void handleColumnUpdated() {
        isDirty = true;
        dataViewUI.setCommitEnabled(true);
        dataViewUI.setCancelEnabled(true);
    }

    List<Object[]> getPageDataFromTable() {
        DefaultTableModel dtm = (DefaultTableModel) tableUI.getModel();
        List<Object[]> rows = new ArrayList<Object[]>();
        int colCnt = dtm.getColumnCount();
        for (Object row : dtm.getDataVector()) {
            Object[] rowObj = new Object[colCnt];
            int i = 0;
            for (Object colVal : (Vector) row) {
                rowObj[i++] = colVal;
            }
            rows.add(rowObj);
        }
        return rows;
    }

    public void enableDeleteBtn(boolean value) {
        dataViewUI.enableDeleteBtn(value);
    }

    void setValueAt(Object val, int row, int col) {
        DefaultTableModel dtm = (DefaultTableModel) tableUI.getModel();
        dtm.setValueAt(val, row, col);
    }
}
