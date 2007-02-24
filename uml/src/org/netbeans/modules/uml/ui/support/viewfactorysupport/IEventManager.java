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

import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;

public interface IEventManager extends IGraphObjectManager
{
	/**
	 * Notifies the node that a link has been added
	*/
	public long onPostAddLink( IETGraphObject pNewLink, boolean bIsFromNode );

	/**
	 * Notifies the node that a link is about to be deleted
	*/
	public long onPreDeleteLink( IETGraphObject pLinkAboutToBeDeleted, boolean bIsFromNode );

	/**
	 * Reset the edges.  Positions them accordingly.
	*/
	public long resetEdges();

	/**
	 * Does this event manager have edges to reset?
	 */ 
	public boolean hasEdgesToReset();

	/**
	 * Notifies the node that a context menu is about to be displayed
	*/
	public long onContextMenu( IProductContextMenu pContextMenu, int logicalX, int logicalY );
	public void onContextMenu( IMenuManager manager);

	/**
	 * Notifies the node that a context menu has been selected
	*/
	public long onContextMenuHandleSelection( IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem );

	/**
	 * Set the menu button sensitivity
	*/
	public boolean setSensitivityAndCheck(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem, int buttonKind );

}
