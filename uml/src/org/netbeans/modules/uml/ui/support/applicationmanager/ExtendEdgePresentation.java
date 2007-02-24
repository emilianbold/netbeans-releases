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

import org.netbeans.modules.uml.core.metamodel.core.constructs.IExtend;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext;

public class ExtendEdgePresentation extends EdgePresentation implements IExtendEdgePresentation {

	/**
	 * 
	 */
	public ExtendEdgePresentation() {
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
		return isExtend() && validateSimpleLinkEnds();
	}

	/**
	 * Verify this guy is an extend connector.
	 */
	public boolean isExtend()
	{
		return getModelElement() instanceof IExtend;
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
		return isExtend() && reconnectSimpleLinkToValidNodes();
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
		boolean successfullyReconnected = false;
		IETNode oldNode = null; // The node that is being disconnected
		IETNode newNode = null; // The proposed, new node to take its place
		
		if (pContext != null)
		{
			oldNode = ((IReconnectEdgeContext)pContext).getPreConnectNode();
			newNode = ((IReconnectEdgeContext)pContext).getProposedEndNode();

			// Allow the target draw engine to determine if connectors should be created
			setReconnectConnectorFlag((IReconnectEdgeContext)pContext);
		}
		
		if (oldNode != null && newNode != null)
		{
			if (isExtend())
			{
				// Get the elements for this PE's model element and the model element of
				// the from and to nodes.
				IElement pEdgeEle = getModelElement();
				IElement pFromNodeEle = TypeConversions.getElement(oldNode);
				IElement pToNodeEle = TypeConversions.getElement(newNode);
				
				if (pEdgeEle != null && pFromNodeEle != null && pToNodeEle != null)
				{
					// Get the to node
					if (pEdgeEle instanceof IExtend && pToNodeEle instanceof IUseCase)
					{
						IUseCase toUseCase = (IUseCase)pToNodeEle;
						IExtend pExtend = (IExtend)pEdgeEle;
						
						if (toUseCase != null && pExtend != null)
						{
							IUseCase pBase = pExtend.getBase();
							IUseCase pExtension = pExtend.getExtension();
							IUseCase pCurrentEle = null;
							if (pBase != null && pExtension != null)
							{
								boolean fromNodeBase = false;
								boolean fromNodeExtension = false;
								
								// See if the node we're disconnecting is the implementing classifier or Supplier
								fromNodeBase = pBase.isSame(pFromNodeEle);
								fromNodeExtension = pExtension.isSame(pFromNodeEle);
								
								if (fromNodeBase)
								{
									pExtend.setBase(toUseCase);
									
									// Verify the change took place
									pCurrentEle = pExtend.getBase();
									successfullyReconnected = toUseCase.isSame(pCurrentEle);
								}
								
								if (fromNodeExtension)
								{
									pExtend.setExtension(toUseCase);
									
									// Verify the change took place
									pCurrentEle = pExtend.getExtension();
									successfullyReconnected = toUseCase.isSame(pCurrentEle);
								}
							}
						}
					}
				}
			}
		}
		return successfullyReconnected;
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
		IETGraphObject pETElement = getETGraphObject();
		//WE need to make sure that we have transform method in ETEdge, ETLabel and ETNode
		//then we need to remove the check (pETElement instanceof IETElement)
		if (pETElement != null)
		{
			retEle = pETElement.transform(typeName);
		}

		return retEle;
	}
}

