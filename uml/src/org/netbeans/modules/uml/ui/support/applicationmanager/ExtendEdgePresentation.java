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

