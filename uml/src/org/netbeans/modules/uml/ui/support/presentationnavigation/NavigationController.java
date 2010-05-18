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



package org.netbeans.modules.uml.ui.support.presentationnavigation;

import org.netbeans.modules.uml.ui.products.ad.projecttreedefaultengine.FilteredItemManager;
import java.awt.datatransfer.Transferable;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink;
import org.netbeans.modules.uml.core.coreapplication.INavigator;
import org.netbeans.modules.uml.core.coreapplication.INavigatorFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDocumentationModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.structure.IArtifact;
import org.netbeans.modules.uml.core.metamodel.structure.IArtifactEventsSink;
import org.netbeans.modules.uml.core.metamodel.structure.ISourceFileArtifact;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEditVerify;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeExpandingContext;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeHandled;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
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
 * TODO: meteora 
 */
public class NavigationController implements 
//        IDrawingAreaSelectionEventsSink,
//											 ICompartmentEventsSink, 
											 ICoreProductInitEventsSink, 
//											 IDrawingAreaEventsSink, 
											 IDocumentationModifiedEventsSink, 
											 IArtifactEventsSink, 
											 IElementLifeTimeEventsSink, 
											 IProjectTreeEventsSink
{
	private boolean m_SinksConnected = false;
	private INavigator m_Navigator = null;

	/**
	 * 
	 */
	public NavigationController()
	{
		super();
		
		connectSinks();
		
		// retrieve the core product
		IProduct prod = ProductHelper.getProduct();
		if (prod != null)
		{
			// Get the core product's navigator factory
			INavigatorFactory factory = prod.getNavigatorFactory();
			if (factory != null)
			{
				// Use the navigator factory to create a navigator
				m_Navigator = factory.createNavigator("");
			}
		}
	}

	/** 
	 * connects event sinks
	 */
	private void connectSinks()
	{
		if (!m_SinksConnected)
		{
			DispatchHelper helper = new DispatchHelper();
			helper.registerForDocumentationModifiedEvents(this);
//			helper.registerDrawingAreaEvents(this);
//			helper.registerDrawingAreaSelectionEvents(this);
//			helper.registerDrawingAreaCompartmentEvents(this);
			helper.registerForInitEvents(this);
			helper.registerForArtifactEvents(this);
			helper.registerForLifeTimeEvents(this);
			helper.registerProjectTreeEvents(this);
			m_SinksConnected = true;
		}
	}

	/** 
	 * disconnects event sinks
	 */
	private void disconnectSinks()
	{
		if (m_SinksConnected)
		{
			DispatchHelper helper = new DispatchHelper();
			try
			{
				helper.revokeDocumentationModifiedSink(this);
//				helper.revokeDrawingAreaSink(this);
//				helper.revokeDrawingAreaSelectionSink(this);
//				helper.revokeDrawingAreaCompartmentSink(this);
				helper.revokeInitSink(this);
				helper.revokeArtifactSink(this);
				helper.revokeLifeTimeSink(this);
				helper.revokeProjectTreeSink(this);
			}
			catch (InvalidArguments e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			m_SinksConnected = false;
		}
	}

	/** 
	 * Called when one or more elements on a diagram are selected
	 * 
	 * @param pParentDiagram[in] the diagram
	 * @param selectedItems[in] a list of one or more selected items.
	 *                          This object only cares about single element selections
	 * @param cell[in] result cell
	 * 
	 * @return HRESULT
	 */
	public void onSelect(IDiagram pParentDiagram, ETList<IPresentationElement> selectedItems,/* ICompartment pComp,*/ IResultCell cell)
	{
		if (selectedItems != null)
		{
			int count = selectedItems.size();
			
			// We only are interested in selection events where one element is selected.
			if (count == 1)
			{
				IPresentationElement pEle = selectedItems.get(0);
				IElement pSubject = pEle.getFirstSubject();
				if (pSubject != null)
				{
					onNavigateToElement(pSubject);
				}
			}
		}
	}

	/** 
	 * called when the user navigates to an Element
	 * 
	 * @param pElement[in] the element navigated to
	 * 
	 * @return HRESULT
	 */
	private void onNavigateToElement( IElement pElement )
	{
		if (pElement != null && m_Navigator != null)
		{
			m_Navigator.navigateToElement(pElement);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaSelectionEventsSink#onUnselect(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement[], org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onUnselect(IDiagram pParentDiagram, IPresentationElement[] unselectedItems, IResultCell cell)
	{
		//nothing to do
	}

	/** 
	 * Called when a compartment is selected.
	 * 
	 * @param pItem[in] the selected compartment
	 * @param bSelected[in] whether or not it's selected
	 * @param cell[in] the result cell
	 * 
	 * @return HRESULT
	 */
//	public void onCompartmentSelected(ICompartment pCompartment, boolean bSelected, IResultCell cell)
//	{
//		if (bSelected)
//		{
//			if (pCompartment != null)
//			{
//				IElement pCompartmentEle = pCompartment.getModelElement();
//				if (pCompartmentEle != null)
//				{
//					onNavigateToElement(pCompartmentEle);
//				}
//			}
//		}
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.ICompartmentEventsSink#onCompartmentCollapsed(org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
//	public void onCompartmentCollapsed(ICompartment pCompartment, boolean bCollapsed, IResultCell cell)
//	{
//		//nothing to do
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductPreInit(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onCoreProductPreInit(ICoreProduct pVal, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductInitialized(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onCoreProductInitialized(ICoreProduct newVal, IResultCell cell)
	{
		//nothing to do
	}

	/** 
	 * Called when the product is terminating.  This is our last chance to disconnect
	 * the event sinks.
	 * 
	 * @param pVal[in] the CoreProduct that is terminating.
	 * @param cell[in] result cell
	 * 
	 * @return HRESULT
	 */
	public void onCoreProductPreQuit(ICoreProduct pVal, IResultCell cell)
	{
		disconnectSinks();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductPreSaved(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onCoreProductPreSaved(ICoreProduct pVal, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductSaved(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onCoreProductSaved(ICoreProduct newVal, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPreCreated(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
//	public void onDrawingAreaPreCreated(IDrawingAreaControl pDiagramControl, IResultCell cell)
//	{
//		//nothing to do
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPostCreated(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
//	public void onDrawingAreaPostCreated(IDrawingAreaControl pDiagramControl, IResultCell cell)
//	{
//		//nothing to do
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaOpened(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaOpened(IDiagram pParentDiagram, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaClosed(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaClosed(IDiagram pParentDiagram, boolean bDiagramIsDirty, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPreSave(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaPreSave(IProxyDiagram pParentDiagram, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPostSave(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaPostSave(IProxyDiagram pParentDiagram, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaKeyDown(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, int, boolean, boolean, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaKeyDown(IDiagram pParentDiagram, int nKeyCode, boolean bControlIsDown, boolean bShiftIsDown, boolean bAltIsDown, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPrePropertyChange(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaPrePropertyChange(IProxyDiagram pProxyDiagram, int nPropertyKindChanged, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPostPropertyChange(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaPostPropertyChange(IProxyDiagram pProxyDiagram, int nPropertyKindChanged, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaTooltipPreDisplay(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement, org.netbeans.modules.uml.ui.support.viewfactorysupport.IToolTipData, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
//	public void onDrawingAreaTooltipPreDisplay(IDiagram pParentDiagram, IPresentationElement pPE, IToolTipData pTooltip, IResultCell cell)
//	{
//		//nothing to do
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaActivated(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaActivated(IDiagram pParentDiagram, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPreDrop(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaDropContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
//	public void onDrawingAreaPreDrop(IDiagram pParentDiagram, IDrawingAreaDropContext pContext, IResultCell cell)
//	{
//		//nothing to do
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPostDrop(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaDropContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
//	public void onDrawingAreaPostDrop(IDiagram pParentDiagram, IDrawingAreaDropContext pContext, IResultCell cell)
//	{
//		//nothing to do
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPreFileRemoved(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaPreFileRemoved(String sFilename, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaFileRemoved(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDrawingAreaFileRemoved(String sFilename, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IDocumentationModifiedEventsSink#onDocumentationPreModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDocumentationPreModified(IElement element, String doc, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IDocumentationModifiedEventsSink#onDocumentationModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDocumentationModified(IElement element, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IArtifactEventsSink#onPreFileNameModified(org.netbeans.modules.uml.core.metamodel.structure.IArtifact, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreFileNameModified(IArtifact pArtifact, String newFileName, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IArtifactEventsSink#onFileNameModified(org.netbeans.modules.uml.core.metamodel.structure.IArtifact, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onFileNameModified(IArtifact pArtifact, String oldFileName, IResultCell cell)
	{
		if (oldFileName != null && oldFileName.length() > 0)
		{
			if (m_Navigator != null)
			{
				m_Navigator.navigateToElement(pArtifact);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IArtifactEventsSink#onPreDirty(org.netbeans.modules.uml.core.metamodel.structure.IArtifact, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreDirty(IArtifact pArtifact, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IArtifactEventsSink#onDirty(org.netbeans.modules.uml.core.metamodel.structure.IArtifact, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDirty(IArtifact pArtifact, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IArtifactEventsSink#onPreSave(org.netbeans.modules.uml.core.metamodel.structure.IArtifact, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreSave(IArtifact pArtifact, String fileName, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IArtifactEventsSink#onSave(org.netbeans.modules.uml.core.metamodel.structure.IArtifact, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onSave(IArtifact pArtifact, String fileName, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementPreCreate(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementPreCreate(String ElementType, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementCreated(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementCreated(IVersionableElement element, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementPreDelete(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementPreDelete(IVersionableElement element, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementDeleted(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementDeleted(IVersionableElement element, IResultCell cell)
	{
		if (element != null)
		{
			ISourceFileArtifact pArtifact = null;
			if (element instanceof ISourceFileArtifact)
			{
				pArtifact = (ISourceFileArtifact)element;
			}
			
			if (pArtifact == null)
			{
				if (element instanceof IClassifier)
				{
					pArtifact = getSourceFileArtifact((IClassifier)element);
				}
			}
			
			if (pArtifact != null)
			{
				if (m_Navigator != null && m_Navigator instanceof ISourceNavigator)
				{
					((ISourceNavigator)m_Navigator).closeArtifact(pArtifact);
				}
			}
		}
	}

	/**
	 * @param classifier
	 * @return
	 */
	private ISourceFileArtifact getSourceFileArtifact(IClassifier classifier)
	{
		ISourceFileArtifact pArtifact = null;
		if (classifier != null)
		{
			ETList<IElement> pElements = classifier.getSourceFiles();
			if (pElements != null)
			{
				int count = pElements.size();
				if (count > 0)
				{
					IElement pEle = pElements.get(0);
					if (pEle instanceof ISourceFileArtifact)
					{
						pArtifact = (ISourceFileArtifact)pEle;
					}
				}
			}
		}
		return pArtifact;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementPreDuplicated(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementPreDuplicated(IVersionableElement element, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementDuplicated(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onElementDuplicated(IVersionableElement element, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onItemExpanding(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeExpandingContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onItemExpanding(IProjectTreeControl pParentControl, IProjectTreeExpandingContext pContext, IResultCell cell)
	{
		//nothing to do
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
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onAfterEdit(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEditVerify, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onAfterEdit(IProjectTreeControl pParentControl, IProjectTreeItem pItem, IProjectTreeEditVerify pVerify, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onDoubleClick(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, boolean, boolean, boolean, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDoubleClick(IProjectTreeControl pParentControl, IProjectTreeItem pItem, boolean isControl, boolean isShift, boolean isAlt, boolean isMeta, IResultCell cell)
	{
		if (pItem != null)
		{
			IElement pElement = pItem.getModelElement();
			
			// In response to a double click, we only navigate to a source
			// file if the element that was double clicked on was a Source
			// File Artifact.
			//
			// If we remove this restriction (i.e. remove the following QI
			// test), then all elements that have source file artifacts will
			// have their source code navigated to when they are double
			// clicked on.  When this restriction is removed, there is
			// something to look out for; the navigation controller will get
			// a double click event followed by a selection event.  This
			// will cause the source code to be navigated to twice.  This
			// double navigation may cause problems (e.g. the infamous
			// CodeWright focus stealing problem) and needs to be fully
			// tested.
			if (pElement instanceof ISourceFileArtifact ||
				pElement instanceof IOperation ||
				pElement instanceof IAttribute )
			{
				onNavigateToElement(pElement);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onSelChanged(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem[], org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onSelChanged(IProjectTreeControl pParentControl, IProjectTreeItem[] pItem, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onRightButtonDown(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeHandled, int, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onRightButtonDown(IProjectTreeControl pParentControl, IProjectTreeItem pItem, IProjectTreeHandled pHandled, int nScreenLocX, int nScreenLocY, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onBeginDrag(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem[], org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onBeginDrag(IProjectTreeControl pParentControl, IProjectTreeItem[] pItem, IProjectTreeDragVerify pVerify, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onMoveDrag(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, java.awt.datatransfer.Transferable, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onMoveDrag(IProjectTreeControl pParentControl, Transferable pItem, IProjectTreeDragVerify pVerify, IResultCell cell)
	{
		//nothing to do
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onEndDrag(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, java.awt.datatransfer.Transferable, int, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onEndDrag(IProjectTreeControl pParentControl, Transferable pItem, int action, IProjectTreeDragVerify pVerify, IResultCell cell)
	{
		//nothing to do
	}

}

