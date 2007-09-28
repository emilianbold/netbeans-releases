/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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



