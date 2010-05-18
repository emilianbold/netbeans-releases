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
