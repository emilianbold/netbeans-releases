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
 * File       : ArrayInitializerExpression.java
 * Created on : Dec 8, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression;

import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.ObjectInstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.ExpressionStateHandler;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;


public class ArrayInitializerExpression extends ExpressionStateHandler
{
	private ITokenDescriptor  m_pStartInitializer = null;
	private ITokenDescriptor  m_pEndInitializer = null;
	
	public void clear()
	{
		m_pStartInitializer  = null;
		m_pEndInitializer = null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IArrayInitializerExpression#getEndPosition()
	 */
	public long getEndPosition()
	{
		long retVal = -1;
	    if(m_pEndInitializer != null)
   		{

	    	retVal = m_pEndInitializer.getPosition() 
	    				+ m_pEndInitializer.getLength();
   		}
	    return retVal;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IArrayInitializerExpression#getStartLine()
	 */
	public long getStartLine() 
	{
		long retVal = -1;
		if(m_pStartInitializer != null)
   		{
      		retVal = m_pStartInitializer.getLine();
   		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IArrayInitializerExpression#getStartPosition()
	 */
	public long getStartPosition()
 	{
		long retVal = -1;
		if(m_pStartInitializer != null)
   		{
      		retVal = m_pStartInitializer.getPosition();
   		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IArrayInitializerExpression#initialize()
	 */
	public void initialize()
	{
		//Not implemented in C++ code.
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IArrayInitializerExpression#processToken(org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor, java.lang.String)
	 */
	public void processToken(ITokenDescriptor pToken, String language)
    {
		if(pToken != null)
   		{
    		String type = pToken.getType();
    		if("Start Array Init".equals(type))
      		{
        		m_pStartInitializer = pToken;
      		}
      		else if("End Array Init".equals(type))
      		{
        		m_pEndInitializer = pToken;
      		}
      		else
      		{
      			super.processToken(pToken, language);
      		}
   		}
	}
	
	public ETPairT<InstanceInformation, Node> writeAsXMI(InstanceInformation pInfo, 
						   							Node pParentNode,
						   							SymbolTable symbolTable,
						   							IREClass pThisPtr,
						   							IREClassLoader pClassLoader)
 	{
		ObjectInstanceInformation ref = null;
		if(pInfo == null)
   		{
      		ref = new ObjectInstanceInformation();
			ref.setInstanceOwner(pThisPtr);
			ref.setInstanceType(pThisPtr);
   		}
		return new ETPairT<InstanceInformation, Node>(ref, null);
	}

	public String toString()
	{
    	String retVal = "";
    	if(m_pStartInitializer != null)
   		{
    		String value =  m_pStartInitializer.getValue();
    		 retVal += value;
   		}

   // Process each expression and add a comment between them.
   		for(int index = 0; index < getExpressionCount(); index++)
   		{
      		IExpressionProxy exp = getExpression(index);
      		if(exp != null)
      		{
         		if(index > 0)
         		{
            		retVal += ", ";
         		}
         		retVal += exp.toString();
      		}
   		}
   		if(m_pEndInitializer != null)
		{
      		String value =  m_pEndInitializer.getValue();
      		retVal += value;
   		}
   		return retVal;
	}

}
