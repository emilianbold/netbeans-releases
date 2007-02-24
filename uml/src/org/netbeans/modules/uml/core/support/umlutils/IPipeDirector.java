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

package org.netbeans.modules.uml.core.support.umlutils;

public interface IPipeDirector
{
	/**
	 * Launches an external application and captures output that goes to stdout.
	*/
	public long gatherOutput( String arguments );

	/**
	 * The directory where the command will be executed.
	*/
	public String getDirectory();

	/**
	 * The directory where the command will be executed.
	*/
	public void setDirectory( String value );

	/**
	 * Launches an external application and captures output that goes to stdout as well as the exit code of the application.
	*/
	public int gatherOutput2( String arguments, boolean gotExitCode );

}
