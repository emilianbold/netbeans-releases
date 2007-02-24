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
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateMachine;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class Interface extends Classifier implements IInterface 
{
	/**
	 * method AddReception
	*/
	public void addReception( IReception rec )
	{
		addFeature(rec);
	}

	/**
	 * method RemoveReception
	*/
	public void removeReception( IReception rec )
	{
		removeFeature(rec);
	}

	/**
	 * property Receptions
	*/
	public ETList<IReception> getReceptions()
	{
		ElementCollector<IReception> collector = new ElementCollector<IReception>();
		return collector.retrieveElementCollection(m_Node,
             "UML:Element.ownedElement/UML:Reception", IReception.class);
	}

	/**
	 * property ProtocolStateMachine
	*/
	public INamespace getProtocolStateMachine()
	{
		INamespace retSpace = null;
		ElementCollector<IStateMachine> collector = new ElementCollector<IStateMachine>();
		IStateMachine machine = collector.retrieveSingleElement(m_Node,"UML:Interface.protocolStateMachine/*", IStateMachine.class);
		if (machine != null)
			retSpace = (INamespace)machine;
		return retSpace;			
	}

	/**
	 * property ProtocolStateMachine
	*/
	public void setProtocolStateMachine( INamespace spcObj )
	{
		super.addChild("UML:Interface.protocolStateMachine",
						"UML:Interface.protocolStateMachine/UML:StateMachine",spcObj);
	}
	
	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 */	
	public void establishNodePresence(Document doc, Node parent)
	{
		super.buildNodePresence("UML:Interface",doc,parent);
	}
	
	public void establishNodeAttributes(Element node)
	{
		super.establishNodeAttributes(node);
		XMLManip.setAttributeValue(node,"isAbstract","true");
	}
	
	/**	 
	 * This routine is overloaded so that we may ensure that the
	 * correct stereotype is set on the interface after it has
	 * been created and added to a project.
	 */
	public void setOwner(IElement owner)
	{
		super.setOwner(owner);
		// Make sure that we have an "interface" stereotype
		super.ensureStereotype("interface");
	}
	
	/**	 
	 * This routine is overloaded here so that certain stereotypes
	 * are removed before a transform is done.
	 */
	public void preTransformNode(String typeName)
	{
		//super.deleteStereotype("PSK_INTERFACE");
            super.deleteStereotype("interface"); //Jyothi: Fix for Bug#6301700
	}
	
	public ETList<String> getCollidingNamesForElement(INamedElement ele)
	{
		ETList<String> values = new ETArrayList<String>();
		values.add("UML:Class");
		values.add("UML:Interface");
		values.add("UML:Enumeration");
		
		return values;
	}
}


