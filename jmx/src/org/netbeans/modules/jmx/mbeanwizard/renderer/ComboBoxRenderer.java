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

/**
 * Class managing the rendering for the combo boxes
 * 
 */
public class ComboBoxRenderer extends  DefaultTableCellRenderer {
	
    /*******************************************************************/
    // here, the model is not typed because more than one table uses it
    // i.e we have to call explicitely the model's internal structure
    // via getValueAt and setValueAt
    /********************************************************************/
    
        private JComboBox comp;
        private Object obj;
        private boolean isEnabled;
	private boolean isEditable = false;
        
        /**
         * Constructor
         * @param comp the combo box to affect the renderer to
         */
        public ComboBoxRenderer(JComboBox comp) {
	    this.comp = comp;
            this.isEnabled = true;
	}
        
        /**
         * Constructor
         * @param comp the combo box to affect the renderer to
         */
        public ComboBoxRenderer(JComboBox comp, boolean isEnabled) {
	    this.comp = comp;
            this.isEnabled = isEnabled;
	}
        
        /**
         * Constructor
         * @param comp the combo box to affect the renderer to
         */
        public ComboBoxRenderer(JComboBox comp, boolean isEnabled, 
                boolean isEditable) {
	    this.comp = comp;
            this.isEnabled = isEnabled;
            this.isEditable = isEditable;
	}

        /**
         * Method returning the modified component (component + rendering)
         * @param table the table in which the component is contained
         * @param value the value of the component
         * @param isSelected true if the component is selected
         * @param hasFocus true if the component has the focus
         * @param row the row of the component in the table
         * @param column the column of the component in the table
         * @return Component the modified component
         */
	public Component getTableCellRendererComponent(JTable table,
						       Object value,
						       boolean isSelected,
						       boolean hasFocus,
						       int row,
						       int column) {
            obj = table.getModel().getValueAt(row,column);
            comp.setSelectedItem(obj);
            comp.setEnabled(isEnabled);
            comp.setEditable(isEditable);
            
            // makes visual line selection possible
            if (row == table.getSelectedRow()) {
                // set editable false to have the good look when selected
                comp.setEditable(false);
                comp.setBackground(table.getSelectionBackground());
            }
            
            return comp;
	}

        /**
         * Returns simply the component
         * @return Component the component
         */
	public Component getComponent() {
	    return comp;
	}
}
