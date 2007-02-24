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
 * File       : PostUnaryExpression.java
 * Created on : Dec 11, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParser;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.ObjectInstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.ExpressionStateHandler;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.StateHandler;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREUnaryOperator;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IReferenceEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.REUnaryOperator;
import org.netbeans.modules.uml.core.reverseengineering.reframework.ReferenceEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

public class PostUnaryExpression extends ExpressionStateHandler
{
    private ITokenDescriptor  m_pOperator = null;
    
    public PostUnaryExpression()
    {
        super();
    }
    
    
    public void processToken(ITokenDescriptor  pToken, String language)
    {
        if(pToken != null)
        {
            String type =  pToken.getType();
            if("Operator".equals(type))
            {
                m_pOperator = pToken;
            }
            else
            {
                super.processToken(pToken, language);
            }
        }
    }
    
    public StateHandler createSubStateHandler(String stateName, String language)
    {
        StateHandler retVal = null;
        retVal = super.createSubStateHandler(stateName, language);
        return retVal;
    }
    
    public ETPairT<InstanceInformation,Node> writeAsXMI(InstanceInformation pInfo,
            Node    pParentNode,
            SymbolTable  symbolTable,
            IREClass       pThisPtr,
            IREClassLoader pClassLoader
            )
    {
        Node pVal = null;
        IExpressionProxy leftSide  = getExpression(0);
        InstanceInformation retVal   = null;
        InstanceInformation rightIns = null;
        
        ETPairT<InstanceInformation, Node> temp = null;
        
        temp = leftSide.writeAsXMI(pInfo, pParentNode, symbolTable, pThisPtr, pClassLoader);
        retVal = temp.getParamOne();
        try
        {
            Node pTopNode = generateXMI(pParentNode, retVal);
            if(pTopNode != null)
            {
                if(pVal != null)
                {
                    pVal = pTopNode;
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        if(retVal == null)
        {
            ObjectInstanceInformation pTemp = new ObjectInstanceInformation();
            pTemp.setInstanceOwner(pThisPtr);
            pTemp.setInstanceType(pThisPtr);
            retVal = pTemp;
        }
        return new ETPairT<InstanceInformation, Node>(retVal, null);
    }
    
    public InstanceInformation  sendOperationEvents(InstanceInformation   pInfo,
            IREClass              pThisPtr,
            SymbolTable           symbolTable,
            IREClassLoader        pClassLoader,
            IUMLParserEventDispatcher pDispatcher,
            Node              pParentNode)
    {
        IExpressionProxy leftSide  = getExpression(0);
        InstanceInformation retVal = null;
        
        if((pDispatcher != null) && (leftSide != null))
        {
            retVal  = leftSide.sendOperationEvents(pInfo, pThisPtr, symbolTable, pClassLoader, pDispatcher, pParentNode);
            try
            {
                Node pTopNode = generateXMI(pParentNode, retVal);
                if(pTopNode != null)
                {
                    IREUnaryOperator  pEvent = new REUnaryOperator();
                    if(pEvent != null)
                    {
                        pEvent.setEventData(pTopNode);
                        pDispatcher.fireUnaryOperator(pEvent, null);
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        
        if(retVal == null)
        {
            ObjectInstanceInformation pTemp = new ObjectInstanceInformation();
            pTemp.setInstanceOwner(pThisPtr);
            pTemp.setInstanceType(pThisPtr);
            retVal = pTemp;
        }
        return retVal;
    }
    
    public String toString()
    {
        String retVal = "";
        int max = getExpressionCount();
        for(int index = 0; index < max; index++)
        {
            IExpressionProxy proxy = getExpression(index);
            if(proxy != null)
            {
                retVal += proxy.toString();
            }
        }
        
        if(m_pOperator != null)
        {
            String value = m_pOperator.getValue();
            if(value.length() > 0)
            {
                retVal += value;
            }
        }
        return retVal;
    }
    
    
    public long getStartPosition()
    {
        return super.getStartPosition();
    }
    
    public long getEndPosition()
    {
        long retVal = -1;
        if(m_pOperator != null)
        {
            retVal = m_pOperator.getPosition() + m_pOperator.getValue().length();
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
        return retVal;
    }
    
    public ITokenDescriptor getOperatorToken()
    {
        return m_pOperator;
    }
    
    public void clear()
    {
        // No Respective code in C++
    }
    
    public Node generateXMI(Node pParentNode, InstanceInformation leftIns)
    {
        try
        {
            XMLManip manip= null;
            Node pTopNode = XMLManip.createElement((Element)pParentNode, "UML:BinaryOperatorAction");
//   			 	TODO aztec
////		 _VH( CreateNode(pParentNode, _T("UML:BinaryOperatorAction"), &pTopNode));
            
            if(pTopNode != null)
            {
                if(m_pOperator != null)
                {
                    String op = m_pOperator.getValue();
                    manip.setAttributeValue(pTopNode,"operator", op);
                    String value = this.toString();
                    manip.setAttributeValue(pTopNode, "representation", value);
                }
                leftIns.getInputPinInformation(pTopNode);
                leftIns.getOutputPinInformation(pTopNode);
                return pTopNode;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}//end of Class
