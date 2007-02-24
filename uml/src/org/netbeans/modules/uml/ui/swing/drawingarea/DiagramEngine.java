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



package org.netbeans.modules.uml.ui.swing.drawingarea;


//import org.apache.xml.utils.IntStack;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IPartFacade;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.ICoreRelationshipDiscovery;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDelayedAction;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.structure.IAssociationClass;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuSorter;
import org.netbeans.modules.uml.ui.support.helpers.GUIBlocker;
import org.netbeans.modules.uml.ui.support.helpers.IGUIBlocker;
import org.netbeans.modules.uml.ui.support.helpers.IGUIBlocker.GBK;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.trackbar.JTrackBar;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSEEdgeLabel;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.TSENodeLabel;
import com.tomsawyer.editor.export.TSEPrintSetup;
import com.tomsawyer.graph.TSGraphObject;
//import com.tomsawyer.util.TSDList;
import org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Provides the base implementation of the IDiagramEngine.  Most of the methods
 * stubbed out.
 * 
 * @author Trey Spiva
 */
public abstract class DiagramEngine implements IDiagramEngine
{
   private IDrawingAreaControl m_DrawingArea;

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#getDrawingArea()
    */
   public IDrawingAreaControl getDrawingArea()
   {
      return m_DrawingArea;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#attach(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl)
    */
   public void attach(IDrawingAreaControl pParentControl)
   {
      m_DrawingArea = pParentControl; 
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#detach()
    */
   public void detach()
   {
      m_DrawingArea = null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#getNamespaceForCreatedElements()
    */
   public INamespace getNamespaceForCreatedElements()
   {
      INamespace retVal = null;
      
      IDrawingAreaControl control = getDrawingArea();
      if(control != null)
      {
         retVal = control.getNamespace();
      }
      
      return retVal;
   }

   public void setQuickKeys( TSEGraphWindow pGraphEditor )
   {
   }
      
   public IDiagram getDiagram()
   {
      IDiagram retVal = null;
      
      if(getDrawingArea() != null)
      {
         retVal = getDrawingArea().getDiagram();
      }
      
      return retVal;   
   }
   
   /**
	* Retrieve the owning element of this diagram engine
	*/
   public IElement getOwner()
   {
   		IDiagram dia = getDiagram();
   		IElement retEle = null;
   		if (dia != null)
   		{
   			retEle = dia.getOwner();
   		}
   		return retEle;
   }
   
   //**************************************************
   // Adapter Methods
   //**************************************************
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#writeToArchive(org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive, org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement)
    */
   public void writeToArchive(IProductArchive pProductArchive, IProductArchiveElement pParentElement)
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#readFromArchive(org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive, org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement)
    */
   public void readFromArchive(IProductArchive pProductArchive, IProductArchiveElement pParentElement)
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#registerAccelerators()
    */
   public void registerAccelerators()
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#setupLayoutSettings(boolean)
    */
   public void setupLayoutSettings(boolean bNewDiagram)
   {
      // The default diagram layout is set in ADDrawingAreaControl.initializeNewDiagram()
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#revokeAccelerators()
    */
   public void revokeAccelerators()
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#onAccelerator(int, int, int, boolean, boolean, int)
    */
   public boolean onAccelerator(int nMsg, int wParam, int lParam, boolean bActive, boolean bWeHaveFocus, int nKeyCode)
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#getContextMenuSorter()
    */
   public IProductContextMenuSorter getContextMenuSorter()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#onContextMenu(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, int, int)
    */
   public void onContextMenu(IProductContextMenu pContextMenu, int logicalX, int logicalY)
   {
      // TODO Auto-generated method stub
      
   }

   public void onContextMenu(IMenuManager manager)
   {
	  // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#onContextMenuHandleSelection(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem)
    */
   public void onContextMenuHandleSelection(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem)
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#setSensitivityAndCheck(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem, int)
    */
   public void setSensitivityAndCheck(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem, int buttonKind)
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#getRelationshipDiscovery()
    */
   public ICoreRelationshipDiscovery getRelationshipDiscovery()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#enterModeFromButton(java.lang.String)
    */
   public boolean enterModeFromButton(String sButtonID)
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#initializeNewDiagram()
    */
   public void initializeNewDiagram()
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#initializeTrackBar()
    */
   public JTrackBar initializeTrackBar()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#preDoLayout(int)
    */
   public boolean preDoLayout(int nLayoutStyle)
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#postDoLayout()
    */
   public void postDoLayout()
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#preCopy()
    */
   public boolean preCopy()
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#postCopy()
    */
   public void postCopy()
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#preDeepSyncBroadcast(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement[])
    */
   public boolean preDeepSyncBroadcast(ETList<IElement> pDeepSyncElements)
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#postDeepSyncBroadcast(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement[])
    */
   public void postDeepSyncBroadcast(ETList<IElement> pDeepSyncElements)
   {
     IDrawingAreaControl ctrl = getDrawingArea();
      if ((ctrl != null) && (pDeepSyncElements != null))
      {
         // Now perform relationship discover amongst the IElements passed in and the
         // elements on the diagram already.

         // Get the model elements currently on the diagram so we can do
         // relationship discovery with those ME's later
         ETList < IElement > pMEsOnTheDiagram = ctrl.getAllItems3();

         // This will discover relationships among the pME's.  It will NOT
         // discover relationships among an item in the pME and and item
         // already in the diagram.
         IDiagramEngine pDiagramEngine = ctrl.getDiagramEngine();
         if (pDiagramEngine != null)
         {
            ICoreRelationshipDiscovery pRelationshipDiscovery = pDiagramEngine.getRelationshipDiscovery();
            if (pRelationshipDiscovery != null)
            {
               pRelationshipDiscovery.discoverCommonRelations(true, 
                                                              pDeepSyncElements, 
                                                              pMEsOnTheDiagram);
            }
         }
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#prePumpMessages()
    */
   public void prePumpMessages()
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#postPumpMessages()
    */
   public void postPumpMessages()
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#postAddObject(com.tomsawyer.graph.TSGraphObject)
    */
   public void postAddObject(TSGraphObject pGraphObject)
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#postAddObjectHandleContainment(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
    */
   public void postAddObjectHandleContainment(IPresentationElement pPE)
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#handleDelayedAction(org.netbeans.modules.uml.core.metamodel.diagrams.IDelayedAction)
    */
	public abstract boolean handleDelayedAction(IDelayedAction action);


   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#convertDiagramsToElements(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement[], org.netbeans.modules.uml.core.support.umlsupport.IStrings)
    */
   public void convertDiagramsToElements(IElement[] pMEs, IStrings pDiagramLocations)
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#postOnDrop(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement[], boolean)
    */
   public void postOnDrop(ETList<IElement> pMEs, boolean bAutoRouteEdges)
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#preCreatePresentationElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
    */
   public boolean preCreatePresentationElement(IElement pElement)
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#enterMode(int)
    */
   public void enterMode(int nDrawingToolKind)
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#enterMode2(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
    */
   public void enterMode2(String sMode, String sFullInitString, String sTSViewString, String sGraphObjectObjectInitString)
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#preHandleDeleteKey()
    */
   public boolean preHandleDeleteKey()
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#onPreMoveObjects(com.tomsawyer.util.TSDList, com.tomsawyer.util.TSDList, com.tomsawyer.util.TSDList, int, int)
    */
   public void onPreMoveObjects(ETList < IETGraphObject > affectedObjects, int dx, int dy)
   {
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#delayedPostMoveObjects(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement[], int, int)
    */
   public void delayedPostMoveObjects(ETList < IPresentationElement > pPEs, int nDeltaX, int nDeltaY)
   {
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#onPreResizeObjects(com.tomsawyer.graph.TSGraphObject)
    */
   public boolean onPreResizeObjects(TSGraphObject graphObject)
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#onPostResizeObjects(com.tomsawyer.graph.TSGraphObject)
    */
   public boolean onPostResizeObjects(TSGraphObject graphObject)
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#onPreScrollZoom(double, double, double)
    */
   public boolean onPreScrollZoom(double pageCenterX, double pageCenterY, double zoomLevel)
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#onPostScrollZoom()
    */
   public boolean onPostScrollZoom()
   {
      // TODO Auto-generated method stub
      return false;
   }

	/**
	 * Ask the user what to do about a name collision
	 *
	 * @param pCompartmentBeingEdited [in] The compartment being edited
	 * @param pElement [in] The element being renamed
	 * @param sProposedName [in] The new name
	 * @param pCollidingElements [in] A list of elements this name collides with
	 * @param bContinue [in] VARIANT_TRUE to continue the edit
	 */
   public boolean questionUserAboutNameCollision(ICompartment pCompartmentBeingEdited, INamedElement pElement, String sProposedName, ETList<INamedElement> pCollidingElements)
   {
		boolean bContinue = true;

		// Get the first colliding element
		INamedElement pFirstCollidingElement = null;
		if (pCollidingElements != null)
		{
			int count = pCollidingElements.getCount();
			if (count > 0)
			{
				pFirstCollidingElement = pCollidingElements.get(0);
			}
		}

		if (pFirstCollidingElement != null && pCompartmentBeingEdited != null && pElement != null && bContinue)
		{
			// Fire the pre so that our derived engines can cancel if they want
			boolean bQuestionUser = preHandleNameCollision(pCompartmentBeingEdited,
												pElement,
												pFirstCollidingElement);
			if (bQuestionUser)
			{
				DialogDisplayer.getDefault().notify(
						new NotifyDescriptor.Message(NbBundle.getMessage(
								DiagramEngine.class, "IDS_NAMESPACECOLLISION")));
				bContinue = false;
				
//				IQuestionDialog pDiag = new SwingQuestionDialogImpl();
//				if ( pDiag != null )
//				{
//					String BUNDLE_NAME = "org.netbeans.modules.uml.ui.swing.drawingarea.Bundle";
//					ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
//					String title = RESOURCE_BUNDLE.getString("IDS_NAMESPACECOLLISION_TITLE");
//					String msg = RESOURCE_BUNDLE.getString("IDS_NAMESPACECOLLISION");
//					QuestionResponse result = pDiag.displaySimpleQuestionDialog(SimpleQuestionDialogKind.SQDK_YESNOCANCEL, MessageIconKindEnum.EDIK_ICONWARNING, msg, 0, null, title);
//					if (result.getResult() == SimpleQuestionDialogResultKind.SQDRK_RESULT_YES)
//					{
//						// User wants to allow the name collision.
//						// User wants to reconnect the presentation element
//						handlePresentationElementReattach(pCompartmentBeingEdited,
//                                                        pElement,
//                                                        pFirstCollidingElement);
//                                                
//						// Cancel the rename
////						bContinue = false;
//					}
//					else if (result.getResult() == SimpleQuestionDialogResultKind.SQDRK_RESULT_NO)
//					{
//						// User wants to allow the name collision
//					}
//					else
//					{
//						// User cancelled it
//						bContinue = false;
//					}
//				}
			}
		}
		return bContinue;
   }

	/**
	 * Fired before the user is questioned about name collisions.  bQuestionUser to FALSE to not ask the user.
	 *
	 * @param pCompartmentBeingEdited [in] The compartment being edited
	 * @param pElement [in] The element being renamed
	 * @param pFirstCollidingElement [in] The first element this name collides with
	 * @param bQuestionUser [in] VARIANT_TRUE to question the user, otherwise accept the name collision
	 */
   public boolean preHandleNameCollision(ICompartment pCompartmentBeingEdited, INamedElement pElement, INamedElement pFirstCollidingElement)
   {
      boolean bQuestionUser = true;
		// Right now we handle only classifiers that are not partfacades or association classes
		if (pFirstCollidingElement != null)
		{
			IDrawEngine pDrawEngine = pCompartmentBeingEdited.getEngine();
			if (pDrawEngine != null)
			{
				bQuestionUser = pDrawEngine.preHandleNameCollision(pCompartmentBeingEdited, 
																	 pElement,
																	 pFirstCollidingElement);
			}
//			if (pFirstCollidingElement instanceof IClassifier)
//			{
//				if (pFirstCollidingElement instanceof IPartFacade)
//				{
//				}
//				else if (pFirstCollidingElement instanceof IAssociationClass)
//				{
//				}
//				else
//				{
//					// Make sure the drawengine will allow this reconnection
//					IDrawEngine pDrawEngine = pCompartmentBeingEdited.getEngine();
//					if (pDrawEngine != null)
//					{
//						bQuestionUser = pDrawEngine.preHandleNameCollision(pCompartmentBeingEdited, 
//																			 pElement,
//																			 pFirstCollidingElement);
//					}
//				}
//			}
//			else
//			{
//				bQuestionUser = false;
//			}
   	}
      return bQuestionUser;
   }

	/**
	 * Reattaches the presentation element to the new model element.
	 *
	 * @param pCompartmentBeingEdited [in] The compartment being edited
	 * @param pElement [in] The element being renamed
	 * @param pFirstCollidingElement [in] The first element this name collides with
	 */
   public void handlePresentationElementReattach(ICompartment pCompartmentBeingEdited, INamedElement pElement, INamedElement pFirstCollidingElement)
   {
		// User wants to reconnect the presentation element
		IPresentationElement pPresentationElement = null;

		pPresentationElement = TypeConversions.getPresentationElement(pCompartmentBeingEdited);

		// See what type of presentation element we've got
		if (pPresentationElement instanceof INodePresentation)
		{
			// Reconnect the presentation element
			reconnectPresentationElement(pPresentationElement, pFirstCollidingElement);

                        //Jyothi: Fix for Bug#6304177 - Nameing class element using an existing class name on the diagram leaves name as Unnamed
                        pPresentationElement.removeSubject(pElement);
                        IGraphPresentation igp = (IGraphPresentation)pPresentationElement;
                        igp.setModelElement(pFirstCollidingElement);
                        if (!pElement.isDeleted()) {
                            pElement.delete();
                        }
                        //Jyothi - end
                    
			IDrawingAreaControl pDrawingAreaControl = getDrawingArea();
			if( pDrawingAreaControl != null )
			{
				// Then validate the diagram so that if this object is connected to any others
				// the invalid links will go away
				pDrawingAreaControl.validateDiagram(false, null);
			}
		}
		else if (pPresentationElement instanceof ILabelPresentation)
		{
			// If we have a label we need to backup to the node and reparent that, then
			// we need to whack all the labels and reset them
			// Zero out the reparenting of this label and reset with the owning
			// node of the label.
			ILabelPresentation pLabelPresentation = (ILabelPresentation)pPresentationElement; 
			pPresentationElement = null;
			IPresentationElement pParentPE = pLabelPresentation.getPresentationOwner();
			if (pParentPE instanceof INodePresentation)
			{
				// We need to reparent this node
				pPresentationElement = pParentPE;
			}
			// Reconnect the presentation element
			reconnectPresentationElement(pPresentationElement, pFirstCollidingElement);

			IDrawingAreaControl pDrawingAreaControl = getDrawingArea();
			if( pDrawingAreaControl != null )
			{
				// Then validate the diagram so that if this object is connected to any others
				// the invalid links will go away
				pDrawingAreaControl.validateDiagram(false, null);
			}
		}
		else if (pPresentationElement instanceof IEdgePresentation)
		{
			// Don't handle the renaming of edges right now
		}
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#prePrint(com.tomsawyer.editor.export.TSEPrintSetup)
    */
   public void prePrint(TSEPrintSetup pPrintHelper)
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#postPrint(com.tomsawyer.editor.export.TSEPrintSetup)
    */
   public void postPrint(TSEPrintSetup pPrintHelper)
   {
      // TODO Auto-generated method stub
      
   }
   
   /**
    * Test if the user wants to delete the data behind the presenation.  This 
    * implementation does not do anything.
    * 
    * @return <code>true</code> if the user wants to delete the data associted 
    *         with the presentation elements.
    */
   public DataVerificationResults verifyDataDeletion(ETList < TSENode > selectedNodes,
                                                     ETList < TSEEdge > selectedEdges, 
                                                     ETList < TSENodeLabel > selectedNodeLabels, 
                                                     ETList < TSEEdgeLabel > selectedEdgeLabels)
   {     
      return null;                                
   }
   
	/**
	 * Reconnects a presentation element to a new model element
	 *
	 * @param pPE [in] The presentation element to reparent
	 * @param pNewModelElement [in] The new model element it should be attached to
	 */
	private void reconnectPresentationElement(IPresentationElement pPE, IElement pNewModelElement)
	{
		if (pPE != null && pNewModelElement != null)
		{
			if (pPE instanceof IProductGraphPresentation)
			{
				IProductGraphPresentation pGraphPresentation = (IProductGraphPresentation)pPE;
				// Reconnect the presentation element
				pGraphPresentation.reconnectPresentationElement(pNewModelElement);
			}
		}
	}
   
   
   static public boolean isContainmentOK()
   {
      if( null == m_blocker )
      {
         m_blocker = new GUIBlocker();
      }
      
      return (!m_blocker.getKindIsBlocked( GBK.DIAGRAM_CONTAINMENT ));
   }
   
   private static IGUIBlocker m_blocker = null;
}
