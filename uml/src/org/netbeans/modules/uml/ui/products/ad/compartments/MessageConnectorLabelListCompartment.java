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
