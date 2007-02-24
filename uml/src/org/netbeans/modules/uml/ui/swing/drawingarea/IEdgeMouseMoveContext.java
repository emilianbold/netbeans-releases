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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;

public interface IEdgeMouseMoveContext
{
	/**
	 * The logical point of the event.
	*/
	public IETPoint getLogicalPoint();

	/**
	 * The logical point of the event.
	*/
	public void setLogicalPoint( IETPoint value );

	/**
	 * The node that the edge has started on.
	*/
	public IETNode getStartNode();

	/**
	 * The node that the edge has started on.
	*/
	public void setStartNode( IETNode value );

	/**
	 * The node that the edge is over.
	*/
	public IETNode getNodeUnderMouse();

	/**
	 * The node that the edge is over.
	*/
	public void setNodeUnderMouse( IETNode value );

	/**
	 * The view description of the edge that is to be created.
	*/
	public String getViewDescription();

	/**
	 * The view description of the edge that is to be created.
	*/
	public void setViewDescription( String value );

	/**
	 * The IElement that is associated with the node the edge is started on.
	*/
	public IElement getStartNodeModelElement();

	/**
	 * The IElement that is associated with the node the edge is to be finished on.
	*/
	public IElement getNodeUnderMouseModelElement();

	/**
	 * Is this location a valid drop location?
	*/
	public boolean getValid();

	/**
	 * Is this location a valid drop location?
	*/
	public void setValid( boolean value );

}
