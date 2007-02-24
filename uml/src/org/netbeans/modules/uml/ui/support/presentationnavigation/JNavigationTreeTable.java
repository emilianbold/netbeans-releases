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



package org.netbeans.modules.uml.ui.support.presentationnavigation;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.ui.support.DiagramBuilder;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.IPresentationTarget;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingNavigationDialog;
import org.netbeans.modules.uml.ui.swing.treetable.JTreeTable;
import org.netbeans.modules.uml.ui.swing.treetable.PropertyValueCellRenderer;
import org.netbeans.modules.uml.ui.swing.treetable.TreeTableModel;

/**
 * @author sumitabhk
 *
 * 
 */
public class JNavigationTreeTable extends JTreeTable
{
	private TreePath m_Selected = null;
	private CommonResourceManager m_Manager = CommonResourceManager.instance();
	private SwingNavigationDialog m_Parent = null;
	
	/**
	 * 
	 */
	public JNavigationTreeTable(TreeTableModel model)
	{
		this(model, (TreeTableCellRenderer)null);
	}

	public JNavigationTreeTable(TreeTableModel model, SwingNavigationDialog parent)
	{
		this(model, (TreeTableCellRenderer)null);
		m_Parent = parent;
	}

	public JNavigationTreeTable(TreeTableModel model, TreeTableCellRenderer renderer)
	{
		super(model, renderer);
		
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		TableColumnModel colMod = getColumnModel();
		
		PropertyValueCellRenderer valueRenderer = new PropertyValueCellRenderer();
		colMod.getColumn(0).setCellRenderer(tree);
		
		colMod.getColumn(1).setCellRenderer(valueRenderer);
		colMod.getColumn(2).setCellRenderer(valueRenderer);
		colMod.getColumn(3).setCellRenderer(valueRenderer);

		addMouseListener(new TreeMouseHandler());
		
	}

	public boolean editCellAt(int row, int column, EventObject e)
	{
		return false;
	}
	
	public TreePath getSelectedPath()
	{
		return m_Selected;
	}
	
	public void expandFirstLevelNodes()
	{
		TreeModel model = getTree().getModel();
		Object obj = model.getRoot();
		if (obj instanceof DefaultMutableTreeNode)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)obj;
			int count = node.getChildCount();
			for (int i=count-1; i>=0; i--)
			{
				tree.expandNode(i, true);
			}
		}
	}
	
	public void handleNavigation(TreePath path)
	{
		//we want to navigate to the double clicked element.
		if (path != null)
		{
			Object obj = path.getLastPathComponent();
			if (obj != null && obj instanceof DefaultMutableTreeNode)
			{
				Object userObj = ((DefaultMutableTreeNode)obj).getUserObject();
				if (userObj != null)
				{
					DiagramBuilder builder = new DiagramBuilder();
					if (userObj instanceof IProxyDiagram)
					{
						IProxyDiagram dia = (IProxyDiagram)userObj;
						String filename = dia.getFilename();
						String presId = dia.getXMIID();
						if (m_Parent != null)
						{
							m_Parent.setTargetXMIID(presId);
						}
						if (filename != null && filename.length() > 0)
						{
							builder.navigateToDiagram(filename, presId, "", "");
						}
					}
					else if (userObj instanceof IPresentationTarget)
					{
						IPresentationTarget target = (IPresentationTarget)userObj;
						String id = target.getPresentationID();
						if (m_Parent != null)
						{
							m_Parent.setTargetXMIID(id);
						}
						builder.navigateToTarget(target);
					}
					else if (userObj instanceof IElement)
					{
						IElement elem = (IElement)userObj;
						String id = elem.getXMIID();
						if (m_Parent != null)
						{
							m_Parent.setTargetXMIID(id);
						}
						builder.navigateToElementInTree(elem);
					}
				}
			}
		}
	}

	public class TreeMouseHandler extends MouseInputAdapter
	{
	   public void mousePressed(MouseEvent e) 
	   {
			int selRow = rowAtPoint(e.getPoint());
			TreePath selPath = getTree().getPathForRow(selRow);
			//TreePath selPath = getTree().getPathForLocation(e.getX(), e.getY());
			if(selRow != -1) 
			{
				if (e.getClickCount() == 1)
				{
					m_Selected = selPath;
					
					//if parent is not null, then set element type on that
					if (m_Parent != null)
					{
						Object obj = selPath.getLastPathComponent();
						if (obj != null && obj instanceof DefaultMutableTreeNode)
						{
							Object userObj = ((DefaultMutableTreeNode)obj).getUserObject();
							if (userObj != null)
							{
								if (userObj instanceof IProxyDiagram)
								{
									m_Parent.setIsDiagram(true);
								}
								else
								{
									m_Parent.setIsDiagram(false);
								}
							}
						}
					}
					
					if (!tree.isExpanded(selPath))
					{
						getTree().expandPath(selPath);
					}
					else
					{
						getTree().collapsePath(selPath);
					}
				}
				else if (e.getClickCount() == 2)
				{
					handleNavigation(selPath);
				}
			}
	   }
	   
	}
	
	public class NavigationTreeCellRenderer extends TreeTableCellRenderer
	{
		public NavigationTreeCellRenderer(TreeTableCellRenderer renderer)
		{
			super();
			tree = renderer;
		}
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
														boolean hasFocus, int row, int column)
		{
			Component retObj = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (column == 0)
			{
				TreePath path = tree.getPathForRow(row);
				Object tempNode = path.getLastPathComponent();
				if (tempNode != null && tempNode instanceof DefaultMutableTreeNode)
				{
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)tempNode;
					Object obj = node.getUserObject();
					if (obj != null )
					{
						if (obj instanceof IPresentationTarget)
						{
							JLabel retObj1 = new JLabel();
							retObj1.setIcon(m_Manager.getIconForDisp(obj));
							return retObj1;
						}
						else if (obj instanceof IProxyDiagram)
						{
							JLabel retObj1 = new JLabel();
							retObj1.setIcon(m_Manager.getIconForDisp(obj));
							return retObj1;
						}
						else if (obj instanceof IElement)
						{
							JLabel retObj1 = new JLabel();
							retObj1.setIcon(m_Manager.getIconForDisp(obj));
							return retObj1;
						}
					}
				}
			}

			return retObj;
		}
		
	}
}

