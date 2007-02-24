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



package org.netbeans.modules.uml.ui.products.ad.drawengines;

import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;

/*
 *
 * @author KevinM
 *
 */
public class ETBridgeEdgeDrawEngine extends ETEdgeDrawEngine {
	public String getElementType() {
		String type = super.getElementType();
		if (type == null) {
			type = new String("Bridge Edge");
		}
		return type;
	}

	public void doDraw(IDrawInfo drawInfo) {
		super.doDraw(drawInfo);
	/*	try {
			int nBridgeKind = BridgeKind.BK_UNKNOWN;

			IETGraphObject pETElement = getParent().getOwner() instanceof IETGraphObject ? (IETGraphObject)getParent().getOwner() : null;

			if (pETElement != null) {
				nBridgeKind = pETElement.getBridgeKind();
			}

			if (nBridgeKind == BridgeKind.BK_ASSOCIATIONCLASS_DOTTEDLINE) {
				// These draw as dashed lines
				drawEdge(drawInfo, DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD, DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD, DrawEngineLineKindEnum.DELK_DASH);
			} else {
				super.doDraw(drawInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	*/
	}

	public String getDrawEngineID() {
		return "BridgeEdgeDrawEngine";
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IEdgeDrawEngine#getAllowReconnection()
	 */
	public boolean getAllowReconnection() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getDrawEngineMatchID()
	 */
	public String getDrawEngineMatchID() {
		return null;
	}

	/**
	* When a presentation element is selected and VK_DELETE is selected, the user is
	* asked if the data model should be affected as well.  Bridges are purely ornamental
	* so they shouldn't do anything
	*/
	public void affectModelElementDeletion() {
		return;
	}

}

