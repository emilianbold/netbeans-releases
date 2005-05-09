package org.netbeans.modules.jmx.mbeanwizard.renderer;
/*
 * ComboBoxRenderer.java
 *
 * Created on 18 février 2005, 18:51
 */

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
