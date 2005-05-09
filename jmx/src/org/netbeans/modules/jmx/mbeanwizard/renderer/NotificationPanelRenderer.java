/*
 * PanelRenderer.java
 *
 * Created on 18 février 2005, 18:51
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
