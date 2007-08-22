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
 * File       : Expression.java
 * Created on : Dec 8, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression;


import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParser;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.PrimitiveInstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.StateHandlerController;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.ExpressionStateHandler;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacility;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacilityManager;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.Log;


public class Expression {

	private StateHandlerController  mController = null;
	private ExpressionStateHandler  m_RootExpression = null;
	private ITokenDescriptor        m_RootToken = null;
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IExpression#addState(java.lang.String, java.lang.String)
	 */
    public Expression() 
    {
        m_RootExpression = null;
        m_RootToken = null;
        mController = new StateHandlerController();
    }
    public Expression(String str) 
    {
        m_RootExpression = null;
        mController = new StateHandlerController();
    }
    
    
	public void addState(String stateName, String language)
	{
		if(mController != null)
			mController.addStateHandler(stateName, language);
		
		if(m_RootExpression == null)
		{
			m_RootExpression = (ExpressionStateHandler)mController.getCurrentState();
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IExpression#addToken(org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor, java.lang.String)
	 */
	public void addToken(ITokenDescriptor pToken, String language)
	{
		if(m_RootExpression != null)
		{
			mController.processToken(pToken, language);
		}
		else
		{
			m_RootToken = pToken;
	        String value = pToken.getValue();
	        String value2 = m_RootToken.getValue();
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IExpression#endState(java.lang.String)
	 */
	public void endState(String stateName)
	{
		mController.removeStateHandler(stateName);	
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IExpression#getEndPosition()
	 */
	public long getEndPosition()
	{
		long retVal = -1;
		if(m_RootExpression != null)
		{
			retVal = m_RootExpression.getEndPosition();
		}
		else if(m_RootToken != null)
		{
			try
			{
				long length =m_RootToken.getLength();
				long startPos = getStartPosition();
				if((startPos >= 0) &&  (length >= 0))
				{
					retVal = startPos + length;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IExpression#getLine()
	 */
	public long getLine()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IExpression#getStartLine()
	 */
	public long getStartLine()
	{
		 long retVal = -1;
		 if(m_RootExpression != null)
		 {
		 	retVal = m_RootExpression.getStartLine();
		 }
		 else if(m_RootToken != null)
		 {
		 	try
		 	{
				retVal = m_RootToken.getLine();
		 	}
		 	catch(Exception e)
		 	{
		 		e.printStackTrace();
		 	}
    	 }
		 return retVal;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IExpression#getStartPosition()
	 */
	public long getStartPosition()
	{
		long retVal = -1;
		if(m_RootExpression != null)
		{
			retVal = m_RootExpression.getStartPosition();
		}
		else if(m_RootToken != null)
		{
			try
			{
				retVal = m_RootToken.getPosition();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
        }
		return retVal;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IExpression#sendOperationEvents(org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation, org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.SymbolTable, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher, org.dom4j.Node)
	 */
	public InstanceInformation sendOperationEvents(InstanceInformation pInfo,
												   IREClass pThisPtr,
												   SymbolTable symbolTable,
												   IREClassLoader pClassLoader,
												   IUMLParserEventDispatcher pDispatcher,
												   Node pParentNode)
	{

		InstanceInformation retVal = null;            
		if(pDispatcher != null)
		{
			try
			{
				if(m_RootExpression != null)
				{         
					retVal = m_RootExpression.sendOperationEvents(pInfo, pThisPtr, symbolTable, pClassLoader, pDispatcher, pParentNode);
				}
				else if(m_RootToken != null)
				{  
					String tokenType = m_RootToken.getType();   
                                        if (tokenType.equalsIgnoreCase("string constant"))
                                            pInfo.sendCreationEvent(pParentNode, 1L, pDispatcher, null);
                                        
					PrimitiveInstanceInformation primRef = new PrimitiveInstanceInformation();
					if(primRef != null)
					{
						primRef.setPrimitiveType(tokenType);
						primRef.setInstantiatedType(tokenType, pClassLoader);
						retVal = primRef;                  
					}
				}   
			}
			catch(Exception e)
			{
				Log.stackTrace(e);
			}
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IExpression#writeAsXMI(org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation, org.dom4j.Node, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.SymbolTable, org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader)
	 */
	public ETPairT<InstanceInformation, Node> writeAsXMI(InstanceInformation pInfo,
										  				Node pParentNode,
										  				SymbolTable symbolTable,
										  				IREClass pThisPtr,
										  				IREClassLoader pClassLoader)
	{
	
		ETPairT<InstanceInformation, Node> retVal = new ETPairT<InstanceInformation, Node>();
		if(m_RootExpression != null)
		{
			retVal = m_RootExpression.writeAsXMI(pInfo, pParentNode, symbolTable, pThisPtr, pClassLoader);
		}
		return retVal;
	}
	
	public String toString()
	{
		String retVal = "";
		if(m_RootExpression != null)
		{
			retVal = m_RootExpression.toString();
		}
		else if(m_RootToken != null)
		{
			try
			{
				String value = m_RootToken.getValue();
				if(value.length() > 0)
				{
					retVal = value;
				}
			}
			catch(Exception  e)
			{
				e.printStackTrace();
			}
	   }
	   return retVal;
	}
	
	public IUMLParserEventDispatcher getEventDispatcher()
	{
		IUMLParserEventDispatcher pVal = null;
		try
		{
		  IFacilityManager pManager = null;
		  ICoreProduct pProduct = null;
		  pProduct = ProductRetriever.retrieveProduct();
		  if(pProduct != null)
		  {
			 pManager = pProduct.getFacilityManager();
			 if(pManager != null)
			 {
				IFacility pFacility = pManager.retrieveFacility("Parsing.UMLParser");
				IUMLParser pParser = (pFacility instanceof IUMLParser)? (IUMLParser)pFacility : null;
				if(pParser != null)
				{
					pVal = pParser.getUMLParserDispatcher();
				}
			 }
		  }
	   }
	   catch(Exception e)
	   {
	   	e.printStackTrace();
	   }
	   return pVal;
	}
}
