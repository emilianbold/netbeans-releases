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
 * File       : TypeCastExpression.java
 * Created on : Dec 11, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression;

import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.MethodDeclaration;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.ObjectInstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.ExpressionStateHandler;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.StateHandler;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public class TypeCastExpression extends ExpressionStateHandler
{
   
   private ITokenDescriptor  m_pArgumentStart =  null;
   private ITokenDescriptor  m_pArgumentEnd =  null;
   private boolean           m_DiscoverType= false;
   private Expression        m_TypeExpression = new Expression();
   
   public TypeCastExpression()
   {
      m_DiscoverType= false;
   }
   
   public void initialize()
   {
      // No Respective Code in C++
   }
   
   public void processToken(ITokenDescriptor pToken, String language)
   {
      if(pToken != null)
      {
         String type = pToken.getType();
         if(m_DiscoverType == true)
         {
            m_TypeExpression.addToken(pToken, language);
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
   
   public StateHandler createSubStateHandler(String stateName, String language)
   {
      StateHandler retVal = null;
      if("Type".equals(stateName))
      {
         retVal = this;
         m_DiscoverType = true;
      }
      else if(m_DiscoverType == true)
      {
         m_TypeExpression.addState(stateName, language);
         retVal = this;
      }
      else
      {
         retVal = super.createSubStateHandler(stateName, language);
      }
      return retVal;
   }
   
   
   public void stateComplete(String stateName)
   {
      if(stateName.equals("Type"))
      {
         m_DiscoverType = false;
      }
      else
      {
         super.stateComplete(stateName);
      }
   }
   
   public ETPairT<InstanceInformation, Node> writeAsXMI(InstanceInformation pInfo,
   Node  pParentNode,
   SymbolTable  symbolTable,
   IREClass  pThisPtr,
   IREClassLoader pClassLoader
   )
   {
      InstanceInformation retVal = null;
      ETPairT<InstanceInformation, Node> temp = null;
      IExpressionProxy proxy = getExpression(0);
      if(proxy != null)
      {
         temp = proxy.writeAsXMI(pInfo, pParentNode, symbolTable, pThisPtr, pClassLoader);
      }
      retVal = temp.getParamOne();
      if(retVal == null)
      {
         retVal = pInfo;
         if(retVal == null)
         {
            ObjectInstanceInformation pTemp = new ObjectInstanceInformation();
            pTemp.setInstanceOwner(pThisPtr);
            pTemp.setInstanceType(pThisPtr);
            retVal = pTemp;
         }
      }
      return  new ETPairT<InstanceInformation, Node>(retVal, null);
   }
   
   public InstanceInformation sendOperationEvents(InstanceInformation  pInfo,
   IREClass             pThisPtr,
   SymbolTable          symbolTable,
   IREClassLoader       pClassLoader,
   IUMLParserEventDispatcher pDispatcher,
   Node           pParentNode)
   {
      InstanceInformation retVal = null;
      IExpressionProxy proxy = getExpression(0);
      if(proxy != null)
      {
         retVal = proxy.sendOperationEvents(pInfo, pThisPtr, symbolTable, pClassLoader, pDispatcher, pParentNode);
      }
      
      try
      {
         retVal = m_TypeExpression.sendOperationEvents(pInfo,pThisPtr,symbolTable,pClassLoader,pDispatcher,pParentNode);
      }
      catch(Exception e)
      {
         // COMErrorManager::ReportError(e);
      }
      
      return retVal;
   }
   
   
   public String toString()
   {
      String retVal = "";
      IExpressionProxy proxy = null;
      if(m_pArgumentStart != null)
      {
         String value =  m_pArgumentStart.getValue();
         retVal += value;
      }
      // 	   	proxy = getExpression(0);
      // 	   	if(proxy != null)
      // 	   	{
      // 	   		retVal += proxy.toString();
      // 	   	}
      retVal += m_TypeExpression.toString();
      if(m_pArgumentEnd != null)
      {
         String value =  m_pArgumentEnd.getValue();
         retVal += value;
      }
      
      int max = getExpressionCount();
      for(int index = 0; index < max; index++)
      {
         proxy = getExpression(index);
         if(proxy != null)
         {
            retVal += proxy.toString();
         }
      }
      return retVal;
   }
   
   /**
    * Retrieve the start position of the expression.  The start position
    * is the file position before the first character of the expression.
    *
    * @return The file position where the expression starts.
    */
   public long getStartPosition()
   {
      long retVal = -1;
      if(m_pArgumentStart != null)
      {
         retVal = m_pArgumentStart.getPosition();
      }
      else
      {
         retVal = super.getStartPosition();
      }
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
      IExpressionProxy proxy = getExpression(getExpressionCount() - 1);
      if(proxy != null)
      {
         retVal = proxy.getEndPosition();
      }
      else
      {
         retVal = super.getEndPosition();
      }
      return retVal;
   }
   
   /**
    * Retrieve the start position of the expression.  The start position
    * is the file position before the first character of the expression.
    *
    * @return The file position where the expression starts.
    */
   public long getStartLine()
   {
      long retVal = -1;
      if(m_pArgumentStart != null)
      {
         retVal = m_pArgumentStart.getLine();
      }
      else
      {
         retVal = super.getStartLine();
      }
      return retVal;
   }
   
   /**
    * Clears up the state handlers data.
    */
   public void clear()
   {
      // No Respective code In C++
   }
   
}
