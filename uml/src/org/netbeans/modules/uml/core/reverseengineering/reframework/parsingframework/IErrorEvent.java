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

package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;

public interface IErrorEvent
{
	/**
	 * Retrieves/Sets the line number that contains the parser error.
	*/
	public int getLineNumber();

	/**
	 * Retrieves/Sets the line number that contains the parser error.
	*/
	public void setLineNumber( int value );

	/**
	 * Retrieves/Sets the column number that contains the parser error.
	*/
	public int getColumnNumber();

	/**
	 * Retrieves/Sets the column number that contains the parser error.
	*/
	public void setColumnNumber( int value );

	/**
	 * Retrieves/Sets a the description of the error.
	*/
	public String getErrorMessage();

	/**
	 * Retrieves/Sets a the description of the error.
	*/
	public void setErrorMessage( String value );

	/**
	 * Sets/Gets the name of the file that contains the error.
	*/
	public String getFilename();

	/**
	 * Sets/Gets the name of the file that contains the error.
	*/
	public void setFilename( String value );

	/**
	 * Retrieves the error event in a foramted string.  The error message will include the error number and column number.
	*/
	public String getFormattedMessage();
}
