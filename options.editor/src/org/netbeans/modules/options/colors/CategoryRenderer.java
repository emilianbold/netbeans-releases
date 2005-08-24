/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.options.colors;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.UIManager;
import org.netbeans.modules.options.colors.ColorModel.Category;


class CategoryRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(
	JList list,
	Object value,
	int index,
	boolean isSelected,
	boolean cellHasFocus
    ) {
	setComponentOrientation (list.getComponentOrientation ());
	if (isSelected) {
	    setBackground (list.getSelectionBackground ());
	    setForeground (list.getSelectionForeground ());
	} else {
	    setBackground (list.getBackground ());
	    setForeground (list.getForeground ());
	}
	setIcon (((Category) value).getIcon ());
	setText (((Category) value).getDisplayName ());

	setEnabled (list.isEnabled ());
	setFont (list.getFont ());
	setBorder (
	    cellHasFocus ? 
		UIManager.getBorder ("List.focusCellHighlightBorder") : 
		noFocusBorder
	);
	return this;
    }
}
    
