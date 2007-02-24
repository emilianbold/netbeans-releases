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

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

public interface IDrawingAreaSynchEventsSink
{
	/**
	 * Fired when we need to get the sync state of a particular presentation element.
	*/
	public void onDrawingAreaPreRetrieveElementSynchState( IPresentationElementSyncState pPresentationElementSyncState, IResultCell cell );

	/**
	 * Fired when we need to get the sync state of a particular presentation element.
	*/
	public void onDrawingAreaPostRetrieveElementSynchState( IPresentationElementSyncState pPresentationElementSyncState, IResultCell cell );

	/**
	 * Fired when a presentation element is about to by synched.
	*/
	public void onDrawingAreaPrePresentationElementPerformSync( IPresentationElementPerformSyncContext pPresentationElementSyncContext, IResultCell cell );

	/**
	 * Fired when a presentation element has been synched.
	*/
	public void onDrawingAreaPostPresentationElementPerformSync( IPresentationElementPerformSyncContext pPresentationElementSyncContext, IResultCell cell );

	/**
	 * Fired when a diagram is about to by synched.
	*/
	public void onDrawingAreaPreDiagramPerformSync( IDiagramPerformSyncContext pDiagramSyncContext, IResultCell cell );

	/**
	 * Fired when a diagram has been synched.
	*/
	public void onDrawingAreaPostDiagramPerformSync( IDiagramPerformSyncContext pDiagramSyncContext, IResultCell cell );

}
