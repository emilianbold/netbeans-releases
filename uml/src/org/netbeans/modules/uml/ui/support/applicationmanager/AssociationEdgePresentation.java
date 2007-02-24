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


package org.netbeans.modules.uml.ui.support.applicationmanager;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.common.generics.ETTripleT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationReference;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.structure.IAssociationClass;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.drawingarea.AutoRoutingAction;
import org.netbeans.modules.uml.ui.controls.drawingarea.IAutoRoutingAction;
import org.netbeans.modules.uml.ui.support.PresentationReferenceHelper;
import org.netbeans.modules.uml.ui.products.ad.drawengines.IAssociationEdgeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETNode;
import org.netbeans.modules.uml.ui.support.IAutoRoutingActionKind;
import org.netbeans.modules.uml.ui.support.NodeEndKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.PointConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.graph.TSNode;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
import com.tomsawyer.util.TSObject;

/*
 * 
 * @author KevinM
 *
 */
public class AssociationEdgePresentation extends EdgePresentation implements IAssociationEdgePresentation {

	/**
	 * 
	 */
	public AssociationEdgePresentation() {
		super();
	}

	/**
	 * Returns the qualifier, if there is one, at this end.
	 *
	 * @param pNode [in] The node that may or may not be a qualifier draw engine.
	 * @param pQualifierPE [out,retval] The presentation element representing the qualifier.  This
	 * could be null.
	 */
	public IPresentationElement getQualifier(ETNode pNode) {
		ETPairT < IPresentationElement, IPresentationElement > qualifiers = getQualifier2(pNode);
		return qualifiers != null ? qualifiers.getParamOne() : null;
	}

	/**
	 Returns the qualifier, if there is one, at this end.
	 *
	 @param pNode [in] The node that may or may not be a qualifier draw engine.
	 @param pQualifierPE [out] The presentation element representing the qualifier.  This
	 could be null.
	 @param pNodeAttachedToQualifierPE [in] The node the qualifier is attached to.  This could
	 be null.
	 */
	public ETPairT < IPresentationElement, IPresentationElement > getQualifier2(ETNode pNode) {
		if (pNode == null)
			return null;

		ETPairT < IPresentationElement, IPresentationElement > hr = new ETPairT < IPresentationElement, IPresentationElement > ();
		try {
			IPresentationElement pQualifierPE = null;
			IPresentationElement pNodeAttachedToQualifierPE = null;

			IDrawEngine pDrawEngine = TypeConversions.getDrawEngine((TSNode) pNode);

			if (pDrawEngine != null) {
				String sID = pDrawEngine.getDrawEngineID();

				if (sID.equals("QualifierDrawEngine")) {
					pQualifierPE = TypeConversions.getPresentationElement(pDrawEngine);
					hr.setParamOne(pQualifierPE);

					if (pQualifierPE != null && pNodeAttachedToQualifierPE != null) {
						pNodeAttachedToQualifierPE = navigatePastQualifier(pQualifierPE);
						hr.setParamTwo(pNodeAttachedToQualifierPE);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hr;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IAssociationEdgePresentation#getSourceQualifier()
	 */
	public IPresentationElement getSourceQualifier() {
		return getQualifier(getSourceNode());
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IAssociationEdgePresentation#getTargetQualifier()
	 */
	public IPresentationElement getTargetQualifier() {
		return getQualifier(getTargetNode());
	}

	/**
	 Returns the association ends
	 *
	 @param pEnd1 [out] The first end
	 @param pEnd2 [out] The second end.
	 */
	protected ETPairT < IAssociationEnd, IAssociationEnd > getEnds() {
		ETPairT < IAssociationEnd, IAssociationEnd > ends = null;
		try {
			IElement pEdgeElement = getModelElement();
			IAssociation pAssociation = pEdgeElement instanceof IAssociation ? (IAssociation) pEdgeElement : null;
			if (pAssociation != null) {
				ETList < IAssociationEnd > pEnds = pAssociation.getEnds();
				long numEnds = pAssociation.getNumEnds();

				if (numEnds == 2) {
					ends = new ETPairT < IAssociationEnd, IAssociationEnd > (pEnds.get(0), pEnds.get(1));
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ends;
	}

	/**
	 Creates a qualifier node at the source node
	 *
	 @param bDoSource [in] true if we're dealing with the source node
	 */
	protected boolean createQualifierNodeAtLocation(boolean bDoSource) {
		boolean hr = true;
		try {
			IPresentationElement pQualifierPE;
			IPresentationElement pNodeAttachedToQualifierPE;
			IElement pAssociationEndElement;
			IDrawingAreaControl pControl = getDrawingArea();

			IAssociationEnd pEnd1 = null;
			IAssociationEnd pEnd2 = null;
			IAssociationEnd pEndToCreateQualifierFor = null;
			TSConstPoint tsNewPoint = null;

			ETPairT < IAssociationEnd, IAssociationEnd > ends = getEnds();
			if (ends != null) {
				pEnd1 = ends.getParamOne();
				pEnd2 = ends.getParamTwo();
			}

			if (bDoSource) {
				tsNewPoint = getTSEdge().getSourcePoint();

				ETPairT < IPresentationElement, IPresentationElement > pPES = getQualifier2(getSourceNode());

				pQualifierPE = pPES != null ? pPES.getParamOne() : null;
				pNodeAttachedToQualifierPE = pPES != null ? pPES.getParamTwo() : null;

				pEndToCreateQualifierFor = pEnd1;
			} else {
				tsNewPoint = getTSEdge().getTargetPoint();
				ETPairT < IPresentationElement, IPresentationElement > pPES = getQualifier2(getTargetNode());

				pQualifierPE = pPES != null ? pPES.getParamOne() : null;
				pNodeAttachedToQualifierPE = pPES != null ? pPES.getParamTwo() : null;

				pEndToCreateQualifierFor = pEnd2;
			}

			if (pQualifierPE == null && pNodeAttachedToQualifierPE == null && pEndToCreateQualifierFor != null && pControl != null) {
				IETPoint pLocation = PointConversions.newETPoint(tsNewPoint);

				// Good.  We know we don't have a qualifier attached to us
				pControl.setModelElement(pEndToCreateQualifierFor);
				TSNode pCreatedNode = pControl.addNode("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Qualifier", pLocation, false, false);

				pControl.setModelElement(null);

				if (pCreatedNode != null) {
					// Need to now reparent this edge to the newnode and create a presentation
					// relation relationship between the class and the qualifier
					TSEEdge pThisEdge = this.getTSEdge();
					TSENode pCreatedTSENode = pCreatedNode instanceof TSENode ? (TSENode) pCreatedNode : null;

					if (pCreatedTSENode != null && pThisEdge != null) {
						ETNode pOldNode = null;

						if (bDoSource) {
							pOldNode = getSourceNode();
							pThisEdge.setSourceNode(pCreatedTSENode);
							pThisEdge.setTargetNode(getTargetNode());
							//pThisEdge.connect();
						} else {
							pOldNode = getTargetNode();
							pThisEdge.setSourceNode(getSourceNode());
							pThisEdge.setTargetNode(pCreatedTSENode);

							//pThisEdge.connect();
						}

						// Convert both the old and source node to presentation elements and
						// create the relationship

						IPresentationElement pOldPE = TypeConversions.getPresentationElement((TSObject)pOldNode);
						IPresentationElement pNewPE = TypeConversions.getPresentationElement((TSObject)pCreatedTSENode);

						// assert (pOldPE &pNewPE);
						if (pOldPE != null && pNewPE != null) {
							IPresentationReference pRef = PresentationReferenceHelper.createPresentationReference(pOldPE, pNewPE);

							// Tell the parent of the qualifier that it needs to relocate this presentation
							// reference
							IDrawEngine pDrawEngine = TypeConversions.getDrawEngine(pOldPE);

							INodeDrawEngine pOwnerDrawEngine = pDrawEngine instanceof INodeDrawEngine ? (INodeDrawEngine) pDrawEngine : null;

							// assert (pOwnerDrawEngine);
							if (pOwnerDrawEngine != null) {
								pOwnerDrawEngine.relocateQualifiers(true);
							}
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
	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IAssociationEdgePresentation#createQualifierNodeAtSourceLocation()
	 */
	public boolean createQualifierNodeAtSourceLocation() {
		return createQualifierNodeAtLocation(true);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IAssociationEdgePresentation#createQualifierNodeAtTargetLocation()
	 */
	public boolean createQualifierNodeAtTargetLocation() {
		return createQualifierNodeAtLocation(false);
	}

	/**
	 Deletes a qualifier node at the source node
	 *
	 @param bDoSource [in] true if we're dealing with the source node
	 */
	protected boolean removeQualifierNodeAtLocation(boolean bDoSource) {
		boolean hr = true;
		try {
			IPresentationElement pQualifierPE;
			IPresentationElement pNodeAttachedToQualifierPE;
			IDrawingAreaControl pControl = getDrawingArea();

			if (bDoSource) {
				ETPairT < IPresentationElement, IPresentationElement > pPES = getQualifier2(getSourceNode());

				pQualifierPE = pPES != null ? pPES.getParamOne() : null;
				pNodeAttachedToQualifierPE = pPES != null ? pPES.getParamTwo() : null;
			} else {
				ETPairT < IPresentationElement, IPresentationElement > pPES = getQualifier2(getTargetNode());

				pQualifierPE = pPES != null ? pPES.getParamOne() : null;
				pNodeAttachedToQualifierPE = pPES != null ? pPES.getParamTwo() : null;
			}

			if (pQualifierPE != null && pNodeAttachedToQualifierPE != null && pControl != null) {
				TSENode pNewTSENode = TypeConversions.getOwnerNode(pNodeAttachedToQualifierPE);

				// Need to now reparent this edge to the newnode and delete the qualifier
				TSEEdge pThisEdge = this.getTSEdge();
				if (pNewTSENode != null && pThisEdge != null) {
					if (bDoSource) {
						pThisEdge.setSourceNode(pNewTSENode);
						pThisEdge.setTargetNode(getTargetNode());

						//pThisEdge.connect();
					} else {
						pThisEdge.setSourceNode(getTargetNode());
						pThisEdge.setTargetNode(pNewTSENode);
						//pThisEdge.connect();
					}

					pControl.postDeletePresentationElement(pQualifierPE);

					// Autoroute the edge
					IPresentationElement pPE = this; // getPEInterface(&pPE);

					if (pPE != null) {
						IAutoRoutingAction pAutoRoutingAction = new AutoRoutingAction();
						//assert (pAutoRoutingAction);
						if (pAutoRoutingAction != null) {
							pAutoRoutingAction.setKind(IAutoRoutingActionKind.ARAK_AUTOMATIC);
							pAutoRoutingAction.add(pPE);
							pControl.postDelayedAction(pAutoRoutingAction);
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
	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IAssociationEdgePresentation#removeQualifierNodeAtSourceLocation()
	 */
	public boolean removeQualifierNodeAtSourceLocation() {
		return removeQualifierNodeAtLocation(true);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IAssociationEdgePresentation#removeQualifierNodeAtTargetLocation()
	 */
	public boolean removeQualifierNodeAtTargetLocation() {
		return removeQualifierNodeAtLocation(false);
	}

	/*
	 * Verifies that the qualifier is either a source or target of this edge.
	 */
	public boolean reconnectToQualifierNode(INodePresentation pQualifierNodePE) {
		if (pQualifierNodePE == null)
			return false;

		boolean bReconnected = false;
		try {
			IElement pQualifierFirstSubject = pQualifierNodePE.getFirstSubject();
			IDrawingAreaControl pControl = getDrawingArea();

			IAssociationEnd pQualifierAsAssocEnd = pQualifierFirstSubject instanceof IAssociationEnd ? (IAssociationEnd) pQualifierFirstSubject : null;
			if (pQualifierAsAssocEnd != null && pControl != null) {
				boolean bIsSourceEnd = false;
				boolean bIsTargetEnd = false;
				int nEndKind = getNodeEnd(pQualifierAsAssocEnd);

				if (nEndKind == NodeEndKindEnum.NEK_FROM || nEndKind == NodeEndKindEnum.NEK_BOTH) {
					bIsSourceEnd = true;
               //bIsTargetEnd = true;
				} else {
					bIsTargetEnd = true;
               //bIsSourceEnd = true;
				}

            // This is code that seemed to be needed by C++ but does is not
            // needed in Java.  I am keeping this code in here just in case.
            // 12-03-04 Trey Spiva
				if (bIsSourceEnd || bIsTargetEnd) {
					IPresentationElement pThisPE = this;
					IAssociationEdgePresentation pAssociationEdgePE = pThisPE instanceof IAssociationEdgePresentation ? (IAssociationEdgePresentation) pThisPE : null;

					//assert (pAssociationEdgePE);
					if (pAssociationEdgePE != null) {
						if (bIsSourceEnd) {
//							pAssociationEdgePE.createQualifierNodeAtSourceLocation();
//							pControl.postDeletePresentationElement(pQualifierNodePE);
							bReconnected = true;
						} else {
//							pAssociationEdgePE.createQualifierNodeAtTargetLocation();
//							pControl.postDeletePresentationElement(pQualifierNodePE);
							bReconnected = true;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bReconnected;
	}

	/*
	 * Validate that the qualifiers are correctly displayed.
	 */
	public boolean validateQualifiers() {
		boolean hr = true;
		try {
			IPresentationElement pSourceQualifier = getSourceQualifier();
			IPresentationElement pTargetQualifier = getTargetQualifier();
			boolean bSourceIsCorrect = true;
			boolean bTargetIsCorrect = true;

			if (pSourceQualifier != null) {
				bSourceIsCorrect = isSourceQualifierCorrect();
			}

			if (pTargetQualifier != null) {
				bTargetIsCorrect = isTargetQualifierCorrect();
			}

			if (bTargetIsCorrect == false || bSourceIsCorrect == false) {
				// Recreate the qualifiers if they are wrong.
				removeQualifierNodeAtSourceLocation();
				removeQualifierNodeAtTargetLocation();

				if (pSourceQualifier != null) {
					createQualifierNodeAtSourceLocation();
				}

				if (pTargetQualifier != null) {
					createQualifierNodeAtTargetLocation();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hr;
	}

	/**
	 Verifies that the source qualifier is correct
	 */
	public boolean isSourceQualifierCorrect() {
		boolean bIsCorrect = true;
		try {
			IPresentationElement pSourceQualifier = getSourceQualifier();
			if (pSourceQualifier != null) {
				IElement pQualifierFirstSubject = pSourceQualifier.getFirstSubject();
				IAssociationEnd pQualifierAsAssocEnd = pQualifierFirstSubject instanceof IAssociationEnd ? (IAssociationEnd) pQualifierFirstSubject : null;
				if (pQualifierAsAssocEnd != null) {
					int nEndKind = getNodeEnd(pQualifierAsAssocEnd);

					if (!(nEndKind == NodeEndKindEnum.NEK_FROM || nEndKind == NodeEndKindEnum.NEK_BOTH)) {
						bIsCorrect = false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bIsCorrect;
	}

	/**
	 Verifies that the source qualifier is correct
	 */
	public boolean isTargetQualifierCorrect() {
		boolean bIsCorrect = true;
		try {

			IPresentationElement pTargetQualifier = getTargetQualifier();

			if (pTargetQualifier != null) {
				IElement pQualifierFirstSubject = pTargetQualifier.getFirstSubject();

				IAssociationEnd pQualifierAsAssocEnd = pQualifierFirstSubject instanceof IAssociationEnd ? (IAssociationEnd) pQualifierFirstSubject : null;
				if (pQualifierAsAssocEnd != null) {
					int nEndKind = getNodeEnd(pQualifierAsAssocEnd);

					if (nEndKind == NodeEndKindEnum.NEK_FROM || nEndKind == NodeEndKindEnum.NEK_BOTH) {
						bIsCorrect = false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bIsCorrect;
	}

	/**
	 * Verifies that this link has valid ends - for the ternary association.
	 *
	 *
	 * @return boolean true if valid
	 */
	protected boolean validateLinkEndsForTernary() {
		return false; // We don't support ternary yet.
	}

	/**
	 * Try to reconnect the link to valid objects - for ternary associations.
	 *
	 * @return bSuccessfullyReconnected
	 */
	protected boolean reconnectLinkToValidNodesForTernary() {
		return true;
	}

	/**
	 Try to reconnect the link to valid objects.
	 *
	 @param bSuccessfullyReconnected [out,retval] true if this link was successfully connected.
	 *
	 @return boolean
	 */
	public boolean reconnectLinkToValidNodes() {
		boolean bSuccessfullyReconnected = false;
		try {

			if (isTernaryAssociation()) {
				bSuccessfullyReconnected = reconnectLinkToValidNodesForTernary();
			} else {
				bSuccessfullyReconnected = reconnectSimpleLinkToValidNodes();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Always return ok, if this routine throws then bSuccessfullyReconnected will be
		// false - that's our error condition
		return true;
	}

	/**
	 Verify this guy is a Association.
	 */
	public boolean isAssociation() {
		boolean bIsAssociation = false;
		try {
			IElement pEdgeElement = getModelElement();

			if (pEdgeElement != null) {
				String metaTypeString = pEdgeElement.getElementType();

				if (metaTypeString != null && metaTypeString.equals("Association")) {
					bIsAssociation = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bIsAssociation;
	}

	/**
	 Verifies that this link has valid ends.
	 *
	 @param bisValid [out,retval] true if the ends of this link match the underlying metadata.
	 *
	 @return boolean
	 */
	public boolean validateLinkEnds() {
		boolean bisValid = false;
		try {
			// Call the baseclass first.  It removes the cached model element to verify that the
			// ME and PE are still hooked up
			bisValid = super.validateLinkEnds();

			boolean bisTernary = isTernaryAssociation();
			if (bisTernary) {
				bisValid = validateLinkEndsForTernary();
			} else if (isAssociation()) {
				// Skip those link ends that act as the association class ends
				// Get the from and to IElements from the presentation data
				IElement pSourceNodeElement = this.getEdgeFromElement();
				IElement pTargetNodeElement = this.getEdgeToElement();
				IElement pProjectDataSourceNodeElement;
				IElement pProjectDataTargetNodeElement;

				// Get the from and to node IETElements
				IPresentationElement pSourceNodePE = this.getEdgeFromPresentationElement();
				IPresentationElement pTargetNodePE = this.getEdgeToPresentationElement();
				// Now, if it's a qualifier goto the connected node
				if (pSourceNodePE != null) {
					IPresentationElement pPEAttachedToQualifier;

					pPEAttachedToQualifier = navigatePastQualifier(pSourceNodePE);
					if (pPEAttachedToQualifier != null) {
						pSourceNodeElement = null;
						pSourceNodePE = pPEAttachedToQualifier;
						pSourceNodeElement = TypeConversions.getElement(pPEAttachedToQualifier);
					}
				}

				if (pTargetNodePE != null) {
					IPresentationElement pPEAttachedToQualifier = navigatePastQualifier(pTargetNodePE);

					if (pPEAttachedToQualifier != null) {
						pTargetNodeElement = null;
						pTargetNodePE = pPEAttachedToQualifier;
						pTargetNodeElement = TypeConversions.getElement(pPEAttachedToQualifier);
					}
				}

				// Get the from and to IElements from the data file

				ETPairT < IElement, IElement > fromToElements = getEdgeFromAndToElement(true);

				pProjectDataSourceNodeElement = fromToElements != null ? fromToElements.getParamOne() : null;
				pProjectDataTargetNodeElement = fromToElements != null ? fromToElements.getParamTwo() : null;

				// The link draw engines can draw with either node at either
				// end.  So we need to see if these two sets match either end.
				boolean bSourceSame = false;
				boolean bTargetSame = false;

				if (pSourceNodeElement != null && pTargetNodeElement != null && pProjectDataSourceNodeElement != null && pProjectDataTargetNodeElement != null) {
					bSourceSame = pProjectDataSourceNodeElement.isSame(pSourceNodeElement);
					bTargetSame = pProjectDataTargetNodeElement.isSame(pTargetNodeElement);

					if (bSourceSame == false || bTargetSame == false) {
						bSourceSame = pProjectDataSourceNodeElement.isSame(pTargetNodeElement);
						bTargetSame = pProjectDataTargetNodeElement.isSame(pSourceNodeElement);
					}
				}

				if (bSourceSame && bTargetSame) {
					bisValid = true;
				}
			} else {
				bisValid = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Always return ok, if this routine throws then bisValid will be 
		// false - that's our error condition
		return true;
	}

	/**
	 * Try to reconnect the link from pOldNode to pNewNode.
	 *
	 */
	protected boolean reconnectLinkForTernary(IETNode pOldNode, IETNode pNewNode, IETNode pAnchoredNode) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#reconnectLink(org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext)
	 */
	public boolean reconnectLink(IReconnectEdgeContext pContext) {
		boolean bSuccessfullyReconnected = false;
		try {

			IETNode pOldNode = null; // The node that is being disconnected
			IETNode pNewNode = null; // The proposed, new node to take its place
			IETNode pAnchoredNode = null; // The node that's not being moved - at the other end of the link

			if (pContext != null) {
				pOldNode = pContext.getPreConnectNode();
				pNewNode = pContext.getProposedEndNode();
				pAnchoredNode = pContext.getAnchoredNode();

				// Allow the target draw engine to determine if connectors should be created
				setReconnectConnectorFlag(pContext);
			}

			if (pOldNode != null && pNewNode != null && pAnchoredNode != null) {
				boolean bIsTernary = isTernaryAssociation();

				if (bIsTernary) {
					bSuccessfullyReconnected = reconnectLinkForTernary(pOldNode, pNewNode, pAnchoredNode);
				} else {
					IElement pEdgeElement;
					IElement pFromNodeElement;
					IElement pToNodeElement;

					// Get the elements for this PE's model element and the model element of
					// the from and to nodes.
					pEdgeElement = getModelElement();
					pFromNodeElement = TypeConversions.getElement(pOldNode);
					pToNodeElement = TypeConversions.getElement(pNewNode);

					if (pEdgeElement != null && pFromNodeElement != null && pToNodeElement != null) {
						IAssociation pAssociation = pEdgeElement instanceof IAssociation ? (IAssociation) pEdgeElement : null;
						IAssociationClass pAssocClass = pEdgeElement instanceof IAssociationClass ? (IAssociationClass) pEdgeElement : null;

						if (pFromNodeElement == pToNodeElement) {
							if (pAssocClass != null) {
								// Pay careful atttention.  This is because the association class element is tied
								// to the association class as well as the little node operating as the
								// connector to all the elements.  Disallow this event if the old or new draw engines
								// represent the little connector.

								String sOldNodeDrawEngineID = null;
								String sNewNodeDrawEngineID = null;

								IDrawEngine pOldNodeDrawEngine = TypeConversions.getDrawEngine(pOldNode);
								IDrawEngine pNewNodeDrawEngine = TypeConversions.getDrawEngine(pNewNode);

								if (pOldNodeDrawEngine != null) {
									sOldNodeDrawEngineID = pOldNodeDrawEngine.getDrawEngineID();
								}

								if (pNewNodeDrawEngine != null) {
									sNewNodeDrawEngineID = pNewNodeDrawEngine.getDrawEngineID();
								}

								if (sOldNodeDrawEngineID != null
									&& (sOldNodeDrawEngineID.length() == 0 || sOldNodeDrawEngineID.equals("AssociationClassConnectorDrawEngine"))
									|| sNewNodeDrawEngineID != null
									&& (sNewNodeDrawEngineID.length() == 0 || sNewNodeDrawEngineID.equals("AssociationClassConnectorDrawEngine"))) {
									bSuccessfullyReconnected = false;
								} else {
									bSuccessfullyReconnected = true;
								}
							} else {
								bSuccessfullyReconnected = true;
							}
						} else {
							// Get the to node
							IClassifier pToNodeClassifier = pToNodeElement instanceof IClassifier ? (IClassifier) pToNodeElement : null;

							// We have some special processing if we're an association class
							if (pToNodeClassifier != null && pAssociation != null) {
								ETList < IAssociationEnd > pEnds;
								IClassifier pCurrentElement;
								long numEnds = 0;

								ETPairT < IElement, IElement > elementData = getEdgeFromAndToElement(true);
								IElement pEnd1 = elementData != null ? elementData.getParamOne() : null;
								IElement pEnd2 = elementData != null ? elementData.getParamTwo() : null;

								pEnds = pAssociation.getEnds();
								numEnds = pAssociation.getNumEnds();

								if (pEnd1 != null && pEnd2 != null && numEnds == 2) {
									boolean bFromNodeEnd1 = false;
									boolean bFromNodeEnd2 = false;

									// See if the node we're disconnecting end #0 or end #1
									bFromNodeEnd1 = pEnd1.isSame(pFromNodeElement);
									bFromNodeEnd2 = pEnd2.isSame(pFromNodeElement);

									if (bFromNodeEnd1 && bFromNodeEnd2) {
										// We have a reflexive edge, associationclasses are tricky because
										// the tomsawyer idea of source and target can't be used because we
										// have two links - each representing a specific end.
										if (pAssocClass != null) {
											// Ask the draw engine what edge is what
											IDrawEngine pDrawEngine = getDrawEngine();

											IAssociationEdgeDrawEngine pAssocEdgeDrawEngine = pDrawEngine instanceof IAssociationEdgeDrawEngine ? (IAssociationEdgeDrawEngine) pDrawEngine : null;
											if (pAssocEdgeDrawEngine != null) {
												IAssociationEnd pThisEnd;
												IAssociationEnd pOtherEnd;
												long nOurIndex = 0;

												ETTripleT < IAssociationEnd, IAssociationEnd, Integer > assocEnds = pAssocEdgeDrawEngine.getAssociationEnd();

												if (assocEnds != null) {
													pThisEnd = assocEnds.getParamOne();
													pOtherEnd = assocEnds.getParamTwo();
													nOurIndex = assocEnds.getParamThree() != null ? assocEnds.getParamThree().longValue() : 0;
												}

												if (nOurIndex == 0) {
													bFromNodeEnd2 = false;
												} else {
													bFromNodeEnd1 = false;
												}
											}
										} else {
											boolean bReconnectTarget = pContext.getReconnectTarget();

											if (bReconnectTarget) {
												bFromNodeEnd2 = false;
											} else {
												bFromNodeEnd1 = false;
											}
										}
									}

									if (bFromNodeEnd1) {
										IAssociationEnd pAssociationEnd1 = pEnds.item(0);

										if (pAssociationEnd1 != null) {
											pAssociationEnd1.setParticipant(pToNodeClassifier);
											// Verify that the change took place
											pCurrentElement = pAssociationEnd1.getParticipant();

											bSuccessfullyReconnected = pToNodeClassifier.isSame(pCurrentElement);
											pCurrentElement = null;
										}
									} else if (bFromNodeEnd2) {
										IAssociationEnd pAssociationEnd2 = pEnds.item(1);
										if (pAssociationEnd2 != null) {
											pAssociationEnd2.setParticipant(pToNodeClassifier);
											// Verify that the change took place
											pCurrentElement = pAssociationEnd2.getParticipant();

											bSuccessfullyReconnected = pToNodeClassifier.isSame(pCurrentElement);
										}
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
		return bSuccessfullyReconnected;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement#transform(java.lang.String)
	 */
	public IPresentationElement transform(String elemName) {

		IPresentationElement newForm = null;
		try {
			// Call our base class which clears out the cached model element
			IPresentationElement pe = super.transform(elemName);

			IETGraphObject pETElement = this.getETGraphObject();

			//assert (pETElement);
			if (pETElement != null) {
				newForm = pETElement.transform(elemName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newForm;
	}

}

