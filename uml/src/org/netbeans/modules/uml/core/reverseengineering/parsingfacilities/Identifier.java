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


/*
 * File       : Identifier.java
 * Created on : Dec 8, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities;

import java.util.Stack;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * Represent a source code identifier.  A identifier is made up of two type of tokens.
 * The <I>name token</I> and the <I>scope operator</I> token.  The name token 
 * represents either a package name or a class  name.  The scope operator token represents
 * the token that seperates name tokens.  The ITokenDescriptor token that represent the
 * scope operator must have a type of <B>"Scope Operator"</B>.
 */
public class Identifier
{
    /**
     * Adds a new token of the identifer.  The name tokens and the scope operator
     * tokens are used to build a identifier.
     * 
     * @param pToken [in] The token.
     */
    public void addToken(ITokenDescriptor token)
    {
        if (!"Scope Operator".equals(token.getType()))
        {
            m_TokenList.add(token);
            if (m_HoldingOperator.size() > 0)
                m_TokenList.add( m_HoldingOperator.pop() );
        }
        else
        {    
            m_HoldingOperator.push( token );
        }
    }

    /**
     * Clears the contents of the indentifier.  The identifier will be an empty 
     * string.
     */
    public void clear()
    {
        m_TokenList.clear();
    }

    /**
     * The position that contained the Identifier.
     * 
     * @param The position number.
     */
    public long getEndPosition()
    {
        if (m_TokenList.size() > 0)
        {
            ITokenDescriptor tok = m_TokenList.get(0);
            // The end position of the identifier is after the last token.  
            // Therefore, I have to tack on the length of the token to 
            // report the end position of the token.
            if (tok != null)
                return tok.getPosition() + tok.getValue().length();
        }
        return -1L;
    }

    /**
     * Retrieves the identifier as it appeared in the source code.  The name and
     * scope operator tokens are used to derive the value.
     *
     * @return The identifer as it appeared in source.
     */
    public String getIdentifierAsSource()
    {
        return getAnnotatedIdentifier(null);
    }

    /**
     * Retrieves the identifier as it represented in UML.  In UML the scope operator
     * is always "::".
     *
     * @return The UML version of the identifier.
     */
    public String getIdentifierAsUML()
    {
        return getAnnotatedIdentifier("::");
    }

    public String getIdentifierAsUML(String DUMMY_FLAG)
    {
        return getAnnotatedIdentifier(DUMMY_FLAG);
    }
    
    /**
     * The length of the identifer.  The length of the identifer will reflect the
     * source code version of the identifier.
     */
    public int getLength()
    {
        return (int) (getEndPosition() - getStartPosition());
    }

    /**
     * The column that contained the Identifier.
     * 
     * @param The column number.
     */
    public int getStartColumn()
    {
        if (m_TokenList.size() > 0)
        {
            ITokenDescriptor tok = m_TokenList.get(0);
            if (tok != null)
                return tok.getColumn();
        }
        return -1;
    }

    /**
     * The line that contained the Identifier.
     * 
     * @param The line number.
     */
    public int getStartLine()
    {
        if (m_TokenList.size() > 0)
        {    
            ITokenDescriptor tok = m_TokenList.get(0);
            if (tok != null)
                // The ANTLR line number are One based.  We need them to be 
                // Zero based.
                return tok.getLine() - 1;
        }
        return -1;
    }

    /**
     * The position that contained the Identifier.
     * 
     * @param The position number.
     */
    public long getStartPosition()
    {
        if (m_TokenList.size() > 0)
        {
            ITokenDescriptor tok = m_TokenList.get(0);
            if (tok != null)
                // The ANTLR line number are One based.  We need them to be 
                // Zero based.
                return tok.getPosition();
        }
        return -1L;
        
    }

    /**
     * Retrieve the list of tokens that make up the identifer.
     *
     * @return 
     */
    public ETList<ITokenDescriptor> getTokenList()
    {
        return m_TokenList;
    }
    
    /**
     * Replaces the scope operator with a specified replacement.  This is
     * mostly useful when creating a UML version of the identifier.  If the
     * last token is a scope operator it will not be added to the returned
     * identifier representation.
     *
     * Reason:
     * 1) An error occured.
     * 2) The identifier is a java import statement identifer.  In
     *    that case the OnDemand Operator was not added to the
     *    identifier.
     * 
     * @param scopeOpReplacement [in] The string to replace the scope operator.
     */
    protected String getAnnotatedIdentifier(String replScope)
    {
        if (m_TokenList == null) return null ;
   
        StringBuffer ret = new StringBuffer();
        
        //kris richards - this is a terrible hack. The 
        //MethodExceptionProcessingStateHandler.addParameterToSymbolTable()
        //method is using this incorrectly to name the expression. So, I have
        //make the naming work.
        if (replScope !=null && replScope.equals ("DUMMY_FLAG")) {
            int lastIndex = m_TokenList.getCount()-1 ;
            if (lastIndex >= 0) {
                ITokenDescriptor tok = m_TokenList.get (lastIndex);
                if (tok != null)
                    return tok.getValue() ;
                else 
                    return null ;
            }
        }
        
        for (int i = 0, count = m_TokenList.size(); i < count; ++i)
        {
            ITokenDescriptor tok = m_TokenList.get(i);
            if (tok == null) continue;

            if (!"Scope Operator".equals(tok.getType()))
            {
                String val = tok.getValue();
                if (val != null && val.length() > 0)
                    ret.append(val);
            }
            // If the scope operator is the last token I do not want to 
            // include it.  See the method comment for more details.
            else if (i + 1 < count)
            {
                if (replScope == null || replScope.length() == 0)
                    ret.append(tok.getValue());
                else
                    // Change the scope operator to "::"
                    ret.append(replScope);
            }
        }
        return ret.toString();
    }
    
    private ETList<ITokenDescriptor> m_TokenList = 
                    new ETArrayList<ITokenDescriptor>();
    private Stack<ITokenDescriptor>  m_HoldingOperator =
                    new Stack<ITokenDescriptor>();
}
