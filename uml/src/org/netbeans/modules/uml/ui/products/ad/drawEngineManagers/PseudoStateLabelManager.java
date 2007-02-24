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

import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IPseudoState;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.preferenceframework.IPreferenceAccessor;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelPlacementKind;

public class PseudoStateLabelManager extends ADLabelManager {

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#createInitialLabels()
	 */
	public void createInitialLabels() {

		boolean bIsDisplayed = false;

		// Get the name text to be displayed
		String sName = getNameText(false);

		if (sName != null && sName.length() > 0) {
			//			CComPtr< IPreferenceManager > prefMan;
			//			_VH( prefMan.CoCreateInstance( __uuidof( PreferenceManager )));

			// Make sure the name isn't unnamed
			IPreferenceAccessor prefMan = PreferenceAccessor.instance();

			String sDefaultName = prefMan != null ?  prefMan.getDefaultElementName() : "";

			bIsDisplayed = isDisplayed(TSLabelKind.TSLK_NAME);

			if (!bIsDisplayed && !(sName == sDefaultName)) {
				createLabelIfNotEmpty(sName, TSLabelKind.TSLK_NAME, TSLabelPlacementKind.TSLPK_CENTER_ABOVE, null);
			}
			sName = "";
		}

		// Make sure the text is ok
		resetLabelsText();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#isValidLabelKind(int)
	 */
	public boolean isValidLabelKind(int nLabelKind) {
		return nLabelKind == TSLabelKind.TSLK_NAME || nLabelKind == TSLabelKind.TSLK_STEREOTYPE;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#resetLabelsText()
	 */
	public void resetLabelsText() {

		boolean done = false;
		int index = 0;
		boolean bDoLayout = false;

		IElement pElement = getModelElement();

		IPseudoState pPseudoState = (IPseudoState) pElement;

		if (pPseudoState != null) {
			// Go through all the product labels and re-get their text.
			while (!done) {
				IETLabel pETLabel = this.getETLabelbyIndex(index);

				if (pETLabel != null) {
					String sText;

					int nLabelKind  = pETLabel.getLabelKind();

					if (nLabelKind == TSLabelKind.TSLK_NAME) {
						// Get the name
						sText = getNameText(false);
					} else if (nLabelKind == TSLabelKind.TSLK_STEREOTYPE) {
						sText = getStereotypeText();
					}
					else
						sText = "";

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
		boolean bCurrentlyShown = isDisplayed(nLabelKind);

		if ((bCurrentlyShown && bShow) || (!bCurrentlyShown && !bShow)) {
			// We have nothing to do!
		} else {
			IElement pElement = getModelElement();

			IPseudoState pPseudoState = pElement instanceof IPseudoState ? (IPseudoState) pElement : null;
			if (pPseudoState != null) {
				if (nLabelKind == TSLabelKind.TSLK_NAME) {
					if (bShow) {
						createNameLabel(true);
					} else {
						discardLabel(nLabelKind);
						invalidate();
					}
				} else if (nLabelKind == TSLabelKind.TSLK_STEREOTYPE) {
					if (bShow) {
						showStereotypeLabel();
					} else {
						discardLabel(nLabelKind);
						invalidate();
					}
				}
			}
		}
	}

	public void createNameLabel(boolean bAssignDefaultName) {
		// Get the text to display
		String sName = getNameText(bAssignDefaultName);
		if (sName != null && sName.length() > 0) {
			createLabelIfNotEmpty(sName, TSLabelKind.TSLK_NAME, TSLabelPlacementKind.TSLPK_CENTER_ABOVE, null);
		}
	}

}
