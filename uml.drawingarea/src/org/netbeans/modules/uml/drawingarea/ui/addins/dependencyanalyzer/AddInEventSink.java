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


package org.netbeans.modules.uml.drawingarea.ui.addins.dependencyanalyzer;

import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeContextMenuEventsSink;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuSelectionHandler;

/**
 * @author sumitabhk
 *
 */
public class AddInEventSink implements IProjectTreeContextMenuEventsSink,
                                       IProductContextMenuSelectionHandler
{
	private DependencyAnalyzerAddIn m_Parent = null;

	/**
	 * 
	 */
	public AddInEventSink()
	{
		super();
	}

	/**
	 * Fired when the context menu is about to be displayed.
	 */
	public void onProjectTreeContextMenuPrepare(IProjectTreeControl pParentControl, IProductContextMenu contextMenu, IResultCell cell)
	{
		if (m_Parent != null)
		{
			m_Parent.onProjectTreeContextMenuPrepare(pParentControl, contextMenu, cell);
		}
		
	}

	/**
	 * Fired when the context menu has been populated.
	 * Use this to override the implementation of the buttons.
	 */
	public void onProjectTreeContextMenuPrepared(IProjectTreeControl pParentControl, IProductContextMenu contextMenu, IResultCell cell)
	{
		
	}

	/**
	 * Fired when someone should handle the display.
	 */
	public void onProjectTreeContextMenuHandleDisplay(IProjectTreeControl pParentControl, IProductContextMenu contextMenu, IResultCell cell)
	{
		
	}

	/**
	 * Fired when a context menu item has been selected.
	 */
	public void onProjectTreeContextMenuSelected(IProjectTreeControl pParentControl, IProductContextMenu contextMenu, IProductContextMenuItem selectedItem, IResultCell cell)
	{
		
	}

	/**
	 * Fired when the context menu is about to be displayed.
	 */
	public void onDrawingAreaContextMenuPrepare(IDiagram pParentDiagram, IProductContextMenu contextMenu, IResultCell cell)
	{
		if (m_Parent != null)
		{
			m_Parent.onDrawingAreaContextMenuPrepare(pParentDiagram, contextMenu, cell);
		}
		
	}

	/**
	 * Fired when the context menu has been populated.
	 * Use this to override the implementation of the buttons.
	 */
	public void onDrawingAreaContextMenuPrepared(IDiagram pParentDiagram, IProductContextMenu contextMenu, IResultCell cell)
	{
		
	}

	/**
	 * Fired when someone should handle the display.
	 */
	public void onDrawingAreaContextMenuHandleDisplay(IDiagram pParentDiagram, IProductContextMenu contextMenu, IResultCell cell)
	{
		
	}

	/**
	 * Fired when a context menu item has been selected.
	 */
	public void onDrawingAreaContextMenuSelected(IDiagram pParentDiagram, IProductContextMenu contextMenu, IProductContextMenuItem selectedItem, IResultCell cell)
	{
		
	}

	/**
	 * If an external interface handles the display of the popup menu then this is called to handle the selection event
	 */
	public void handleSelection(IProductContextMenu pContextMenu, IProductContextMenuItem pSelectedItem)
	{
		if (m_Parent != null)
		{
			m_Parent.handleSelection(pContextMenu, pSelectedItem);
		}
		
	}

	public void setParent(DependencyAnalyzerAddIn addin)
	{
		m_Parent = addin;
	}

}



