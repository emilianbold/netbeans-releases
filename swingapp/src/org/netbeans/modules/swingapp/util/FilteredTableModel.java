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

package org.netbeans.modules.swingapp.util;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * A custom TableModel which can filter the rows based on a search string
 *
 * @author joshua.marinacci@sun.com
 */
public class FilteredTableModel extends AbstractTableModel {
    private TableModel model;
    private String filterString;
    List<List<Object>> rows;
    
    public FilteredTableModel(TableModel model) {
        this.rows = new ArrayList<List<Object>>();
        this.model = model;
        rebuildRows();
        model.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                rebuildRows();
            }
        });
    }
    
    public void setFilterString(String filterString) {
        this.filterString = filterString;
        rebuildRows();
    }
    
    public int getRowCount() {
        return rows.size();
    }
    
    public int getColumnCount() {
        return model.getColumnCount();
    }
    
    @Override
    public String getColumnName(int columnIndex) {
        return model.getColumnName(columnIndex);
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return model.getColumnClass(columnIndex);
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // do nothing, editing not supported
        return false;
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        return rows.get(rowIndex).get(columnIndex);
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        // do nothing, editing not supported
    }
    
    public TableModel getTableModel() {
        return model;
    }
    
    private void rebuildRows() {
        rows = new ArrayList<List<Object>>();
        for(int r=0; r<model.getRowCount(); r++) {
            List<Object> row = new ArrayList<Object>();
            boolean passesFilter = false;
            for(int c=0; c<model.getColumnCount(); c++) {
                Object o = model.getValueAt(r,c);
                row.add(o);
                if(o instanceof String) {
                    if(filter((String)o)) {
                        passesFilter = true;
                    }
                }
            }
            if(passesFilter) {
                rows.add(row);
            }
        }
        TableModelEvent evt = new TableModelEvent(this);
        fireTableChanged(evt);
    }
    
    private boolean filter(String string) {
        //if the filter is empty then let it pass
        if(filterString == null || "".equals(filterString)) { // NOI18N
            return true;
        }
        
        if(string == null) {
            return false;
        }
        
        if(string.toLowerCase().contains(filterString.toLowerCase())) {
            return true;
        }
        
        return false;
    }
    
}