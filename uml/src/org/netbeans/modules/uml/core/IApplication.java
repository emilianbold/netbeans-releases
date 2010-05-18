/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
