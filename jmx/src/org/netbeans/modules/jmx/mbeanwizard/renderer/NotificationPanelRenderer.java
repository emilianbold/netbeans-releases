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
public class NotificationPanelRenderer extends  DefaultTableCellRenderer {
	
        private JPanel comp;
        private JTextField text;
        
	public NotificationPanelRenderer(JPanel comp, JTextField text) {
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
