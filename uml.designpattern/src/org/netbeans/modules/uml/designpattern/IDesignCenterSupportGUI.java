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


package org.netbeans.modules.uml.designpattern;

import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;


public interface IDesignCenterSupportGUI
{
	// Populate the passed in tree node with its children
	public void populateTreeItem(Object pParent);

	// The project tree the addin deals with
	public IProjectTreeControl getProjectTree();

	// The project tree the addin deals with
	public void setProjectTree(IProjectTreeControl newVal);

	// Called when the design center is about to display its right click menu
	public ETList <String> onProjectTreeContextMenuPrepare(IProductContextMenu pMenu);

	// Called when the design center is about to display its right click menu
	public void onProjectTreeContextMenuPrepared(IProductContextMenu pMenu );

}
