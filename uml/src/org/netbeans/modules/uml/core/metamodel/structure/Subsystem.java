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
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackageableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class Subsystem extends Component implements ISubsystem
{

	/**
	 * 
	 */
	public Subsystem() 
	{
		super();	
	}
	
	public ETList<IPackageableElement> getRealizationElements()
	{
		ElementCollector<IPackageableElement> collector = new ElementCollector<IPackageableElement>();
		return collector.retrieveElementCollectionWithAttrIDs(this,"realizationElement", IPackageableElement.class);
	}
	
	public void removeRealizationElement( IPackageableElement element )
	{
		UMLXMLManip.removeChild(m_Node,element);
	}
	
	public void addRealizationElement( IPackageableElement element )
	{
		addElementByID(element,"realizationElement");
	}
	
	public void addSpecificationElement( IPackageableElement element )
	{
		addElementByID(element,"specificationElement");
	}
	
	public void removeSpecificationElement( IPackageableElement element )
	{
		UMLXMLManip.removeChild(m_Node,element);
	}
	
	public ETList<IPackageableElement> getSpecificationElements()
	{
		ElementCollector<IPackageableElement> collector = new ElementCollector<IPackageableElement>();
		return collector.retrieveElementCollectionWithAttrIDs(this,"specificationElement", IPackageableElement.class);
	}
	
	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 */	
	public void establishNodePresence(Document doc, Node parent)
	{
		buildNodePresence("UML:Subsystem",doc,parent);
	}	
}


