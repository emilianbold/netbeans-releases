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



package org.netbeans.modules.uml.ui.swing.preferencedialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import org.netbeans.modules.uml.core.configstringframework.ConfigStringHelper;
import org.netbeans.modules.uml.core.configstringframework.IConfigStringTranslator;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceManager;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;

/**
 * @author sumitabhk
 *
 */
public class JPreferenceDialogTable extends JTable
{
	PreferenceDialogUI m_UI = null;
	/**
	 * 
	 */
	public JPreferenceDialogTable()
	{
		super();
	}
	
	public PreferenceDialogUI getPreferenceDialogUI()
	{
		return m_UI;
	}

	public JPreferenceDialogTable(ISwingPreferenceTableModel model, PreferenceDialogUI ui)
	{
		super(model);
		
		m_UI = ui;
		
		PreferenceDialogTableCellEditor cellEditor = new PreferenceDialogTableCellEditor(ui);
		
		getColumnModel().getColumn(1).setCellEditor(cellEditor);
		getColumnModel().getColumn(0).setCellEditor(cellEditor);
		PreferenceTableCellRenderer renderer = new PreferenceTableCellRenderer();
		renderer.setUI(ui);
		getColumnModel().getColumn(0).setCellRenderer(renderer);
		getColumnModel().getColumn(1).setCellRenderer(renderer);
	}


	public class PreferenceTableCellRenderer extends JLabel implements TableCellRenderer
	{
		private PreferenceDialogUI m_UI = null;
		
		public void setUI(PreferenceDialogUI ui)
		{
			m_UI = ui;
		}
		/* (non-Javadoc)
		 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table, 
													   Object value, 
													   boolean isSelected, 
													   boolean hasFocus, 
													   int row, 
													   int col)
		{
			Component retObj = null;
			if (col == 0)
			{
				if (value instanceof IPropertyElement)
				{
					IPropertyElement pEle = (IPropertyElement)value;
					IPropertyDefinition pDef = pEle.getPropertyDefinition();
					if (pDef != null)
					{
						String val = pDef.getDisplayName();
						setText(val);
                                                
                                                //CBeckham - removed hardcoding of fontsize to handle larger fonts
                                                //setFont(new java.awt.Font("Dialog", 0, 11));
						setFont(new java.awt.Font("Dialog", 0, getFont().getSize()));
						setIcon(null);
						setBorder(new javax.swing.border.EmptyBorder(0, 2, 0, 0));
						//setOpaque(true);
						//setBackground(Color.CYAN);
						//setEnabled(false);
					}
				}
			}
			else if (col == 1)
			{
				Object obj = table.getValueAt(row, 0);
				if (obj instanceof IPropertyElement)
				{
					IPropertyElement pEle = (IPropertyElement)obj;
					IPropertyDefinition pDef = pEle.getPropertyDefinition();
					String pDefName = pDef.getName();
					int pos = pDefName.indexOf("Font");
					int pos2 = pDefName.indexOf("Color");
					if (pos > -1)
					{
						Font f = m_UI.buildCurrentFont(pEle);
						setFont(f);
						IConfigStringTranslator translator = ConfigStringHelper.instance().getTranslator();
						if (pDef != null)
						{
							String val = pEle.getValue();
							if (val != null)
							{
								val = translator.translate(pDef, val);
								setText(val);
							}
							else
							{
								setText((String)value);
							}
							setIcon(null);
						}
					}
					else if (pos2 > -1)
					{
						Color c = m_UI.buildCurrentColor(pEle);
						String val = pEle.getValue();
						Icon icon = null;
						if (c != null)
						{
							icon = new ColorizedIcon(c);
						}
						setText("");
						setIcon(icon);
					}
					else
					{
						IConfigStringTranslator translator = ConfigStringHelper.instance().getTranslator();
						if (pDef != null)
						{
							String val = pEle.getValue();
							if (val != null)
							{
								val = translator.translate(pDef, val);
								setText(val);
							}
							else
							{
								setText((String)value);
							}
							setIcon(null);
						}
					}
					//I need to set it disabled if the property
					//element belongs to a read-only file.
					IPreferenceManager2 prefMan = ProductRetriever.retrieveProduct().getPreferenceManager();
					if (prefMan != null)
					{
						if(prefMan.isEditable(pEle))
						{
							setBackground(Color.WHITE);
						}
						else
						{
							setEnabled(false);
							setBackground(Color.WHITE);
							//setOpaque(false);
						}
					}
				}
			}
			return this;//getModel().getValueAt(arg4, arg5);
		}
	}
	private class ColorizedIcon implements Icon {
	  Color color;
	  public ColorizedIcon (Color c) {
		color = c;
	  }
	  public void paintIcon (Component c, Graphics g, int x, int y) {
		int width = getIconWidth();
		int height = getIconHeight();
		g.setColor (color);
		g.fillRect(x, y, width, height);
		//g.fillOval (x, y, width, height);
	  }
	  public int getIconWidth() {
		return 170;
	  }
	  public int getIconHeight() { 
		return 15;
	  }
	}

}


