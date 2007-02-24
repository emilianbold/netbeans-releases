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
 * File       : MethodRaisedExceptionStateHandler.java
 * Created on : Dec 11, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.Expression;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IJumpEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.JumpEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class MethodRaisedExceptionStateHandler extends MethodDetailStateHandler
{
    private Expression m_ExceptionExpression = new Expression();
    private boolean m_InExceptionClass;
    
    public MethodRaisedExceptionStateHandler(String language)
    {
        super(language);
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
        ETPairT<InstanceInformation, Node> retVal = 
        m_ExceptionExpression.writeAsXMI(pInfo, pParentNode, symbolTable,
                                                pThisPtr, pClassLoader);
        retVal.setParamTwo(generateXML(pParentNode, retVal.getParamOne()));
        return retVal;
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
        InstanceInformation retVal = m_ExceptionExpression.sendOperationEvents(pInstance, pThisPtr, symbolTable,
                                                   pClassLoader, pDispatcher, pParentNode);
        Node pData = generateXML(pParentNode, retVal);
        
        if(pData != null)
        {
            IJumpEvent pEvent = new JumpEvent();
        
            if(pEvent != null)
            {
                pEvent.setEventData(pData);
                pDispatcher.fireJumpEvent(pEvent, null);
            }
        }
        return retVal;
    }
    
    public Node generateXML(Node pParentNode, InstanceInformation pInfo) 
    {
        if(pInfo == null) return null;
        
        Node pThrowsNode = createNode(pParentNode, "UML:JumpAction"); 
        
        if(pThrowsNode != null)
        {
            String value = "Throw";
            setNodeAttribute(pThrowsNode, "type", value);
            pInfo.getInputPinInformation(pThrowsNode);
        }
        return pThrowsNode;
    }
    
    public StateHandler createSubStateHandler(String stateName, String language) 
    {
        StateHandler retVal = null;
        
        if("Identifier".equals(stateName))
        {
          retVal = this;
        }
        else if("Exception".equals(stateName))
        {
            m_InExceptionClass = true;
            retVal = this;
        }

        if(m_InExceptionClass)
        {
            m_ExceptionExpression.addState(stateName, language);
            retVal = this;
        }        
        return retVal;
    }
    
    public void initialize() 
    {
        // No valid implementation in the C++ code base.
    }
    
    public void processToken(ITokenDescriptor pToken, String language) 
    {
        if(pToken == null) return;
        
        String type = pToken.getType();
        
        String value = pToken.getValue();

        if(m_InExceptionClass)
        {
            m_ExceptionExpression.addToken(pToken, language);
        }
    }
    
    public void stateComplete(String stateName) 
    {
        if("Exception".equals(stateName))
        {
            m_InExceptionClass = false;
        }
        else if("RaisedException".equals(stateName))
        {        
            reportData();
        }
        else if(m_InExceptionClass == true)
        {
           m_ExceptionExpression.endState(stateName);
        }
    }
}
