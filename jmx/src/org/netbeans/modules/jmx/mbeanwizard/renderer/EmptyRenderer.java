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
