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

import org.netbeans.modules.uml.core.eventframework.IEventDispatchHelper;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;

/**
 *
 * @author Trey Spiva
 */
public interface IWSProjectEventDispatcher extends IEventDispatchHelper
{
	/**
	 * method DispatchWSProjectPreCreate
	 */
	public IResultCell dispatchWSProjectPreCreate( IWorkspace space, String projName )
	throws InvalidArguments;

	/**
	 * method DispatchWSProjectCreated
	 */
	public void dispatchWSProjectCreated( IWSProject wsProject ) throws InvalidArguments;

	/**
	 * method DispatchWSProjectPreOpen
	 */
	public boolean dispatchWSProjectPreOpen( IWorkspace space, String projectName ) throws InvalidArguments;

	/**
	 * method DispatchWSProjectOpened
	 */
	public void dispatchWSProjectOpened( IWSProject wsProject ) throws InvalidArguments;

	/**
	 * method DispatchWSProjectPreRemove
	 */
	public boolean dispatchWSProjectPreRemove( IWSProject project ) throws InvalidArguments;

	/**
	 * method DispatchWSProjectRemoved
	 */
	public void dispatchWSProjectRemoved( IWSProject project ) throws InvalidArguments;

	/**
	 * method DispatchWSProjectPreInsert
	 */
	public boolean dispatchWSProjectPreInsert( IWorkspace space, String projectName ) throws InvalidArguments;

	/**
	 * method DispatchWSProjectInserted
	 */
	public void dispatchWSProjectInserted( IWSProject project ) throws InvalidArguments;

	/**
	 * method DispatchWSProjectPreRename
	 */
	public boolean dispatchWSProjectPreRename( IWSProject project, String newName ) throws InvalidArguments;

	/**
	 * method DispatchWSProjectRenamed
	 */
	public void dispatchWSProjectRenamed( IWSProject project, String oldName ) throws InvalidArguments;

	/**
	 * method DispatchWSProjectPreClose
	 */
	public boolean dispatchWSProjectPreClose( IWSProject project ) throws InvalidArguments;

	/**
	 * method DispatchWSProjectClosed
	 */
	public void dispatchWSProjectClosed( IWSProject project ) throws InvalidArguments;

	/**
	 * method DispatchWSProjectPreSave
	 */
	public boolean dispatchWSProjectPreSave( IWSProject project ) throws InvalidArguments;

	/**
	 * method DispatchWSProjectSaved
	 */
	public void dispatchWSProjectSaved( IWSProject project ) throws InvalidArguments;

}
