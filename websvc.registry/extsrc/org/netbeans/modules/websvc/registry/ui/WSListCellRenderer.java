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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
            java.net.URL url = WSListCellRenderer.class.getResource("/org/netbeans/modules/websvc/registry/resources/UDDIRegistry.gif");
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
