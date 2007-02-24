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

package org.netbeans.modules.uml.core.scm;

/**
 * Interface that SCM tools must support.
 */
public interface ISCMTool extends ISCMFeatureAvailability
{
	/**
	 * Initializes the SCM tool with information about the client calling into its services.
	*/
	public void initialize( Object data, String callerName );

   /**
	 * Sets the absolute path to the directory the SCM tool will work from.
    * The project name is not specifed.
	*/
	public void setWorkingDirectory( String data);

	/**
	 * Sets the absolute path to the directory the SCM tool will work from.
	*/
	public void setWorkingDirectory( String data, String projectName );

	/**
	 * Retrieves the SCM ID. Initialize and SetWorkingDirectory must first be called.
	*/
	public String getSCMID();

	/**
	 * Retrieves the SCM ID. Initialize and SetWorkingDirectory must first be called.
	*/
	public void setSCMID( String value );

	/**
	 * Is this tool available for feature requests?.
	*/
	public boolean getIsAvailable();

}
