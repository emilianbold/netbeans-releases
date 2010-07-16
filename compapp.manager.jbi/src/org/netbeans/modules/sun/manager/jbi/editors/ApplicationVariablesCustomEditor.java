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
package org.netbeans.modules.sun.manager.jbi.editors;

import java.awt.Component;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentConfigurationDescriptor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 * A custom editor for editing typed application variables.
 * A typed application variable is a triplet: [Name, Type, Value].
 * Four types are supported: STRING, NUMBER, BOOLEAN and PASSWORD.
 *
 * @author jqian
 */
public class ApplicationVariablesCustomEditor extends SimpleTabularDataCustomEditor {
        
    public static final int NAME_COLUMN = 0;
    public static final int TYPE_COLUMN = 1;
    public static final int VALUE_COLUMN = 2;
            
    public ApplicationVariablesCustomEditor(SimpleTabularDataEditor editor,
            String tableLabelText, String tableLabelDescription,
            JBIComponentConfigurationDescriptor descriptor,
            boolean isWritable) {
        super(editor, tableLabelText, tableLabelDescription, 
                descriptor, isWritable);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected Vector createRow() {
        ApplicationVariableTypeSelectionPanel typeSelectionPanel =
                new ApplicationVariableTypeSelectionPanel();
        
        DialogDescriptor dd = new DialogDescriptor(typeSelectionPanel,
                NbBundle.getMessage(ApplicationVariablesCustomEditor.class,
                "TTL_SELECT_APPLICATION_VARIALBE_TYPE")); // NOI18N
        typeSelectionPanel.requestFocus();
        DialogDisplayer.getDefault().notify(dd);
        
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            String type = typeSelectionPanel.getTypeChoice();
            Vector row = super.createRow();
            row.set(TYPE_COLUMN, type);
            
            // init default boolean value
            if (type.equals(ApplicationVariableType.BOOLEAN.toString())) {
                row.set(VALUE_COLUMN, Boolean.FALSE.toString()); 
            }
            
            return row;
        } else {
            return null;
        }
    }
    
    @Override
    protected JTable createTable(DefaultTableModel tableModel) {
        final Border myBorder = new EmptyBorder(1, 4, 1, 1);
        
        JTable table = new JTable(tableModel) {
            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                TableCellRenderer renderer = new DefaultTableCellRenderer() {
                    
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table,
                            Object value,
                            boolean isSelected,
                            boolean hasFocus,
                            int row, int column) {
                        
                        if (column == NAME_COLUMN) {
                            /*
                            // Highlight key columns
                            if (value != null) {
                                value = "<html><body><b>" + value + "</b></body></html>"; // NOI18N
                            }*/
                            Component component = super.getTableCellRendererComponent(
                                    table, value, isSelected, hasFocus, row, column);
                            ((JComponent)component).setBorder(myBorder);
                            return component;
                        } else {
                            DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
                            Vector rowData = (Vector) tableModel.getDataVector().get(row);
                            String type = (String) rowData.get(TYPE_COLUMN);
                            ApplicationVariableType avType = ApplicationVariableType.getType(type);
                            TableCellRenderer renderer = TabularDataCellRendererFactory.getRenderer(avType);                            
                            return renderer.getTableCellRendererComponent(
                                    table, value, isSelected, hasFocus, row, column);
                        }
                    }
                };
                return renderer;
            }
            
            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                if (!isWritable) {
                    return null;
                } else if (column == NAME_COLUMN) {
                    return TabularDataCellEditorFactory.getEditor(String.class);
                } else {
                    DefaultTableModel tableModel = (DefaultTableModel) getModel();
                    Vector rowData = (Vector) tableModel.getDataVector().get(row);
                    String type = (String) rowData.get(TYPE_COLUMN);
                    ApplicationVariableType avType = ApplicationVariableType.getType(type);
                    return TabularDataCellEditorFactory.getEditor(avType);
                }
            }
        };

        return table;
    }
    
    @Override
    protected void configureTableColumns(JTable table) {
        super.configureTableColumns(table); 
        
        // Hide the type column in the table
        TableColumnModel columnModel = table.getColumnModel();
        TableColumn typeColumn = columnModel.getColumn(TYPE_COLUMN);
        columnModel.removeColumn(typeColumn);
    }
    
    @Override
    protected TableCellRenderer createTableHeaderRenderer() {
        return new TabularDataTableHeaderRenderer() {
            // hide the type column
            @Override
            protected int getColumnIndex(int column) {
                return column == NAME_COLUMN ? NAME_COLUMN : VALUE_COLUMN;
            }
        };    
    }
}
