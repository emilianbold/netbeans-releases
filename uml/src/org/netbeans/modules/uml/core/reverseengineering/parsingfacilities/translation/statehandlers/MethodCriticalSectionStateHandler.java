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
 * File       : MethodCriticalSectionStateHandler.java
 * Created on : Dec 10, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IOpParserOptions;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IRECriticalSection;
import org.netbeans.modules.uml.core.reverseengineering.reframework.ITestEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.RECriticalSection;
import org.netbeans.modules.uml.core.reverseengineering.reframework.TestEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class MethodCriticalSectionStateHandler
    extends MethodCompositeStateHandler
{
    private ITokenDescriptor m_pKeyword;
    private boolean m_LockObject;
    private boolean m_InBody;

    private Node m_LockObjectNode;
    
    public MethodCriticalSectionStateHandler(String language)
    {
        super(language);
    }
    
    public void initialize() 
    {
        IUMLParserEventDispatcher pDispatcher = getEventDispatcher();

        if(pDispatcher != null)
        {            
            pDispatcher.fireBeginCriticalSection(null);
        }

        Node pNode = createNode("UML:CriticalSectionAction");

        if(pNode != null)
        {
            setDOMNode(pNode);
        }
    }

    public void processToken(ITokenDescriptor pToken, String language) 
    {
        if(pToken == null) return;
        
        String type = pToken.getType();
        
        String value = pToken.getValue();

        if("Keyword".equals(type))
        {
            m_pKeyword = pToken;

            handleKeyword(pToken);
        }
        else if(m_LockObject)
        {
            addTestConditionToken(pToken, language);
        }
    }

    public StateHandler createSubStateHandler(String stateName, String language) 
    {
        MethodDetailStateHandler retVal = this;
        
        if("Lock Object".equals(stateName))
        {
            m_LockObject = true;
            m_InBody     = false;
      
            beginLockObject();
        }   
        else if("Body".equals(stateName))
        {
            m_LockObject = false;
            m_InBody     = true;
        }
        else if(m_LockObject == true)
        {
            addTestConditionState(stateName, language);
        }
        else if(m_InBody)
        {
            IOpParserOptions pOptions = getOpParserOptions();

            retVal = StatementFactory.retrieveStatementHandler(stateName, 
                                                               language, 
                                                               pOptions, 
                                                               getSymbolTable());
            Node pBodyNode = getDOMNode();
            initializeHandler(retVal, pBodyNode);                                                               
        }   
   
        return retVal;
    }

    public void stateComplete(String stateName) 
    {
        if(m_LockObject)
        {
            endTestConditionState(stateName);
        }

        if("CriticalSection".equals(stateName))
        {
            endSynchronized();
        }
        else if("Lock Object".equals(stateName))
        {
            m_LockObject = false;
            m_InBody     = false;
      
            endTestCondition();
        }
        else if("Body".equals(stateName))
        {
            m_LockObject = false;
            m_InBody     = false;
        }
    }
    
    /**
     * Builds the XMI that will represent the expression.  The 
     * XML DOM Nodes that represent the expression will be added
     * as children to the specified DOM Node.
     *
     * @param pInfo [in] The instance that is the input pin.
     * @param pParentNode [in] The node that will contain the XMI node.
     * @param pVal [out] The data.
     */
    public ETPairT<InstanceInformation, Node> writeAsXMI(
                                                InstanceInformation pInfo,
                                                Node pParentNode,
                                                SymbolTable symbolTable,
                                                IREClass pThisPtr,
                                                IREClassLoader pClassLoader)
    {
        return new ETPairT<InstanceInformation, Node>(pInfo, getDOMNode());
    }

    /**
     * Sends out the UMLParser structure details events that represent the 
     * method call data.  
     *
     * @param pInfo [in] The instance information context.
     * @param symbolTable [in] The symbol table to use for lookups.
     * @param pClassLoader [in] The classloader to use when searching for 
     *                          class definitions.
     * @param pDispatcher [in] The event dispatcher used to send the events.
     * 
     * @return The instance context.
     */
    public InstanceInformation sendOperationEvents(
                                            InstanceInformation pInstance,
                                            IREClass pThisPtr,
                                            SymbolTable symbolTable,
                                            IREClassLoader pClassLoader,
                                            IUMLParserEventDispatcher pDispatcher,
                                            Node pParentNode)
    {
        // No valid implementation in the C++ code base.p
        return null;
    }

    
    /**
     * Sends the OnBeginTest event to the OperationDetailListner(s)
     * that are registred with the UMLParser event dispatcher.  The 
     * options will determine if the test section details will be sent.
     */    
    protected void beginLockObject() 
    {
        IUMLParserEventDispatcher pDispatcher = getEventDispatcher();

        if(pDispatcher != null)
        {            
            pDispatcher.fireBeginTest(null);
        }

        m_LockObjectNode = createNode("UML:Clause.test");

    }

    protected void endSynchronized() 
    {
        IUMLParserEventDispatcher pDispatcher = getEventDispatcher();

        if(pDispatcher != null)
        {            
            IRECriticalSection pEvent = new RECriticalSection();
         
            if(pEvent != null)
            {
                Node pTopNode = getDOMNode();
            
                pEvent.setEventData(pTopNode);
                pDispatcher.fireCriticalSection(pEvent,null);
                pDispatcher.fireEndCriticalSection(pEvent, null);
            }
        }
    }

    protected void endTestCondition() 
    {
        String data = writeTestXMI(m_LockObjectNode);

        // Fire the end initialization event events to all listeners.
        IUMLParserEventDispatcher pDispatcher = getEventDispatcher();

        if(pDispatcher != null)
        {            
            Node pNode = getDOMNode();

            if(pNode != null)
            {
                InstanceInformation inputInstance = sendTestEvents();
                if(inputInstance != null)
                {
                    Node inpNode = inputInstance.getInputPinInformation(pNode);
                }

                ITestEvent pEvent = new TestEvent();

                if(pEvent != null)
                {
                    pEvent.setEventData(pNode);
                    pDispatcher.fireEndTest(pEvent, null);
                }
            }
        }
    }

    protected void beginBody() 
    {
        // No valid implementation in the C++ code base.
    }

    protected void endBody() 
    {
        // No valid implementation in the C++ code base.
    }

}
