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


package org.netbeans.modules.uml.ui.swing.treetable;
import java.awt.Component;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.configstringframework.ConfigStringTranslator;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.support.umlutils.PropertyElement;
import org.netbeans.modules.uml.ui.swing.propertyeditor.PropertyEditor;


/**
 * @author sumitabhk
 *
 */
public class PropertyTreeTableModel implements TreeTableModel
{
	// Names of the columns.
	static protected String[]  cNames = {" ", " ", " "};

	// Types of the columns.
	static protected Class[]  cTypes = { ImageIcon.class, PropertyElement.class,
					 String.class};

	private Vector<Object> m_Children = null;
	private HashMap<Object, Vector<Object> > m_BuiltChildren = new HashMap<Object, Vector<Object>>();
	private Vector<Icon> m_Icons = new Vector<Icon>();

	protected JDefaultMutableTreeNode root = null;

	private static JTreeTable treeTable = null;

	protected EventListenerList listenerList = new EventListenerList();
	private PropertyEditor m_editor = null;
	private ConfigStringTranslator m_Translator = new ConfigStringTranslator();
	
	private Object m_EditableComponent = null;

	/**
	 * 
	 */
	public PropertyTreeTableModel()
	{
		super();
	}

	public PropertyTreeTableModel(JDefaultMutableTreeNode root, PropertyEditor editor)
	{
		super();
		this.root = root;
		m_editor = editor;
	}

	public PropertyTreeTableModel(JDefaultMutableTreeNode root, IPropertyElement ele)
	{
		super();
		this.root = root;
		JDefaultMutableTreeNode node = new JDefaultMutableTreeNode(ele, true);
		root.add(node);
		
	}
	public static void main(String[] args)
	{
	}

	/* (non-Javadoc)
	 * @see TreeTableModel#getColumnCount()
	 */
	public int getColumnCount()
	{
		return cNames.length;
	}

	/* (non-Javadoc)
	 * @see TreeTableModel#getColumnName(int)
	 */
	public String getColumnName(int column)
	{
		return cNames[column];
	}

	/* (non-Javadoc)
	 * @see TreeTableModel#getColumnClass(int)
	 */
	public Class getColumnClass(int column)
	{
		if (column == 1)
		{
			return this.getClass();
		}
		if (column <= cTypes.length)
			return cTypes[column];
		return null;
	}

	/* (non-Javadoc)
	 * @see TreeTableModel#getValueAt(java.lang.Object, int)
	 */
	public Object getValueAt(Object node, int column)
	{
		if (node instanceof JDefaultMutableTreeNode)
		{
			JDefaultMutableTreeNode n = (JDefaultMutableTreeNode)node;
			Object obj = n.getUserObject();
			if (obj instanceof IPropertyElement) {
				IPropertyElement ele = (IPropertyElement)n.getUserObject();
				if (column == 1)
				{
					IPropertyDefinition pDef = ele.getPropertyDefinition();
					if (pDef != null)
					{
						if (n.isRoot())
						{
							return m_editor.calculateTopElementName(ele);
						}
						else
						{
							return pDef.getDisplayName();
						}
					}
				}
				else if (column == 2)
				{
					String retValue = null;
					String value = ele.getTranslatedValue();
					retValue = value;
					try {
						IPropertyDefinition pDef = ele.getPropertyDefinition();
						if (pDef != null)
						{
							long mult = pDef.getMultiplicity();
							if (mult > 1)
							{
								String str = pDef.getValidValues();
								if (str == null)
								{
									//here we do not want to show anything
									retValue = null;
								}
							}
							else
							{
								int enumVal = Integer.valueOf(value).intValue();
								//there is a possibility that enums are specified in definitions,
								//in which case the value obtained will not be correct.
								String enumValues = pDef.getFromAttrMap("enumValues");
								if (enumValues != null)
								{
									StringTokenizer tokenizer = new StringTokenizer(enumValues, "|");
									int counter = 0;
									while (tokenizer.hasMoreTokens())
									{
										String token = tokenizer.nextToken();
										if (token.equals(value))
										{
											enumVal = counter;
											break;
										}
										counter++;
									}
								}
								//here we want to find out the enum value and show the
								//translated value.
								String values = pDef.getValidValues();
								if (values != null && values.indexOf("|") >= 0)
								{
									StringTokenizer tokenizer = new StringTokenizer(values, "|");
									int j=0;
									while (tokenizer.hasMoreTokens())
									{
										String token = tokenizer.nextToken();
										if (j == enumVal)
										{
											String transVal = m_Translator.translate(pDef, token);
											retValue = transVal;
										}
										j++;
									}
								}
							}
						}
					}catch (NumberFormatException e)
					{
					}
					return retValue;
				}
				else if (column == 0)
				{
					Object obj1 = getRoot();
					if (obj1 instanceof JDefaultMutableTreeNode)
					{
						JDefaultMutableTreeNode root = (JDefaultMutableTreeNode)obj1;
						
						//We want to show images only if there is only one node selected.
						int countChild = root.getChildCount();
						if (countChild == 1)
						{
							root = (JDefaultMutableTreeNode)root.getChildAt(0);
							int index = root.getIndex(n);
							//ETSystem.out.println("Index for " + ele.getName() + " " + index);
							if (m_Icons.size() == 0)
							{
								m_Icons = m_editor.loadImages(root);
							}
							if (m_Icons != null && m_Icons.size() > index+1)
							{
								//check to see if we have filtered the list in any way
								if (m_editor.isShowingFilteredOnIcons() || 
									m_editor.isShowingComboFilteredList())
								{
									//an already showing filtered list, so need to
									// show only the first icon.
									if (index == -1 && n.equals(root))
									{
										return m_Icons.elementAt(index+1);
									}
									else
									{
										return null;
									}
								}
								else
								{
									//get the right icon.
									if (index == -1 && n.equals(root))
									{
										return m_Icons.elementAt(index+1);
									}
									else if (index >= 0)
									{
										return m_Icons.elementAt(index+1);
									}
								}
							}
							else
							{
								return null;
							}
						}
					}
				}
			}
		}
		return null;
	}


	/* (non-Javadoc)
	 * @see TreeTableModel#isCellEditable(java.lang.Object, int)
	 */
	public boolean isCellEditable(Object node, int column)
	{
//		if (column == 2)
//		{
//			return true;
//		}
		return true;
	}

	/* (non-Javadoc)
	 * @see TreeTableModel#setValueAt(java.lang.Object, java.lang.Object, int)
	 */
	public void setValueAt(Object aValue, Object node, int column)
	{
		if (aValue != null && node != null)
		{
			ETSystem.out.println("Calling setValueAt for " + column + aValue.toString() + node.toString());
		}
		if (aValue instanceof JTreeTable.TreeTableCellRenderer)
		{
			JTreeTable.TreeTableCellRenderer rend = 
					(JTreeTable.TreeTableCellRenderer)aValue;
			
		}
		//m_editor.processLastCell();
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
	 */
	public Object getChild(Object parent, int index)
	{
		Object retObj = null;
		if (parent != null)
		{
			JDefaultMutableTreeNode node = (JDefaultMutableTreeNode)parent;
			int count = node.getChildCount();
			if (count > 0 && index <= count)
			{
				retObj = node.getChildAt(index);
			}
			else
			{
				retObj = getChildren(parent)[index];
			}
		}
		return retObj; 
	}

	protected Object[] getChildren(Object node) {
		Object[] retObj = null;
//		if (m_BuiltChildren != null)
//		{
//			Vector<Object> obj = m_BuiltChildren.get(node);
//			if (obj != null)
//			{
//				retObj = obj.toArray();
//			}
//			else
//			{
//				DefaultMutableTreeNode n = (DefaultMutableTreeNode)node;
//				IPropertyElement ele = (IPropertyElement)n.getUserObject();
//				Vector<Object> children = PropertyEditor.instance().buildSubElementsThatNeedToDisplay(ele, n);
//				m_BuiltChildren.put(node, children);
//				retObj = children.toArray();
//			}
//		}

		JDefaultMutableTreeNode n = (JDefaultMutableTreeNode)node;
		int count = n.getChildCount();
		if (count == 0)
		{
			IPropertyElement ele = (IPropertyElement)n.getUserObject();
			Vector<Object> children = m_editor.buildSubElementsThatNeedToDisplay(ele, n);
			//m_BuiltChildren.put(node, children);
			retObj = children.toArray();
		}
		
		return retObj;
	}

	public void setChildren(Vector<Object> newChildren)
	{
		m_Children.clear();
		m_Children = newChildren;
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
	 */
	public int getChildCount(Object parent)
	{
		int retCount = 0;
		if (parent != null)
		{
			JDefaultMutableTreeNode node = (JDefaultMutableTreeNode)parent;
			int count = node.getChildCount();
			if (count > 0)
			{
				retCount = count;
			}
			else
			{
				retCount = getChildren(parent).length;
			}
		}
		return retCount;
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
	 */
	public boolean isLeaf(Object node)
	{
		if (node instanceof JDefaultMutableTreeNode)
		{
			JDefaultMutableTreeNode n = (JDefaultMutableTreeNode)node;
			Object obj = n.getUserObject();
			if (obj != null && obj instanceof IPropertyElement)
			{
				IPropertyElement ele = (IPropertyElement)obj;
				IPropertyDefinition pDef = ele.getPropertyDefinition();
				long mult = pDef.getMultiplicity();
				String name = ele.getName();
				Vector subDefs = pDef.getSubDefinitions();
				int subDefCount = 0;
				if (subDefs != null)
				{
					subDefCount = subDefs.size();
				}
				if (mult <= 1)
				{
					Vector elems = ele.getSubElements();
					if (elems == null || (elems != null && elems.size() == 0) )
					{
						if (name != null && name.equals("dummy") && subDefCount > 0)
						{
							return false;
						}
						else
						{
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
	 */
	public void valueForPathChanged(TreePath path, Object newValue)
	{
		
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
	 */
	public int getIndexOfChild(Object parent, Object child)
	{
		for (int i = 0; i < getChildCount(parent); i++) {
		if (getChild(parent, i).equals(child)) { 
				return i; 
			}
		}
		return -1; 
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
	 */
	public void addTreeModelListener(TreeModelListener l)
	{
		listenerList.add(TreeModelListener.class, l);
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
	 */
	public void removeTreeModelListener(TreeModelListener l)
	{
		listenerList.remove(TreeModelListener.class, l);
		
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getRoot()
	 */
	public Object getRoot()
	{
		return root;
	}
	
	/*
	 * Notify all listeners that have registered interest for
	 * notification on this event type.  The event instance 
	 * is lazily created using the parameters passed into 
	 * the fire method.
	 * @see EventListenerList
	 */
	protected void fireTreeStructureChanged(Object source, Object[] path, 
										int[] childIndices, 
										Object[] children) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==TreeModelListener.class) {
				// Lazily create the event:
				if (e == null)
					e = new TreeModelEvent(source, path, 
										   childIndices, children);
				((TreeModelListener)listeners[i+1]).treeStructureChanged(e);
			}          
		}
	}
	
	/*
	 * Notify all listeners that have registered interest for
	 * notification on this event type.  The event instance 
	 * is lazily created using the parameters passed into 
	 * the fire method.
	 * @see EventListenerList
	 */
	protected void fireTreeNodesChanged(Object source, Object[] path, 
										int[] childIndices, 
										Object[] children) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==TreeModelListener.class) {
				// Lazily create the event:
				if (e == null)
					e = new TreeModelEvent(source, path, 
										   childIndices, children);
				((TreeModelListener)listeners[i+1]).treeNodesChanged(e);
			}          
		}
	}

	/*
	 * Notify all listeners that have registered interest for
	 * notification on this event type.  The event instance 
	 * is lazily created using the parameters passed into 
	 * the fire method.
	 * @see EventListenerList
	 */
	protected void fireTreeNodesInserted(Object source, Object[] path, 
										int[] childIndices, 
										Object[] children) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==TreeModelListener.class) {
				// Lazily create the event:
				if (e == null)
					e = new TreeModelEvent(source, path, 
										   childIndices, children);
				((TreeModelListener)listeners[i+1]).treeNodesInserted(e);
			}          
		}
	}

	/*
	 * Notify all listeners that have registered interest for
	 * notification on this event type.  The event instance 
	 * is lazily created using the parameters passed into 
	 * the fire method.
	 * @see EventListenerList
	 */
	protected void fireTreeNodesRemoved(Object source, Object[] path, 
										int[] childIndices, 
										Object[] children) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==TreeModelListener.class) {
				// Lazily create the event:
				if (e == null)
					e = new TreeModelEvent(source, path, 
										   childIndices, children);
				((TreeModelListener)listeners[i+1]).treeNodesRemoved(e);
			}          
		}
	}

	public void expand(int row, boolean val)
	{
		treeTable.getTree().expandNode(row, val);
		
	}
	
	public void setTreeTable (JTreeTable tree)
	{
		treeTable = tree;
	}
	
	public Vector<Icon> getIcons()
	{
		return m_Icons;
	}

	public void setEditingComponent(Object obj)
	{
		if (obj == null && m_EditableComponent != null)
		{
			try
			{
				((Component)m_EditableComponent).removeNotify();
			}
			catch (Exception e)
			{
				//do nothing, removeNotify is throwing at times.
			}
		}
		else
		{
			m_EditableComponent = obj;
		}
	}

	public Object getEditingComponent()
	{
		return m_EditableComponent;
	}

}

