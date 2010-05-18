/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.visualweb.web.ui.dt.component.propertyeditors;

import com.sun.rave.propertyeditors.util.JavaInitializer;
import org.netbeans.modules.visualweb.web.ui.dt.component.util.DesignMessageUtil;
import com.sun.rave.web.ui.model.Option;
import java.util.ArrayList;
import javax.swing.event.TableModelListener;
import org.netbeans.modules.visualweb.propertyeditors.TabularPropertyEditor;
import org.netbeans.modules.visualweb.propertyeditors.TabularPropertyModel;

/**
 * An editor for properties that take lists of options, such as the
 * <code>items</code> property on all list-like components.
 *
 * @author gjmurphy
 */
public class OptionsPropertyEditor extends TabularPropertyEditor {

    public OptionsPropertyEditor() {
    }

    private OptionsTableModel tableModel;

    protected TabularPropertyModel getTabularPropertyModel() {
        if (tableModel == null)
            tableModel = new OptionsTableModel();
        return tableModel;
    }

    public String getJavaInitializationString() {
        Object value = this.getValue();
        if (!(value instanceof Option[]))
            return null;
        Option[] options = (Option[]) value;
        StringBuffer buffer = new StringBuffer();
        buffer.append("new " + Option.class.getName() + "[] {");
        for (int i = 0; i < options.length; i++) {
            if (i > 0)
                buffer.append(", ");
            buffer.append("new " + Option.class.getName() + "(");
            buffer.append(JavaInitializer.toJavaInitializationString(options[i].getValue()));
            buffer.append(", ");
            buffer.append(JavaInitializer.toJavaInitializationString(options[i].getLabel()));
            buffer.append(")");
        }
        buffer.append("}");
        return buffer.toString();
    }


    static final String[] columnNames = new String[] {
        DesignMessageUtil.getMessage(OptionsPropertyEditor.class, "Options.label"), //NOI18N
                DesignMessageUtil.getMessage(OptionsPropertyEditor.class, "Options.value") //NOI18N
    };

    static class OptionsTableModel implements TabularPropertyModel {

        final int labelIndex = 0;
        final int valueIndex = 1;

        ArrayList options;

        public void setValue(Object value) {
            if (value instanceof Option[]) {
                options = new ArrayList();
                Option[] values = (Option[]) value;
                for (int i = 0; i < values.length; i++)
                    options.add(new Option(values[i].getValue(), values[i].getLabel()));
            }
        }

        public Object getValue() {
            if (options == null)
                return null;
            return options.toArray(new Option[options.size()]);
        }

        public void addTableModelListener(TableModelListener listener) {
        }

        public void removeTableModelListener(TableModelListener listener) {
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex < 0 || columnIndex >= columnNames.length)
                return false;
            if (rowIndex < 0 || rowIndex >= options.size())
                return false;
            return true;
        }

        public void setValueAt(Object newValue, int rowIndex, int columnIndex) {
            if (isCellEditable(rowIndex, columnIndex)) {
                if (columnIndex == labelIndex)
                    ((Option) options.get(rowIndex)).setLabel(newValue.toString());
                else
                    ((Option) options.get(rowIndex)).setValue(newValue);
            }
        }

        public Class getColumnClass(int columnIndex) {
            if (columnIndex == labelIndex)
                return String.class;
            else if (options.size() > 0 && options.get(0) != null)
                return ((Option) options.get(0)).getValue().getClass();
            else
                return Object.class;
        }
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex >= 0 && rowIndex < options.size()) {
                if (columnIndex == labelIndex)
                    return ((Option) options.get(rowIndex)).getLabel();
                else
                    return ((Option) options.get(rowIndex)).getValue();
            }
            return null;
        }
        
        public int getRowCount() {
            return options.size();
        }
        
        public String getColumnName(int columnIndex) {
            return columnNames[columnIndex];
        }
        
        public int getColumnCount() {
            return columnNames.length;
        }
        
        public boolean canAddRow() {
            return true;
        }
        
        public boolean addRow() {
            options.add(new Option(columnNames[valueIndex], columnNames[labelIndex]));
            return true;
        }
        
        public boolean canMoveRow(int indexFrom, int indexTo) {
            if (indexFrom < 0 || indexTo < 0 || indexFrom == indexTo )
                return false;
            if (indexFrom >= options.size() || indexTo >= options.size())
                return false;
            return true;
        }
        
        public boolean moveRow(int indexFrom, int indexTo) {
            if (!canMoveRow(indexFrom, indexTo))
                return false;
            options.add(indexTo, options.remove(indexFrom));
            return true;
        }
        
        public boolean canRemoveRow(int index) {
            if (index < 0 || index >= options.size())
                return false;
            return true;
        }
        
        public boolean removeRow(int index) {
            if (!canRemoveRow(index))
                return false;
            options.remove(index);
            return true;
        }
        
        public boolean removeAllRows() {
            if( options.isEmpty() )
                return true;
            
            int numRows = options.size();
            options.clear();
            
            return true;
        }
    }
    
}
