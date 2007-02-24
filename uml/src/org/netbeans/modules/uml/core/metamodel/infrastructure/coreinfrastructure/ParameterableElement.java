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


package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.OwnerRetriever;


public class ParameterableElement extends NamedElement
								  implements IParameterableElement  
{
	public ParameterableElement()
	{
		super();
	}
	
	/**
	 * property Default
	*/
	public IParameterableElement getDefaultElement()
	{
		ElementCollector<IParameterableElement> collector = 
									new ElementCollector<IParameterableElement>();
		return collector.retrieveSingleElementWithAttrID(this,"default", IParameterableElement.class);				
	}

	/**
	 * property Default
	*/
	public void setDefaultElement( IParameterableElement element )
	{
		super.setElement(element,"default");
	}

	/**
	 *  Sets the default element on this parameter via a name indicating its type.
 	 *
 	 * @param newVal[in] The type to use as the DefaultElement
	 */
	public void setDefaultElement2( String newVal )
	{
		INamedElement type = super.resolveSingleTypeFromString(newVal);
		if (type != null)
			setDefaultElement((IParameterableElement)type);
	}

	/**
	 * property Template
	*/
	public IClassifier getTemplate()
	{
		return OwnerRetriever.getOwnerByType(this, IClassifier.class);
	}

	/**
	 * property Template
	*/
	public void setTemplate( IClassifier newVal )
	{
		super.setOwner(newVal);
	}

	/**
	 * Name of the meta type that must be used when instantiating the template.
	*/
	public String getTypeConstraint()
	{
		return super.getAttributeValue("typeConstraint");
	}

	/**
	 * Name of the meta type that must be used when instantiating the template.
	*/
	public void setTypeConstraint( String newVal )
	{
		super.setAttributeValue("typeConstraint",newVal);
	}
	
	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 */	
	public void establishNodePresence(Document doc, Node parent)
	{
		super.buildNodePresence("UML:ParameterableElement",doc,parent);
	}		
}


