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
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface ILanguageSyntax
{
	/**
	 * Retrieves the token information that defines a string literal.
	*/
	public ISyntaxToken getStringDelimiter();

	/**
	 * Retrieve the token information that is used to define a character literal.
	*/
	public ISyntaxToken getCharacterDelimiter();

	/**
	 * The set of tokens that define the syntax of a language.
	*/
	public ETList<ISyntaxToken> getSyntaxTokens();

	/**
	 * The set of tokens that define the syntax of a language.
	*/
	public void setSyntaxTokens( ETList<ISyntaxToken> value );

	/**
	 * Retrieves all the tokens that have the specified token type.
	*/
	public ETList<ISyntaxToken> getTokensByKind( /* TokenKind */ int Kind );

	/**
	 * Retrieves all the syntax tokens that have the specified token type and category.
	*/
	public ETList<ISyntaxToken> getTokensByCategory( /* TokenKind */ int Kind, String Category );

}
