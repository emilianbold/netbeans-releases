/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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



