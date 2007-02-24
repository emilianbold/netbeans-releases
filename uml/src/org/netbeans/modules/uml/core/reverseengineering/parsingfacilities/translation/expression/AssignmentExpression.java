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
 * File       : AssignmentExpression.java
 * Created on : Dec 8, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;

public class AssignmentExpression extends BinaryExpression 
{

	public AssignmentExpression() {
		super();
	}
	
	public InstanceInformation	sendOperationEvents(InstanceInformation       pInfo,
													IREClass                  pThisPtr,
													SymbolTable               symbolTable,
													IREClassLoader            pClassLoader,
													IUMLParserEventDispatcher pDispatcher,
													Node               		  pParentNode)
	{
		InstanceInformation leftInstance = getLeftHandInstance(symbolTable, pThisPtr, pClassLoader, pParentNode);
		if(leftInstance != null)
		{
			if(leftInstance.isValid() == true)
			{
				if(leftInstance.isPrimitive() == false)
				{
					leftInstance.sendDestroy(pParentNode, pDispatcher);         
				}
			}     
		}
		
	 InstanceInformation rightInstance = getRightHandInstance(leftInstance, pThisPtr, symbolTable, pClassLoader, pDispatcher, pParentNode);
	 if(rightInstance != null)
	 {
		 String value = rightInstance.getInstanceTypeName();
		 if(leftInstance != null)
		 {
			leftInstance.setInstantiatedType(value, pClassLoader);
		 }
	  }
	  return leftInstance;
   }

   public InstanceInformation getLeftHandInstance(SymbolTable    symbolTable, 
												  IREClass       pThisPtr, 
												  IREClassLoader pClassLoader,
												  Node           pParentNode)
   {
	  InstanceInformation retVal = null;
	  String instanceName = getLeftHandSideString();
	  retVal = symbolTable.findInstance(instanceName);
	  if(retVal == null)
	  {
		 retVal = InstanceInformation.getInstanceDeclaration(instanceName, pThisPtr, pClassLoader);
		 if(retVal != null)
		 {
			symbolTable.addInstance(retVal, true);
			retVal.sendReference(pParentNode);
		 }
	  }
	  else if(retVal.isValid() == false)
	  {
		 retVal.sendReference(pParentNode);
	  }
	  return retVal;
   }

   public InstanceInformation getRightHandInstance(InstanceInformation  pInfo,
												   IREClass             pThisPtr,
												   SymbolTable          symbolTable,
												   IREClassLoader       pClassLoader,
												   IUMLParserEventDispatcher pDispatcher,
												   Node               pParentNode)
   	{
   		InstanceInformation retVal = null;
   		IExpressionProxy rightSide = getExpression(1);
   		if(rightSide != null)
   		{
   			retVal = rightSide.sendOperationEvents(pInfo, pThisPtr, symbolTable, pClassLoader, pDispatcher, pParentNode);
   		}
   		return retVal;
   }

}
