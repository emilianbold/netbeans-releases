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
 * File       : MethodReturnStateHandler.java
 * Created on : Dec 11, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.Expression;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IReturnEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.ReturnEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class MethodReturnStateHandler extends MethodDetailStateHandler
{
    private Expression m_ReturnExpression = new Expression();
    private ITokenDescriptor m_pKeyword;

    public MethodReturnStateHandler(String language)
    {
        super(language);
    }
    
    public StateHandler createSubStateHandler(String stateName, String language) 
    {
        
        StateHandler retVal = this;
        
        if ("Conditional Expression".equals(stateName)) {
          //kris richards - 
          // It is now assumed that the "Conditional Expression" state
          // will occur as a substate of the MethodVariableStateHandler. Therefore
          // the state is trap in the MethodVariableStateHandler.createSubStateHandler 
          // which in turn instantiates a MethodConditionalStateHandler instead of a 
          // ConditionalExpression. Essentially we are making the trinary ('?') operator
          // look like a basic if-else statement for SQD-REOperation.
            retVal = StatementFactory.retrieveStatementHandler("Conditional", language, getOpParserOptions(), getSymbolTable()) ;
        }
        else {        
            m_ReturnExpression.addState(stateName, language);
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

        if("Keyword".equals(type))
        {
            m_pKeyword = pToken;

            // I do not care about the HRESULT.
            handleKeyword(pToken);
        }
        else 
        {
            m_ReturnExpression.addToken(pToken, language);
        }
    }
    
    public void stateComplete(String stateName) 
    {
        if("Return".equals(stateName))
            endReturn();
        else
           m_ReturnExpression.endState(stateName);
    }
    
    protected void startReturn() 
    {
        // No valid implementation in the C++ code base.
    }

    protected void endReturn() 
    {
        IUMLParserEventDispatcher pDispatcher = getEventDispatcher();
        
        if(pDispatcher != null)
        {
            IReturnEvent pEvent= new ReturnEvent();
         
            if(pEvent != null)
            {                           
                InstanceInformation ref = reportExpressionData();                

                Node pReturnNode = createNode("UML:ReturnAction"); 

                if(pReturnNode != null)
                {
                    if(ref != null)
                    {
                        Node pInputPin = ref.getInputPinInformation(pReturnNode);
                    }

                    String data = m_ReturnExpression.toString();
                    setNodeAttribute(pReturnNode, "representation", data);   
               }            

                pEvent.setEventData(pReturnNode);
                pDispatcher.fireReturnAction(pEvent, null);
            }
        }        
    }

    protected InstanceInformation reportExpressionData() 
    {
        InstanceInformation retVal = null;

        Node pNode = getDOMNode();

        IREClass pThisClass = getClassBeingProcessed();

        IUMLParserEventDispatcher pDispatcher = getEventDispatcher();
   
        IREClassLoader pLoader = getClassLoader();

        if(pDispatcher != null && pLoader != null)
        {
            if(isWriteXMI())
            {
                //TODO: Aztec: Confirm
                Node data = 
                        m_ReturnExpression.writeAsXMI(getReferenceInstance(), 
                                            pNode,
                                            getSymbolTable(), 
                                            pThisClass, 
                                            pLoader).getParamTwo();
           }
           else
           {
                retVal = m_ReturnExpression.sendOperationEvents(getReferenceInstance(), 
                                                              pThisClass,
                                                              getSymbolTable(), 
                                                              pLoader, 
                                                              pDispatcher,
                                                              pNode);
            }
        }
        return retVal;
    }

}
