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

package org.netbeans.modules.uml.designpattern;

import org.netbeans.modules.uml.ui.products.ad.projecttreedefaultengine.FilteredItemManager;
import java.awt.datatransfer.Transferable;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspacePreCreateEventPayload;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEditVerify;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeExpandingContext;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeHandled;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuSelectionHandler;

/**
 * @author sumitabhk
 *
 */
// TODO: meteora
public class CatalogEventSink implements IProjectTreeEventsSink,
//										IDrawingAreaContextMenuEventsSink,
										IProductContextMenuSelectionHandler,
//										IDrawingAreaEventsSink,
										IProjectEventsSink,
										IWorkspaceEventsSink,
										IWSProjectEventsSink,
										ICoreProductInitEventsSink
{
	private DesignPatternCatalog m_Parent = null;

	/**
	 *
	 */
	public CatalogEventSink()
	{
		super();
	}
	// IProjectTreeEvents
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onItemExpanding(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeExpandingContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onItemExpanding(IProjectTreeControl pParentControl, IProjectTreeExpandingContext pContext, IResultCell cell)
	{
	}

   public void onItemExpandingWithFilter(IProjectTreeControl pParentControl,
                                          IProjectTreeExpandingContext pContext,
                                          FilteredItemManager manager, IResultCell cell)
    {
       onItemExpanding(pParentControl, pContext, cell);
    }

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onBeforeEdit(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEditVerify, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onBeforeEdit(IProjectTreeControl pParentControl, IProjectTreeItem pItem, IProjectTreeEditVerify pVerify, IResultCell cell)
	{
            // since we are listening for the design pattern catalog,
            // we only want to respond to events being sent by the design center tree
            // so we do this check so that we don't respond to the project tree events
            if (pParentControl != null) 
            {
		String sMgrName = pParentControl.getConfigMgrName();
		if (sMgrName.equals("DesignCenter"))
		{
			 // Forward to the parent if interested.
			 m_Parent.onBeforeEdit(pParentControl, pItem, pVerify);
		}
            }
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onAfterEdit(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEditVerify, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onAfterEdit(IProjectTreeControl pParentControl, IProjectTreeItem pItem, IProjectTreeEditVerify pVerify, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onDoubleClick(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, boolean, boolean, boolean, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDoubleClick(IProjectTreeControl pParentControl, IProjectTreeItem pItem, boolean isControl, boolean isShift, boolean isAlt, boolean isMeta, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onSelChanged(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem[], org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onSelChanged(IProjectTreeControl pParentControl, IProjectTreeItem[] pItem, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onRightButtonDown(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeHandled, int, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onRightButtonDown(IProjectTreeControl pParentControl, IProjectTreeItem pItem, IProjectTreeHandled pHandled, int nScreenLocX, int nScreenLocY, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onBeginDrag(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem[], org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onBeginDrag(IProjectTreeControl pParentControl, IProjectTreeItem[] pItem, IProjectTreeDragVerify pVerify, IResultCell cell)
	{
            // since we are listening for the design pattern catalog,
            // we only want to respond to events being sent by the design center tree
            // so we do this check so that we don't respond to the project tree events
            if (pParentControl != null) 
            {
		String sMgrName = pParentControl.getConfigMgrName();
		if (sMgrName.equals("DesignCenter"))
		{
			 // Forward to the parent if interested.
			 m_Parent.onBeginDrag(pParentControl, pItem, pVerify);
		}
            }
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onMoveDrag(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, java.awt.datatransfer.Transferable, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onMoveDrag(IProjectTreeControl pParentControl, Transferable pItem, IProjectTreeDragVerify pVerify, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onEndDrag(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, java.awt.datatransfer.Transferable, int, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onEndDrag(IProjectTreeControl pParentControl, Transferable pItem, int action, IProjectTreeDragVerify pVerify, IResultCell cell)
	{
	}

	// IDrawingAreaContextMenuEventsSink
	/**
	 * Fired when the context menu is about to be displayed.
	 */
	public void onDrawingAreaContextMenuPrepare(IDiagram pParentDiagram, IProductContextMenu contextMenu, IResultCell cell)
	{
		if (m_Parent != null)
		{
			m_Parent.onDrawingAreaContextMenuPrepare(pParentDiagram, contextMenu);
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
	// IProductContextMenuSelectionHandler
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
	// IDrawingAreaEventsSink
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPreCreated(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
//	public void onDrawingAreaPreCreated(IDrawingAreaControl pDiagramControl, IResultCell cell)
//	{
//	}
//
//	/* (non-Javadoc)
//	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPostCreated(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
//	 */
//	public void onDrawingAreaPostCreated(IDrawingAreaControl pDiagramControl, IResultCell cell)
//	{
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaOpened(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaOpened(IDiagram pParentDiagram, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaClosed(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaClosed(IDiagram pParentDiagram, boolean bDiagramIsDirty, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPreSave(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaPreSave(IProxyDiagram pParentDiagram, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPostSave(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaPostSave(IProxyDiagram pParentDiagram, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaKeyDown(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, int, boolean, boolean, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaKeyDown(IDiagram pParentDiagram, int nKeyCode, boolean bControlIsDown, boolean bShiftIsDown, boolean bAltIsDown, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPrePropertyChange(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaPrePropertyChange(IProxyDiagram pProxyDiagram, int nPropertyKindChanged, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPostPropertyChange(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaPostPropertyChange(IProxyDiagram pProxyDiagram, int nPropertyKindChanged, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaTooltipPreDisplay(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement, org.netbeans.modules.uml.ui.support.viewfactorysupport.IToolTipData, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
//	public void onDrawingAreaTooltipPreDisplay(IDiagram pParentDiagram, IPresentationElement pPE, IToolTipData pTooltip, IResultCell cell)
//	{
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaActivated(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaActivated(IDiagram pParentDiagram, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPreDrop(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaDropContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
//	public void onDrawingAreaPreDrop(IDiagram pParentDiagram, IDrawingAreaDropContext pContext, IResultCell cell)
//	{
//		if (m_Parent != null)
//		{
//			m_Parent.onDrawingAreaPreDrop(pParentDiagram, pContext, cell);
//		}
//	}
//
//	/* (non-Javadoc)
//	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPostDrop(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaDropContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
//	 */
//	public void onDrawingAreaPostDrop(IDiagram pParentDiagram, IDrawingAreaDropContext pContext, IResultCell cell)
//	{
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPreFileRemoved(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaPreFileRemoved(String sFilename, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaFileRemoved(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaFileRemoved(String sFilename, IResultCell cell)
	{
	}
	// ICoreProductInitEventsSink
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductPreInit(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onCoreProductPreInit(ICoreProduct pVal, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductInitialized(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onCoreProductInitialized(ICoreProduct newVal, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductPreQuit(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onCoreProductPreQuit(ICoreProduct pProduct, IResultCell cell)
	{
		if (m_Parent != null)
		{
			m_Parent.onCoreProductPreQuit(pProduct);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductPreSaved(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onCoreProductPreSaved(ICoreProduct pProduct, IResultCell cell)
	{
		if (m_Parent != null)
		{
			m_Parent.onCoreProductPreSaved(pProduct);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductSaved(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onCoreProductSaved(ICoreProduct newVal, IResultCell cell)
	{
	}
	// IProjectEventsSink
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onPreModeModified(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreModeModified(IProject pProject, String newValue, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onModeModified(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onModeModified(IProject pProject, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onPreDefaultLanguageModified(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreDefaultLanguageModified(IProject pProject, String newValue, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onDefaultLanguageModified(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDefaultLanguageModified(IProject pProject, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectPreCreate(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectPreCreate(IWorkspace space, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectCreated(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectCreated(IProject pProject, IResultCell cell)
	{
		if (m_Parent != null)
		{
			m_Parent.onProjectCreated(pProject);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectPreOpen(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectPreOpen(IWorkspace space, String projName, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectOpened(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectOpened(IProject pProject, IResultCell cell)
	{
		if (m_Parent != null)
		{
			m_Parent.onProjectOpened(pProject);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectPreRename(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectPreRename(IProject pProject, String newName, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectRenamed(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectRenamed(IProject pProject, String oldName, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectPreClose(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectPreClose(IProject pProject, IResultCell cell)
	{
      if (m_Parent != null)
      {
         m_Parent.onProjectPreClosed(pProject, cell);
      }
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectClosed(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectClosed(IProject pProject, IResultCell cell)
	{
		if (m_Parent != null)
		{
			m_Parent.onProjectClosed(pProject);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectPreSave(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectPreSave(IProject pProject, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectSaved(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectSaved(IProject pProject, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onPreReferencedLibraryAdded(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreReferencedLibraryAdded(IProject pProject, String refLibLoc, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onReferencedLibraryAdded(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onReferencedLibraryAdded(IProject pProject, String refLibLoc, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onPreReferencedLibraryRemoved(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreReferencedLibraryRemoved(IProject pProject, String refLibLoc, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onReferencedLibraryRemoved(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onReferencedLibraryRemoved(IProject pProject, String refLibLoc, IResultCell cell)
	{
	}
	// IWorkspaceEventsSink
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspacePreCreate(org.netbeans.modules.uml.core.workspacemanagement.IWorkspacePreCreateEventPayload, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWorkspacePreCreate(IWorkspacePreCreateEventPayload pEvent, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspaceCreated(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWorkspaceCreated(IWorkspace space, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspacePreOpen(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWorkspacePreOpen(String fileName, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspaceOpened(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWorkspaceOpened(IWorkspace space, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspacePreSave(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWorkspacePreSave(String fileName, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspaceSaved(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWorkspaceSaved(IWorkspace space, IResultCell cell)
	{
		if (m_Parent != null)
		{
			m_Parent.onWorkspaceSaved(space);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspacePreClose(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWorkspacePreClose(IWorkspace space, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspaceClosed(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWorkspaceClosed(IWorkspace space, IResultCell cell)
	{
	}
	// IWSProjectEventsSink
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreCreate(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectPreCreate(IWorkspace space, String projectName, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectCreated(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectCreated(IWSProject project, IResultCell cell)
	{
		if (m_Parent != null)
		{
			m_Parent.onWSProjectCreated(project);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreOpen(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectPreOpen(IWorkspace space, String projName, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectOpened(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectOpened(IWSProject project, IResultCell cell)
	{
		if (m_Parent != null)
		{
			m_Parent.onWSProjectOpened(project);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreRemove(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectPreRemove(IWSProject project, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectRemoved(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectRemoved(IWSProject project, IResultCell cell)
	{
		if (m_Parent != null)
		{
			m_Parent.onWSProjectRemoved(project);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreInsert(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectPreInsert(IWorkspace space, String projectName, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectInserted(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectInserted(IWSProject project, IResultCell cell)
	{
		if (m_Parent != null)
		{
			m_Parent.onWSProjectInserted(project);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreRename(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectPreRename(IWSProject project, String newName, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectRenamed(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectRenamed(IWSProject project, String oldName, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreClose(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectPreClose(IWSProject project, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectClosed(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectClosed(IWSProject project, IResultCell cell)
	{
		if (m_Parent != null)
		{
			m_Parent.onWSProjectClosed(project);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreSave(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectPreSave(IWSProject project, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectSaved(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectSaved(IWSProject project, IResultCell cell)
	{
		if (m_Parent != null)
		{
			m_Parent.onWSProjectSaved(project);
		}
	}

	public void setParent(DesignPatternCatalog addin)
	{
		m_Parent = addin;
	}
}


