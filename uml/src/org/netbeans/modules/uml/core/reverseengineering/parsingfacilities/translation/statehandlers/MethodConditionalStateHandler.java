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
 * File       : MethodConditionalStateHandler.java
 * Created on : Dec 10, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IOpParserOptions;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClause;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREConditional;
import org.netbeans.modules.uml.core.reverseengineering.reframework.ITestEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.REClause;
import org.netbeans.modules.uml.core.reverseengineering.reframework.REConditional;
import org.netbeans.modules.uml.core.reverseengineering.reframework.TestEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class MethodConditionalStateHandler extends MethodCompositeStateHandler
{
    private ITokenDescriptor    m_pKeyword;    
    private Node                m_BodyNode;
    private Node                m_TestNode;
    private Node                m_GroupNode;
    private Node                m_ConditionalNode;
    
    private boolean             m_IsInTestCondition=false;
    private boolean             m_IsInBody=false;
    private boolean             m_IsInnerConditional=false;
    private boolean             m_IsDeterminate=true;
    private boolean             m_ForceClause;
    
    public MethodConditionalStateHandler(String language, boolean forceClause)
    {
        super(language);
        m_ForceClause = forceClause;
    }
    
    public void startCondition() 
    {
        // No valid implementation in the C++ code base.
    }

    public void beginTestCondition() 
    {
        IUMLParserEventDispatcher pDispatcher 
            = getEventDispatcher();
      
        if(pDispatcher != null)
        {
            pDispatcher.fireBeginTest(null);
        }
      
        Node pNode = getDOMNode();
        if(pNode == null)
        {
            pNode = getConditionalNode();
        }

        if(pNode != null)
        {
            m_TestNode = createNode(pNode, "UML:Clause.test"); 
        }
    }

    public void beginBody() 
    {
        m_BodyNode = createNode("UML:Clause.body");
    }

    public void endTestCondition() 
    {
        // Fire the end initialization event events to all listeners.
        String data = writeTestXMI(m_TestNode);
        
        IUMLParserEventDispatcher pDispatcher = getEventDispatcher();

        if(pDispatcher != null)
        {
            sendTestEvents();
            ITestEvent pEvent = new TestEvent();

            if(pEvent != null)
            {    
                Node pNode = getDOMNode();

                if(pNode != null)
                {
                    pEvent.setEventData(pNode);
                    pDispatcher.fireEndTest(pEvent, null);
                }
            }
        }
    }

    public void endBody() 
    {
        IUMLParserEventDispatcher pDispatcher = getEventDispatcher();
      
        Node pNode = getDOMNode();

        if(pNode != null)
        {
            if(m_IsDeterminate)
            {
                setNodeAttribute(pNode, "isDeterminate", true);
            }
      
            if(pDispatcher != null)
            {
                IREClause pEvent = new REClause();
         
                if(pEvent != null)
                {            
                    pEvent.setEventData(pNode);
                    pDispatcher.fireEndClause(pEvent, null);
                }
            }
        }
    }

    public void endCondtional(String nodeName) 
    {
        IUMLParserEventDispatcher pDispatcher = getEventDispatcher();

        //if(IsInnerConditional() == false)
        if(nodeName.equals(getConditionalNodeName()))
        { 
            // Since GetDOMNode returns UML:Clause I will have to follow
            // the node tree to locate the UML:ConditionalAction node.
            IREConditional pEvent = new REConditional();

            if(pEvent != null)
            {
                Node pNode = locateNode(nodeName);  
            
                if(pNode != null)
                {
                    pEvent.setEventData(pNode);   
                    pDispatcher.fireEndConditional(pEvent, null);
                }
            }
        }
    }
    
    public void initialize() 
    {
        setIsInnerConditional(true);

        IUMLParserEventDispatcher pDispatcher = getEventDispatcher();

        if(pDispatcher != null)
        {            
            pDispatcher.fireBeginConditional(null);

            if(m_ForceClause)
            {
                pDispatcher.fireBeginClause(null);
            }
        }

        Node pCondNode = createNode(getConditionalNodeName());

        if(pCondNode != null)
        {  
            setConditionalNode(pCondNode);

            Node pCondClause = createNode(pCondNode, getClauseGroupNodeName()); 
            setClauseGroupNode(pCondClause);

            if(pCondClause != null && m_ForceClause)
            {
                Node pClauseNode = createNode(pCondClause,"UML:Clause");                         
                setDOMNode(pClauseNode);
            }         
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
        else if(m_IsInTestCondition)
        {
            addTestConditionToken(pToken, language);
        }
    }

    public StateHandler createSubStateHandler(String stateName, String language)
    {
        MethodDetailStateHandler retVal = null;

        if("Test Condition".equals(stateName))
        {
            m_IsInTestCondition = true;
            m_IsDeterminate     = false;
            m_IsInBody          = false;

            beginTestCondition();
            retVal = this;
        }
        else if("Body".equals(stateName))
        {
            m_IsInTestCondition = false;
            m_IsInBody          = true;

            beginBody();
            retVal = this;
        }
        else if(m_IsInTestCondition)
        {
            addTestConditionState(stateName, language);
            retVal = this;
        }
        else if(m_IsInBody)
        {
            IOpParserOptions pOptions = getOpParserOptions();

            retVal = StatementFactory.retrieveStatementHandler(stateName, 
                                                               language, 
                                                               pOptions,
                                                               getSymbolTable()); 

            if("Else Conditional".equals(stateName))
            {
                // End the current scope before starting a new conditional.
                //EndScope();

                Node pCondClause = getClauseGroupNode();
                initializeHandler(retVal, pCondClause);         
           }
           else
           {
                initializeHandler(retVal, m_BodyNode);
           }
        }
        return retVal;
    }

    public void stateComplete(String stateName) 
    {
        if(m_IsInTestCondition)
        {
            endTestConditionState(stateName);
        }
   
        if("Test Condition".equals(stateName))
        {
            endTestCondition();
        }
        else if("Body".equals(stateName))
        {
            endBody();
        }
        else if("Conditional".equals(stateName))
        {
             endCondtional("UML:ConditionalAction");
        }
    }
    
    protected Node getClauseGroupNode() 
    {
        return m_GroupNode;
    }

    protected void setClauseGroupNode(Node newVal) 
    {
        m_GroupNode = newVal;
    }

    protected Node getConditionalNode() {
        return m_ConditionalNode;
    }

    protected void setConditionalNode(Node newVal) 
    {
        setDOMNode(newVal);
        m_ConditionalNode = newVal;
    }

    protected void setIsInnerConditional(boolean value) 
    {
        m_IsInnerConditional = value;
    }

    protected boolean isInnerConditional() 
    {
        return m_IsInnerConditional;
    }

    protected Node locateNode(String nodeName) 
    {
        return locateNode(getDOMNode(), nodeName);
    }

    protected Node locateNode(Node pNode, String wantedName)
    {
        if(pNode == null) return null;
        
        Node pVal = null;
        
        String nodeName = null;
        if(pNode instanceof Element)
           nodeName = ((Element)pNode).getQualifiedName();
        else
           nodeName = pNode.getName();
      
        if(nodeName.equals(wantedName))
        {          
            pVal = pNode;
        }
        else
        {
            Node pParent = pNode.getParent();
         
            if(pParent != null)
            {
                pVal = locateNode(pParent, wantedName);
            }
        }
        
        return pVal;
    }

    protected String getConditionalNodeName() 
    {
        return "UML:ConditionalAction";
    }

    protected String getClauseGroupNodeName() 
    {
        return "UML:ConditionalAction.clause";
    }
}
