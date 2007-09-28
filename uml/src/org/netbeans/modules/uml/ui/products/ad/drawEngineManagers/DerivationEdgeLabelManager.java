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



package org.netbeans.modules.uml.ui.products.ad.drawEngineManagers;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IDerivation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IUMLBinding;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelPlacementKind;

public class DerivationEdgeLabelManager extends ADLabelManager implements IDerivationEdgeLabelManager {

	public DerivationEdgeLabelManager() {
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

		bCurrentlyShown = isDisplayed(TSLabelKind.TSLK_STEREOTYPE);

		if (!bCurrentlyShown) {
			String sStereotypeText = getStereotypeText();

			if (sStereotypeText != null && sStereotypeText.length() > 0) {
				createLabelIfNotEmpty(sStereotypeText, TSLabelKind.TSLK_STEREOTYPE, TSLabelPlacementKind.TSLPK_CENTER_BELOW, null);
			}
		}

		bCurrentlyShown = isDisplayed(TSLabelKind.TSLK_DERIVATION_BINDING);

		if (!bCurrentlyShown) {
			String sBindText = getBindText();

			if (sBindText != null && sBindText.length() > 0) {
				createLabelIfNotEmpty(sBindText, TSLabelKind.TSLK_DERIVATION_BINDING, TSLabelPlacementKind.TSLPK_CENTER_ABOVE, null);
			}
		}

		// Make sure the text is ok
		resetLabelsText();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#handleEditNoChange(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel, java.lang.String)
	 */
	public void handleEditNoChange(IETLabel pLabel, String sNewString) {
		// Fix W6592:  This ensures that if the text does not change we still see the <<bind>>
		resetLabelsText();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#isValidLabelKind(int)
	 */
	public boolean isValidLabelKind(int nLabelKind) {

		boolean bIsValid = false;
		if (nLabelKind == TSLabelKind.TSLK_STEREOTYPE || nLabelKind == TSLabelKind.TSLK_DERIVATION_BINDING) {
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
				} else if (nLabelKind == TSLabelKind.TSLK_DERIVATION_BINDING) {
					sText = getBindText();
				}

				// Here's where we set the text of the label
				String sOldText = pETLabel.getText();

				if (sText != null && sText.length() > 0) {
					//if (!(sText.equals(sOldText)))
					{
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
			} else if (nLabelKind == TSLabelKind.TSLK_DERIVATION_BINDING) {
				if (bShow) {
					String sBindText = getBindText();

					if (sBindText != null && sBindText.length() > 0) {
						createLabel(sBindText, TSLabelKind.TSLK_DERIVATION_BINDING, TSLabelPlacementKind.TSLPK_CENTER_ABOVE, null);
					}
				} else {
					discardLabel(nLabelKind);
					invalidate();
				}
			}
		}
	}

	/**
	 * Gets the bind text.
	 *
	 * @param sText [in] The bind text that should be displayed to the user.
	 */
	protected String getBindText() {
		String retValue = "";

		IElement pElement = getModelElement();

		IDerivation pDerivation = (IDerivation) pElement;

		if (pDerivation != null) {
			ETList < IUMLBinding > pBindings = null;

			int count = 0;

			String sTempBindText = "<<bind>>";

			pBindings = pDerivation.getBindings();

			if (pBindings != null) {
				count = pBindings.size();
			}

			for (int i = 0; i < count; i++) {
				IUMLBinding pBinding = (IUMLBinding) pBindings.get(i);

				if (pBinding != null) {
					IParameterableElement pFormal = pBinding.getFormal();
					IParameterableElement pActual = pBinding.getActual();

					String sFormalName = "";
					String sActualName = "";

					if (pFormal != null) {
						sFormalName = pFormal.getName();
					}
					if (pActual != null) {
						sActualName = pActual.getName();
					}

					if (i != 0) {
						sTempBindText += ",";
					}

					if (sFormalName != null && sFormalName.length() > 0) {
						sTempBindText += sFormalName;
						sTempBindText += "::";
					}
					if (sActualName != null)
					{
						if (sActualName.length() == 0)
						{
							sActualName = "int";
						}
						sTempBindText += sActualName;
					}
				}
			}

			if (sTempBindText.length() > 0) {
				retValue = sTempBindText;
			}
		}
		return retValue;
	}
}

