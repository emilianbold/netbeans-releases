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
package org.netbeans.modules.uml.ui.support.drawingproperties.FontColorDialogs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;

import org.netbeans.modules.uml.ui.support.drawingproperties.IColorProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.IFontProperty;

/**
 * @author sumitabhk
 *
 */
public class DrawingPropertyCellRenderer extends DefaultTableCellRenderer
{

	/**
	 * 
	 */
	public DrawingPropertyCellRenderer()
	{
		super();
	}

	public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus,
				int row, int column)
	{
		Component newComponent = this;
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
		if (table instanceof FontColorTreeTable)
		{
			FontColorTreeTable myTable = (FontColorTreeTable)table;
			DefaultMutableTreeNode node = null;//myTable.getNodeAtGridRow(row);
			if (value != null)
			{
				if (value != null && value instanceof IDrawingProperty)
				{
					IDrawingProperty pEle = (IDrawingProperty)value;
					JPanel cellPanel = new JPanel();
					cellPanel.setLayout(new FlowLayout());
					JButton actionButton = new JButton("...");
					actionButton.setPreferredSize(new Dimension(10, 10));
					if (value instanceof IFontProperty)
					{
						IFontProperty pFontProperty = (IFontProperty)value;
						JTextField dispalyLabel = new JTextField();
						dispalyLabel.setEditable(false);
						setBackground(Color.WHITE);
						setText(pFontProperty.getFaceName() + "(" + pFontProperty.getSize() + ")");
						newComponent = dispalyLabel;
					}
					else if (value instanceof IColorProperty)
					{
						IColorProperty pColorProperty = (IColorProperty)value;
						JTextField dispalyLabel = new JTextField();
						dispalyLabel.setEditable(false);
						setBackground(new Color(pColorProperty.getColor()));
						setText("");
//						dispalyLabel.setBackground(Color.RED);
						newComponent = dispalyLabel;
					}
					//cellPanel.add(dispalyLabel);
					//cellPanel.add(actionButton);
					//newComponent = cellPanel;
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




