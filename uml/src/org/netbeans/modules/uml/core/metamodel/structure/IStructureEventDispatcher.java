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
