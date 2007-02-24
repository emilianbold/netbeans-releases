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

/**
 * @author sumitabhk
 *
 */
public class SyntaxToken implements ISyntaxToken
{
	private String m_Category = null;
	private int m_Kind = 0;
	private String m_Name = null;
	private String m_Type = null;

	/**
	 * Gets the name of the syntax element.
	 *
	 * @param pVal [out] The name of the token.
	 */
	public String getName()
	{
		return m_Name;
	}

	/**
	 * Sets the name of the syntax element.
	 *
	 * @param newVal [in] The name of the token.
	 */
	public void setName(String newVal)
	{
		m_Name = newVal;
	}

	/**
	 * Retrieves the type of the token.
	 * 
	 * @param pVal [out] The token type.
	 */
	public int getKind()
	{
		return m_Kind;
	}

	/**
	 * Sets the type of the token.
	 * 
	 * @param newVal [int] The token type.
	 */
	public void setKind(int newVal)
	{
		m_Kind = newVal;
	}

	/**
	 * Gets the category that defines the token type.  The category 
	 * can be used to further define the token type.
	 *
	 * @param pVal [out] The token's category.
	 */
	public String getCategory()
	{
		return m_Category;
	}

	/**
	 * Sets the category that defines the token type.  The category 
	 * can be used to further define the token type.
	 *
	 * @param newVal [in] The token's category.
	 */
	public void setCategory(String newVal)
	{
		m_Category = newVal;
	}

	/**
	 * Gets the type of the syntax token.
	 *
	 * @param pVal [out] The token's type.
	 */
	public String getType()
	{
		return m_Type;
	}

	/**
	 * Sets the type of the syntax token.
	 *
	 * @param newVal [in] The token's type.
	 */
	public void setType(String newVal)
	{
		m_Type = newVal;
	}
}


