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


package org.netbeans.modules.uml.core.metamodel.profiles;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.constructs.Class;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


/**
 * Stereotype is the implementation of the UML Stereotype meta type.
 * A stereotype defines how an existing metaclass (or stereotype) may be
 * extended, and enables the use of platform or domain specific
 * terminology or notation in addition to the ones used for the extended
 * metaclass.
 */
public class Stereotype extends Class implements IStereotype
{

	/**
	 *
	 * Retrieves the names of the Meta types that this Stereotype can be applied to.
	 */
	public ETList<String> appliesTo() 
	{
		String values = UMLXMLManip.getAttributeValue(m_Node, "appliesTo");
		ETList<String> strs = null;
		if (values != null && values.length() > 0)
		{
			strs = StringUtilities.splitOnDelimiter(values, " ");			
		}
		return strs;				
	}

	/**
	 *
	 * Adds a meta type that this Stereotype will now be able to be applied to.
	 *
	 * @param sType[in] The name of the meta type, e.g. "Class"
	 */
	public void addApplicableMetaType(String sType) 
	{
		String curValue = UMLXMLManip.getAttributeValue( m_Node, "appliesTo" );
		if (curValue != null && curValue.length() > 0)
		{
			curValue += " ";
			curValue += sType;		
		}
		else
		{
			curValue = sType;
		}
		
		UMLXMLManip.setAttributeValue(this,"appliesTo",curValue);
	}

	/**
	 *
	 * Removes the passed-in meta type from this Stereotype's list of applicable
	 * types. Essentially, this now prevents this Stereotype from being applied 
	 * to an instance of that meta type.
	 *
	 * @param sType[in] The meta type to remove
	 */
	public void removeApplicableMetaType(String sType) 
	{
		if (sType != null && sType.length() > 0)
		{
			String curValue = UMLXMLManip.getAttributeValue(m_Node, "appliesTo" );
			if (curValue != null && curValue.length() > 0)
			{
				String newValue = UMLXMLManip.removeElementFromString(sType, curValue);
				UMLXMLManip.setAttributeValue(this,"appliesTo",newValue);
			}
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
		buildNodePresence("UML:Stereotype",doc,parent);
	}	

}


