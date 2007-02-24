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
 * File       : MethodVariableStateHandler.java
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
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class MethodVariableStateHandler extends MethodParameterStateHandler
{
    private Expression      m_Expression = new Expression();
    private boolean         m_InInitializer = false;
    private boolean         m_TreatAsExpression = false;

    // This is only for VB.  I need to make a VB variable state handler.
    private boolean         m_IsCreateObject = false;


    /**
     * @param language
     * @param stateName
     */
    public MethodVariableStateHandler(String language, boolean treatAsExpression)
    {
        super(language, "Variable Definition");
        m_TreatAsExpression = treatAsExpression;
        m_InInitializer = false;
        m_IsCreateObject = false;
    }
    
    public StateHandler createSubStateHandler(String stateName, String language) 
    {
        StateHandler retVal = null;
        
        if("Initializer".equals(stateName))
        {
            retVal = this;
            m_InInitializer = true;
        }
        else if(m_InInitializer == true)
        {
            m_Expression.addState(stateName, language);
            retVal = this;
        }
        else if("Object Creation".equals(stateName))
        {
            // This is specific to Visual Basic.  
            // When we refactor I should make this a VB only action.
            retVal = this;
            m_IsCreateObject = true;
        }
        else 
        {
            retVal = super.createSubStateHandler(stateName, language);
        }        
        return retVal;
    }
    
    public void initialize() 
    {
        super.initialize();
    }
    
    public void processToken(ITokenDescriptor pToken, String language) 
    {
        if(m_InInitializer)
        {
            m_Expression.addToken(pToken, language);
        }
        else
        {
            super.processToken(pToken, language); 
        }
    }
    
    public void stateComplete(String stateName) 
    {
        if("Initializer".equals(stateName))
        {
            m_InInitializer = false;
        }
        else if(m_InInitializer)
        {
            m_Expression.endState(stateName);
        }
        else
        {
            super.stateComplete(stateName);   
        }
    }        
    
    public String toString() 
    {
        String retVal = super.toString();
        retVal += "=" + m_Expression.toString();
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

    public InstanceInformation addInstanceToSymbolTable() 
    {
        InstanceInformation retVal = null;
        if(!m_TreatAsExpression)
        {
           retVal = super.addInstanceToSymbolTable();
        }
        return retVal;
    }
    
    public InstanceInformation sendOperationEvents(
                                            InstanceInformation pInstance,
                                            IREClass pThisPtr,
                                            SymbolTable symbolTable,
                                            IREClassLoader pClassLoader,
                                            IUMLParserEventDispatcher pDispatcher,
                                            Node pParentNode)
    {
        setSymbolTable(symbolTable);
        InstanceInformation pInfo = super.addInstanceToSymbolTable();
        processNewInstance(pInfo, pThisPtr, pClassLoader, pDispatcher);
        return pInfo;
    }
    
    public ETPairT<InstanceInformation, Node> writeAsXMI(
                                                InstanceInformation pInfo,
                                                Node pParentNode,
                                                SymbolTable symbolTable,
                                                IREClass pThisPtr,
                                                IREClassLoader pClassLoader)
    {
        ETPairT<InstanceInformation, Node> retVal = 
            new ETPairT<InstanceInformation, Node>();
        setSymbolTable(symbolTable);
        InstanceInformation info = super.addInstanceToSymbolTable();
        if(pClassLoader != null)
        {  
            if(isPrimitive())
            {
                String typeName = info.getInstanceTypeName();
                info.setInstantiatedType(typeName, pClassLoader);
                retVal.setParamTwo(info.generateCreationXMI(pParentNode, 
                                                            getLineNumber(),null));
            }
         
            IREClass pThisClass = getClassBeingProcessed();

            Node pNode = getDOMNode();
            retVal = m_Expression.writeAsXMI(info, 
                                      pNode, 
                                      symbolTable,
                                      pThisPtr, 
                                      pClassLoader);

        }

        return retVal;
    }


    protected void processNewInstance(InstanceInformation pInfo) 
    {
        if(pInfo != null) 
        {
            IUMLParserEventDispatcher pDispatcher = getEventDispatcher();
      
            IREClassLoader pLoader = getClassLoader();

            if(pDispatcher != null && pLoader != null)
            {  
                IREClass pThisClass = getClassBeingProcessed();
                processNewInstance(pInfo, pThisClass, pLoader, pDispatcher);
            }
        }
    }

    protected void processNewInstance(InstanceInformation pInfo, 
                                    IREClass pThisPtr, 
                                    IREClassLoader pClassLoader, 
                                    IUMLParserEventDispatcher pDispatcher) 
    {
        if(pInfo != null)
        {
            if(pDispatcher != null && pClassLoader != null)
            {  
                String typeName = pInfo.getInstanceTypeName();
                if(isPrimitive())
                {
                    pInfo.setInstantiatedType(typeName, pClassLoader);
               
                    Node pNode = getDOMNode();
                    if(pNode != null)
                    {
                        pInfo.sendCreationEvent(pNode, getLineNumber(),pDispatcher, null);
                    }
                }
                else if(m_IsCreateObject == true)
                {
                   pInfo.setInstantiatedType(typeName, pClassLoader);
                   Node pNode = getDOMNode();
                   if(pNode != null)
                   {
                   	    pInfo.sendCreationEvent(pNode, getLineNumber(), pDispatcher,null);
                   }
                }
                	
            
                IREClass pThisClass = getClassBeingProcessed();

                Node pNode = getDOMNode();
            
            
                m_Expression.sendOperationEvents(pInfo, 
                                                  pThisPtr,
                                                  getSymbolTable(), 
                                                  pClassLoader,
                                                  pDispatcher,
                                                  pNode);
                // If the expression did not send a creation event then it is a
                // reference.  
                //
                // Example:
                // A a = SomeCall();
                // 
                // The variable a is referencing what ever was returned from the 
                // method SomeCall.  Therefore, send a reference not a creation.

                if(pInfo.isInstantiated() == false)
                {
                   pInfo.sendReference(pNode);
                }
            }
        }
    }

}
