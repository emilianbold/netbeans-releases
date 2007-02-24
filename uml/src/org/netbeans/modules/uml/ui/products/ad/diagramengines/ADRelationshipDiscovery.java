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

// Workfile. ADRelationshipDiscovery.java
// Revision. 3
//   Author. KevinM
//     Date. Oct 27, 2003 2:57:01 PM												
//  Modtime. 11/2/2003 10:07:22 PM 2:57:01 PM	

package org.netbeans.modules.uml.ui.products.ad.diagramengines;

import java.awt.Point;
import java.util.Iterator;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.common.generics.IteratorT;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IPartFacade;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationReference;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IUsage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.OwnerRetriever;
import org.netbeans.modules.uml.core.metamodel.infrastructure.ICollaboration;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IPort;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IRelationFactory;
import org.netbeans.modules.uml.core.metamodel.infrastructure.RelationFactory;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IImplementation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.metamodel.structure.IAssociationClass;
import org.netbeans.modules.uml.core.metamodel.structure.IComment;
import org.netbeans.modules.uml.core.metamodel.structure.IComponent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.DiscoveryFacility;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETFilteredArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.drawingarea.PEKind;
import org.netbeans.modules.uml.ui.controls.drawingarea.RelationshipDiscovery;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.IConnectMessageKind;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.IConnectorsCompartment;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.ILifelineDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.IMessageEdgeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.drawEngineManagers.IAssociationClassEventManager;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETNode;
import org.netbeans.modules.uml.ui.support.PresentationReferenceHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IEventManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.PointConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.DiagramEnums;
import org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import com.tomsawyer.drawing.TSConnector;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.graph.TSNode;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IMessageKind;
import org.netbeans.modules.uml.core.support.Debug;

/**
 * @author KevinM
 *
 */
public class ADRelationshipDiscovery extends RelationshipDiscovery implements IADRelationshipDiscovery
{
   MapElementToDrawEngine m_MapLifelineEngines = new MapElementToDrawEngine("Lifeline");
   MapElementToDrawEngine m_MapMessageEngines = new MapElementToDrawEngine("Message");
   MapElementToDrawEngine m_MapInteractionEngines = new MapElementToDrawEngine("Interaction");
   
	/**
	 * 
	 */
	public ADRelationshipDiscovery() {
		super();
	}

   public ETList < IPresentationElement > discoverNestedLinks(ETList < IElement > pDiscoverOnTheseElements)
   {
      try
      {
         if (m_DrawingArea == null)
         {
            return null;
         }
         
         ETList < IElement > pFoundModelElements = (pDiscoverOnTheseElements != null)
            ? pDiscoverOnTheseElements
            : m_DrawingArea.getAllItems3();

         if ( (pFoundModelElements != null) &&
              (pFoundModelElements.size() > 0) )
         {
            ETList < INamedElement > pNamedElements = new ETArrayList < INamedElement > ();
            ETList < IPresentationElement > pCreatedPEs = new ETArrayList < IPresentationElement > ();
            
            // Reduce this list to just namedelements
            for (Iterator iter = pFoundModelElements.iterator(); iter.hasNext();)
            {
               IElement element = (IElement)iter.next();
               
               // Add all the named elements, except for a few that
               // should never have nested links
               if ( (element instanceof INamedElement) &&
                     !(element instanceof IPort) )
               {
                  pNamedElements.add( (INamedElement)element );
               }
            }

            Iterator < INamedElement > namedElementIter = pNamedElements.iterator();
            while (namedElementIter.hasNext())
            {
               // Now start looking for children and parents among the list
               INamedElement pNamedElement = namedElementIter.next();
               if (pNamedElement != null)
               {
                  INamespace pNamespace = pNamedElement.getNamespace();
                  if (pNamedElements.contains(pNamespace))
                  {
                     // Create the nested link
                     IPresentationElement pCreatedPE = createNestedLink(pNamedElement, pNamespace);
                     if (pCreatedPE != null)
                     {
                        pCreatedPEs.add(pCreatedPE);
                     }
                  }
               }
            }

            return pCreatedPEs != null && pCreatedPEs.size() > 0 ? pCreatedPEs : null;
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return null;
   }

	/**
	 * Discovers Comments
	 *
	 * @param pDiscoverOnTheseElements [in] The elements that should be used to search for nested link relations.
	 * @param pPresentationElements [out,retval] The created presentation elements
	 */
	public ETList < IPresentationElement > discoverCommentLinks(ETList < IElement > pDiscoverOnTheseElement) {
		try {
			if (m_DrawingArea == null)
				return null;

			ETList < IElement > pFoundModelElements = pDiscoverOnTheseElement != null && pDiscoverOnTheseElement.size() > 0 ? pDiscoverOnTheseElement : m_DrawingArea.getAllItems3();

			IteratorT < IElement > foundIter = new IteratorT < IElement > (pFoundModelElements);

			// Get a list of all the IComments

			ETList < IPresentationElement > pCreatedPEs = new ETArrayList < IPresentationElement > ();
			ETList < IElement > pComments = new ETArrayList < IElement > ();

			// Create a list of just comments
			while (foundIter.hasNext()) {
				IElement pElement = foundIter.next();

				if (pElement instanceof IComment) {
					if (!pComments.find(pElement))
						pComments.add((IComment) pElement);
				}
			}
			IteratorT < IComment > commentIter = new IteratorT < IComment > (pComments);
			foundIter.reset(pFoundModelElements);

			// Now go over the list of elements and see if any of them are annotated by the comments
			while (foundIter.hasNext()) {
				IElement pElement = foundIter.next();
				if (pElement instanceof INamedElement) {
					INamedElement pNamedElement = (INamedElement) pElement;
					// Go over the list of comments and see if any of them annotate this guy
					commentIter.reset(pComments);
					while (commentIter.hasNext()) {
						IComment pComment = commentIter.next();
						if (pComment != null) {
							if (pComment.getIsAnnotatedElement(pNamedElement)) {
								IPresentationElement pCreatedPE = createCommentLink(pComment, pElement);
								if (pCreatedPE != null) {
									pCreatedPEs.add(pCreatedPE);
								}
							}
						}
					}
				}
			}

			return pCreatedPEs != null && pCreatedPEs.size() > 0 ? pCreatedPEs : null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Discovers PartFacade Links on the collaboration diagram
	 *
	 * @param pDiscoverOnTheseElements [in] The elements that should be used to search for partfacade link relations.
	 * @param pPresentationElements [out,retval] The created presentation elements
	 */
	public ETList < IPresentationElement > discoverPartFacadeLinks(ETList < IElement > pDiscoverOnTheseElements) {
		try {
			if (m_DrawingArea == null)
				return null;

			ETList < IElement > pFoundModelElements = pDiscoverOnTheseElements != null && pDiscoverOnTheseElements.size() > 0 ? pDiscoverOnTheseElements : m_DrawingArea.getAllItems3();

			IteratorT < IElement > foundIter = new IteratorT < IElement > (pFoundModelElements);
			ETList < IPresentationElement > pCreatedPEs = new ETArrayList < IPresentationElement > ();
			ETList < IElement > pCollaborations = new ETArrayList < IElement > ();

			// Create a list of just collaborations
			while (foundIter.hasNext()) {
				IElement pElement = foundIter.next();

				if (pElement instanceof ICollaboration) {
					if (!pCollaborations.find(pElement))
						pCollaborations.add((ICollaboration) pElement);
				}
			}

			// Now go over the list of elements and see if any of them are part facades that 
			// have a collaboration on the diagram
			IteratorT < ICollaboration > collaborationIter = new IteratorT < ICollaboration > (pCollaborations);
			if (pFoundModelElements != null)
				foundIter.reset(pFoundModelElements);

			while (foundIter.hasNext()) {
				IElement pElement = foundIter.next();
				if (pElement instanceof IParameterableElement) {
					IParameterableElement pPartFacade = (IParameterableElement) pElement;

					collaborationIter.reset(pCollaborations);
					// Go over the list of collaborations and see if any of them have this guy as a template parameter
					while (collaborationIter.hasNext()) {
						ICollaboration pCollaboration = collaborationIter.next();
						if (pCollaboration.getIsTemplateParameter(pPartFacade)) {
							IPresentationElement pCreatedPE = createPartFacadeLink(pPartFacade, pCollaboration);
							if (pCreatedPE != null)
								pCreatedPEs.add(pCreatedPE);
						}
					}
				}
			}
			return pCreatedPEs.size() > 0 ? pCreatedPEs : null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Discover associationclass links when the user drops onto the diagram
	 *
	 * @param pDiscoverOnTheseElements [in] The elements that should be used to search for associationclass relations.
	 * @param pPresentationElements [out,retval] The created presentation elements
	 */
	public ETList < IPresentationElement > discoverAssociationClassLinks(ETList < IElement > pDiscoverOnTheseElements) {
		try {
			if (m_DrawingArea == null)
				return null;

			ETList < IElement > pFoundModelElements = pDiscoverOnTheseElements != null && pDiscoverOnTheseElements.size() > 0 ? pDiscoverOnTheseElements : m_DrawingArea.getAllItems3();

			IteratorT < IElement > foundIter = new IteratorT < IElement > (pFoundModelElements);

			// Create a list of just collaborations
			while (foundIter.hasNext()) {
				IElement pElement = foundIter.next();
				// Go through and discover on all association classes

				if (pElement instanceof IAssociationClass) {
					IAssociationClass pAssocClass = (IAssociationClass) pElement;

					ETList < IPresentationElement > pAssocClassPEs = m_DrawingArea.getAllItems2(pAssocClass);

					IteratorT < IPresentationElement > iter = new IteratorT < IPresentationElement > (pAssocClassPEs);
					while (iter.hasNext()) {
						// Get the event manager for the presentation element and let it
						// do the actual discovery.
						IPresentationElement pPE = iter.next();
						if (pPE == null)
							continue;

						IDrawEngine pDrawEngine = TypeConversions.getDrawEngine(pPE);
						if (pDrawEngine != null) {
							IAssociationClassEventManager pAssocEventManager =
								pDrawEngine.getEventManager() instanceof IAssociationClassEventManager ? (IAssociationClassEventManager) pDrawEngine.getEventManager() : null;
							if (pAssocEventManager != null) {
								boolean bDiscovered = pAssocEventManager.discoverBridges(pFoundModelElements);
							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null; // this function does nothing.
	}

	/**
	 * Creates port presentation elements on the component presentation element.  This routine creates
	 * the necessary presentation reference relationship.
	 *
	 * @param pComponentPE [in] The component to create the ports on
	 * @param pPresentationElements [out,retval] The created port PEs
	 */
	public ETList < IPresentationElement > createPortPresentationElements(IPresentationElement pComponentPE) {
		if (pComponentPE == null)
			return null;

		try {

			IElement pPEElement = TypeConversions.getElement(pComponentPE);
			IComponent pComponent = pPEElement instanceof IComponent ? (IComponent) pPEElement : null;
			if (pComponent == null)
				return null;

			ETList < IPresentationElement > pCreatedPEs = new ETArrayList < IPresentationElement > ();

			// Create the ports
			IteratorT < IPort > portsIter = new IteratorT < IPort > (pComponent.getExternalInterfaces());
			while (portsIter.hasNext()) {
				IPort pPort = portsIter.next();
				IPresentationElement pPortPE = createPortPresentationElement(pComponentPE, pPort);
				if (pPortPE != null)
					pCreatedPEs.add(pPortPE);
			}

			return pCreatedPEs.size() > 0 ? pCreatedPEs : null;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	//	These routines are used in DiscoverCommonRelations	

	/* Discover nested links when the user drops onto the diagram
	*
	* @param bAutoRouteEdges [in] Should we autoroute the edges
	* @param pNewElementsBeingCreated [in] The elements being created dropped onto the diagram
	* @param pElementsAlreadyOnTheDiagrams [in] The elements already on the diagram
	* @param pPresentationElements [out,retval] The created presentation elements
	*/

	public ETList < IPresentationElement > discoverNestedLinks(boolean bAutoRouteEdges, ETList < IElement > pNewElementsBeingCreated, ETList < IElement > pElementsAlreadyOnTheDiagrams) {
		try {
			// Discover nested links.  This is complicated because we don't want to discover links between 
			// items already on the diagram.  Just between the new elements being dropped and elements already
			// on the diagram.
			if (!isStaticDiagram())
				return null;

			ETList < IPresentationElement > pPresentationElements = new ETArrayList < IPresentationElement > ();
			ETList < IPresentationElement > pCreatedElements = discoverNestedLinks(pNewElementsBeingCreated);

			// Discover nested links among items being created on the diagram
			if (pCreatedElements != null && pPresentationElements != null) {
				pPresentationElements.addAll(pCreatedElements);
			}
			
			if (pCreatedElements != null)
			{
				pCreatedElements.clear();
			}

			// Now see if any of the elements being created are related to elements already on the diagram			
			IteratorT < IElement > iter = new IteratorT < IElement > (pNewElementsBeingCreated);
			ETList < IElement > elements = new ETArrayList < IElement > ();
			// See if parents to the dropped items exist on the diagram
			while (iter.hasNext()) {
				IElement pElement = iter.next();

				INamedElement pNamedElement = pElement instanceof INamedElement ? (INamedElement) pElement : null;
				if (pNamedElement != null) {
					INamespace pNamespace = pNamedElement.getNamespace();

					if (pNamespace != null) {
						if (pElementsAlreadyOnTheDiagrams != null && pElementsAlreadyOnTheDiagrams.find(pNamespace)) {
							elements.clear();
							elements.add(pNamespace);
							elements.add(pElement);

							pCreatedElements = discoverNestedLinks(elements);
							if (pCreatedElements != null && pPresentationElements != null) {
								pPresentationElements.addAll(pCreatedElements);
							}
							
							if (pCreatedElements != null){
								pCreatedElements.clear();
							}					
						}
					}
				}
			}
			
			iter.reset(pElementsAlreadyOnTheDiagrams);
				
			// Now see if children to the dropped items exist on the diagram
			while (iter.hasNext()) {
				IElement pElement = iter.next();

				INamedElement pNamedElement = pElement instanceof INamedElement ? (INamedElement) pElement : null;
				if (pNamedElement != null) {
					INamespace pNamespace = pNamedElement.getNamespace();

					if (pNamespace != null) {
						if (pElementsAlreadyOnTheDiagrams != null && pNewElementsBeingCreated.find(pNamespace)) {
							elements.clear();
							elements.add(pNamespace);
							elements.add(pElement);

							pCreatedElements = discoverNestedLinks(elements);
							if (pCreatedElements != null && pPresentationElements != null) {
								pPresentationElements.addAll(pCreatedElements);
							}
							
							if (pCreatedElements != null){
								pCreatedElements.clear();
							}
						}
					}
				}
			}
			return pPresentationElements != null && pPresentationElements.size() > 0 ? pPresentationElements : null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Creates a port PE on the collaboration PE
	 *
	 * @param pComponentPE [in] The component where we need to create the port PE
	 * @param pPortToCreate [in] The port that needs a presentation element
	 * @param pPresentationElement [out,retval] The presentation element that just got created.
	 */
	public IPresentationElement createPortPresentationElement(IPresentationElement pComponentPE, IPort pPortToCreate) {
		if (pComponentPE == null || pPortToCreate == null)
			return null;

		try {
			// Need to create the presentation element
			IPresentationElement pPresentationElement = createNodePresentationElement((IElement) pPortToCreate, null);
			if (pPresentationElement != null) {
				//  IPresentationReference pReference;
				// Create a relationship between the port PE and the component PE
				PresentationReferenceHelper.createPresentationReference(pComponentPE, pPresentationElement);

			}
			return pPresentationElement;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Creates port provided and required interfaces on the port.
	 *
	 * @param pPortPE  The port to create port provided and required interfaces on
	 * @param pPresentationElements [out,retval] The created interface PEs
	 */
	public ETList < IPresentationElement > createPortProvidedAndRequiredInterfaces(IPresentationElement pPortPE) {
		if (pPortPE == null)
			return null;

		try {
			IElement pElement = TypeConversions.getElement(pPortPE);
			if (pElement instanceof IPort) {
				IPort pPort = (IPort) pElement;
				ETList < IPresentationElement > pCreatedPEs = new ETArrayList < IPresentationElement > ();
				ETList < IInterface > pProvidedInterfaces = pPort.getProvidedInterfaces();
				ETList < IInterface > pRequiredInterfaces = pPort.getRequiredInterfaces();

				if (pProvidedInterfaces != null) {
					Iterator < IInterface > iter = pProvidedInterfaces.iterator();
					// Create the provided interfaces
					while (iter.hasNext()) {
						IInterface pInterface = iter.next();
						if (pInterface != null) {
							ETList < IPresentationElement > pCreatedProvidedPEs = createPortProvidedInterface(pPortPE, pInterface);
							pCreatedPEs.addAll(pCreatedProvidedPEs);
						}
					}
				}

				if (pRequiredInterfaces != null) {
					Iterator < IInterface > iter = pRequiredInterfaces.iterator();
					// Create the required interfaces
					while (iter.hasNext()) {
						IInterface pInterface = iter.next();

						if (pInterface != null) {
							ETList < IPresentationElement > pCreatedRequiredPEs = createPortRequiredInterface(pPortPE, pInterface);
							pCreatedPEs.addAll(pCreatedRequiredPEs);
						}
					}
				}
				return pCreatedPEs != null && pCreatedPEs.size() > 0 ? pCreatedPEs : null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Discovers Messages between lifelines on an SQD.
	 *
	 * @param pDiscoverOnTheseElements [in] The elements that should be used to search for messages.
	 * @param pPresentationElements [out,retval] The created presentation elements
	 */
	public ETList < IPresentationElement > discoverMessages(ETList < IElement > pDiscoverOnTheseElements) {
		// pDiscoverOnTheseElements can be null, which implies all the lifelines on the diagram.
		try {
			if (m_DrawingArea != null)
			{
				switch (m_DrawingArea.getDiagramKind()) {
					case DiagramEnums.DK_COLLABORATION_DIAGRAM :
						// TODO:  discover the message on the collaboration diagram
						break;
	
					case DiagramEnums.DK_SEQUENCE_DIAGRAM :
						return discoverSQDMessages(pDiscoverOnTheseElements);
	
					default :
						// do nothing
						break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Creates a port provided interface on the port presentation element
	 *
	 * @param pPortPE [in] The port to create port provided interface
	 * @param pInterface [in] The port provided interface
	 * @param pPresentationElements [out,retval] The created interface PEs
	 */
	public ETList < IPresentationElement > createPortProvidedInterface(IPresentationElement pPortPE, IInterface pInterface) {
		if (pPortPE == null || pInterface == null || m_DrawingArea == null)
			return null;

		try {
			ETList < IPresentationElement > pCreatedPEs = new ETArrayList < IPresentationElement > ();

			// Create the interface as a lollypop and then create the edge connecting the port and the interface
			IPresentationElement pInterfaceAsLollypop = null;
			IETPoint pPoint = new ETPoint();

			m_DrawingArea.setModelElement(pInterface);
			TSNode pCreatedNode = m_DrawingArea.addNode(new String("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Interface LollypopNotation"), pPoint, false, false);
			m_DrawingArea.setModelElement(null);
			if (pCreatedNode != null) {
				pInterfaceAsLollypop = TypeConversions.getPresentationElement(pCreatedNode);
			}

			if (pInterfaceAsLollypop != null) {
				pCreatedPEs.add(pInterfaceAsLollypop);

				// Create the edge.  The edge is an interface of the draw engine type PortProvidedInterfaceEdgeDrawEngine
				IPresentationElement cpCreatedEdgePE;
				cpCreatedEdgePE = createEdgeForElement(pInterface, "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge PortProvidedInterface", pPortPE, pInterfaceAsLollypop);
				if (cpCreatedEdgePE != null) {
					pCreatedPEs.add(cpCreatedEdgePE);
				}
			}
			return pCreatedPEs.size() > 0 ? pCreatedPEs : null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/// Discovers generalization relationships on the current diagram
	public ETList < IPresentationElement > discoverGeneralizations() {
		try {
			ETList < IPresentationElement > pCreatedPresentationElements = new ETArrayList < IPresentationElement > ();

			// Get all the presentation elements on the current graph
			ETList < IElement > pCurrentElements = m_DrawingArea.getAllItems3();
			if (pCurrentElements == null)
				return null;

			Iterator < IElement > iter = pCurrentElements.iterator();
			while (iter.hasNext()) {
				IElement pElement = iter.next();
				IClassifier pClassifier = pElement instanceof IClassifier ? (IClassifier) pElement : null;
				// Now see if we have generalizations
				if (pClassifier != null) {

					// Get the generalization on the classifier
					ETList < IGeneralization > pGeneralizations = pClassifier.getGeneralizations();
					if (pGeneralizations == null)
						continue;

					Iterator < IGeneralization > generalizationIter = pGeneralizations.iterator();
					while (generalizationIter.hasNext()) {
						IGeneralization pGeneralization = generalizationIter.next();
						if (pGeneralization != null) {
							// Create the presentation element
							IPresentationElement pCreatedElement = createPresentationElement(pGeneralization);
							if (pCreatedElement != null) {
								pCreatedPresentationElements.add(pCreatedElement);
							}
						}
					}
				}
			}

			// autoroute the edges
			autoRouteEdges(pCreatedPresentationElements);
			return pCreatedPresentationElements.size() > 0 ? pCreatedPresentationElements : null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Special case the creates an IInterface presentation element
	 *
	 * @param pElement [in] The interface which we need to create a presentation element for.
	 * @param pClass [in] The class to attach the interface to.
	 * @param pPresentationElement [out,retval] The newly created presentation element.
	 */

	public IPresentationElement createInterfaceAsIconPresentationElement(IInterface pElement, IPresentationElement pClassPE) {
		if (pElement == null || pClassPE == null)
			return null;

		try {
			IElement pImplementation = null;
			IProductGraphPresentation pProductPE = pClassPE instanceof IProductGraphPresentation ? (IProductGraphPresentation) pClassPE : null;
			IElement pClass = pProductPE != null ? pProductPE.getModelElement() : null;
			INamedElement pNamedClass = pClass instanceof INamedElement ? (INamedElement) pClass : null;

			if (pNamedClass != null) {
				// Make sure that this class has a suplier that is the IInterface passed in.
				IteratorT < IDependency > iter = new IteratorT < IDependency > (pNamedClass.getClientDependencies());

				while (iter.hasNext()) {
					IDependency pDependency = iter.next();

					IImplementation pTempImplementation = pDependency instanceof IImplementation ? (IImplementation) pDependency : null;
					if (pTempImplementation != null) {
						INamedElement pSupplier = pTempImplementation.getSupplier();
						if (pSupplier != null && pSupplier.isSame(pElement)) {
							pImplementation = pTempImplementation;
							break;
						}
					}
				}
			}

			if (pImplementation != null) {
				// The default for an interface is the icon
				IPresentationElement pPresentationElement = createPresentationElement(pElement);

				// If it gets created then create a implementation between the two
				if (pPresentationElement != null) {
					IPresentationElement pImplementationLinkPE = createLinkPresentationElement(pImplementation, pClassPE, pPresentationElement);
					return pImplementationLinkPE;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Special case the creates an IInterface presentation element
	 *
	 * @param pElement [in] The interface which we need to create a presentation element for.
	 * @param pPresentationElement [out,retval] The newly created presentation element.
	 */
	public IPresentationElement createInterfaceAsClassPresentationElement(IInterface pElement) {
		return createPresentationElementUsingInitString(pElement, "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Interface ClassNotation", null, PEKind.PEK_ANY);
	}

	/**
	 * Creates a port provided interface on the port presentation element
	 *
	 * @param pPortPE [in] The port to create port required interface
	 * @param pInterface [in] The port provided interface
	 * @param pPresentationElements [out,retval] The created interface PEs
	 */
	public ETList < IPresentationElement > createPortRequiredInterface(IPresentationElement pPortPE, IInterface pInterface) {
		if (pPortPE == null || pInterface == null || m_DrawingArea == null)
			return null;

		try {
			ETList < IPresentationElement > pCreatedPEs = new ETArrayList < IPresentationElement > ();

			// Get the usage relationship that may already exist between these two items
			IUsage pUsage = getUsageRelationship(pPortPE, pInterface);
			if (pUsage == null)
				return null;

			// Create the interface as a lollypop and then create the edge connecting the port and the interface
			IPresentationElement pInterfaceAsLollypop;
			IETPoint pPoint = new ETPoint();

			m_DrawingArea.setModelElement(pInterface);
			TSNode pCreatedNode = m_DrawingArea.addNode("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Interface LollypopNotation", pPoint, false, false);
			m_DrawingArea.setModelElement(null);
			if (pCreatedNode != null) {
				pInterfaceAsLollypop = TypeConversions.getPresentationElement(pCreatedNode);
			} else
				return null;

			if (pInterfaceAsLollypop != null) {
				pCreatedPEs.add(pInterfaceAsLollypop);

				// Create the edge which is a usage from the port to the interface
				// If there is not a usage edge already, this'll create a new usage relationship
				IPresentationElement cpCreatedEdgePE = createEdgeForElement(pUsage, "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Usage", pPortPE, pInterfaceAsLollypop);
				if (cpCreatedEdgePE != null) {
					pCreatedPEs.add(cpCreatedEdgePE);
				}
			}

			return pCreatedPEs.size() > 0 ? pCreatedPEs : null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public IPresentationElement createAssociationClassPresentationElement(IAssociationClass pAssociationClass, IPresentationElement pClass1, IPresentationElement pClass2) {
		if (pAssociationClass == null || pClass1 == null || pClass2 == null)
			return null;

		IPresentationElement pPresentationElement = null;
		try {
			// The default for an interface is the icon
			pPresentationElement = createPresentationElement(pAssociationClass);

			if (pPresentationElement != null) {

				IDrawEngine pEngine = TypeConversions.getDrawEngine(pPresentationElement);
				IEventManager pEventManager = pEngine != null ? pEngine.getEventManager() : null;

				IAssociationClassEventManager pAssEventManager = pEventManager instanceof IAssociationClassEventManager ? (IAssociationClassEventManager) pEventManager : null;

				if (pAssEventManager != null) {
					boolean bReconnectedOK = pAssEventManager.reconnectBridges(pClass1, pClass2);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pPresentationElement;
	}

	public ETList < IPresentationElement > discoverCommentLinks(boolean bAutoRouteEdges, ETList < IElement > pNewElementsBeingCreated, ETList < IElement > pElementsAlreadyOnTheDiagrams) {
		try {
			// Now we need to discover comment links.  This is equally complicated for the same reasons as above.
			// We need to find all comments being dropped and see if there are any annotated elements already on 
			// the diagram then visa versa.
			ETList < IPresentationElement > pCreatedElements = discoverCommentLinks(pNewElementsBeingCreated);
			ETList < IPresentationElement > pPresentationElements = new ETArrayList < IPresentationElement > ();

			// Discover comment links among items being created on the diagram
			if (pCreatedElements != null && pPresentationElements != null) {
				pPresentationElements.addAll(pPresentationElements);
			}
			
			if (pCreatedElements != null){
				pCreatedElements.clear();
			}
			

			// Now see if any of the elements being created are related to elements already on the diagram.  The
			// easiest way to do this is get a list of all IComments being dropped and those already on the diagram.
			// Then discover using the two sets of elements
			ETList < IElement > pCommentsBeingCreated = new ETArrayList < IElement > ();
			ETList < IElement > pCommentsAlreadyOnTheDiagram = new ETArrayList < IElement > ();
			ETList < IElement > pNonCommentsBeingCreated = new ETArrayList < IElement > ();
			ETList < IElement > pNonCommentsAlreadyOnTheDiagram = new ETArrayList < IElement > ();

			IteratorT < IElement > iter = new IteratorT < IElement > (pNewElementsBeingCreated);

			// Divide the items being dropped into two groups - comments and non-comments
			while (iter.hasNext()) {
				IElement pElement = iter.next();
				if (pElement instanceof IComment) {
					pCommentsBeingCreated.add(pElement);
				} else {
					pNonCommentsBeingCreated.add(pElement);
				}
			}

			// Divide the items already on the diagram into two groups - comments and non-comments
			if (pElementsAlreadyOnTheDiagrams != null)
				iter.reset(pElementsAlreadyOnTheDiagrams);

			while (iter.hasNext()) {
				IElement pElement = iter.next();
				if (pElement instanceof IComment) {
					pCommentsAlreadyOnTheDiagram.add(pElement);
				} else {
					pNonCommentsAlreadyOnTheDiagram.add(pElement);
				}
			}

			// Now do three calls to search for links
			// pCommentsBeingCreated + pNonCommentsAlreadyOnTheDiagram
			// pNonCommentsBeingCreated + pCommentsAlreadyOnTheDiagram
			// pCommentsBeingCreated + pCommentsAlreadyOnTheDiagram (comments can annotate comments)
			//
			// The only bug is that comment to comment links will reappear when both comments are already
			// on the diagram.  But the code is much simpler and this use case doesn't pop up.
			long commentsBeingCreatedCount = pCommentsBeingCreated.size();
			long nonCommentsBeingCreatedCount = pNonCommentsBeingCreated.size();
			long commentsAlreadyOnTheDiagramCount = pCommentsAlreadyOnTheDiagram.size();
			long nonCommentsAlreadyOnTheDiagramCount = pNonCommentsAlreadyOnTheDiagram.size();
			
			if (pCreatedElements != null)
			{
				pCreatedElements.clear();
			}
			
			// 1. pCommentsBeingCreated + pNonCommentsAlreadyOnTheDiagram
			if (commentsBeingCreatedCount > 0 && nonCommentsAlreadyOnTheDiagramCount > 0) {
				ETList < IElement > pNewList = new ETArrayList < IElement > ();
				pNewList.addAll(pCommentsBeingCreated);
				pNewList.addAll(pNonCommentsAlreadyOnTheDiagram);

				pCreatedElements = discoverCommentLinks(pNewList);
				if (pCreatedElements != null && pPresentationElements != null) {
					pPresentationElements.addAll(pCreatedElements);
				}
				
				if (pCreatedElements != null)
				{
					pCreatedElements.clear();
				}
			}

			// 2. pNonCommentsBeingCreated + pCommentsAlreadyOnTheDiagram
			if (nonCommentsBeingCreatedCount > 0 && commentsAlreadyOnTheDiagramCount > 0) {
				ETList < IElement > pNewList = new ETArrayList < IElement > ();

				pNewList.addAll(pNonCommentsBeingCreated);
				pNewList.addAll(pCommentsAlreadyOnTheDiagram);

				pCreatedElements = discoverCommentLinks(pNewList);
				if (pCreatedElements != null && pPresentationElements != null) {
					pPresentationElements.addAll(pCreatedElements);
				}
				
				if (pCreatedElements != null)
				{
					pCreatedElements.clear();
				}
			}

			// 3. pCommentsBeingCreated + pCommentsAlreadyOnTheDiagram
			if (commentsBeingCreatedCount > 0 && commentsAlreadyOnTheDiagramCount > 0) {
				ETList < IElement > pNewList = new ETArrayList < IElement > ();

				pNewList.addAll(pCommentsBeingCreated);
				pNewList.addAll(pCommentsAlreadyOnTheDiagram);

				pCreatedElements = discoverCommentLinks(pNewList);
				if (pCreatedElements != null && pPresentationElements != null) {
					pPresentationElements.addAll(pCreatedElements);
				}
				
				if (pCreatedElements != null)
				{
					pCreatedElements.clear();
				}
			}
			return pPresentationElements.size() > 0 ? pPresentationElements : null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Discover part facade links when the user drops onto the diagram
	 *
	 * @param bAutoRouteEdges [in] Should we autoroute the edges
	 * @param pNewElementsBeingCreated [in] The elements being created dropped onto the diagram
	 * @param pElementsAlreadyOnTheDiagrams [in] The elements already on the diagram
	 * @param pPresentationElements [out,retval] The created presentation elements
	 */
	public ETList < IPresentationElement > discoverPartFacadeLinks(boolean bAutoRouteEdges, ETList < IElement > pNewElementsBeingCreated, ETList < IElement > pElementsAlreadyOnTheDiagrams) {
		try {
			// Now we need to discover part facade links.  This is equally complicated for the same reasons as above.
			// We need to find all part facade being dropped and see if there are any collaborations already on 
			// the diagram then visa versa.
			ETList < IPresentationElement > pCreatedElements = discoverPartFacadeLinks(pNewElementsBeingCreated);
			ETList < IPresentationElement > pPresentationElements = new ETArrayList < IPresentationElement > ();

			// Discover partfacade links among items being created on the diagram
			if (pCreatedElements != null && pPresentationElements != null) {
				pPresentationElements.addAll(pCreatedElements);
			}
			pCreatedElements.clear();

			// Now see if any of the elements being created are related to elements already on the diagram.  The
			// easiest way to do this is get a list of all IPartFacade's being dropped and those already on the diagram.
			// Then discover using the two sets of elements
			ETList < IElement > pPartFacadesBeingCreated = new ETArrayList < IElement > ();
			ETList < IElement > pPartFacadesAlreadyOnTheDiagram = new ETArrayList < IElement > ();
			ETList < IElement > pCollaborationsBeingCreated = new ETArrayList < IElement > ();
			ETList < IElement > pCollaborationsAlreadyOnTheDiagram = new ETArrayList < IElement > ();

			IteratorT < IElement > iter = new IteratorT < IElement > (pNewElementsBeingCreated);

			// Divide the items being dropped into two groups - collaborations and partfacades
			while (iter.hasNext()) {
				IElement pElement = iter.next();

				if (pElement instanceof IPartFacade) {
					pPartFacadesBeingCreated.add(pElement);
				} else if (pElement instanceof ICollaboration) {
					pCollaborationsBeingCreated.add(pElement);
				}
			}

			// Divide the items already on the diagram into two groups - collaborations and partfacades
			if (pElementsAlreadyOnTheDiagrams != null)
				iter.reset(pElementsAlreadyOnTheDiagrams);
			while (iter.hasNext()) {
				IElement pElement = iter.next();

				if (pElement instanceof IPartFacade) {
					pPartFacadesAlreadyOnTheDiagram.add(pElement);
				} else if (pElement instanceof ICollaboration) {
					pCollaborationsAlreadyOnTheDiagram.add(pElement);
				}
			}

			// Now do three calls to search for links
			// pPartFacadesBeingCreated + pNonPartFacadesAlreadyOnTheDiagram
			// pNonPartFacadesBeingCreated + pPartFacadesAlreadyOnTheDiagram
			//
			// The only bug is that partFacade to partFacade links will reappear when both partFacades are already
			// on the diagram.  But the code is much simpler and this use case doesn't pop up.
			long partFacadesBeingCreatedCount = pPartFacadesBeingCreated.size();
			long collaborationsBeingCreatedCount = pCollaborationsBeingCreated.size();
			long partFacadesAlreadyOnTheDiagramCount = pPartFacadesAlreadyOnTheDiagram.size();
			long collaborationsAlreadyOnTheDiagramCount = pCollaborationsAlreadyOnTheDiagram.size();

			pCreatedElements.clear();
			// 1. pPartFacadesBeingCreated + pNonPartFacadesAlreadyOnTheDiagram
			if (partFacadesBeingCreatedCount > 0 && collaborationsAlreadyOnTheDiagramCount > 0) {
				ETList < IElement > pNewList = new ETArrayList < IElement > ();

				pNewList.addAll(pPartFacadesBeingCreated);
				pNewList.addAll(pCollaborationsAlreadyOnTheDiagram);

				pCreatedElements = discoverPartFacadeLinks(pNewList);
				if (pCreatedElements != null && pPresentationElements != null) {
					pPresentationElements.addAll(pCreatedElements);
				}
				pCreatedElements.clear();
				pNewList.clear();
			}

			// 2. pNonPartFacadesBeingCreated + pPartFacadesAlreadyOnTheDiagram
			if (collaborationsBeingCreatedCount > 0 && partFacadesAlreadyOnTheDiagramCount > 0) {
				ETList < IElement > pNewList = new ETArrayList < IElement > ();

				pNewList.addAll(pCollaborationsBeingCreated);
				pNewList.addAll(pPartFacadesAlreadyOnTheDiagram);

				pCreatedElements = discoverPartFacadeLinks(pNewList);
				if (pCreatedElements != null && pPresentationElements != null) {
					pPresentationElements.addAll(pCreatedElements);
				}
				pCreatedElements.clear();
				pNewList.clear();
			}
			return pPresentationElements != null && pPresentationElements.size() > 0 ? pPresentationElements : null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Discover associationclass links when the user drops onto the diagram
	 *
	 * @param bAutoRouteEdges [in] Should we autoroute the edges
	 * @param pNewElementsBeingCreated [in] The elements being created dropped onto the diagram
	 * @param pElementsAlreadyOnTheDiagrams [in] The elements already on the diagram
	 * @param pPresentationElements [out,retval] The created presentation elements
	 */
	public ETList < IPresentationElement > discoverAssociationClassLinks(boolean bAutoRouteEdges, ETList < IElement > pNewElementsBeingCreated, ETList < IElement > pElementsAlreadyOnTheDiagrams) {
		try {
			// Now we need to discover associationclass links.  This is equally complicated for the same 
			// reasons as above.  We need to find all associationclasses being dropped and see if 
			// there are any collaborations already on the diagram then visa versa.

			// Discover association class links among items being created on the diagram
			ETList < IPresentationElement > pCreatedElements = discoverAssociationClassLinks(pNewElementsBeingCreated);
			ETList < IPresentationElement > pPresentationElements = new ETArrayList < IPresentationElement > ();
			if (pCreatedElements != null && pPresentationElements != null) {
				pPresentationElements.addAll(pCreatedElements);
			}
			
			if (pCreatedElements != null)
				pCreatedElements.clear();

			// Now see if any of the elements being created are related to elements already on the diagram.

			// TODO - NEED TO FINISH THIS UP, This is a C++ Comment, Kevin Madden
			return pPresentationElements != null && pPresentationElements.size() > 0 ? pPresentationElements : null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Used to create a nested link between the argument elements
	 *
	 * @param pNestedChild [in] The child element that's owned by pNamespace
	 * @param pNamespace [in]  The namespace owner of pNestedChild
	 * @param pCreatedPE [out] The created presentation element for the link.  Note that it's not an error
	 * for a NULL to be returned - the link may already exist.
	 */
	public IPresentationElement createNestedLink(INamedElement pNestedChild, INamespace pNamespace) {
		if (pNestedChild == null || pNamespace == null)
			return null;

		try {
			IElement pFromElement = pNestedChild instanceof IElement ? (IElement) pNestedChild : null;
			IElement pToElement = pNamespace instanceof IElement ? (IElement) pNamespace : null;
			IElement pLinkElement = pNestedChild;
			String sInitializationString = new String("org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge NestedLink");

			ETList < IPresentationElement > pStartPEs;
			ETList < IPresentationElement > pEndPEs;

			// See if they both have presentation elements on this diagram
			pStartPEs = m_DrawingArea.getAllNodeItems(pFromElement);
			pEndPEs = m_DrawingArea.getAllNodeItems(pToElement);
			// Now see how many presentation items we have

			long numStartPEs = pStartPEs != null ? pStartPEs.size() : 0;
			long numEndPEs = pEndPEs != null ? pEndPEs.size() : 0;

			// We have presentation elements on both the general and specific
			if (numStartPEs > 0 && numEndPEs > 0 && numStartPEs == 1 && numEndPEs == 1) {
				IPresentationElement pFromPresentationElement = (IPresentationElement) pStartPEs.iterator().next();
				IPresentationElement pToPresentationElement = (IPresentationElement) pEndPEs.iterator().next();
				if (linkAlreadyExists(pLinkElement, pFromPresentationElement, pToPresentationElement) == false
					&& linkIsValid(sInitializationString, pFromPresentationElement, pToPresentationElement) == true) {
					// In the case of nested links we do one more check not done for other links.  If the 
					// from element is sitting on the to element then don't do the link.  The namespace
					// containment is being represented by graphical containment - no need for the link.
					INodePresentation pFromNodePresentation = pFromPresentationElement instanceof INodePresentation ? (INodePresentation) pFromPresentationElement : null;
					INodePresentation pToNodePresentation = pToPresentationElement instanceof INodePresentation ? (INodePresentation) pToPresentationElement : null;
					if (pFromNodePresentation != null && pToPresentationElement != null) {
						IETRect pFromBoundingRect = pFromNodePresentation.getBoundingRect();
						IETRect pToBoundingRect = pToNodePresentation.getBoundingRect();
						boolean bIsParentGraphicalContainer;
						if (pFromBoundingRect != null && pToBoundingRect != null) {
							// See if this child has a parent graphical container which is the parent
							// namespace.  If so then don't create the link
							//bIsParentGraphicalContainer = pToBoundingRect.isContained(pFromBoundingRect); //Jyothi: This is the trouble spot.. it should return true as the class is contained in the package..
                                                        bIsParentGraphicalContainer = pToBoundingRect.contains(pFromBoundingRect); //Jyothi.
						} else
							bIsParentGraphicalContainer = false;

						if (!bIsParentGraphicalContainer) {
							return createEdgeForElement(pLinkElement, sInitializationString, pFromPresentationElement, pToPresentationElement);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public IPresentationElement createPresentationElement(IElement pElement) {
		if (pElement == null)
			return null;

		try {
			// Filter out all the items that don't belong on the structural diagram
			String sElementType = pElement.getElementType();
			if (sElementType != null && sElementType.compareTo("Message") != 0) {
				return super.createPresentationElement(pElement);
			} else
				return null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	protected boolean linkAlreadyExists(IElement pLinkElement, IPresentationElement pStartNode, IPresentationElement pEndNode) {

		INodePresentation pStartNodePresentation = pStartNode instanceof INodePresentation ? (INodePresentation) pStartNode : null;
		INodePresentation pEndNodePresentation = pEndNode instanceof INodePresentation ? (INodePresentation) pEndNode : null;
		if (pStartNodePresentation != null && pEndNodePresentation != null) {
			ETList < IPresentationElement > pPEs = pStartNodePresentation.getEdgesWithEndPoint(true, true, pEndNodePresentation);
			if (pPEs == null)
				return false;

			// See if any of these links have pLinkElement as a model element
			Iterator < IPresentationElement > iter = pPEs.iterator();
			while (iter.hasNext()) {
				IPresentationElement pThisPE = iter.next();
				if (pThisPE != null) {
					IElement pThisElement = pThisPE.getFirstSubject();
					if (pThisElement != null && pThisElement.isSame(pLinkElement)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Checks to see if this link is allowed between these two types
	 *
	 * @param sInitializationString [in] The init string for the new link
	 * @param pLinkElement [in] The element that is attached to the new link
	 * @param pStartNode [in] The start node of the link we're trying to find
	 * @param pEndNode [in] The end node of the link we're trying to find
	 */
	protected boolean linkIsValid(String sInitializationString, IPresentationElement pStartNode, IPresentationElement pEndNode) {
		if (pStartNode == null || pEndNode == null || sInitializationString == null)
			return false;

		if (sInitializationString.compareTo("org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge NestedLink") == 0) {
			IElement pStartME = TypeConversions.getElement(pStartNode);
			IElement pEndME = TypeConversions.getElement(pEndNode);

			String sStartType = pStartME != null ? pStartME.getElementType() : null;
			String sEndType = pEndME != null ? pEndME.getElementType() : null;

			if (sStartType != null
				&& sEndType != null
				&& (sStartType.compareTo("PartFacade") == 0 || sEndType.compareTo("Collaboration") == 0 || sStartType.compareTo("Collaboration") == 0 || sEndType.compareTo("PartFacade") == 0)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Create a new TS Edge
	 */
	public IPresentationElement createEdgeForElement(IElement pElement, final String bsEdgeInitString, IPresentationElement sourcePE, IPresentationElement targetPE) {
		if (pElement == null || sourcePE == null || targetPE == null)
			return null;

		IPresentationElement ppCreatedEdgePE = null;
		try {

			IETNode cpSourceNode = TypeConversions.getETNode(sourcePE);
			IETNode cpTargetNode = TypeConversions.getETNode(targetPE);

			if (cpSourceNode != null && cpTargetNode != null) {
				IETEdge cpCreatedEdge = createEdgeForElement(pElement, bsEdgeInitString, cpSourceNode, cpTargetNode);
				if (cpCreatedEdge != null) {
					ppCreatedEdgePE = TypeConversions.getPresentationElement(cpCreatedEdge);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ppCreatedEdgePE;
	}

	/**
	 * Create a new TS Edge
	 *
	 * @note This function should probably be moved to IAxDrawingAreaControl
	 */
	protected IETEdge createEdgeForElement(IElement pElement, final String bsEdgeInitString, IETNode pSourceNode, IETNode pTargetNode) {
		if (pElement == null || pSourceNode == null || pTargetNode == null || m_DrawingArea == null)
			return null;

		IETEdge ppCreatedEdge = null;
		try {

			// Set the model element so the edge attaches and isn't creating a new model element
			m_DrawingArea.setModelElement(pElement);

			ppCreatedEdge = (IETEdge) m_DrawingArea.addEdge(bsEdgeInitString, (TSNode) pSourceNode.getObject(), (TSNode) pTargetNode.getObject(), false, false);

			// Reset the model element back to NULL
			m_DrawingArea.setModelElement(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ppCreatedEdge;
	}

	/**
	 * Used to create a part facade link between the argument elements
	 *
	 * @param pPartFacade [in] The partfacade already on the diagram
	 * @param pCollaboration [in] A collaboration that has pPartFacade as a template parameter, between these two we want to create a new edge
	 * @param pCreatedPE [out] The created PE, null if a link already exists.
	 */
	public IPresentationElement createPartFacadeLink(IParameterableElement pParameterableElement, ICollaboration pCollaboration) {
		return createLink(new String("org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge PartFacade"), pCollaboration, pParameterableElement, pParameterableElement);
	}

	/**
	 * Used to create a link
	 *
	 * @param sInitializationString [in] The init string for the edge to created
	 * @param pFromElement [in] The source of the new link
	 * @param pToElement [in] The target of the new link
	 * @param pLinkElement [in] The controlling element for the link
	 * @param pCreatedPE [out] The presentation element that we created.  If the link already exists then
	 * this will be null.
	 */
	protected IPresentationElement createLink(String sInitializationString, IElement pFromElement, IElement pToElement, IElement pLinkElement) {
		if (pFromElement == null || pToElement == null || pLinkElement == null || sInitializationString == null)
			return null;

		try {
			ETList < IPresentationElement > pStartPEs = m_DrawingArea.getAllNodeItems(pFromElement);
			ETList < IPresentationElement > pEndPEs = m_DrawingArea.getAllNodeItems(pToElement);

			// We have presentation elements on both the general and specific
			// We only handle the easy case for now where they each have only one
			// PE on this diagram.
			if (pStartPEs != null && pEndPEs != null && pStartPEs.size() == 1 && pEndPEs.size() == 1) {
				IPresentationElement pFromPresentationElement = (IPresentationElement) pStartPEs.iterator().next();
				IPresentationElement pToPresentationElement = (IPresentationElement) pEndPEs.iterator().next();
				if (linkAlreadyExists(pLinkElement, pFromPresentationElement, pToPresentationElement) == false
					&& linkIsValid(sInitializationString, pFromPresentationElement, pToPresentationElement) == true) {
					return createEdgeForElement(pLinkElement, sInitializationString, pFromPresentationElement, pToPresentationElement);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Used to create a comment link between the argument elements
	 *
	 * @param pComment [in] The comment already on the diagram
	 * @param pAnnotatedElement [in] An annotated element of pComment, between these two we want to create a new edge
	 * @param pCreatedPE [out] The created PE, NULL if a link already exists.
	 */
	protected IPresentationElement createCommentLink(IComment pComment, IElement pAnnotatedElement) {
		return createLink(new String("org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge CommentEdge"), pAnnotatedElement, pComment, pComment);
	}

	/**
	 * Discover messages on the SQD
	 */
   public ETList < IPresentationElement > discoverSQDMessages(ETList < IElement > elements)
   {
      // elements can be null, which implies all the lifelines on the diagram.
      //  if( ppNewPEs );
      if (m_DrawingArea == null)
      {
         return null;
      }

      try
      {
         INamespace namespace = m_DrawingArea.getNamespace();
         IInteraction interaction = namespace instanceof IInteraction ? (IInteraction)namespace : null;

         if (interaction == null)
         {
            return null;
         }

         // Use all the lifelines and interactions on the diagram if there are no input elements
         if (elements == null)
         {
            elements = m_DrawingArea.getAllElementsByType("Lifeline"); // NO NLS

            ETList < IElement > moreElements = m_DrawingArea.getAllElementsByType("Interaction");
//            if (moreElements != null)
            if ( (moreElements != null) && (elements != null) )  //Jyothi: Fix for NPE (Bug#6380623) 
            {
               elements.addAll(moreElements);
            }
         }

         // For each message, see if either end is in the list of elements

         ETList < IMessage > messages = interaction.getMessages();
         if (messages != null)
         {
            // This is the list of messages that don't have presentation elements on the diagram
            ETList < IMessage > addMessages = null;

            // All the message presentation elements on this diagram
            ETList < IPresentationElement > messagePEs = m_DrawingArea.getAllByType("Message"); // NO NLS

            if (messagePEs != null)
            {
               // TS logical vertical location, start with max long for the top of the diagram
               //					LONG_MAX;
               int verticalOffset = 0;

               // keeps track of the previous message's kind
               int kindPrevious = IMessageKind.MK_UNKNOWN;
               
               IteratorT < IMessage > msgIter = new IteratorT < IMessage > (messages);
               while (msgIter.hasNext())
               {
                  IMessage message = msgIter.next();

                  if (message != null)
                  {
                     int kind = message.getKind();

                     // Determine if the message is represented by a presentation on this diagram
                     boolean bIsRepresentedInList = isRepresentedInList(messagePEs, message);
                     if (!bIsRepresentedInList)
                     {
                        verticalOffset = addSQDMessage(message, elements, kind, kindPrevious, verticalOffset);
                     }
                     else
                     {
                        m_MapMessageEngines.testInitialize(m_DrawingArea);

                        // Move the vertical location to just below the existing message
                        IPresentationElement pe = m_MapMessageEngines.get(message);
                        IETRect rect = TypeConversions.getLogicalBoundingRect(pe);
                        
                        //Jyothi: Fixing the NPE #6383633
//                        verticalOffset = rect.getBottom();
                        if (rect != null)
                            verticalOffset = rect.getBottom();
                        else
                            Debug.out.println(" RECT IS NULL... ");

                        // TODO:  determine if the above needs an offset
                     }

                     kindPrevious = kind;
                  }
               }
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return null; // This function does nothing in C++
   }

   /**
    * Determine the location where the messages should start based on the input lifelines
    */
   protected int determineMessageStart( ILifeline lifeline, int verticalOffset )
   {
      m_MapLifelineEngines.testInitialize( m_DrawingArea );
      IDrawEngine drawEngine = m_MapLifelineEngines.getDrawEngine( lifeline );
      if (drawEngine instanceof ILifelineDrawEngine)
      {
         ILifelineDrawEngine engine = (ILifelineDrawEngine)drawEngine;
         
         IETRect rectBounding = TypeConversions.getLogicalBoundingRect( engine );
         verticalOffset = Math.min( verticalOffset, rectBounding.getTop() - 50 );
         // The above offset mathches the offset in CSequenceDiagramGenerator.createLifelines()
         // TODO: determine if this location offset is OK
      }
      
      return verticalOffset;
   }

   /**
    * Add the message presentation element to the SQD if either of its end's lifelines is in the list of elements
    */
   protected int addSQDMessage( IMessage message,
                                 ETList< IElement > elements,
                                 final int kind,
                                 final int kindPrevious,
                                 int verticalOffset )
   {
      if( null == message ) throw new IllegalArgumentException();
      if( null == elements ) throw new IllegalArgumentException();

      ILifeline sendingLifeline = message.getSendingLifeline();
   
      ILifeline receivingLifeline = message.getReceivingLifeline();

      if( (sendingLifeline != null) &&
          (receivingLifeline != null)  )
      {
         if( Long.MAX_VALUE != verticalOffset )
         {
            boolean bIsMessageToSelf = sendingLifeline.isSame( receivingLifeline );

            // This switch statement is copied from CSequenceDiagramGenerator.createMessages()
            // Determine the delta offset for the vertical location
            // ILifelineDrawEngine.createMessage() also changes this height
            switch( kindPrevious )
            {
            case IMessageKind.MK_CREATE:
               verticalOffset -= 40;
               break;

            case IMessageKind.MK_RESULT:
               if( kind == IMessageKind.MK_RESULT )
               {
                  verticalOffset -= 15;
               }
               else
               {
                  verticalOffset -= (bIsMessageToSelf) ? 50 : 35;
                  verticalOffset -=  35;
               }
               break;

            case IMessageKind.MK_SYNCHRONOUS:
               if( bIsMessageToSelf )
               {
                  verticalOffset -= 5;
               }
               break;

            default:
               verticalOffset -= 20;
               break;
            }
         }

         // when the lifeline is on either end of the message
         // create the message's presentation element
         if( areBothInList( elements, sendingLifeline, receivingLifeline ))
         {
            m_MapLifelineEngines.testInitialize( m_DrawingArea );
            ILifelineDrawEngine sendingEngine = (ILifelineDrawEngine)m_MapLifelineEngines.getDrawEngine( sendingLifeline );
            ILifelineDrawEngine receivingEngine = (ILifelineDrawEngine)m_MapLifelineEngines.getDrawEngine( receivingLifeline );

            if( (sendingEngine != null) &&
                (receivingEngine != null) )
            {
               if( Integer.MAX_VALUE == verticalOffset )
               {
                  determineMessageStart( sendingLifeline, verticalOffset );
                  determineMessageStart( receivingLifeline, verticalOffset );
               }

               ETPairT<IMessageEdgeDrawEngine,Integer> result =
                  sendingEngine.createMessage( message, receivingEngine, verticalOffset );
               verticalOffset = result.getParamTwo().intValue();
            }
         }
      }
      else if( (sendingLifeline == null) &&
               (receivingLifeline != null) )
      {
         // UPDATE:  Move this code to IMessage?
         IInteraction sendingInteraction = null;
         {
            IEventOccurrence event = message.getSendEvent();
            if( event != null )
            {
               OwnerRetriever< IInteraction > ownerRetriever = new OwnerRetriever< IInteraction >( event );
               sendingInteraction = ownerRetriever.getOwnerByType(IInteraction.class);
            }
         }

         if( sendingInteraction != null )
         {
            if( areBothInList( elements, sendingInteraction, receivingLifeline ))
            {
                m_MapInteractionEngines.testInitialize( m_DrawingArea );
                m_MapLifelineEngines.testInitialize( m_DrawingArea );

               if( Integer.MAX_VALUE == verticalOffset )
               {
                  determineMessageStart( receivingLifeline, verticalOffset );
               }
               else
               {
                  // Need to move the vertical location down a bit so we don't hook to the wrong piece
                  verticalOffset -= 5;
               }

               IPresentationElement sendingPE = m_MapInteractionEngines.get( sendingInteraction );
               IPresentationElement receivingPE = m_MapLifelineEngines.get( receivingLifeline );
               if( (sendingPE != null) &&
                   (receivingPE != null) )
               {
                   createEdgeForMessage( message, sendingPE, receivingPE, verticalOffset );
               }
            }
         }
      }
      else if( (sendingLifeline != null) &&
               (null == receivingLifeline) )
      {
         // UPDATE:  Move this code to IMessage?
         IInteraction receivingInteraction = null;
         {
            IEventOccurrence event = message.getReceiveEvent();
            if( event != null )
            {
               OwnerRetriever< IInteraction > ownerRetriever = new OwnerRetriever< IInteraction >( event );
               receivingInteraction = ownerRetriever.getOwnerByType(IInteraction.class);
            }
         }

         if( receivingInteraction != null )
         {
            if( areBothInList( elements, sendingLifeline, receivingInteraction ))
            {
                m_MapInteractionEngines.testInitialize( m_DrawingArea );
                m_MapLifelineEngines.testInitialize( m_DrawingArea );

               if( Long.MAX_VALUE == verticalOffset )
               {
                  determineMessageStart( sendingLifeline, verticalOffset );
               }
               else
               {
                  // Need to move the vertical location down a bit so we don't hook to the wrong piece
                  verticalOffset -= 5;
               }

               IPresentationElement sendingPE = m_MapLifelineEngines.get( sendingLifeline );
               IPresentationElement receivingPE = m_MapInteractionEngines.get( receivingInteraction );
               if( (sendingPE != null) &&
                   (receivingPE != null) )
               {
                   createEdgeForMessage( message, sendingPE, receivingPE, verticalOffset );
               }
            }
         }
      }

      return verticalOffset;
   }

   /**
    * Determine that both the input elements (1 and 2) are in the input list of elements.
    */
   protected boolean areBothInList( ETList< IElement > elements,
                                    IElement element1,
                                    IElement element2 )
   {
      boolean bIsInList = false;

      if( (element1 != null) &&
          (element2 != null) )
      {
         bIsInList = elements.isInList( element1 );
         if( !bIsInList )
         {
            bIsInList = elements.isInList( element2 );
         }
      }

      return bIsInList;
   }

   /**
    * Create a new TS Edge
    */
   protected void createEdgeForMessage( IMessage message,
                                        IPresentationElement sourcePE,
                                        IPresentationElement targetPE,
                                        int verticalLocation )
   {
      if( null == message ) throw new IllegalArgumentException();
      if( null == sourcePE ) throw new IllegalArgumentException();
      if( null == targetPE ) throw new IllegalArgumentException();

      // Create the TS Edge
      IPresentationElement peEdge = createEdgeForMessage( message,
                                             sourcePE,
                                             targetPE );
      TSEEdge tsEdge = TypeConversions.getOwnerEdge( peEdge, false );                                             

      if( tsEdge != null )
      {
         IConnectorsCompartment sourceCompartment = 
            (IConnectorsCompartment)TypeConversions.getCompartment( sourcePE, IConnectorsCompartment.class );
      
         IConnectorsCompartment targetCompartment = 
            (IConnectorsCompartment)TypeConversions.getCompartment( targetPE, IConnectorsCompartment.class );

         // Fix W7315:  Need to move the message to the "optimal" location,
         // For all messages except the result message the "optimal" location
         // is the highest vertical location of either end.
         // For result messages its the lowest vertical location.
         
         int kind = message.getKind();

         final int verticalDefault = (IMessageKind.MK_RESULT == kind) ? Integer.MAX_VALUE : Integer.MIN_VALUE;
         int sourceVertical = verticalDefault;
         int targetVertical = verticalDefault;

         if( (sourceCompartment != null) &&
             (targetCompartment != null) )
         {
            final IETRect rectSource = TypeConversions.getLogicalBoundingRect( sourcePE );
            final IETRect rectTarget = TypeConversions.getLogicalBoundingRect( targetPE );

            final boolean bIsLeftToRight = (rectSource.getCenterX() < rectTarget.getCenterX());
            final int cmkDirection =
               bIsLeftToRight ? IConnectMessageKind.CMK_LEFT_TO_RIGHT : IConnectMessageKind.CMK_RIGHT_TO_LEFT;

            // Connect the source end of the message
            Point ptSource = new Point( bIsLeftToRight ? rectSource.getRight() : rectSource.getLeft(), verticalLocation );
            IETPoint etPoint = PointConversions.newETPoint( ptSource );
            if( etPoint != null )
            {
               TSConnector sourceConnector = sourceCompartment.connectMessage( etPoint,
                                                          kind,
                                                          (IConnectMessageKind.CMK_START | cmkDirection),
                                                          null );
               if( sourceConnector != null )
               {
                  tsEdge.setSourceConnector( sourceConnector );

                  sourceVertical = (int)sourceConnector.getCenterY();
               }
            }

            // Connect the target end of the message
            Point ptTarget = new Point( bIsLeftToRight ? rectTarget.getLeft() : rectTarget.getRight(), verticalLocation );
            etPoint = PointConversions.newETPoint( ptTarget );
            if( etPoint != null )
            {
               TSConnector targetConnector =
                  targetCompartment.connectMessage( etPoint,
                                                    kind,
                                                   (IConnectMessageKind.CMK_FINISH | cmkDirection),
                                                   null );
               if( targetConnector != null )
               {
                  tsEdge.setTargetConnector( targetConnector );

                  targetVertical = (int)targetConnector.getCenterY();
               }
            }
         }

         IDrawEngine drawEngine = TypeConversions.getDrawEngine( peEdge );
         if (drawEngine instanceof IMessageEdgeDrawEngine)
         {
            IMessageEdgeDrawEngine engine = (IMessageEdgeDrawEngine)drawEngine;
            
            // Update the location of the message
            final long lNewVertical = (IMessageKind.MK_RESULT == kind)
               ? Math.min( sourceVertical, targetVertical )
               : Math.max( sourceVertical, targetVertical );
            engine.move( lNewVertical, false );

            // Make sure the operation is shown
            ILabelManager labelManager = engine.getLabelManager();
            if( labelManager != null )
            {
               labelManager.createInitialLabels() ;
            }
         }
      }
   }

   /**
    * Create a new TS Edge
    */
   protected IPresentationElement createEdgeForMessage( IMessage message,
                                                        IPresentationElement sourcePE,
                                                        IPresentationElement targetPE )
   {
      if( null == message ) throw new IllegalArgumentException();
      if( null == sourcePE ) throw new IllegalArgumentException();
      if( null == targetPE ) throw new IllegalArgumentException();
      
      IPresentationElement createdEdge = null;

      int kind = message.getKind();

      // This switch statement must match the one in LifelineDrawEngineImpl.createEdge()
      String strEdgeInitString;
      switch ( kind )
      {
         case IMessageKind.MK_CREATE :
            strEdgeInitString = "Embarcadero.ADViewFactory#CRelationEdge Message create";
            break;

         default :
            assert(false); // did we add another message kind?
            // no break

         case IMessageKind.MK_SYNCHRONOUS :
            strEdgeInitString = "Embarcadero.ADViewFactory#CRelationEdge Message";
            break;

         case IMessageKind.MK_ASYNCHRONOUS :
            strEdgeInitString = "Embarcadero.ADViewFactory#CRelationEdge Message asynchronous";
            break;

         case IMessageKind.MK_RESULT :
            strEdgeInitString = "Embarcadero.ADViewFactory#CRelationEdge Message result";
            break;
      };

      if ( strEdgeInitString.length() > 0 )
      {
         createdEdge = createEdgeForElement( message, strEdgeInitString, sourcePE, targetPE );
      }

      return createdEdge;
   }


	public static boolean isRepresentedInList(ETList < IPresentationElement > list, IElement pElement) {
		try {
			if (pElement == null || list == null)
				return false;
			IteratorT < IPresentationElement > iter = new IteratorT < IPresentationElement > (list);
			while (iter.hasNext()) {
				IPresentationElement pe = iter.next();
				if (pe != null) {
					IElement firstSubject = pe.getFirstSubject();
					if (firstSubject != null && pElement.isSame(firstSubject))
						return true;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	/**
	 * Is the diagram we represent a static diagram (ie not a sequence or collaboration)
	 *
	 * @return true if the diagram we represent is a class diagram
	 */
	protected boolean isStaticDiagram() {
		boolean bIsStaticDiagram = false;

		try {
			if (m_DrawingArea != null) {
				int diagramKind = m_DrawingArea.getDiagramKind();
				if (diagramKind != DiagramEnums.DK_SEQUENCE_DIAGRAM && diagramKind != DiagramEnums.DK_COLLABORATION_DIAGRAM) {
					bIsStaticDiagram = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bIsStaticDiagram;
	}

	/**
	 *
	 * Returns the usage relationship between these two items
	 *
	 * @param pPortPE [in] The port to create port required interface
	 * @param pInterface [in] The port provided interface
	 * @param pUsage [out] The discovered usage relationship
	 */
	protected IUsage getUsageRelationship(IPresentationElement pPortPE, IInterface pInterface) {
		if (pPortPE == null || pPortPE == null)
			return null;

		try {
			IRelationFactory factory = new RelationFactory();

			if (factory != null) {
				ETList < IElement > pDiscoverOnTheseElements = new ETArrayList < IElement > ();
				IElement pPortElement = TypeConversions.getElement(pPortPE);

				pDiscoverOnTheseElements.add(pPortElement);
				pDiscoverOnTheseElements.add(pInterface);

				IteratorT < IRelationProxy > proxiesIter = new IteratorT < IRelationProxy > (factory.determineCommonRelations(pDiscoverOnTheseElements));
				while (proxiesIter.hasNext()) {
					IRelationProxy proxy = proxiesIter.next();
					if (proxy != null) {
						IElement connection = proxy.getConnection();

						IUsage pPossibleUsage = connection instanceof IUsage ? (IUsage) connection : null;
						if (pPossibleUsage != null) {
							return pPossibleUsage;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.diagrams.ICoreRelationshipDiscovery#discoverCommonRelations(boolean)
	 */
	public ETList < IPresentationElement > discoverCommonRelations(boolean bAutoRouteEdges, ETList < IElement > pNewElementsBeingCreated, ETList < IElement > pElementsAlreadyOnTheDiagrams) 
	{
		try
		{
			// Normal relationship discovery logic
			ETList < IPresentationElement > allDiscovered = new ETArrayList<IPresentationElement>();
			
			ETList < IPresentationElement > newPresentationElements;
			newPresentationElements = super.discoverCommonRelations(bAutoRouteEdges, pNewElementsBeingCreated, pElementsAlreadyOnTheDiagrams);
			
				
			// Discover nested links and update the busy state if it's active
			//CBusyCtrlProxy::UpdateIfBusyActive(AfxGetInstanceHandle(), IDS_DISCOVERING_NESTED_LINKS);
			ETList < IPresentationElement > nestedLinks;
			nestedLinks = discoverNestedLinks(bAutoRouteEdges,
											 pNewElementsBeingCreated, 
											 pElementsAlreadyOnTheDiagrams);

			// Discover comment links and update the busy state if it's active
			//CBusyCtrlProxy::UpdateIfBusyActive(AfxGetInstanceHandle(), IDS_DISCOVERING_COMMENT_LINKS);
			ETList < IPresentationElement > commentLinks;
			commentLinks = discoverCommentLinks(bAutoRouteEdges,
											  pNewElementsBeingCreated, 
											  pElementsAlreadyOnTheDiagrams);

			// Discover part facade links and update the busy state if it's active
			//CBusyCtrlProxy::UpdateIfBusyActive(AfxGetInstanceHandle(), IDS_DISCOVERING_ASSOCIATIONCLASS_LINKS);
			ETList < IPresentationElement > associationClassLinks;
			associationClassLinks = discoverAssociationClassLinks(bAutoRouteEdges,
														  pNewElementsBeingCreated, 
														  pElementsAlreadyOnTheDiagrams);
														  
			// Okay add them up.
			if (newPresentationElements != null)
				allDiscovered.addThese(newPresentationElements);

			if (nestedLinks != null)
				allDiscovered.addThese(nestedLinks);
			if (commentLinks != null)
				allDiscovered.addThese(commentLinks);
			if (associationClassLinks != null)
				allDiscovered.addThese(associationClassLinks);
			
			return allDiscovered;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
		
	}

   public ETList < IPresentationElement > discoverCommonRelations(boolean bAutoRouteEdges, ETList < IElement > pDiscoverOnTheseElements) 
   {
      ETList<IPresentationElement> retVal = super.discoverCommonRelations(bAutoRouteEdges,pDiscoverOnTheseElements);
      
      if(isStaticDiagram())
      {
         ETList<IPresentationElement> createdElements = discoverNestedLinks(pDiscoverOnTheseElements);
         
         if(createdElements != null && retVal != null)
            retVal.addThese(createdElements);
      }
      else
      {
         ETList<IPresentationElement> createdElements = discoverMessages(null);
         if(createdElements != null && retVal != null)
            retVal.addThese(createdElements);
      }
      
      {
         ETList<IPresentationElement> createdElements = discoverCommentLinks(pDiscoverOnTheseElements);
         if(createdElements != null && retVal != null)
            retVal.addThese(createdElements);
      }   
      {
         ETList<IPresentationElement> createdElements = discoverPartFacadeLinks(pDiscoverOnTheseElements);
         if(createdElements != null && retVal != null)
            retVal.addThese(createdElements);
      }
      
      {
         ETList<IPresentationElement> createdElements = discoverAssociationClassLinks(pDiscoverOnTheseElements);
         if(createdElements != null && retVal != null)
            retVal.addThese(createdElements);
      }
      
      return retVal;
   }
}
