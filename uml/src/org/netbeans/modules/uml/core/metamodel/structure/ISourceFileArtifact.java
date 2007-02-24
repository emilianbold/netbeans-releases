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

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.roundtripframework.codegeneration.IFileSystemManipulation;
import org.netbeans.modules.uml.core.roundtripframework.codegeneration.ISourceCodeManipulation;


public interface ISourceFileArtifact extends IArtifact
{
	/**
	 * The source files language.
	*/
	public ILanguage getLanguage();

	/**
	 * The source file artifact's file name, minus any directory information
	*/
	public String getShortName();

	/**
	 * The disk drive that the source file artifact resides on
	*/
	public String getDrive();

	/**
	 * The source file artifact's base directory
	*/
	public String getBaseDirectory();

	/**
	 * The source file artifact's directory (with no trailing directory delimiter)
	*/
	public String getDirectory();

	/**
	 * Registers an Event Sink that will be notified whenever the ISourceFileArtifact's source code is changed.
	*/
	public void registerForSourceFileArtifactEvents( ISourceFileArtifactEventsSink pEventsSink );

	/**
	 * Revokes an Event Sink that has previously been registered
	*/
	public void revokeSourceFileArtifactSink( ISourceFileArtifactEventsSink pEventsSink );

	/**
	 * Fires the OnTextInserted event.
	*/
	public void fireTextInserted( int fileOffset, String insertedText );

	/**
	 * Fires the OnRangeDeleted event
	*/
	public void fireRangeDeleted( int rangeStart, int rangeEnd, String deletedText );

	/**
	 * Fires the OnRangeModified event
	*/
	public void fireRangeModified( int rangeStart, int rangeEnd, String originalText, String newText );

	/**
	 * Calculates the CRC of the ISourceFileArtifact's source code
	*/
	public int calculateCRC();

	/**
	 * Returns an ISourceCodeManipulation object useful for modifying this ISourceFileArtifact's source code
	*/
	public ISourceCodeManipulation modify( IFileSystemManipulation pFileSystemManipulation );

	/**
	 * Returns the Source Code for this Artifact
	*/
	public String getSourceCode();

	/**
	 * Gets / Sets SourceCodeHolder
	*/
	public ISourceCodeHolder getSourceCodeHolder();

	/**
	 * Gets / Sets SourceCodeHolder
	*/
	public void setSourceCodeHolder( ISourceCodeHolder value );

	/**
	 * Commits all changes made to the ISourceFileArtifact's source code
	*/
	public void commitChanges();

	/**
	 * Returns the Source Code for this Artifact
	*/
	public void setSourceCode( String value );

	/**
	 * The absolute path to the source file.
	*/
	public String getSourceFile();

	/**
	 * The absolute path to the source file.
	*/
	public void setSourceFile( String value );

	/**
	 * Fires the OnSourceFileNameChanged Event.
	*/
	public void fireSourceFileNameChanged( String oldFileName, String newFileName );

	/**
	 * Makes sure that the Artifact can be written to
	*/
	public boolean ensureWriteAccess();

}
