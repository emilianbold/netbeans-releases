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
 * Created on Jun 18, 2003
 *
 */
package org.netbeans.modules.uml.ui.swing.treetable;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreePath;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
//import org.netbeans.modules.uml.ui.addins.webreport.WebRPTFormatDlg;
import org.netbeans.modules.uml.ui.swing.propertyeditor.PropertyEditorResources;


public class RPTWizPropertyTreeTable extends JTreeTable implements ActionListener
{
	private JPopupMenu m_popup = null;
	//private WebRPTFormatDlg m_editor = null;


	private int m_CurRow = 0;
	private IPropertyElement m_CurElement = null;

	/**
	 *
	 */
	public RPTWizPropertyTreeTable(TreeTableModel treeTableModel/*, WebRPTFormatDlg editor*/)
	{
		super(treeTableModel);
//		m_editor = editor;

		m_popup = new JPopupMenu();

		GridBagLayout gbl = new GridBagLayout();
		double[] vals = {0.0, 0.5, 0.5};
		gbl.columnWeights = vals;
		setLayout(gbl);
		gbl.invalidateLayout(this);
		doLayout();

		TableColumnModel colMod = getColumnModel();
		TreeTableCellEditor cellEditor = new TreeTableCellEditor(tree/*, editor*/);
		colMod.getColumn(1).setCellEditor( cellEditor);
		colMod.getColumn(2).setCellEditor( cellEditor);
		colMod.getColumn(0).setCellEditor( cellEditor);

		colMod.getColumn(0).setMinWidth(16);
		colMod.getColumn(0).setMaxWidth(16);

		RPTWizPropertyValueCellRenderer valueRenderer = new RPTWizPropertyValueCellRenderer();
		colMod.getColumn(1).setCellRenderer(tree);
		colMod.getColumn(2).setCellRenderer(valueRenderer);

		MouseListener popupListener = new TreeTablePopupListener();

		this.addMouseListener(popupListener);
		getTree().addMouseListener(new TreeMouseHandler());

		ToolTipManager.sharedInstance().registerComponent(this);
		setShowVerticalLines(true);
	}

	public void handlePopupDisplay(MouseEvent e)
	{
		m_popup.removeAll();
		//pass this on to the PropertyEditor, passing in the location of
		// mouse click.
		int row = rowAtPoint(e.getPoint());
		//TreePath path = getTree().getPathForLocation(e.getX(), e.getY());
		TreePath path = getTree().getPathForRow(row);

		if (path != null)
		{
			Object obj = path.getLastPathComponent();
			ETSystem.out.println(obj.getClass());
			JDefaultMutableTreeNode node = (JDefaultMutableTreeNode)obj;
			IPropertyElement ele = (IPropertyElement)node.getUserObject();
			if (ele != null)
			{
				//set the current row and selected element to get popup menu to work.
				m_CurRow = row;
				m_CurElement = ele;

				IPropertyDefinition def = ele.getPropertyDefinition();
//				String[] strs = m_editor.showMenuBasedOnDefinition(def, ele);
//				m_editor.setRightClickRow(row);
//				if (strs != null)
//				{
//					int count = strs.length;
//					for (int i=0; i<count; i++)
//					{
//						JMenuItem menuItem = new JMenuItem(strs[i]);
//						menuItem.addActionListener(this);
//						m_popup.add(menuItem);
//					}
//				}
			}
		}
		if (m_popup != null) {
			m_popup.show(e.getComponent(),
					   e.getX(), e.getY());
		}

	}

//	public WebRPTFormatDlg getPropertyEditor()
//	{
//		return  null; //m_editor;
//	}

	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem)(e.getSource());
		String srcText = source.getText();
		if (srcText.equals(PropertyEditorResources.getString("PropertyEditor.Create_Menu")))
		{
//			m_editor.onPopupCreate(m_CurRow, m_CurElement);
		}
		else if (srcText.equals(PropertyEditorResources.getString("PropertyEditor.Delete_Menu")))
		{
//			m_editor.onPopupDelete();
		}
	}

	public class TreeMouseHandler extends MouseInputAdapter
	{
	   public void mousePressed(MouseEvent e)
	   {
			int selRow = getTree().getRowForLocation(e.getX(), e.getY());
			TreePath selPath = getTree().getPathForLocation(e.getX(), e.getY());
			if(selRow != -1) {
				if(e.getClickCount() == 2)
				{
//				  m_editor.handleDoubleClick(selRow, selPath);
//				  m_editor.refresh();
				}
				else if (e.getClickCount() == 1)
				{
		  			Object obj = selPath.getLastPathComponent();
		  			if (obj instanceof JDefaultMutableTreeNode)
		  			{
		  				//getTree().getUI().
//						if(selPath != null && !getModel().isLeaf(selPath.getLastPathComponent())){
//							int                     boxWidth;
//							java.awt.Insets                  i = tree.getInsets();
//
//							if(getExpandedIcon() != null)
//							boxWidth = getExpandedIcon().getIconWidth();
//							else
//							boxWidth = 8;
//
//							int                     boxLeftX = (i != null) ? i.left : 0;
//
//							if (leftToRight) {
//								boxLeftX += (((path.getPathCount() + depthOffset - 2) *
//									  totalChildIndent) + getLeftChildIndent()) -
//										  boxWidth / 2;
//							}
//							else {
//								boxLeftX += lastWidth - 1 -
//										((path.getPathCount() - 2 + depthOffset) *
//									 totalChildIndent) - getLeftChildIndent() -
//										boxWidth / 2;
//							}
//							int boxRightX = boxLeftX + boxWidth;
//
//							return mouseX >= boxLeftX && mouseX <= boxRightX;
//						}
//						return false;
					}
				}
			}
	   }

	}


}


