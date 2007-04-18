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

package org.netbeans.modules.compapp.javaee.sunresources.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.compapp.javaee.sunresources.generated.sunresources13.Property;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author echou
 */
public class SunResourcePropertyEditor extends java.beans.PropertyEditorSupport {
    
    private List<Property> propertyList;
    
    /** Creates a new instance of SunResourcePropertyEditor */
    public SunResourcePropertyEditor(List<Property> propertyList) {
        this.propertyList = propertyList;
    }
    
    public boolean supportsCustomEditor() {
        return true;
    }
    
    public java.awt.Component getCustomEditor() {
        PropertyModel model = new PropertyModel();
        PropertyPanel panel = new PropertyPanel(model);
        return panel;
    }
    
    private class PropertyPanel extends JPanel {
        private PropertyModel model;
        private JTable table;
        
        public PropertyPanel(PropertyModel model) {
            super(new BorderLayout());
            this.setPreferredSize(new Dimension(400, 200));
            this.model = model;
            this.table = new JTable(model);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane tableScrollPane = new JScrollPane(table);
            PopupListener popListener = new PopupListener();
            table.addMouseListener(popListener);
            tableScrollPane.addMouseListener(popListener);
            add(tableScrollPane);
        }
        
        class PopupListener extends MouseAdapter {
            public void mousePressed(MouseEvent e) {
                createPopup(e);
            }
            public void mouseReleased(MouseEvent e) {
                createPopup(e);
            }
            private void createPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    JPopupMenu popup = new JPopupMenu();
                    JMenuItem addItem = new JMenuItem("Add");
                    addItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            addItem_performed();
                        }
                    });
                    JMenuItem deleteItem = new JMenuItem("Delete");
                    deleteItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            deleteItem_performed();
                        }
                    });
                    if (table.getSelectedRow() == -1 || model.getRowCount() == 0) {
                        deleteItem.setEnabled(false);
                    }
                    popup.add(addItem);
                    popup.addSeparator();
                    popup.add(deleteItem);
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
            
            private void addItem_performed() {
                JPanel addPropertyPanel = new JPanel();
                addPropertyPanel.setLayout(new GridLayout(2,2));
                JTextField nameField = new JTextField();
                nameField.setPreferredSize(new Dimension(100, 20));
                JTextField valueField = new JTextField();
                valueField.setPreferredSize(new Dimension(100, 20));
                addPropertyPanel.add(new JLabel("Property Name"));
                addPropertyPanel.add(nameField);
                addPropertyPanel.add(new JLabel("Property Value"));
                addPropertyPanel.add(valueField);
                String title = "Add Property";
                DialogDescriptor dd = new DialogDescriptor(
                    addPropertyPanel, 
                    title, 
                    true,
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.OK_OPTION,
                    DialogDescriptor.BOTTOM_ALIGN,
                    null,
                    null
                );
                Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
                dialog.setVisible(true);
                
                if (dd.getValue().equals(NotifyDescriptor.CANCEL_OPTION)) {
                    return;
                }
                model.addRow(nameField.getText(), valueField.getText());
            }
            
            private void deleteItem_performed() {
                model.deleteRow(table.getSelectedRow());
            }
        }
    }
    
    private class PropertyModel extends AbstractTableModel {
        private String[] columnNames = new String[] {
            "Property Name", "Property Value"
        };
        
        public int getRowCount() {
            return propertyList.size();
        }

        public int getColumnCount() {
            return 2;
        }
        
        public String getColumnName(int column) {
            return columnNames[column];
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Property p = propertyList.get(rowIndex);
            if (columnIndex == 0) {
                return p.getName();
            } else {
                return p.getValue();
            }
        }
        
        public boolean isCellEditable(int row, int column) {
            if (column == 1) {
                return true;
            } else {
                return false;
            }
        }
        
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex != 1 || !(aValue instanceof String)) {
                return;
            }
            Property p = propertyList.get(rowIndex);
            p.setValue((String) aValue);
            this.fireTableDataChanged();
        }
        
        public void addRow(String name, String value) {
            Property p = new Property();
            p.setName(name);
            p.setValue(value);
            propertyList.add(p);
            this.fireTableDataChanged();
        }
        
        public void deleteRow(int row) {
            propertyList.remove(row);
            this.fireTableDataChanged();
        }
    }
}
