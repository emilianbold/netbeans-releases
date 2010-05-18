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
 * File       : IterationVariable.java
 * Created on : Oct 30, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.generativeframework;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class IterationVariable
    extends CompoundVariable
    implements IIterationVariable
{

    private IExpansionVariable m_ListVar = null;
    private String m_ListOption = null;
    private IExpansionVariable m_Var = null;
    private String m_VarOption = null;
    private String m_Delimiter = null;
    private String m_Literal = null;

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.generativeframework.IIterationVariable#getDelimiter()
     */
    public String getDelimiter()
    {
        return m_Delimiter;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.generativeframework.IIterationVariable#getListVariable()
     */
    public IExpansionVariable getListVariable()
    {
        return m_ListVar;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.generativeframework.IIterationVariable#getListVarName()
     */
    public String getListVarName()
    {
        return m_ListOption;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.generativeframework.IIterationVariable#getLiteral()
     */
    public String getLiteral()
    {
        return m_Literal;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.generativeframework.IIterationVariable#getVar()
     */
    public IExpansionVariable getVar()
    {
        return m_Var;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.generativeframework.IIterationVariable#getVarName()
     */
    public String getVarName()
    {
        return m_VarOption;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.generativeframework.IIterationVariable#setDelimiter(java.lang.String)
     */
    public void setDelimiter(String delim)
    {
        m_Delimiter = delim;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.generativeframework.IIterationVariable#setListVariable(org.netbeans.modules.uml.core.generativeframework.IExpansionVariable)
     */
    public void setListVariable(IExpansionVariable expVar)
    {
        m_ListVar = expVar;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.generativeframework.IIterationVariable#setListVarName(java.lang.String)
     */
    public void setListVarName(String listVarName)
    {
        m_ListOption = listVarName;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.generativeframework.IIterationVariable#setLiteral(java.lang.String)
     */
    public void setLiteral(String literal)
    {
        m_Literal = literal;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.generativeframework.IIterationVariable#setVar(org.netbeans.modules.uml.core.generativeframework.IExpansionVariable)
     */
    public void setVar(IExpansionVariable expVar)
    {
        m_Var = expVar;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.generativeframework.IIterationVariable#setVarName(java.lang.String)
     */
    public void setVarName(String varName)
    {
        m_VarOption = varName;
    }
    
    public String expand(Node context)
    {
        String results = null;

        validate();

        if(m_ListOption != null)
        {
            // Execute the variable specified in the ListName option
            m_ListVar = null;
            m_ListVar = retrieveVarNode(m_ListOption);

            if(m_ListVar != null)
            {
                String listResults = m_ListVar.expand(context);

                 ETList <Node> resultNodes = m_ListVar.getResultNodes();

                 if(resultNodes != null)
                 {
                    int numResultNodes = resultNodes.size();

                    if(numResultNodes > 0)
                    {
                        // Now retrieve the var to apply the result nodes to

                        String varResults = null;

                        if(m_VarOption != null)
                        {
                            m_ListVar = null;
                            
                            m_Var = retrieveVarNode(m_VarOption);

                            if(m_Var == null)
                            {
                                // If no var variable, then assume it is a pointer to a template
                                Node node = null;
                                for(int x = 0; x < numResultNodes; ++x)
                                {
                                    node = resultNodes.get(x);
                                    if(node != null)
                                    {
                                        String varResult = expandTemplateWithNode(m_VarOption, node);
           
                                        if(varResult != null)
                                        {
                                            varResults += varResult;

                                            if(x < numResultNodes - 1 )
                                            {
                                                appendDelimiter(varResults);
                                            }
                                        }
                                    }
                                }
                            }
                            else
                            {
                                Node node = null;
                                for(int x = 0; x < numResultNodes; ++x)
                                {
                                    node = resultNodes.get(x);
                                    if( node != null)
                                    {
                                        // Need to look into NOT caching away this
                                        // variable, as there seems to be no point in it.
                                        // Just causing us state management issues...

                                        m_Var.setResults(null);
                                        m_Var.setResultNodes(null);
                                        String varResult = m_Var.expand(node);

                                        if(varResult != null)
                                        {
                                            varResults += varResult;

                                            if(x < numResultNodes - 1)
                                            {
                                                appendDelimiter(varResults);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else
                        {
                            // Let's check to see if we have a literal

                            String literal = getLiteral();

                            if(literal != null)
                            {
                                // We do have one. So, for every node in results,
                                // append this literal value

                                for(int x = 0; x < numResultNodes; ++x)
                                {
                                    varResults += literal;
                                }
                            }
                        }                  
                        if(varResults != null)
                        {
                            m_Results = varResults;
                            results = varResults;
                        }
                    }
                }
            }
        }
        return results;
    }
    
    protected String appendDelimiter(String var)
    {
        if(var == null) return null;
        
        String pRetVal = new String(var);
        
        int count = m_Delimiter.length();
        
        if(m_Delimiter != null)
        {
            for( int  x = 0; x < count; x++ )
            {
                char c = m_Delimiter.charAt(x);

                if( c == '\\' )
                {
                    if( x + 1 < count)
                    {
                        char ahead = m_Delimiter.charAt(x+1);

                        if( ahead == 'n' )
                        {
                            pRetVal += '\n';
                            x++;
                        }
                        else if( ahead == 't' )
                        {
                            pRetVal += '\t';
                            x++;
                        }
                        else if( ahead == 'r' )
                        {
                            pRetVal += '\r';
                            x++;
                        }
                    }
                    else
                    {
                        pRetVal += c;
                    }
                }
                else
                {
                    pRetVal += c;
                }
            }
        }
        return pRetVal;
    }
    
    protected void validate()
    {
        if (m_Expander == null)
        {
            throw new IllegalStateException("Context not initialized");
        }
    }

}
