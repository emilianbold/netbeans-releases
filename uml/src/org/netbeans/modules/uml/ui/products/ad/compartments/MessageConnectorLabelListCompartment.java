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


//	 $Date$
package org.netbeans.modules.uml.ui.products.ad.compartments;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessageConnector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.ui.support.CreationFactoryHelper;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;

public class MessageConnectorLabelListCompartment extends ETListCompartment implements IMessageConnectorLabelListCompartment {

	// This is the message we're attached to
	private IMessage m_pMessage;

	public MessageConnectorLabelListCompartment() {
		super();
		this.setShowName(false);
	}

	public MessageConnectorLabelListCompartment(IDrawEngine pDrawEngine) {
		super(pDrawEngine);
		this.setShowName(false);
	}

	public String getCompartmentID() {
		return "MessageConnectorLabelListCompartment";
	}

	/**
	 * Draws each of the individual compartments.
	 *
	 * @param pInfo [in] Information about the draw event (ie the DC, are we printing...)
	 * @param boundingRect [in] The bounding rect to draw into
	 */
	public void draw(IDrawInfo pInfo, IETRect boundingRect) {


		if (pInfo != null) {
			
			// Get the first compartment
			ICompartment pCompartment = getCompartment(0);

			IADLabelNameCompartment pNameCompartment = null;
			IMessageConnectorArrowCompartment pMessageConnectorArrowCompartment = null;
			
			if (pCompartment instanceof IADLabelNameCompartment){				
				pNameCompartment = (IADLabelNameCompartment) pCompartment;
			}else if (pCompartment instanceof IMessageConnectorArrowCompartment){				
				pMessageConnectorArrowCompartment = (IMessageConnectorArrowCompartment) pCompartment;
			}

			// Get the second compartment
			pCompartment = null;
			
			pCompartment = getCompartment(1);
			
			if (pNameCompartment == null && pCompartment instanceof IADLabelNameCompartment) {
				pNameCompartment = (IADLabelNameCompartment)pCompartment;
			}
			if (pMessageConnectorArrowCompartment == null && pCompartment instanceof IMessageConnectorArrowCompartment) {
				pMessageConnectorArrowCompartment = (IMessageConnectorArrowCompartment)pCompartment;
			}

			if (pNameCompartment != null && pMessageConnectorArrowCompartment != null) {
				long nTempX = 0;
				long nTempY = 0;

				IETSize pETSize = pNameCompartment.calculateOptimumSize(pInfo, false);

				if (pETSize != null) {

					// The arrow compartment figures out where it wants to draw and then returns
					// where the name should be.
					IETRect pNameRect = pMessageConnectorArrowCompartment.draw2(pInfo, boundingRect, pETSize);

					if (pNameRect != null) {
						pNameCompartment.draw(pInfo, pNameRect);
					}
				}
			}
		}
	}

	/**
	 * Create a copy of yourself.
	 *
	 * @param pParentDrawEngine [in] The parent draw engine for this compartment
	 * @param pRetCompartment[out,retval] The clone of this compartment
	 */
	public ICompartment clone(IDrawEngine pParentDrawEngine) {

		ICompartment pRetCompartment = null;

		IMessageConnectorLabelListCompartment pNewCompartment = (IMessageConnectorLabelListCompartment) CreationFactoryHelper.createCompartment("MessageConnectorLabelListCompartment");

		if (pNewCompartment != null) {
			pNewCompartment = this;

			pRetCompartment = pNewCompartment;
			pNewCompartment.setEngine(pParentDrawEngine);
		}

		return pRetCompartment;
	}

	/**
	 * Adds a compartment by calling ListCompartmentImpl::AddCompartment() 
	 *
	 * @param pCompartment - The compartment to add.  If NULL nothing happens
	 * @param nIndex - The position in the visible list to place this compartment
	 */
	public synchronized long addCompartment(ICompartment pCompartment, int nIndex, boolean bRedrawNow) {

		//		// don't allow re-entrant adds
		//		CSingleLock lock(& m_Semaphore);
		//
		//		lock.Lock(0);
		//		if (lock.IsLocked()) {
		super.addCompartment(pCompartment, nIndex, bRedrawNow);

		//			lock.Unlock();
		//		}

	 return 0;
	}

	/**
	 * Initialize the compartments
	 */
	public void initCompartments(IPresentationElement pElement) {

		this.initCompartments(pElement);

	}

	/**
	 * Adds a model element to this compartment.
	 *
	 * @param pElement[in] The model element to be added
	 * @param nIndex[in] Where should the new compartment be created in the list of the current compartments?
	 */
	public synchronized void addModelElement(IElement pElement, int nIndex) {

		IDrawEngine engine = this.getEngine();

		if (engine != null) {

			IOperation pOperation = null;

			m_pMessage = (IMessage) pElement;

			if (m_pMessage != null) {
				pOperation = m_pMessage.getOperationInvoked();
			}

			if (m_pMessage != null && pOperation != null) {

				IADLabelNameCompartment pNameCompartment = (IADLabelNameCompartment) CreationFactoryHelper.createCompartment("ADLabelNameCompartment");
				IMessageConnectorArrowCompartment pMessageConnectorArrowCompartment = (IMessageConnectorArrowCompartment) CreationFactoryHelper.createCompartment("MessageConnectorArrowCompartment");

				if (pNameCompartment != null && pMessageConnectorArrowCompartment != null) {
					pNameCompartment.setEngine(engine);
					pMessageConnectorArrowCompartment.setEngine(engine);

					pNameCompartment.addModelElement(pOperation, -1);
					pMessageConnectorArrowCompartment.addModelElement(m_pMessage, -1);

					//Clear compartments before adding. When reading from archive we end up with compartments. However the
					// call to deepsync calls this method after readfromarchive and causes duplicate comps
					clearCompartments();
					
					super.addCompartment(pNameCompartment, -1, false);
					super.addCompartment(pMessageConnectorArrowCompartment, -1, false);
				}
			}
		}

	}
	/**
	 * Returns the IMessage and IMessageConnector we represent
	 *
	 * @param pConnector [out] The message connector the link that owns this label represents
	 * @param pMessage [out] The message this label represents
	 */
	protected ETPairT < IMessageConnector, IMessage > getMetaData() {

		ETPairT < IMessageConnector, IMessage > retVal = new ETPairT < IMessageConnector, IMessage > ();

		IMessageConnector pConnector = null;
		IMessage pMessage = null;

		// The presentation element of the owning edge should be attached to the message connector 

		IETLabel owner = (IETLabel) this.getEngine().getParentETElement();

		IETGraphObject ownerEdge = null;
		
		if (owner != null) {
		  ownerEdge = owner.getParentETElement();
		}

		if (ownerEdge != null) {
			IElement pME = TypeConversions.getElement(ownerEdge);
			if (pME != null) {
				if (pME instanceof IMessageConnector){
					pConnector = (IMessageConnector)pME;
				}
			}
		}

		// The model element the draw engine is attached to is the message
		IDrawEngine pEngine = getEngine();

		if (pEngine != null) {

			IElement pEngineElement = TypeConversions.getElement(pEngine);

			if (pEngineElement != null) {
				if (pEngineElement instanceof IMessage){
					pMessage = (IMessage)pEngineElement;
				}
			}
		}

		retVal.setParamOne(pConnector);
		retVal.setParamTwo(pMessage);

		return retVal;

	}

}
