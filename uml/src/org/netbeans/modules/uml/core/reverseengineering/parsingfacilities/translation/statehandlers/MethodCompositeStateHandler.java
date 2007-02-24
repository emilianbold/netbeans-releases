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
 * File       : MethodCompositeStateHandler.java
 * Created on : Dec 10, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IOpParserOptions;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.Expression;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * @author Aztec
 */
public class MethodCompositeStateHandler extends MethodDetailStateHandler
{
    Expression m_TestConditionExpression = new Expression();
    Expression m_InitializerExpression = new Expression();
    Expression m_PostProcessingExpression = new Expression();

    
    MethodCompositeStateHandler(String language)
    {
        super(language);
    }
    
    //  *********************************************************************
    //  Token Methods
    //  *********************************************************************
    
    protected void addTestConditionToken(ITokenDescriptor pToken, String language)
    {
        m_TestConditionExpression.addToken(pToken, language);
    }
    
    
    protected void addInitializerToken(ITokenDescriptor pToken, String language)
    {
        m_InitializerExpression.addToken(pToken, language);
    }
    
    protected void addPostProcessingToken(ITokenDescriptor pToken, String language)
    {
        m_PostProcessingExpression.addToken(pToken, language);
    }
    
    //  *********************************************************************
    //  Add State Methods
    //  *********************************************************************
    
    protected void addTestConditionState(String stateName, String language)
    {
        m_TestConditionExpression.addState(stateName, language);
    }
    
    protected void addInitializerState(String stateName, String language)
    {
        m_InitializerExpression.addState(stateName, language);
    }
    
    protected void addPostProcessingState(String stateName, String language)
    {
        m_PostProcessingExpression.addState(stateName, language);
    }

    //  *********************************************************************
    //  End State Methods
    //  *********************************************************************

    protected void endTestConditionState(String stateName)
    {
        m_TestConditionExpression.endState(stateName);
    }

    protected void endInitializerState(String stateName)
    {
        m_InitializerExpression.endState(stateName);
    }

    protected void endPostProcessingState(String stateName)
    {
        m_PostProcessingExpression.endState(stateName);
    }
    
    //  *********************************************************************
    //  Write XMI Methods
    //  *********************************************************************
    protected String writeTestXMI(Node pNode)
    {
        String retVal = m_TestConditionExpression.toString();  
        long line = m_TestConditionExpression.getStartLine();

        if(pNode != null)
        {
            IREClass pThisClass = getClassBeingProcessed();

            IREClassLoader pLoader = getClassLoader();

            Node pData = m_TestConditionExpression.writeAsXMI(
                                                null, 
                                                pNode, 
                                                getSymbolTable(),
                                                pThisClass, 
                                                pLoader).getParamTwo();
      
            setNodeAttribute(pNode, "representation", retVal);        
            setNodeAttribute(pNode, "line", Long.toString(line - 1L));
        }
        return retVal;
    }

    protected String writeInitializeXMI(Node pNode)
    {
        String retVal = m_InitializerExpression.toString();   

        if(pNode != null)
        {
            IREClass pThisClass = getClassBeingProcessed();

            IREClassLoader pLoader = getClassLoader();

            Node pData = m_InitializerExpression.writeAsXMI(
                                                null, 
                                                pNode, 
                                                getSymbolTable(),
                                                pThisClass, 
                                                pLoader).getParamTwo();
      
            setNodeAttribute(pNode, "representation", retVal);        
        }
        return retVal;
    }

    protected String writePostProcessingXMI(Node pNode)
    {
        String retVal = m_PostProcessingExpression.toString();   
       
        if(pNode != null)
        {
            IREClass pThisClass = getClassBeingProcessed();

            IREClassLoader pLoader = getClassLoader();

            Node pData = m_PostProcessingExpression.writeAsXMI(
                                                null, 
                                                pNode, 
                                                getSymbolTable(),
                                                pThisClass, 
                                                pLoader).getParamTwo();
      
            setNodeAttribute(pNode, "representation", retVal);        
        }
        return retVal;
    }
    
    //  *********************************************************************
    //  Send Event Methods
    //  *********************************************************************

    protected InstanceInformation sendInitializationEvents()
    {
        InstanceInformation retVal = null;

        IOpParserOptions pOptions = getOpParserOptions();

        if(pOptions != null)
        {
            if(pOptions.isProcessInit())
            {
                retVal = sendOperationEvents(m_InitializerExpression);
            }
        }
        return retVal;
 }

    protected InstanceInformation sendTestEvents()
    {
        InstanceInformation retVal = null;

            IOpParserOptions pOptions = getOpParserOptions();

            if(pOptions != null)
            {
                if(pOptions.isProcessTest())
                {
                    retVal = sendOperationEvents(m_TestConditionExpression);
                }
            }
            return retVal;
    }


    protected InstanceInformation sendPostProcessingEvents()
    {
        InstanceInformation retVal = null;

            IOpParserOptions pOptions = getOpParserOptions();

            if(pOptions != null)
            {
                if(pOptions.isProcessPost())
                {
                    retVal = sendOperationEvents(m_PostProcessingExpression);
                }
            }
            return retVal;
    }


    protected InstanceInformation sendOperationEvents(Expression expression)
    {
        InstanceInformation retVal = null;

        IUMLParserEventDispatcher pDispatcher = getEventDispatcher();
        
        if(pDispatcher != null)
        {

            IREClass pThisClass = getClassBeingProcessed();

            IREClassLoader pLoader = getClassLoader();

            Node pNode = getDOMNode();
            retVal = expression.sendOperationEvents(getReferenceInstance(), 
                                                    pThisClass,
                                                    getSymbolTable(), 
                                                    pLoader, 
                                                    pDispatcher,
                                                    pNode);
        }
        return retVal;
    }
    
    /**
     * Responsible to initializing a new scope.
     */
    protected void beginScope()
    {
        getSymbolTable().pushScope();
    }

    /**
     * Responsible to cleaning up before a scope is existed.
     */
    protected void endScope()
    {
        Node pNode = getDOMNode();
        getSymbolTable().popScope(pNode);
    }


    /**
     * Adds the <I>Keyword</I> token descriptor.  For method details I am
     * also added the <I>line</I> attribute to the XML DOM Node.
     *
     * @param pToken [in] The token information.
     */
    protected void handleKeyword(ITokenDescriptor pToken)
    {
        if(pToken != null)
        {
            super.handleKeyword(pToken);
         
            Node pNode = getDOMNode();
         
            long lineNumber = pToken.getLine();
         
            if(pNode != null && lineNumber >= 0)
            {
                // The ANTLR line number are One based.  We need them to be Zero based.
                lineNumber--;

                XMLManip.setAttributeValue(pNode, 
                                                "line", 
                                                Long.toString(lineNumber));
            }
        }
    }
}
