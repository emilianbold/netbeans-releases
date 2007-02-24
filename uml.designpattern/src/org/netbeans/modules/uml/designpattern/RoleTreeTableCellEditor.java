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
 * Created on Jun 9, 2003
 *
 */
package org.netbeans.modules.uml.designpattern;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.StringTokenizer;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;


import org.netbeans.modules.uml.ui.swing.treetable.JDefaultMutableTreeNode;
import org.netbeans.modules.uml.ui.swing.treetable.JDescribeButton;
import org.netbeans.modules.uml.ui.swing.treetable.TreeTableModel;

/**
 * @author sumitabhk
 *
 */
public class RoleTreeTableCellEditor extends AbstractCellEditor implements
		 TableCellEditor
{
	private JRoleTreeTable.TreeTableCellRenderer tree = null;
	private WizardRoles m_Clazz = null;

	/**
	 *
	 */
	public RoleTreeTableCellEditor()
	{
		super();
	}

	public RoleTreeTableCellEditor(JRoleTreeTable.TreeTableCellRenderer tr)
	{
		super();
		tree = tr;
	}

	public RoleTreeTableCellEditor(JRoleTreeTable.TreeTableCellRenderer tr, WizardRoles clazz)
	{
		super();
		tree = tr;
		m_Clazz = clazz;
	}

	public JRoleTreeTable.TreeTableCellRenderer getTree()
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
		Component retObj = null;
		if (c == 2)
		{
			TreePath path = getTree().getPathForRow(r);
			if (path != null)
			{
				Object obj = path.getLastPathComponent();
				if (obj instanceof JDefaultMutableTreeNode)
				{
					JDefaultMutableTreeNode node = (JDefaultMutableTreeNode)obj;
					WizardRoleObject roleObj = (WizardRoleObject)node.getUserObject();
					if (roleObj != null)
					{
						IDesignPatternRole pRole = roleObj.getRole();
						if (pRole != null)
						{
							int mult = pRole.getMultiplicity();
							if (mult > 1 && (node.getParent().getParent() == null))
							{
								// if the role is multiple, but there is no parent, the user
								// must have clicked on the main node of the role, not one of the
								// children, so display a "+" button
								//JDescribeButton btn = new JDescribeButton(r, m_Clazz);
								//btn.setBorder(null);
								retObj = null; //btn;
							}
							else
							{
								// need to populate the combo list
								// this information has been built and stored in a map
								// so find it in the map
								String str = m_Clazz.buildRoleMap(pRole);
								if (str != null && str.length() > 0)
								{
									JRoleComboBox temp = new JRoleComboBox(roleObj, m_Clazz);
									temp.setBorder(null);
									StringTokenizer tokenizer = new StringTokenizer(str, "|");
									while (tokenizer.hasMoreTokens())
									{
										String transVal = tokenizer.nextToken();
										temp.addItem(transVal);
									}
									temp.setFont(table.getFont());
									temp.setSelectedItem(value);
									retObj = temp;
								}
								else
								{
									JRoleTextField temp = new JRoleTextField(roleObj, m_Clazz);
									temp.setEditable(true);
									String s = (String)value;
									temp.setText(s);
									temp.setBorder(null);
									temp.setFont(table.getFont());
									retObj = temp;
								}
							}
						}
					}
				}
			}
		}
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
	public boolean isCellEditable(EventObject e) {
		if (e instanceof MouseEvent) {
			TreeModel model = tree.getModel();
			if (model instanceof TreeTableModel)
			{
				TreeTableModel tModel = (TreeTableModel)model;
				for (int counter = tModel.getColumnCount() - 1; counter >= 0;
					 counter--)
				{
					if (tModel.getColumnClass(counter).equals(RoleTreeTableModel.class) )
					//if (counter == 1)
					{
						MouseEvent me = (MouseEvent)e;
						if (me.getClickCount() == 2)
						{
							//the element was double clicked, need to expand/collapse
							//TODO m_Editor.handleDoubleClick();
						}
						else
						{
							//need to pass on this mouse event to the tree.
							MouseEvent newME = new MouseEvent(tree, me.getID(),
							   me.getWhen(), me.getModifiers(),
							   me.getX(),
							   me.getY(), me.getClickCount(),
											   me.isPopupTrigger());
							tree.dispatchEvent(newME);
							break;
						}
					}
				}
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	public Object getCellEditorValue()
	{
		return tree;
	}
}



