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
import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.JTable;

/**<Notification Description>
 *
 * @author alex
 */
public class ComboBoxRenderer extends  DefaultTableCellRenderer {
	
        JComboBox comp;
        Object obj;
	
        public ComboBoxRenderer(JComboBox comp) {
	    this.comp = comp;
	}

	public Component getTableCellRendererComponent(JTable table,
						       Object value,
						       boolean isSelected,
						       boolean hasFocus,
						       int row,
						       int column) {
            obj = table.getModel().getValueAt(row,column);
            comp.setSelectedItem(obj);
	    return comp;
	}

	public Component getComponent() {
	    return comp;
	}
}
