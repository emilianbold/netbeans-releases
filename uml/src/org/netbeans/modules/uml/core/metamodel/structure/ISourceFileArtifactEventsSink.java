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

public interface ISourceFileArtifactEventsSink
{
	/**
	 * Called when text is inserted into an ISourceFileArtifact's source code
	*/
	public void onTextInserted( int fileOffset, String insertedText );

	/**
	 * Called when a range of text is deleted from an ISourceFileArtifact's source code
	*/
	public void onRangeDeleted( int rangeStart, int rangeEnd, String deletedText );

	/**
	 * Called when a range of text in an ISourceFileArtifact's source code is replaced with another piece of text.
	*/
	public void onRangeModified( int rangeStart, int rangeEnd, String originalText, String newText );

	/**
	 * Called when changes should be committed.
	*/
	public void onCommitChanges();

	/**
	 * Called when the source file artifact has been renamed
	*/
	public void onSourceFileNameChanged( String oldName, String newName );

}
