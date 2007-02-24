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
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public class DeploymentSpecification extends NamedElement 
									 implements IDeploymentSpecification
{
	/**
	 * 
	 */	
	public DeploymentSpecification() 
	{
		super();
	}
	
	public ETList<IArtifact> getDeploymentDescriptors()
	{
		ElementCollector<IArtifact> collector = new ElementCollector<IArtifact>();
		return collector.retrieveElementCollectionWithAttrIDs(this,"deploymentDescriptor", IArtifact.class);
	}
	
	public void removeDeploymentDescriptor( IArtifact artifact )
	{
		final IArtifact art = artifact;		
		new ElementConnector<IDeploymentSpecification>().removeByID
							   (
								 this,art,"deploymentDescriptor",
								 new IBackPointer<IDeploymentSpecification>() 
								 {
									public void execute(IDeploymentSpecification obj) 
									{
									   art.setContent(obj);
									}
								 }										
								);
	}
	
	public void addDeploymentDescriptor( IArtifact artifact )
	{
		final IArtifact art = artifact;		
		new ElementConnector<IDeploymentSpecification>().addChildAndConnect(
										this, true, "deploymentDescriptor", 
										"deploymentDescriptor", art,
										 new IBackPointer<IDeploymentSpecification>() 
										 {
											 public void execute(IDeploymentSpecification obj) 
											 {
												art.setContent(obj);
											 }
										 }
										 );
	}
	
	public INode getContainer()
	{
		ElementCollector<INode> collector =
										 new ElementCollector<INode>();
		return collector.retrieveSingleElementWithAttrID(this,"container", INode.class);
	}
	
	public void setContainer( INode newVal )
	{
		setElement(newVal,"container");
	}
	
	public String getDeploymentLocation()
	{
		return getAttributeValue("deploymentLocation");
	}
	
	public void setDeploymentLocation( String value )
	{
		setAttributeValue("deploymentLocation",value);
	}
	
	public String getExecutionLocation()
	{
		return getAttributeValue("executionLocation");
	}
	
	public void setExecutionLocation( String value )
	{
		setAttributeValue("executionLocation",value);
	}
	
	public ETList<IDeployment> getDeployments()
	{
		ElementCollector<IDeployment> collector = 
						new ElementCollector<IDeployment>();
		return collector.retrieveElementCollectionWithAttrIDs(this,"deployment", IDeployment.class);
	}
	
	public void removeDeployment( IDeployment deployment )
	{
		final IDeployment dep = deployment;		
		new ElementConnector<IDeploymentSpecification>().removeByID
							   (
								 this,dep,"deployment",
								 new IBackPointer<IDeploymentSpecification>() 
								 {
									public void execute(IDeploymentSpecification obj) 
									{
										dep.setSpecification(obj);
									}
								 }										
								);										
	}
	
	public void addDeployment( IDeployment deployment )
	{
		final IDeployment dep = deployment;		
		new ElementConnector<IDeploymentSpecification>().addChildAndConnect(
										this, true, "deployment", 
										"deployment", dep,
										 new IBackPointer<IDeploymentSpecification>() 
										 {
											 public void execute(IDeploymentSpecification obj) 
											 {
												dep.setSpecification(obj);
											 }
										 }
										 );		
	}
	
	public IComponent getConfiguredComponent()
	{
		ElementCollector<IComponent> collector =
										 new ElementCollector<IComponent>();
		return collector.retrieveSingleElementWithAttrID(this,"configuredComponent", IComponent.class);
	}

	public void setConfiguredComponent( IComponent component )
	{
		final IComponent comp = component;		
		new ElementConnector<IDeploymentSpecification>().addChildAndConnect(
										this, true, "configuredComponent", 
										"configuredComponent", comp,
										 new IBackPointer<IDeploymentSpecification>() 
										 {
											 public void execute(IDeploymentSpecification obj) 
											 {
												comp.addDeploymentSpecification(obj);
											 }
										 }
										 );
	}
	
	public IComponentAssembly getConfiguredAssembly()
	{
		ElementCollector<IComponentAssembly> collector =
										 new ElementCollector<IComponentAssembly>();
		return collector.retrieveSingleElementWithAttrID(this,"configuredAssembly", IComponentAssembly.class);
	}

	public void setConfiguredAssembly( IComponentAssembly component )
	{
		final IComponentAssembly comp = component;		
		new ElementConnector<IDeploymentSpecification>().addChildAndConnect(
										this, true, "configuredAssembly", 
										"configuredAssembly", comp,
										 new IBackPointer<IDeploymentSpecification>() 
										 {
											 public void execute(IDeploymentSpecification obj) 
											 {
												comp.addContent(obj);
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
		buildNodePresence("UML:DeploymentSpecification",doc,parent);
	}	
	
	

}



