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

import javax.swing.table.DefaultTableCellRenderer;
import org.netbeans.modules.jmx.WizardConstants;
import java.awt.Component;
import java.awt.BorderLayout;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.JButton;

/**
 *
 * @author alex
 */
public class OperationPanelRenderer extends  DefaultTableCellRenderer {
	
        private JPanel comp;
        private JTextField text;

	public OperationPanelRenderer(JPanel comp, JTextField text) {
	    this.comp = comp;
            this.text = text;
	}
       
	public Component getTableCellRendererComponent(JTable table,
						       Object value,
						       boolean isSelected,
						       boolean hasFocus,
						       int row,
						       int column) {
            
            String oText = (String) table.getModel().getValueAt(row, column);
            text.setText(oText);
           
	    return comp;
	}

	public Component getComponent() {
	    return comp;
	}
}
