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
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;

public interface IWorkspaceEventDispatchHelper extends IEventDispatchHelper
{
	/**
	 * Dispatches the OnWorkspacePreCreate event to all listeners.
	*/
	public boolean dispatchWorkspacePreCreate( String fileName, String Name )
		throws InvalidArguments;

	/**
	 * Dispatches the OnWorkspaceCreated event to all listeners.
	*/
	public void dispatchWorkspaceCreated( IWorkspace space )
		throws InvalidArguments;

	/**
	 * Dispatches the OnWorkspacePreOpen event to all listeners.
	*/
	public boolean dispatchWorkspacePreOpen( String fileName )
		throws InvalidArguments;

	/**
	 * Dispatches the OnWorkspacePreOpen event to all listeners.
	*/
	public void dispatchWorkspaceOpened( IWorkspace space )
		throws InvalidArguments;

	/**
	 * Dispatches the OnWorkspacePreClose event to all listeners.
	*/
	public boolean dispatchWorkspacePreClose( IWorkspace space )
		throws InvalidArguments;

	/**
	 * Dispatches the OnWorkspaceClosed event to all listeners.
	*/
	public void dispatchWorkspaceClosed( IWorkspace space )
		throws InvalidArguments;

}
