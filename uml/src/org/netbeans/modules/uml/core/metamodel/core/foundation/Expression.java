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


package org.netbeans.modules.uml.core.metamodel.core.foundation;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * @author sumitabhk
 *
 */
public class Expression extends ValueSpecification implements IExpression{

	/**
	 *
	 */
	public Expression() {
		super();
	}

	/**
	 * Gets the text of the expression in the given language.
	 *
	 * @param body[out]
	 *
	 * @return S_OK
	 */
	public String getBody() {
		String val = XMLManip.retrieveNodeTextValue(m_Node, "UML:Expression.body");
		return val;
	}

	/**
	 * Sets the text of the expression in the given language.
	 * 
	 * @param newVal[in] 
	 *
	 * @return S_OK
	 */
	public void setBody(String newVal) {
		IExpressionListener listener = retrieveListener();
		boolean proceed = true;
		
		if (listener != null)
		{
			proceed = listener.onPreBodyModified(this, newVal);
		}
		
		if (proceed)
		{
			if (newVal.length() > 0)
			{
				UMLXMLManip.setNodeTextValue(m_Node, "UML:Expression.body", newVal, false);
			}
			else
			{
				UMLXMLManip.setNodeTextValue(m_Node, "UML:Expression.body", "", false);
			}
			
			if (listener != null)
			{
				listener.onBodyModified(this);
			}
		}
		else
		{
			//cancel the event
		}
	}

	public String getLanguage() {
		return getAttributeValue("language");
	}

	/**
	 *
	 * Sets the language property of this expression.
	 *
	 * @param newVal[in] The new value
	 *
	 * @return HRESULT
	 *
	 */
	public void setLanguage(String newVal) {
		IExpressionListener listener = retrieveListener();
		boolean proceed = true;
		
		if (listener != null)
		{
			proceed = listener.onPreLanguageModified(this, newVal);
		}
		
		if (proceed)
		{
			setAttributeValue("language", newVal);
			
			if (listener != null)
			{
				listener.onLanguageModified(this);
			}
		}
		else
		{
			//cancel the event
		}
	}

	/**
	 *
	 * Retrieves the grandparent node of this parameter node and queries
	 * to see if that object supports the IExpressionListener interface.
	 * If it does, it is returned.
	 *
	 * @param listener[out] The interface, else 0
	 *
	 * @return HRESULT
	 *
	 */
	private IExpressionListener retrieveListener() {
		IExpressionListener list = null;
		if (m_Node != null)
		{
			// The element that we care about is two parents up in the tree, i.e., the grandparent
			// of this node.
			Node parent = m_Node.getParent();
			if (parent != null)
			{
				Node grandParent = parent.getParent();
				if (grandParent != null)
				{
					FactoryRetriever ret = FactoryRetriever.instance();
					
					String name = XMLManip.retrieveSimpleName(grandParent);
                    Object element = ret.createTypeAndFill(name, grandParent);
                    if (element instanceof IExpressionListener)
                        list = (IExpressionListener) element;
				}
			}
		}
		return list;
	}

	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 *
	 * @return HRESULT
	 */
	public void establishNodePresence( Document doc, Node parent )
	{
	   buildNodePresence( "UML:Expression", doc, parent );
	}

}



