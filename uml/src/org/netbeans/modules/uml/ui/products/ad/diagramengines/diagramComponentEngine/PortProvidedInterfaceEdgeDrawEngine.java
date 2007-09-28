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


