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
 * Created on Dec 11, 2003
 *
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.TemplateInstantiationStateHandler;
import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.Identifier;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.MethodDeclaration;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.ObjectInstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.ExpressionStateHandler;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.StateHandler;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlsupport.Log;

public class ObjectCreationExpression extends ExpressionStateHandler
{
    private ETList< ITokenDescriptor > m_Tokens = null;
    private ITokenDescriptor  m_pArgumentStart  = null;
    private ITokenDescriptor  m_pArgumentEnd    = null;
    private boolean           m_InIdentifierState;
    private Identifier        m_Identifier      = new Identifier();
    private ITokenDescriptor  m_pOperator       = null;
    private ITokenDescriptor  m_pPrimitive      = null;
    private boolean m_ExpressionList ;
    private TemplateInstantiationStateHandler mTemplateHandler = null;
    
    public ObjectCreationExpression()
    {
        super();
        m_pArgumentStart = null;
        m_pArgumentEnd =  null;
        m_InIdentifierState = false;
        m_ExpressionList = false;
    }
    
    public void processToken(ITokenDescriptor  pToken, String language)
    {
        if(pToken != null)
        {
            String type = pToken.getType();
            if("Operator".equals(type))
            {
                m_pOperator = pToken;
            }
            else if("Primitive Type".equals(type))
            {
                m_pPrimitive = pToken;
            }
            else if( ( "Identifier".equals(type) || "Scope Operator".equals(type) )
            && (m_ExpressionList == false) )
            {
                m_Identifier.addToken(pToken);
            }
            else if("Argument Start".equals(type))
            {
                m_pArgumentStart = pToken;
            }
            else if("Argument End".equals(type))
            {
                m_pArgumentEnd = pToken;
            }
            else
            {
                super.processToken(pToken, language);
            }
        }
    }
    
    public  StateHandler createSubStateHandler(String stateName, String language)
    {
        StateHandler retVal = null;
        
        if("Identifier".equals(stateName) && (!m_ExpressionList))
        {
            m_InIdentifierState = true;
	    retVal = this;
        }
        else if("Expression List".equals(stateName))
        {
            retVal = this;
            m_ExpressionList = true;
        }
        else if("Template Instantiation".equals(stateName))
        {
            mTemplateHandler = new TemplateInstantiationStateHandler(false);
            retVal = mTemplateHandler;
        }
        else
        {
            retVal = super.createSubStateHandler(stateName, language);
        }
        return retVal;
    }
    
    /**
     * Notification that the a state has completed.
     *
     * @param stateName [in] The name of the state.
     */
    public void stateComplete(String stateName)
    {
        if("Expression List".equals(stateName))
        {
            m_ExpressionList = false;
        }
        else
        {
            super.stateComplete(stateName);
        }
    }
    
    
    public ETPairT<InstanceInformation, Node> writeAsXMI(InstanceInformation  pInfo,
            Node    pParentNode,
            SymbolTable    symbolTable,
            IREClass       pThisPtr,
            IREClassLoader pClassLoader
            )
    {
        return new ETPairT<InstanceInformation, Node>(pInfo, null);
    }
    
    public InstanceInformation sendOperationEvents(InstanceInformation       pInfo,
            IREClass                  pThisPtr,
            SymbolTable               symbolTable,
            IREClassLoader            pClassLoader,
            IUMLParserEventDispatcher pDispatcher,
            Node                      pParentNode)
    {
        InstanceInformation retVal = pInfo;
        try
        {
            int lineNumber = 0;
            if(m_pOperator != null)
            {
                lineNumber = m_pOperator.getLine();
            }
            if(retVal != null)
            {
                symbolTable.addInstance(retVal,false);
            }
            else
            {
                ObjectInstanceInformation pTemp = new ObjectInstanceInformation();
                pTemp.setInstanceOwner(pThisPtr);
                IREClass  pType = pTemp.getInstantiatedType();
                if(pType != null)
                {
                    pTemp.setInstanceType(pType);
                }
                // I want all users of the object creation know that they are using
                // anonymous object.  Therefore, the instance name on the instance information
                // must be "<RESULT>".
                pTemp.setInstanceName("<RESULT>");
                
                retVal = pTemp;
            }
            if((retVal != null) && (pDispatcher != null))
            {
                String typeName = m_Identifier.getIdentifierAsUML();
                if(mTemplateHandler != null)
                {
                    typeName = mTemplateHandler.getTypeNameAsUML();
                }
                
                retVal.setInstantiatedType(typeName,
                        pClassLoader);
                
                ETList< ETPairT<InstanceInformation, String>> args =
                        getArgumentInstances(retVal,
                        pThisPtr,
                        symbolTable,
                        pClassLoader,
                        pDispatcher,
                        pParentNode);
                
                MethodDeclaration declaration = getMethodDeclaration(retVal,
                        pThisPtr,
                        pClassLoader,
                        args);
                
                retVal.sendCreationEvent( pParentNode, lineNumber, pDispatcher, declaration);
                if(declaration != null)
                {
                    declaration = null;
                }
            }
        }
        catch(Exception e)
        {
//            e.printStackTrace() ;
            Log.stackTrace(e);
        }
        return retVal;
    }
    
    
    /**
     * Retrieves all the method call argurments.
     *
     * @param pInfo [in] The instance information context.
     * @param pThisClass [in]
     * @param symbolTable [in] The table to look up variables.
     * @param pClassLoader [in] The loader to use when locating class definitions.
     * @param pDispatcher [in] The dispatcher to use when sending events.
     */
    
    
    public  ETList< ETPairT<InstanceInformation, String>>
            getArgumentInstances(InstanceInformation  pInfo,
            IREClass            pThisPtr,
            SymbolTable               symbolTable,
            IREClassLoader            pClassLoader,
            IUMLParserEventDispatcher pDispatcher,
            Node               pParentNode)
    {
        ETList< ETPairT<InstanceInformation, String> > retVal =
                new ETArrayList< ETPairT<InstanceInformation, String> >();
        int max = getExpressionCount();
        for(int index = 0; index < max; index++)
        {
            IExpressionProxy proxy = getExpression(index);
            if(proxy != null)
            {
                InstanceInformation curRef = null;
                if(pDispatcher != null)
                {
                    curRef = proxy.sendOperationEvents(pInfo,
                            pThisPtr,
                            symbolTable,
                            pClassLoader,
                            pDispatcher,
                            pParentNode);
                }
                else
                {
                    ETPairT<InstanceInformation, Node> pair =  proxy.writeAsXMI(pInfo,
                            pParentNode,
                            symbolTable,
                            pThisPtr,
                            pClassLoader );
                    curRef = pair.getParamOne();
                    
                }
                if(curRef != null)
                {
                    ETPairT<InstanceInformation, String>  valuePair  =
                            new ETPairT<InstanceInformation, String>();
                    valuePair.setParamOne(curRef);
                    valuePair.setParamTwo(proxy.toString());
                    retVal.add(valuePair);
                }
            }
        }
        return retVal;
    }
    
    /**
     * Retrieve the method declaration that describes the method the invoked
     * method.
     *
     * @param pInfo[in] The reciever of the method call.
     * @param pThisPtr [in] The sender of the method call.
     * @param pClassLoader [in] Used to search for type information.
     * @param arguments [in] The arguments passed to the call.
     */
    public MethodDeclaration getMethodDeclaration(InstanceInformation   pInfo,
            IREClass       pThisPtr,
            IREClassLoader pClassLoader,
            ETList< ETPairT<InstanceInformation, String>>   arguments)
    {
        MethodDeclaration retVal = null;
        if(m_Identifier.getLength() > 0)
        {
            String methodName = m_Identifier.getIdentifierAsSource();
            if(methodName.length() > 0)
            {
                if(pInfo == null)
                {
                    ObjectInstanceInformation pTemp = new ObjectInstanceInformation();
                    pTemp.setInstanceOwner(pThisPtr);
                    pTemp.setInstanceType(pThisPtr);
                    retVal = pTemp.getMethodDeclaration(methodName, arguments, pClassLoader,false);
                }
                else
                {
                    retVal = pInfo.getMethodDeclaration(methodName, arguments, pClassLoader,false);
                }
            }
        }
        return retVal;
    }
    
    public String toString()
    {
        String retVal = "";
        if(m_pOperator != null)
        {
            String opName =  m_pOperator.getValue();
            retVal += opName;
            retVal += " ";
        }
        if(m_Identifier.getLength() > 0)
        {
            retVal += m_Identifier.getIdentifierAsSource();
            if(m_pArgumentStart != null)
            {
                String value =  m_pArgumentStart.getValue();
                retVal += value;
            }
            retVal += super.toString();
            if(m_pArgumentEnd != null)
            {
                String value = m_pArgumentEnd.getValue();
                retVal += value;
            }
        }
        else if(m_pPrimitive != null)
        {
            String typeName =  m_pPrimitive.getValue();
            retVal += typeName;
            retVal += super.toString();
        }
        else if(mTemplateHandler != null)
        {
            String typeName = mTemplateHandler.toString();
            retVal += typeName;
            retVal += super.toString();
            
            if(m_pArgumentStart != null)
            {
                String value =  m_pArgumentStart.getValue();
                retVal += value;
            }
            retVal += super.toString();
            if(m_pArgumentEnd != null)
            {
                String value = m_pArgumentEnd.getValue();
                retVal += value;
            }
        }
        return retVal;
    }
    
    public long getStartPosition()
    {
        long retVal = -1;
        if(m_pOperator != null)
        {
            retVal = m_pOperator.getPosition();
        }
        else
        {
            retVal = super.getStartPosition();
        }
        return retVal;
    }
    
    public long getEndPosition()
    {
        long retVal = -1;
        if(m_pArgumentEnd != null)
        {
            retVal = m_pArgumentEnd.getPosition() + m_pArgumentEnd.getLength();
        }
        else
        {
            retVal = super.getEndPosition();
        }
        
        return retVal;
    }
    
    public long getStartLine()
    {
        long retVal = -1;
        if(m_pOperator != null)
        {
            retVal = m_pOperator.getLine();
        }
        else
        {
            retVal = super.getStartLine();
        }
        return (int)retVal;
    }
    
    public void clear()
    {
//	   m_Tokens.clear();
    }
    
    
}

