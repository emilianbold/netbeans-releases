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
 * File       : SubExpressionProxy.java
 * Created on : Dec 10, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression;

import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.ExpressionStateHandler;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class SubExpressionProxy implements IExpressionProxy
{
	private ExpressionStateHandler mSubExpression = null;

    public SubExpressionProxy(ExpressionStateHandler expression)
    {
		mSubExpression = expression;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IExpressionProxy#clear()
     */
    public void clear()
    {
		if(mSubExpression != null)
			mSubExpression= null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IExpressionProxy#getEndPosition()
     */
    public long getEndPosition()
    {
		long retVal = -1;
		if(mSubExpression != null)
		{
			try
			{
				retVal = mSubExpression.getEndPosition();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}   
      
		}
		return retVal;
    }

   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IExpressionProxy#getStartLine()
     */
    public long getStartLine()
    {
		long retVal = -1;
		if(mSubExpression != null)
		{
			try
			{
				retVal = mSubExpression.getStartLine();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}   
		}
		return retVal;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IExpressionProxy#getStartPosition()
     */
    public long getStartPosition()
    {
		long retVal = -1;
		if(mSubExpression != null)
		{
			try
			{
				retVal = mSubExpression.getStartPosition();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}   
		}
		return retVal;
    }
    
    
	public String toString()
	{
	   String retVal = "";
	   if(mSubExpression != null)
	   {
		  retVal = mSubExpression.toString();
	   }
	   return retVal;
	}


    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IExpressionProxy#sendOperationEvents(org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation, org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher, org.dom4j.Node)
     */
    public InstanceInformation sendOperationEvents(InstanceInformation pInstance,
												   IREClass pThisPtr,
												   SymbolTable symbolTable,
												   IREClassLoader pClassLoader,
												   IUMLParserEventDispatcher pDispatcher,
												   Node pParentNode)
    {
		InstanceInformation retVal = null;
		if(mSubExpression != null)
		{
			retVal = mSubExpression.sendOperationEvents(pInstance, pThisPtr, symbolTable, pClassLoader, pDispatcher, pParentNode);
		}
		return retVal;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IExpressionProxy#writeAsXMI(org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation, org.dom4j.Node, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable, org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader)
     */
    public ETPairT<InstanceInformation, Node> writeAsXMI(InstanceInformation pInfo,
    													 Node pParentNode,
    													 SymbolTable symbolTable,
    													 IREClass pThisPtr,
    													 IREClassLoader pClassLoader)
    {
		ETPairT<InstanceInformation, Node> retVal = new ETPairT<InstanceInformation,Node>(null,null);
		if(mSubExpression != null)
		{
			retVal = mSubExpression.writeAsXMI(pInfo, pParentNode, symbolTable, pThisPtr, pClassLoader);
		}
		return retVal;
    }
    
    public void processToken(ITokenDescriptor  pToken, String language)
    {
       if(mSubExpression != null)
       {
          mSubExpression.processToken(pToken, language);
       }
    }
}
