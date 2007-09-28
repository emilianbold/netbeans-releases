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



package org.netbeans.modules.uml.ui.support.viewfactorysupport;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;

public interface ILabelManager extends IGraphObjectManager {
	/**
	 * Creates the initial labels on a newly created node or edge
	*/
	public void createInitialLabels();

	/**
	 * Reset the label text.
	*/
	public void resetLabelsText();

	/**
	 * Called when the model element associated with the label/engine has changed.
	*/
	public void modelElementHasChanged(INotificationTargets pTargets);

	/**
	 * Reset the labels.  Creates the defaults and positions them accordingly.
	*/
	public void resetLabels();

	/**
	 * Relayout the labels on this link/node.
	*/
	public void relayoutLabels();

	/**
	 * Removes all labels.
	*/
	public void discardAllLabels();

	/**
	 * Notifies the node that a context menu is about to be displayed
	*/
	public void onContextMenu(IProductContextMenu pContextMenu, int logicalX, int logicalY);

	public void onContextMenu(IMenuManager manager);

	/**
	 * Notifies the node that a context menu has been selected
	*/
	public void onContextMenuHandleSelection(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem);

	/**
	 * Set the menu button sensitivity
	*/
	public void setSensitivityAndCheck(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem, int buttonKind);

	/**
	 * Handles the changing of the model element when a label is edited.
	*/
	public void handleEditChange(IETLabel pLabel, String sNewString);

	/**
	 * Notification to the label manager that edit took place, but nothing changed.  Used in multiplicity editing.
	*/
	public void handleEditNoChange(IETLabel pLabel, String sNewString);

	/**
	 * Deletes the label by type.
	*/
	public void discardLabel(int nLabelKind);

	/**
	 * Deletes the ET label.
	*/
	public void discardETLabel(IETLabel pETLabel);

	/**
	 * Does this label manager know how to display this label?
	*/
	public boolean isValidLabelKind(int nLabelKind);

	/**
	 * Are we currently displaying this label?
	*/
	public boolean isDisplayed(int nLabelKind);

	/**
	 * Show or hide this label
	*/
	public void showLabel(int nLabelKind, boolean bShow);

	/**
	 * Retrieve a label by its index
	*/
	public IETLabel getLabelByIndex(int lIndex);

	/**
	 * Get the IPresentationElement for this label
	*/
	public IPresentationElement getLabel(int nLabelKind);

	/**
	 * Edits this label
	*/
	public void editLabel(int nLabelKind);

	/**
	 * The rectangle encompassing all the labels, in logical coordinates
	*/
	public IETRect getLogicalBoundingRectForAllLabels();

	/**
	 * Sets the forced stereotype string used to show a stereotype label when no actual stereotype is involved
	*/
	public void setForcedStereotypeString(String sForcedString);

	/**
	 * Gets the stereotype text.
	*/
	public String getStereotypeText();

}
