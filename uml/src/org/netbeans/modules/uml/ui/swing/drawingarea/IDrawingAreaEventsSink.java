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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IToolTipData;

public interface IDrawingAreaEventsSink
{
	/**
	 * Fired when a drawing area is created.
	*/
	public void onDrawingAreaPreCreated( IDrawingAreaControl pDiagramControl, IResultCell cell );

	/**
	 * Fired when a drawing area is created.
	*/
	public void onDrawingAreaPostCreated( IDrawingAreaControl pDiagramControl, IResultCell cell );

	/**
	 * Fired when a drawing area is opened.
	*/
	public void onDrawingAreaOpened( IDiagram pParentDiagram, IResultCell cell );

	/**
	 * Fired when a drawing area is closed.
	*/
	public void onDrawingAreaClosed( IDiagram pParentDiagram, boolean bDiagramIsDirty, IResultCell cell );

	/**
	 * Fired when a drawing area is saved.
	*/
	public void onDrawingAreaPreSave( IProxyDiagram pParentDiagram, IResultCell cell );

	/**
	 * Fired when a drawing area is saved.
	*/
	public void onDrawingAreaPostSave( IProxyDiagram pParentDiagram, IResultCell cell );

	/**
	 * A key has been pressed.
	*/
	public void onDrawingAreaKeyDown( IDiagram pParentDiagram, int nKeyCode, boolean bControlIsDown, boolean bShiftIsDown, boolean bAltIsDown, IResultCell cell );

	/**
	 * Fired when a drawing area property has changed.
	*/
	public void onDrawingAreaPrePropertyChange( IProxyDiagram pProxyDiagram, /* DrawingAreaPropertyKind */ int nPropertyKindChanged, IResultCell cell );

	/**
	 * Fired when a drawing area property has changed.
	*/
	public void onDrawingAreaPostPropertyChange( IProxyDiagram pProxyDiagram, /* DrawingAreaPropertyKind */ int nPropertyKindChanged, IResultCell cell );

	/**
	 * The tooltip is about to be displayed.
	*/
	public void onDrawingAreaTooltipPreDisplay( IDiagram pParentDiagram, IPresentationElement pPE, IToolTipData pTooltip, IResultCell cell );

	/**
	 * Fired when a drawing area has been activated.
	*/
	public void onDrawingAreaActivated( IDiagram pParentDiagram, IResultCell cell );

	/**
	 * Fired right before items are dropped onto the diagram.
	*/
	public void onDrawingAreaPreDrop( IDiagram pParentDiagram, IDrawingAreaDropContext pContext, IResultCell cell );

	/**
	 * Fired after items are dropped onto the diagram.
	*/
	public void onDrawingAreaPostDrop( IDiagram pParentDiagram, IDrawingAreaDropContext pContext, IResultCell cell );

	/**
	 * Fired right before a diagram file is removed from disk.
	*/
	public void onDrawingAreaPreFileRemoved( String sFilename, IResultCell cell );

	/**
	 * Fired after a diagram file is removed from disk.
	*/
	public void onDrawingAreaFileRemoved( String sFilename, IResultCell cell );

}
