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
 * File       : SuperConstructorCallExpression.java
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
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.UnknownMethodDeclaration;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.ExpressionStateHandler;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREGeneralization;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IRESuperClass;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public class SuperConstructorCallExpression extends MethodCallExpression
{

	public SuperConstructorCallExpression() 
    {
        setDiscoverMethodName(false);
	}
	
	public MethodDeclaration getMethodDeclaration(InstanceInformation   pInfo,
												  IREClass  pThisPtr,
												  IREClassLoader pClassLoader,
												  ETList<ETPairT<InstanceInformation,String>>  arguments)
	{
		MethodDeclaration retVal = findSuperConstructor(pInfo, pThisPtr, pClassLoader, arguments);
		if(retVal == null)
		{
			IREGeneralization  pGeneralizations = pThisPtr.getGeneralizations();
			if(pGeneralizations != null)
			{  
				int count = pGeneralizations.getCount();
				if(count > 0)
				{
					IRESuperClass  pCurSuper = pGeneralizations.item(0);
					if(pCurSuper != null)
					{
						String methodName = pCurSuper.getName();
						retVal = new UnknownMethodDeclaration(methodName);
						retVal.setInstanceName("<SUPER>");
					}
				 }
			 }
		}
		return retVal;
	}

	public MethodDeclaration findSuperConstructor(InstanceInformation pInfo,
												  IREClass       pThisPtr,
												  IREClassLoader pClassLoader,
												  ETList<ETPairT<InstanceInformation,String>>   arguments)
	{
		MethodDeclaration retVal = null;
		if(pThisPtr != null)
		{
			IREGeneralization pGeneralizations = pThisPtr.getGeneralizations();
			if(pGeneralizations != null)
			{  
				int count = pGeneralizations.getCount();
				for(int index = 0; (index < count) && (retVal == null); index++)
				{
					IRESuperClass pCurSuper = pGeneralizations.item(index);
					if(pCurSuper != null)
					{
						String methodName = pCurSuper.getName();
						if(methodName.length() > 0)
						{
							IREClass pSuperClass = pClassLoader.loadClass(methodName, pThisPtr);
							if(pSuperClass != null)
							{
								ObjectInstanceInformation pTemp = new ObjectInstanceInformation();
								pTemp.setInstanceOwner(pSuperClass);
								pTemp.setInstanceType(pSuperClass);                     
								retVal = pTemp.getMethodDeclaration(methodName, arguments, pClassLoader, false);                     
								if(retVal == null)
								{
									retVal = getMethodDeclaration(pInfo, pSuperClass, pClassLoader, arguments);
								}
								else
								{
									retVal.setInstanceName("<SUPER>");
								}
							}
						}
					}
				}
			}        
		}
	   return retVal;
	}
}
