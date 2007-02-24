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

package org.netbeans.modules.uml.core;

import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;

public interface IApplication
{
	/**
	 * Creates a top level model used to house all other elements.
	*/
	public IProject createProject();

	/**
	 * Opens a top level model used to house all other elements.
	*/
	public IProject openProject( String fileName );

	/**
	 * Closes a top level model used to house all other elements.
	*/
	public void closeProject( IProject Project, boolean save );

	/**
	 * Closes all the open projects.
	*/
	public void closeAllProjects( boolean save );

	/**
	 * Retrieves all open projects. This will only return projects with a .etd file extension
	*/
	public ETList<IProject> getProjects();

	/**
	 * Retrieves all open projects of a given file extension. For example, 'ettd'.
	*/
	public ETList<IProject> getProjects( String fileExtension );

	/**
	 * Retrieves an open project that matches the passed in name.
	*/
	public IProject getProjectByName( String projectName );
	public IProject getProjectByName(IWorkspace pWorkspace, String projectName );

	/**
	 * Retrieves an open project that has been saved to the passed in file.
	*/
	public IProject getProjectByFileName( String fileName );

	/**
	 * Retrieves the open project that has an ID that matches id.
	*/
	public IProject getProjectByID( String projID );

	/**
	 * Creates an empty Workspace with the given name.
	*/
	public IWorkspace createWorkspace( String fileName, String name );

	/**
	 * Opens the workspace specified. location should be an absolute path to the workspace.
	*/
	public IWorkspace openWorkspace( String location );

	/**
	 * Closes the workspace.
	*/
	public void closeWorkspace( IWorkspace space, String fileName, boolean save );

	/**
	 * Imports a Project into the specified Workspace.
	*/
	public IWSProject importProject( IWorkspace space, IProject Project );

	/**
	 * Opens a Project from within the given Workspace.
	*/
	public IProject openProject( IWorkspace space, String projName );

	/**
	 * Opens a Project from within the given Workspace.
	*/
	public IProject openProject( IWorkspace space, IWSProject workspaceProject );

	/**
	 * Destroys this Application.
	*/
	public void destroy();

	/**
	 * Returns the running executable's location.
	*/
	public String getInstallLocation();

	/**
	 * Returns the number of closed projects.
	*/
	public int getNumClosedProjects();

	/**
	 * Returns the number of opened projects.
	*/
	public int getNumOpenedProjects();

	/**
	 * Access to the QueryManager object.
	*/
	public IQueryManager getQueryManager();

	/**
	 * Access to the QueryManager object.
	*/
	public void setQueryManager( IQueryManager value );

	/**
	 * Retrieves the current version of the application.
	*/
	public String getApplicationVersion();

	/**
	 * Saves the project
	*/        
        public void saveProject( IProject project );
}
