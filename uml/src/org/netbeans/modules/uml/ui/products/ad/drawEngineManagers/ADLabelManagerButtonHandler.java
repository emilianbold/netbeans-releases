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
