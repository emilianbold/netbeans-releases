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
 * Created on Mar 9, 2004
 *
 */
package org.netbeans.modules.uml.ui.addins.associateDialog;

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeContextMenuEventsSink;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuSelectionHandler;

/**
 * @author jingmingm
 *
 */
public class AddInEventSink implements IProjectTreeContextMenuEventsSink, IProductContextMenuSelectionHandler
{
	private AssociateDlgAddIn m_Parent = null;

		/**
		 * 
		 */
		public AddInEventSink()
		{
			super();
			// TODO Auto-generated constructor stub
		}

		/* (non-Javadoc)
		 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeContextMenuEventsSink#onProjectTreeContextMenuPrepare(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
		 */
		public void onProjectTreeContextMenuPrepare(IProjectTreeControl pParentControl, IProductContextMenu contextMenu, IResultCell cell)
		{
			if (m_Parent != null)
			{
				//m_Parent.onProjectTreeContextMenuPrepare(pParentControl, contextMenu);
			}
		
		}

		/* (non-Javadoc)
		 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeContextMenuEventsSink#onProjectTreeContextMenuPrepared(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
		 */
		public void onProjectTreeContextMenuPrepared(IProjectTreeControl pParentControl, IProductContextMenu contextMenu, IResultCell cell)
		{
		
		}

		/* (non-Javadoc)
		 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeContextMenuEventsSink#onProjectTreeContextMenuHandleDisplay(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
		 */
		public void onProjectTreeContextMenuHandleDisplay(IProjectTreeControl pParentControl, IProductContextMenu contextMenu, IResultCell cell)
		{
		
		}

		/* (non-Javadoc)
		 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeContextMenuEventsSink#onProjectTreeContextMenuSelected(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
		 */
		public void onProjectTreeContextMenuSelected(IProjectTreeControl pParentControl, IProductContextMenu contextMenu, IProductContextMenuItem selectedItem, IResultCell cell)
		{
		
		}

		/**
		 * If an external interface handles the display of the popup menu then this is called to handle the selection event
		 */
		public void handleSelection(IProductContextMenu pContextMenu, IProductContextMenuItem pSelectedItem)
		{
			if (m_Parent != null)
			{
				//m_Parent.handleSelection(pContextMenu, pSelectedItem);
			}
		
		}

		public void setParent(AssociateDlgAddIn addin)
		{
			m_Parent = addin;
		}
}



