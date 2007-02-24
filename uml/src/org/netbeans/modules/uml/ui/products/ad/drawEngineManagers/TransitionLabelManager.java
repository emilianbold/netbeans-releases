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

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.ui.support.NodeEndKindEnum;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelPlacementKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;

public class TransitionLabelManager extends ADLabelManager {

	private static final int CK_PRE = 0;
	private static final int CK_POST = 1;

	public TransitionLabelManager() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#modelElementHasChanged(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
	 */
	public void modelElementHasChanged(INotificationTargets pTargets) {
		resetLabelsText();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#createInitialLabels()
	 */
	public void createInitialLabels() {

		boolean bCurrentlyShown = false;

		// Get the name text to be displayed
		String sName = getNameText(false);

		if (sName != null && sName.length() > 0) {
			bCurrentlyShown = isDisplayed(TSLabelKind.TSLK_NAME);

			if (!bCurrentlyShown) {
				createLabelIfNotEmpty(sName, TSLabelKind.TSLK_NAME, TSLabelPlacementKind.TSLPK_CENTER_BELOW, null);
			}
			sName = "";
		}

		bCurrentlyShown = isDisplayed(TSLabelKind.TSLK_STEREOTYPE);

		if (!bCurrentlyShown) {
			// Get the text to be displayed
			sName = "";

			sName = getStereotypeText();

			if (sName != null && sName.length() > 0) {
				createLabelIfNotEmpty(sName, TSLabelKind.TSLK_STEREOTYPE, TSLabelPlacementKind.TSLPK_CENTER_BELOW, null);
			}
		}

		// Make sure the text is ok
		resetLabelsText();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#isValidLabelKind(int)
	 */
	public boolean isValidLabelKind(int nLabelKind) {

		boolean bIsValid = false;

		if (nLabelKind == TSLabelKind.TSLK_STEREOTYPE || nLabelKind == TSLabelKind.TSLK_NAME || nLabelKind == TSLabelKind.TSLK_PRE_CONDITION || nLabelKind == TSLabelKind.TSLK_POST_CONDITION) {
			bIsValid = true;
		}
		return bIsValid;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#resetLabelsText()
	 */
	public void resetLabelsText() {

		boolean done = false;
		int index = 0;

		boolean bDoLayout = false;

		IElement pElement = getModelElement();

		// Go through all the product labels and re-get their text.
		while (!done) {
			IETLabel pETLabel = this.getETLabelbyIndex(index);

			if (pETLabel != null) {

				String sText = "";

				int nLabelKind = TSLabelKind.TSLK_UNKNOWN;

				nLabelKind = pETLabel.getLabelKind();

				if (nLabelKind == TSLabelKind.TSLK_STEREOTYPE) {
					sText = getStereotypeText();
				} else if (nLabelKind == TSLabelKind.TSLK_NAME) {
					sText = getNameText(false);
				} else if (nLabelKind == TSLabelKind.TSLK_PRE_CONDITION) {

					ETPairT < IConstraint, String > result = getConditionText(CK_PRE, false);
					IConstraint pConstraint = result.getParamOne();
					sText = result.getParamTwo();

				} else if (nLabelKind == TSLabelKind.TSLK_POST_CONDITION) {
					ETPairT < IConstraint, String > result = getConditionText(CK_POST, false);

					IConstraint pConstraint = result.getParamOne();
					sText = result.getParamTwo();
				}

				// Here's where we set the text of the label
				String sOldText = pETLabel.getText();

				if (sText != null && sText.length() > 0) {
					if (!(sText.equals(sOldText))) {
						pETLabel.setText(sText);
						pETLabel.reposition();

						bDoLayout = true;
					}
					pETLabel.sizeToContents();
				} else {
					// If there is no text then remove the label
					removeETLabel(index);
				}
			} else {
				done = true;
			}
			index++;
		}

		if (bDoLayout) {
			// Relayout the labels
			relayoutLabels();
		}

		invalidate();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#showLabel(int, boolean)
	 */
	public void showLabel(int nLabelKind, boolean bShow) {

		// See if it's already shown
		boolean bCurrentlyShown = false;

		bCurrentlyShown = isDisplayed(nLabelKind);

		if ((bCurrentlyShown && bShow) || (!bCurrentlyShown && !bShow)) {
			// We have nothing to do!
		} else {
			if (nLabelKind == TSLabelKind.TSLK_STEREOTYPE) {
				if (bShow) {
					showStereotypeLabel();
				} else {
					discardLabel(nLabelKind);
					invalidate();
				}
			} else if (nLabelKind == TSLabelKind.TSLK_NAME) {
				if (bShow) {
					createNameLabel(true);
				} else {
					discardLabel(nLabelKind);
					invalidate();
				}
			} else if (nLabelKind == TSLabelKind.TSLK_PRE_CONDITION) {
				if (bShow) {
					createConditionLabel(CK_PRE, true);
				} else {
					discardLabel(nLabelKind);
					invalidate();
				}
			} else if (nLabelKind == TSLabelKind.TSLK_POST_CONDITION) {
				if (bShow) {
					createConditionLabel(CK_POST, true);
				} else {
					discardLabel(nLabelKind);
					invalidate();
				}
			}
		}
	}

	// The transition conditions don't update.  If users doubleclick we may want to resize
	public void handleEditNoChange(IETLabel pLabel, String sNewString) {

		int nLabelKind = TSLabelKind.TSLK_UNKNOWN;

		nLabelKind = pLabel.getLabelKind();

		if (nLabelKind == TSLabelKind.TSLK_PRE_CONDITION || nLabelKind == TSLabelKind.TSLK_POST_CONDITION) {
			resetLabelsText();
		}
	}

	// Creates the name label
	protected void createNameLabel(boolean bAssignDefaultName) {

		boolean bCurrentlyShown = false;

		bCurrentlyShown = isDisplayed(TSLabelKind.TSLK_NAME);

		if (!bCurrentlyShown) {
			String sName = getNameText(bAssignDefaultName);

			if (sName != null && sName.length() > 0) {
				createLabelIfNotEmpty(sName, TSLabelKind.TSLK_NAME, TSLabelPlacementKind.TSLPK_CENTER_ABOVE, null);
			}
		}
	}

	// Returns the ITransition pre or post condition text
	protected ETPairT < IConstraint, String > getConditionText(int nKind, boolean bAssignDefaultValue) {

		String sText = "";
		IConstraint pConstraint = null;

		String sGuardWithBrackets;
		ITransition pTransition = getTransition();

		if (pTransition != null) {
			IConstraint pFoundConstraint = null;

			if (nKind == this.CK_PRE) {
				pFoundConstraint = pTransition.getPreCondition();
			} else {
				pFoundConstraint = pTransition.getPostCondition();
			}

			if (pFoundConstraint != null) {
				String sExpText = "";

				sExpText = pFoundConstraint.getExpression();
				pConstraint = pFoundConstraint;

				// Assign a default value if we're told to
				if (/*bAssignDefaultValue Need this because label gets deleted when sExpText=""  && */ (sExpText == null || sExpText.length() == 0)) {
					sExpText = "  ";
				}

				if (sExpText != null && sExpText.length() > 0) {
					String sConstraintText = "";

					// These should be {'s but the languages file
					// can't handle both [ (activity edge) and { right now.
					sConstraintText += "[";
					sConstraintText += sExpText;
					sConstraintText += "]";

					sText = sConstraintText;
				}
			}
		}

		return new ETPairT < IConstraint, String > (pConstraint, sText);
	}

	// Creates the condition label
	protected void createConditionLabel(int nKind, boolean bAssignDefaultValue) {

		String sCondition = "";
		ITransition pTransition = null;
		IConstraint pConstraint = null;

		// Get the transition
		pTransition = getTransition();

		// Get the text to display
		ETPairT < IConstraint, String > result = getConditionText(nKind, bAssignDefaultValue);

		pConstraint = result.getParamOne();
		sCondition = result.getParamTwo();

		if (sCondition.length() > 0 && pTransition != null && pConstraint != null) {
			// Find the node where the condition is located.
			IEdgePresentation pThisEdgePresentation = TypeConversions.getEdgePresentation(m_rawParentETGraphObject);

			if (pThisEdgePresentation != null) {
				IStateVertex pAssociatedVertex;
				int nLabelKind;
				int nLabelPlacement;
				int nEndKind = NodeEndKindEnum.NEK_UNKNOWN;

				// Get the vertext
				if (nKind == CK_PRE) {
					pAssociatedVertex = pTransition.getSource();
					nLabelKind = TSLabelKind.TSLK_PRE_CONDITION;
				} else {
					pAssociatedVertex = pTransition.getTarget();

					nLabelKind = TSLabelKind.TSLK_POST_CONDITION;
				}

				// Get the location of the node source or target
				if (pAssociatedVertex != null) {
					nEndKind = pThisEdgePresentation.getNodeEnd(pAssociatedVertex);
				}

				if (nEndKind == NodeEndKindEnum.NEK_FROM || nEndKind == NodeEndKindEnum.NEK_BOTH) {
					nLabelPlacement = TSLabelPlacementKind.TSLPK_FROM_NODE_BELOW;
					createLabelIfNotEmpty(sCondition, nLabelKind, nLabelPlacement, pConstraint);
				} else if (nEndKind == NodeEndKindEnum.NEK_TO) {
					nLabelPlacement = TSLabelPlacementKind.TSLPK_TO_NODE_BELOW;
					createLabelIfNotEmpty(sCondition, nLabelKind, nLabelPlacement, pConstraint);
				}
			}
		}
	}

	// Returns the transition
	protected ITransition getTransition() {

		ITransition retValue = null;

		IElement pElement = getModelElement();

		ITransition pTransition = (ITransition) pElement;

		if (pTransition != null) {
			retValue = pTransition;
		}

		return retValue;
	}

}
