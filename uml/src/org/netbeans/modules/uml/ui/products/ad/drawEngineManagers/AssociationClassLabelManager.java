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


package org.netbeans.modules.uml.ui.products.ad.drawEngineManagers;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.common.generics.ETTripleT;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.ui.products.ad.drawengines.IAssociationEdgeDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IEventManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;

public class AssociationClassLabelManager extends AssociationLabelManager {

	static int sbCallOtherManagers = 0;

	public void modelElementHasChanged(INotificationTargets pTargets) {

		// Used to avoid recursion when calling other label managers
		sbCallOtherManagers = 0;
		sbCallOtherManagers++;

		// Call our base class then call the other label managers
		super.modelElementHasChanged(pTargets);

		if (sbCallOtherManagers == 1) {
			// If we are the primary caller then tell the other label managers to
			// create initial labels as well.

			ETTripleT < IAssociationClassLabelManager, IAssociationClassLabelManager, IAssociationClassLabelManager > result = this.getOtherLabelManagers();

			IAssociationClassLabelManager pEnd0EdgeMgr = result.getParamOne();
			IAssociationClassLabelManager pSmallNodeMgr = result.getParamTwo();
			IAssociationClassLabelManager pEnd1EdgeMgr = result.getParamThree();

			ILabelManager pUs = getLabelManagerInterface();

			if (pEnd0EdgeMgr != null && pEnd0EdgeMgr != pUs) {
				pEnd0EdgeMgr.modelElementHasChanged(pTargets);
			}
			if (pSmallNodeMgr != null && pSmallNodeMgr != pUs) {
				pSmallNodeMgr.modelElementHasChanged(pTargets);
			}
			if (pEnd1EdgeMgr != null && pEnd1EdgeMgr != pUs) {
				pEnd1EdgeMgr.modelElementHasChanged(pTargets);
			}
		}
		sbCallOtherManagers--;
	}

	public void showNameLabel(IAssociationEnd pEnd) {

		boolean bIsUs = false;

		IAssociationClassLabelManager pResponsibleLabelMgr = getResponsibleLabelManager(pEnd);

		if (pResponsibleLabelMgr == this) {
			bIsUs = true;
		}

		if (bIsUs) {
			super.showNameLabel(pEnd);
		} else if (pResponsibleLabelMgr != null) {

			pResponsibleLabelMgr.showNameLabel(pEnd);
		}
	}

	public void showMultiplicityLabel(IAssociationEnd pEnd) {

		boolean bIsUs = false;

		IAssociationClassLabelManager pResponsibleLabelMgr = getResponsibleLabelManager(pEnd);

		if (pResponsibleLabelMgr == this) {
			bIsUs = true;
		}

		if (bIsUs) {
			super.showMultiplicityLabel(pEnd);

		} else if (pResponsibleLabelMgr != null) {

			pResponsibleLabelMgr.showMultiplicityLabel(pEnd);
		}
	}

	// Create the various labels
	public void createAssociationNameLabel(boolean bAssignDefaultName) {

		boolean bIsUs = false;

		IAssociationClassLabelManager pResponsibleLabelMgr = getResponsibleLabelManager(TSLabelKind.TSLK_ASSOCIATION_NAME);

		if (pResponsibleLabelMgr == this) {
			bIsUs = true;
		}

		if (bIsUs) {
			super.createAssociationNameLabel(bAssignDefaultName);
		} else if (pResponsibleLabelMgr != null) {
			pResponsibleLabelMgr.createAssociationNameLabel(bAssignDefaultName);
		} else {

			ETSystem.out.println("Failed in AssociationClassLabelManager.createAssociationNameLabel()");
			//NL TODO dor debugging
			super.createAssociationNameLabel(bAssignDefaultName);

		}
	}

	public void createAssociationEndNameLabel(IAssociationEnd pEnd, boolean bAssignDefaultName) {

		boolean bIsUs = false;

		IAssociationClassLabelManager pResponsibleLabelMgr = getResponsibleLabelManager(pEnd);

		if (pResponsibleLabelMgr == this) {
			bIsUs = true;
		}

		if (bIsUs) {
			super.createAssociationEndNameLabel(pEnd, bAssignDefaultName);
		} else if (pResponsibleLabelMgr != null) {
			pResponsibleLabelMgr.createAssociationEndNameLabel(pEnd, bAssignDefaultName);
		} else {

			ETSystem.out.println("Failed in AssociationClassLabelManager.createAssociationEndNameLabel()");
			//NL TODO dor debugging
			super.createAssociationEndNameLabel(pEnd, bAssignDefaultName);

		}

	}

	public void createAssociationEndMultiplicityLabel(IAssociationEnd pEnd, boolean bAssignDefaultValue) {

		boolean bIsUs = false;

		IAssociationClassLabelManager pResponsibleLabelMgr = getResponsibleLabelManager(pEnd);

		if (pResponsibleLabelMgr == this) {
			bIsUs = true;
		}

		if (bIsUs) {
			super.createAssociationEndMultiplicityLabel(pEnd, bAssignDefaultValue);

		} else if (pResponsibleLabelMgr != null) {
			pResponsibleLabelMgr.createAssociationEndMultiplicityLabel(pEnd, bAssignDefaultValue);

		} else {
			ETSystem.out.println("Failed in AssociationClassLabelManager.createAssociationEndMultiplicityLabel()");
			//NL TODO dor debugging
			super.createAssociationEndMultiplicityLabel(pEnd, bAssignDefaultValue);

		}
	}

	// ILabelManager Overrides
	// Creates the initial labels on a newly created node or edge
	public void createInitialLabels() {

		// Used to avoid recursion when calling other label managers
		sbCallOtherManagers = 0;
		sbCallOtherManagers++;

		// Call our base class then call the other label managers
		super.createInitialLabels();

		if (sbCallOtherManagers == 1) {
			// If we are the primary caller then tell the other label managers to
			// create initial labels as well.

			ETTripleT < IAssociationClassLabelManager, IAssociationClassLabelManager, IAssociationClassLabelManager > result = this.getOtherLabelManagers();

			IAssociationClassLabelManager pEnd0EdgeMgr = result.getParamOne();
			IAssociationClassLabelManager pSmallNodeMgr = result.getParamTwo();
			IAssociationClassLabelManager pEnd1EdgeMgr = result.getParamThree();

			ILabelManager pUs = getLabelManagerInterface();

			if (pEnd0EdgeMgr != null && pEnd0EdgeMgr != pUs) {
				pEnd0EdgeMgr.createInitialLabels();
			}
			if (pSmallNodeMgr != null && pSmallNodeMgr != pUs) {
				pSmallNodeMgr.createInitialLabels();
			}
			if (pEnd1EdgeMgr != null && pEnd1EdgeMgr != pUs) {
				pEnd1EdgeMgr.createInitialLabels();
			}
		}
		sbCallOtherManagers--;
	}

	public void resetLabelsText() {

		// Used to avoid recursion when calling other label managers
		sbCallOtherManagers = 0;
		sbCallOtherManagers++;

		// Call our base class then call the other label managers
		super.resetLabelsText();

		if (sbCallOtherManagers == 1) {
			// If we are the primary caller then tell the other label managers to
			// reset as well.

			ETTripleT < IAssociationClassLabelManager, IAssociationClassLabelManager, IAssociationClassLabelManager > result = this.getOtherLabelManagers();

			IAssociationClassLabelManager pEnd0EdgeMgr = result.getParamOne();
			IAssociationClassLabelManager pSmallNodeMgr = result.getParamTwo();
			IAssociationClassLabelManager pEnd1EdgeMgr = result.getParamThree();

			ILabelManager pUs = getLabelManagerInterface();

			if (pEnd0EdgeMgr != null && pEnd0EdgeMgr != pUs) {
				pEnd0EdgeMgr.resetLabelsText();
			}
			if (pSmallNodeMgr != null && pSmallNodeMgr != pUs) {
				pSmallNodeMgr.resetLabelsText();
			}
			if (pEnd1EdgeMgr != null && pEnd1EdgeMgr != pUs) {
				pEnd1EdgeMgr.resetLabelsText();
			}
		}
		sbCallOtherManagers--;
	}

	public void showLabel(int nLabelKind, boolean bShow) {

		boolean bIsUs = false;

		IAssociationClassLabelManager pResponsibleLabelMgr = getResponsibleLabelManager(nLabelKind);

		if (pResponsibleLabelMgr == this) {
			bIsUs = true;
		}
		if (bIsUs) {
			super.showLabel(nLabelKind, bShow);
			
		} else if (pResponsibleLabelMgr != null) {
			pResponsibleLabelMgr.showLabel(nLabelKind, bShow);
		}
	}

	// Does this label manager know how to display this label?
	public boolean isValidLabelKind(int nLabelKind) {

		boolean bIsValid = false;

		if (nLabelKind == TSLabelKind.TSLK_ASSOCIATION_NAME
			|| nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END0_ROLE_NAME
			|| nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END1_ROLE_NAME
			|| nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END0_MULTIPLICITY
			|| nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END1_MULTIPLICITY
			|| nLabelKind == TSLabelKind.TSLK_STEREOTYPE) {
			bIsValid = true;
		}
		return bIsValid;
	}

	// Used to restore the edit text for multiplicities which my look like 1..1 back to just '1'
	public void handleEditNoChange(IETLabel pLabel, String sNewString) {

		int nLabelKind = TSLabelKind.TSLK_UNKNOWN;

		nLabelKind = pLabel.getLabelKind();

		if (nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END0_MULTIPLICITY || nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END1_MULTIPLICITY) {

			boolean bIsUs = false;

			IAssociationClassLabelManager pResponsibleLabelMgr = getResponsibleLabelManager(nLabelKind);

			if (pResponsibleLabelMgr == this) {
				bIsUs = true;
			}

			if (bIsUs) {
				super.resetLabelsText();

			} else if (pResponsibleLabelMgr != null) {

				pResponsibleLabelMgr.resetLabelsText();
			}
		}
	}

	public void handleEditChange(IETLabel pLabel, String sNewString) {

		int nLabelKind = TSLabelKind.TSLK_UNKNOWN;

		nLabelKind = pLabel.getLabelKind();

		if (nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END0_MULTIPLICITY || nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END1_MULTIPLICITY) {

			boolean bIsUs = false;

			IAssociationClassLabelManager pResponsibleLabelMgr = getResponsibleLabelManager(nLabelKind);

			if (pResponsibleLabelMgr == this) {
				bIsUs = true;
			}

			if (bIsUs) {
				super.resetLabelsText();

			} else if (pResponsibleLabelMgr != null) {

				pResponsibleLabelMgr.resetLabelsText();
			}
		}
	}

	// Is this label displayed?
	public boolean isDisplayed(int nLabelKind) {

		boolean retValue = false;

		boolean bIsUs = false;

		IAssociationClassLabelManager pResponsibleLabelMgr = getResponsibleLabelManager(nLabelKind);

		if (pResponsibleLabelMgr == this) {
			bIsUs = true;
		}

		if (bIsUs) {
			retValue = super.isDisplayed(nLabelKind);

		} else if (pResponsibleLabelMgr != null) {

			retValue = pResponsibleLabelMgr.isDisplayed(nLabelKind);
		}

		return retValue;
	}

	// Returns the label manager responsible for that type of label
	protected IAssociationClassLabelManager getResponsibleLabelManager(int nLabelKind) {

		IAssociationClassLabelManager pResponsibleLabelMgr = null;

		// Get the correct label manager and forward the request to that guy

		ETTripleT < IAssociationClassLabelManager, IAssociationClassLabelManager, IAssociationClassLabelManager > result = this.getOtherLabelManagers();

		IAssociationClassLabelManager pEnd0EdgeMgr = result.getParamOne();
		IAssociationClassLabelManager pSmallNodeMgr = result.getParamTwo();
		IAssociationClassLabelManager pEnd1EdgeMgr = result.getParamThree();

		if (nLabelKind == TSLabelKind.TSLK_ASSOCIATION_NAME || nLabelKind == TSLabelKind.TSLK_STEREOTYPE) {
			pResponsibleLabelMgr = pSmallNodeMgr;
		} else if (nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END0_ROLE_NAME || nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END0_MULTIPLICITY) {
			pResponsibleLabelMgr = pEnd0EdgeMgr;
		} else if (nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END1_ROLE_NAME || nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END1_MULTIPLICITY) {
			pResponsibleLabelMgr = pEnd1EdgeMgr;
		}
		return pResponsibleLabelMgr;

	}

	// Returns the label manager responsible for this end
	protected IAssociationClassLabelManager getResponsibleLabelManager(IAssociationEnd pEnd) {

		IAssociationClassLabelManager pResponsibleLabelMgr = null;

		// Get the correct label manager and forward the request to that guy

		ETTripleT < IAssociationClassLabelManager, IAssociationClassLabelManager, IAssociationClassLabelManager > result = this.getOtherLabelManagers();

		IAssociationClassLabelManager pEnd0EdgeMgr = result.getParamOne();
		IAssociationClassLabelManager pSmallNodeMgr = result.getParamTwo();
		IAssociationClassLabelManager pEnd1EdgeMgr = result.getParamThree();

		IAssociation pAssoc = pEnd.getAssociation();

		if (pAssoc != null) {
			int nIndex = 0;

			nIndex = pAssoc.getEndIndex(pEnd);

			if (nIndex == 0) {
				pResponsibleLabelMgr = pEnd0EdgeMgr;
			} else if (nIndex == 1) {
				pResponsibleLabelMgr = pEnd1EdgeMgr;
			}
		}
		return pResponsibleLabelMgr;
	}

	// Returns the label managers associated with the other segments
	protected ETTripleT < IAssociationClassLabelManager, IAssociationClassLabelManager, IAssociationClassLabelManager > getOtherLabelManagers() {

		IAssociationClassLabelManager pSourceEdgeMgr = null;
		IAssociationClassLabelManager pSmallNodeMgr = null;
		IAssociationClassLabelManager pTargetEdgeMgr = null;

		IDrawEngine pDrawEngine = TypeConversions.getDrawEngine(m_rawParentETGraphObject);

		if (pDrawEngine != null) {

			IEventManager pEventManager = pDrawEngine.getEventManager();

			IAssociationClassEventManager pAssocEventManager = (IAssociationClassEventManager) pEventManager;

			if (pEventManager != null) {
				IBridgeElements bridgeElements = pAssocEventManager.getBridgeElements();
				
				IETGraphObject pSourceEdge = bridgeElements.getSourceEdge();
				IETGraphObject pSmallNode = bridgeElements.getSmallNode();
				IETGraphObject pTargetEdge = bridgeElements.getTargetEdge();
				IETGraphObject pDottedEdge = bridgeElements.getDottedEdge();
				IETGraphObject pSourceNode = bridgeElements.getSourceNode();
				IETGraphObject pTargetNode = bridgeElements.getTargetNode();

				IDrawEngine pTempDE = null;

				ILabelManager pTempLabelManager = null;

				//NL TODO This following  won'twork until getBridgeElements is converted properly in order to return the bridge elements in java
				//pAssocEventManager.getBridgeElements(pSourceEdge, pSmallNode, pTargetEdge, pDottedEdge, pSourceNode, pTargetNode);

				// Now return the label managers

				///
				// Source Edge could be either end #0 or end #1 we need to figure out which
				///
				if (pSourceEdge != null && pTargetEdge != null) {
					pTempDE = null;
					pTempLabelManager = null;
					int nSourceIndex = 0;

					pTempDE = TypeConversions.getDrawEngine(pSourceEdge);

					IAssociationEdgeDrawEngine pAssocDrawEngine = (IAssociationEdgeDrawEngine) pTempDE;

					if (pAssocDrawEngine != null) {

						ETTripleT < IAssociationEnd, IAssociationEnd, Integer > result = pAssocDrawEngine.getAssociationEnd();
						IAssociationEnd pThisEnd = result.getParamOne();
						IAssociationEnd pOtherEnd = result.getParamTwo();
						nSourceIndex = result.getParamThree().intValue();

						pTempLabelManager = pTempDE.getLabelManager();

						if (pTempLabelManager != null) {
							if (nSourceIndex == 0) {
								pSourceEdgeMgr = (IAssociationClassLabelManager) pTempLabelManager;
							} else {
								pTargetEdgeMgr = (IAssociationClassLabelManager) pTempLabelManager;
							}
						}
					}

					///
					// Target Edge
					///
					pTempDE = null;
					pTempLabelManager = null;
					pTempDE = TypeConversions.getDrawEngine(pTargetEdge);

					if (pTempDE != null) {
						pTempLabelManager = pTempDE.getLabelManager();

						if (pTempLabelManager != null) {
							if (pSourceEdgeMgr == null) {
								pSourceEdgeMgr = (IAssociationClassLabelManager) pTempLabelManager;
							} else {
								pTargetEdgeMgr = (IAssociationClassLabelManager) pTempLabelManager;
							}
						}
					}
				}

				///
				// Small Node
				///
				pTempDE = null;
				pTempLabelManager = null;
				pTempDE = TypeConversions.getDrawEngine(pSmallNode);

				if (pTempDE != null) {
					pTempLabelManager = pTempDE.getLabelManager();

					if (pTempLabelManager != null) {
						pSmallNodeMgr = (IAssociationClassLabelManager) pTempLabelManager;
					}
				}
			}
		}
		return new ETTripleT < IAssociationClassLabelManager, IAssociationClassLabelManager, IAssociationClassLabelManager > (pSourceEdgeMgr, pSmallNodeMgr, pTargetEdgeMgr);

	}

}
