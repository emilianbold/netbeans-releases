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


package org.netbeans.modules.uml.ui.support.applicationmanager;

import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IProductDiagramManager
{
	/**
	 * Tell the gui to open a diagram.  If bMaximized then the diagram is opened maximized so it draws.
	*/
	public IDiagram openDiagram( String sTOMFilename, boolean bMaximized, IDiagramCallback pDiagramCreatedCallback );

	/**
	 * Tell the gui to open a diagram
	*/
	public IDiagram openDiagram2( IProxyDiagram pProxyDiagram, boolean bMaximized, IDiagramCallback pDiagramCreatedCallback );

	/**
	 * Tell the gui to close a diagram
	*/
	public long closeDiagram( String sTOMFilename );

	/**
	 * Tell the gui to close a diagram
	*/
	public long closeDiagram2( IDiagram pDiagram );

	/**
	 * Tell the gui to close this diagram
	*/
	public long closeDiagram3( IProxyDiagram pProxyDiagram );

	/**
	 * Tell the gui to open the new diagram dialog
	*/
	public IDiagram newDiagramDialog( INamespace pNamespace, /* DiagramKind */ int nDefaultDiagram, int lAvailableDiagramKinds, IDiagramCallback pDiagramCreatedCallback );

	/**
	 * Bring this diagram to the front
	*/
	public long raiseWindow( IDiagram pOpenControl );

	/**
	 * Returns the currently active diagram.
	*/
	public IDiagram getCurrentDiagram();

	/**
	 * Returns the diagram with this name, returns 0 if the diagram is not open.
	*/
	public IDiagram getOpenDiagram( String sTOMFilename );

	/**
	 * Create a new diagram
	*/
	public IDiagram createDiagram( /* DiagramKind */ int nDiagramKind, INamespace pNamespace, String sDiagramName, IDiagramCallback pDiagramCreatedCallback );

	/**
	 * Returns all the open diagrams.
	*/
	public ETList<IProxyDiagram> getOpenDiagrams();

	/**
	 * Tell the gui to minimize a diagram
	*/
	public long minimizeDiagram( String sTOMFilename, boolean bMinimize );

	/**
	 * Tell the gui to minimize a diagram
	*/
	public long minimizeDiagram2( IDiagram pDiagram, boolean bMinimize );

	/**
	 * Tell the gui to minimize this diagram
	*/
	public long minimizeDiagram3( IProxyDiagram pProxyDiagram, boolean bMinimize );
   
   /** Refresh the diagram by reading in the contents of the diagram file. */
   public void refresh(IProxyDiagram proxy);

}
