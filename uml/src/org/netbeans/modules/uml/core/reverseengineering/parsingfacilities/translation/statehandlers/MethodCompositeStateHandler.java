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
