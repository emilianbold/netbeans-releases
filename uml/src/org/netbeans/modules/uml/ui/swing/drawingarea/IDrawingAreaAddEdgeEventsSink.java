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

public interface IDrawingAreaAddEdgeEventsSink
{
	/**
	 * Fired when an edge is about to be created.
	*/
	public void onDrawingAreaStartingEdge( IDiagram pParentDiagram, IEdgeCreateContext pContext, IResultCell cell );

	/**
	 * Fired when an edge bend is about to be created.
	*/
	public void onDrawingAreaEdgeShouldCreateBend( IDiagram pParentDiagram, IEdgeCreateBendContext pContext, IResultCell cell );

	/**
	 * Fired when an edge is being moved while created.
	*/
	public void onDrawingAreaEdgeMouseMove( IDiagram pParentDiagram, IEdgeMouseMoveContext pContext, IResultCell cell );

	/**
	 * Fired when an edge is about to be finished.
	*/
	public void onDrawingAreaFinishEdge( IDiagram pParentDiagram, IEdgeFinishContext pContext, IResultCell cell );

}
