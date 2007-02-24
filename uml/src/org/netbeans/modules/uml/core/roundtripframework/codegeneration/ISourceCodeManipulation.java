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

public interface ISourceCodeManipulation
{
	/**
	 * Get/Set the Source Code
	*/
	public String getSourceCode();

	/**
	 * Get/Set the Source Code
	*/
	public void setSourceCode( String value );

	/**
	 * Deletes a range of characters from start to one before stop
	*/
	public long deleteRange( int start, int stop );

	/**
	 * Modifies a range of characters from start to one before stop
	*/
	public long modifyRange( int start, int stop, String newText );

	/**
	 * Inserts text
	*/
	public long insertText( int insertBefore, String insertedText );

	/**
	 * Commits all changes
	*/
	public long commitChanges();

	/**
	 * Determines if the specified range has already been modified.  The range must exactly matches the range of a previously made modification for this operation to return True.
	*/
	public boolean isModificationRange( int start, int stop );

	/**
	 * If the specified range exactly matches the range of a previously made modification, the text of that modification is returned
	*/
	public String getModificationText( int start, int stop );

	/**
	 * Set the cookie that represents a relationship between this object and its owning FileSystemManipulation object
	*/
	public void setCookie( int value );

	/**
	 * Set the FileSystemManipulation object that owns this SourceCodeManipulation object
	*/
	public void setFileSystemManipulation( IFileSystemManipulation value );

	/**
	 * Returns a substring of the source code where the range of the substring is specified using offsets into the original, unchanged source code.  However, the returned substring will have applied to it any modifications made to that substring
	*/
	public String getSourceCode( int rangeStart, int rangeEnd );

	/**
	 * Gets / Sets SourceFileArtifact
	*/
	public ISourceFileArtifact getSourceFileArtifact();

	/**
	 * Gets / Sets SourceFileArtifact
	*/
	public void setSourceFileArtifact( ISourceFileArtifact value );

}
