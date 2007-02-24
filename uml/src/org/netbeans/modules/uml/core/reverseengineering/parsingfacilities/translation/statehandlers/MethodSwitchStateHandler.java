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


/*
 * File       : MethodSwitchStateHandler.java
 * Created on : Dec 11, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IOpParserOptions;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.Expression;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class MethodSwitchStateHandler extends MethodConditionalStateHandler
{
    private boolean                 m_IsSwitch;
    private boolean                 m_IsInJumpTest;
    private Expression              m_JumpTestExpression = new Expression();
    private Node                    m_JumpTestNode;
    private ITokenDescriptor        m_pKeyword;
    private String                  m_JumpTest;

    
    public MethodSwitchStateHandler(String language)
    {
        super(language, false);
        m_IsInJumpTest = false;
    }
    
    public StateHandler createSubStateHandler(String stateName, String language) 
    {
        MethodDetailStateHandler retVal = null;
        
        if("Option Group".equals(stateName))
        {
            retVal = beginOptionGroup(stateName, language);
        }
        else if("Option".equals(stateName))
        {
            m_IsInJumpTest = false;      
            beginOption(stateName, language);
            retVal = this;
        }
        else
        {
            retVal = (MethodDetailStateHandler)super.createSubStateHandler
                    (stateName, language);

            if(retVal == null)
            {
                if("Jump Test".equals(stateName))
                {
                    m_IsInJumpTest = true;
                    beginJumpTest();
                    retVal = this;
                }  
                else if(m_IsInJumpTest)
                {
                    m_JumpTestExpression.addState(stateName, language);
                    retVal = this;
                }
            }
        }
        return retVal;
    }
    
    public void processToken(ITokenDescriptor pToken, String language) 
    {
        if(pToken == null) return;
        
        super.processToken(pToken, language);
        
        String type = pToken.getType();
        
        String value = pToken.getValue();
        
        if("Keyword".equals(type))
        {
            m_pKeyword = pToken;

            // I do not care about the HRESULT.
            handleKeyword(pToken);
        }
        else if(m_IsInJumpTest)
        {
            m_JumpTestExpression.addToken(pToken, language);
        }
    }
    
    public void stateComplete(String stateName) 
    {
        super.stateComplete(stateName);

        if("Jump Test".equals(stateName))
        {
            endJumpTest();
        }
        else if("Option Conditional".equals(stateName))
        {
            endCondtional("UML:SwitchAction");
        }   
    }    
    
    public String writeTestXMI(Node pNode)
    {
        String retVal = super.writeTestXMI(pNode);   
        m_JumpTest = retVal + " = ";
        return retVal;
    }
    
    protected void beginJumpTest() 
    {
        m_JumpTestNode = createNode("UML:Clause.test"); 
    }

    protected MethodDetailStateHandler beginOption(String stateName, String language) 
    {
        // No valid implementation in the C++ code base.
        return null;
    }

    protected MethodDetailStateHandler beginOptionGroup(String stateName, String language) 
    {
        MethodDetailStateHandler retVal = null;

        beginScope();
        
        IOpParserOptions pOptions = getOpParserOptions();
      
        retVal = StatementFactory.retrieveStatementHandler("Else Conditional",
                                                            language, 
                                                            pOptions,
                                                            getSymbolTable()); 


        if(retVal != null)
        {
            Node pCondClause = getClauseGroupNode();
            initializeHandler(retVal, pCondClause);
        }

        return retVal;
    }

    protected void endJumpTest() 
    {
        if(m_JumpTestNode != null)
        {         
            IREClass pThisClass = getClassBeingProcessed();

            IREClassLoader pLoader = getClassLoader();

            Node pData = m_JumpTestExpression.writeAsXMI(null, 
                                           m_JumpTestNode, 
                                           getSymbolTable(),
                                           pThisClass, 
                                           pLoader).getParamTwo();

            m_JumpTest = m_JumpTestExpression.toString();
            setNodeAttribute(m_JumpTestNode, "representation", m_JumpTest);   
        }
    }

    protected void endOption() 
    {
        Node pCondClause = getClauseGroupNode();
        setDOMNode(pCondClause);
    }

    protected String getConditionalNodeName() 
    {
        return "UML:SwitchAction";
    }

    protected String getClauseGroupNodeName() 
    {
        return "UML:ConditionalAction.switchClause";
    }
}
