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
import org.netbeans.modules.jmx.mbeanwizard.renderer.ComboBoxRenderer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.jmx.mbeanwizard.editor.NotificationPanelEditor;
import org.netbeans.modules.jmx.MBeanNotificationType;
import org.netbeans.modules.jmx.mbeanwizard.popup.NotificationTypePopup;
import org.netbeans.modules.jmx.mbeanwizard.renderer.NotificationPanelRenderer;


/**
 * Class responsible for the notification table in the Notification Panel
 *
 */
public class NotificationTable extends JTable {
    
    /*******************************************************************/
    // here we use raw model calls (i.e getValueAt and setValueAt) to
    // access the model data because the inheritance pattern
    // makes it hard to type these calls and to use the object model
    /********************************************************************/
    
    private JPanel ancestorPanel = null;
    
    /**
     * Constructor
     * @param ancestorPanel the parent panel
     * @param model the table model of this table
     */
    public NotificationTable(JPanel ancestorPanel, AbstractTableModel model) {
        super(model);
        this.setRowHeight(25);
        this.setPreferredScrollableViewportSize(new Dimension(500, 70));
        this.setRowSelectionAllowed(true);
        this.setColumnSelectionAllowed(false);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.ancestorPanel = ancestorPanel;
    }
    
    /**
     * Returns the cell editor for the table according to the column
     * @param row the row to be considered
     * @param column the column to be considered
     * @return TableCellEditor the cell editor
     */
    public TableCellEditor getCellEditor(final int row, int column) {
        
        if(row >= getRowCount())
            return null;
        
        final JTable table = this;
        final MBeanNotificationTableModel model = 
                (MBeanNotificationTableModel)this.getModel();
        
        if (column == 0) {
            JComboBox nameField = new JComboBox();
            nameField.addItem(WizardConstants.NOTIFICATION);
            nameField.addItem(WizardConstants.ATTRIBUTECHANGE_NOTIFICATION);
            nameField.setName("notifClassBox");// NOI18N
            nameField.setEditable(true);
            nameField.setEnabled(true);
            Object o = getModel().getValueAt(row,column);
            nameField.setSelectedItem(o);
            final org.netbeans.modules.jmx.MBeanNotification notif = 
                    (org.netbeans.modules.jmx.MBeanNotification) model.getNotification(row);
            nameField.addItemListener(new ItemListener() {
                private String notifType;
          
                public void itemStateChanged(ItemEvent evt) {
                    if (evt.getStateChange() == evt.DESELECTED)
                        notifType = evt.getItem().toString();
                    else {
                        ArrayList<MBeanNotificationType> array  =
                                new ArrayList<MBeanNotificationType>();

                        if (evt.getItem().toString().equals(
                                WizardConstants.ATTRIBUTECHANGE_NOTIFICATION)) {
                            array.add(new MBeanNotificationType(
                                    WizardConstants.NOTIF_TYPE_ATTRIBUTE_CHANGE));
                            notif.setNotificationTypeList(array);
                        } else if (notifType.equals(
                                WizardConstants.ATTRIBUTECHANGE_NOTIFICATION)) {
                            notif.setNotificationTypeList(array);
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                model.fireTableDataChanged();
                            }
                        });
                    }
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
                    final JTextField typeField = new JTextField();
                    typeField.setEditable(false);
                    typeField.setName("typeTextField");// NOI18N
                    JButton typeButton = new JButton(
                            WizardConstants.MBEAN_POPUP_EDIT_BUTTON);
                    typeButton.setName("notifTypePopupJButton");// NOI18N
                    typeButton.setMargin(new java.awt.Insets(2,2,2,2));
                    typeButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            NotificationTypePopup notifPopup = 
                                    new NotificationTypePopup(
                                    ancestorPanel, model, typeField, row);
                        }
                    });
                    
                    //gets all the notification types of the current 
                    //notification
                    ArrayList<MBeanNotificationType> notifType = 
                            (ArrayList<MBeanNotificationType>)
                                    getModel().getValueAt(row,column);
                    
                    // if the notif type list size contains only one element 
                    // and this
                    // element is Attribute Change the button and the textfield
                    // are disabled
                    if ((notifType.size() == 1) &&
                            (notifType.get(0).getNotificationType().equals(
                            WizardConstants.NOTIF_TYPE_ATTRIBUTE_CHANGE))) {
                        typeButton.setEnabled(false);
                        typeField.setEditable(false);
                    }
                    
                    JPanel panel = new JPanel(new BorderLayout());
                    panel.add(typeField, BorderLayout.CENTER);
                    panel.add(typeButton, BorderLayout.EAST);
                    return new NotificationPanelEditor(model, panel, 
                            typeField, typeButton, row);
                }
            }
            return super.getCellEditor(row,column);
        }
    }
    
    /**
     * Returns the cell renderer for the table according to the column
     * @param row the row to be considered
     * @param column the column to be considered
     * @return TableCellRenderer the cell renderer
     */
    public TableCellRenderer getCellRenderer(int row, int column) {
        
        if(row >= getRowCount())
            return null;
        
        if (column == 0) {
            JComboBox nameField = new JComboBox();
            nameField.addItem(getModel().getValueAt(row,column));
            return new ComboBoxRenderer(nameField,true,true);
        } else {
            if (column == 2) {
                JTextField typeField = new JTextField();
                typeField.setEditable(false);
                typeField.setName("typeTextField");// NOI18N
                JButton typeButton = 
                        new JButton(WizardConstants.MBEAN_POPUP_EDIT_BUTTON);
                typeButton.setMargin(new java.awt.Insets(2,2,2,2));
                
                //gets all the notification types of the current notification
                ArrayList<MBeanNotificationType> notifType = 
                        (ArrayList<MBeanNotificationType>)
                getModel().getValueAt(row,column);
                
                // if the notif type list size contains only one element 
                // and this
                // element is Attribute Change the button and the textfield 
                // are disabled
                if ((notifType.size() == 1) &&
                        (notifType.get(0).getNotificationType().equals(
                        WizardConstants.NOTIF_TYPE_ATTRIBUTE_CHANGE))) {
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
