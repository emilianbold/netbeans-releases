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
 * File       : MethodExpressionStateHandler.java
 * Created on : Dec 12, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.ObjectInstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.Expression;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class MethodExpressionStateHandler extends MethodDetailStateHandler
{
    private Expression m_Expression = new Expression();
    private String m_StateName;
    
    public MethodExpressionStateHandler(String stateName, String language)
    {
        super(language);
        m_StateName = stateName;
        createSubStateHandler(stateName, language);
    }
    
    public StateHandler createSubStateHandler(String stateName, String language)
    {
        if(m_Expression != null)
        m_Expression.addState(stateName, language);   
        return this;
    }
    
    public void initialize()
    {
        // No valid implementation in the C++ code base.
    }
    
    public void processToken(ITokenDescriptor pToken, String language)
    {
        if(m_Expression != null)
        m_Expression.addToken(pToken, language);
    }
    
    public void stateComplete(String stateName)
    {
        if(m_Expression != null)
        m_Expression.endState(stateName);
        if(stateName.equals(m_StateName))
        {
            reportData();
        }
    }
    
    public InstanceInformation sendOperationEvents(
                                        InstanceInformation pInstance,
                                        IREClass pThisPtr,
                                        SymbolTable symbolTable,
                                        IREClassLoader pClassLoader,
                                        IUMLParserEventDispatcher pDispatcher,
                                        Node pParentNode)
    {
        InstanceInformation retVal = null;
        
        if(m_Expression != null)
        retVal =  m_Expression.sendOperationEvents(pInstance, 
                                                   pThisPtr, 
                                                   symbolTable, 
                                                   pClassLoader, 
                                                   pDispatcher,
                                                   pParentNode);
        if(retVal == null)
        {
            ObjectInstanceInformation temp = new ObjectInstanceInformation();
            temp.setInstanceOwner(pThisPtr);
            temp.setInstanceType(pThisPtr);
            retVal = temp;
        }
        
        return retVal;
    }
    
    public ETPairT<InstanceInformation, Node> writeAsXMI(
                                                InstanceInformation pInfo,
                                                Node pParentNode,
                                                SymbolTable symbolTable,
                                                IREClass pThisPtr,
                                                IREClassLoader pClassLoader)
    {
        ETPairT<InstanceInformation, Node> retVal = null;
        
        retVal = m_Expression.writeAsXMI(pInfo,         
                                          pParentNode,
                                          symbolTable,
                                          pThisPtr, 
                                          pClassLoader);
                                          
        if(retVal.getParamOne() == null)
        {
            ObjectInstanceInformation temp = new ObjectInstanceInformation();
            temp.setInstanceOwner(pThisPtr);
            temp.setInstanceType(pThisPtr);
            retVal.setParamOne(temp);
        }
        
        return retVal;                                          
                                  
                                          

    }
    
    public long getStartPosition()
    {
        return m_Expression.getStartPosition();
    }

    public long getEndPosition()
    {
        return m_Expression.getEndPosition();
    }

    public long getStartLine()
    {
        return m_Expression.getStartLine();
    }
    
    public String toString()
    {
        return m_Expression.toString();
    }
}
