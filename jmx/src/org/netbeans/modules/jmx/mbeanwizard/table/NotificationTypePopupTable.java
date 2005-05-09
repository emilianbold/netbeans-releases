/*
 * NotificationTypePopupTable.java
 *
 * Created on April 4, 2005, 1:41 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.jmx.mbeanwizard.table;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import org.netbeans.modules.jmx.mbeanwizard.editor.JTextFieldCellEditor;
import java.awt.Dimension;

/**
 *
 * @author an156382
 */
public class NotificationTypePopupTable extends JTable {
    
    
    /** Creates a new instance of AttributeTable */
    public NotificationTypePopupTable(AbstractTableModel model) {
        super(model);
        this.setRowHeight(25);
        this.setPreferredScrollableViewportSize(new Dimension(250, 70));
        this.setRowSelectionAllowed(true);
        this.setColumnSelectionAllowed(false);
    }
    
    public TableCellEditor getCellEditor(int row, int column) {
        
        if(row >= getRowCount())
            return null;
        
        final JTextField typeField = new JTextField();
        String o = ((String)getModel().getValueAt(row,column));
        typeField.setText(o);
        return new JTextFieldCellEditor(typeField, this);
    }
     
}
