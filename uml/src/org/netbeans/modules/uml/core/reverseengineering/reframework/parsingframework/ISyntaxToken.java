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

public interface ISyntaxToken
{
	/**
	 * The name of the syntax element.
	*/
	public String getName();

	/**
	 * The name of the syntax element.
	*/
	public void setName( String value );

	/**
	 * Specifies the type of the token.
	*/
	public int getKind();

	/**
	 * Specifies the type of the token.
	*/
	public void setKind( /* TokenKind */ int value );

	/**
	 * Defines a category that defines the token type.  The category can be used to further define the token type.
	*/
	public String getCategory();

	/**
	 * Defines a category that defines the token type.  The category can be used to further define the token type.
	*/
	public void setCategory( String value );

	/**
	 * Defines the type of the syntax token.
	*/
	public String getType();

	/**
	 * Defines the type of the syntax token.
	*/
	public void setType( String value );

}
