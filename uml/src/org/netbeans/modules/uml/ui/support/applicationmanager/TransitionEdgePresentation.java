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

