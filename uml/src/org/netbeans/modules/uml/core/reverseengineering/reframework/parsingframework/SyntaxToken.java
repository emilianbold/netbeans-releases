/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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


