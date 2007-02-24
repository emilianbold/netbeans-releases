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


package org.netbeans.modules.uml.ui.products.ad.drawEngineManagers;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.common.generics.IteratorT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.structure.IAssociationClass;
import org.netbeans.modules.uml.core.support.umlmessagingcore.UMLMessagingHelper;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.drawingarea.DrawEnginesToResetAction;
import org.netbeans.modules.uml.ui.controls.drawingarea.IDrawEnginesToResetAction;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETNode;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IEventManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.PointConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.graph.TSEdge;
import com.tomsawyer.graph.TSNode;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSPoint;
import com.tomsawyer.drawing.geometry.TSPoint;

import java.util.Iterator;

/**
 * @author KevinM
 *
 */
public class AssociationClassEventManager extends ADClassifierEventManager implements IAssociationClassEventManager {

	protected INodePresentation m_AssociationClassNodePE = null;
	protected static final int nSmallNodeSize = 10;


	public AssociationClassEventManager()
	{
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawEngineManagers.IAssociationClassEventManager#reconnectBridges(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
	 */
	public boolean reconnectBridges(IPresentationElement pClass1, IPresentationElement pClass2) {

		boolean bReconnectedOK = false;
		try {
			// Get the diagram
			IDrawingAreaControl pControl = getDrawingArea();

			// First remove all the bridges
			removeAllBridges(false);

			TSENode pClass1Node = TypeConversions.getOwnerNode(pClass1);
			TSENode pClass2Node = TypeConversions.getOwnerNode(pClass2);

			if (pClass1Node != null && pClass2Node != null) {
				TSConstPoint class1Point = pClass1Node.getCenter();
				TSConstPoint class2Point = pClass2Node.getCenter();
				TSPoint smallNodePoint =
					new TSPoint(Math.min(class1Point.getX(), class2Point.getX()) + Math.abs(class1Point.getX() - class2Point.getX()) / 2, Math.min(class1Point.getY(), class2Point.getY()) + Math.abs(class1Point.getY() - class2Point.getY()) / 2);

				// Now we've got the points.  Create the edges.
				TSNode pSmallNode = null;
				TSEdge pSourceEdge = null;
				TSEdge pTargetEdge = null;
				TSEdge pDottedLineEdge = null;
				TSENode pAssociationClassifier = TypeConversions.getOwnerNode(this.getParentETGraphObject());

				IElement pElement = TypeConversions.getElement(getParentETGraphObject());

				TSNode pEdgeSourceNode = pClass1Node instanceof TSNode ? (TSNode) pClass1Node : null;
				TSNode pEdgeTargetNode = pClass2Node instanceof TSNode ? (TSNode) pClass2Node : null;

				// Create the small node
				IETPoint pETLocation = PointConversions.newETPoint(smallNodePoint);
				pSmallNode = pControl.addNode("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI AssociationClassConnectorDrawEngine", pETLocation, false, false, pElement);
				if (pSmallNode != null) {
					TSENode pTSENode = (TSENode) pSmallNode;
					pTSENode.setWidth((double) nSmallNodeSize);
					pTSENode.setHeight((double) nSmallNodeSize);

					pSourceEdge = pControl.addEdge("org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge AssociationEnd", pEdgeSourceNode, pSmallNode, false, false, pElement);

					if (pSourceEdge != null) {
						// Create the target edge
						pTargetEdge = pControl.addEdge("org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge AssociationEnd", pSmallNode, pEdgeTargetNode, false, false, pElement);

						if (pTargetEdge != null && pAssociationClassifier != null) {
							TSNode pComAssociationClassifier = pAssociationClassifier instanceof TSNode ? (TSNode) pAssociationClassifier : null;

							//assert (pComAssociationClassifier);
							if (pComAssociationClassifier != null) {
								// Create the dotted line
								pDottedLineEdge = pControl.addEdge("org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Association", pSmallNode, pComAssociationClassifier, false, false, pElement);
							}
						}
					}
				}

				boolean bFailure = false;

				// assert (pSmallNode &pAssociationClassifier &pSourceEdge &pTargetEdge &pDottedLineEdge);
				if (pSmallNode == null || pAssociationClassifier == null || pSourceEdge == null || pTargetEdge == null || pDottedLineEdge == null) {
					// We should remove all the elements
					bFailure = true;
				}
			}

			// Now verify the connections
			boolean bBridgesValid = true;
			boolean bHasBridges = false;

			ETPairT < Boolean, Boolean > valid = validate();

			bReconnectedOK = valid != null ? valid.getParamTwo().booleanValue() : false; // Ok if no bridges
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bReconnectedOK;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawEngineManagers.IAssociationClassEventManager#discoverBridges()
	 */
	public boolean discoverBridges(ETList < IElement > pDiscoverOnTheseElements) {
		boolean bDiscovered = false;

		try {
			ETPairT < Boolean, Boolean > hasValidRetCode = validate();
			boolean bValid = hasValidRetCode != null ? hasValidRetCode.getParamOne().booleanValue() : false;
			boolean bHasBridges = hasValidRetCode != null ? hasValidRetCode.getParamTwo().booleanValue() : false;
			if (!bValid || !bHasBridges) {
				// Create some new bridges.

				IDrawingAreaControl pControl = getDrawingArea();

				// Get the diagram and the element

				IElement pElement = TypeConversions.getElement(this.getParentETGraphObject());

				IAssociationClass pAssocClass = pElement instanceof IAssociationClass ? (IAssociationClass) pElement : null;
				IAssociation pAssocClassAsAssociation = pElement instanceof IAssociation ? (IAssociation) pElement : null;
				if (pAssocClass != null && pControl != null && pAssocClassAsAssociation != null) {
					// Find the presentation elements at either ends pParticipants;

					ETList < IElement > pParticipants = pAssocClassAsAssociation.getAllParticipants();
					long count = pParticipants != null ? pParticipants.getCount() : 0;

					if (count == 2) {
						// Get the presentation elements on this diagram for the participants and
						// create the bridge nodes
						ETList < IPresentationElement > pElementPEs1 = null;
						ETList < IPresentationElement > pElementPEs2 = null;

						IElement pElement1 = pParticipants.item(0);
						IElement pElement2 = pParticipants.item(1);

						if (pElement1 != null && pElement2 != null) {
							// Make sure these items are in the list of items to be discovered
							boolean bIsInList1 = true;
							boolean bIsInList2 = true;
							if (pDiscoverOnTheseElements != null) {
								bIsInList1 = pDiscoverOnTheseElements.isInList(pElement1);
								bIsInList2 = pDiscoverOnTheseElements.isInList(pElement2);
							}

							if (bIsInList1 && bIsInList2) {
								// Remove all bridges and reconnect
								removeAllBridges(false);

								pElementPEs1 = pControl.getAllItems2(pElement1);
								pElementPEs2 = pControl.getAllItems2(pElement2);

								long numPEs1 = 0;
								long numPEs2 = 0;
								if (pElementPEs1 != null && pElementPEs1 != null) {
									numPEs1 = pElementPEs1.getCount();
									numPEs2 = pElementPEs2.getCount();
								}

								if (numPEs1 == 1 && numPEs2 == 1) {
									IPresentationElement pElementPE1 = pElementPEs1.item(0);
									IPresentationElement pElementPE2 = pElementPEs2.item(0);

									if (pElementPE1 != null && pElementPE2 != null) {
										bDiscovered = reconnectBridges(pElementPE1, pElementPE2);
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bDiscovered;
	}

	/**
	 * Verifies that the bridges are the correct type.
	 *
	 * @param bBridgesValid true if the bridges are valid
	 * @param bHasBridges true if there are bridges attached
	 */
	public ETPairT < Boolean, Boolean > validate() {

		boolean bBridgesValid = false;
		boolean bHasBridges = false;
		try {

			IBridgeElements bridgeElements = getBridgeElements();

			IETGraphObject pSourceEdge = bridgeElements != null ? bridgeElements.getSourceEdge() : null;
			IETGraphObject pSmallNode = bridgeElements != null ? bridgeElements.getSmallNode() : null;
			IETGraphObject pTargetEdge = bridgeElements != null ? bridgeElements.getTargetEdge() : null;
			IETGraphObject pDottedEdge = bridgeElements != null ? bridgeElements.getDottedEdge() : null;
			IETGraphObject pSourceNode = bridgeElements != null ? bridgeElements.getSourceNode() : null;
			IETGraphObject pTargetNode = bridgeElements != null ? bridgeElements.getTargetNode() : null;

			if (pSourceEdge == null && pSmallNode == null && pTargetEdge == null && pDottedEdge == null && pSourceNode == null && pTargetNode == null) {
				// An association class can live on its own
				bBridgesValid = true;
				bHasBridges = false;
			} else {
				if (pSourceEdge != null && pSmallNode != null && pTargetEdge != null && pDottedEdge != null && pSourceNode != null && pTargetNode != null) {

					// The source and target must be IClass's
					IElement pSourceNodeElement = TypeConversions.getElement(pSourceNode);
					String sSourceNodeElementType = pSourceNodeElement != null ? pSourceNodeElement.getElementType() : null;

					IElement pTargetNodeElement = TypeConversions.getElement(pTargetNode);
					String sTargetNodeElementType = pTargetNodeElement != null ? pTargetNodeElement.getElementType() : null;

					// Get the kind of the small node and make sure it's got the correct draw engine
					IDrawEngine pDrawEngine = TypeConversions.getDrawEngine(pSmallNode);
					String sDrawEngineID = pDrawEngine != null ? pDrawEngine.getDrawEngineID() : null;

					// Now do the verification, I believe that associations are valid between
					// classifiers so we got the metatype only for debug
					IClassifier pSourceClassifier = pSourceNodeElement instanceof IClassifier ? (IClassifier) pSourceNodeElement : null;
					IClassifier pTargetClassifier = pTargetNodeElement instanceof IClassifier ? (IClassifier) pTargetNodeElement : null;

					if (pSourceClassifier != null && pTargetClassifier != null && sDrawEngineID != null && sDrawEngineID.equals("AssociationClassConnectorDrawEngine")) {
						bBridgesValid = true;
						bHasBridges = true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ETPairT < Boolean, Boolean > (new Boolean(bBridgesValid), new Boolean(bHasBridges));
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawEngineManagers.IAssociationClassEventManager#createBridges(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge)
	 */
	public void createBridges(IETEdge pInitialEdge) {
		if (pInitialEdge == null)
			return; 

		boolean hr = true;
		try {
			TSEEdge pTSEEdge = TypeConversions.getOwnerEdge(pInitialEdge);
			TSENode pSourceNode = null;
			TSENode pTargetNode = null;
			TSConstPoint sourcePoint = null;
			TSConstPoint targetPoint = null;
			IETPoint midPoint = new ETPoint();

			// Get the diagram
			IDrawingAreaControl pControl = getDrawingArea();

			if (pTSEEdge != null && pControl != null) {
				pSourceNode = (TSENode)pTSEEdge.getSourceNode();
				pTargetNode = (TSENode)pTSEEdge.getTargetNode();

				// Get the from and to points
				sourcePoint = pTSEEdge.getSourcePoint();
				targetPoint = pTSEEdge.getTargetPoint();

				midPoint = pControl.getMidPoint(pTSEEdge);

				//CPointConversions.ETPointToPOINT(pETPoint, midPoint);
			}

			// Create our bridges
			//assert(pTSEEdge & pControl);
			if (pTSEEdge != null && pControl != null && pTargetNode != null && pSourceNode != null) {
				TSPoint smallNodePoint;
				TSPoint associationClassPoint;

				smallNodePoint = PointConversions.ETPointToTSPoint(midPoint);

				if (Math.abs(sourcePoint.getX() - targetPoint.getX()) > Math.abs(sourcePoint.getY() - targetPoint.getY())) {
					// horizontal alignment
					associationClassPoint = smallNodePoint;
					associationClassPoint.setY(associationClassPoint.getY() - 50);
				} else {
					// This is a virtical alignment
					associationClassPoint = smallNodePoint;
					associationClassPoint.setX(associationClassPoint.getX() - 50);
				}

				// Now we've got the points.  Create the edges.
				TSNode pSmallNode = null;
				TSEdge pSourceEdge = null;
				TSEdge pTargetEdge = null;
				TSEdge pDottedLineEdge = null;

				TSENode pAssociationClassifier = TypeConversions.getOwnerNode(getParentETGraphObject());

				IElement pElement = TypeConversions.getElement(getParentETGraphObject());

				// Create the small node
				IETPoint pETLocation = PointConversions.newETPoint(smallNodePoint);

				pSmallNode = pControl.addNode("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI AssociationClassConnectorDrawEngine", pETLocation, false, false, pElement);

				if (pSmallNode != null) {
					ETNode pTSENode = (ETNode) pSmallNode;
					pTSENode.setWidth((double)nSmallNodeSize);
					pTSENode.setHeight((double)nSmallNodeSize);

					// Create the source edge
					TSNode pEdgeSourceNode = pSourceNode;
					TSNode pEdgeTargetNode = pTargetNode;

					pSourceEdge = pControl.addEdge("org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge AssociationEnd", pEdgeSourceNode, pSmallNode, false, false, pElement);

					if (pSourceEdge != null) {
						// Create the target edge
						pTargetEdge = pControl.addEdge("org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge AssociationEnd", pSmallNode, pEdgeTargetNode, false, false, pElement);

						if (pTargetEdge != null) {
							TSNode pComAssociationClassifier = (TSNode) pAssociationClassifier;

							// assert (pComAssociationClassifier);
							if (pComAssociationClassifier != null) {
								// Create the dotted line
								pDottedLineEdge = pControl.addEdge("org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge AssociationEnd", pSmallNode, pComAssociationClassifier, false, false, pElement);
							}
						}
					}
				}

				boolean bFailure = false;

				//assert (pSmallNode &pAssociationClassifier &pSourceEdge &pTargetEdge &pDottedLineEdge);
				if (pSmallNode == null || pAssociationClassifier == null || pSourceEdge == null || pTargetEdge == null || pDottedLineEdge == null) {
					// We should remove all the elements
					bFailure = true;
				}

				// If we have a failure remove all the elements
				if (bFailure) {
					removeAllBridges(false);
					//UMLMessagingHelper messageHelper = new UMLMessagingHelper(IDS_MESSAGINGFACILITY);

					//UMLMessagingHelper.sendErrorMessage(IDS_FAILEDTOCREATEASSOCIATIONCLASS);
				}

				// Relayout special if we've got a recursive association link
				if (pSourceNode == pTargetNode) {
					relayoutRecursiveAssociationLink(pInitialEdge, (IETNode) pSourceNode, (IETNode) pTargetNode);
				}

				// Remove the initial edge
				pControl.postDeletePresentationElement(pInitialEdge.getGraphObject());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Relayout special if we've got a recursive association link
	*/
	protected void relayoutRecursiveAssociationLink(IETEdge pInitialEdge, IETNode sourceNode, IETNode targetNode) {
		/*
			// First get the bends to preserve the bends      
			TSPointDList pointList;
			TSPathNodeIter pathIter(TSEEdge.convertToObject(pInitialEdge));
			double nMaxDistanceFromNode = 0;
			TSPoint nBendWithMaxDistanceFromNode;
			long nIndexOfBendWithMaxDistanceFromNode = 0;
			long nIndex = 0;
			final long nMovementSize = 120;
		
			TSPoint nodeCenterPoint;
			TSRect nodeBoundingRect;
			pSourceNode.getCenter(nodeCenterPoint);
			pSourceNode.getBoundingRect(nodeBoundingRect);
		
			while (pathIter) {
				TSPoint point = new TSPoint;
				pathIter.pNode().getCenter(* pPoint);
		
				pointList.prependPoint(pPoint);
		
				if (nIndex == 0) {
					nBendWithMaxDistanceFromNode = * pPoint;
					nIndexOfBendWithMaxDistanceFromNode = nIndex;
					nMaxDistanceFromNode = nodeCenterPoint.distance(* pPoint);
				} else {
					double nCurrentDistance = nodeCenterPoint.distance(* pPoint);
					if (nCurrentDistance > nMaxDistanceFromNode) {
						nBendWithMaxDistanceFromNode = * pPoint;
						nIndexOfBendWithMaxDistanceFromNode = nIndex;
						nMaxDistanceFromNode = nodeCenterPoint.distance(* pPoint);
					}
				}
		
				pathIter++;
				nIndex++;
			}
		
			// Shouldn't get here unless the nIndex >= 2
			if (nIndex >= 2) {
				//
				// Move the small node to the max distance location.
				//
				pSmallNode.moveTo(nBendWithMaxDistanceFromNode.x(), nBendWithMaxDistanceFromNode.y());
		
				//
				// Move the association class
				//
				TSSide side = nodeBoundingRect.closestSide(nBendWithMaxDistanceFromNode);
		
				TSPoint nAssociationClassPoint(nBendWithMaxDistanceFromNode);
				if (side == TS_SIDE_TOP) {
					nAssociationClassPoint.y(nAssociationClassPoint.y() + nMovementSize);
				} else if (side == TS_SIDE_BOTTOM) {
					nAssociationClassPoint.y(nAssociationClassPoint.y() - nMovementSize);
				} else if (side == TS_SIDE_LEFT) {
					nAssociationClassPoint.x(nAssociationClassPoint.x() - nMovementSize);
				} else if (side == TS_SIDE_RIGHT) {
					nAssociationClassPoint.x(nAssociationClassPoint.x() + nMovementSize);
				}
				pAssociationClassifier.moveTo(nAssociationClassPoint.x(), nAssociationClassPoint.y());
		
				pointList.deleteAllCellsAndPoints();
			}
			*/
	}

	/**
		Verifies that the edges have the correct elements, used to upgrade from old association 
		class code
		*/
	public boolean verifyEdgeParents() {
		boolean hr = true;
		try {
			if (isDrawEngine("ClassDrawEngine")) {
				// Get the edge that's a RelationEdgeDrawEngine, that's the dashed line
				IPresentationElement pThisPE = TypeConversions.getPresentationElement(this.getParentETGraphObject());
				INodePresentation pNodePE = pThisPE instanceof INodePresentation ? (INodePresentation) pThisPE : null;
				if (pNodePE != null) {
					IPresentationElement pDashedLineElement = null;
					IPresentationElement pSmallNodeElement = null;
					IPresentationElement pTargetSolidLineElement = null;
					IPresentationElement pSourceSolidLineElement = null;
					long count = 0;

					ETList < IPresentationElement > pEdgesWithDrawEngine = pNodePE.getEdgesWithDrawEngine("BridgeEdgeDrawEngine", true, true);
					if (pEdgesWithDrawEngine != null) {
						count = pEdgesWithDrawEngine.getCount();

						if (count > 0) {
							// Get the dashed line
							pDashedLineElement = pEdgesWithDrawEngine.item(0);
						}
					}

					if (pDashedLineElement != null) {
						// Find the attached small node
						IEdgePresentation pEdgePE = pDashedLineElement instanceof IEdgePresentation ? (IEdgePresentation) pDashedLineElement : null;
						if (pEdgePE != null) {
							ETPairT < IDrawEngine, IDrawEngine > retCode = pEdgePE.getEdgeFromAndToDrawEnginesWithID("BridgeNodeDrawEngine");
							IDrawEngine pFromDrawEngine = retCode != null ? retCode.getParamOne() : null;
							IDrawEngine pToDrawEngine = retCode != null ? retCode.getParamTwo() : null;

							// Get the draw engine that's of type GenericNodeDrawEngine
							if (pFromDrawEngine != null) {
								pSmallNodeElement = TypeConversions.getPresentationElement(pFromDrawEngine);
							}

							if (pToDrawEngine != null) {
								pSmallNodeElement = TypeConversions.getPresentationElement(pToDrawEngine);
							}
						}
					}

					// Now grab the elements that represent the two non-dashed lines
					if (pSmallNodeElement != null) {
						INodePresentation pSmallNodePE = pSmallNodeElement instanceof INodePresentation ? (INodePresentation) pSmallNodeElement : null;
						if (pSmallNodePE != null) {
							count = 0;

							pEdgesWithDrawEngine = pSmallNodePE.getEdgesWithDrawEngine("BridgeEdgeDrawEngine", true, true);

							if (pEdgesWithDrawEngine != null) {
								count = pEdgesWithDrawEngine.getCount();
							}

							for (int i = 0; i < count; i++) {
								IPresentationElement pThisPresentationElement = pEdgesWithDrawEngine.item(i);
								if (pThisPresentationElement != null && pThisPresentationElement != pDashedLineElement) {
									if (pSourceSolidLineElement == null) {
										pSourceSolidLineElement = pThisPresentationElement;
									} else if (pTargetSolidLineElement == null) {
										pTargetSolidLineElement = pThisPresentationElement;
									}
								}
							}
						}
					}

					// At this point we should have all the ETElements.  Now we need to upgrade.
					if (pDashedLineElement != null && pSmallNodeElement != null && pTargetSolidLineElement != null && pSourceSolidLineElement != null) {
						IDrawingAreaControl pControl = getDrawingArea();

						// Get the diagram
						if (pControl != null) {
							IDrawEnginesToResetAction pResetAction;

							///
							// Reset the dashed line
							///
							pResetAction = new DrawEnginesToResetAction();

							if (pResetAction != null) {
								pResetAction.init4(pDashedLineElement, "AssociationEnd");
								pControl.postDelayedAction(pResetAction);
							}

							///
							// Reset the small node
							///
							pResetAction = new DrawEnginesToResetAction();

							if (pResetAction != null) {
								pResetAction.init4(pSmallNodeElement, "AssociationClassConnectorDrawEngine");
								pControl.postDelayedAction(pResetAction);
							}

							///
							// Reset the source edge
							///

							pResetAction = new DrawEnginesToResetAction();

							if (pResetAction != null) {
								pResetAction.init4(pTargetSolidLineElement, "AssociationEnd");
								pControl.postDelayedAction(pResetAction);
							}

							///
							// Reset the target edge
							///
							pResetAction = new DrawEnginesToResetAction();

							if (pResetAction != null) {
								pResetAction.init4(pSourceSolidLineElement, "AssociationEnd");
								pControl.postDelayedAction(pResetAction);
							}

							pControl.pumpMessages(false);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hr;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawEngineManagers.IAssociationClassEventManager#getBridgeElements()
	 */
	public IBridgeElements getBridgeElements() {
		return new AssociationClassEventManagerElements(this);
	}

	/**
	 *
	 Notifies the node/edge that it is about to be deleted.
	 *
	 *
	 @return 
	 *
	 */
	protected void onPreDelete() {
		selectAllBridges(false);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IGraphObjectManager#onGraphEvent(int)
	 */
	public void onGraphEvent(int nKind) {
		try {
			switch (nKind) {
				case IGraphEventKind.GEK_PRE_DELETEGATHERSELECTED :
					{
						if (isDrawEngine("ClassDrawEngine")) {
							onPreDelete();
						} else {
							INodePresentation pNodePE = getAssociationClassPE();

							if (pNodePE != null) {
								boolean bSelected = pNodePE.getSelected();

								if (!bSelected) {
									IDrawEngine pDrawEngine = TypeConversions.getDrawEngine(pNodePE);

									if (pDrawEngine != null) {
										IEventManager pEventManager = pDrawEngine.getEventManager();
										if (pEventManager != null) {
											// Forward this event to the association class
											pEventManager.onGraphEvent(nKind);
										}
									}
								}
							}
						}

						// Clear out our cached presentation element that points to the association
						// class.
						m_AssociationClassNodePE = null;
					}
					break;
			}

			// Call the base class
			super.onGraphEvent(nKind);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IEventManager#setSensitivityAndCheck(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem, int)
	 */
	public boolean setSensitivityAndCheck(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem, int buttonKind) {
		// TODO Auto-generated method stub
		return super.setSensitivityAndCheck(pContextMenu, pMenuItem, buttonKind);
	}
	
	/*
	 * Returns a list of Presentation elements involved in the Bridge.
	 */
   protected ETList < IPresentationElement > getBridgePresentations( boolean bIncludeSelf )
   {
      ETList < IPresentationElement > pPES = new ETArrayList < IPresentationElement > ();

      IBridgeElements bridgeElements = getBridgeElements();
      if (bridgeElements != null)
      {
         IPresentationElement pe;
         
         if (bIncludeSelf)
         {
            pe = TypeConversions.getPresentationElement(getParentETGraphObject());
            if (pe != null)
            {
               pPES.add(pe);
            }
         }

         pe = TypeConversions.getPresentationElement(bridgeElements.getSmallNode());
         if (pe != null)
         {
            pPES.add(pe);
         }

         pe = TypeConversions.getPresentationElement(bridgeElements.getSourceEdge());
         if (pe != null)
         {
            pPES.add(pe);
         }

         pe = TypeConversions.getPresentationElement(bridgeElements.getTargetEdge());
         if (pe != null)
         {
            pPES.add(pe);
         }

         pe = TypeConversions.getPresentationElement(bridgeElements.getDottedEdge());
         if (pe != null)
         {
            pPES.add(pe);
         }
      }
      
      return pPES;
   }

	/*
	 * Selects all the presentation elements in the Bridge.
	 */
   public void selectAllBridges(boolean bSelectSelf)
   {
      try
      {
         // Get the diagram
         ETList < IPresentationElement > pPES = getBridgePresentations( bSelectSelf );
         IteratorT < IProductGraphPresentation > iter = new IteratorT < IProductGraphPresentation > (pPES);
         // Make sure these are all select so they get deleted as well
         while (iter.hasNext())
         {
            iter.next().setSelected(true);
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

	/**
	 Deletes all the bridges
	 */
   protected boolean removeAllBridges(boolean bRemoveSelf)
   {
      boolean hr = true;
      try
      {
         // Get the diagram
         IDrawingAreaControl pControl = getDrawingArea();

         if (pControl != null)
         {
            ETList < IPresentationElement > pPES = getBridgePresentations( bRemoveSelf );

            Iterator < IPresentationElement > iter = pPES.iterator();
            while (iter.hasNext())
            {
               pControl.postDeletePresentationElement(iter.next());
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return hr;
   }

   /**
    Returns the assocation class node presentation
    */
   public INodePresentation getAssociationClassPE()
   {
      INodePresentation pAssociationClassNodePE = null;

      try
      {
         if (m_AssociationClassNodePE != null)
         {
            return m_AssociationClassNodePE;
         }

         IPresentationElement pThisPE = TypeConversions.getPresentationElement(getParentETGraphObject());

         if (isDrawEngine("ClassDrawEngine"))
         {
            // We're associated with the actual association class - the one showing
            // operations and attributes.
            m_AssociationClassNodePE = (INodePresentation)pThisPE;
         }
         else if (isDrawEngine("AssociationEdgeDrawEngine"))
         {
            // We're one of the edges, navigate to to the small, round connector and then to
            // the association class.
            IEdgePresentation pEdgePE = pThisPE instanceof IEdgePresentation ? (IEdgePresentation)pThisPE : null;
            if (pEdgePE != null)
            {
               ETPairT < INodePresentation, INodePresentation > retCode = pEdgePE.getEdgeFromAndToPresentationElement();

               INodePresentation pFromNode = retCode != null ? retCode.getParamOne() : null;
               INodePresentation pToNode = retCode != null ? retCode.getParamTwo() : null;

               IDrawEngine pFromDrawEngine = TypeConversions.getDrawEngine(pFromNode);
               IDrawEngine pToDrawEngine = TypeConversions.getDrawEngine(pToNode);
               String sFromDrawEngineID = pFromDrawEngine != null ? pFromDrawEngine.getDrawEngineID() : null;
               String sToDrawEngineID = pToDrawEngine != null ? pToDrawEngine.getDrawEngineID() : null;

               //					if (sFromDrawEngineID != null && sFromDrawEngineID.equals("AssociationClassConnectorDrawEngine") && sToDrawEngineID != null sToDrawEngineID.equals("AssociationClassConnectorDrawEngine")) {
               //						assert("Something is very wrong!");
               //					}

               INodePresentation pSmallConnectorPE;
               if (sFromDrawEngineID != null && sFromDrawEngineID.equals("AssociationClassConnectorDrawEngine"))
               {
                  pSmallConnectorPE = TypeConversions.getNodePresentation(pFromDrawEngine);
               }
               else if (sToDrawEngineID != null && sToDrawEngineID.equals("AssociationClassConnectorDrawEngine"))
               {
                  pSmallConnectorPE = TypeConversions.getNodePresentation(pToDrawEngine);
               }
               else
               {
                  pSmallConnectorPE = null;
               }

               if (pSmallConnectorPE != null)
               {
                  m_AssociationClassNodePE = navigateFromConnectorToAssociationClass(pSmallConnectorPE);
               }
            }
	 }
	 else if (isDrawEngine("AssociationClassConnectorDrawEngine"))
         {
               // We're the little round node
               INodePresentation pSmallConnectorPE = pThisPE instanceof INodePresentation ? (INodePresentation)pThisPE : null;
               if (pSmallConnectorPE != null)
               {
                  m_AssociationClassNodePE = navigateFromConnectorToAssociationClass(pSmallConnectorPE);
               }
         }

         if (m_AssociationClassNodePE != null)
         {
            pAssociationClassNodePE = m_AssociationClassNodePE;
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return pAssociationClassNodePE;
   }

	/**
	 Is the draw engine of the type indicated
	 */
	protected boolean isDrawEngine(final String sDrawEngineID) {
		boolean bIsDrawEngine = false;

		boolean hr = true;
		try {
			IDrawEngine pDrawEngine = TypeConversions.getDrawEngine(this.getParentETGraphObject());

			if (pDrawEngine != null) {
				String sThisDrawEngineID = pDrawEngine.getDrawEngineID();

				if (sThisDrawEngineID != null && sThisDrawEngineID.equals(sDrawEngineID)) {
					bIsDrawEngine = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bIsDrawEngine;
	}

	/**
	 *
	 */
	protected INodePresentation navigateFromConnectorToAssociationClass(INodePresentation smallConnectorPE) {
		INodePresentation pAssociationClassNodePE = null;

		try {
			String sFromDrawEngineID = null;
			String sToDrawEngineID = null;
			// Get the edges and find the association class at the other end       
			ETList < IETGraphObject > pElements = smallConnectorPE != null ? smallConnectorPE.getEdges(true, true) : null;
			long count = pElements != null ? pElements.getCount() : 0;

			for (int i = 0; i < count; i++) {
				IETGraphObject pElement = pElements.item(i);
				if (pElement != null) {
					IEdgePresentation pEdgePE = TypeConversions.getEdgePresentation(pElement);
					IElement pEdgeElement = TypeConversions.getElement(pElement);

					IAssociationClass pAssocClass = pEdgeElement instanceof IAssociationClass ? (IAssociationClass) pEdgeElement : null;
					if (pEdgePE != null && pAssocClass != null) {

						IElement pFromDrawEngineElement;
						IElement pToDrawEngineElement;

						sFromDrawEngineID = "";
						sToDrawEngineID = "";

						// Must be a draw engine of type ClassDrawEngine and be of the same element
						// that the small connector is
						ETPairT < IDrawEngine, IDrawEngine > retCode = pEdgePE.getEdgeFromAndToDrawEnginesWithID("ClassDrawEngine");

						IDrawEngine pFromDrawEngine = retCode != null ? retCode.getParamOne() : null;
						IDrawEngine pToDrawEngine = retCode != null ? retCode.getParamTwo() : null;

						if (pFromDrawEngine != null) {
							pFromDrawEngineElement = TypeConversions.getElement(pFromDrawEngine);
							if (pFromDrawEngineElement == pEdgeElement) {
								pAssociationClassNodePE = TypeConversions.getNodePresentation(pFromDrawEngine);
							}
						}

						if (pToDrawEngine != null) {
							pToDrawEngineElement = TypeConversions.getElement(pToDrawEngine);
							if (pToDrawEngineElement == pEdgeElement) {
								pAssociationClassNodePE = TypeConversions.getNodePresentation(pToDrawEngine);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pAssociationClassNodePE;
	}
}
