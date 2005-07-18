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
package org.netbeans.modules.jmx.mbeanwizard.editor;
import org.netbeans.modules.jmx.WizardConstants;
import javax.swing.table.TableCellEditor;
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ChangeEvent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;

import java.awt.Component;
import java.util.ArrayList;
import javax.swing.JButton;
import org.netbeans.modules.jmx.MBeanNotificationType;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanNotificationTableModel;



/**
 * Class which handels the behaviour of the panel which popups the notification
 * type window
 *
 */
public class NotificationPanelEditor implements TableCellEditor {
    
    /*******************************************************************/
    // here, the model is not typed because more than one table uses it
    // i.e we have to call explicitely the model's internal structure
    // via getValueAt and setValueAt
    /********************************************************************/
    
    private JPanel thisPanel;
    private JTextField text;
    private JButton button;
    private int editingRow;
    private MBeanNotificationTableModel model;
    
    protected EventListenerList listenerList = new EventListenerList();
    protected ChangeEvent changeEvent = new ChangeEvent(this);
    
    /**
     * Constructor
     * @param panel the panel containing the textfield and the popup button
     * @param jTextField the textfield
     * @param button the popup button
     * @param editingRow the current edited row in the table
     * @param model the notification table model
     */
    public NotificationPanelEditor(MBeanNotificationTableModel model, 
            JPanel panel,
            JTextField jTextField, JButton button, int editingRow) {
        this.thisPanel = panel;
        this.model = model;
        this.text = jTextField;
        this.button = button;
        this.editingRow = editingRow;
    }
    
    /**
     * Overriden method; called eached time the component gets in the editor 
     * mode
     * @param table the JTable in which the component is in
     * @param value the object with the current value
     * @param isSelected boolean indicating whether the component is selected or not
     * @param row the selected row in the table
     * @param column the selected column in the table
     * @return Component the modified component
     */
    public Component getTableCellEditorComponent(JTable table, Object value, 
            boolean isSelected,
            int row, int column) {
        
        ArrayList<MBeanNotificationType> oText = 
                (ArrayList<MBeanNotificationType>)
                        table.getModel().getValueAt(row, column);
        String notifTypeString = "";
        for (int i = 0; i < oText.size(); i++) {
            notifTypeString += oText.get(i).getNotificationType();
            
            if (i < oText.size()-1)
                notifTypeString += ",";
        }
        text.setText(notifTypeString);
        
        if (notifTypeString.equals
                (WizardConstants.NOTIF_TYPE_ATTRIBUTE_CHANGE)) {
            button.setEnabled(false);
        }
        
        return thisPanel;
    }
    
    /**
     * Adds a listener to the listener list
     * @param listener a CellEditorListener
     */
    public void addCellEditorListener(CellEditorListener listener) {
        listenerList.add(CellEditorListener.class,listener);
    }
    
    /**
     * Removes a listener from the listener list
     * @param listener a CellEditorListener
     */
    public void removeCellEditorListener(CellEditorListener listener) {
        listenerList.remove(CellEditorListener.class, listener);
    }
    
    /**
     * Fires an event when the editing on the cell editor stops
     */
    protected void fireEditingStopped() {
        CellEditorListener listener;
        Object[] listeners = listenerList.getListenerList();
        for (int i=0;i< listeners.length;i++) {
            if (listeners[i] == CellEditorListener.class) {
                listener = (CellEditorListener) listeners[i+1];
                listener.editingStopped(changeEvent);
            }
        }
    }
    
    /**
     * fires an event when the editing is cancelled on the cell editor
     */
    protected void fireEditingCanceled() {
        CellEditorListener listener;
        Object[] listeners = listenerList.getListenerList();
        for (int i=0;i< listeners.length;i++) {
            if (listeners[i] == CellEditorListener.class) {
                listener = (CellEditorListener) listeners[i+1];
                listener.editingCanceled(changeEvent);
            }
        }
    }
    
    public void cancelCellEditing() {
        fireEditingCanceled();
    }
    
    public boolean stopCellEditing() {
        cancelCellEditing();
        return true;
    }
    
    public boolean isCellEditable(java.util.EventObject event) {
        return true;
    }
    
    public boolean shouldSelectCell(java.util.EventObject event) {
        return true;
    }
    
    public Object getCellEditorValue() {
        return model.getNotification(editingRow).getNotificationTypeList();
    }
    
}
