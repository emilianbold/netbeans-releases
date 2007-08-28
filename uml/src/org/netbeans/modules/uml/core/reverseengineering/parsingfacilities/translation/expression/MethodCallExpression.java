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
 * File       : MethodCallExpression.java
 * Created on : Dec 10, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression;

import java.util.ArrayList;
import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.MethodDeclaration;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.ObjectInstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.REClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.ExpressionStateHandler;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.StateHandler;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IDependencyEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.ScopeKind;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class MethodCallExpression extends ExpressionStateHandler
{
   private ITokenDescriptor  m_pArgumentStart = null;
   private ITokenDescriptor  m_pArgumentEnd = null;
   private ITokenDescriptor  m_MethodName = null;
   private ITokenDescriptor  m_ExtraScopeOperator = null;
   private Expression        m_InstanceExpression = new Expression();
   private boolean           m_DiscoverMethodName;
   private int               m_OtherInstanceStates;
   
   public MethodCallExpression()
   {
      super();
      m_pArgumentStart = null;
      m_pArgumentEnd = null;
      m_MethodName = null;
      m_DiscoverMethodName = true;
      m_OtherInstanceStates = 0;
   }
   
   public void clear()
   {
      //		No Any Respective Code In C++
   }
   
   public void initialize()
   {
      // No any Respective Code IN C++
   }
   
   public void setDiscoverMethodName(boolean value)
   {
      m_DiscoverMethodName = value;
   }
   
   
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IMethodCallExpression#createSubStateHandler(java.lang.String, java.lang.String)
         */
   public StateHandler createSubStateHandler(String stateName, String language)
   {
      StateHandler retVal = null;
      if("Expression List".equals(stateName))
      {
         retVal = this;
         if(m_OtherInstanceStates > 0)
         {
            m_OtherInstanceStates++;
            m_DiscoverMethodName = true;
            m_InstanceExpression.addState(stateName, language);
         }
         else
         {
            m_DiscoverMethodName = false;
         }
      }
      else if("Argument".equals(stateName))
      {
         retVal = this;
         m_DiscoverMethodName = false;
      }
      else if(m_DiscoverMethodName == true)
      {
         if(m_OtherInstanceStates > 0)
         {
            m_OtherInstanceStates++;
            m_InstanceExpression.addState(stateName, language);
         }
         else if( !"Identifier".equals(stateName))
         {
            if(m_DiscoverMethodName == true)
            {
               m_OtherInstanceStates++;
            }
            m_InstanceExpression.addState(stateName, language);
         }
         retVal = this;
      }
      else
      {
         retVal = super.createSubStateHandler(stateName, language);
      }
      return retVal;
   }
   
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IMethodCallExpression#getArgumentInstances(org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation, org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher, org.dom4j.Node)
         */
   public ETList<ETPairT<InstanceInformation,String>> getArgumentInstances(InstanceInformation pInfo,
                                                                           IREClass pThisPtr,
                                                                           SymbolTable symbolTable,
                                                                           IREClassLoader pClassLoader,
                                                                           IUMLParserEventDispatcher pDispatcher,
                                                                           Node pParentNode)
   {
      ETList<ETPairT<InstanceInformation,String>> retVal = new ETArrayList<ETPairT<InstanceInformation,String>> ();
      int max = getExpressionCount();
      
      for(int index = 0; index < max; index++)
      {
         IExpressionProxy proxy = getExpression(index);
         if(proxy != null)
         {            
            InstanceInformation curRef = null;
            if(pDispatcher != null)
            {
               curRef = proxy.sendOperationEvents(pInfo, pThisPtr, symbolTable, pClassLoader, pDispatcher,	pParentNode);
            }
            else
            {
               ETPairT<InstanceInformation, Node> pair = null;
               pair = proxy.writeAsXMI(pInfo, pParentNode, symbolTable, pThisPtr, pClassLoader);
               
               if(pair != null)
               {
                  curRef = pair.getParamOne();
               }
            }
            if(curRef != null)
            {
               ETPairT<InstanceInformation,String> paramInfo = new ETPairT<InstanceInformation,String>(curRef, proxy.toString());
               retVal.add(paramInfo);
            }
         }
      }
      return retVal;
   }
   
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IMethodCallExpression#getEndPosition()
         */
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
   
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IMethodCallExpression#getStartLine()
         */
   public long getStartLine()
   {
      long retVal = m_InstanceExpression.getStartLine();
      if((retVal < 0) && (m_MethodName != null))
      {
         retVal = m_MethodName.getLine();
      }
      return retVal;
   }
   
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IMethodCallExpression#getStartPosition()
         */
   public long getStartPosition()
   {
      long retVal = m_InstanceExpression.getStartPosition();
      if((retVal < 0) && (m_MethodName != null))
      {
         retVal = m_MethodName.getPosition();
      }
      return retVal;
   }
   
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IMethodCallExpression#sendOperationEvents(org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation, org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher, org.dom4j.Node)
         */
   public InstanceInformation sendOperationEvents(InstanceInformation pInfo,
   IREClass pThisPtr,
   SymbolTable symbolTable,
   IREClassLoader pClassLoader,
   IUMLParserEventDispatcher pDispatcher,
   Node pParentNode)
   {
      InstanceInformation retVal = null;
      
      InstanceInformation instRef = 
              m_InstanceExpression.sendOperationEvents(null, pThisPtr, symbolTable, 
              pClassLoader, pDispatcher, pParentNode);
      
      if(instRef == null)
      {
          String sImport = null ;
          
          if (isStringConstant()) {
              instRef = getStringInstance(pClassLoader);
              
          } else if ((sImport = isStaticImport(pClassLoader, pThisPtr)) != null) {
              instRef = getStaticImportInstance (sImport, pClassLoader) ;
          }
          else
              instRef = getThisInstance(pThisPtr);
      }
      
      ETList<ETPairT<InstanceInformation,String>> args = 
              getArgumentInstances(retVal, pThisPtr, symbolTable, pClassLoader, pDispatcher, pParentNode);
      
      MethodDeclaration declaration = 
              getMethodDeclaration(instRef, pThisPtr, pClassLoader, args);
      
      try
      {
         if(declaration != null)
         {
            if (declaration.getOperation() == null || declaration.getOperation().getOwnerScope() == ScopeKind.SK_CLASSIFIER) {
                declaration.setInstanceName("");
            }
             
            retVal = declaration.getReturnInstance(pClassLoader);
            InstanceInformation ref = instRef;
            if(ref == null)
            {
               ObjectInstanceInformation pTemp = new ObjectInstanceInformation();
               pTemp.setInstanceOwner(pThisPtr);
               pTemp.setInstanceType(pThisPtr);
               pTemp.setInstanceName("<THIS>");
               ref = pTemp;
            }
            long lineNumber = -1;
            if(m_MethodName != null)
            {
               lineNumber = m_MethodName.getLine();
            }
            
            declaration.sendMethodCallEvent(pParentNode,
            (int) lineNumber,
            ref,
            args,
            pDispatcher);
         }
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
      return retVal;
   }
   
   private boolean isStringConstant() {
       String s = m_InstanceExpression.toString() ;
       if (s.startsWith("\"") && s.endsWith("\""))
           return true ;
       
       return false ;
   }
   
   private String isStaticImport(IREClassLoader classLoader, IREClass contextClass) {
       String queryName = this.m_MethodName.getValue() ;
       
       ArrayList < IDependencyEvent > dependencies = (ArrayList < IDependencyEvent >) classLoader.getDependencies(contextClass);
       if (dependencies != null)
            for (IDependencyEvent dependency : dependencies) {
                if (dependency == null) continue;
                
                if(dependency.isStaticDependency() == true) {
                    String name = dependency.getSupplier();
                    
                    if (name != null && name.length() > 0) {
                        //because it is a static import, the last section of the name
                        //needs to be removed to get the class
                        
                        String methodName = name.substring(name.lastIndexOf("::")+2);
                        if (queryName.equals(methodName)) {
                            return name.substring(0,name.lastIndexOf("::"));
                        }
                    }
                }
            }
       
   return null;    
   }
   
   private InstanceInformation getStaticImportInstance(String clazz, IREClassLoader pClassLoader)
   {
      ObjectInstanceInformation retVal = new ObjectInstanceInformation();
      retVal.setInstanceName(""); 
      
      retVal.setInstantiatedType(clazz, pClassLoader);
      retVal.setIsStatic(true);
      
      return retVal;
      
   }
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IMethodCallExpression#stateComplete(java.lang.String)
         */
public void stateComplete(String stateName)
   {
      if((m_DiscoverMethodName == true) &&  (m_OtherInstanceStates > 0))
      {
         m_OtherInstanceStates--;
         m_InstanceExpression.endState(stateName);
      }
      else if(("Expression List".equals(stateName)) &&  (m_DiscoverMethodName == true)       &&
      (m_OtherInstanceStates <= 0))
      {
         m_DiscoverMethodName = false;
      }
      super.stateComplete(stateName);
   }
   
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IMethodCallExpression#writeAsXMI(org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation, org.dom4j.Node, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable, org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass, null)
         */
   public ETPairT<InstanceInformation,Node> writeAsXMI(InstanceInformation pInfo,
   Node pParentNode,
   SymbolTable symbolTable,
   IREClass pThisPtr,
   IREClassLoader pClassLoader)
   {
      InstanceInformation retVal = null;
      InstanceInformation reciever = null;
      Node pNode = null;
      ETPairT<InstanceInformation,Node> temp = null;
      if(m_MethodName != null)
      {
         String methodName = m_MethodName.getValue();
         temp = m_InstanceExpression.writeAsXMI(pInfo, pParentNode, symbolTable, pThisPtr, pClassLoader);
         
         reciever = temp.getParamOne();
         ETList<ETPairT<InstanceInformation,String>> args = getArgumentInstances(reciever, pThisPtr, symbolTable, pClassLoader, null, pParentNode);
         MethodDeclaration declaration = getMethodDeclaration(reciever, pThisPtr, pClassLoader, args);
         try
         {
            if(declaration != null)
            {
               retVal = declaration.getReturnInstance(pClassLoader);
               InstanceInformation ref = reciever;
               if(ref == null)
               {
                  ObjectInstanceInformation pTemp = new ObjectInstanceInformation();
                  pTemp.setInstanceOwner(pThisPtr);
                  pTemp.setInstanceType(pThisPtr);
                  ref = pTemp;
               }
               long lineNumber = m_MethodName.getLine();
               pNode = declaration.generateXML(pParentNode,
               (int) lineNumber, ref, args);
               declaration = null;
            }
         }
         catch(Exception e)
         {
            e.printStackTrace();
         }
      }
      return new ETPairT<InstanceInformation,Node>(retVal , pNode);
   }
   
   public MethodDeclaration getMethodDeclaration(InstanceInformation pInfo, IREClass pThisPtr,
   IREClassLoader pClassLoader, ETList<ETPairT<InstanceInformation,String>>  arguments)
   {
      MethodDeclaration retVal = null;
      String methodName = null;
      methodName = getMethodName(methodName, pThisPtr);
      if(methodName != null && methodName.length() > 0)
      {
         if(pInfo == null)
         {
            ObjectInstanceInformation pTemp = new ObjectInstanceInformation();
            pTemp.setInstanceOwner(pThisPtr);
            pTemp.setInstanceType(pThisPtr);
            retVal = pTemp.getMethodDeclaration(methodName,
            arguments,
            pClassLoader,
            true);
         }
         else
         {
            retVal = pInfo.getMethodDeclaration(methodName,
            arguments,
            pClassLoader,
            true);
         }
      }
      return retVal;
   }
   
   public String toString()
   {
      String retVal = "";
      retVal += m_InstanceExpression.toString();
      if(m_ExtraScopeOperator != null)
      {
         String value =  m_ExtraScopeOperator.getValue();
         retVal += value;
      }
      
      if(m_MethodName != null)
      {
         String value =  m_MethodName.getValue();
         retVal += value;
      }
      if(m_pArgumentStart != null)
      {
         String value = m_pArgumentStart.getValue();
         retVal += value;
      }
      
      // Now process the arguments.
      int max = getExpressionCount();
      for(int index = 0; index < max; index++)
      {
         IExpressionProxy proxy = getExpression(index);
         if(proxy != null)
         {
            retVal += proxy.toString();
         }
      }
      
      if(m_pArgumentEnd != null)
      {
         String value =  m_pArgumentEnd.getValue();
         retVal += value;
      }
      return retVal;
   }
   
   
   
   public void processToken(ITokenDescriptor  pToken, String language)
   {
      if(pToken != null)
      {
         String type =  pToken.getType();
         String value = pToken.getValue();
         if(m_DiscoverMethodName == true)
         {
            if(m_OtherInstanceStates == 0)
            {
               if("Argument Start".equals(type))
               {
                  m_DiscoverMethodName = false;
                  m_pArgumentStart = pToken;
               }
               else if("Argument End".equals(type))
               {
                  m_DiscoverMethodName = false;
                  m_pArgumentEnd = pToken;
               }
               else if("Scope Operator".equals(type))
               {
                  if(m_ExtraScopeOperator != null)
                  {
                     m_InstanceExpression.addToken(m_ExtraScopeOperator, language);
                     m_ExtraScopeOperator = null;
                  }
                  m_InstanceExpression.addState("Identifier", language);
                  m_ExtraScopeOperator = pToken;
               }
               else if( "Identifier".equals(type))
               {
                  if(m_MethodName != null)
                  {
                     m_InstanceExpression.addToken(m_MethodName, language);
                     m_MethodName = null;
                  }
                  m_MethodName = pToken;
               }
               //                  else if("Super Class Reference".equals(type))
               //                  {
               //                     m_InstanceExpression.addToken(pToken, language);
               //                  }
               else if("Class".equals(type))
               {
                  
                  if(m_MethodName != null)
                  {
                     m_InstanceExpression.addToken(m_MethodName, language);
                     m_MethodName = null;
                  }
                  // Always add the identifier token to the instance expression.
                  //
                  // REASON: the class token is always an attribute (Property) of
                  //         an object.  So if we have method call there must be
                  //         something that follows.
                  m_InstanceExpression.addToken(pToken, language);
               }
               
               // I am comment out the above code to handle the "Class" type token.
               // Basically if the above test fail then we should forward the tokens
               // to the instance expression.
               else
               {
                  m_InstanceExpression.addToken(pToken, language);
               }
            }
            else
            {
               m_InstanceExpression.addToken(pToken, language);
            }
         }
         else if("Argument End".equals(type))
         {
            m_DiscoverMethodName = false;
            m_pArgumentEnd = pToken;
         }
         else
         {
            super.processToken(pToken, language);
         }
      }
   }
   
   public InstanceInformation getStringInstance(IREClassLoader pClassLoader)
   {
      ObjectInstanceInformation retVal = new ObjectInstanceInformation();
      
//      retVal.setInstanceOwner(emptyStr);
//      retVal.setInstanceType(emptyStr);
      retVal.setInstanceName(""); 
      
      retVal.setInstantiatedType("java::lang::String", pClassLoader);
      
      return retVal;
   }
   
   public InstanceInformation getThisInstance(IREClass pThis)
   {
      ObjectInstanceInformation retVal = new ObjectInstanceInformation();
      
      retVal.setInstanceOwner(pThis);
      retVal.setInstanceType(pThis);
      retVal.setInstanceName("<THIS>");
      return retVal;
   }
   
   public String  getMethodName(String pVal, IREClass pThisPtr)
   {
      pVal = null;
      try
      {
         if(m_MethodName != null)
         {
            pVal =  m_MethodName.getValue();
         }
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
      return pVal;
   }
}// End Of Class
