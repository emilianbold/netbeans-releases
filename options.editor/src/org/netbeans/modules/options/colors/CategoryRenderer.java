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

package org.netbeans.modules.options.colors;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import org.netbeans.api.editor.settings.EditorStyleConstants;


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
	setIcon ((Icon) ((AttributeSet) value).getAttribute ("icon"));
	setText ((String) ((AttributeSet) value).getAttribute (EditorStyleConstants.DisplayName));

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
    
