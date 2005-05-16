/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.jmx.mbeanwizard.table;
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.mbeanwizard.editor.JComboBoxCellEditor;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanNotificationTableModel;
import org.netbeans.modules.jmx.mbeanwizard.editor.JTextFieldCellEditor;
import org.netbeans.modules.jmx.mbeanwizard.listener.DisplayPopupListener;
import org.netbeans.modules.jmx.mbeanwizard.renderer.ComboBoxRenderer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.jmx.mbeanwizard.editor.NotificationPanelEditor;
import org.netbeans.modules.jmx.mbeanwizard.renderer.NotificationPanelRenderer;


/**
 *
 * @author an156382
 */
public class NotificationTable extends JTable {
    
    private JPanel ancestorPanel = null;
     
    /** Creates a new instance of AttributeTable */
    public NotificationTable(JPanel ancestorPanel, AbstractTableModel model) {
        super(model);
        this.setRowHeight(25);
        this.setPreferredScrollableViewportSize(new Dimension(500, 70));
        this.setRowSelectionAllowed(true);
        this.setColumnSelectionAllowed(false);
        
        this.ancestorPanel = ancestorPanel;
    }
    
    public TableCellEditor getCellEditor(int row, int column) {
        
        if(row >= getRowCount())
            return null;
        
        final JTable table = this;
        final MBeanNotificationTableModel model = (MBeanNotificationTableModel)this.getModel();
        
        if (column == 0) {
            JComboBox nameField = new JComboBox();
            nameField.addItem(WizardConstants.NOTIFICATION);
            nameField.addItem(WizardConstants.ATTRIBUTECHANGE_NOTIFICATION);
            nameField.setName("notifClassBox");
            nameField.setEditable(true);
            nameField.setEnabled(true);
            Object o = getModel().getValueAt(row,column);
            nameField.setSelectedItem(o);
            
            nameField.addItemListener(new ItemListener() {
                
                public void itemStateChanged(ItemEvent evt) {
                    
                    if (evt.getItem().toString().equals(WizardConstants.ATTRIBUTECHANGE_NOTIFICATION)) {
                        if (table.getSelectedRow() != -1) 
                            model.setValueAt(WizardConstants.NOTIF_TYPE_ATTRIBUTE_CHANGE,
                                    table.getSelectedRow(), model.IDX_NOTIF_TYPE);
                    } else {
                        model.setValueAt(WizardConstants.NOTIF_TYPE_DEFVALUE,
                                table.getSelectedRow(), model.IDX_NOTIF_TYPE);
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            model.fireTableDataChanged();
                        }
                    });
                }
            });
          
            return new JComboBoxCellEditor(nameField, this);
        } else {
            if (column == 1) {
                JTextField descrField = new JTextField();
                String o = ((String)getModel().getValueAt(row,column));
                descrField.setText(o);
                return new JTextFieldCellEditor(descrField, this);
            } else {
                if (column == 2) {
                    JTextField typeField = new JTextField();
                    typeField.setEditable(false);
                    JButton typeButton = new JButton(WizardConstants.MBEAN_POPUP_EDIT_BUTTON);
                    typeButton.setName("notifTypePopupJButton");
                    typeButton.setMargin(new java.awt.Insets(2,2,2,2));
                    typeButton.addActionListener(
                            new DisplayPopupListener(ancestorPanel, this, model,
                                                     typeField));
                    
                    String o = ((String)getModel().getValueAt(row,column));
                    if (o.equals(WizardConstants.NOTIF_TYPE_ATTRIBUTE_CHANGE)) {
                        typeButton.setEnabled(false);
                        typeField.setEditable(false);
                    }
                    
                    JPanel panel = new JPanel(new BorderLayout());
                    panel.add(typeField, BorderLayout.CENTER);
                    panel.add(typeButton, BorderLayout.EAST);
                    return new NotificationPanelEditor(panel, typeField, typeButton);
                }
            }
            return super.getCellEditor(row,column);
        }
    }
    
    public TableCellRenderer getCellRenderer(int row, int column) {
        
        if(row >= getRowCount())
                return null;
        
            if (column == 0) {
                JComboBox nameField = new JComboBox();
                nameField.setEnabled(true);
                nameField.setEditable(true);
                nameField.addItem(WizardConstants.NOTIFICATION);
                nameField.addItem(WizardConstants.ATTRIBUTECHANGE_NOTIFICATION);
                return new ComboBoxRenderer(nameField);
            } else {
                if (column == 2) {
                   JTextField typeField = new JTextField();
                   typeField.setEditable(false);
                   JButton typeButton = new JButton(WizardConstants.MBEAN_POPUP_EDIT_BUTTON);
                   typeButton.setMargin(new java.awt.Insets(2,2,2,2));
                   String o = ((String)getModel().getValueAt(row,column));
                    if (o.equals(WizardConstants.NOTIF_TYPE_ATTRIBUTE_CHANGE)) {
                        typeButton.setEnabled(false);
                        typeField.setEditable(false);
                    }
                   
                   JPanel panel = new JPanel(new BorderLayout());
                   panel.add(typeField, BorderLayout.CENTER);
                   panel.add(typeButton, BorderLayout.EAST);
                   return new NotificationPanelRenderer(panel, typeField);
                }
            }
        return super.getCellRenderer(row,column);
    }
}
