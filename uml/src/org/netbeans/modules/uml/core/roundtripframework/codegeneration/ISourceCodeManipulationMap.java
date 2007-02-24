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

package org.netbeans.modules.uml.core.roundtripframework.codegeneration;

import org.netbeans.modules.uml.core.metamodel.structure.ISourceFileArtifact;

public interface ISourceCodeManipulationMap
{
	/**
	 * Returns a ISourceCodeManipulationMap object for the given ISourceFileArtifact object
	*/
	public long getSourceCodeManipulator( ISourceFileArtifact pArtifact, ISourceCodeManipulation ppManipulation );

	/**
	 * The changes made to every ISourceFileArtifact in the map are committed to disk.
	*/
	public long commitAllChanges();

	/**
	 * The changes made to the given ISourceFileArtifact object are committed to disk.
	*/
	public long commitChanges( ISourceFileArtifact pArtifact );

	/**
	 * Removes an entry from the map.  Any changes made to the ISourceFileArtifact's code are lost.
	*/
	public long removeSourceCodeManipulator( ISourceFileArtifact pArtifact );

	/**
	 * Gets / Sets FileSystemManipulation
	*/
	public IFileSystemManipulation getFileSystemManipulation();

	/**
	 * Gets / Sets FileSystemManipulation
	*/
	public void setFileSystemManipulation( IFileSystemManipulation value );

	/**
	 * Gets / Sets AutoCommit
	*/
	public boolean getAutoCommit();

	/**
	 * Gets / Sets AutoCommit
	*/
	public void setAutoCommit( boolean value );

}
