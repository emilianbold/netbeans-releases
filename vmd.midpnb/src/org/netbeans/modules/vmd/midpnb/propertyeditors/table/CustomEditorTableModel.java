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
package org.netbeans.modules.vmd.midpnb.propertyeditors.table;

import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Anton Chechel
 */
class CustomEditorTableModel extends DefaultTableModel {

    private Vector<String> header = new Vector<String>();
    private boolean hasHeader;

    public void removeLastColumn() {
        if (hasHeader && header.size() > 0) {
            header.remove(header.size() - 1);
        }

        int columnCount = getColumnCount();
        if (columnCount > 0) {
            int size = dataVector.size();
            for (int i = 0; i < size; i++) {
                Vector row = (Vector) dataVector.elementAt(i);
                row.remove(columnCount - 1);
            }
        }
        fireTableStructureChanged();
    }

    @Override
    public void removeRow(int row) {
        dataVector.removeElementAt(row);
        fireTableStructureChanged();
    }

    @Override
    @SuppressWarnings(value = "unchecked") // NOI18N
    public void addRow(Object[] rowData) {
        dataVector.addElement(convertToVector(rowData));
        fireTableStructureChanged();
    }

    @SuppressWarnings(value = "unchecked") // NOI18N
    public void addColumn(String columnName, boolean needRow) {
        if (hasHeader) {
            header.addElement(columnName);
        }

        if (dataVector.size() > 0) {
            for (int i = 0; i < dataVector.size(); i++) {
                Vector row = (Vector) dataVector.elementAt(i);
                row.addElement(columnName);
            }
        } else if (needRow) {
            Vector row = new Vector(1);
            row.add(columnName);
            dataVector.addElement(row);
        }
        fireTableStructureChanged();
    }

    @Override
    public int getRowCount() {
        return dataVector.size() + (hasHeader ? 1 : 0);
    }

    @Override
    public int getColumnCount() {
        if (hasHeader) {
            return header.size();
        } else if (dataVector.size() > 0) {
            return ((Vector) dataVector.get(0)).size();
        }
        return 0;
    }

    @Override
    public Object getValueAt(int row, int column) {
        Object value;
        if (hasHeader) {
            if (row == 0) {
                value = header.elementAt(column);
            } else {
                value = super.getValueAt(row - 1, column);
            }
        } else {
            value = super.getValueAt(row, column);
        }
        return value;
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        if (hasHeader) {
            if (row == 0) {
                header.setElementAt((String) aValue, column);
                fireTableStructureChanged();
            } else {
                super.setValueAt(aValue, row - 1, column);
            }
        } else {
            super.setValueAt(aValue, row, column);
        }
    }

    @Override
    public void setDataVector(Object[][] dataArrays, Object[] columnArray) {
        if (hasHeader) {
            header.clear();
            for (int i = 0; i < columnArray.length; i++) {
                header.addElement((String) columnArray[i]);
            }
        }
        dataVector = nonNullVector(convertToVector(dataArrays));
        fireTableStructureChanged();
    }

    private static Vector nonNullVector(Vector v) {
        return (v != null) ? v : new Vector();
    }

    public void clear() {
        dataVector.clear();
        fireTableStructureChanged();
    }

    public void setUseHeader(boolean useHeader) {
        if (useHeader && header.size() != getColumnCount()) {
            header.clear();
            for (int i = 0; i < getColumnCount(); i++) {
                header.addElement(""); // NOI18N
            }
        }
        this.hasHeader = useHeader;
        fireTableStructureChanged();
    }

    public boolean hasHeader() {
        return hasHeader;
    }

    public Vector<String> getHeader() {
        return header;
    }
}