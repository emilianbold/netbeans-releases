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


package org.netbeans.modules.uml.core.metamodel.core.constructs;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.EncapsulatedClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IReception;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author sumitabhk
 *
 */
public class Class extends EncapsulatedClassifier implements IClass
{
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IClass#addReception(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IReception)
	 */
	public void addReception(IReception rec) 
	{
		 super.addFeature(rec);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IClass#removeReception(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IReception)
	 */
	public void removeReception(IReception rec) {
		super.removeFeature(rec);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IClass#getReceptions()
	 */
	public ETList <IReception> getReceptions() {
        ElementCollector < IReception > col = new ElementCollector < IReception >();
//        return col.retrieveElementCollection( m_Node, (IReception)null, "UML:Classifier.feature/UML:Reception");
        return col.retrieveElementCollection( m_Node, "UML:Element.ownedElement/UML:Reception", IReception.class);
	}

	public boolean getIsActive() {
		return getBooleanAttributeValue( "isActive", false );
	}

	public void setIsActive(boolean value) {
		setBooleanAttributeValue( "isActive", value );
	}

	public boolean getIsStruct() {
		return getBooleanAttributeValue( "isStruct" , false );
	}

	public void setIsStruct(boolean value) {
		setBooleanAttributeValue( "isStruct", value );
	}

	public boolean getIsUnion() {
		return getBooleanAttributeValue( "isUnion", false );
	}

	public void setIsUnion(boolean value) {
		setBooleanAttributeValue( "isUnion", value );
	}

	public ETList<String> getCollidingNamesForElement(INamedElement ele)
	{
		ETList<String> values = new ETArrayList<String>();
		values.add("UML:Class");
		values.add("UML:Interface");
		values.add("UML:Enumeration");
		
		return values;
	}

	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 *
	 * @return HRESULT
	 */
    public void establishNodePresence( Document doc , Node node )
    {
        buildNodePresence( "UML:Class", doc, node );
    }
}


