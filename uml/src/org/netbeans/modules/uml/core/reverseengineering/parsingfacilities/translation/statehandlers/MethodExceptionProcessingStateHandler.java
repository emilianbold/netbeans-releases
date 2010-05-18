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
 * File       : MethodExceptionProcessingStateHandler.java
 * Created on : Dec 11, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IOpParserOptions;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.Identifier;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.ObjectInstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREExceptionJumpHandlerEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREExceptionProcessingEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.REExceptionJumpHandlerEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.REExceptionProcessingEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.support.umlsupport.Log;

/**
 * @author Aztec
 */
public class MethodExceptionProcessingStateHandler
    extends MethodDetailStateHandler
{
    private boolean m_InExceptionHandler;
    private boolean m_InDefaultHandler;
    private boolean m_InBody;
    private ITokenDescriptor m_pKeyword;

    private Identifier m_ExceptionName = new Identifier();
    private Node m_Signal;
    private boolean m_ExceptionParameter;
    private String m_ExceptionInstance = null;
    
    /**
     * @param language
     */
    public MethodExceptionProcessingStateHandler(String language)
    {
        super(language);
    }
    
    public StateHandler createSubStateHandler(String stateName, String language) 
    {
        MethodDetailStateHandler retVal = this;
    
        if("Exception Handler".equals(stateName))
        {
            if(m_InBody)
            {
               leaveScope();
            }
      
            m_InExceptionHandler = true;
            m_InDefaultHandler   = false;
            m_InBody             = false;

            beginJumpHandler(false);

            retVal = this;
        }   
        else if("Default Processing".equals(stateName))
        {
            if(m_InBody)
            {
                leaveScope();
            }

            m_InExceptionHandler = false;
            m_InDefaultHandler   = true;
            m_InBody             = false;

            beginJumpHandler(true);
            retVal = this;
        }   
        else if("Body".equals(stateName))
        {
            m_InExceptionHandler = false;
            m_InDefaultHandler   = false;
            m_InBody             = true;

            retVal = this;
        }
        else if("Parameter".equals(stateName))
        {
            beginExceptionParameter();
            m_ExceptionParameter = true;
        }
        else if(("Type".equals(stateName) || "Identifier".equals(stateName)) 
        && m_ExceptionParameter == true)
        {
            retVal = this;
        }
        else
        {
            IOpParserOptions pOptions = getOpParserOptions();

            retVal = StatementFactory.retrieveStatementHandler(stateName, 
                                                               language, 
                                                               pOptions, 
                                                               getSymbolTable());

            Node pNode = getDOMNode();
            if(pNode != null)
            {
               initializeHandler(retVal, pNode);
            }
        }   
        return retVal;
    }
    
    public void initialize() 
    {
        Node pCondNode = createNode("UML:GroupAction"); 
        setDOMNode(pCondNode);
        
        IUMLParserEventDispatcher pDispatcher = getEventDispatcher();
        
        if(pDispatcher != null)
        {            
           pDispatcher.fireBeginExceptionProcessing(null);
        }
        
        getSymbolTable().pushScope();
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
        else if("Name".equals(type))
        {
            handleName(pToken);
            if(m_ExceptionParameter == true)
            {
               m_ExceptionInstance = value;
            }
        }
        else if ("Identifier".equals(type) ||
               "Scoped Operator".equals(type))
        {
            if(m_ExceptionParameter)
            {
                m_ExceptionName.addToken(pToken);
                
            }
        }
    }
    
    public void stateComplete(String stateName) 
    {
        if("Exception Handler".equals(stateName))
        {  
            endJumpHandler();
            setDOMNode("UML:GroupAction");
        }
        else if("Default Processing".equals(stateName))
        {
            endJumpHandler();
            setDOMNode("UML:GroupAction");
        }
        else if("Exception Processing".equals(stateName))
        {
            finishExceptionProcessing();
        }
        else if("Parameter".equals(stateName))
        {
            m_ExceptionParameter = false;
            addParameterToSymbolTable();
        }
    }
    
    protected void beginJumpHandler(boolean isDefault) 
    {
        getSymbolTable().pushScope();
        
        Node pRoot = getDOMNode();
      
        if(pRoot != null)
        {         
            Node pJumpHandler = createNode(pRoot, "UML:JumpHandler");
         
            if(pJumpHandler != null)
            {
                setNodeAttribute(pJumpHandler, "isDefault", isDefault);
                setDOMNode(pJumpHandler);
            }
        }

        IUMLParserEventDispatcher pDispatcher = getEventDispatcher();

        if(pDispatcher != null)
        {            
            pDispatcher.fireBeginExceptionJumpHandler(null);
        }
    }

    protected void endJumpHandler() 
    {
        leaveScope();

        setTypeName();

        IUMLParserEventDispatcher pDispatcher = getEventDispatcher();

        if(pDispatcher != null)
        {            
            Node pNode = getDOMNode();
      
            if(pNode != null)
            {
                IREExceptionJumpHandlerEvent pEvent 
                            = new REExceptionJumpHandlerEvent();
                if(pEvent != null)
                {
                    pEvent.setEventData(pNode);
                    pDispatcher.fireEndExceptionJumpHandler(pEvent, null);
                }
            }
        }
    }

    protected void finishExceptionProcessing() 
    {
        Node pNode = getDOMNode();

        IUMLParserEventDispatcher pDispatcher = getEventDispatcher();

        if(pDispatcher != null)
        {
            if(pNode != null)
            {
                IREExceptionProcessingEvent pEvent 
                                            = new REExceptionProcessingEvent();
                if(pEvent != null)
                {
                    pEvent.setEventData(pNode);
                    pDispatcher.fireEndExceptionProcessing(pEvent, null);
                }
            }
        }
    }

    protected void setDOMNode(String wantedNodeName) 
    {
        Node pCurNode = getDOMNode();
      
        if(pCurNode != null)
        {
            Node pParent = getParentNode(pCurNode, wantedNodeName);

            if(pParent != null)
            {
                setDOMNode(pParent);
            }
        }
    }

    protected Node getParentNode(Node pCurNode, String wantedNodeName) 
    {
        Node pVal = null;
        if(pCurNode != null && pVal != null)
        {
            String nodeName = pCurNode.getName();

            if(wantedNodeName.equals(nodeName))
            {
                pVal = pCurNode;
            }
            else
            {
                Node pParent = pCurNode.getParent();

                if(pParent != null)
                {
                    pVal = getParentNode(pParent, wantedNodeName);
                }
            }
        }
        
        return pVal;
    }

    protected void leaveScope() 
    {
        Node pNode = getDOMNode();
        getSymbolTable().popScope(pNode);
    }
    
    protected void setTypeName() 
    {
        long line=0,col=0,pos=0,len=0;
        String value = null;
        if(m_Signal != null)
        {
            String nameString = null;
            if(m_ExceptionName != null)
            	nameString = m_ExceptionName.getIdentifierAsUML("DUMMY_FLAG");
            setNodeAttribute(m_Signal, "name", nameString) ;

            if(m_ExceptionName != null)
            {
                line = m_ExceptionName.getStartLine();
                col  = m_ExceptionName.getStartColumn();
                pos  = m_ExceptionName.getStartPosition();
                len  = m_ExceptionName.getLength();
                value = m_ExceptionName.getIdentifierAsSource(); 
            }
            

            createTokenDescriptor(m_Signal, "EndPosition", line, col, pos, value, len);
            setNodeAttribute("line", Long.toString(line)) ;   
        }

    }

    protected void handleName(ITokenDescriptor pToken) 
    {
        if(pToken == null) return;
        
        String nameString = pToken.getValue();
        setNodeAttribute(m_Signal, "instanceName", nameString) ;

        handleStartPosition(m_Signal, pToken);

    }

    protected void beginExceptionParameter() 
    {
        getSymbolTable().pushScope();
        
        Node pRoot = getDOMNode();
      
        if(pRoot != null)
        {  
            m_Signal = createNode(pRoot, "UML:Signal");
        }
    }

    public void addParameterToSymbolTable() {
        try {
            IREClassLoader  pLoader = getClassLoader();
            IREClass pClass = getClassBeingProcessed();
            
            String typeName = m_ExceptionName.getIdentifierAsUML("DUMMY_FLAG");
            IREClass  pExceptionClass = pLoader.loadClass(typeName, pClass);
            InstanceInformation ref = new ObjectInstanceInformation(m_ExceptionInstance,
                    typeName,
                    pExceptionClass);
            getSymbolTable().addInstance(ref, false);
            //m_ExceptionName.clear();
            //m_ExceptionInstance = "";
        } catch(Exception e) {
            Log.stackTrace(e);
        }
    }



}
