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

import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;
import org.netbeans.modules.uml.core.support.umlutils.IDataFormatter;

public interface ICGChangeNotification
{
	/**
	 * Initializes a Change Notification from an IChangeRequest object
	*/
	public void _Initialize( IChangeRequest pVal );

	/**
	 * Gets / Sets CodeGenerationHelper
	*/
	public ICodeGenerationHelper getCodeGenerationHelper();

	/**
	 * Gets / Sets Language
	*/
	public String getLanguage();

	/**
	 * Gets / Sets Language
	*/
	public void setLanguage( String value );

	/**
	 * Gets / Sets DataFormatter
	*/
	public IDataFormatter getDataFormatter();

	/**
	 * Gets / Sets DataFormatter
	*/
	public void setDataFormatter( IDataFormatter value );

	/**
	 * Gets / Sets FileSystemManipulation
	*/
	public IFileSystemManipulation getFileSystemManipulation();

	/**
	 * Gets / Sets FileSystemManipulation
	*/
	public void setFileSystemManipulation( IFileSystemManipulation value );

	/**
	 * Gets / Sets SourceCodeManipulationMap
	*/
	public ISourceCodeManipulationMap getSourceCodeManipulationMap();

	/**
	 * Gets / Sets SourceCodeManipulationMap
	*/
	public void setSourceCodeManipulationMap( ISourceCodeManipulationMap value );

	/**
	 * Returns the IChangeRequest object that this ICGChangeNotification wraps
	*/
	public IChangeRequest getRoundTripChangeRequest();

}
