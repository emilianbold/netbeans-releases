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
 * Created on Dec 10, 2003
 *
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression;

import java.util.Stack;

import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.Identifier;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.ObjectInstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.ExpressionStateHandler;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.StateHandler;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREGeneralization;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IRESuperClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public class IdentifierExpression extends ExpressionStateHandler
{
   private Identifier m_Identifier = new Identifier();
   private ITokenDescriptor  m_ExtraScopeOperator = null;
   private int  m_NumIdentifier;
   boolean m_IsSuperReference;
   boolean m_IsThisReference;
   boolean m_IsClassReflection;
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IIdentifierExpression#clear()
     */
   public void clear()
   {
      m_Identifier.clear();
   }
   
   public IdentifierExpression()
   {
      m_NumIdentifier = 0;
      m_IsSuperReference = false;
      m_IsThisReference =false;
      m_IsClassReflection = false;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.IStateHandler#createSubStateHandler(java.lang.String, java.lang.String)
     */
   public StateHandler createSubStateHandler(String stateName, String language)
   {
      StateHandler retVal = null;
      if("Identifier".equals(stateName))
      {
         retVal = this;
      }
      else
      {
         retVal = super.createSubStateHandler(stateName, language);
      }
      return retVal;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.IExpressionStateHandler#getStartLine()
     */
   public long getStartLine()
   {
      return m_Identifier.getStartLine();
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.IExpressionStateHandler#getStartPosition()
     */
   public long getStartPosition()
   {
      return m_Identifier.getStartPosition();
   }
   
   public long  getEndPosition()
   {
      return m_Identifier.getEndPosition();
   }
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.IExpressionStateHandler#writeAsXMI(org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation, org.dom4j.Node, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable, org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader)
     */
   public ETPairT<InstanceInformation,Node> writeAsXMI(InstanceInformation pInfo,
   Node pParentNode,
   SymbolTable symbolTable,
   IREClass pThisPtr,
   IREClassLoader pClassLoader)
   {
      String instanceName = this.toString();
      InstanceInformation val = symbolTable.findInstance(instanceName);
      InstanceInformation temp = null;
      
      ETPairT<InstanceInformation,Node> retVal = new ETPairT< InstanceInformation,Node>(val,null);
      if(val == null)
      {
         retVal = super.writeAsXMI(pInfo, pParentNode, symbolTable, pThisPtr, pClassLoader);
         temp = getInstance(retVal.getParamOne(), pParentNode, pThisPtr, symbolTable, pClassLoader);
      }
      return retVal;
   }
   
   public void processToken(ITokenDescriptor  pToken, String language)
   {
      try
      {
         String type = pToken.getType();
         String value = pToken.getValue();
         //if((m_Identifier.GetLength() <= 0) && (type == _T("Scope Operator")))
         if((m_NumIdentifier < 2) && ("Scope Operator".equals(type)))
         {
            m_ExtraScopeOperator = pToken;
         }
         if( "Identifier".equals(type))
         {
            m_NumIdentifier++;
            if(m_NumIdentifier >= 2)
            {
               m_ExtraScopeOperator = null;
               m_NumIdentifier      = 0;
            }
         }
         else if("Super Class Reference".equals(type))
         {
            setIsSuperReference(true);
         }
         else if("This Reference".equals(type))
         {
            setIsThisReference(true);
         }
         else if("Class".equals(type))
         {
            // We are handling Reflection.  Therefore, only get the class that represents
            // Reflection "Class".  In the future I will need to make this a lot more
            // flexiable.  We will most likely want this to be a state.
            //
            // Example:
            //   Class Reflection
            //      Identifier {Class to be reflected.}
            //
            //m_Identifier.Clear();
            setIsClassReflection(true);
            m_NumIdentifier++;
            if(m_NumIdentifier >= 2)
            {
               m_ExtraScopeOperator = null;
               m_NumIdentifier      = 0;
            }
         }
         m_Identifier.addToken(pToken);
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }
   
   public InstanceInformation sendOperationEvents(InstanceInformation  pInfo,
   IREClass pThisPtr,
   SymbolTable symbolTable,
   IREClassLoader pClassLoader,
   IUMLParserEventDispatcher pDispatcher,
   Node pParentNode
   )
   {
      InstanceInformation retVal = null;
      if(isSuperReference() == true)
      {
         ObjectInstanceInformation pTemp = new ObjectInstanceInformation();
         pTemp.setInstanceOwner(pThisPtr);
         pTemp.setInstanceType(pThisPtr);
         pTemp.setInstanceName("<SUPER>");
         
         // InstanceRef manages the deletion of the pointer.  So, I do not have to
         // worry about deleting the pTemp here.
         retVal = pTemp;
      }
      else if(isThisReference() == true)
      {
         ObjectInstanceInformation pTemp = new ObjectInstanceInformation();
         pTemp.setInstanceOwner(pThisPtr);
         pTemp.setInstanceType(pThisPtr);
         pTemp.setInstanceName("<THIS>");
         
         // InstanceRef manages the deletion of the pointer.  So, I do not have to
         // worry about deleting the pTemp here.
         retVal = pTemp;
      }
      else if(isClassReflection() == true)
      {
         ObjectInstanceInformation pTemp = new ObjectInstanceInformation();
         if(pTemp != null)
         {
            pTemp.setInstanceOwner(pThisPtr);
            pTemp.setInstantiatedType("Class", pClassLoader);
            IREClass  pInstantiatedType = pTemp.getInstantiatedType();
            if(pInstantiatedType != null)
            {
               pTemp.setInstanceType(pInstantiatedType);
            }
         }
         // InstanceRef manages the deletion of the pointer.  So, I do not have to
         // worry about deleting the pTemp here.
         retVal = pTemp;
      }
      else
      {
         String instanceName = toString();
         retVal = symbolTable.findInstance(instanceName);
         
         // If the symbol was not found in the symbol table I want to look for
         // the instance.  When calling GetInstance reference events will be sent
         // where needed.
         //
         // If the symbol was found in the symbol table but was not instantiated then
         // I need to send a reference event.
         if(retVal == null)
         {
            String curInstanceName = null;
            retVal = super.sendOperationEvents(pInfo, pThisPtr, symbolTable, pClassLoader, pDispatcher, pParentNode);
            if(retVal == pInfo)
            {
               retVal = null;
            }
            retVal = getInstance(retVal, pParentNode, pThisPtr, symbolTable, pClassLoader);
         }
         else if((retVal != null) && (retVal.isValid() == false))
         {
            retVal.sendReference(pParentNode);
         }
      }
      return retVal;
   }
   
   boolean isSuperReference()
   {
      return m_IsSuperReference;
   }
   
   void setIsSuperReference(boolean value)
   {
      m_IsSuperReference = value;
   }
   
   boolean isThisReference()
   {
      return m_IsThisReference;
   }
   
   void setIsThisReference(boolean value)
   {
      m_IsThisReference = value;
   }
   
   public boolean isClassReflection()
   {
      return m_IsClassReflection;
   }
   
   public void setIsClassReflection(boolean value)
   {
      m_IsClassReflection = value;
   }
   
   
   /**
    * Converts the expression data into a string representation.
    *
    * @return The string representation.
    */
   public String toString()
   {
      String retVal = super.toString();
      if(m_ExtraScopeOperator != null)
      {
         String value = m_ExtraScopeOperator.getValue();
         retVal += value;
      }
      retVal += m_Identifier.getIdentifierAsSource();
      return retVal;
   }
   
   public InstanceInformation getInstance(InstanceInformation pInfo,
   Node pParentNode,
   IREClass pThisPtr,
   SymbolTable symbolTable,
   IREClassLoader pClassLoader)
   {
      InstanceInformation retVal = pInfo;
      
      Stack<EventExecutor.RefVariableDef> refData = new Stack<EventExecutor.RefVariableDef>();
      
      String curInstanceName = "";
      ETList<ITokenDescriptor> tokens = m_Identifier.getTokenList();
      
      IREClass curType = pThisPtr;
      for(int index = 0; index < tokens.size(); index++)
      {
         ITokenDescriptor pDescriptor = tokens.get(index);
         if(pDescriptor != null)
         {
            String type = pDescriptor.getType();
            
            
            if(type != null && type.length() > 0 &&
            !"Scope Operator".equals(type))
            {
               String attrName = pDescriptor.getValue();
            if(attrName.length() > 0)
            {
               curInstanceName += attrName;
            }
               if("Identifier".equals(type))
               {
                  if(retVal != null)
                  {
                     retVal = retVal.getInstanceDeclaration(attrName, pClassLoader);
                     if(retVal == null)
                     {
                        retVal = symbolTable.findInstance(curInstanceName);
                        if(retVal == null)
                        {
                           retVal = searchForInstance(attrName, curInstanceName, pThisPtr, symbolTable, pClassLoader);
                           
                           if(retVal == null)
                           {
                              // Basically create an Anonymous instance.  We are mostly in a referencing a
                              // static member.
                              retVal =  new ObjectInstanceInformation();
                              retVal.setInstanceTypeName(attrName);
                           }                           
                        }
                     }
                  }
                  else
                  {
                     if(retVal == null)
                     {
                        retVal = symbolTable.findInstance(curInstanceName);
                        if(retVal == null)
                        {
                           retVal = searchForInstance(attrName, curInstanceName, curType, symbolTable, pClassLoader);
                           if(retVal == null)
                           {
                              // Basically create an Anonymous instance.  We are mostly in a referencing a
                              // static member.
                              retVal =  new ObjectInstanceInformation();
                              retVal.setInstanceTypeName(attrName);
                           }
                        }
                     }
                     else
                     {
                        retVal = searchForInstance(attrName, curInstanceName, pThisPtr, symbolTable, pClassLoader);
                     }
                  }
                  if(retVal != null)
                  {
                     EventExecutor.RefVariableDef data =
                     retVal.getReferenceInfo();
                     refData.push(data);
                     
                     if (retVal instanceof ObjectInstanceInformation)
                     {
                        ObjectInstanceInformation objInstance = 
                            (ObjectInstanceInformation)retVal;
                        curType = objInstance.getInstanceType();
                        if (curType == null) curType = pThisPtr;
                     }
                     
                  }
               }
               else if("Super Class Reference".equals(type))
               {
                  retVal = getSuperInstance(pThisPtr, pClassLoader);
               }
            }
         }
         
         
      }
      EventExecutor.sendVariableReference(refData, pParentNode);
      return retVal;
   }
   
   
   public InstanceInformation searchForInstance(String attrName,
                                                String curInstanceName,
                                                IREClass  pThisPtr,
                                                SymbolTable  symbolTable,
                                                IREClassLoader pClassLoader)
   {
      InstanceInformation retVal = InstanceInformation.getInstanceDeclaration(attrName, pThisPtr, pClassLoader);
      if(retVal != null)
      {
         retVal.setInstanceName(curInstanceName);
         symbolTable.addInstance(retVal, true);
      }
      else
      {
         // Since I was not able to locate an instance variable, maybe it
         // is a class name.  This will be true if a data member or method call
         // is static.
         //
         // Java Example ETSystem.out.println();
         // out is a static data member or the class System.
         ObjectInstanceInformation pClassTemp = new ObjectInstanceInformation();
         pClassTemp.setInstanceOwner(pThisPtr);
         pClassTemp.setInstantiatedType(attrName, pClassLoader);
         IREClass pClass =  pClassTemp.getInstantiatedType();
         if(pClass != null)
         {
            pClassTemp.setInstanceType(pClass);
            retVal = pClassTemp;
            String typeName = pClassTemp.getInstanceTypeName();
         }
      }
      return retVal;
   }
   
   public InstanceInformation getSuperInstance(IREClass pThisPtr, IREClassLoader pClassLoader)
   {
      InstanceInformation retVal = null;
      if((pThisPtr != null) && (pClassLoader != null))
      {
         IREGeneralization  pGeneralization = pThisPtr.getGeneralizations();
         if(pGeneralization != null)
         {
            int max = pGeneralization.getCount();
            if(max > 0)
            {
               IRESuperClass  pSuperClassDef = pGeneralization.item(0);
               if(pSuperClassDef != null)
               {
                  String superClassName = pSuperClassDef.getName();
                  if(superClassName.length() > 0)
                  {
                     ObjectInstanceInformation pClassTemp = new ObjectInstanceInformation();
                     pClassTemp.setInstanceOwner(pThisPtr);
                     pClassTemp.setInstantiatedType(superClassName, pClassLoader);
                     IREClass pClass = pClassTemp.getInstantiatedType();
                     if(pClass != null)
                     {
                        pClassTemp.setInstanceType(pClass);
                        retVal = pClassTemp;
                     }
                     
                  }
               }
            }
         }
      }
      return retVal;
   }
}
