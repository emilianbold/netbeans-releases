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
 * File       : PostUnaryExpression.java
 * Created on : Dec 11, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression;

import java.util.logging.Level;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.ObjectInstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.ExpressionStateHandler;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.StateHandler;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREUnaryOperator;
import org.netbeans.modules.uml.core.reverseengineering.reframework.REUnaryOperator;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.support.UMLLogger;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

public class PostUnaryExpression extends ExpressionStateHandler
{
    private ITokenDescriptor  m_pOperator = null;
    
    public PostUnaryExpression()
    {
        super();
    }
    
    
    @Override
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
    
    @Override
    public StateHandler createSubStateHandler(String stateName, String language)
    {
        StateHandler retVal = null;
        retVal = super.createSubStateHandler(stateName, language);
        return retVal;
    }
    
    @Override
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
    
    @Override
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
    
    @Override
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
    
    
    @Override
    public long getStartPosition()
    {
        return super.getStartPosition();
    }
    
    @Override
    public long getEndPosition()
    {
        long retVal = -1;
        if(m_pOperator != null)
        {
            retVal = m_pOperator.getPosition() + m_pOperator.getValue().length();
        }
        return retVal;
    }
    
    @Override
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
            Node pTopNode = XMLManip.createElement((Element)pParentNode, "UML:BinaryOperatorAction");
            
            if(pTopNode != null)
            {
                if(m_pOperator != null)
                {
                    String op = m_pOperator.getValue();
                    XMLManip.setAttributeValue(pTopNode,"operator", op);
                    String value = this.toString();
                    XMLManip.setAttributeValue(pTopNode, "representation", value);
                }
                if(leftIns==null)
                {
                    UMLLogger.logMessage("Null leftIns (Problem with post unary expression/operation)", Level.INFO);//log for debug purpose, as null may not be good here but still better then stop on npe
                    return null;
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
