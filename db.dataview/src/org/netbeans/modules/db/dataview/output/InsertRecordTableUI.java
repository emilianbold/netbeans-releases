/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 - 2009 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 - 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.dataview.output;

import org.netbeans.modules.db.dataview.table.ResultSetJXTable;
import java.sql.Types;
import java.util.Arrays;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.swingx.JXTable;
import org.netbeans.modules.db.dataview.meta.DBColumn;

/**
 * @author Shankari
 */
class InsertRecordTableUI extends ResultSetJXTable {

    boolean isRowSelectionAllowed = rowSelectionAllowed;

    public InsertRecordTableUI(DataView dataView) {
        super(dataView);
        if (getRSColumnCount() < 7) {
            setAutoResizeMode(JXTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        }
    }   

    protected Object[] createNewRow() {
        Object[] row = new Object[getRSColumnCount()];
        for (int i = 0, I = getRSColumnCount(); i < I; i++) {
            DBColumn col = getDBColumn(i);
            if (col.isGenerated()) {
                row[i] = "<GENERATED>";
            } else if (col.hasDefault()) {
                row[i] = "<DEFAULT>";
            } else if (col.getJdbcType() == Types.TIMESTAMP) {
                row[i] = "<CURRENT_TIMESTAMP>";
            } else if (col.getJdbcType() == Types.DATE) {
                row[i] = "<CURRENT_DATE>";
            } else if (col.getJdbcType() == Types.TIME) {
                row[i] = "<CURRENT_TIME>";
            }
        }
        return row;
    }

    protected void removeRows() {
        if (isEditing()) {
            getCellEditor().cancelCellEditing();
        }
        int[] rows = getSelectedRows();
        if (rows.length == 0) return ;
        Arrays.sort(rows);
        DefaultTableModel model = (DefaultTableModel) getModel();
        for (int i = (rows.length - 1); i >= 0; i--) {
            model.removeRow(rows[i]);
        }
        if (getRowCount() == 0) {
            model.addRow(createNewRow());
        }
    }
}
