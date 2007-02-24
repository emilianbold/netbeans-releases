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


package org.netbeans.modules.uml.ui.support.drawingproperties.FontColorDialogs;
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

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.configstringframework.ConfigStringTranslator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationResourceMgr;
import org.netbeans.modules.uml.ui.support.diagramsupport.IPresentationTarget;
import org.netbeans.modules.uml.ui.support.drawingproperties.DrawingPropertyResource;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingProperty;
import org.netbeans.modules.uml.ui.swing.propertyeditor.PropertyEditor;
import org.netbeans.modules.uml.ui.swing.treetable.JTreeTable;
import org.netbeans.modules.uml.ui.swing.treetable.TreeTableModel;


/**
 * @author sumitabhk
 *
 */
public class FontColorTreeTableModel implements TreeTableModel
{
	// Names of the columns.
	static protected String[]  cNames = {DrawingPropertyResource.getString("IDS_PROPERTY"), DrawingPropertyResource.getString("IDS_NAME")};

	// Types of the columns.
	static protected Class[]  cTypes = { Object.class, String.class};

	private Vector<Object> m_Children = null;

	protected DefaultMutableTreeNode root = null;

	private static JTreeTable treeTable = null;

	protected EventListenerList listenerList = new EventListenerList();
	private ConfigStringTranslator m_Translator = new ConfigStringTranslator();

	/**
	 * 
	 */
	public FontColorTreeTableModel()
	{
		super();
	}

	public FontColorTreeTableModel(DefaultMutableTreeNode root, IDrawingProperty ele)
	{
		super();
		this.root = root;
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
		if (node instanceof DefaultMutableTreeNode)
		{
			DefaultMutableTreeNode n = (DefaultMutableTreeNode)node;
			Object obj = n.getUserObject();
			if (obj instanceof IDrawingProperty) 
			{
				IDrawingProperty pDrawingProperty = (IDrawingProperty)obj;
				if (column == 0)
				{
					String displayName = pDrawingProperty.getResourceName();
					IPresentationResourceMgr pMgr = ProductHelper.getPresentationResourceMgr();
					if (pMgr != null)
					{
						// Convert that name to something more reasonable
						ETPairT<String, String> val = pMgr.getDisplayName(pDrawingProperty.getDrawEngineName(), pDrawingProperty.getResourceName());
						if (val != null)
						{
							displayName = val.getParamOne();
						}

						if (displayName == null || displayName.length() == 0)
						{
							displayName = pDrawingProperty.getResourceName();
						}
					}
					return displayName;
				}
				else if (column == 1)
				{
					return obj;
				}
			}
			else
			{
				if (column == 0)
				{
					return node;
				}
				else if (column == 1)
				{
					return null;
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
		return true;
	}

	/* (non-Javadoc)
	 * @see TreeTableModel#setValueAt(java.lang.Object, java.lang.Object, int)
	 */
	public void setValueAt(Object aValue, Object node, int column)
	{
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
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)parent;
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
		DefaultMutableTreeNode n = (DefaultMutableTreeNode)node;
		int count = n.getChildCount();
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
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)parent;
			int count = node.getChildCount();
			if (count > 0)
			{
				retCount = count;
			}
			else
			{
				Object[] objs = getChildren(parent);
				if (objs != null)
				{
					retCount = objs.length;
				}
			}
		}
		return retCount;
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
	 */
	public boolean isLeaf(Object node)
	{
		if (node instanceof DefaultMutableTreeNode)
		{
			DefaultMutableTreeNode n = (DefaultMutableTreeNode)node;
			Object obj = n.getUserObject();
			if (obj != null && obj instanceof IDrawingProperty)
			{
				IDrawingProperty ele = (IDrawingProperty)obj;
				String name = ele.getResourceName();
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
	
}

