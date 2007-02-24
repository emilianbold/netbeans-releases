/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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


/*
 * Created on Jun 10, 2003
 *
 */
package org.netbeans.modules.uml.ui.swing.treetable;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * @author sumitabhk
 *
 */
public class PropertyNameCellRenderer extends DefaultTableCellRenderer
{

	/**
	 *
	 */
	public PropertyNameCellRenderer()
	{
		super();
		this.putClientProperty("JTree.lineStyle", "Angled");
		
	}

	public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected,
						hasFocus, row, column);
		if(isSelected)
		{
			setBackground(table.getSelectionBackground());
		}
		else
			setBackground(table.getBackground());
		
		return this;
	}

}




