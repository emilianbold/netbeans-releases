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
				UMLXMLManip.setNodeTextValue(this, "UML:Expression.body", newVal, false);
			}
			else
			{
				UMLXMLManip.setNodeTextValue(this, "UML:Expression.body", "", false);
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



