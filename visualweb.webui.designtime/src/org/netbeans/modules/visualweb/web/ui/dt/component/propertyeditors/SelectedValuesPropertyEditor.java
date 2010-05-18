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

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.propertyeditors.util.JavaInitializer;
import org.netbeans.modules.visualweb.web.ui.dt.component.util.DesignMessageUtil;
import com.sun.rave.web.ui.model.MultipleSelectOptionsList;
import com.sun.rave.web.ui.model.Option;
import java.util.ArrayList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.netbeans.modules.visualweb.propertyeditors.TabularPropertyEditor;
import org.netbeans.modules.visualweb.propertyeditors.TabularPropertyModel;

/**
 * An editor for properties that specify the options that are "selected" among
 * a list of options. An option is "selected" if the value of its
 * <code>value</code> property is equal to one of the objects in the selected
 * property.
 *
 * @author gjmurphy
 */
public class SelectedValuesPropertyEditor extends TabularPropertyEditor {

    public SelectedValuesPropertyEditor() {
    }

    private SelectedValuesTableModel tableModel;

    protected TabularPropertyModel getTabularPropertyModel() {
        if (tableModel == null)
            tableModel = new SelectedValuesTableModel();
        tableModel.setDesignProperty(this.getDesignProperty());
        return tableModel;
    }

    public String getJavaInitializationString() {
        StringBuffer buffer = new StringBuffer();
        Object value = this.getValue();
        if (value instanceof Object[]) {
            buffer.append("new Object[] {");
            Object[] values = (Object[]) value;
            for (int i = 0; i < values.length; i++) {
                if (i > 0)
                    buffer.append(", ");
                buffer.append(JavaInitializer.toJavaInitializationString(values[i]));
            }
            buffer.append("}");
        } else {
            buffer.append(JavaInitializer.toJavaInitializationString(value));
        }
        return buffer.toString();
    }

    public String getAsText() {
        StringBuffer buffer = new StringBuffer();
        Object value = this.getValue();
        if (value instanceof Object[]) {
            Object[] values = (Object[]) value;
            for (int i = 0; i < values.length; i++) {
                if (i > 0)
                    buffer.append(", ");
                buffer.append(values[i].toString());
            }
        } else if (value != null) {
            buffer.append(value.toString());
        }
        return buffer.toString();
    }

    static final String[] columnNames = new String[] {
        DesignMessageUtil.getMessage(OptionsPropertyEditor.class, "Options.label"), //NOI18N
                DesignMessageUtil.getMessage(OptionsPropertyEditor.class, "Options.value"), //NOI18N
                DesignMessageUtil.getMessage(OptionsPropertyEditor.class, "Options.selected") //NOI18N
    };

    class SelectedValuesTableModel implements TabularPropertyModel {

        final int labelIndex = 0;
        final int valueIndex = 1;
        final int selectedIndex = 2;

        DesignProperty designProperty;
        // List of the component options from which selections are made
        ArrayList options;
        // List of boolean values that reflected selected state of the options
        ArrayList selected;
        // If true, component allows multiple selections
        boolean isMultiple;

        public void setValue(Object value) {
            options = new ArrayList();
            selected = new ArrayList();
            if (designProperty != null) {
                DesignBean bean = designProperty.getDesignBean();
                Option[] opts = (Option[]) bean.getProperty("options").getValue();
                DesignProperty multipleProperty = bean.getProperty("multiple");
                if ((multipleProperty != null && Boolean.TRUE.equals(multipleProperty.getValue())) ||
                        bean.getInstance() instanceof MultipleSelectOptionsList) {
                    Object[] values = value == null ? new Object[0] : (Object[]) value;
                    isMultiple = true;
                    for (int i = 0; i < opts.length; i++) {
                        options.add(opts[i]);
                        selected.add(Boolean.FALSE);
                        for (int j = 0; j < values.length; j++) {
                            if (opts[i].getValue().equals(values[j]))
                                selected.set(i, Boolean.TRUE);
                        }
                    }
                } else {
                    isMultiple = false;
                    for (int i = 0; i < opts.length; i++) {
                        options.add(opts[i]);
                        if (opts[i].getValue().equals(value))
                            selected.add(Boolean.TRUE);
                        else
                            selected.add(Boolean.FALSE);
                    }
                }
            }
        }
        
        public Object getValue() {
            if (selected == null)
                return null;
            if (isMultiple) {
                ArrayList values = new ArrayList();
                for (int i = 0; i < selected.size(); i++) {
                    if (Boolean.TRUE.equals(selected.get(i)))
                        values.add(((Option) options.get(i)).getValue());
                }
                return values.toArray(new Object[values.size()]);
            } else {
                for (int i = 0; i < selected.size(); i++) {
                    if (Boolean.TRUE.equals(selected.get(i)))
                        return ((Option) options.get(i)).getValue();
                }
            }
            return null;
        }
        
        ArrayList tableModelListenerList = new ArrayList();
        
        public void addTableModelListener(TableModelListener listener) {
            tableModelListenerList.add(listener);
        }
        
        public void removeTableModelListener(TableModelListener listener) {
            tableModelListenerList.remove(listener);
        }
        
        public void setDesignProperty(DesignProperty designProperty) {
            this.designProperty = designProperty;
        }
        
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex != selectedIndex)
                return false;
            if (rowIndex < 0 || rowIndex >= options.size())
                return false;
            return true;
        }
        
        public void setValueAt(Object newValue, int rowIndex, int columnIndex) {
            if (!isCellEditable(rowIndex, columnIndex))
                return;
            // If underlying property takes a list of values, update the list, and
            // set property to new state of list.
            if (isMultiple) {
                selected.set(rowIndex, newValue);
                ArrayList selectedValues = new ArrayList();
                for (int i = 0; i < options.size(); i++) {
                    if (Boolean.TRUE.equals(selected.get(i)))
                        selectedValues.add(((Option) options.get(i)).getValue());
                }
                //this.setValue(selectedValues.toArray());
            }
            // If underlying property takes a single value, and user has just deselected
            // the previously selected value, then set property value to null.
            else if(Boolean.FALSE.equals(newValue)) {
                selected.set(rowIndex, Boolean.FALSE);
                //this.setValue(null);
            }
            // If underlying property takes a single value, and user has just selected
            // a new option, deselect previously selected option, and set property value
            // to new option's value.
            else {
                for (int i = 0; i < selected.size(); i++) {
                    if (Boolean.TRUE.equals(selected.get(i))) {
                        selected.set(i, Boolean.FALSE);
                        //((TabularPropertyPanel) super.getCustomEditor()).updateTableData(i, selectedIndex);
                        for (int j = 0; j < tableModelListenerList.size(); j++) {
                            TableModelListener l = (TableModelListener) tableModelListenerList.get(j);
                            l.tableChanged(new TableModelEvent(this, i, i, selectedIndex));
                        }
                    }
                }
                selected.set(rowIndex, Boolean.TRUE);
                //this.setValue(((Option) options.get(rowIndex)).getValue());
            }
        }
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex >= 0 && rowIndex < options.size()) {
                if (columnIndex == labelIndex)
                    return ((Option) options.get(rowIndex)).getLabel();
                else if (columnIndex == valueIndex)
                    return ((Option) options.get(rowIndex)).getValue();
                else
                    return selected.get(rowIndex);
            }
            return null;
        }
        
        public Class getColumnClass(int columnIndex) {
            if (columnIndex == labelIndex)
                return String.class;
            else if (columnIndex == selectedIndex)
                return Boolean.class;
            else if (options.size() > 0 && options.get(0) != null)
                return ((Option) options.get(0)).getValue().getClass();
            else
                return Object.class;
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
            return false;
        }
        
        public boolean addRow() {
            return false;
        }
        
        public boolean canMoveRow(int indexFrom, int indexTo) {
            return false;
        }
        
        public boolean moveRow(int indexFrom, int indexTo) {
            return false;
        }
        
        public boolean canRemoveRow(int index) {
            return false;
        }
        
        public boolean removeRow(int index) {
            return false;
        }
        
        public boolean removeAllRows() {
            return false;
        }
        
    }
    
}
