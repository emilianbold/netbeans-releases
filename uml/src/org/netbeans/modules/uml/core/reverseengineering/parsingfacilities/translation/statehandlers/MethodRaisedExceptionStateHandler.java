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
