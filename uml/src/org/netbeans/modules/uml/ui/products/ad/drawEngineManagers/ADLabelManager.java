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
