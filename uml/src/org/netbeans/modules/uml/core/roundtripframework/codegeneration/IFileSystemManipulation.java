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

public interface IFileSystemManipulation
{
	/**
	 * Creates a File
	*/
	public boolean createFile( String filePath );

	/**
	 * Deletes a File
	*/
	public boolean deleteFile( String filePath );

	/**
	 * Creates a Directory
	*/
	public boolean createDirectory( String directory );

	/**
	 * Deletes a Directory
	*/
	public boolean deleteDirectory( String directory );

	/**
	 * Returns SourceCodeManipulation object that allows a file to be modified
	*/
	public ISourceCodeManipulation modifyFile( String filePath );

	/**
	 * Moves a file
	*/
	public boolean moveFile( String sourcePath, String destinationPath );

	/**
	 * Moves a directory and all of its contents.  Destination path is created if it does not exist.
	*/
	public boolean moveDirectory( String sourcePath, String destinationPath, boolean overwrite );

	/**
	 * Unregisters the SourceCodeManipulation object that was given this cookie
	*/
	public long unregister( int Cookie );

	/**
	 * Returns the file that the cookie holder should save their file to
	*/
	public String getFileName( int Cookie );

}
