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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.propertyeditors;

import com.sun.rave.propertyeditors.util.JavaInitializer;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.event.TableModelListener;

/**
 * An editor for properties that take an array of Strings.
 *
 * @author gjmurphy
 */
public class StringArrayPropertyEditor extends TabularPropertyEditor implements
        com.sun.rave.propertyeditors.StringArrayPropertyEditor {

    private static ResourceBundle bundle =
            ResourceBundle.getBundle(StringArrayPropertyEditor.class.getPackage().getName() + ".Bundle"); //NOI18N

    private static String columnName = bundle.getString("StringArrayPropertyEditor.label");
    private static String columnValue = bundle.getString("StringArrayPropertyEditor.value");

    public StringArrayPropertyEditor() {
        super();
    }

    public String getJavaInitializationString() {
        StringBuffer buffer = new StringBuffer();
        Object value = getValue();
        if (!(value instanceof String[]))
            return null;
        String[] strings = (String[]) value;
        buffer.append("new String[] {");
        for (int i = 0; i < strings.length; i++) {
            if (i > 0)
                buffer.append(", ");
            buffer.append(JavaInitializer.toJavaInitializationString(strings[i]));
        }
        buffer.append("}");
        return buffer.toString();
    }

    private StringArrayTableModel tableModel;

    protected TabularPropertyModel getTabularPropertyModel() {
        if (tableModel == null)
            tableModel = new StringArrayTableModel();
        return tableModel;
    }


    class StringArrayTableModel implements TabularPropertyModel {

        ArrayList stringList;

        public StringArrayTableModel() {
            stringList = new ArrayList();
        }

        public void setValue(Object value) {
            stringList.clear();
            if (value != null && value instanceof String[]) {
                String[] values = (String[]) value;
                for (int i = 0; i < values.length; i++)
                    stringList.add(values[i]);
            }
        }

        public Object getValue() {
            String[] values = new String[stringList.size()];
            return stringList.toArray(values);
        }

        public void addTableModelListener(TableModelListener listener) {
        }

        public void removeTableModelListener(TableModelListener listener) {
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex != 0)
                return false;
            if (rowIndex < 0 || rowIndex >= stringList.size())
                return false;
            return true;
        }

        public void setValueAt(Object newValue, int rowIndex, int columnIndex) {
            if (isCellEditable(rowIndex, columnIndex)) {
                stringList.set(rowIndex, newValue);
            }
        }

        public Class getColumnClass(int columnIndex) {
            return String.class;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex >= 0 && rowIndex < stringList.size()) {
                return stringList.get(rowIndex);
            }
            return null;
        }

        public int getRowCount() {
            return stringList.size();
        }

        public String getColumnName(int columnIndex) {
            if (columnIndex == 0)
                return columnName;
            return null;
        }

        public int getColumnCount() {
            return 1;
        }

        public boolean canAddRow() {
            return true;
        }
        
        public boolean addRow() {
            stringList.add(columnValue);
            return true;
        }
        
        public boolean canMoveRow(int indexFrom, int indexTo) {
            if (indexFrom < 0 || indexTo < 0 || indexFrom == indexTo )
                return false;
            if (indexFrom >= stringList.size() || indexTo >= stringList.size())
                return false;
            return true;
        }
        
        public boolean moveRow(int indexFrom, int indexTo) {
            if (!canMoveRow(indexFrom, indexTo))
                return false;
            stringList.add(indexTo, stringList.remove(indexFrom));
            return true;
        }
        
        public boolean canRemoveRow(int index) {
            if (index < 0 || index >= stringList.size())
                return false;
            return true;
        }
        
        public boolean removeRow(int index) {
            if (!canRemoveRow(index))
                return false;
            stringList.remove(index);
            return true;
        }
        
        public boolean removeAllRows() {
            
            if( stringList.isEmpty() )
                return true;
            
            stringList.clear();
            return true;
        }
    }
    
}
