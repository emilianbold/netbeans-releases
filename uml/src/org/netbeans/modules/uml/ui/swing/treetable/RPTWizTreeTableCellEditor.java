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
package org.netbeans.modules.uml.ui.swing.treetable;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.StringTokenizer;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.configstringframework.ConfigStringTranslator;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
//import org.netbeans.modules.uml.ui.addins.webreport.WebRPTFormatDlg;


public class RPTWizTreeTableCellEditor extends AbstractCellEditor implements
		 TableCellEditor
{
	private JTreeTable.TreeTableCellRenderer tree = null;
	//private WebRPTFormatDlg m_Editor = null;

	/**
	 *
	 */
	public RPTWizTreeTableCellEditor()
	{
		super();
	}

	public RPTWizTreeTableCellEditor(JTreeTable.TreeTableCellRenderer tr)
	{
		super();
		tree = tr;
	}

//	public RPTWizTreeTableCellEditor(JTreeTable.TreeTableCellRenderer tr, PropertyEditor editor)
//	{
//		super();
//		tree = tr;
//		m_Editor = editor;
//	}

	public JTreeTable.TreeTableCellRenderer getTree()
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

		Component retObj = null;

		//set the current row on property editor
//		m_Editor.setCurrentRow(r);

		boolean isEditable = true;

		if (c == 2)
		{
			TreePath path = getTree().getPathForRow(r);
			if (path != null)
			{
				Object obj = path.getLastPathComponent();
				if (obj instanceof JDefaultMutableTreeNode)
				{
					JDefaultMutableTreeNode node = (JDefaultMutableTreeNode)obj;
					final IPropertyElement ele = (IPropertyElement)node.getUserObject();
					IPropertyDefinition def = ele.getPropertyDefinition();
					long mult = def.getMultiplicity();
					String values = def.getValidValues();
					ETSystem.out.println(values);
					if (values != null)
					{
						if (values.equals("FormatString"))
						{
							//This node is non-editable, I need to format string to show.
						}
						else if (values.equals("#DataTypeList"))
						{
							String buffer = "buffer buffer"; //m_Editor.buildDataTypeList(ele);
							JDescribeComboBox temp = new JDescribeComboBox(/*m_Editor*/);
							temp.setBorder(null);
							StringTokenizer tokenizer = new StringTokenizer(buffer, "|");
							while (tokenizer.hasMoreTokens())
							{
								String transVal = tokenizer.nextToken();
								temp.addItem(transVal);
							}
							temp.setSelectedItem(value);
							retObj = temp;
						}
						else
						{
							String cType = def.getControlType();
							if (cType != null && cType.equals("read-only"))
							{
								JTextField temp = new JTextField();
								temp.setEditable(false);
								isEditable = false;
								temp.setBorder(null);
								if (value != null)
								{
									ConfigStringTranslator translator = new ConfigStringTranslator();
									temp.setText(translator.translate(def, value.toString()));
								}
								retObj = temp;
							}
							else
							{
								ConfigStringTranslator translator = new ConfigStringTranslator();
								JComboBox temp = new JComboBox();
								temp.setBorder(null);
								//temp.addFocusListener(new EditorFocusListener());
								//cannot make is editable as the focus listener will stop working
								//temp.setEditable(true);
								temp.setPopupVisible(false);
								StringTokenizer tokenizer = new StringTokenizer(values, "|");
								while (tokenizer.hasMoreTokens())
								{
									String transVal = translator.translate(def, tokenizer.nextToken());
									temp.addItem(transVal);
								}
								temp.setSelectedItem(value);

								retObj = temp;
							}
						}
					}
					else
					{
						String cType = def.getControlType();
						if (mult > 1 && (cType == null || !cType.equals("read-only")))
						{
							if (table instanceof JPropertyTreeTable)
							{
								//WebRPTFormatDlg editor = ((RPTWizPropertyTreeTable)table).getPropertyEditor();
								JDescribeButton btn = new JDescribeButton(r/*, editor*/);
								btn.setBorder(null);
								retObj = btn;

								//retObj.addFocusListener(new EditorFocusListener());
								//mark the component as non-editable, so that
								//we do not add property listener to it.
								isEditable = false;
							}
						}
						else
						{
							JTextField temp = new JTextField();
							temp.setBorder(null);
							if (value != null)
							{
								temp.setText(value.toString());
//								temp.addActionListener(new ActionListener() {
//
//									public void actionPerformed(ActionEvent e)
//									{
//										ele.setModified(true);
//									}
//								});
							}
							if (cType == null || cType.equals("read-only") )
							{
								temp.setEditable(false);

								//mark the component as non-editable, so that
								//we do not add property listener to it.
								isEditable = false;
							}
							retObj = temp;
						}
					}
					//ETSystem.out.println("in getTableCellRen... = " + mult + values);
				}
			}
		}
		else if (c == 1)
		{
			TreePath path = getTree().getPathForRow(r);
			if (path != null)
			{
				Object obj = path.getLastPathComponent();
				if (obj instanceof JDefaultMutableTreeNode)
				{
					JDefaultMutableTreeNode node = (JDefaultMutableTreeNode)obj;
					if (node.getChildCount() > 0)
					{
						retObj = null;//tree;
					}
					else
					{
						return null;
					}
				}
			}
		}
		else if (c == 0)
		{
			//ETSystem.out.println("First column selected for images filter");
		//	m_Editor.sortPropertyEditorElements(r);
			return null;
		}

		//I want to add property change listener only if the component is
		// in values column and is editable.
		if (retObj != null && c == 2 && isEditable)
		{
//			retObj.addPropertyChangeListener(new PropertyChangeListener()
//			{
//
//				public void propertyChange(PropertyChangeEvent arg0)
//				{
//					m_Editor.columnValueChanged(arg0);
//				}
//			}
//			);
		}

		if (retObj != null && c == 2)
		{
			retObj.addFocusListener(new FocusListener() {

				public void focusGained(FocusEvent e)
				{
					if (!e.isTemporary())
					{
		//				m_Editor.handleFocusGainedOnCellEvent(e);
					}
					//ETSystem.out.println("Focus gained on " + obj.getClass());
				}

				public void focusLost(FocusEvent e)
				{
					if (!e.isTemporary())
					{
		//				m_Editor.handleFocusLostOnCellEvent(e);
					}
					//ETSystem.out.println("Focus lost on " + obj.getClass() + " new value = " + text);
				}
			});
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
					if (tModel.getColumnClass(counter).equals(PropertyTreeTableModel.class) )
					//if (counter == 1)
					{
						MouseEvent me = (MouseEvent)e;
						if (me.getClickCount() == 2)
						{
							//the element was double clicked, need to expand/collapse
		//					m_Editor.handleDoubleClick();
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
		// TODO Auto-generated method stub
		return tree;
	}

	private class EditorFocusListener implements FocusListener
	{
		public void focusGained(FocusEvent e)
		{
			//m_Editor.handleFocusGainedOnCellEvent(e);
			ETSystem.out.println("Focus gained on " );
		}

		public void focusLost(FocusEvent e)
		{
			//m_Editor.handleFocusLostOnCellEvent(e);
			ETSystem.out.println("Focus lost on " );
		}
	}
}



