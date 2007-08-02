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

package org.netbeans.modules.uml.core.metamodel.structure;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public class Comment extends NamedElement implements IComment 
{

	/**
	 * 
	 */
	public Comment() 
	{
		super();	
	}
	
	/**
	 * Is this element in the AnnotatedElements list?
	 *
	 * @param element[in] The element to check
	 */
	public boolean getIsAnnotatedElement( INamedElement element )
	{
		boolean isAnnotated = false;
		String annotatedElems = XMLManip.getAttributeValue(m_Node,
                        "annotatedElement");
		if (annotatedElems != null && annotatedElems.length() > 0)
		{
			String xmiID = element.getXMIID();
			if ( annotatedElems.indexOf(xmiID) != -1)
			{
				isAnnotated = true;														   
			}
		}
		return isAnnotated;
	}
	
	public ETList<INamedElement> getAnnotatedElements()
	{
		ElementCollector<INamedElement> collector = 
                        new ElementCollector<INamedElement>();
		return collector.retrieveElementCollectionWithAttrIDs(this,
                        "annotatedElement", INamedElement.class);
	}
	
	public void removeAnnotatedElement( INamedElement element )
	{
		removeElementByID(element,"annotatedElement");
	}
	
	public void addAnnotatedElement( INamedElement element )
	{
		addElementByID(element,"annotatedElement");
	}
	
	/**
	 *
	 * The body of the comment.
	 */
	public void setBody( String newValue )
	{
		UMLXMLManip.setNodeTextValue(this,"UML:Comment.body",newValue,false);
	}
	
	public String getBody()
	{
		return XMLManip.retrieveNodeTextValue(m_Node,"UML:Comment.body");
	}
	
	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 */	
	public void establishNodePresence(Document doc, Node parent)
	{
		buildNodePresence("UML:Comment",doc,parent);
	}	
}



