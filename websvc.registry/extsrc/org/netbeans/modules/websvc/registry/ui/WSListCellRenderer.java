/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.registry.ui;

import javax.swing.*;
import java.awt.Component;

import org.openide.ErrorManager;

/**
 * Display an icon and a string for each object in the list
 * @author  Jeff Hoffman & Octavian Tanase
 */
public class WSListCellRenderer extends JLabel implements ListCellRenderer {
    private ImageIcon longIcon = null;
    
    // This is the only method defined by ListCellRenderer.
    // We just reconfigure the JLabel each time we're called.
    
    public Component getListCellRendererComponent(JList list,
    Object value,            // value to display
    int index,               // cell index
    boolean isSelected,      // is the cell selected
    boolean cellHasFocus) {  // the list and the cell have the focus
        
        String s = value.toString();
        setText(s);
        if (s.equals("<choose>")) {
            // nothing
        } else {
            java.net.URL url = getClass().getResource("/org/netbeans/modules/websvc/registry/resources/UDDIRegistry.gif");
            if (url != null) {
                longIcon = new javax.swing.ImageIcon(url);
			} else {
				ErrorManager.getDefault().log(ErrorManager.ERROR, "Error loading the resource " + this.toString());
			}
            
            setIcon(longIcon);
        }
        
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setEnabled(list.isEnabled());
        setFont(list.getFont());
        setOpaque(true);
        return this;
    }
}
