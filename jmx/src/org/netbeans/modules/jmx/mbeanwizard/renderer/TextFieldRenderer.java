/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
    
    /** Creates a new instance of TextFieldRenderer */
    public TextFieldRenderer(JTextField comp, boolean isEnabled,
            boolean isEditable) {
        this.comp = comp;
        this.isEnabled = isEnabled;
        this.isEditable = isEditable;
    }
    
    public Component getTableCellRendererComponent(JTable table,
						       Object value,
						       boolean isSelected,
						       boolean hasFocus,
						       int row,
						       int column) {
            //comp.setEditable(false);
            obj = table.getModel().getValueAt(row,column); 
            comp.setText((String)obj);
            comp.setEnabled(isEnabled);
            comp.setEditable(isEditable);
            
            /*
            // makes visual line selection possible
            if (row == table.getSelectedRow()) {
                comp.setBackground(table.getSelectionBackground());
            }
             */
            
	    return comp;
	}
}
