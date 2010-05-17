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


package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression;

import java.util.Stack;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.ILanguageFacilityFactory;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParser;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.LanguageFacilityFactory;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IReferenceEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.ReferenceEvent;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlsupport.Log;

public class EventExecutor
{
	private Stack<RefVariableDef> refVariableStack = new Stack();
	
    public static class RefVariableDef
    {
        public String   variableName, variableType, declaringClassifier;
        public IREClass classDefinition;
    }
    
	public Stack<RefVariableDef> getRefVariableDefStack()
	{
		return refVariableStack;
	}
	
	public static void sendVariableReference(Stack<RefVariableDef> refData, Node pParentNode)
	{
		try
		{
			IUMLParserEventDispatcher  pDispatcher = getEventDispatcher();
			if((pDispatcher != null) && (pParentNode != null) && 
                    (refData != null && refData.empty() == false))
			{
				Node pRefVar = formatVariableReference(refData, pParentNode);
				if(pRefVar != null)
				{
					IReferenceEvent  pEvent = new ReferenceEvent();
					if(pEvent != null)
					{
						pEvent.setEventData(pRefVar);
						pDispatcher.fireReferencedVariable(pEvent, null);
					}
				}
			}
		}
		catch(Exception e)
		{
			Log.stackTrace(e);
     	}   
	}

	public static void sendDestroyAction(InstanceInformation ref, Node pParentNode)
	{
		try
		{
			IUMLParserEventDispatcher  pDispatcher = getEventDispatcher();
      		if(pDispatcher != null) 
      		{
      			ref.sendDestroy(pParentNode, pDispatcher);
      		}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}   
	}

	public static Node formatVariableReference(Stack refData, Node pParentNode)
	{
		Node pVal = null;
		try
		{
			Node     pCurNode = pParentNode;      
			while(refData.size() > 0)
			{         
				Node  pRefVar = XMLManip.createElement((Element)pCurNode, "ReferenceVariable");
				if(pRefVar != null)
				{    
					RefVariableDef data = (RefVariableDef) refData.pop(); 
					XMLManip.setAttributeValue(pRefVar, "name", data.variableName);
					XMLManip.setAttributeValue(pRefVar, "type", data.variableType);           
					XMLManip.setAttributeValue(pRefVar, "declaringClass", data.declaringClassifier);  
            
				// Only copy the first node to pVal.
					if(pVal == null)
					{
						pVal = pRefVar;
					}
					if(refData.size() == 1)
					{
						addClassifier(pVal, data);
					}
					pCurNode = null;
					pCurNode = pRefVar;               
				}
				if (!refData.empty())
				{
					refData.pop(); 
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}   
  	   return pVal;   
	}

	public static void addClassifier(Node pParentNode, RefVariableDef data)
	{
		try
		{      
			if(data.classDefinition != null)
			{
				Node  pNode = data.classDefinition.getEventData();
				if(pNode != null)
				{
					Node pCloneNode = (Node)pNode.clone();
					if(pCloneNode != null)
					{
						//TODO: Aztec:
						pCloneNode.setParent((Element)pParentNode);
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}   
   	}

	/**
	 * Retrieves the dispatcher to use when sending events.
	 *
	 * @param pVal [out] The result.
	 */
	public static IUMLParserEventDispatcher getEventDispatcher()
	{
		IUMLParserEventDispatcher pVal = null;
		try
		{
			ILanguageFacilityFactory  pFactory 	= new LanguageFacilityFactory();
			if(pFactory != null)
			{
				IUMLParser pParser = pFactory.getUMLParser();
				if(pParser != null)
				{
					pVal = pParser.getUMLParserDispatcher();
				}
			}          
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}   
		return pVal;
	}

	
	public static void createTokenDescriptor(Node pNode, String value, String type, long line, long col, long pos, long length)
	{
		try
		{
			Node pDescriptors =	ensureElementExists(pNode, "TokenDescriptors", "TokenDescriptors");
			if(pDescriptors != null)
			{
				Node  pDesc = XMLManip.createElement((Element)pDescriptors, "TDescriptor");
				if(pDesc != null)
				{
					Element pElement  = (pDesc instanceof Element)? (Element)pDesc :null;
					if(pElement != null)
					{
						pElement.setAttributeValue("line", new Long(line).toString()); 
				   		pElement.setAttributeValue("column", new Long(col).toString()); 
				   		pElement.setAttributeValue("position", new Long(pos).toString()); 
				   		pElement.setAttributeValue("type", type); 
				   		pElement.setAttributeValue("value", value); 
				   		pElement.setAttributeValue("length", new Long(length).toString()); 
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}   
	}
	
	public static Node ensureElementExists( Node curNode, String name, String query)
	{      
		Node node = null;
		try
		{  
			node = curNode.selectSingleNode(query);

		  // If not able to find the node then create it.
			if( node == null )
			{
			 // Node doesn't exist, so we need to create it.
				Document  doc  =  curNode.getDocument();
				if( doc != null)
				{
					Node newNode = XMLManip.createElement((Element)curNode, name);
					if( newNode != null)
					{
						node = newNode;
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}   
	   return node;
	}  
}
