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


package org.netbeans.modules.uml.core.generativeframework;

import java.util.Stack;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;

/**
 * @author sumitabhk
 */
public class ExpansionVarLocator {
    private static final int PS_OPTIONAL   = 0;
    private static final int PS_GATHERING  = 1;
    private static final int PS_COMMENT    = 2;
    private static final int PS_ESCAPE     = 3;
    private static final int PS_VAR        = 4;

	/**
	 * @param manager
	 * @param templateFile
	 * @param templateBuffer
	 * @param contextElement
	 */
	public ExpansionVarLocator(ITemplateManager manager, 
                               String filename, 
                               String templateBuffer, 
                               IElement contextElement) 
    {
        m_Manager = manager;
        m_Filename = filename;
        m_OrigBuffer = templateBuffer;
        m_ContextElement = contextElement;
        
        if (m_ContextElement != null)
            m_ContextNode = m_ContextElement.getNode();
        
        initialize();
	}
    
    /**
     * @param manager
     * @param templateFile
     * @param templateBuffer
     * @param contextNode
     */
    public ExpansionVarLocator(ITemplateManager manager, 
                               String filename, 
                               String templateBuffer, 
                               Node contextNode)
    {
        m_Manager = manager;
        m_Filename = filename;
        m_OrigBuffer = templateBuffer;
        m_ContextNode = contextNode;
        
        initialize();
    }
    
    private void initialize()
    {
        m_State.push( new Integer( PS_GATHERING ) );
        establishContext();
    }
    
    private void establishContext()
    {
        IVariableFactory fact = getFactory();
        if (fact != null)
        {
            String config = m_Manager.getConfigLocation();
            
            IVariableExpander expander = new VariableExpander();
            expander.setConfigFile(config);
            expander.setManager(m_Manager);
            
            fact.setExecutionContext(expander);
        }
    }
    
    private IVariableFactory getFactory()
    {
        return m_Manager != null? m_Manager.getFactory() : null;
    }
    
	/**
	 * @return
	 */
	public String expandVars() {
        int length = m_OrigBuffer.length();
        for ( ; m_CurIndex < length; ++m_CurIndex )
        {
            char c = m_OrigBuffer.charAt(m_CurIndex);
            switch (c)
            {
            case '!':
                if (lookAhead() == '%')
                    gatherOptionalVar();
                break;
            case '/':
                if (lookAhead() == '%')
                    gatherComment();
                else
                    appendResult(c);
                break;
            case '\\':
            {
                char ahead = lookAhead();
                if ("%!/\\".indexOf(ahead) != -1)
                {
                    m_CurIndex++;
                    appendResult(ahead);
                }
                else
                {
                    appendResult(c);
                }
                break;
            }
            case '%':
            {
                char ahead = lookAhead();
                switch (m_State.peek().intValue())
                {
                case PS_GATHERING:
                    if (ahead == '%')
                        gatherVar();
                    break;
                case PS_VAR:
                    if (ahead == '%')
                        popVar();
                    break;
                case PS_OPTIONAL:
                    if (ahead == '!')
                        popOptionalVar();
                    else if (ahead == '%')
                        gatherVar();
                    break;
                }
                break;
            }
            default:
                appendResult(c);
                break;
            }
        }
        
        return m_FinalResults.toString();
	}
    
    private void gatherComment()
    {
        // We just eat comments
        m_CurIndex++; // Eat the '%' right after the '/'

        // Now find the newline character
        int end = m_OrigBuffer.indexOf('\n', m_CurIndex);
        if (end == -1) end = m_OrigBuffer.length() - 1;

        m_CurIndex = end + 1;
    }
    
    private void gatherOptionalVar()
    {
        m_State.push( new Integer(PS_OPTIONAL) );
        m_CurVar.push( "" );
        IVariableExpander expander = getExecutionContext();
        if (expander != null)
            expander.beginGathering();
        eatMarkers();
    }
    
    private char lookAhead()
    {
        int nextIndex = m_CurIndex + 1;
        return nextIndex < m_OrigBuffer.length()? 
									m_OrigBuffer.charAt(nextIndex) : 0;
    }

    private void setResult(String result)
    {
        if (m_CurVar.size() > 0)
        {    
            m_CurVar.pop();
            m_CurVar.push(result);
        }
        else
        {    
            m_FinalResults = new StringBuffer(result);
        }
    }

    private void gatherVar()
    {
        m_State.push( new Integer( PS_VAR ) );
        m_CurVar.push( "" );
        eatMarkers();
    }
    
    private void eatMarkers()
    {
        m_CurIndex++;   // Eat the second '%'
    }
    
    private void popOptionalVar()
    {
        m_State.pop();
        m_CurIndex++;   // Eat the second '%'
        
        String curVar = m_CurVar.pop();
        IVariableExpander expander = getExecutionContext();
        if (expander != null)
        {
            if (expander.endGathering())
                appendResult(curVar);
        }
        else
        {    
            appendResult(curVar);
        }
    }
    
    private void popVar()
    {
        m_State.pop();
        m_CurIndex++;
        String curVar = m_CurVar.pop();
        expandVariable(curVar);
    }
    
    private void expandVariable(String var)
    {
        if (var != null && var.length() > 0 && m_Manager != null)
        {
            IVariableFactory factory = m_Manager.getFactory();
            if (factory != null)
            {
                IExpansionVariable newVar = factory.createVariableWithText(var);
                if (newVar != null)
                {
                    IVariableExpander expander = factory.getExecutionContext();
                    if (expander != null)
                    {
                        String curResults = retrieveGatheredText();
                        String result = 
                            expander.expand(curResults, newVar, m_ContextNode);
                        if (result != null && result.length() > 0)
                            setResult(result);
                    }
                }
            }
        }
    }
    
    private String retrieveGatheredText()
    {
        return m_CurVar.size() > 0? m_CurVar.peek() : m_FinalResults.toString();
    }
    
    private IVariableExpander getExecutionContext()
    {
        if (m_Manager != null)
        {
            IVariableFactory fact = m_Manager.getFactory();
            return fact != null? fact.getExecutionContext() : null;
        }
        return null;
    }
    
    private void appendResult(char c)
    {
        if (m_CurVar.size() > 0)
            m_CurVar.push(m_CurVar.pop() + c);
        else
            m_FinalResults.append(c);
    }
    
    private void appendResult(String result)
    {
        if (m_CurVar.size() > 0)
            m_CurVar.push( Formatter.convertNewLines(m_CurVar.pop(), result) );
        else
            m_FinalResults.append(
                    Formatter.convertNewLines(
                            m_FinalResults.toString(), result) );
    }
    
    public ITemplateManager getManager()
    {
        return m_Manager;
    }

    private Stack<String>   m_CurVar = new Stack<String>();
    private Stack<Integer>  m_State  = new Stack<Integer>();
    private int             m_CurIndex;
    private IElement        m_ContextElement;
    private Node            m_ContextNode;
    private StringBuffer    m_FinalResults = new StringBuffer();
    private String          m_OrigBuffer;
    private ITemplateManager m_Manager;
    private String          m_Filename;
}
