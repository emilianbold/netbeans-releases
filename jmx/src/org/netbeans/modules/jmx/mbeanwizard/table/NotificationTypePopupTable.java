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
