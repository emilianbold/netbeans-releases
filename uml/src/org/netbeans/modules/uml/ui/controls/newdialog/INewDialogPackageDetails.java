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

public interface INewDialogPackageDetails extends INewDialogTabDetails
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
	 * The namespace this diagram will occupy
	*/
	public INamespace getNamespace();

	/**
	 * The namespace this diagram will occupy
	*/
	public void setNamespace( INamespace value );

	/**
	 * Add an additional namespace to our list of possible namespaces.
	*/
	public long addNamespace( INamespace pNamespace );

	/**
	 * Should we create a scoped diagram?
	*/
	public boolean getCreateScopedDiagram();

	/**
	 * Should we create a scoped diagram?
	*/
	public void setCreateScopedDiagram( boolean value );

	/**
	 * The name of the scoped diagram
	*/
	public String getScopedDiagramName();

	/**
	 * The name of the scoped diagram
	*/
	public void setScopedDiagramName( String value );

	/**
	 * The kind of the scoped diagram
	*/
	public int getScopedDiagramKind();

	/**
	 * The kind of the scoped diagram
	*/
	public void setScopedDiagramKind( /* DiagramKind */ int value );

	/**
	 * The kind of package to create
	*/
	public int getPackageKind();

	/**
	 * The kind of package to create
	*/
	public void setPackageKind( /* NewPackageKind */ int value );

	/**
	 * Should the dialog show the From Reverse Engineering selection?
	*/
	public boolean getAllowFromRESelection();

	/**
	 * Should the dialog show the From Reverse Engineering selection?
	*/
	public void setAllowFromRESelection( boolean value );

}
