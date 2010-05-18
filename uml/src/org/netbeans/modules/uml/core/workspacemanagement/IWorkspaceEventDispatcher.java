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

package org.netbeans.modules.uml.core.workspacemanagement;

import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

public interface IWorkspaceEventDispatcher extends IEventDispatcher
{
	/**
	 * Registers a sink that will received all notifications concerning Workspaces.
	*/
	public void registerForWorkspaceEvents( IWorkspaceEventsSink sink );

	/**
	 * Registers a sink that will received all notifications concerning WSProjects.
	*/
	public void registerForWSProjectEvents( IWSProjectEventsSink handler );

	/**
	 * Registers a sink that will received all notifications concerning WSElements.
	*/
	public void registerForWSElementEvents( IWSElementEventsSink sink );

	/**
	 * Registers a sink that will received all notifications concerning modifications of WSElements.
	*/
	public void registerForWSElementModifiedEvents( IWSElementModifiedEventsSink sink );

	/**
	 * Removes a sink listening for wokspace events.
	*/
	public void revokeWorkspaceSink( IWorkspaceEventsSink sink );

	/**
	 * Removes a sink listening for wokspace project events.
	*/
	public void revokeWSProjectSink( IWSProjectEventsSink sink );

	/**
	 * Removes a sink listening for WSElement events.
	*/
	public void revokeWSElementSink( IWSElementEventsSink sink );

	/**
	 * Removes a sink listening for WSElementModify events.
	*/
	public void revokeWSElementModifiedSink( IWSElementModifiedEventsSink sink );

	/**
	 * Calling this method will result in the firing of any listeners interested in this event.
	*/
	public boolean fireWorkspacePreCreate( IWorkspacePreCreateEventPayload payLoad );

	/**
	 * Calling this method will result in the firing of any listeners interested in this event.
	*/
	public void fireWorkspaceCreated( IWorkspace space, IEventPayload payLoad );

	/**
	 * Calling this method will result in the firing of any listeners interested in this event.
	*/
	public boolean fireWorkspacePreOpen( String fileName, IEventPayload payLoad );

	/**
	 * Calling this method will result in the firing of any listeners interested in this event.
	*/
	public void fireWorkspaceOpened( IWorkspace space, IEventPayload payLoad );

	/**
	 * Calling this method will result in the firing of any listeners interested in this event.
	*/
	public boolean fireWorkspacePreSave( String location, IEventPayload payLoad );

	/**
	 * Calling this method will result in the firing of any listeners interested in this event.
	*/
	public void fireWorkspaceSaved( IWorkspace space, IEventPayload payLoad );

	/**
	 * Calling this method will result in the firing of any listeners interested in this event.
	*/
	public boolean fireWorkspacePreClose( IWorkspace space, IEventPayload payLoad );

	/**
	 * Calling this method will result in the firing of any listeners interested in this event.
	*/
	public void fireWorkspaceClosed( IWorkspace space, IEventPayload payLoad );

	/**
	 * Calling this method will result in the firing of any listeners interested in this event.
	*/
	public IResultCell fireWSProjectPreCreate( IWorkspace space, String projectName, IEventPayload payLoad );

	/**
	 * Calling this method will result in the firing of any listeners interested in this event.
	*/
	public void fireWSProjectCreated( IWSProject project, IEventPayload payLoad );

	/**
	 * Calling this method will result in the firing of any listeners interested in this event.
	*/
	public boolean fireWSProjectPreOpen( IWorkspace project, String projName, IEventPayload payLoad );

	/**
	 * Calling this method will result in the firing of any listeners interested in this event.
	*/
	public void fireWSProjectOpened( IWSProject project, IEventPayload payLoad );

	/**
	 * Calling this method will result in the firing of any listeners interested in this event.
	*/
	public boolean fireWSProjectPreRemove( IWSProject project, IEventPayload payLoad );

	/**
	 * Calling this method will result in the firing of any listeners interested in this event.
	*/
	public void fireWSProjectRemoved( IWSProject project, IEventPayload payLoad );

	/**
	 * Calling this method will result in the firing of any listeners interested in this event.
	*/
	public boolean fireWSProjectPreInsert( IWorkspace space, String projectName, IEventPayload payLoad );

	/**
	 * Calling this method will result in the firing of any listeners interested in this event.
	*/
	public void fireWSProjectInserted( IWSProject project, IEventPayload payLoad );

	/**
	 * Calling this method will result in the firing of any listeners interested in this event.
	*/
	public boolean fireWSProjectPreRename( IWSProject project, String newName, IEventPayload payLoad );

	/**
	 * Calling this method will result in the firing of any listeners interested in this event.
	*/
	public void fireWSProjectRenamed( IWSProject project, String oldName, IEventPayload payLoad );

	/**
	 * Calling this method will result in the firing of any listeners interested in this event.
	*/
	public boolean fireWSProjectPreClose( IWSProject project, IEventPayload payLoad );

	/**
	 * Calling this method will result in the firing of any listeners interested in this event.
	*/
	public void fireWSProjectClosed( IWSProject project, IEventPayload payLoad );

	/**
	 * Calling this method will result in the firing of any listeners interested in this event.
	*/
	public boolean fireWSProjectPreSave( IWSProject project, IEventPayload payLoad );

	/**
	 * Calling this method will result in the firing of any listeners interested in this event.
	*/
	public void fireWSProjectSaved( IWSProject project, IEventPayload payLoad );

	/**
	 * Calling this method will result in the firing of any listeners interested in this event.
	*/
	public boolean fireWSElementPreCreate( IWSProject wsProject, String location, String Name, String data, IEventPayload payLoad );

	/**
	 * Calling this method will result in the firing of any listeners interested in this event.
	*/
	public void fireWSElementCreated( IWSElement element, IEventPayload payLoad );

	/**
	 * Calling this method will result in the firing of any listeners interested in this event.
	*/
	public boolean fireWSElementPreSave( IWSElement wsProject, IEventPayload payLoad );

	/**
	 * Calling this method will result in the firing of any listeners interested in this event.
	*/
	public void fireWSElementSaved( IWSElement element, IEventPayload payLoad );

	/**
	 * Calling this method will result in the firing of any listeners interested in this event.
	*/
	public boolean fireWSElementPreRemove( IWSElement wsProject, IEventPayload payLoad );

	/**
	 * Calling this method will result in the firing of any listeners interested in this event.
	*/
	public void fireWSElementRemoved( IWSElement element, IEventPayload payLoad );

	/**
	 * Fired right after a WSElement's name has changed.
	*/
	public boolean fireWSElementPreNameChanged( IWSElement element, String proposedValue, IEventPayload payLoad );

	/**
	 * Fired right after a WSElement's name has changed.
	*/
	public void fireWSElementNameChanged( IWSElement element, IEventPayload payLoad );

	/**
	 * Fired whenever the owner of the WSElement is about to be changed.
	*/
	public boolean fireWSElementPreOwnerChange( IWSElement element, IWSProject newOwner, IEventPayload payLoad );

	/**
	 * Fired right after a WSElement's owner has changed.
	*/
	public void fireWSElementOwnerChanged( IWSElement element, IEventPayload payLoad );

	/**
	 * Fired whenever the location of the WSElement is about to be changed.
	*/
	public boolean fireWSElementPreLocationChanged( IWSElement element, String proposedLocation, IEventPayload payLoad );

	/**
	 * Fired right after a WSElement's location has changed.
	*/
	public void fireWSElementLocationChanged( IWSElement element, IEventPayload payLoad );

	/**
	 * Fired whenever the data of the WSElement is about to be changed.
	*/
	public boolean fireWSElementPreDataChanged( IWSElement element, String newData, IEventPayload payLoad );

	/**
	 * Fired right after a WSElement's data has changed.
	*/
	public void fireWSElementDataChanged( IWSElement element, IEventPayload payLoad );

	/**
	 * Fired whenever the documentation field of the WSElement is about to be changed.
	*/
	public boolean fireWSElementPreDocChanged( IWSElement element, String doc, IEventPayload payLoad );

	/**
	 * Fired right after a WSElement's documentation field has changed.
	*/
	public void fireWSElementDocChanged( IWSElement element, IEventPayload payLoad );

	/**
	 * Fired whenever a WSElement is about to be modified.
	*/
	public boolean fireWSElementPreModify( IWSElement wsProject, IEventPayload payLoad );

	/**
	 * Fired right after a WSElement was modified.
	*/
	public void fireWSElementModified( IWSElement element, IEventPayload payLoad );
	
	public boolean fireWSElementPreAliasChanged(IWSElement element, String newVal, IEventPayload payload);
	public void fireWSElementAliasChanged(IWSElement element, IEventPayload payload);
}
