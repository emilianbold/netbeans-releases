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


package org.netbeans.modules.uml.core.metamodel.structure;

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;

public interface IProjectEventsSink
{
	/**
	 * Fired whenever the passed in Project's Mode property is about to change.
	*/
	public void onPreModeModified( IProject pProject, String newValue, IResultCell cell );

	/**
	 * Fired whenever the passed in Project's Mode has been changed.
	*/
	public void onModeModified( IProject pProject, IResultCell cell );

	/**
	 * Fired whenever the passed in Project's DefaultLanguage property is about to change.
	*/
	public void onPreDefaultLanguageModified( IProject pProject, String newValue, IResultCell cell );

	/**
	 * Fired whenever the passed in Project's DefaultLanguage property has been changed.
	*/
	public void onDefaultLanguageModified( IProject pProject, IResultCell cell );

	/**
	 * Fired right before a Project is created.
	*/
	public void onProjectPreCreate( IWorkspace space, IResultCell cell );

	/**
	 * Fired after a Project is created.
	*/
	public void onProjectCreated( IProject Project, IResultCell cell );

	/**
	 * Fired right before a Project is opened.
	*/
	public void onProjectPreOpen( IWorkspace space, String projName, IResultCell cell );

	/**
	 * Fired after a Project is opened.
	*/
	public void onProjectOpened( IProject Project, IResultCell cell );

	/**
	 * Fired right before a Project is renamed.
	*/
	public void onProjectPreRename( IProject Project, String newName, IResultCell cell );

	/**
	 * Fired after a Project is renamed.
	*/
	public void onProjectRenamed( IProject Project, String oldName, IResultCell cell );

	/**
	 * Fired right before a Project is closed.
	*/
	public void onProjectPreClose( IProject Project, IResultCell cell );

	/**
	 * Fired after a Project has been closed.
	*/
	public void onProjectClosed( IProject Project, IResultCell cell );

	/**
	 * Fired right before a Project is saved.
	*/
	public void onProjectPreSave( IProject Project, IResultCell cell );

	/**
	 * Fired after a Project is saved.
	*/
	public void onProjectSaved( IProject Project, IResultCell cell );

	/**
	 * Fired right before a referenced library location is added to the Project.
	*/
	public void onPreReferencedLibraryAdded( IProject Project, String refLibLoc, IResultCell cell );

	/**
	 * Fired right after a referenced library location is added to the Project.
	*/
	public void onReferencedLibraryAdded( IProject Project, String refLibLoc, IResultCell cell );

	/**
	 * Fired right before a referenced library location is removed from the Project.
	*/
	public void onPreReferencedLibraryRemoved( IProject Project, String refLibLoc, IResultCell cell );

	/**
	 * Fired right after a referenced library location is removed from the Project.
	*/
	public void onReferencedLibraryRemoved( IProject Project, String refLibLoc, IResultCell cell );

}
