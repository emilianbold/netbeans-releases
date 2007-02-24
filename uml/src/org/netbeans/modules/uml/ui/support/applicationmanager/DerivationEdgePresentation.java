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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IDerivation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext;

/**
 * @author sumitabhk
 *
 */
public class DerivationEdgePresentation extends EdgePresentation
{

	/**
	 * Verifies that this link has valid ends.
	 *
	 * @param bIsValid [out,retval] true if the ends of this link match the underlying metadata
	 */
	public boolean validateLinkEnds()
	{
		boolean isValid = false;
		if (isDerivation())
		{
			isValid = validateSimpleLinkEnds();
		}
		return isValid;
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
		if (isDerivation())
		{
			success = reconnectSimpleLinkToValidNodes();
		}
		return success;
	}
	
	/**
	 * Try to reconnect the link from pOldNode to pNewNode.
	 *
	 * @param pContext [in] Information about how the edge was reconnected
	 * @param bSuccessfullyReconnected [out,retval] Set to true to allow this reconnect to succeed.
	 * 
	 * @return HRESULT
	 */
	public boolean reconnectLink(Object dispContext)
	{
		boolean success = false;
		IETNode oldNode = null;
		IETNode newNode = null;
		IETNode anchorNode = null;
		if (dispContext instanceof IReconnectEdgeContext)
		{
			IReconnectEdgeContext pContext = (IReconnectEdgeContext)dispContext;
			oldNode = pContext.getPreConnectNode();
			newNode = pContext.getProposedEndNode();
			anchorNode = pContext.getAnchoredNode();

			// Allow the target draw engine to determine if connectors should be created
			setReconnectConnectorFlag(pContext);
		}
		
		if (oldNode != null && newNode != null && anchorNode != null)
		{
			if (isDerivation())
			{
				IElement pEdgeElement = null;
				IElement pFromNodeEle = null;
				IElement pToNodeEle = null;
				
				// Get the elements for this PE's model element and the model element of
				// the from and to nodes.
				pEdgeElement = getModelElement();
				pFromNodeEle = TypeConversions.getElement(oldNode);
				pToNodeEle = TypeConversions.getElement(newNode);
				
				if (pEdgeElement != null && pFromNodeEle != null && pToNodeEle != null)
				{
					if (pToNodeEle instanceof IClassifier && pEdgeElement instanceof IDerivation)
					{
						IClassifier pToNodeClassifier = (IClassifier)pToNodeEle;
						IDerivation pDerivation = (IDerivation)pEdgeElement;
						
						IClassifier pDerivedClassifier = null;
						IClassifier pTemplate = null;
						IClassifier pCurrEle = null;
						
						pDerivedClassifier = pDerivation.getDerivedClassifier();
						pTemplate = pDerivation.getTemplate();
						if (pDerivedClassifier != null && pTemplate != null)
						{
							boolean bFromNodeDerivedClassifier = false;
							boolean bFromNodeTemplate = false;
							
							// See if the node we're disconnecting is the implementing classifier or Template
							bFromNodeDerivedClassifier = pDerivedClassifier.isSame(pFromNodeEle);
							bFromNodeTemplate = pTemplate.isSame(pFromNodeEle);
							
							if (bFromNodeDerivedClassifier)
							{
								pDerivation.setDerivedClassifier(pToNodeClassifier);
								
								// Verify the change took place
								pCurrEle = pDerivation.getDerivedClassifier();
								success = pToNodeClassifier.isSame(pCurrEle);
							}
							
							if (bFromNodeTemplate)
							{
								pDerivation.setTemplate(pToNodeClassifier);

								// Verify the change took place
								pCurrEle = pDerivation.getTemplate();
								success = pToNodeClassifier.isSame(pCurrEle);
							}
						}
					}
				}
			}
		}
		return success;
	}
	
	/**
	 * Verify this guy is a Derivation.
	 */
	public boolean isDerivation()
	{
		boolean retVal = false;
		IETGraphObject pETElement = getETGraphObject();
		if (pETElement != null)
		{
			IElement pEdgeElement = TypeConversions.getElement(pETElement);
			if (pEdgeElement instanceof IDerivation)
			{
				retVal = true;
			}
		}
		return retVal;
	}
	
}



