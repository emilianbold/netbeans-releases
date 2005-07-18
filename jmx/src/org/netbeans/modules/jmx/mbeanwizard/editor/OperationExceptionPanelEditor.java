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
import javax.swing.table.TableCellEditor;
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ChangeEvent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;

import java.awt.Component;
import java.util.ArrayList;
import org.netbeans.modules.jmx.MBeanOperationException;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanMethodTableModel;



/**
 * Class which handels the behaviour of the panel which popups the exception
 * window
 *
 */
public class OperationExceptionPanelEditor implements TableCellEditor {
    
    /*******************************************************************/
    // here, the model is not typed because more than one table uses it
    // i.e we have to call explicitely the model's internal structure
    // via getValueAt and setValueAt
    /********************************************************************/
    
    private MBeanMethodTableModel model;
    private JPanel thisPanel;
    private JTextField text;
    private int editingRow;
    
    protected EventListenerList listenerList = new EventListenerList();
    protected ChangeEvent changeEvent = new ChangeEvent(this);
    
    /**
     * Constructor
     * @param panel the panel containing the textfield and the popup button
     * @param jTextField the textfield
     * @param model the model of the operation table
     * @param editingRow the current edited row in the table
     */
    public OperationExceptionPanelEditor(MBeanMethodTableModel model, 
            JPanel panel, JTextField jTextField,
            int editingRow) {
        this.model = model;
        this.thisPanel = panel;
        this.text = jTextField;
        this.editingRow = editingRow;
    }
    
    /**
     * Overriden method; called eached time the component gets in the 
     * editor mode
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
        
        ArrayList<MBeanOperationException> oText = 
                (ArrayList<MBeanOperationException>)
                    table.getModel().getValueAt(row, column);
        String excepClassString = "";
        for (int i = 0; i < oText.size(); i++) {
            excepClassString += oText.get(i).getExceptionClass();
            
            if (i < oText.size()-1)
                excepClassString += ",";
        }
        text.setText(excepClassString);
        
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
        return model.getOperation(editingRow).getExceptionsList();
    }
    
}
