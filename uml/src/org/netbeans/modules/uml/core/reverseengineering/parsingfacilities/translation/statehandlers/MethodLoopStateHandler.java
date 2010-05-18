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
 * File       : MethodLoopStateHandler.java
 * Created on : Dec 11, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IOpParserOptions;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.Expression;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IInitializeEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IPostProcessingEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IRELoop;
import org.netbeans.modules.uml.core.reverseengineering.reframework.ITestEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.InitializeEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.PostProcessingEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.RELoop;
import org.netbeans.modules.uml.core.reverseengineering.reframework.TestEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * @author Aztec
 */
public class MethodLoopStateHandler extends MethodCompositeStateHandler
{
    private boolean m_IsInTestCondition;
    private boolean m_IsInInitialize;
    private boolean m_IsInPostProcessing;
    private boolean m_IsInBody;

    private Node m_TestNode;
    private Node m_InitializeNode;
    private Node m_PostProcessingNode;
    private Node m_BodyNode;

    private ITokenDescriptor m_pKeyword;
    private ITokenDescriptor m_pConditionalSeparator;
    private ITokenDescriptor m_pPostSeparator;
    
    private String m_StringRepresentation = "";
    
    private Expression m_BodyExpression;
    
    public MethodLoopStateHandler(String language)
    {
        super(language);
    }
    
    public StateHandler createSubStateHandler(String stateName, String language) 
    {
        MethodDetailStateHandler retVal = null;
    
        if("Test Condition".equals(stateName))
        {
            m_IsInTestCondition  = true;
            m_IsInBody           = false;
            m_IsInPostProcessing = false;
            m_IsInInitialize     = false;

            beginTestCondition();      
            retVal = this;
        }
        else if("Loop Initializer".equals(stateName))
        {
            m_IsInTestCondition  = false;
            m_IsInBody           = false;
            m_IsInPostProcessing = false;
            m_IsInInitialize     = true;


            beginInitialize();
            retVal = this;
        }
        else if("Loop PostProcess".equals(stateName))
        {
            m_IsInTestCondition  = false;
            m_IsInBody           = false;
            m_IsInPostProcessing = true;
            m_IsInInitialize     = false;

            beginPostProcessing();
            retVal = this;
        }
        else if("Body".equals(stateName))
        {
            m_IsInTestCondition = false;
            m_IsInPostProcessing = false;
            m_IsInInitialize = false;
            m_IsInBody       = true;

            beginBody();
            retVal = this;
        }
        else if(m_IsInTestCondition)
        {
            addTestConditionState(stateName, language);
            //m_TestConditionExpression.AddState(stateName, language);
            retVal = this;
        }
        else if(m_IsInInitialize)
        {
            //Kris - issue 78409 - the pOptions was a dead call. The addInitializerState
            //seemed to correctly add the new StateHandler to the stack, but then
            //this (MehodLoopStateHandler) was returned. So added the call to
            //retrieveStatementHandler to retrieve the handler created to handle the 
            //variable definition. Also, of course, removed the retVal=this line.
            IOpParserOptions pOptions = getOpParserOptions();
            
            addInitializerState(stateName, language);
            retVal = StatementFactory.retrieveStatementHandler(
                                                    stateName, 
                                                    language, 
                                                    pOptions, 
                                                    getSymbolTable());
            
        }
        else if(m_IsInPostProcessing)
        {      
            //m_PostProcessingExpression.AddState(stateName, language);
            addPostProcessingState(stateName, language);
            retVal = this;
        }
        else if(m_IsInBody)
        {
            IOpParserOptions pOptions = getOpParserOptions();

            retVal = StatementFactory.retrieveStatementHandler(
                                                    stateName, 
                                                    language, 
                                                    pOptions, 
                                                    getSymbolTable());
            initializeHandler(retVal, m_BodyNode);
        }
        return retVal;
    }
    
    public void initialize() 
    {
        Node pLoopNode = createNode("UML:LoopAction"); 
        setDOMNode(pLoopNode);
        
        IUMLParserEventDispatcher pDispatcher = getEventDispatcher();
        
        if(pDispatcher != null)
        {            
           pDispatcher.fireBeginLoop(null);
        }
        beginScope();
    }
    
    public void processToken(ITokenDescriptor pToken, String language) 
    {
        if(pToken == null) return;
        
        String type = pToken.getType();
        
        String value = pToken.getValue();

        if("Keyword".equals(type))
        {
            m_pKeyword = pToken;

            // I do not care about the HRESULT.
            handleKeyword(pToken);
        }
        else if("Conditional Separator".equals(type))
        {
            m_pConditionalSeparator = pToken;
        }
        else if("PostProcessor Separator".equals(type))
        {
            m_pPostSeparator = pToken;
        }
        else if(m_IsInTestCondition)
        {
            //m_TestConditionExpression.AddToken(pToken, language);
            addTestConditionToken(pToken, language);
        }
        else if(m_IsInInitialize)
        {
            //m_InitializerExpression.AddToken(pToken, language);
            addInitializerToken(pToken, language);
        }
        else if(m_IsInPostProcessing)
        {
            //m_PostProcessingExpression.AddToken(pToken, language);
            addPostProcessingToken(pToken, language);
        }
        else if(m_IsInBody)
        {
            if(m_BodyExpression != null)
            m_BodyExpression.addToken(pToken, language);
        }
    }
    
    public void stateComplete(String stateName) 
    {
        if(m_IsInTestCondition)
        {
            //m_TestConditionExpression.EndState(stateName);
            endTestConditionState(stateName);
        }
        else if(m_IsInInitialize)
        {
            //m_InitializerExpression.EndState(stateName);
            endInitializerState(stateName);
        }
        else if(m_IsInPostProcessing)
        {
            //m_PostProcessingExpression.EndState(stateName);
            endPostProcessingState(stateName);
        }

        if("Test Condition".equals(stateName))
        {
            endTestCondition();
        }
        else if("Loop Initializer".equals(stateName))
        {
            endInitialize();
        }
        else if("Loop PostProcess".equals(stateName))
        {
            endPostProcessing();
        }
        else if("Body".equals(stateName))
        {
            endBody();
        }
        else if("Loop".equals(stateName))
        {
            endCondtional();
        }
    }
    
    protected void beginTestCondition() 
    {
        IUMLParserEventDispatcher pDispatcher = getEventDispatcher();

        if(pDispatcher != null)
        {
            pDispatcher.fireBeginTest(null);
        }
        m_TestNode = createNode("UML:LoopAction.test");
    }

    protected void beginInitialize()
    {
        IUMLParserEventDispatcher pDispatcher = getEventDispatcher();

        if(pDispatcher != null)
        {
            pDispatcher.fireBeginInitialize(null);
        }

        m_InitializeNode = createNode("UML:LoopAction.initialize");   
    }

    protected void beginPostProcessing() 
    {
        IUMLParserEventDispatcher pDispatcher = getEventDispatcher();

        if(pDispatcher != null)
        {
            pDispatcher.fireBeginPostProcessing(null);
        }

        m_PostProcessingNode = createNode("UML:LoopAction.postprocess");   

    }

    protected void beginBody() 
    {
        m_BodyNode = createNode("UML:LoopAction.body");   
    }

    protected void endTestCondition() {
      String data = writeTestXMI(m_TestNode);

      // Fire the end initialization event events to all listeners.
      IUMLParserEventDispatcher dispatcher = getEventDispatcher();

      if (dispatcher != null)
      {
         sendTestEvents();
         ITestEvent event = new TestEvent();

         //             _VH(pEvent->put_StringRepresentation(dataBSTR) );
         Node node = getDOMNode();

         if (node != null)
         {
            event.setEventData(node);
            dispatcher.fireEndTest(event,null);
         }
      }

      if (m_pConditionalSeparator != null)
      {
         String value  = m_pConditionalSeparator.getValue();

         // TODO: In the future we will refactor the state handlers
         //       to be dynamically loaded.  At that time I need to
         //       make a VB and Java MethodLoopStateHandler to correctly
         //       format the string.  For now I will go by the length
         //       of the seperator.  Java and C++ seperator will be ';'
         //       so its length will be 1, while seperator for VB will be
         //       "to".
         if (value.length() > 1)
         {
            m_StringRepresentation += " ";
         }
         m_StringRepresentation += value;
         m_StringRepresentation += " ";
      }

      m_StringRepresentation += data;
    }

    protected void endInitialize() 
    {
        String data = writeInitializeXMI(m_InitializeNode);
        
        // Fire the end initialization event events to all listeners.
        IUMLParserEventDispatcher pDispatcher = getEventDispatcher();

        if(pDispatcher != null)
        {
            sendInitializationEvents();
            IInitializeEvent pEvent = new InitializeEvent();

            if(pEvent != null)
            {
                pEvent.setStringRepresentation(data);
                pDispatcher.fireEndInitialize(pEvent, null);
            }
        }
      
        m_StringRepresentation += data;

    }

    protected void endPostProcessing() 
    {
        String data = writeInitializeXMI(m_PostProcessingNode);

        // Fire the end initialization event events to all listeners.
        IUMLParserEventDispatcher pDispatcher = getEventDispatcher();

        if(pDispatcher != null)
        {           
            sendPostProcessingEvents();
            IPostProcessingEvent pEvent = new PostProcessingEvent();

            if(pEvent != null)
            {
                pEvent.setStringRepresentation(data);
                pDispatcher.fireEndPostProcessing(pEvent, null);
            }
        }

        if(m_pPostSeparator != null)
        {
            String value = m_pPostSeparator.getValue();

           // TODO: In the future we will refactor the state handlers
           //       to be dynamically loaded.  At that time I need to
           //       make a VB and Java MethodLoopStateHandler to correctly
           //       format the string.  For now I will go by the length
           //       of the seperator.  Java and C++ seperator will be ';'
           //       so its length will be 1, while seperator for VB will be
           //       "step".
            if(value != null && value.length() > 1)
            {
                m_StringRepresentation += " ";
            }

            m_StringRepresentation += value;
            m_StringRepresentation += " ";
        }
        m_StringRepresentation += data;
    }

    protected void endBody() 
    {
        // No valid implementation in the C++ code base.
    }

    protected void endCondtional() 
    {
        IUMLParserEventDispatcher pDispatcher = getEventDispatcher();
        endScope();
        
        IRELoop pEvent = new RELoop();

        if(pEvent != null)
        {
            Node pNode = getDOMNode();

            if(pNode != null)
            {
                XMLManip.setAttributeValue(pNode,
                                            "representation", 
                                            m_StringRepresentation);
                pEvent.setEventData(pNode);
                if(pDispatcher != null)
                    pDispatcher.fireEndLoop(pEvent, null);
            }         
        }
    }

}
