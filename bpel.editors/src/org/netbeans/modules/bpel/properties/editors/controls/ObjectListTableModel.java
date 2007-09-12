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
package org.netbeans.modules.bpel.properties.editors.controls;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * The base class for table models based on the list of single-type objects.
 * Type has to be specified with Generics approach.
 *
 * @author nk160297
 */
public abstract class ObjectListTableModel<T> extends AbstractTableModel {
    
    static final long serialVersionUID = 1L;
    
    private TableColumnModel columnModel;
    private ArrayList<T> myList;
    
    public ObjectListTableModel(TableColumnModel columnModel) {
        this.columnModel = columnModel;
        this.myList = new ArrayList<T>();
    }
    
    public abstract void reload();
    
    public int getRowCount() {
        return myList.size();
    }
    
    public int getColumnCount() {
        return columnModel.getColumnCount();
    }
    
    public T getRowObject(int rowIndex) {
        return myList.get(rowIndex);
    }
    
    public Class<? extends Object> getColumnClass(int columnIndex) {
        return String.class;
    }
    
    public void addRow(T row) {
        insertRow(myList.size(), row);
    }
    
    public void insertRow(int index, T row) {
        if (index < 0 || index > myList.size()) {
            return; // index out of scope
        }
        //
        myList.add(index, row);
        fireTableRowsInserted(index, index);
    }
    
    public void deleteRow(T row) {
        int index = myList.indexOf(row);
        if (index != -1) {
            myList.remove(index);
            fireTableRowsDeleted(index, index);
        }
    }
    
    public void deleteRow(int index) {
        if (index < 0 || index >= myList.size()) {
            return; // index out of scope
        }
        //
        myList.remove(index);
        fireTableRowsDeleted(index, index);
    }
    
    public void updateRow(int index) {
        if (index < 0 || index >= myList.size()) {
            return; // index out of scope
        }
        fireTableRowsUpdated(index, index);
    }
    
    public Iterator<T> getRowIterator() {
        return myList.iterator();
    }
    
    public int indexOf(T cp) {
        return myList.indexOf(cp);
    }
    
    public List<T> getRowsList() {
        return myList;
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        TableColumn column = columnModel.getColumn(columnIndex);
        if (column != null) {
            TableCellEditor editor = column.getCellEditor();
            if (editor != null) {
                return true;
            }
        }
        return false;
    }
}

