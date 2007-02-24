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

import java.awt.Color;
import java.awt.event.ActionEvent;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.support.NodeEndKindEnum;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineArrowheadKindEnum;

/*
 * 
 * @author KevinM
 *
 */
public class ETGeneralizationEdgeDrawEngine extends ETEdgeDrawEngine {
	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getElementType()
	 */
	public String getElementType() {
		String type = super.getElementType();
		if (type == null) {
			type = new String("Generalization");
		}
		return type;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#doDraw(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo)
	 */
	public void doDraw(IDrawInfo drawInfo) {
		try {
			boolean bDidDraw = false;
			// Color edgeColor = GetColor(GetResourceID(CK_BORDERCOLOR));
			Color edgeColor = getLineColor();
			// At extreeme zoom levels we don't want to hit the metadata, so just
			// draw a line.  SimpleDrawEdge looks at the zoom and draws just an edge
			// if the user can't see an arrowhead anyway.
			if (simpleDrawEdge(drawInfo, this.getLineKind()) == false) {
				IEdgePresentation pPE = this.getEdgePresentationElement();
				IElement pModelElement = pPE != null ? pPE.getFirstSubject() : null;

				IGeneralization pGeneralization = pModelElement instanceof IGeneralization ? (IGeneralization) pModelElement : null;

				if (pGeneralization != null) {
					String sGeneralXMIID = pGeneralization.getGeneralXMIID();

					// See what end the general is on (the one with the arrowhead)
					int nEndKind = pPE.getNodeEnd3(sGeneralXMIID);

					int startArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
					int endArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
					if (nEndKind == NodeEndKindEnum.NEK_FROM || nEndKind == NodeEndKindEnum.NEK_BOTH) {
						startArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_FILLED_WHITE;
					} else if (nEndKind == NodeEndKindEnum.NEK_TO) {
						endArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_FILLED_WHITE;
					}

					drawEdge(drawInfo, startArrowheadKind, endArrowheadKind, getLineKind());
					bDidDraw = true;
				}
			} else {
				bDidDraw = true;
			}

			if (bDidDraw == false) {
				super.doDraw(drawInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawengines.ETEdgeDrawEngine#getEndArrowKind()
	 */
	protected int getEndArrowKind() {
		return DrawEngineArrowheadKindEnum.DEAK_FILLED_WHITE;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#onContextMenu(org.netbeans.modules.uml.ui.products.ad.application.IMenuManager)
	 */
	public void onContextMenu(IMenuManager manager) {
		// Add the stereotype label pullright
		addStandardLabelsToPullright(StandardLabelKind.SLK_ALL, manager);
		super.onContextMenu(manager);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.application.action.IETContextMenuHandler#setSensitivityAndCheck(java.lang.String, org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass)
	 */
	public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pClass) {
		boolean retVal = handleStandardLabelSensitivityAndCheck(id, pClass);
		if (!retVal) {
			super.setSensitivityAndCheck(id, pClass);
		}
		return retVal;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.application.action.IETContextMenuHandler#onHandleButton(java.awt.event.ActionEvent, java.lang.String)
	 */
	public boolean onHandleButton(ActionEvent e, String id) {
		boolean handled = handleStandardLabelSelection(e, id);
		if (!handled) {
			handled = super.onHandleButton(e, id);
		}
		return handled;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getDrawEngineID()
	 */
	public String getDrawEngineID() {
		return "GeneralizationEdgeDrawEngine";
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawengines.ETDrawEngine#getManagerMetaType(int)
	 */
	public String getManagerMetaType(int nManagerKind) {
		String sManager = null;

		if (nManagerKind == MK_LABELMANAGER) {
			sManager = "SimpleStereotypeAndNameLabelManager";
		}

		return sManager;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#isDrawEngineValidForModelElement()
	 */
	public boolean isDrawEngineValidForModelElement() {
		boolean bIsValid = false;
		try {
			String currentMetaType = getMetaTypeOfElement();
			if (currentMetaType != null && currentMetaType.equals("Generalization")) {
				bIsValid = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bIsValid;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initResources()
	 */
	public void initResources()
	{
		this.setLineColor("generalizationedgecolor", Color.BLACK);
		super.initResources();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IEdgeDrawEngine#verifyEdgeEnds()
	 */
	public void verifyEdgeEnds() {
		try {
			IEdgePresentation pPE = this.getEdgePresentationElement();
			IElement pModelElement = pPE != null ? pPE.getFirstSubject() : null;

			IGeneralization pGeneralization = pModelElement instanceof IGeneralization ? (IGeneralization) pModelElement : null;
			if (pGeneralization != null) {
				IClassifier pGeneral = pGeneralization.getGeneral();

				// See what end the general is on (the one with the arrowhead)
				int nEndKind = pPE.getNodeEnd(pGeneral);

				if (nEndKind == NodeEndKindEnum.NEK_TO) {
					if (!parentDiagramIsReadOnly()) {
						// This is wrong.  It ends up with an incorrect parent/child relationship.  
						// Switch it unless the diagram is readonly
						postSwapEdgeEnds();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
