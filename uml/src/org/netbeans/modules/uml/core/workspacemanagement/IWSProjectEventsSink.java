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

package org.netbeans.modules.uml.core.workspacemanagement;

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

public interface IWSProjectEventsSink
{
	/**
	 * Fired right before a WSProject is created.
	*/
	public void onWSProjectPreCreate( IWorkspace space, String projectName, IResultCell cell );

	/**
	 * Fired after a WSProject is created.
	*/
	public void onWSProjectCreated( IWSProject project, IResultCell cell );

	/**
	 * Fired right before a WSProject is opened.
	*/
	public void onWSProjectPreOpen( IWorkspace space, String projName, IResultCell cell );

	/**
	 * Fired after a WSProject is opened.
	*/
	public void onWSProjectOpened( IWSProject project, IResultCell cell );

	/**
	 * Fired right before a WSProject is removed from the Workspace.
	*/
	public void onWSProjectPreRemove( IWSProject project, IResultCell cell );

	/**
	 * Fired after a WSProject is removed from the Workspace.
	*/
	public void onWSProjectRemoved( IWSProject project, IResultCell cell );

	/**
	 * Fired right before a WSProject is inserted into a Workspace.
	*/
	public void onWSProjectPreInsert( IWorkspace space, String projectName, IResultCell cell );

	/**
	 * Fired after a WSProject has been inserted into an existing Workspace.
	*/
	public void onWSProjectInserted( IWSProject project, IResultCell cell );

	/**
	 * Fired right before a WSProject is renamed.
	*/
	public void onWSProjectPreRename( IWSProject project, String newName, IResultCell cell );

	/**
	 * Fired after a WSProject is renamed.
	*/
	public void onWSProjectRenamed( IWSProject project, String oldName, IResultCell cell );

	/**
	 * Fired right before a WSProject is closed.
	*/
	public void onWSProjectPreClose( IWSProject project, IResultCell cell );

	/**
	 * Fired after a WSProject has been closed.
	*/
	public void onWSProjectClosed( IWSProject project, IResultCell cell );

	/**
	 * Fired right before a WSProject is saved.
	*/
	public void onWSProjectPreSave( IWSProject project, IResultCell cell );

	/**
	 * Fired after a WSProject is saved.
	*/
	public void onWSProjectSaved( IWSProject project, IResultCell cell );

}
