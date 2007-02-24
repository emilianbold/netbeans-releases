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

import org.netbeans.modules.uml.core.metamodel.core.foundation.DirectedRelationship;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public class Deployment extends DirectedRelationship implements IDeployment 
{

	/**
	 * 
	 */
	public Deployment() 
	{
		super();		
	}

	public ETList<IArtifact> getDeployedArtifacts()
	{
		ElementCollector<IArtifact> collector=new ElementCollector<IArtifact>();
		return collector.retrieveElementCollectionWithAttrIDs(this,"source", IArtifact.class);
	}
	
	public void addDeployedArtifact( IArtifact pVal )
	{
		addSource(pVal);
	}
	
	public void removeDeployedArtifact( IArtifact pVal )
	{
		removeSource(pVal);
	}
	
	public INode getLocation()
	{
		ElementCollector<INode> collector =
										 new ElementCollector<INode>();
		return collector.retrieveSingleElementWithAttrID(this,"target", INode.class);
	}
	
	public void setLocation( INode node )
	{
		final INode tmpNode = node;
		new ElementConnector<IDeployment>().setSingleElementAndConnect
						(
							this, tmpNode, 
							"target",
							 new IBackPointer<INode>() 
							 {
								 public void execute(INode obj) 
								 {
                                     obj.addDeployment(Deployment.this);
								 }
							 },
							 new IBackPointer<INode>() 
							 {
								 public void execute(INode obj) 
								 {
                                     obj.removeDeployment(Deployment.this);
								 }
							 }										
						);
	}
	
	public IDeploymentSpecification getSpecification()
	{
		ElementCollector<IDeploymentSpecification> collector =
										 new ElementCollector<IDeploymentSpecification>();
		return collector.retrieveSingleElementWithAttrID(this,"specification", IDeploymentSpecification.class);
	}
	
	public void setSpecification(IDeploymentSpecification pSpec )
	{
		new ElementConnector<IDeployment>().setSingleElementAndConnect
						(
							this, pSpec, 
							"specification",
							 new IBackPointer<IDeploymentSpecification>() 
							 {
								 public void execute(IDeploymentSpecification newD) 
								 {
                                     newD.addDeployment(Deployment.this);
								 }
							 },
							 new IBackPointer<IDeploymentSpecification>() 
							 {
								 public void execute(IDeploymentSpecification oldD) 
								 {
                                     oldD.removeDeployment(Deployment.this);
								 }
							 }										
						);
	}
	
	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 */	
	public void establishNodePresence(Document doc, Node parent)
	{
		buildNodePresence("UML:Deployment",doc,parent);
	}	
	
}


