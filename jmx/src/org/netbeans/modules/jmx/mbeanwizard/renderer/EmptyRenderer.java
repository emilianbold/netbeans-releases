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
