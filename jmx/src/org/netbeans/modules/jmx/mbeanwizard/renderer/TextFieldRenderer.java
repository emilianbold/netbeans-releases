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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author an156382
 */
public class TextFieldRenderer extends  DefaultTableCellRenderer {
    
    protected JTextField comp;
    protected Object obj;
    
    protected boolean isEnabled;
    protected boolean isEditable;
    
    /** Creates a new instance of WrapperTextFieldRenderer */
    public TextFieldRenderer(JTextField jtf) {
        this.comp = jtf;
    }
    
    public Component getTableCellRendererComponent(JTable table,
						       Object value,
						       boolean isSelected,
						       boolean hasFocus,
						       int row,
						       int column) {
            comp.setEditable(false);
	    return comp;
	}

        /**
         * Returns simply the component
         * @return Component the component
         */
//	public Component getComponent() {
//	    return comp;
//	}
}
