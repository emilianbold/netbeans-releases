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

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

public interface IArtifactEventsSink
{
	/**
	 * Fired whenever the passed in Artifact's file name is about to change.
	*/
	public void onPreFileNameModified( IArtifact pArtifact, String newFileName, IResultCell cell );

	/**
	 * Fired whenever the passed in Artifact's file name has been changed.
	*/
	public void onFileNameModified( IArtifact pArtifact, String oldFileName, IResultCell cell );

	/**
	 * Fired whenever the passed in Artifact's contents are about to become dirty.
	*/
	public void onPreDirty( IArtifact pArtifact, IResultCell cell );

	/**
	 * Fired whenever the passed in Artifact's contents are dirty.
	*/
	public void onDirty( IArtifact pArtifact, IResultCell cell );

	/**
	 * Fired whenever the passed in Artifact is about to be saved.
	*/
	public void onPreSave( IArtifact pArtifact, String fileName, IResultCell cell );

	/**
	 * Fired whenever the passed in Artifact has been saved.
	*/
	public void onSave( IArtifact pArtifact, String fileName, IResultCell cell );

}
