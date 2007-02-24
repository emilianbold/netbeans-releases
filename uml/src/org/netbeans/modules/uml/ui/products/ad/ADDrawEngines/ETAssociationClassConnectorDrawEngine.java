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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.ReconnectEdgeCreateConnectorKind;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;

import java.awt.Color;

/**
 * @author KevinM
 *
 */
public class ETAssociationClassConnectorDrawEngine extends ADNodeDrawEngine {

	protected final static int nAssociationClassSmallNodeSize = 11;
	/**
	 * 
	 */
	public ETAssociationClassConnectorDrawEngine() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#doDraw(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo)
	 */
	public void doDraw(IDrawInfo pDrawInfo) {
		if (pDrawInfo != null) {
			// Draw a dashed ellipse

			GDISupport.drawDashedEllipse(pDrawInfo.getTSEGraphics(), pDrawInfo.getDeviceBounds().getRectangle(), Color.BLACK, this.getBkColor());
		}

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getDrawEngineID()
	 */
	public String getDrawEngineID() {
		return "AssociationClassConnectorDrawEngine";
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#sizeToContents()
	 */
	public void sizeToContents() {
		try {
			if (this.getOwnerNode() != null) {
				resize(nAssociationClassSmallNodeSize, nAssociationClassSmallNodeSize, true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initCompartments(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
	 */
	public void initCompartments(IPresentationElement pElement) {
		// Bridges don't have compartments.
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#isDrawEngineValidForModelElement()
	 */
	public boolean isDrawEngineValidForModelElement() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#setParent(org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI)
	 */
	public void setParent(IETGraphObjectUI pParent) {
		super.setParent(pParent);
		if (pParent != null && this.getNodeUI() != null) {
			this.getNodeUI().setResizable(false);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getDrawEngineMatchID()
	 */
	public String getDrawEngineMatchID() {
		return null;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getManagerMetaType(int)
	 */
	public String getManagerMetaType(int nManagerKind) {
		String type = null;

		if (nManagerKind == MK_EVENTMANAGER) {
			type = "AssociationClassEventManager";
		} else if (nManagerKind == MK_LABELMANAGER) {
			type = "AssociationClassLabelManager";
		}
		return type;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine#getReconnectConnector(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
	 */
	public int getReconnectConnector( IPresentationElement pEdgePE )
	{
		return ReconnectEdgeCreateConnectorKind.RECCK_DONT_CREATE;
	}
}

