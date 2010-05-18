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


