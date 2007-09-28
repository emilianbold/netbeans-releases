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


package org.netbeans.modules.uml.ui.products.ad.ADDrawEngines;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.ProductButtonHandler;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;

/**
 * @author KevinM
 *
 */
public class ADDrawEngineButtonHandler extends ProductButtonHandler implements IADDrawEngineButtonHandler {

	protected ADDrawEngineButtonHandler()
	{		
		this(null);
	}
	
	public ADDrawEngineButtonHandler(IDrawEngine drawEngine)
	{
		super();
		m_parentDrawEngine = drawEngine;		
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADDrawEngineButtonHandler#addPortMenuItems(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu)
	 */
	public void addPortMenuItems(IDrawEngine pDrawEngine, IProductContextMenu pContextMenu) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADDrawEngineButtonHandler#addCustomizeMenuItems(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu)
	 */
	public void addCustomizeMenuItems(IProductContextMenu pContextMenu) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADDrawEngineButtonHandler#addPseudoStateMenuItems(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu)
	 */
	public void addPseudoStateMenuItems(IProductContextMenu pContextMenu) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADDrawEngineButtonHandler#addFinalStateMenuItems(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu)
	 */
	public void addFinalStateMenuItems(IProductContextMenu pContextMenu) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADDrawEngineButtonHandler#addEventTransitionMenuItems(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, boolean)
	 */
	public void addEventTransitionMenuItems(IProductContextMenu pContextMenu, boolean bShow) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADDrawEngineButtonHandler#addInterfaceEdgeMenuItems(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu)
	 */
	public void addInterfaceEdgeMenuItems(IProductContextMenu pContextMenu) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADDrawEngineButtonHandler#addAssociationAndAggregationEdgeMenuItems(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
	 */
	public void addAssociationAndAggregationEdgeMenuItems(IProductContextMenu pContextMenu, IElement pLinkElement) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADDrawEngineButtonHandler#addActivityEdgeMenuItems(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
	 */
	public void addActivityEdgeMenuItems(IProductContextMenu pContextMenu, IElement pLinkElement) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADDrawEngineButtonHandler#addTransitionEdgeMenuItems(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
	 */
	public void addTransitionEdgeMenuItems(IProductContextMenu pContextMenu, IElement pLinkElement) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADDrawEngineButtonHandler#addAssociationEndSetMultiplicityMenuItems(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu)
	 */
	public void addAssociationEndSetMultiplicityMenuItems(IProductContextMenu pContextMenu) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADDrawEngineButtonHandler#addAssociationMultiLabelSelectionsPullright(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, boolean)
	 */
	public void addAssociationMultiLabelSelectionsPullright(IProductContextMenu pContextMenu, boolean bInMiddle) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADDrawEngineButtonHandler#addAssociationEndLabelsPullright(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu)
	 */
	public void addAssociationEndLabelsPullright(IProductContextMenu pContextMenu) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADDrawEngineButtonHandler#addQualifiersButton(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu)
	 */
	public void addQualifiersButton(IProductContextMenu pContextMenu) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADDrawEngineButtonHandler#addNameLabelPullright(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu)
	 */
	public void addNameLabelPullright(IDrawEngine pEngine, IProductContextMenu pContextMenu) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADDrawEngineButtonHandler#addStereotypeLabelPullright(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu)
	 */
	public void addStereotypeLabelPullright(IDrawEngine pEngine, IProductContextMenu pContextMenu) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADDrawEngineButtonHandler#addBindLabelPullright(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu)
	 */
	public void addBindLabelPullright(IProductContextMenu pContextMenu) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADDrawEngineButtonHandler#addRepresentPartButtons(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu)
	 */
	public void addRepresentPartButtons(IProductContextMenu pContextMenu) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADDrawEngineButtonHandler#addGraphicShapePullright(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu)
	 */
	public void addGraphicShapePullright(IProductContextMenu pContextMenu) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADDrawEngineButtonHandler#addContainmentOnOffPullright(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu)
	 */
	public void addContainmentOnOffPullright(IProductContextMenu pContextMenu) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADDrawEngineButtonHandler#parentDiagramIsReadOnly()
	 */
	public boolean parentDiagramIsReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADDrawEngineButtonHandler#setDrawEngine(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine)
	 */
	public void setDrawEngine(IDrawEngine drawengine) {
		m_parentDrawEngine = drawengine;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADDrawEngineButtonHandler#getDrawEngine()
	 */
	public IDrawEngine getDrawEngine() {
		return m_parentDrawEngine;
	}
	
	protected IDrawEngine m_parentDrawEngine;
}
