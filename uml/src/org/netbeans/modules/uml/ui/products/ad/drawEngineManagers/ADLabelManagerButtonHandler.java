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

import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.contextmenusupport.ProductButtonHandler;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;

public class ADLabelManagerButtonHandler extends ProductButtonHandler {

	public ADLabelManagerButtonHandler(ILabelManager pLabelManager) {
		super();
		this.m_LabelManager = pLabelManager;
	}

	// Adds the reset label menu button
	public void addResetLabelsButton(IProductContextMenu pContextMenu) {

	}

	// Add the buttons to show either the message or operation
	public void addMessageLabelButtons(IProductContextMenu pContextMenu, boolean bDefaultSensitivity) {

		ETList < IProductContextMenuItem > cpMenuItems = pContextMenu.getSubMenus();

		if (cpMenuItems != null) {
			IProductContextMenuItem cpMenuItem;

			cpMenuItem = this.createOrGetPullright(cpMenuItems, "IDS_LABELS_TITLE", this.loadString("IDS_LABELS_TITLE"));

			if (cpMenuItem != null) {
				// Get the sub menu items for this popup
				ETList < IProductContextMenuItem > cpSubMenuItems = cpMenuItem.getSubMenus();
				if (cpSubMenuItems != null) {
					this.addMenuItem(pContextMenu,
									cpSubMenuItems,
						MenuButtonKind.MBK_SHOW_OPERATION_NAME,
						this.loadString("IDS_SHOW_OPERATION_NAME"),
						this.loadString("IDS_SHOW_OPERATION_NAME_DSCR"),
						"MBK_SHOW_OPERATION_NAME",
						bDefaultSensitivity,
						null);

					this.addMenuItem(
						pContextMenu,
						cpSubMenuItems,
						MenuButtonKind.MBK_SHOW_MESSAGE_NAME,
						this.loadString("IDS_SHOW_MESSAGE_NAME"),
						this.loadString("IDS_SHOW_MESSAGE_NAME_DSCR"),
						"MBK_SHOW_MESSAGE_NAME",
						bDefaultSensitivity,
						null);

				}
			}
		}

	}

	// Resets the labels on the edge or node
	public void resetLabels() {

	}

	// Called when a specific button is called.  The menuSelected is of kind MenuButtonKind.
	public boolean handleButton(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem, int menuSelected) {
		boolean retValue = false;
		if (m_LabelManager != null) {
			m_LabelManager.onContextMenuHandleSelection(pContextMenu, pMenuItem);
		}
		return retValue;
	}

	// Set the menu button sensitivity and check state
	public void setSensitivityAndCheck(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem, int buttonKind) {
		if (m_LabelManager != null) {
			m_LabelManager.setSensitivityAndCheck(pContextMenu, pMenuItem, buttonKind);
		}
	}

	//   // Loads a string based on an ID
	//   virtual CString LoadString(UINT id);

	/// The parent compartment which we need to notify when a selection happens.  This must be a raw pointer or deadly embrace.
	protected ILabelManager m_LabelManager = null;

}
