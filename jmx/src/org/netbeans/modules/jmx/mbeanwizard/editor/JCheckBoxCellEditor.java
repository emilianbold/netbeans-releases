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

import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableModel;

/**
 *
 * @author an156382
 */
public class JCheckBoxCellEditor extends DefaultCellEditor {
    
    private JCheckBox tf;
    private JTable table;
    private TableModel model;
    
    /** Creates a new instance of JCheckBoxCellEditor */
    public JCheckBoxCellEditor(JCheckBox tf, JTable table) {
        super(tf);
        this.tf = tf;
        this.table = table;
        this.model = table.getModel();
        //((AbstractJMXTableModel)this.model).addTableRemoveListener(this);
    }
    
    public Component getTableCellEditorComponent(JTable table,
            Object value, boolean isSelected, int row, int column) {
        //tf.setSelectedItem(model.getValueAt(row,column).toString());
        tf.setSelected((Boolean)model.getValueAt(row,column));
        return tf;
    }
}
