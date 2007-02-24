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


import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphObjectValidation;

public interface IGraphObjectManager
{
	/**
	 * Sets the parent ET element
	*/
	public void setParentETGraphObject( IETGraphObject value );

	/**
	 * Sets the parent ET element
	*/
	public IETGraphObject getParentETGraphObject();

	/**
	 * Notifies the node an event has been generated at the graph.
	*/
	public void onGraphEvent( /* IGraphEventKind */ int nKind );

	/**
	 * Validates this object.
	*/
	public long validate( IGraphObjectValidation pValidationKind );

}
