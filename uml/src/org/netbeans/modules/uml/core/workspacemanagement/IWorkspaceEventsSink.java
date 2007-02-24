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

public interface IWorkspaceEventsSink
{
	/**
	 * Fired right before a workspace is created.
	*/
	public void onWorkspacePreCreate( IWorkspacePreCreateEventPayload pEvent, IResultCell cell );

	/**
	 * Fired after a workspace is created.
	*/
	public void onWorkspaceCreated( IWorkspace space, IResultCell cell );

	/**
	 * Fired right before a workspace is opened.
	*/
	public void onWorkspacePreOpen( String fileName, IResultCell cell );

	/**
	 * Fired after a workspace is opened.
	*/
	public void onWorkspaceOpened( IWorkspace space, IResultCell cell );

	/**
	 * Fired right before a workspace is saved.
	*/
	public void onWorkspacePreSave( String fileName, IResultCell cell );

	/**
	 * Fired after a workspace is saved.
	*/
	public void onWorkspaceSaved( IWorkspace space, IResultCell cell );

	/**
	 * Fired right before a workspace is closed.
	*/
	public void onWorkspacePreClose( IWorkspace space, IResultCell cell );

	/**
	 * Fired right after a workspace is closed.
	*/
	public void onWorkspaceClosed( IWorkspace space, IResultCell cell );

}
