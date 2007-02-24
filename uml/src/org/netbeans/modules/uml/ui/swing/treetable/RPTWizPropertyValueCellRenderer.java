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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JTable;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.table.DefaultTableCellRenderer;

import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;


public class RPTWizPropertyValueCellRenderer extends DefaultTableCellRenderer
{

	/**
	 * 
	 */
	public RPTWizPropertyValueCellRenderer()
	{
		super();
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
		
		//if the corresponding property definition is read-only I want to show that.
		boolean isReadOnly = false;
		boolean isFormatString = false;
		boolean isRootNode = false;
		if (table instanceof JPropertyTreeTable)
		{
			JPropertyTreeTable myTable = (JPropertyTreeTable)table;
			JDefaultMutableTreeNode node = myTable.getPropertyEditor().getNodeAtGridRow(row);
			if (node != null)
			{
				isRootNode = node.isRoot();
				Object obj = node.getUserObject();
				if (obj != null && obj instanceof IPropertyElement)
				{
					IPropertyElement pEle = (IPropertyElement)obj;
					IPropertyDefinition pDef = pEle.getPropertyDefinition();
					if (pDef != null)
					{
						String cType = pDef.getControlType();
						if (cType != null && cType.equals("read-only"))
						{
							isReadOnly = true;
						}
						
						String validVals = pDef.getValidValues();
						if (validVals != null && validVals.equals("FormatString"))
						{
							isFormatString = true;
						}
					}
				}
			}
		}
		
		if (isReadOnly || isFormatString)
		{
			setBackground(new Color(232,228,232));
		}

		if (isRootNode)
		{
			setBackground(new Color(192, 192, 192));
		}

		setBorder(new MyBorder());
		return this;
	}

	public Border getRadioButtonBorder() {
	UIDefaults table = UIManager.getLookAndFeelDefaults();
	Border radioButtonBorder = BorderUIResource.getBlackLineBorderUIResource();
	//		new BorderUIResource.LineBorderUIResource(Color.BLACK, 1);
	return radioButtonBorder;
	}

	public class MyBorder extends LineBorder {

		public MyBorder()
		{
			super(Color.GRAY);
		}

		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			Color oldColor = g.getColor();
			int i;

		/// PENDING(klobad) How/should do we support Roundtangles?
			g.setColor(lineColor);
			
			g.setColor(new Color(232,228,232));
			g.drawLine(x, y, x, y+height);
			g.drawLine(x, y, x+width, y);
			g.drawLine(x+width, y, x+width, y+height);
			
			g.setColor(oldColor);
		}
	}

}



