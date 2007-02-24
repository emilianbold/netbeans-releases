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

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.StructuredClassifier;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public class Node extends StructuredClassifier implements INode
{

	/**
	 * 
	 */
	public Node() 
	{
		super();		
	}
	
	public ETList<IDeployment> getDeployments()
	{
		ElementCollector<IDeployment> collector = new ElementCollector<IDeployment>();
		return collector.retrieveElementCollectionWithAttrIDs(this,"deployment", IDeployment.class);
	}
	
	public void removeDeployment( IDeployment deployment )
	{
		final IDeployment dep = deployment;
		new ElementConnector<INode>().removeByID(this,dep,"deployment",
			new IBackPointer<INode>() 
			{
				public void execute(INode obj) 
				{
					dep.setLocation(obj);
				}
			}
		);		
	}
	
	public void addDeployment( IDeployment deploy )
	{
		final IDeployment deployment = deploy;		
		new ElementConnector<INode>().addChildAndConnect(
										this, true, "deployment", 
										"deployment", deployment,
										 new IBackPointer<INode>() 
										 {
											 public void execute(INode obj) 
											 {
												deployment.setLocation(obj);
											 }
										 }
										 );
	}
	
	public void removeDeployedElement( INamedElement element )
	{
		removeElementByID(element,"deployedElement");
	}
	
	public void addDeployedElement( INamedElement element )
	{
		addElementByID(element,"deployedElement");
	}
	
	public ETList<INamedElement> getDeployedElements()
	{
		ElementCollector<INamedElement> collector = new ElementCollector<INamedElement>();
		return collector.retrieveElementCollectionWithAttrIDs(this,"deployedElement", INamedElement.class);
	}
	
	
	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 */	
	public void establishNodePresence(org.dom4j.Document doc, org.dom4j.Node parent)
	{
		buildNodePresence("UML:Node",doc,parent);
	}	
}


