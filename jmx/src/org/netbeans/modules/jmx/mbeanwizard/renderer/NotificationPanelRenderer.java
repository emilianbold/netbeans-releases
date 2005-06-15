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
import java.util.ArrayList;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.JPanel;
import org.netbeans.modules.jmx.mbeanwizard.mbeanstructure.MBeanNotificationType;


/**
 * Class managing the rendering for the panel responsible for the
 * notification popup display and result textfield
 *
 */
public class NotificationPanelRenderer extends  DefaultTableCellRenderer {
    
    /*******************************************************************/
    // here, the model is not typed because more than one table uses it
    // i.e we have to call explicitely the model's internal structure
    // via getValueAt and setValueAt
    /********************************************************************/
    
    private JPanel comp;
    private JTextField text;
    
    /**
     * Constructor
     * @param comp the panel containing the popup button and the result 
     * text field
     * @param text the result text field
     */
    public NotificationPanelRenderer(JPanel comp, JTextField text) {
        this.comp = comp;
        this.text = text;
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
        
        ArrayList<MBeanNotificationType> oText = 
                (ArrayList<MBeanNotificationType>)
                    table.getModel().getValueAt(row, column);
        String notifTypeString = "";
        for (int i = 0; i < oText.size(); i++) {
            notifTypeString += oText.get(i).getNotificationType();
            
            if (i < oText.size()-1)
                notifTypeString += ",";
        }
        text.setText(notifTypeString);
        
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
