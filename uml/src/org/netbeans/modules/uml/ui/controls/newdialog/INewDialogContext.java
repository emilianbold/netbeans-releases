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
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;

public interface INewDialogContext
{
	/**
	 * Sets / Gets the workspace that should be used for this instance of the new dialog.
	*/
	public IWorkspace getWorkspace();

	/**
	 * Sets / Gets the workspace that should be used for this instance of the new dialog.
	*/
	public void setWorkspace( IWorkspace value );

	/**
	 * Sets / Gets the project that should be used for this instance of the new dialog.
	*/
	public IProject getProject();

	/**
	 * Sets / Gets the project that should be used for this instance of the new dialog.
	*/
	public void setProject( IProject value );

	/**
	 * TRUE to use all extensions when getting the number of open projects.
	*/
	public boolean getUseAllProjectExtensions();

	/**
	 * TRUE to use all extensions when getting the number of open projects.
	*/
	public void setUseAllProjectExtensions( boolean value );

	/**
	 * Returns the number of open projects.
	*/
	public int getNumOpenProjects();

	/**
	 * Sets / Gets the project tree that should be used for this instance of the new dialog.
	*/
	public IProjectTreeControl getProjectTree();

	/**
	 * Sets / Gets the project tree that should be used for this instance of the new dialog.
	*/
	public void setProjectTree( IProjectTreeControl value );

}
