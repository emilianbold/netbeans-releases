/*
 * EmptyRenderer.java
 *
 * Created on July 28, 2005, 2:44 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.jmx.mbeanwizard.renderer;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * As simple text field renderer which gives the appearence of an empty
 * non editable and disabled text field
 */
public class EmptyRenderer extends TextFieldRenderer {
    
    /** Creates a new instance of EmptyRenderer */
    public EmptyRenderer(JTextField jtf) {
        super(jtf);
    }
    
    public Component getTableCellRendererComponent(JTable table,
            Object value, boolean isSelected, boolean hasFocus,
	    int row, int column) {
            
            comp.setEnabled(false);
            comp.setEditable(false);
            
            // makes visual line selection possible
            if (row == table.getSelectedRow())
                comp.setBackground(table.getSelectionBackground());
            
	    return comp;
	}
    
}
