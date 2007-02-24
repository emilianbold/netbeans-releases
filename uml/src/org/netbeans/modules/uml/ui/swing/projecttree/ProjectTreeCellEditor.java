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



package org.netbeans.modules.uml.ui.swing.projecttree;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.tree.TreeCellEditor;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.ui.controls.editcontrol.EditControlImpl;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeDiagramNode;
import org.netbeans.modules.uml.ui.controls.projecttree.TreeElementNode;

/**
 * @author sumitabhk
 *
 */
public class ProjectTreeCellEditor implements TreeCellEditor
{
	EditControlImpl m_EditControl = null;
	/**
	 * 
	 */
	public ProjectTreeCellEditor()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeCellEditor#getTreeCellEditorComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int)
	 */
	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row)
	{
		//TreePath path = tree.getPathForRow(row);
		//Object sel = path.getLastPathComponent();

		if (value instanceof TreeElementNode)
		{
			TreeElementNode node = (TreeElementNode)value;
			String str = node.getDisplayedName();
			m_EditControl = new EditControlImpl(this);
			m_EditControl.setBorder(null);
			m_EditControl.setOpaque(false);
			
			m_EditControl.setVisible(true);
			m_EditControl.setText(str);
			
			IProjectTreeItem item = node.getDataItem();
			IElement elem = item.getModelElement();
			m_EditControl.setElement(elem);
			
			return m_EditControl;
		}
      else if (value instanceof ProjectTreeDiagramNode)
      {
         //we want to allow editing of diagrams too.
         ProjectTreeDiagramNode node = (ProjectTreeDiagramNode)value;
         String str = node.getDisplayedName();
         m_EditControl = new EditControlImpl(this);
         m_EditControl.setBorder(null);
         m_EditControl.setOpaque(false);
         
         m_EditControl.setVisible(true);
         m_EditControl.setText(str);
         
         IProjectTreeItem item = node.getDataItem();
         IProxyDiagram pDia = item.getDiagram();
         if (pDia != null && pDia.isOpen())
         {
            IDiagram dia = pDia.getDiagram();
            m_EditControl.setElement(dia);
         }
         
         return m_EditControl;
      }
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	public Object getCellEditorValue()
	{
		// TODO Auto-generated method stub
		return m_EditControl;
	}

	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#isCellEditable(java.util.EventObject)
	 */
	public boolean isCellEditable(EventObject anEvent)
	{
		if (anEvent instanceof MouseEvent)
		{
         if (anEvent.getSource() instanceof JTree)
         {
            JTree tree = (JTree)anEvent.getSource();
            Object selObject = tree.getLastSelectedPathComponent();
            
            if (selObject instanceof TreeElementNode)
            {
               TreeElementNode node = (TreeElementNode)selObject;
               
               MouseEvent e = (MouseEvent)anEvent;
               if (e.getClickCount() == 3)
               {
                  return true;
               }
               if (e.getClickCount() == 1)
               {
               }               
            }
         }
         
		}
		
		if (anEvent == null)
		{
			//we are programmatically going in edit mode, so return true.
			return true;
		}
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#shouldSelectCell(java.util.EventObject)
	 */
	public boolean shouldSelectCell(EventObject anEvent)
	{
		// TODO Auto-generated method stub
		return false;
	}

        /* (non-Javadoc)
         * @see javax.swing.CellEditor#stopCellEditing()
         */
        public boolean stopCellEditing() {
            // TODO Auto-generated method stub
            ETSystem.out.println("Calling stopCellEditing");
            if (m_EditControl != null) {
                String sText = m_EditControl.getText();
                if( (sText == null) || (sText.equals("") == true) ) {
                    return false;
                }
                m_EditControl.handleSave();
            }
            return true;
        }

	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#cancelCellEditing()
	 */
	public void cancelCellEditing()
	{
		// TODO Auto-generated method stub
		ETSystem.out.println("Calling cancelCellEditing");
		if (m_EditControl != null)
		{
			m_EditControl.handleRollback();
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#addCellEditorListener(javax.swing.event.CellEditorListener)
	 */
	public void addCellEditorListener(CellEditorListener l)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#removeCellEditorListener(javax.swing.event.CellEditorListener)
	 */
	public void removeCellEditorListener(CellEditorListener l)
	{
		// TODO Auto-generated method stub
		
	}

}



