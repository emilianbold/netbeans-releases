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

import org.netbeans.modules.uml.core.metamodel.core.foundation.DirectedRelationship;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;


public class UMLBinding extends DirectedRelationship implements IUMLBinding
{
	/**
	 * property Formal
	*/
	public IParameterableElement getFormal()
	{
		ElementCollector<IParameterableElement> collector = new ElementCollector<IParameterableElement>();
		return collector.retrieveSingleElementWithAttrID(this,"formal", IParameterableElement.class);		
	}

	/**
	 * property Formal
	*/
	public void setFormal( IParameterableElement formal )
	{
		setElement(formal,"formal");
	}
	/**
	 * Sets the formal parameter via name.
	 * 
	 * @param newVal[in]    The name of the parameter
	 * 
	 * @return HRESULT
	 * @see put_Formal
	 */
	public void setFormal2(String newVal )
	{
			UMLXMLManip manip;

			INamedElement namedElement = UMLXMLManip.resolveSingleTypeFromString(this, newVal);
			if (namedElement instanceof IParameterableElement)
			{
				IParameterableElement parmElement = (IParameterableElement)namedElement;
				setFormal( parmElement );
			}
	}

	/**
	 * property Actual
	*/
	public IParameterableElement getActual()
	{
		ElementCollector<IParameterableElement> collector = new ElementCollector<IParameterableElement>();
		return collector.retrieveSingleElementWithAttrID(this,"actual", IParameterableElement.class);			
	}

	/**
	 * property Actual
	*/
	public void setActual( IParameterableElement actual )
	{
		setElement(actual,"actual");
	}
	/**
	 * Sets the actual parameter via a name
	 * 
	 * @param newVal[in] The name
	 * 
	 * @return HRESULT
	 * @see put_Actual
	 */	
	public void setActual2(String newVal )
	{
			UMLXMLManip manip;

			INamedElement namedElement = UMLXMLManip.resolveSingleTypeFromString(this, newVal);
			if (namedElement instanceof IParameterableElement)
			{
				IParameterableElement parmElement = (IParameterableElement)namedElement;
				setActual( parmElement );
			}
	}
	
	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 */
	public void establishNodePresence(Document doc, Node parent)
	{
		buildNodePresence("UML:Binding",doc,parent);
	}			
}

