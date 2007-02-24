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
