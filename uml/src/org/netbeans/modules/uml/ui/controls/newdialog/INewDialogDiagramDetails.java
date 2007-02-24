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


package org.netbeans.modules.uml.ui.controls.newdialog;

import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;

public interface INewDialogDiagramDetails extends INewDialogTabDetails
{
	/**
	 * Name of the diagram
	*/
	public String getName();

	/**
	 * Name of the diagram
	*/
	public void setName( String value );

	/**
	 * The selected namespace
	*/
	public INamespace getNamespace();

	/**
	 * The selected namespace
	*/
	public void setNamespace( INamespace value );

	/**
	 * Add an additional namespace to our list of possible namespaces.
	*/
	public long addNamespace( INamespace pNamespace );

	/**
	 * The selected diagram kind
	*/
	public int getDiagramKind();

	/**
	 * The selected diagram kind
	*/
	public void setDiagramKind( /* DiagramKind */ int value );

	/**
	 * The diagram kinds that are available for selection in the dialog
	*/
	public int getAvailableDiagramKinds();

	/**
	 * The diagram kinds that are available for selection in the dialog
	*/
	public void setAvailableDiagramKinds( int value );

}
