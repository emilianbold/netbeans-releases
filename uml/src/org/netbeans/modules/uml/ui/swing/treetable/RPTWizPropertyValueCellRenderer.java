/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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



