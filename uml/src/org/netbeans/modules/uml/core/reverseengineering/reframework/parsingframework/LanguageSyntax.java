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

import org.netbeans.modules.uml.core.reverseengineering.reframework.IDataTypeKind;
import org.netbeans.modules.uml.core.reverseengineering.reframework.ITokenKind;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;

/**
 * @author sumitabhk
 *
 */
public class LanguageSyntax implements ILanguageSyntax
{
	ETList<ISyntaxToken> m_Tokens = new ETArrayList<ISyntaxToken>();

	/**
	 * Retrieves the token information that defines a string literal.
	 *
	 * @param pVal [out] The token that delimites a string
	 */
	public ISyntaxToken getStringDelimiter()
	{
		ISyntaxToken retToken = null;

		// The string delimiter should always have the token type kind and a category of
		// string. 
		ETList<ISyntaxToken> pTokenList = getTokensByCategory(ITokenKind.DELIMITER, "String");
		if (pTokenList != null)
		{
			// There should only be one syntax token that matches the specified category.
			// However, even if there is more than one I will only use the first syntax token
			// that was defined.
			int count = pTokenList.size();
			if (count > 0)
			{
				retToken = (ISyntaxToken)pTokenList.get(0);
			}
		}
		
		return retToken;
	}

	/**
	 * Retrieve the token information that is used to define a character literal.
	 * 
	 * @param pVal [out] The token information
	 */
	public ISyntaxToken getCharacterDelimiter()
	{
		ISyntaxToken retToken = null;

		// The string delimiter should always have the token type kind and a category of
		// string. 
		ETList<ISyntaxToken> pTokenList = getTokensByCategory(ITokenKind.DELIMITER, "Character");
		if (pTokenList != null)
		{
			// There should only be one syntax token that matches the specified category.
			// However, even if there is more than one I will only use the first syntax token
			// that was defined.
			int count = pTokenList.size();
			if (count > 0)
			{
				retToken = (ISyntaxToken)pTokenList.get(0);
			}
		}
		
		return retToken;
	}

	/**
	 * Gets the set of tokens that define the syntax of a language.
	 * 
	 * @param pVal [out] The set of tokens
	 */
	public ETList<ISyntaxToken> getSyntaxTokens()
	{
		return m_Tokens;
	}

	/**
	 * Sets the set of tokens that define the syntax of a language.
	 * 
	 * @param newVal [in] The set of tokens
	 */
	public void setSyntaxTokens(ETList<ISyntaxToken> newVal)
	{
		m_Tokens = newVal;
	}

	/**
	 * Retrieves all the tokens that have the specified token type.
	 * 
	 * @param wantedKind [in] The token type to retrieve
	 * @param pVal [out] A token list of matching tokens
	 */
	public ETList<ISyntaxToken> getTokensByKind(int wantedKind)
	{
		ETList<ISyntaxToken> retVal = new ETArrayList<ISyntaxToken>();
		
		int count = m_Tokens.size();

		// Go through all of the syntax tokens and check if the token is the one
		// that we want.  If it is add it to the return list.
		for (int i=0; i<count; i++)
		{
			ISyntaxToken pToken = (ISyntaxToken)m_Tokens.get(i);
			int pKind = pToken.getKind();
			if (pKind == wantedKind)
			{
				retVal.add(pToken);
			}
		}
		
		return retVal;
	}

	/**
	 * Retrieves all the syntax tokens that have the specified token type and category.
	 * 
	 * @param wantedKind [in] The token type to retrieve
	 * @param wantedCategory [in] The token category to retrieve
	 * @param pVal [out] A token list of matching tokens
	 */
	public ETList<ISyntaxToken> getTokensByCategory(int wantedKind, 
													String wantedCategory)
	{
		ETList<ISyntaxToken> retVal = new ETArrayList<ISyntaxToken>();
		
		// I could just call GetTokensByKind then filter the returned collection based on the category.
		// However, I would then be checking two list.  So, to optimize I will only check the
		// list once and check both the kind and the category as I go.
		int count = m_Tokens.size();

		// Go through all of the syntax tokens and check if the token is the one
		// that we want.  If it is add it to the return list.
		for (int i=0; i<count; i++)
		{
			ISyntaxToken pToken = (ISyntaxToken)m_Tokens.get(i);
			int pKind = pToken.getKind();
			if (pKind == wantedKind)
			{
				// Only retrieve the category if we have a node that matches the desired kind.
				// This is again another optimization.  Only retrieve what we need to determine
				// if the node is a desired node.
				String pCategory = pToken.getCategory();
				if (pCategory.equals(wantedCategory))
				{
					retVal.add(pToken);
				}
			}
		}
		
		return retVal;
	}

}


