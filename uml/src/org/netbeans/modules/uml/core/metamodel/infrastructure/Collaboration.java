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


package org.netbeans.modules.uml.core.metamodel.infrastructure;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class Collaboration extends StructuredClassifier 
						   implements ICollaboration  
{
	public ETList<INamedElement> getConstrainingElements()
	{
		ElementCollector<INamedElement> coll = new ElementCollector<INamedElement>();
		return coll.retrieveElementCollection
											(m_Node, "UML:Collaboration.constrainingElement/*", INamedElement.class);
	}
	
	public void removeConstrainingElement( INamedElement element )
	{
		UMLXMLManip.removeChild(m_Node,element);
	}
	
	public void addConstrainingElement( INamedElement element )
	{
		super.addChild("UML:Collaboration.constrainingElement",
					   "UML:Collaboration.constrainingElement",
					   element);
	}
	
	public ETList<IClassifier> getNestedClassifiers()
	{
		ElementCollector<IClassifier> coll = new ElementCollector<IClassifier>();
		return coll.retrieveElementCollection
											(m_Node, "UML:Collaboration.nestedClassifier/*", IClassifier.class);			
	}
	
	public void removeNestedClassifier( IClassifier classifier )
	{
		UMLXMLManip.removeChild(m_Node,classifier);
	}
	
	public void addNestedClassifier( IClassifier classifier )
	{
		super.addChild("UML:Collaboration.nestedClassifier",
					   "UML:Collaboration.nestedClassifier",
					   classifier);
	}
	
	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 */	
	public void establishNodePresence(Document doc, Node parent)
	{
		buildNodePresence("UML:Collaboration",doc,parent);
	}	
	
	/**
	 * Retrieves the name of the element typically used for creating icons.  
	 * It is composed of the element type and any other information needed to 
	 * make this type unique, such as 'Class' or 'PseudoState_Interface'
	 *
	 * The default implementation of this routine just returns the element type.
	 */
	public String getExpandedElementType()
	{
		String retType = getElementType();
		// if we have a collaboration with template parameters then we are
		// assuming that it is a design pattern
		ETList<IParameterableElement> parameters = super.getTemplateParameters();
		if (parameters != null)
		{
			int count = parameters.size();
			if (count > 0)
			{
				retType = "DesignPattern"; 
			}
		}
		return retType;
	}

	/**
	 * Does this element have an expanded element type or is the expanded element type always the element type?
	 */
	public boolean getHasExpandedElementType()
	{
		return true;
	}
}


