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

import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;

public interface IStructureEventDispatcher extends IEventDispatcher
{
	/**
	 * Registers an event sink to handle IProjectEventsSink events.
	*/
	public void registerForProjectEvents( IProjectEventsSink handler );

	/**
	 * Removes a sink listening for IProjectEventsSink events.
	*/
	public void revokeProjectSink( IProjectEventsSink handler );
	
	/**
	 * Registers an event sink to handle IArtifactEventsSink events.
	*/
	public void registerForArtifactEvents( IArtifactEventsSink handler );

	/**
	 * Removes a sink listening for IArtifactEventsSink events.
	*/
	public void revokeArtifactSink( IArtifactEventsSink handler );

	/**
	 * Fired whenever the passed in Project's Mode property is about to change.
	*/
	public boolean firePreModeModified( IProject pProject, String newValue, IEventPayload payLoad );

	/**
	 * Fired whenever the passed in Project's Mode has been changed.
	*/
	public void fireModeModified( IProject pProject, IEventPayload payLoad );

	/**
	 * Fired whenever the passed in Project's DefaultLanguage property is about to change.
	*/
	public boolean firePreDefaultLanguageModified( IProject pProject, String newValue, IEventPayload payLoad );

	/**
	 * Fired whenever the passed in Project's DefaultLanguage property has been changed.
	*/
	public void fireDefaultLanguageModified( IProject pProject, IEventPayload payLoad );

	/**
	 * Fired right before a Project is created.
	*/
	public boolean fireProjectPreCreate( IWorkspace space, IEventPayload payLoad );

	/**
	 * Fired after a Project is created.
	*/
	public void fireProjectCreated( IProject Project, IEventPayload payLoad );

	/**
	 * Fired right before a Project is opened.
	*/
	public boolean fireProjectPreOpen( IWorkspace space, String fileName, IEventPayload payLoad );

	/**
	 * Fired after a Project is opened.
	*/
	public void fireProjectOpened( IProject Project, IEventPayload payLoad );

	/**
	 * Fired right before a Project is renamed.
	*/
	public boolean fireProjectPreRename( IProject Project, String newName, IEventPayload payLoad );

	/**
	 * Fired after a Project is renamed.
	*/
	public void fireProjectRenamed( IProject Project, String oldName, IEventPayload payLoad );

	/**
	 * Fired right before a Project is closed.
	*/
	public boolean fireProjectPreClose( IProject Project, IEventPayload payLoad );

	/**
	 * Fired after a Project has been closed.
	*/
	public void fireProjectClosed( IProject Project, IEventPayload payLoad );

	/**
	 * Fired right before a Project is saved.
	*/
	public boolean fireProjectPreSave( IProject Project, IEventPayload payLoad );

	/**
	 * Fired after a Project is saved.
	*/
	public void fireProjectSaved( IProject Project, IEventPayload payLoad );	

	/**
	 * Fired right before an Artifact's file name is modified.
	*/
	public boolean fireArtifactFileNamePreModified( IArtifact Artifact, String newName, IEventPayload payLoad );

	/**
	 * Fired after an Artifact's file name is modified.
	*/
	public void fireArtifactFileNameModified( IArtifact Artifact, String oldName, IEventPayload payLoad );

	/**
	 * Fired whenever the passed in Artifact's contents are about to become dirty.
	*/
	public boolean fireArtifactPreDirty( IArtifact pArtifact, IEventPayload payLoad );

	/**
	 * Fired whenever the passed in Artifact's contents are dirty.
	*/
	public void fireArtifactDirty( IArtifact pArtifact, IEventPayload payLoad );

	/**
	 * Fired whenever the passed in Artifact is about to be saved.
	*/
	public boolean fireArtifactPreSave( IArtifact pArtifact, String fileName, IEventPayload payLoad );

	/**
	 * Fired whenever the passed in Artifact has been saved.
	*/
	public void fireArtifactSave( IArtifact pArtifact, String fileName, IEventPayload payLoad );

	/**
	 * Fired right before a referenced library location is added to the Project.
	*/
	public boolean firePreReferencedLibraryAdded( IProject Project, String refLibLoc, IEventPayload payLoad );

	/**
	 * Fired right after a referenced library locatiFire is added to the Project.
	*/
	public void fireReferencedLibraryAdded( IProject Project, String refLibLoc, IEventPayload payLoad );

	/**
	 * Fired right before a referenced library locatiFire is removed from the Project.
	*/
	public boolean firePreReferencedLibraryRemoved( IProject Project, String refLibLoc, IEventPayload payLoad );

	/**
	 * Fired right after a referenced library locatiFire is removed from the Project.
	*/
	public void fireReferencedLibraryRemoved( IProject Project, String refLibLoc, IEventPayload payLoad );

}
