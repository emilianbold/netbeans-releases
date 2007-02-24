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
import org.netbeans.modules.uml.core.metamodel.core.constructs.IPartFacade;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.ICollaboration;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IConnectableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IStructuredClassifier;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext;

/**
 * @author sumitabhk
 *
 */
public class PartFacadeEdgePresentation extends EdgePresentation{
	
	public PartFacadeEdgePresentation()
	{
	}

	/**
	 * Try to reconnect the link to valid objects.
	 *
	 * @param bSuccessfullyReconnected[out,retval] true if this link was successfully connected
	 */
	public boolean reconnectLinkToValidNodes()
	{
		boolean retVal = false;
		if (isPartFacade())
		{
			retVal = reconnectSimpleLinkToValidNodes();
		}
		return retVal;
	}

	/**
	 * Verifies that this link has valid ends.
	 *
	 * @param bIsValid[out,retval] true if the ends of this link match the underlying metadata
	 */
	public boolean validateLinkEnds()
	{
		boolean retVal = false;
		if (isPartFacade())
		{
			// Get the elements for this PE's model element and the model element of
			// the Source and to nodes.
			IElement pEdgeEle = getModelElement();
			IElement pSourceNodeEle = null;
			IElement pTargetNodeEle = null;

			// Get the from and to node IElements
			ETPairT<IElement, IElement> retObj = getEdgeFromAndToElement(false);
			if (retObj != null)
			{
				pSourceNodeEle = retObj.getParamOne();
				pTargetNodeEle = retObj.getParamTwo();
			}
			
			// One of the nodes should be the partfacade, the other a collaboration.  The
			// edge should have the same model element as the partfacade.
			if (pEdgeEle != null && pEdgeEle instanceof IPartFacade && pSourceNodeEle != null && pTargetNodeEle != null)
			{
				// Get the collaboration
				IPartFacade pPartFacade = null;
				ICollaboration pCollaboration = null;
				if (pSourceNodeEle instanceof IPartFacade)
				{
					pPartFacade = (IPartFacade)pSourceNodeEle;
					if (pTargetNodeEle instanceof ICollaboration)
					{
						pCollaboration = (ICollaboration)pTargetNodeEle;
					}
				}
				else
				{
					if (pTargetNodeEle instanceof IPartFacade)
					{
						pPartFacade = (IPartFacade)pTargetNodeEle;
					}
					if (pSourceNodeEle instanceof ICollaboration)
					{
						pCollaboration = (ICollaboration)pSourceNodeEle;
					}
				}
				
				// You can get the Collaboration from the Roles by QI'ing the PartFacade to a 
				// ConnectableElement, then calling the RoleContext property, which gives you 
				// an IStructuredClassifier interface. QI that to ICollaboration. Bingo
				if (pPartFacade != null && pPartFacade instanceof IConnectableElement)
				{
					IConnectableElement pConnectableEle = (IConnectableElement)pPartFacade;
					if (pCollaboration != null)
					{
						ETList<IStructuredClassifier> pClassifiers = pConnectableEle.getRoleContexts();
						int count = 0;
						if (pClassifiers != null)
						{
							count = pClassifiers.size();
						}
						
						// Now see if any of the role contexts are the classifier
						boolean isSame = false;
						for (int i=0; i<count; i++)
						{
							IStructuredClassifier pClassifier = pClassifiers.get(i);
							isSame = pClassifier.isSame(pCollaboration);
							if (isSame)
							{
								break;
							}
						}
						
						if (isSame)
						{
							retVal = true;
						}
					}
				}
			}
		}
		return retVal;
	}

	/**
	 * Try to reconnect the link from pOldNode to pNewNode.
	 *
	 * @param pContext [in] Information about how the edge was reconnected
	 * @param bSuccessfullyReconnected [out,retval] Set to true to allow this reconnect to succeed.
	 *
	 * @return HRESULT
	 */
	public boolean reconnectLink(IReconnectEdgeContext pContext)
	{
		boolean retVal = false;
		if (pContext != null)
		{
			IETNode pOldNode = pContext.getPreConnectNode();
			IETNode pNewNode = pContext.getProposedEndNode();
			IETNode pAnchoredNode = pContext.getAnchoredNode();
			
			// Allow the target draw engine to determine if connectors should be created
			setReconnectConnectorFlag(pContext);
			
			if (pOldNode != null && pNewNode != null && pAnchoredNode != null)
			{
				// Get the elements for this PE's model element and the model element of
				// the from and to nodes.
				IElement pEdgeEle = getModelElement();
				IElement pFromNodeEle = TypeConversions.getElement(pOldNode);
				IElement pToNodeEle = TypeConversions.getElement(pNewNode);
				IElement pAnchorEle = TypeConversions.getElement(pAnchoredNode);
				
				// This element should be a PartFacade
				IPartFacade pPartFacade = null;
				ICollaboration pNamedFromEle = null;
				ICollaboration pNamedToEle = null;
				ICollaboration pNamedAnchorEle = null;
				
				if (pEdgeEle instanceof IPartFacade)
				{
					pPartFacade = (IPartFacade)pEdgeEle;
				}
				
				if (pFromNodeEle instanceof ICollaboration)
				{
					pNamedFromEle = (ICollaboration)pFromNodeEle;
				}
				if (pToNodeEle instanceof ICollaboration)
				{
					pNamedToEle = (ICollaboration)pToNodeEle;
				}
				if (pAnchorEle instanceof ICollaboration)
				{
					pNamedAnchorEle = (ICollaboration)pAnchorEle;
				}
				
				if (pPartFacade != null && pNamedFromEle != null && pNamedToEle != null)
				{
					// See if we are moving the end closest to the PartFacade, moving the link
					// from one PartFacade to another.
					boolean bPartFacadeEndIsFromNode = pPartFacade.isSame(pFromNodeEle);
					if (bPartFacadeEndIsFromNode)
					{
						if (pToNodeEle instanceof IPartFacade)
						{
							// The Bridge link is connected to the IPartFacade.  Change that.
							IETGraphObject pETElement = getETGraphObject();
							if (pETElement != null && pNamedAnchorEle != null)
							{
								//
								// Don't worry about this right now.  Cameron doesn't want to allow
								// reconnecting PartFacade links so we've set 
								// PartFacadeEdgeDrawEngineImpl::get_AllowReconnection to return false.
								//
//		   #if 0
//								// Get the other end of the node
//								_VH(pPartFacade->RemoveAnnotatedElement(pNamedAnchoredNodeElement));
//								_VH(pNewPartFacade->AddAnnotatedElement(pNamedAnchoredNodeElement));
//								_VH(put_ModelElement(pNewPartFacade));
//
//								// Verify the change took place
//								_VH(pNewPartFacade->get_IsAnnotatedElement(pNamedAnchoredNodeElement, bSuccessfullyReconnected));
//		   #endif
							}
						}
					}
					else
					{
//						#if 0
//									   // We're moving the annotated end.
//									   _VH(pPartFacade->RemoveAnnotatedElement(pNamedFromNodeElement));
//									   _VH(pPartFacade->AddAnnotatedElement(pNamedToNodeElement));
//
//									   // Verify the change took place
//									   _VH(pPartFacade->get_IsAnnotatedElement(pNamedToNodeElement, bSuccessfullyReconnected));
//									   *bSuccessfullyReconnected = true;
//						#endif
					}
				}
			}
		}
		return retVal;
	}

	/**
	 * The IElement this PE is attached to has transformed.  We may need to transform this PE as well.
	 *
	 * @param typeName [in] The new type to transform to
	 * @param newForm [out] The new, created presentation element
	 *
	 * @return HRESULT
	 */
	public IPresentationElement transform( String typeName)
	{
		IPresentationElement retEle = null;
		IETGraphObject pETEle = getETGraphObject();
		if (pETEle != null)
		{
			retEle = pETEle.transform(typeName);
		}
		return retEle;
	}

	/**
	 * Verify this guy is a part facade.
	 */
	private boolean isPartFacade()
	{
		boolean retVal = false;
		IElement pEdgeEle = getModelElement();
		if (pEdgeEle != null && pEdgeEle instanceof IPartFacade)
		{
			retVal = true;
		}
		return retVal;
	}

}


