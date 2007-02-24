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

import org.netbeans.modules.uml.core.metamodel.structure.IProject;

public interface INewDialogProjectDetails extends INewDialogTabDetails
{
	/**
	 * Name of the project
	*/
	public String getName();

	/**
	 * Name of the project
	*/
	public void setName( String value );

	/**
	 * Location of the project
	*/
	public String getLocation();

	/**
	 * Location of the project
	*/
	public void setLocation( String value );

	/**
	 * Should this project be added to source control?
	*/
	public boolean getAddToSourceControl();

	/**
	 * Should this project be added to source control?
	*/
	public void setAddToSourceControl( boolean value );

	/**
	 * The kind of project to create
	*/
	public int getProjectKind();

	/**
	 * The kind of project to create
	*/
	public void setProjectKind( /* NewProjectKind */ int value );

	/**
	 * The default mode of the project
	*/
	public String getMode();

	/**
	 * The default mode of the project
	*/
	public void setMode( String value );

	/**
	 * The default language of the project
	*/
	public String getLanguage();

	/**
	 * The default language of the project
	*/
	public void setLanguage( String value );

	/**
	 * Should the dialog show the From Reverse Engineering selection?
	*/
	public boolean getAllowFromRESelection();

	/**
	 * Should the dialog show the From Reverse Engineering selection?
	*/
	public void setAllowFromRESelection( boolean value );

	/**
	 * Specifies if the language control is to be read.
	*/
	public boolean getIsLanguageReadOnly();

	/**
	 * Specifies if the language control is to be read.
	*/
	public void setIsLanguageReadOnly( boolean value );

	/**
	 * Specifies whether or not to prompt the user to create a new diagram.
	*/
	public boolean getPromptToCreateDiagram();

	/**
	 * Specifies whether or not to prompt the user to create a new diagram.
	*/
	public void setPromptToCreateDiagram( boolean value );

	/**
	 * The Project created when these details are processed.
	*/
	public IProject getCreatedProject();

	/**
	 * The Project created when these details are processed.
	*/
	public void setCreatedProject( IProject value );

}
