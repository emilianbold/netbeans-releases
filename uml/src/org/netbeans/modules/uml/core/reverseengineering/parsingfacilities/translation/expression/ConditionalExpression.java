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
 * File       : ConditionalExpression.java
 * Created on : Dec 9, 2003
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


public class ConditionalExpression  extends ExpressionStateHandler
{
	private ITokenDescriptor  m_pPrecedenceStart     = null;
	private ITokenDescriptor  m_pPrecedenceEnd 	     = null;
	private ITokenDescriptor  m_pQuestionOperator    = null;
	private ITokenDescriptor  m_pConditionalOperator = null;
	
	public void clear()
	{
		m_pPrecedenceStart     = null;
		m_pPrecedenceEnd       = null;
		m_pQuestionOperator    = null;
		m_pConditionalOperator = null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IConditionalExpression#getEndPosition()
	 */
	public long getEndPosition()
	{
		long retVal = -1;
  	   	if(m_pPrecedenceEnd != null)
		{
  	   		retVal = m_pPrecedenceEnd.getPosition() 
  	   						+ m_pPrecedenceEnd.getLength();
		 }
		 else
		 {
//		 	TODO aztec
			retVal = new ExpressionStateHandler().getEndPosition();
//		 	retVal = ExpressionStateHandler::GetEndPosition();
		 }
		 return retVal;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IConditionalExpression#getStartLine()
	 */
	public long getStartLine()
	{
		 long retVal = -1;
		 if(m_pPrecedenceStart != null)
		 {
			retVal = m_pPrecedenceStart.getLine();
		 }
		 else
		 {
			retVal = new ExpressionStateHandler().getStartLine();
		 }
		 return retVal;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IConditionalExpression#getStartPosition()
	 */
	public long getStartPosition()
	{
		long retVal = -1;
		if(m_pPrecedenceStart != null)
		{
			retVal = m_pPrecedenceStart.getPosition();
		}
		else
		{
			retVal = new ExpressionStateHandler().getStartPosition();
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IConditionalExpression#initialize()
	 */
	public void initialize()
	{
		// No any code in C++
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IConditionalExpression#processToken(org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor, java.lang.String)
	 */
	public void processToken(ITokenDescriptor pToken, String language)
	{
		if(pToken != null)
		{
			String type = pToken.getType();
			if("Precedence Start".equals(type))
			{
				m_pPrecedenceStart = pToken;
			}
			else if("Precedence End".equals(type))
			{
				m_pPrecedenceEnd = pToken;
			}
			else if("Operator".equals(type))
			{
				if(m_pQuestionOperator == null)
				{
					m_pQuestionOperator = pToken;
				}
				else if(m_pConditionalOperator == null)
				{
					m_pConditionalOperator = pToken;
				}
			}
			else
			{
				super.processToken(pToken, language);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IConditionalExpression#writeAsXMI(org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.InstanceRef, org.dom4j.Node, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.SymbolTable, org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader)
	 */
	public ETPairT<InstanceInformation,Node>  writeAsXMI(InstanceInformation pInfo, Node pParentNode,	SymbolTable symbolTable,IREClass pThisPtr, IREClassLoader pClassLoader)
	{
		InstanceInformation ref = pInfo;
		if(ref == null)
		{
			ObjectInstanceInformation pTemp = new ObjectInstanceInformation();
			pTemp.setInstanceOwner(pThisPtr);
			pTemp.setInstanceType(pThisPtr);
			ref = pTemp;
		}
		return new ETPairT<InstanceInformation, Node>(ref, null);
	}
	
	public String toString()
	{
		String retVal = "";
	   if(m_pPrecedenceStart != null)
	   {
		  String value =  m_pPrecedenceStart.getValue();
		  retVal += value;
	   }
	   // Add the part of the expression. 
	   IExpressionProxy questionExp = getExpression(0);
	   if(questionExp != null)
	   {
		  retVal += questionExp;
	   }
	   // Add the Question mark operator to the expression.
	   if(m_pQuestionOperator != null)
	   {
		  String value = m_pQuestionOperator.getValue();
		  retVal += " "+value+" ";
	   }
	   // Add the Truth part to the expression string.
	   IExpressionProxy trueExp = getExpression(1);
	   if(trueExp != null)
	   {
		  retVal += trueExp;
	   }
	   // Add the Colon mark operator to the expression.
	   if(m_pConditionalOperator != null)
	   {
		  String value =  m_pConditionalOperator.getValue();
		  retVal += " "+value+" ";
	   }
	   // Add the False part to the expression string.
	   IExpressionProxy falseExp = getExpression(2);
	   if(falseExp != null)
	   {
		  retVal += falseExp;
	   }
	   if(m_pPrecedenceEnd != null)
	   {
		  String value =  m_pPrecedenceEnd.getValue();
		  retVal += value;
	   }
	   return retVal;
	}
}
