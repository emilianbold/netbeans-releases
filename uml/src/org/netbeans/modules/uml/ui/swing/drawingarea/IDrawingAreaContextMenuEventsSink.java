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


package org.netbeans.modules.uml.ui.swing.drawingarea;

import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;

public interface IDrawingAreaContextMenuEventsSink
{
	/**
	 * Fired when the context menu is about to be displayed.
	*/
	public void onDrawingAreaContextMenuPrepare( IDiagram pParentDiagram, IProductContextMenu contextMenu, IResultCell cell );

	/**
	 * Fired when the context menu is about to be displayed.
	*/
	public void onDrawingAreaContextMenuPrepared( IDiagram pParentDiagram, IProductContextMenu contextMenu, IResultCell cell );

	/**
	 * Fired when someone should handle the display.
	*/
	public void onDrawingAreaContextMenuHandleDisplay( IDiagram pParentDiagram, IProductContextMenu contextMenu, IResultCell cell );

	/**
	 * Fired when a context menu item has been selected.
	*/
	public void onDrawingAreaContextMenuSelected( IDiagram pParentDiagram, IProductContextMenu contextMenu, IProductContextMenuItem selectedItem, IResultCell cell );

}
