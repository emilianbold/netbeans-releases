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
 * File       : BinaryExpression.java
 * Created on : Dec 8, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression;

import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.ObjectInstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.ExpressionStateHandler;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.StateHandler;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREBinaryOperator;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.REBinaryOperator;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

public class BinaryExpression  extends ExpressionStateHandler
{
    private ITokenDescriptor   m_pOperator = null;
    private ITokenDescriptor   m_precedenceStart = null;
    private ITokenDescriptor   m_precedenceEnd = null;
    private ITokenDescriptor   m_rightSidePrecedence = null;
    private ITokenDescriptor   m_rightSidePrecedenceEnd = null;
    private boolean            m_leftSideFound = false;
    private boolean            m_precedenceEndAfterLeft = false;
    
    public void clear()
    {
        m_pOperator = null;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IBinaryExpression#createSubStateHandler(java.lang.String, java.lang.String)
         */
    public StateHandler createSubStateHandler(String stateName,String language)
    {
        StateHandler retVal = null;
        retVal = super.createSubStateHandler(stateName, language);
        
        if(retVal == null)
        {
            retVal = this;
        }
        else
        {
            m_leftSideFound = true;
            if(m_rightSidePrecedence != null)
            {
                retVal.processToken(m_rightSidePrecedence,language);
                m_rightSidePrecedence = null;
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IBinaryExpression#getLeftHandSideString()
         */
    public String getLeftHandSideString()
    {
        String retVal = null;
        IExpressionProxy leftSide = getExpression(0);
        if(leftSide != null)
        {
            retVal = leftSide.toString();
        }
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IBinaryExpression#getOperatorAsString()
         */
    public String getOperatorAsString()
    {
        String retVal = "";
        if(m_pOperator != null)
        {
            String value =  m_pOperator.getValue();
            retVal += " "+value+" ";
        }
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IBinaryExpression#getRightHandSideString()
         */
    public String getRightHandSideString()
    {
        StringBuffer retVal = new StringBuffer("");
        IExpressionProxy leftSide = getExpression(1);
        if(leftSide != null)
        {
            retVal.append(leftSide.toString());
        }
        
        if(getExpressionCount()>2)
        {
            for(int index = 2; index < getExpressionCount(); index++)
            {
                IExpressionProxy proxy = getExpression(index);
                if(proxy != null)
                {
                    retVal.append(proxy.toString());
                }
            }
        }
        return retVal.toString();
    }
    
    protected ITokenDescriptor getLeftHandPrecedenceTokenStart()
    {
        return m_precedenceStart;
    }
    
    protected ITokenDescriptor getLeftHandPrecedenceTokenEnd()
    {
        return m_precedenceEnd;
    }
    
    protected ITokenDescriptor getRightHandPrecedenceTokenStart()
    {
        return m_rightSidePrecedence;
    }
    
    protected ITokenDescriptor getRightHandPrecedenceTokenEnd()
    {
        return m_rightSidePrecedenceEnd;
    }    
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IBinaryExpression#initialize()
     */
    public void initialize()
    {
//		Not Any Code In C++
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IBinaryExpression#processToken(org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor, java.lang.String)
         */
    public void processToken(ITokenDescriptor pToken, String language)
    {
        String value = pToken.getType();
        String text =  pToken.getValue();
        if("Operator".equals(value))
        {
            if(m_pOperator == null)
            {
                m_pOperator = pToken;
                String value1 = pToken.getValue();
            }
        }
        else if("Precedence Start".equals(value))
        {
            // Save this token until the next expression.  The precedence start will
            // always appear before the expression.
            if(m_leftSideFound == true)
            {
                m_rightSidePrecedence = pToken;
            }
            else
            {
                m_precedenceStart = pToken;
            }
        }
        else if("Precedence End".equals(value))
        {
            if(m_precedenceStart == null)
            {
                // The Precedence End token will alway appear after the expression.
                if(getExpressionCount() > 0)
                {
                    IExpressionProxy proxy = getExpression(getExpressionCount() - 1);
                    if(proxy != null)
                    {
                        proxy.processToken(pToken, language);
                    }
                }
            }
            else if((m_rightSidePrecedence != null) && (m_rightSidePrecedenceEnd == null))
            {
                m_rightSidePrecedenceEnd = pToken;
            }
            else if((m_precedenceStart != null) && (m_precedenceEnd != null))
            {
                // The Precedence End token will alway appear after the expression.
                if(getExpressionCount() > 0)
                {
                    IExpressionProxy proxy = getExpression(getExpressionCount() - 1);
                    if(proxy != null)
                    {
                        proxy.processToken(pToken, language);
                    }
                }
            }
            else
            {
                m_precedenceEndAfterLeft = m_leftSideFound;
                m_precedenceEnd = pToken;
            }
        }
        else
        {
            super.processToken(pToken, language);
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IBinaryExpression#sendOperationEvents(org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.InstanceRef, org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.SymbolTable, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher, org.dom4j.Node)
         */
    public InstanceInformation sendOperationEvents(InstanceInformation pInfo, IREClass pThisPtr, SymbolTable symbolTable, IREClassLoader pClassLoader, IUMLParserEventDispatcher pDispatcher,Node pParentNode)
    {
        IExpressionProxy leftSide  = getExpression(0);
        IExpressionProxy rightSide = getExpression(1);
        
        InstanceInformation retVal   = null;
        InstanceInformation rightIns = null;
        
        if((pDispatcher != null) && (leftSide != null) && (rightSide != null))
        {
            retVal   = leftSide.sendOperationEvents(pInfo, pThisPtr, symbolTable, pClassLoader, pDispatcher, pParentNode);
            rightIns = rightSide.sendOperationEvents(pInfo, pThisPtr, symbolTable, pClassLoader, pDispatcher, pParentNode);
            
            try
            {
                Node pTopNode =  generateXMI(pParentNode, retVal, rightIns);
                if(pTopNode != null)
                {
                    IREBinaryOperator  pEvent = new REBinaryOperator();
                    if(pEvent != null)
                    {
                        pEvent.setEventData(pTopNode);
                        pDispatcher.fireBinaryOperator(pEvent,null);
                    }
                }
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
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
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IBinaryExpression#WriteAsXMI(org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.InstanceRef, org.dom4j.Node, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.SymbolTable, org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader)
         */
    public ETPairT<InstanceInformation, Node> writeAsXMI(InstanceInformation pInfo, Node pParentNode, SymbolTable symbolTable, IREClass pThisPtr, IREClassLoader pClassLoader)
    {
        IExpressionProxy leftSide  = getExpression(0);
        IExpressionProxy rightSide = getExpression(1);
        
        InstanceInformation retVal   = null;
        InstanceInformation rightIns = null;
        
        ETPairT<InstanceInformation, Node> leftTemp = null;
        ETPairT<InstanceInformation, Node> rightTemp = null;
        
        String str = leftSide.toString();
        leftTemp   = leftSide.writeAsXMI(pInfo, pParentNode, symbolTable, pThisPtr, pClassLoader);
        rightTemp = rightSide.writeAsXMI(pInfo, pParentNode, symbolTable, pThisPtr, pClassLoader);
        
        retVal = leftTemp.getParamOne();
        rightIns = rightTemp.getParamOne();
        
        Node pTopNode = generateXMI(pParentNode, retVal, rightIns);
        if(retVal == null)
        {
            ObjectInstanceInformation pTemp = new ObjectInstanceInformation();
            pTemp.setInstanceOwner(pThisPtr);
            pTemp.setInstanceType(pThisPtr);
            retVal = pTemp;
        }
        return new ETPairT<InstanceInformation, Node>(retVal, null);
    }
    
    public String toString()
    {
        StringBuffer retVal = new StringBuffer("");
        
        if(m_precedenceStart != null)
        {
            retVal.append(m_precedenceStart.getValue());
        }
        
        retVal.append(getLeftHandSideString());
        
        if( (m_precedenceEnd != null) && (m_precedenceEndAfterLeft == true) )
        {
            retVal.append(m_precedenceEnd.getValue());
        }
        
        retVal.append(getOperatorAsString());
        retVal.append(getRightHandSideString());
        
        if( (m_precedenceEnd != null) && (m_precedenceEndAfterLeft == false) )
        {
            retVal.append(m_precedenceEnd.getValue());
        }
        
        return retVal.toString();
    }
    
    public Node generateXMI(Node pParentNode, InstanceInformation  leftIns, InstanceInformation rightIns)
    {
        if(leftIns == null || rightIns == null)
            return null;
        
        Node pTopNode = null;
        try
        {
            pTopNode = createNode(pParentNode, "UML:BinaryOperatorAction");
            
            if(pTopNode != null)
            {
                if(m_pOperator != null)
                {
                    String xOp = getOperatorAsString();
                    XMLManip.setAttributeValue(pTopNode, "operator", xOp);
                    String value = this.toString();
                    XMLManip.setAttributeValue(pTopNode, "representation", value);
                }
                leftIns.getInputPinInformation(pTopNode);
                rightIns.getInputPinInformation(pTopNode);
                leftIns.getOutputPinInformation(pTopNode);
                return pTopNode;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return pTopNode;
    }
}
