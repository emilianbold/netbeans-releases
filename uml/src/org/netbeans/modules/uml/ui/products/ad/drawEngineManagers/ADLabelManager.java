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

//	 Author:: nickl
//	   Date:: Nov 7, 2003 11:38:48 AM												
//	Modtime:: 3/12/2004 6:53:25 PM 11:38:48 AM	

package org.netbeans.modules.uml.ui.products.ad.drawEngineManagers;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ModelElementChangedKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelKind;

public class ADLabelManager extends LabelManager implements IADLabelManager {

	// Reposition the label
	public void sizeToContents() {

	}

	// Reposition the label
	public void rePosition() {
	}

	// Creates the context menu button handler
	public void createContextMenuButtonHandler() {
		if (m_ButtonHandler == null) {
			ILabelManager pThisLabelManager = this.getLabelManagerInterface();
			if (pThisLabelManager != null) {
				m_ButtonHandler = new ADLabelManagerButtonHandler(pThisLabelManager);
			}
		}
	}

	// Used to update the name and stereotype labels
	public void modelElementHasChanged(INotificationTargets pTargets) {

		// See what's valid
		boolean bNameValue = false;
		boolean bStereotypeValue = false;

		bNameValue = this.isValidLabelKind(TSLabelKind.TSLK_NAME);
		bStereotypeValue = this.isValidLabelKind(TSLabelKind.TSLK_STEREOTYPE);

		if (bNameValue || bStereotypeValue) {
			if (bNameValue) {
				// Get the changed model element and kind
				IElement pChangedElement = null;
				IElement pSecondaryChangedME = null;
				IFeature pChangedFeature = null;
				int nKind = ModelElementChangedKind.MECK_UNKNOWN;

				pChangedElement = pTargets.getChangedModelElement();
				pSecondaryChangedME = pTargets.getSecondaryChangedModelElement();

				if (pSecondaryChangedME instanceof IFeature){
				  pChangedFeature = (IFeature) pSecondaryChangedME;
				}

				nKind = pTargets.getKind();

				if ((pChangedFeature != null || pChangedElement != null) && nKind == ModelElementChangedKind.MECK_NAMEMODIFIED) {
					boolean bCurrentlyShown = false;

					bCurrentlyShown = this.isDisplayed(TSLabelKind.TSLK_NAME);
					if (!bCurrentlyShown) {
					    // fix for 6458810 - don't show the label as a side-effect of changes to labels of other elements 	     
					    // Note: we'll still show the label when the the changes are directly related to the label,
					    // for example when the label text (element name) was changed through the properties window 
					    IElement pThisElement = this.getModelElement();
					    boolean bIsSame = (pChangedElement != null && pThisElement != null) 
						? pChangedElement.isSame(pThisElement) 
						: false;
					    if (bIsSame) {
						this.showLabel(TSLabelKind.TSLK_NAME, true);
					    }
					} else {
						this.resetLabelText(TSLabelKind.TSLK_NAME);
					}
				}
			}

			if (bStereotypeValue) {
				// Handle the stereotype change
				this.handleStereotypeChange(pTargets);
			}
		}

	}

	// ILabelManager Overrides
	public void onContextMenu(IProductContextMenu pContextMenu, int logicalX, int logicalY) {
		this.createContextMenuButtonHandler();
		if (m_ButtonHandler != null) {
			m_ButtonHandler.addResetLabelsButton(pContextMenu);
		}
	}

	public void onContextMenuHandleSelection(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem) {

	}

	// Resets the text for an individual label type
	public void resetLabelText(int nLabelKind) {
		if (nLabelKind == TSLabelKind.TSLK_NAME) {
			// Reset the name text
			String sText = "";
			IETLabel pETLabel = null;

			sText = this.getNameText(false);
			pETLabel = this.getETLabelbyKind(nLabelKind);

			if (pETLabel != null) {
				if (sText.length() > 0) {
					this.setLabelString(pETLabel, sText);
				} else {
					this.showLabel(nLabelKind, false);
				}
			}
		} else {
			super.resetLabelText(nLabelKind);
		}

	}

	// Get the button handler
	public ADLabelManagerButtonHandler getButtonHandler() {
		return this.m_ButtonHandler;
	}

	// Returns the name text if this element is an INamedElement
	protected String getNameText(boolean bAssignDefaultName) {
		String nameText = "";

		IElement elem = getModelElement();
		
		if (elem != null && elem instanceof INamedElement) {

			INamedElement pNamedElement = (INamedElement) elem;
			
			// Get the name or the alias depending on the preference setting
			nameText = pNamedElement.getNameWithAlias();

			// If the user has told us then assign a default name
			if ((nameText == null || nameText.length() == 0) && bAssignDefaultName) {
				String xName = this.retrieveDefaultName();
				if (xName != null && xName.length() > 0) {
					pNamedElement.setNameWithAlias(xName);
					nameText = pNamedElement.getNameWithAlias();
				}
			}
		}

		return nameText;
	}

	// Our button handler.
	protected ADLabelManagerButtonHandler m_ButtonHandler;

}
