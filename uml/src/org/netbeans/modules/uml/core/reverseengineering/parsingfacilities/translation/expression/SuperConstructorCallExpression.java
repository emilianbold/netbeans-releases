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
