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



package org.netbeans.modules.uml.ui.controls.doccontrol;

import org.netbeans.modules.uml.ui.products.ad.projecttreedefaultengine.FilteredItemManager;
import java.awt.datatransfer.Transferable;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDocumentationModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink;
import org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventsSink;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.workspacemanagement.IWSElement;
import org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspacePreCreateEventPayload;
//import org.netbeans.modules.uml.ui.controls.drawingarea.DiagramAreaEnumerations;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEditVerify;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeExpandingContext;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeHandled;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
//import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
//import org.netbeans.modules.uml.ui.support.viewfactorysupport.IToolTipData;
//import org.netbeans.modules.uml.ui.swing.drawingarea.ICompartmentEventsSink;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaDropContext;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaSelectionEventsSink;

/**
 * @author sumitabhk
 *
 */
public class DocumentControlEventHandler implements 
//        IDrawingAreaEventsSink, IDrawingAreaSelectionEventsSink,
													IElementModifiedEventsSink, INamedElementEventsSink,
													IProjectTreeEventsSink, IElementLifeTimeEventsSink,
//													ICompartmentEventsSink, 
                                                                                                        IDocumentationModifiedEventsSink,
													IWorkspaceEventsSink, IWSProjectEventsSink,
													IPreferenceManagerEventsSink, IWSElementEventsSink,
													ICoreProductInitEventsSink, IProjectEventsSink
{
	private DocumentationControl m_DocCtrl = null;

	/**
	 * 
	 */
	public DocumentControlEventHandler()
	{
		super();
	}
	
	/**
	 *
	 * Sets member with CDocCtrl pointer.
	 *
	 * @param CDocCtrl* pDocCtrl[in]
	 *
	 * @return void
	 *
	 */
   void setDocCtrl(DocumentationControl pDocCtrl)
	{
		m_DocCtrl = pDocCtrl;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPreCreated(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
//	public void onDrawingAreaPreCreated(IDrawingAreaControl pDiagramControl, IResultCell cell)
//	{
//		// TODO Auto-generated method stub
//		
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPostCreated(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
//	public void onDrawingAreaPostCreated(IDrawingAreaControl pDiagramControl, IResultCell cell)
//	{
//		// TODO Auto-generated method stub
//		
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaOpened(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaOpened(IDiagram pParentDiagram, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaClosed(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaClosed(IDiagram pParentDiagram, boolean bDiagramIsDirty, IResultCell cell)
	{
		if (m_DocCtrl != null)
		{
			m_DocCtrl.onDrawingAreaClosed(pParentDiagram, bDiagramIsDirty, cell);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPreSave(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaPreSave(IProxyDiagram pParentDiagram, IResultCell cell)
	{
		if (m_DocCtrl != null)
		{
			m_DocCtrl.onDrawingAreaPreSave(pParentDiagram, cell);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPostSave(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaPostSave(IProxyDiagram pParentDiagram, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaKeyDown(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, int, boolean, boolean, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaKeyDown(IDiagram pParentDiagram, int nKeyCode, boolean bControlIsDown, boolean bShiftIsDown, boolean bAltIsDown, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPrePropertyChange(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaPrePropertyChange(IProxyDiagram pProxyDiagram, int nPropertyKindChanged, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPostPropertyChange(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaPostPropertyChange(IProxyDiagram pProxyDiagram, int nPropertyKindChanged, IResultCell cell)
	{
//		if (m_DocCtrl != null && nPropertyKindChanged == DiagramAreaEnumerations.DAPK_DOCUMENTATION)
//		{
//			m_DocCtrl.onDrawingAreaDocumentationModified(pProxyDiagram);
//		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaTooltipPreDisplay(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement, org.netbeans.modules.uml.ui.support.viewfactorysupport.IToolTipData, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
//	public void onDrawingAreaTooltipPreDisplay(IDiagram pParentDiagram, IPresentationElement pPE, IToolTipData pTooltip, IResultCell cell)
//	{
//		// TODO Auto-generated method stub
//		
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaActivated(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaActivated(IDiagram pParentDiagram, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPreDrop(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaDropContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
//	public void onDrawingAreaPreDrop(IDiagram pParentDiagram, IDrawingAreaDropContext pContext, IResultCell cell)
//	{
//		// TODO Auto-generated method stub
//		
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPostDrop(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaDropContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
//	public void onDrawingAreaPostDrop(IDiagram pParentDiagram, IDrawingAreaDropContext pContext, IResultCell cell)
//	{
//		// TODO Auto-generated method stub
//		
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPreFileRemoved(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaPreFileRemoved(String sFilename, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaFileRemoved(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaFileRemoved(String sFilename, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaSelectionEventsSink#onSelect(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram)
	 */
//	public void onSelect(IDiagram pParentDiagram, ETList<IPresentationElement> selectedItems, ICompartment pCompartment, IResultCell cell )
//	{
//		if (m_DocCtrl != null && selectedItems != null)
//		{
//			if (pCompartment == null)
//			{
//				m_DocCtrl.onDrawingAreaSelect(pParentDiagram, selectedItems);
//			}
//			else
//			{
//				m_DocCtrl.onCompartmentSelected(pCompartment, true, null);
//			}
//		}
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaSelectionEventsSink#onUnselect(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement[], org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onUnselect(IDiagram pParentDiagram, IPresentationElement[] unselectedItems, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementModifiedEventsSink#onElementPreModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementPreModified(IVersionableElement element, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementModifiedEventsSink#onElementModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementModified(IVersionableElement element, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onPreNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreNameModified(INamedElement element, String proposedName, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onNameModified(INamedElement element, IResultCell cell)
	{
		if (m_DocCtrl != null)
		{
			m_DocCtrl.onNameModified(element);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onPreVisibilityModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreVisibilityModified(INamedElement element, int proposedValue, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onVisibilityModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onVisibilityModified(INamedElement element, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onPreAliasNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreAliasNameModified(INamedElement element, String proposedName, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onAliasNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onAliasNameModified(INamedElement element, IResultCell cell)
	{
		if (m_DocCtrl != null)
		{
			m_DocCtrl.onAliasNameModified(element);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onPreNameCollision(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, java.lang.String)
	 */
	public void onPreNameCollision(INamedElement element, String proposedName, ETList<INamedElement> collidingElements, IResultCell cell )
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onNameCollision(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement)
	 */
	public void onNameCollision(INamedElement element, ETList<INamedElement> collidingElements, IResultCell cell )
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onItemExpanding(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeExpandingContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onItemExpanding(IProjectTreeControl pParentControl, IProjectTreeExpandingContext pContext, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onAfterEdit(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEditVerify, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onAfterEdit(IProjectTreeControl pParentControl, IProjectTreeItem pItem, IProjectTreeEditVerify pVerify, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onDoubleClick(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, boolean, boolean, boolean, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDoubleClick(IProjectTreeControl pParentControl, IProjectTreeItem pItem, boolean isControl, boolean isShift, boolean isAlt, boolean isMeta, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onSelChanged(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem[], org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onSelChanged(IProjectTreeControl pParentControl, IProjectTreeItem[] pItem, IResultCell cell)
	{
		if (m_DocCtrl != null)
		{
			m_DocCtrl.onProjectTreeSelChanged(pItem);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onRightButtonDown(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeHandled, int, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onRightButtonDown(IProjectTreeControl pParentControl, IProjectTreeItem pItem, IProjectTreeHandled pHandled, int nScreenLocX, int nScreenLocY, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onBeginDrag(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem[], org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onBeginDrag(IProjectTreeControl pParentControl, IProjectTreeItem[] pItem, IProjectTreeDragVerify pVerify, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onMoveDrag(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, java.awt.datatransfer.Transferable, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onMoveDrag(IProjectTreeControl pParentControl, Transferable pItem, IProjectTreeDragVerify pVerify, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onEndDrag(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, java.awt.datatransfer.Transferable, int, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onEndDrag(IProjectTreeControl pParentControl, Transferable pItem, int action, IProjectTreeDragVerify pVerify, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementPreCreate(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementPreCreate(String ElementType, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementCreated(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementCreated(IVersionableElement element, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementPreDelete(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementPreDelete(IVersionableElement element, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementDeleted(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementDeleted(IVersionableElement element, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementPreDuplicated(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementPreDuplicated(IVersionableElement element, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementDuplicated(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementDuplicated(IVersionableElement element, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.ICompartmentEventsSink#onCompartmentSelected(org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
//	public void onCompartmentSelected(ICompartment pCompartment, boolean bSelected, IResultCell cell)
//	{
//		if (m_DocCtrl != null)
//		{
//			m_DocCtrl.onCompartmentSelected(pCompartment, bSelected, cell);
//		}
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.ICompartmentEventsSink#onCompartmentCollapsed(org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
//	public void onCompartmentCollapsed(ICompartment pCompartment, boolean bCollapsed, IResultCell cell)
//	{
//		// TODO Auto-generated method stub
//		
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IDocumentationModifiedEventsSink#onDocumentationPreModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDocumentationPreModified(IElement element, String doc, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IDocumentationModifiedEventsSink#onDocumentationModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDocumentationModified(IElement element, IResultCell cell)
	{
		if (m_DocCtrl != null)
		{
			m_DocCtrl.onDocumentModified(element);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspacePreCreate(org.netbeans.modules.uml.core.workspacemanagement.IWorkspacePreCreateEventPayload, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWorkspacePreCreate(IWorkspacePreCreateEventPayload pEvent, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspaceCreated(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWorkspaceCreated(IWorkspace space, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspacePreOpen(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWorkspacePreOpen(String fileName, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspaceOpened(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWorkspaceOpened(IWorkspace space, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspacePreSave(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWorkspacePreSave(String fileName, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspaceSaved(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWorkspaceSaved(IWorkspace space, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspacePreClose(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWorkspacePreClose(IWorkspace space, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspaceClosed(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWorkspaceClosed(IWorkspace space, IResultCell cell)
	{
		if (m_DocCtrl != null)
		{
			m_DocCtrl.onWorkspaceClosed();
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreCreate(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectPreCreate(IWorkspace space, String projectName, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectCreated(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectCreated(IWSProject project, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreOpen(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectPreOpen(IWorkspace space, String projName, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectOpened(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectOpened(IWSProject project, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreRemove(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectPreRemove(IWSProject project, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectRemoved(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectRemoved(IWSProject project, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreInsert(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectPreInsert(IWorkspace space, String projectName, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectInserted(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectInserted(IWSProject project, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreRename(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectPreRename(IWSProject project, String newName, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectRenamed(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectRenamed(IWSProject project, String oldName, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreClose(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectPreClose(IWSProject project, IResultCell cell)
	{
		if (m_DocCtrl != null)
		{
			m_DocCtrl.onWSProjectPreClose();
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectClosed(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectClosed(IWSProject project, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreSave(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectPreSave(IWSProject project, IResultCell cell)
	{
		if (m_DocCtrl != null)
		{
			m_DocCtrl.onWSProjectPreSave();
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectSaved(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectSaved(IWSProject project, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventsSink#onPreferenceChange(java.lang.String, org.netbeans.modules.uml.core.support.umlutils.IPropertyElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreferenceChange(String name, IPropertyElement pElement, IResultCell cell)
	{
		if (m_DocCtrl != null)
		{
			m_DocCtrl.onPreferenceChange(name, pElement);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventsSink#onPreferenceAdd(java.lang.String, org.netbeans.modules.uml.core.support.umlutils.IPropertyElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreferenceAdd(String Name, IPropertyElement pElement, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventsSink#onPreferenceRemove(java.lang.String, org.netbeans.modules.uml.core.support.umlutils.IPropertyElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreferenceRemove(String Name, IPropertyElement pElement, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventsSink#onPreferencesChange(org.netbeans.modules.uml.core.support.umlutils.IPropertyElement[], org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreferencesChange(IPropertyElement[] pElements, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementPreCreate(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, java.lang.String, java.lang.String, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementPreCreate(IWSProject wsProject, String location, String Name, String data, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementCreated(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementCreated(IWSElement element, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementPreSave(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementPreSave(IWSElement element, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementSaved(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementSaved(IWSElement element, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementPreRemove(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementPreRemove(IWSElement element, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementRemoved(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementRemoved(IWSElement element, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementPreNameChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementPreNameChanged(IWSElement element, String proposedValue, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementNameChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementNameChanged(IWSElement element, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementPreOwnerChange(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementPreOwnerChange(IWSElement element, IWSProject newOwner, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementOwnerChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementOwnerChanged(IWSElement element, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementPreLocationChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementPreLocationChanged(IWSElement element, String proposedLocation, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementLocationChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementLocationChanged(IWSElement element, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementPreDataChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementPreDataChanged(IWSElement element, String newData, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementDataChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementDataChanged(IWSElement element, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementPreDocChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementPreDocChanged(IWSElement element, String doc, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementDocChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementDocChanged(IWSElement element, IResultCell cell)
	{
		if (m_DocCtrl != null)
		{
			m_DocCtrl.onWSElementDocChanged(element, cell);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementPreAliasChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementPreAliasChanged(IWSElement element, String proposedValue, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementAliasChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSElementAliasChanged(IWSElement element, IResultCell cell)
	{
		if (m_DocCtrl != null)
		{
			m_DocCtrl.onWSElementAliasChanged(element);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductPreInit(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onCoreProductPreInit(ICoreProduct pVal, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductInitialized(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onCoreProductInitialized(ICoreProduct newVal, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductPreQuit(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onCoreProductPreQuit(ICoreProduct pVal, IResultCell cell)
	{
		if (m_DocCtrl != null)
		{
			m_DocCtrl.connectSinks(false);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductPreSaved(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onCoreProductPreSaved(ICoreProduct pVal, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductSaved(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onCoreProductSaved(ICoreProduct newVal, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onPreModeModified(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreModeModified(IProject pProject, String newValue, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onModeModified(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onModeModified(IProject pProject, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onPreDefaultLanguageModified(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreDefaultLanguageModified(IProject pProject, String newValue, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onDefaultLanguageModified(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDefaultLanguageModified(IProject pProject, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectPreCreate(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectPreCreate(IWorkspace space, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectCreated(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectCreated(IProject Project, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectPreOpen(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectPreOpen(IWorkspace space, String projName, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectOpened(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectOpened(IProject project, IResultCell cell)
	{
		if (m_DocCtrl != null)
		{
			m_DocCtrl.onProjectOpened(project);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectPreRename(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectPreRename(IProject Project, String newName, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectRenamed(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectRenamed(IProject Project, String oldName, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectPreClose(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectPreClose(IProject Project, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectClosed(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectClosed(IProject project, IResultCell cell)
	{
		if (m_DocCtrl != null)
		{
			m_DocCtrl.onProjectClosed(project);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectPreSave(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectPreSave(IProject Project, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectSaved(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectSaved(IProject Project, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onPreReferencedLibraryAdded(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreReferencedLibraryAdded(IProject Project, String refLibLoc, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onReferencedLibraryAdded(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onReferencedLibraryAdded(IProject Project, String refLibLoc, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onPreReferencedLibraryRemoved(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreReferencedLibraryRemoved(IProject Project, String refLibLoc, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onReferencedLibraryRemoved(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onReferencedLibraryRemoved(IProject Project, String refLibLoc, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

}



