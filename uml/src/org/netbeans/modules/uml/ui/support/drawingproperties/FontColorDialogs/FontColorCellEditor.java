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
 * Created on Jun 9, 2003
 *
 */
package org.netbeans.modules.uml.ui.support.drawingproperties.FontColorDialogs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import org.netbeans.modules.uml.ui.support.drawingproperties.DrawingPropertyResource;
import org.netbeans.modules.uml.ui.support.drawingproperties.FontChooser;
import org.netbeans.modules.uml.ui.support.drawingproperties.IColorProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.IFontProperty;


/**
 * @author sumitabhk
 *
 */
public class FontColorCellEditor extends AbstractCellEditor implements
		 TableCellEditor 
{
	private DrawingPropertyCellRenderer tree = null;
	private IDrawingProperty m_DrawingProperty = null;
	private JTable m_Table = null;

	/**
	 * 
	 */
	public FontColorCellEditor()
	{
		super();
	}

	public FontColorCellEditor(DrawingPropertyCellRenderer tr)
	{
		super();
		tree = tr;
	}

	public DrawingPropertyCellRenderer getTree()
	{
		return tree;
	}

	/**
	 * TreeTableCellEditor implementation. Component returned is the
	 * JTree.
	 */
	public Component getTableCellEditorComponent(JTable table,
							 Object value,
							 boolean isSelected,
							 int r, int c)
	{
		//PropertyEditor.instance().setCurrentRow(r);
		
		m_Table = table;
		
		Component retObj = new JLabel();
		
		if (c == 1)
		{
			if (value != null && value instanceof IDrawingProperty)
			{
				m_DrawingProperty = (IDrawingProperty)value;
				
				Box editBox = Box.createHorizontalBox();
				JTextField field = new JTextField();
				field.setEditable(false);
				JButton button = new JButton("...");
				editBox.add(field);
				editBox.add(Box.createHorizontalStrut(3));
				editBox.add(button);
				final int row = r;
				final int col = c;
				button.addActionListener
				(
					new ActionListener()
               {
                  public void actionPerformed(ActionEvent e)
                  {
							editResource(row, col);
                  }
               }
				);
				
				if (value instanceof IFontProperty)
				{
					IFontProperty pFontProperty = (IFontProperty)value;
					field.setBackground(Color.WHITE);
					field.setText(pFontProperty.getFaceName());
				}
				else if (value instanceof IColorProperty)
				{
					IColorProperty pColorProperty = (IColorProperty)value;
					field.setBackground(new Color(pColorProperty.getColor()));
				}
				
				retObj = editBox;
			}
//			TreePath path = getTree().getPathForRow(r);
//			if (path != null)
//			{
//				Object obj = path.getLastPathComponent();
//				if (obj instanceof JDefaultMutableTreeNode)
//				{
//					JDefaultMutableTreeNode node = (JDefaultMutableTreeNode)obj;
//					if (node.getChildCount() > 0)
//					{
//						retObj = null;//tree;
//					}
//					else
//					{
//						return null;
//					}
//				}
//			}
		}
		else if (c == 0)
		{
			return null;
		}
		
//		if (retObj != null && c == 2)
//		{
//			retObj.addFocusListener(new FocusListener() {
//
//				public void focusGained(FocusEvent e)
//				{
//					if (!e.isTemporary())
//					{
//						//m_Editor.handleFocusGainedOnCellEvent(e);
//					}
//					//ETSystem.out.println("Focus gained on ");
//				}
//
//				public void focusLost(FocusEvent e)
//				{
//					if (!e.isTemporary())
//					{
//						//m_Editor.handleFocusLostOnCellEvent(e);
//					}
//					//ETSystem.out.println("Focus lost on " + e.getOppositeComponent());
//					if (e.getOppositeComponent() == null)
//					{
//						// if the component that is taking the focus away from the property editor is null
//						// then we are ASSUMING that it is another application
//						stopCellEditing();
//					}
//				}
//			});
//			
//			retObj.addKeyListener(new KeyListener() {
//
//				public void keyTyped(KeyEvent arg0) {
//					// TODO Auto-generated method stub
//					
//				}
//
//				public void keyPressed(KeyEvent arg0) 
//				{
//					handleKeyPress(arg0);
//				}
//
//				public void keyReleased(KeyEvent arg0) {
//					// TODO Auto-generated method stub
//					
//				}
//			});
//			
//		}
//		m_Editor.setEditingComponent(retObj);
		return retObj;
	}

	/**
	 * Overridden to return false, and if the event is a mouse event
	 * it is forwarded to the tree.<p>
	 * The behavior for this is debatable, and should really be offered
	 * as a property. By returning false, all keyboard actions are
	 * implemented in terms of the table. By returning true, the
	 * tree would get a chance to do something with the keyboard
	 * events. For the most part this is ok. But for certain keys,
	 * such as left/right, the tree will expand/collapse where as
	 * the table focus should really move to a different column. Page
	 * up/down should also be implemented in terms of the table.
	 * By returning false this also has the added benefit that clicking
	 * outside of the bounds of the tree node, but still in the tree
	 * column will select the row, whereas if this returned true
	 * that wouldn't be the case.
	 * <p>By returning false we are also enforcing the policy that
	 * the tree will never be editable (at least by a key sequence).
	 */
	public boolean isCellEditable(EventObject e) 
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	public Object getCellEditorValue()
	{
		return tree;
	}
	
	public boolean shouldSelectCell(EventObject anEvent)
	{
		boolean retVal = false;
		if (anEvent instanceof MouseEvent)
		{
			retVal = true;
		}
		return retVal;
	}
	
	protected void editResource(int row, int col)
	{
		if (m_DrawingProperty != null)
		{
			if (m_DrawingProperty instanceof IFontProperty)
			{
				IFontProperty pFontProperty = (IFontProperty)m_DrawingProperty;
				Font font = FontChooser.selectFont(pFontProperty.getFont());
				if (font != null)
				{
					pFontProperty.setFont(font);
				}
			}
			else if (m_DrawingProperty instanceof IColorProperty)
			{
				IColorProperty pColorProperty = (IColorProperty)m_DrawingProperty;
				String title = DrawingPropertyResource.getString("IDS_COLORS");
				Color color = JColorChooser.showDialog(m_Table, title, null);
				if (color != null)
				{
					pColorProperty.setColor(color.getRGB());
				}
			}
			
			// Add to changed list
			if (m_Table != null && m_Table instanceof FontColorTreeTable)
			{
				FontColorTreeTable pFontColorTreeTable = (FontColorTreeTable)m_Table;
				BasicColorsAndFontsDialog pBasicColorsAndFontsDialog = pFontColorTreeTable.getParentDlg();
				if (pBasicColorsAndFontsDialog != null && pBasicColorsAndFontsDialog instanceof ApplicationColorsAndFonts)
				{
					ApplicationColorsAndFonts pApplicationColorsAndFonts = (ApplicationColorsAndFonts)pBasicColorsAndFontsDialog;
					pApplicationColorsAndFonts.addChangedProperty(m_DrawingProperty);
				} 
			}
			
			// Refresh
			if (m_Table != null)
			{
				// J2127
				if (m_Table.getCellEditor() != null)
				{
					m_Table.getCellEditor().stopCellEditing();
				}
				// J1803
				//m_Table.editCellAt(row, col);
			}
		}
	}
}




