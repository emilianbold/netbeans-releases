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



package org.netbeans.modules.uml.ui.controls.drawingarea;

import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import java.util.Iterator;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr;
import org.netbeans.modules.uml.ui.support.applicationmanager.PresentationTypeDetails;
import org.netbeans.modules.uml.ui.support.applicationmanager.TSGraphObjectKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.MetaModelHelper;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.DiagramEnums;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IRelationFactory;
import org.netbeans.modules.uml.core.metamodel.infrastructure.RelationFactory;
import com.tomsawyer.editor.TSEGraph;
import com.tomsawyer.editor.TSEObjectUI;
import com.tomsawyer.graph.TSEdge;
import com.tomsawyer.graph.TSNode;

/**
 * @author KevinM
 */
public class RelationshipDiscovery implements IRelationshipDiscovery {

	// Data
	protected IDrawingAreaControl m_DrawingArea = null;
	protected IPresentationTypesMgr m_PresentationTypesMgr = null;

	/**
	 *
	 */
	public RelationshipDiscovery() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IRelationshipDiscovery#createPresentationElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
	 */
	public IPresentationElement createPresentationElement(IElement pElement) {
		return localCreatePresentationElement(pElement, null, PEKind.PEK_ANY);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IRelationshipDiscovery#createNodePresentationElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, org.netbeans.modules.uml.core.support.umlsupport.IETPoint)
	 */
	public IPresentationElement createNodePresentationElement(IElement pElement, IETPoint pLocation) {
		return localCreatePresentationElement(pElement, pLocation, PEKind.PEK_NODE);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IRelationshipDiscovery#createLinkPresentationElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
	 */
   public IPresentationElement createLinkPresentationElement(IElement pElement, IPresentationElement pFromPresentationElement, IPresentationElement pToPresentationElement)
   {
      if (m_DrawingArea == null)
         return null;
      IPresentationElement pPresentationElement = null;

      try
      {
         int nDiagramKind = m_DrawingArea.getDiagramKind(); /* DiagramKind */
         String elementType = pElement.getElementType();
         String initString = m_PresentationTypesMgr.getMetaTypeInitString(elementType, nDiagramKind);

         if (initString != null && initString.length() > 0)
         {
            IETNode pTSDStartNode = TypeConversions.getETNode(pFromPresentationElement);
            IETNode pTSDEndNode = TypeConversions.getETNode(pToPresentationElement);
            if (pTSDStartNode != null && pTSDEndNode != null)
            {
               // Make sure this link isn't already on the diagram
               ETList < IPresentationElement > pEdgePEs;
               long numEdgesOnDiagram = 0;

               // See if they both have presentation elements on this diagram
               pEdgePEs = m_DrawingArea.getAllItems2(pElement);
               numEdgesOnDiagram = pEdgePEs != null ? pEdgePEs.size() : 0;
               if (numEdgesOnDiagram == 0)
               {
                  // Set the model element so the edge attaches and isn't creating a new model element
                  m_DrawingArea.setModelElement(pElement);

                  TSEdge createdEdge = m_DrawingArea.addEdge( initString, (TSNode)pTSDStartNode, (TSNode)pTSDEndNode, false, false );
                  pPresentationElement = TypeConversions.getPresentationElement( createdEdge );
               }
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      // No matter what happens, make sure the drawing area is initialized back to creating model elements
      m_DrawingArea.setModelElement(null);
      return pPresentationElement;
   }

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IRelationshipDiscovery#createLinkPresentationElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
	 */
   public IPresentationElement createLinkPresentationElement(IElement pElement, IElement pFromElement, IElement pToElement)
   {
      IPresentationElement pPresentationElement = null;
      try
      {
         TSEGraph pGraph = m_DrawingArea.getCurrentGraph();

         String elementType = pElement.getElementType();
         if (elementType != null && elementType.length() > 0 && pGraph != null && pFromElement != null && pToElement != null && m_PresentationTypesMgr != null)
         {
            // Look up this element in the presentation types manager
            int nDiagramKind = m_DrawingArea.getDiagramKind(); /* DiagramKind */
            String initString = m_PresentationTypesMgr.getMetaTypeInitString(elementType, nDiagramKind);

            if (initString != null && initString.length() > 0)
            {
               PresentationTypeDetails graphKind = m_PresentationTypesMgr.getInitStringDetails(initString, nDiagramKind); /* TSGraphObjectKind */

               // Now that we have the init string create a node
               if (graphKind.getObjectKind() == TSGraphObjectKind.TSGOK_NODE)
               {
                  //	CString message;
                  //	message.Format(_T("CAxDrawingAreaControl::CreateLinkPresentationElement found a non-link - %s"), W2T(initString));

                  //	UMLMessagingHelper messageService(_Module.getModuleInstance(), IDS_MESSAGINGFACILITY);
                  //	?? =messageService.SendMessage(MT_INFO , xstring(message) ) );
               }
               else if (graphKind.getObjectKind() == TSGraphObjectKind.TSGOK_EDGE)
               {
                  // See if they both have presentation elements on this diagram
                  ETList < IPresentationElement > pStartPEs = m_DrawingArea.getAllNodeItems(pFromElement);
                  ETList < IPresentationElement > pEndPEs = m_DrawingArea.getAllNodeItems(pToElement);

                  long numStartPEs = pStartPEs != null ? pStartPEs.size() : 0;
                  long numEndPEs = pEndPEs != null ? pEndPEs.size() : 0;

                  // We have presentation elements on both the general and specific
                  if (numStartPEs > 0 && numEndPEs > 0)
                  {
                     // We only handle the easy case for now where they each have only one
                     // PE on this diagram.
                     if (numStartPEs == 1 && numEndPEs == 1)
                     {
                        IPresentationElement pFromPresentationElement = (IPresentationElement)pStartPEs.iterator().next();
                        IPresentationElement pToPresentationElement = (IPresentationElement)pEndPEs.iterator().next();
                        pPresentationElement = createLinkPresentationElement(pElement, pFromPresentationElement, pToPresentationElement);
                     }
                  }
               }
               else
               {
                  //CString message;
                  //message.Format(_T("CAxDrawingAreaControl::CreateLinkPresentationElement no initialization string - %s"), 
                  //	elementType.length()?W2T(elementType):_T(""));

                  //UMLMessagingHelper messageService(_Module.getModuleInstance(), IDS_MESSAGINGFACILITY);
                  //?? =messageService.SendMessage(MT_INFO , xstring(message) ) );
               }
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return pPresentationElement;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IRelationshipDiscovery#discoverCommonRelations(boolean)
	 */
	public ETList < IPresentationElement > discoverCommonRelations(boolean bAutoRouteEdges) {
		ETList < IElement > pFoundModelElements = m_DrawingArea != null ? m_DrawingArea.getAllItems3() : null;
		return pFoundModelElements != null ? discoverCommonRelations(bAutoRouteEdges, pFoundModelElements) : null;
	}

	public ETList < IPresentationElement > discoverCommonRelations(boolean bAutoRouteEdges, ETList < IElement > pDiscoverOnTheseElements) {
		ETList < IPresentationElement > pCreatedPresentationElements = new ETArrayList < IPresentationElement > ();
		try {
			long numElements = pDiscoverOnTheseElements != null ? pDiscoverOnTheseElements.size() : 0;

			if (numElements > 0) {
				IRelationFactory factory = new RelationFactory();
				if (factory != null) {
					ETList < IRelationProxy > proxies = factory.determineCommonRelations(pDiscoverOnTheseElements);

					long numProxies = proxies != null ? proxies.size() : 0;

					if (numProxies > 0) {
						Iterator < IRelationProxy > proxyIter = proxies.iterator();
						while (proxyIter.hasNext()) {
							IRelationProxy proxy = proxyIter.next();

							IElement from = proxy.getFrom();
							IElement to = proxy.getTo();

							IElement connection = proxy.getConnection();
							// Need to make this more efficient by using the from and to information
							IPresentationElement pCreatedElement = createLinkPresentationElement(connection, from, to);

							if (pCreatedElement != null) {
								pCreatedPresentationElements.add(pCreatedElement);
							}
						}
					}
				}
			}

			if (bAutoRouteEdges && pCreatedPresentationElements != null) {
				// Autorout the edges
				autoRouteEdges(pCreatedPresentationElements);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return pCreatedPresentationElements != null && pCreatedPresentationElements.size() > 0 ? pCreatedPresentationElements : null;
	}

	/**
	 * Discovers common relationships (generalization, association...) among the presentation elements on the diagram
	 * This routine is used during drag and drop.  Relations are discovered among the elements being dropped and
	 * then between those elements being dropped and elements on the diagram.  Note that elements already on the
	 * diagram don't get relations discovered among them.  Just among them and the elements being dropped.
	 *
	 * @param bAutoRouteEdges [in] Should we autoroute the edges?
	 * @param pNewElementsBeingCreated [in] The elements being created dropped onto the diagram
	 * @param pElementsAlreadyOnTheDiagrams [in] The elements already on the diagram
	 * @param pPresentationElements [out,retval] The created presentation elements
	 */
	public ETList < IPresentationElement > discoverCommonRelations(boolean bAutoRouteEdges, ETList < IElement > pNewElementsBeingCreated, ETList < IElement > pElementsAlreadyOnTheDiagrams) {
		try {
			ETList < IPresentationElement > pCreatedPresentationElements = new ETArrayList < IPresentationElement > ();
			int items = pNewElementsBeingCreated != null ? pNewElementsBeingCreated.size() : 0;
			if (items > 0) {
				IRelationFactory factory = new RelationFactory();
				if (factory != null) {
					ETList < IRelationProxy > proxies = factory.determineCommonRelations3(pNewElementsBeingCreated, pElementsAlreadyOnTheDiagrams);

					if (proxies != null && proxies.size() > 0) {
						Iterator < IRelationProxy > proxyIter = proxies.iterator();
						while (proxyIter.hasNext()) {
							IRelationProxy proxy = proxyIter.next();

							IElement from = proxy.getFrom();
							IElement to = proxy.getTo();

							IElement connection = proxy.getConnection();
							// Need to make this more efficient by using the from and to information
							IPresentationElement pCreatedElement = createLinkPresentationElement(connection, from, to);

							if (pCreatedElement != null) {
								pCreatedPresentationElements.add(pCreatedElement);
							}
						}
					}
				}
			}

         
			if (bAutoRouteEdges) {
				// Autoroute the edges
				autoRouteEdges(pCreatedPresentationElements);
			}

			return pCreatedPresentationElements.size() > 0 ? pCreatedPresentationElements : null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Discovers common relationships (generalization, association...) among the selected elements on the diagram.
	 *
	 * @param pPresentationElements [out,retval] The created presentation elements, if any.
	 */
	public ETList < IPresentationElement > discoverCommonRelationsAmongSelectedElements() {
		try {
			// Get the selected presentation elements and convert to IElements to perform the
			// discovery on.

			ETList < IElement > pSelected = m_DrawingArea != null ? m_DrawingArea.getSelected4() : null;

			// Call relationship discovery
			return discoverCommonRelations(true, pSelected);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/// Autoroute all these edges
	protected void autoRouteEdges(ETList < IPresentationElement > pPresentationElements) {
            
            for(IPresentationElement element : pPresentationElements)
            {
                if(element instanceof IEdgePresentation)
                {
                    IEdgePresentation edgePres = (IEdgePresentation)element;
                    edgePres.autoRoute(true);
                }
            }
	}

	/// Create a presentation element based on the init string
	protected IPresentationElement createPresentationElementUsingInitString(IElement pElement, String initString, IETPoint pLocation, int nLimitToThisKind) /* PEKind*/ {
		if (pElement == null || initString == null)
			return null;
		IPresentationElement pPresentationElement = null;

		try {
			int nDiagramKind = DiagramEnums.DK_DIAGRAM;
			int graphKind = TSGraphObjectKind.TSGOK_INVALID;
			TSEGraph pGraph;

			// Set the model element so the edge attaches and isn't creating a new model element
			m_DrawingArea.setModelElement(pElement);

			if (m_DrawingArea != null) {
				nDiagramKind = m_DrawingArea.getDiagramKind();
				pGraph = m_DrawingArea.getCurrentGraph();
			} else
				pGraph = null;

			if (m_PresentationTypesMgr != null) {
				
				PresentationTypeDetails details = m_PresentationTypesMgr.getInitStringDetails(initString, nDiagramKind);
				if (details != null)
					graphKind = details.getObjectKind();
			}

			if ((nLimitToThisKind == PEKind.PEK_ANY || nLimitToThisKind == PEKind.PEK_NODE) && (graphKind == TSGraphObjectKind.TSGOK_NODE || graphKind == TSGraphObjectKind.TSGOK_NODE_RESIZE)) {
				IETPoint pETLocation = pLocation != null ? pLocation : new ETPoint(0, 0);

				TSNode pCreatedNode = m_DrawingArea.addNode(initString, pETLocation, false, false, pElement);
				pPresentationElement = pCreatedNode != null ? TypeConversions.getPresentationElement(pCreatedNode) : null;
			} else if ((nLimitToThisKind == PEKind.PEK_ANY || nLimitToThisKind == PEKind.PEK_EDGE) && (graphKind == TSGraphObjectKind.TSGOK_EDGE)) {
				IElement pStartNode;
				IElement pEndNode;
				MetaModelHelper pHelper = new MetaModelHelper();
				if (pHelper != null) {
					// Return the ends of the relationship
					MetaModelHelper.RelationEnds ends = pHelper.getRelationshipEnds(pElement);
					pStartNode = ends.getStartElement();
					pEndNode = ends.getEndElement();

					if (pStartNode != null && pEndNode != null) {
						pPresentationElement = createLinkPresentationElement(pElement, pStartNode, pEndNode);
					}
				}
			} else if (graphKind == TSGraphObjectKind.TSGOK_INVALID) {

				//			 CComBSTR elementType;
				//			 CString message;
				//
				//			 _VH(pElement->get_ElementType(&elementType));
				//			 message.Format(_T("RelationshipDiscoveryImpl::CreatePresentationElementUsingInitString no initialization string - %s"), 
				//				elementType.Length()?W2T(elementType):_T(""));
				//
				//			 UMLMessagingHelper messageService(_Module.GetModuleInstance(), IDS_MESSAGINGFACILITY);
				//			 _VH(messageService.SendMessage(MT_INFO , xstring(message) ) );
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// No matter what happens, make sure the drawing area is initialized back to creating model elements
		m_DrawingArea.setModelElement(null);
		return pPresentationElement;

	}
	/// Creates a presentation element based on the pElement argument with the kind nLimitToThisKind
	protected IPresentationElement localCreatePresentationElement(IElement pElement, IETPoint pLocation, int nLimitToThisKind) /* PEKind*/ {
		// pElement can be null, ProcessDiagramElement in the drawing area can change it
		if (m_DrawingArea != null && pElement != null) {
			// Fix W1762:  It is possible to make pElement NULL,
			// e.g. if the element is not allowed on the diagram.
			IElement pChangedElement = m_DrawingArea.processOnDropElement(pElement);

			if (pChangedElement != null) {
				// From the element we need to get the TS init strings and do the create.
				TSEGraph pGraph;

				int nDiagramKind = m_DrawingArea.getDiagramKind();
				pGraph = m_DrawingArea.getCurrentGraph();
				if (pGraph != null) {
					// Look up this element in the presentation types manager and get the init string
					String initString;

					if (m_PresentationTypesMgr != null) {
						// Call this version of the init string getter.  This one grabs the type off
						// the element and also deals with flags - such as on roles (PartFacades) where
						// one role represents an actor and another a class or use case.
						initString = m_PresentationTypesMgr.getMetaTypeInitString(pChangedElement, nDiagramKind);
					} else
						initString = null;

					// If we have an init string then we need to determine if it's a node or an edge
					if (initString != null && initString.length() > 0) {
						return createPresentationElementUsingInitString(pChangedElement, initString, pLocation, nLimitToThisKind);
					}
				}
			}
		}
		return null;
	}

	public void setParentDrawingArea(IDrawingAreaControl pParentDrawingArea) {
		m_DrawingArea = pParentDrawingArea;
		if (m_DrawingArea != null) {
			try {
				m_PresentationTypesMgr = m_DrawingArea.getPresentationTypesMgr();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}

