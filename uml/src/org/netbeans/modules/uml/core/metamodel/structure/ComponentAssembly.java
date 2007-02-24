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

import org.netbeans.modules.uml.core.metamodel.core.foundation.AutonomousElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public class ComponentAssembly extends AutonomousElement 
							   implements IComponentAssembly  
{
	public ETList<IDeploymentSpecification> getContents()
	{
		ElementCollector<IDeploymentSpecification> collector = 
									new ElementCollector<IDeploymentSpecification>();
		return collector.retrieveElementCollectionWithAttrIDs(this,"content", IDeploymentSpecification.class);
	}

	public void removeContent( IDeploymentSpecification pSpec )
	{
		final IDeploymentSpecification spec = pSpec;		
		new ElementConnector<IComponentAssembly>().removeByID
							   (
								 this,spec,"content",
								 new IBackPointer<IComponentAssembly>() 
								 {
									public void execute(IComponentAssembly obj) 
									{
									   spec.setConfiguredAssembly(obj);
									}
								 }										
								);
	}
	
	public void addContent( IDeploymentSpecification pSpec )
	{
		final IDeploymentSpecification spec = pSpec;
		new ElementConnector<IComponentAssembly>().addChildAndConnect(
										this, true, "content", 
										"content", spec,
										 new IBackPointer<IComponentAssembly>() 
										 {
											 public void execute(IComponentAssembly obj) 
											 {
												spec.setConfiguredAssembly(obj);
											 }
										 }
										);
	}
	
	public ETList<IComponent> getComponents()
	{
		ElementCollector<IComponent> collector = 
									new ElementCollector<IComponent>();
		return collector.retrieveElementCollectionWithAttrIDs(this,"assembledComponent", IComponent.class);
	}
	
	public void removeComponent( IComponent comp )
	{
		final IComponent component = comp;		
		new ElementConnector<IComponentAssembly>().removeByID
							   (
								 this,component,"assembledComponent",
								 new IBackPointer<IComponentAssembly>() 
								 {
									public void execute(IComponentAssembly obj) 
									{
									   component.removeAssembly(obj);
									}
								 }										
								);
	}
	
	public void addComponent( IComponent comp )
	{
		final IComponent component = comp;
		new ElementConnector<IComponentAssembly>().addChildAndConnect(
										this, true, "assembledComponent", 
										"assembledComponent", component,
										 new IBackPointer<IComponentAssembly>() 
										 {
											 public void execute(IComponentAssembly obj) 
											 {
												component.addAssembly(obj);
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
		buildNodePresence("UML:ComponentAssembly",doc,parent);
	}	
}


