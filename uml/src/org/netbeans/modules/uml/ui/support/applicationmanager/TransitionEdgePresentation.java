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

import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext;

public class TransitionEdgePresentation extends EdgePresentation implements ITransitionEdgePresentation {

	/**
	 * 
	 */
	public TransitionEdgePresentation() {
		super();
	}

	/**
	 * Verifies that this link has valid ends.
	 *
	 * @param bIsValid [out,retval] true if the ends of this link match the underlying metadata.
	 *
	 * @return HRESULT
	 */
	public boolean validateLinkEnds()
	{
      // Call the baseclass first.  It removes the cached model element to verify that the
      // ME and PE are still hooked up
		boolean valid = super.validateLinkEnds();
      
		if (isTransition())
		{
			valid = validateSimpleLinkEnds();
		}
      
		return valid;
	}

	/**
	 * Try to reconnect the link to valid objects.
	 *
	 * @param bSuccessfullyReconnected [out,retval] true if this link was successfully connected.
	 *
	 * @return HRESULT
	 */
	public boolean reconnectLinkToValidNodes()
	{
		boolean success = false;
		if (isTransition())
		{
			//success = reconnectSimpleLinkToValidNodes();
		}
		return success;
	}

	/**
	 * Verify this guy is a transition.
	 */
	public boolean isTransition()
	{
		boolean trans = false;
		IElement pEdgeEle = getModelElement();
		if (pEdgeEle != null && pEdgeEle instanceof ITransition)
		{
			trans = true;
		}
		return trans;
	}

	/**
	 * The IElement this PE is attached to has transformed.  We may need to transform this PE as well.
	 *
	 * @param typeName[in]
	 * @param newForm[out]
	 * 
	 * @return HRESULT
	 */
	public IPresentationElement transform( String typeName)
	{
		// Call our base class which clears out the cached model element
		IPresentationElement retEle = super.transform(typeName);
		IETGraphObject pETElement =  getETGraphObject();
		if (pETElement != null)
		{
			//retEle = pETElement.transform(typeName);
		}
		return retEle;
	}

	/**
	 * Try to reconnect the link from pOldNode to pNewNode.
	 *
	 * @param pContext [in] Information about how the edge was reconnected
	 * @param bSuccessfullyReconnected [out,retval] Set to true to allow this reconnect to succeed.
	 *
	 * @return HRESULT
	 */
	public boolean reconnectLink(Object pContext)
	{
		boolean success = false;
		IETNode pOldNode = null;
		IETNode pNewNode = null;
		if (pContext != null && pContext instanceof IReconnectEdgeContext)
		{
			IReconnectEdgeContext pEdgeContext = (IReconnectEdgeContext)pContext;
			pOldNode = pEdgeContext.getPreConnectNode();
			pNewNode = pEdgeContext.getProposedEndNode();

			// Allow the target draw engine to determine if connectors should be created
			setReconnectConnectorFlag(pEdgeContext);
		}
		
		if (pOldNode != null && pNewNode != null)
		{
			if (isTransition())
			{
				// Get the elements for this PE's model element and the model element of
				// the from and to nodes.
				IElement pEdgeEle = getModelElement();
				IElement pFromNodeEle = TypeConversions.getElement(pOldNode);
				IElement pToNodeEle = TypeConversions.getElement(pNewNode);
				
				if (pEdgeEle != null && pFromNodeEle != null && pToNodeEle != null)
				{
					if (pToNodeEle instanceof IStateVertex && pEdgeEle instanceof ITransition)
					{
						ITransition pTransition = (ITransition)pEdgeEle;
						IStateVertex pVertex = (IStateVertex)pToNodeEle;
						
						IStateVertex pSource = pTransition.getSource();
						IStateVertex pTarget = pTransition.getTarget();
						IStateVertex pCurrentEle = null;
						
						if (pSource != null && pTarget != null)
						{
							boolean bFromNodeSource = pSource.isSame(pFromNodeEle);
							boolean bFromNodeTarget = pTarget.isSame(pFromNodeEle);
							
							// See if the node we're disconnecting is the implementing classifier or Supplier
							if (bFromNodeSource)
							{
								pTransition.setSource(pVertex);

								// Verify the change took place
								pCurrentEle = pTransition.getSource();
								success = pVertex.isSame(pCurrentEle);
							}
							
							if (bFromNodeTarget)
							{
								pTransition.setTarget(pVertex);

								// Verify the change took place
								pCurrentEle = pTransition.getTarget();
								success = pVertex.isSame(pCurrentEle);
							}
						}
					}
				}
			}
		}
		
		return success;
	}
}

