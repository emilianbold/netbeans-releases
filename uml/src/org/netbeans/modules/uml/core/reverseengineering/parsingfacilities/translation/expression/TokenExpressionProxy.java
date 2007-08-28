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
 * File       : TokenExpressionProxy.java
 * Created on : Dec 10, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression;

import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.PrimitiveInstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class TokenExpressionProxy implements IExpressionProxy
{

    private ITokenDescriptor mToken = null;
    public TokenExpressionProxy(ITokenDescriptor pToken)
    {
        mToken = pToken;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IExpressionProxy#clear()
     */
    public void clear()
    {
     	if(mToken != null)
   		{
      		mToken = null;
   		}
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IExpressionProxy#getEndPosition()
     */
    public long getEndPosition()
    {
        long retVal = -1;
   		try
   		{
      		if(mToken != null)
      		{
         		long length =mToken.getLength();
         		long startPos = getStartPosition();
         		if((startPos >= 0) &&  (length >= 0))
         		{
            		retVal = startPos + length;
         		}
     		 }
   		}
   		catch(Exception e)
		{
			 e.printStackTrace();
		}   
   		return (int)retVal;
    }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IExpressionProxy#getStartLine()
     */
    public long getStartLine()
    {
		long retVal = -1;
   		if(mToken != null)
   		{
    		try
      		{
        		retVal = mToken.getLine();
      		}
      		catch(Exception e)
	  		{
		 		e.printStackTrace();
	  		}      
   		}
        return (int)retVal;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IExpressionProxy#getStartPosition()
     */
    public long getStartPosition()
    {
        long retVal = -1;
   		if(mToken != null)
   		{
      		try
      		{
         		retVal = mToken.getPosition();
      		}
      		catch(Exception e)
		  	{
			 	e.printStackTrace();
		  	}   
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
		PrimitiveInstanceInformation pInfo = new PrimitiveInstanceInformation();
   		if(mToken != null)
   		{
      		String type = mToken.getType();
      		if("Integer Constant".equals(type))
      		{
         		pInfo.setPrimitiveType("int");
         		retVal = pInfo;
      		}
      		else if("Character Constant".equals(type))
      		{
         		pInfo.setPrimitiveType("char");
         		retVal = pInfo;
      		}
      		else if("String Constant".equals(type))
      		{
         		pInfo.setPrimitiveType("java.lang.String");
         		retVal = pInfo;
      		}
      		else if("Float Constant".equals(type))
      		{
         		pInfo.setPrimitiveType("float");
         		retVal = pInfo;
      		}
      		else if("Double Constant".equals(type))
      		{
         		pInfo.setPrimitiveType("double");
         		retVal = pInfo;
      		}
      		else if("Long Constant".equals(type))
      		{
         		pInfo.setPrimitiveType("long");
         		retVal = pInfo;
      		}
      		else if("Byte Constant".equals(type))
      		{
         		pInfo.setPrimitiveType("byte");
         		retVal = pInfo;
      		}
      		else if("Short Constant".equals(type))
      		{
         		pInfo.setPrimitiveType("short");
         		retVal = pInfo;
     		}
      		else if("NULL".equals(type))
      		{
         		pInfo.setPrimitiveType(null);
         		retVal = pInfo;
      		}
      		else if("Boolean Constant".equals(type) || "Boolean".equals(type))
      		{
         		pInfo.setPrimitiveType("boolean");
         		retVal = pInfo;
      		}
      		else
      		{
         		String value = mToken.getValue();
         		if(value.length() > 0)
         		{
            		retVal = symbolTable.findInstance(value);
         		}
      		}
   		}
  		return retVal;
    }

	public String toString()
	{
   		String retVal = null;
   		if(mToken != null)
   		{
      		String value = mToken.getValue();
      		if(value.length() > 0)
      		{
         	     retVal = value;
      		}
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
        
		InstanceInformation retVal = null;
		if(mToken != null)
		{
			String type = mToken.getType();
	  		if(("Integer Constant".equals(type))     ||
		 	   ("Character Constant".equals(type))   ||
		 	   ("String Constant".equals(type))      ||
		 	   ("Float Constant".equals(type))       ||
		 	   ("Double Constant".equals(type))      ||
		 	   ("Long Constant".equals(type))        ||
		 	   ("NULL".equals(type))
			  )
	  		{
		 		PrimitiveInstanceInformation pInfon = new PrimitiveInstanceInformation();
		 		pInfon.setPrimitiveType(type);
		 		retVal = pInfon;
	  		}
	  		else
	  		{
				String value = mToken.getValue();
		 		if(value.length() > 0)
		 		{
					retVal = symbolTable.findInstance(value);
		 		}
	  		}
   		}
   		return new ETPairT<InstanceInformation, Node>(retVal, null);
    }

    public void processToken(ITokenDescriptor pToken, String lang)
    {
     // No respective Code In C++  
    }

}// End Of Class
