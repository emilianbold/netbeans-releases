/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * File       : ExpressionStateHandler.java
 * Created on : Dec 10, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;

import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.ObjectInstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.ExpressionFactory;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IExpressionProxy;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.SubExpressionProxy;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.TokenExpressionProxy;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


/**
 * @author Aztec
 */
public class ExpressionStateHandler extends StateHandler
{
    private ETList<IExpressionProxy> m_Expressions
                    = new ETArrayList<IExpressionProxy>();

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.IExpressionStateHandler#addSubExpression(org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.IExpressionStateHandler)
     */
    public void addSubExpression(ExpressionStateHandler exp)
    {
        SubExpressionProxy proxy = new SubExpressionProxy(exp);

        m_Expressions.add(proxy);

    }

    /**
     * Create a new state handler to be added to the state mechanism.  If the
     * state is not a state that is being processed then a new state handler is
     * not created.
     *
     * @param stateName [in] The state name.
     * @param language [in] The langauge being processed.
     *
     * @return
     */
    public StateHandler createSubStateHandler(String stateName, String language)
    {
       ExpressionStateHandler retVal = ExpressionFactory
                                        .getExpressionForState(stateName, language);
       addSubExpression(retVal);

       return retVal;

    }

    /**
     * Retrieve the end position of the expression.  The end position
     * is the file position after the last character of the expression.
     *
     * @return The file position where the expression ends.
     */
    public long getEndPosition()
    {
        long retVal = -1;
        for(int index = 0; index < getExpressionCount(); index++)
        {
           IExpressionProxy proxy = getExpression(index);

           if(proxy != null)
           {
              if(retVal > -1)
              {
                 retVal = (retVal > proxy.getStartPosition())
                            ? retVal : proxy.getStartPosition();
              }
              else
              {
                 retVal = proxy.getStartPosition();
              }
           }
        }
        return retVal;
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.IExpressionStateHandler#getStartLine()
     */
    public long getStartLine()
    {
        long retVal = -1;
        for(int index = 0; index < getExpressionCount(); index++)
        {
           IExpressionProxy proxy = getExpression(index);

           if(proxy != null)
           {
              if(retVal > -1)
              {
                 retVal = (retVal < proxy.getStartLine())
                            ? retVal : proxy.getStartLine();
              }
              else
              {
                 retVal = proxy.getStartLine();
              }
           }
        }
        return retVal;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.IExpressionStateHandler#getStartPosition()
     */
    public long getStartPosition()
    {
        long retVal = -1;

        IExpressionProxy proxy = getExpression(0);

        if(proxy != null)
        {
           retVal = proxy.getStartPosition();
        }

        return retVal;
    }

    public void initialize()
    {
        // No valid implementation in the C++ code base.
    }

    /**
     * Process a new token.  The tokens that are processed are in the
     * context of an object creation.
     *
     * @param pToken [in] The token to be processed.
     */
    public void processToken(ITokenDescriptor pToken, String language)
    {
       addToken(pToken, language);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.IExpressionStateHandler#sendOperationEvents(org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation, org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher, org.dom4j.Node)
     */
    public InstanceInformation sendOperationEvents(
        InstanceInformation pInstance,
        IREClass pThisPtr,
        SymbolTable symbolTable,
        IREClassLoader pClassLoader,
        IUMLParserEventDispatcher pDispatcher,
        Node pParentNode)
    {
        InstanceInformation retVal = pInstance;

        int max = getExpressionCount();
        IExpressionProxy proxy = null;
        for(int index = 0; index < max; index++)
        {
            proxy = getExpression(index);
            if(proxy != null)
            {
                retVal = proxy.sendOperationEvents(retVal,
                                                     pThisPtr,
                                                     symbolTable,
                                                     pClassLoader,
                                                     pDispatcher,
                                                     pParentNode);
            }
        }
        return retVal;
    }

    /**
     * Notification that the a state has completed.
     *
     * @param stateName [in] The name of the state.
     */
    public void stateComplete(String val)
    {
        // No valid implementation in the C++ code base.
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.IExpressionStateHandler#writeAsXMI(org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation, org.dom4j.Node, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable, org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader)
     */
    public ETPairT<InstanceInformation, Node> writeAsXMI(
        InstanceInformation pInfo,
        Node pParentNode,
        SymbolTable symbolTable,
        IREClass pThisPtr,
        IREClassLoader pClassLoader)
    {
        ETPairT<InstanceInformation, Node> retVal
            = new ETPairT<InstanceInformation, Node>(pInfo, null);

        int max = getExpressionCount();
        IExpressionProxy proxy = null;
        for(int index = 0; index < max; index++)
        {
            proxy = getExpression(index);
            if(proxy != null)
            {
                retVal = proxy.writeAsXMI(pInfo,
                                         pParentNode,
                                         symbolTable,
                                         pThisPtr,
                                         pClassLoader);
            }
        }


        if(retVal.getParamOne() == null)
        {
           ObjectInstanceInformation pTemp = new ObjectInstanceInformation();
           pTemp.setInstanceOwner(pThisPtr);
           pTemp.setInstanceType(pThisPtr);
           retVal.setParamOne(pTemp);
        }
        return retVal;

    }

    /**
     * Converts the expression data into a string representation.
     *
     * @return The string representation.
     */
    public String toString()
    {
        String retVal = "";

        int max = getExpressionCount();

        IExpressionProxy proxy;
        for(int index = 0; index < max; index++)
        {
            proxy = getExpression(index);
            if(proxy != null)
            {
                if((retVal != null) && (retVal.length() > 0))
                {
                    retVal += ", ";
                }
                retVal += proxy.toString();
          }
       }

       return retVal;
    }

    /**
     * Adds a new token to the expression handler.
     *
     * @param exp [in] The token to add.
     */
    protected void addToken(ITokenDescriptor exp, String language)
    {
       TokenExpressionProxy proxy = new TokenExpressionProxy(exp);

       m_Expressions.add(proxy);
    }

    /**
     * Retrieves an expression from the collection of sub expressions.
     *
     * @param index [in] The expression to retrieve.
     *
     * @return The expression or NULL if the expression does not exist.
     */
    protected IExpressionProxy getExpression(int index)
    {
       IExpressionProxy retVal = null;

       if(index < m_Expressions.size())
       {
          retVal = m_Expressions.get(index);
       }
       return retVal;
    }

    /**
     * Retrieve the number of subexpressions that are being managed
     * by the expression.
     *
     * @return
     */
    protected int getExpressionCount()
    {
       return m_Expressions.size();
    }

}
