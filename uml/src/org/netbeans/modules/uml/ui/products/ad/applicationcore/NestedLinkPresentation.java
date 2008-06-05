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



package org.netbeans.modules.uml.ui.products.ad.applicationcore;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
//import org.netbeans.modules.uml.ui.support.applicationmanager.EdgePresentation;
//import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
//import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
//import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
//import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext;

/**
 * @author KevinM
 *
  */
public class NestedLinkPresentation
//        extends EdgePresentation //TODO
        implements INestedLinkPresentation {

	/**
	 * 
	 */
	public NestedLinkPresentation() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.applicationcore.INestedLinkPresentation#isParentNamespace(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
	 */
	public boolean isParentNamespace(IElement pEndElement) {
		boolean bIsValid = false;
		try { //TODO
			if (isNamedElement()) {
				// Get the elements for this PE's model element and the model element of
				// the Source and to nodes.
//				IElement pEdgeElement = getModelElement();
//
//				// Get the from and to node IElements
//				// Target is the parent namespace (the + sign)
//				// Source is the child namespace
//				ETPairT < IElement, IElement > pElements = getEdgeFromAndToElement(false);
//
//				IElement pSourceNodeElement = pElements != null ? pElements.getParamOne() : null;
//				IElement pTargetNodeElement = pElements != null ? pElements.getParamTwo() : null;
//
//				// One of the nodes should be the comment, the other an annotated comment.  The
//				// edge should have the same model element as the comment.
//				if (pEdgeElement != null && pSourceNodeElement != null && pTargetNodeElement != null) {
//					ETPairT < IElement, IElement > parentChild = verifyNamespaceConnection(pSourceNodeElement, pTargetNodeElement);
//					IElement pParent = parentChild.getParamOne();
//					IElement pChild = parentChild.getParamTwo();
//					if (pParent != null && pChild != null) {
//						// The is same will determine if the argument element is the same as the parent
//						bIsValid = pParent.isSame(pEndElement);
//					}
//				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return bIsValid;
	}

//	/* (non-Javadoc)
//	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#reconnectLink(org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext)
//	 */
//	public boolean reconnectLink(IReconnectEdgeContext pContext) {
//		boolean bSuccessfullyReconnected = false;
//		if (pContext == null)
//			return false;
//			
//		try {
//
//			IETNode pOldNode = pContext.getPreConnectNode(); // The node that is being disconnected
//			IETNode pNewNode = pContext.getProposedEndNode(); // The proposed, new node to take its place
//			IETNode pAnchoredNode = pContext.getAnchoredNode(); // The node that's not being moved - at the other end of the link
//
//			if (isNamedElement() && pOldNode != null && pNewNode != null && pAnchoredNode != null) {
//				// Get the elements for this PE's model element and the model element of
//				// the from and to nodes.
//				IElement pEdgeElement = getModelElement();
//				IElement pFromNodeElement = TypeConversions.getElement(pOldNode);
//				IElement pToNodeElement = TypeConversions.getElement(pNewNode);
//				IElement pAnchoredNodeElement = TypeConversions.getElement(pAnchoredNode);
//				IPresentationElement pThis = this;
//
//				// Convert them the namedelements
//				INamedElement pFromNodeNamedElement = getNamedElement(pFromNodeElement);
//				INamedElement pToNodeNamedElement = getNamedElement(pToNodeElement);
//				INamedElement pAnchoredNodeNamedElement = getNamedElement(pAnchoredNodeElement);
//
//				if (pFromNodeNamedElement != null && pToNodeElement != null && pAnchoredNodeElement != null) {
//					// Get the namespaces of all the named elements
//					INamespace pFromNodeNamespace = pFromNodeNamedElement.getNamespace();
//					INamespace pToNodeNamespace = pToNodeNamedElement.getNamespace();
//					INamespace pAnchoredNamespace = pAnchoredNodeNamedElement.getNamespace();
//
//					if (pFromNodeNamespace != null && pToNodeNamespace != null && pAnchoredNamespace != null) {
//						// Now we need to see what's moving, the parent or the child (the child has the +)
//						boolean bIsSame = pFromNodeNamespace.isSame(pAnchoredNodeElement);
//
//						if (bIsSame) {
//							if (pAnchoredNodeNamedElement instanceof INamespace) {
//								INamespace pAnchoredNodeAsNamespace = (INamespace) pAnchoredNodeNamedElement;
//								// Moving the non-plus sign end
//								boolean bCaughtException = false;
//								try {
//									// Change the TO node to be in namespace of the anchored element
//									pToNodeNamedElement.setNamespace(pAnchoredNodeAsNamespace);
//									// Change the FROM node to be in the same namespace as the achored element is in
//									pFromNodeNamedElement.setNamespace(pAnchoredNamespace);
//								} catch (Exception e) {
//									bCaughtException = true;
//								}
//
//								if (!bCaughtException) {
//									// Make sure the namespace change took place
//									INamespace pCurrentNamespace = pToNodeNamedElement.getNamespace();
//									if (pCurrentNamespace != null) {
//										bIsSame = pCurrentNamespace.isSame(pAnchoredNodeAsNamespace);
//										if (bIsSame) {
//											// Set the new model element to be the parent namespace
//											setModelElement(pCurrentNamespace);
//
//											bSuccessfullyReconnected = true;
//										}
//									}
//								}
//							}
//						} else {
//							if (pToNodeElement instanceof INamespace) {
//								INamespace pToNodeAsNamespace = (INamespace) pToNodeElement;
//								// Moving the + sign
//								boolean bCaughtException = false;
//								try {
//									// Change the anchored node to be in the namespace of the to element
//									pAnchoredNodeNamedElement.setNamespace(pToNodeAsNamespace);
//								} catch (Exception e) {
//									bCaughtException = true;
//								}
//
//								if (!bCaughtException) {
//									// Make sure the namespace change took place
//									INamespace pCurrentNamespace = pAnchoredNodeNamedElement.getNamespace();
//									if (pCurrentNamespace != null) {
//										bIsSame = pCurrentNamespace.isSame(pToNodeAsNamespace);
//										if (bIsSame) {
//											// Set the new model element to be the parent namespace
//											setModelElement(pCurrentNamespace);
//
//											bSuccessfullyReconnected = true;
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return bSuccessfullyReconnected;
//
//	}

//	/* (non-Javadoc)
//	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#reconnectLinkToValidNodes()
//	 */
//	public boolean reconnectLinkToValidNodes() {
//
//		try {
//			boolean bSuccessfullyReconnected = false;
//
//			if (isNamedElement()) {
//				IDiagram pDiagram = getDiagram();
//
//				// First find out if the source or the target is the child model element.
//
//				IPresentationElement pThis = this;
//
//				if (pThis instanceof IEdgePresentation && pDiagram != null) {
//					IEdgePresentation pThisEdgePresentation = (IEdgePresentation) pThis;
//
//					// Target is the parent namespace (the + sign)
//					// Source is the child namespace
//					ETPairT < IElement, IElement > pElements = pThisEdgePresentation.getEdgeFromAndToElement(false);
//
//					IElement pSourceModelElement = pElements != null ? pElements.getParamOne() : null;
//					IElement pTargetModelElement = pElements != null ? pElements.getParamTwo() : null;
//					ETPairT < IETGraphObject, IETGraphObject > nodes = getEdgeFromAndToNode();
//					IETGraphObject pFromNode = nodes != null ? nodes.getParamOne() : null;
//					IETGraphObject pToNode = nodes != null ? nodes.getParamTwo() : null;
//					IPresentationElement pTargetPE = pToNode != null ? pToNode.getPresentationElement() : null;
//
//					IPresentationElement pSourcePE = pFromNode != null ? pFromNode.getPresentationElement() : null;
//
//					INamedElement pSourceAsNamedElement = pSourceModelElement instanceof INamedElement ? (INamedElement) pSourceModelElement : null;
//					if (pSourceAsNamedElement != null && pTargetPE != null && pSourcePE != null) {
//						INamespace pSourceNamespace = pSourceAsNamedElement.getNamespace();
//						if (pSourceNamespace != null) {
//							// Find this namespace on the diagram
//							ETList < IPresentationElement > pPEs = pDiagram.getAllItems2(pSourceNamespace);
//							long count = pPEs != null ? pPEs.getCount() : 0;
//
//							if (count == 1) {
//								IPresentationElement pEndPE = pPEs.get(0);
//								if (pEndPE != null) {
//									bSuccessfullyReconnected = pDiagram.reconnectLink(pThis, pEndPE, pSourcePE);
//								}
//							}
//						}
//					}
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		// Always return ok, if this routine throws then bSuccessfullyReconnected will be
//		// false - that's our error condition
//		return true;
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation#validateLinkEnds()
	 */
	public boolean validateLinkEnds() {
		boolean bIsValid = false;
		try { //TODO
			if (isNamedElement()) {
				// Get the elements for this PE's model element and the model element of
				// the Source and to nodes.
//				IElement pEdgeElement = getModelElement();
//
//				// Get the from and to node IElements
//				// Target is the parent namespace (the + sign)
//				// Source is the child namespace
//				ETPairT < IElement, IElement > pElements = getEdgeFromAndToElement(false);
//
//				IElement pSourceNodeElement = pElements != null ? pElements.getParamOne() : null;
//				IElement pTargetNodeElement = pElements != null ? pElements.getParamTwo() : null;
//
//				// One of the nodes should be the comment, the other an annotated comment.  The
//				// edge should have the same model element as the comment.
//				if (pEdgeElement != null && pSourceNodeElement != null && pTargetNodeElement != null) {
//					ETPairT < IElement, IElement > parentChild = verifyNamespaceConnection(pSourceNodeElement, pTargetNodeElement);
//					IElement pParent = parentChild.getParamOne();
//					IElement pChild = parentChild.getParamTwo();
//					if (pParent != null && pChild != null) {
//						bIsValid = true;
//					}
//				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bIsValid;
	}

	/*
	 * Verify this guy is a named element
	 */
	protected boolean isNamedElement() {
//		return getModelElement() instanceof INamedElement;
            return false; //TODO
	}

	/*
	 * Simple casting function.
	 */
	protected INamedElement getNamedElement(IElement pElement) {
		return pElement instanceof INamedElement ? (INamedElement) pElement : null;
	}

	/*
	 * Given two model elements return the parent and the child, or null if one isn't in the namespace of another
	 */
	protected ETPairT < IElement, IElement > verifyNamespaceConnection(IElement pSourceElement, IElement pTargetElement) {
		IElement pParentElement = null;
		IElement pChildElement = null;
		try {

			INamedElement pSourceNamedElement = pSourceElement instanceof INamedElement ? (INamedElement) pSourceElement : null;
			INamedElement pTargetNamedElement = pTargetElement instanceof INamedElement ? (INamedElement) pTargetElement : null;
			if (pSourceNamedElement != null && pTargetNamedElement != null) {
				// One should be a namespace of the other
				INamespace pSourceNamespace = pSourceNamedElement.getNamespace();
				INamespace pTargetNamespace = pTargetNamedElement.getNamespace();
				boolean bIsSame = false;
				if (pSourceNamespace != null || pTargetNamespace != null) {
					if (pSourceNamespace != null) {
						bIsSame = pSourceNamespace.isSame(pTargetElement);
					}

					if (bIsSame) {
						pParentElement = pTargetElement;
						pChildElement = pSourceElement;
					} else if (pTargetNamespace != null) {
						bIsSame = pTargetNamespace.isSame(pSourceElement);
						if (bIsSame) {
							pChildElement = pTargetElement;
							pParentElement = pSourceElement;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ETPairT < IElement, IElement > (pParentElement, pChildElement);
	}
}
