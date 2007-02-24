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
