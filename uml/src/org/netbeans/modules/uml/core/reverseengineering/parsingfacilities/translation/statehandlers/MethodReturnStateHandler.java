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
        
        m_ReturnExpression.addState(stateName, language);
   
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
