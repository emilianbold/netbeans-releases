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


package org.netbeans.modules.uml.ui.addins.diagramcreator;

import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

/**
 * @author sumitabhk
 *
 */
public interface IGenealogyTree
{
	// Adds a node to our list of nodes that are undetermined what their parentage is
	public void addUndeterminedNode(INodePresentation pNodeToAdd);

	// Call this after all the undertermined nodes are created.  It builds the genaeology.
	public void buildGenealogy(IDrawingAreaControl pDiagram);

	// Find the parent/child relationships
	public void determineParentChildRelationships();

	// Calculates the rectangles of the various parents and children
	public void calculateRectangles( IETRect rootGraphRect );

	// Places the nodes on the diagram
	public void placeNodes();

	// Sends an event to all nodes
	public void onGraphEvent(int nKind, String message);

	// Sets the stacking order
	public void setStackingOrder();

	// Prints the tree.  Used in debug
	public void printTree();

	/// Distributes all the ports on component draw engines
	public void distributeAllComponentPorts();

}


