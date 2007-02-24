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



package org.netbeans.modules.uml.ui.products.ad.diagramengines.diagramComponentEngine;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IPort;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.ADEdgeDrawEngine;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineArrowheadKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ModelElementChangedKind;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

/**
 * @author josephg
 *
 */
public class PortProvidedInterfaceEdgeDrawEngine extends ADEdgeDrawEngine {
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getDrawEngineID()
	 */
	public String getDrawEngineID() {
		return "PortProvidedInterfaceEdgeDrawEngine";
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawengines.ETEdgeDrawEngine#getEndArrowKind()
	 */
	protected int getEndArrowKind() {
		return DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawengines.ETEdgeDrawEngine#getLineKind()
	 */
	protected int getLineKind() {
		return DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawengines.ETEdgeDrawEngine#getStartArrowKind()
	 */
	protected int getStartArrowKind() {
		return DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#isDrawEngineValidForModelElement()
	 */
	public boolean isDrawEngineValidForModelElement() {
		return getMetaTypeOfElement().compareTo("Interface")==0;
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IEdgeDrawEngine#getAllowReconnection()
	 */
	public boolean getAllowReconnection() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#affectModelElementDeletion()
	 */
	public void affectModelElementDeletion() {
		ETPairT<IPort,IInterface> elements = getRelationshipElements();
		if(elements != null) {
			elements.getParamOne().removeProvidedInterface(elements.getParamTwo());
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#modelElementHasChanged(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
	 */
	public long modelElementHasChanged(INotificationTargets pTargets) {
		if(pTargets == null)
			return 0;
			
		IElement modelElement =pTargets.getChangedModelElement();
		int nKind = pTargets.getKind();
		
		if(nKind == ModelElementChangedKind.MECK_ELEMENTADDEDTONAMESPACE) {
			IEdgePresentation thisEdgePresentation = this.getEdgePresentationElement();
			
			if(thisEdgePresentation != null) {
				if(!thisEdgePresentation.validateLinkEnds()) {
					IDrawingAreaControl control = getDrawingArea();
					if( control != null ) {
						control.postDeletePresentationElement(thisEdgePresentation);
					}
				}
			}
		}
		return 0;
	}

	protected ETPairT<IPort,IInterface> getRelationshipElements() {
		IPort retPort = null;
		IInterface retInterface = null;
		IEdgePresentation thisEdgePresentation = getEdgePresentationElement();
		if(thisEdgePresentation != null) {
			ETPairT<IElement,IElement> endElements = thisEdgePresentation.getEdgeFromAndToElement(false);
			
			if(endElements.getParamOne() instanceof IPort) {
				retPort = (IPort)endElements.getParamOne();
				if(endElements.getParamTwo() instanceof IInterface) {
					retInterface = (IInterface)endElements.getParamTwo();			
				}
			}
			else {
				if(endElements.getParamTwo() instanceof IPort) {
					retPort = (IPort)endElements.getParamTwo();
					if(endElements.getParamOne() instanceof IInterface) {
						retInterface = (IInterface)endElements.getParamOne();
					}
				}
			}
		}
		if(retPort != null && retInterface != null)
			return new ETPairT<IPort,IInterface>(retPort,retInterface);
			
		return null;
	}
}


