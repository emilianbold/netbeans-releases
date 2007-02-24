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
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.Dependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class RoleBinding extends Dependency implements IRoleBinding 
{
	public RoleBinding()
	{
		super();
	}
	
	/**
	 * property Collaboration
	*/
	public ICollaborationOccurrence getCollaboration()
	{
		ElementCollector<ICollaborationOccurrence> collector = new ElementCollector<ICollaborationOccurrence>();
		return collector.retrieveSingleElementWithAttrID(this,"collaboration", ICollaborationOccurrence.class);		
	}

	/**
	 * property Collaboration
	*/
	public void setCollaboration( ICollaborationOccurrence collab )
	{
		final ICollaborationOccurrence collaboration = collab;
		new ElementConnector<IRoleBinding>().setSingleElementAndConnect
						(
							this, collaboration, 
							"collaboration",
							 new IBackPointer<ICollaborationOccurrence>() 
							 {
								 public void execute(ICollaborationOccurrence obj) 
								 {
                                     obj.addRoleBinding(RoleBinding.this);
								 }
							 },
							 new IBackPointer<ICollaborationOccurrence>() 
							 {
								 public void execute(ICollaborationOccurrence obj) 
								 {
                                     obj.removeRoleBinding(RoleBinding.this);
								 }
							 }										
						);		
	}

	/**
	 * property Feature
	*/
	public IStructuralFeature getFeature()
	{
		ElementCollector<IStructuralFeature> collector = new ElementCollector<IStructuralFeature>();
		return collector.retrieveSingleElementWithAttrID(this,"feature", IStructuralFeature.class);			
	}

	/**
	 * property Feature
	*/
	public void setFeature( IStructuralFeature feature )
	{
		super.addElementByID(feature,"feature");
	}

	/**
	 * property Role
	*/
	public IStructuralFeature getRole()
	{
		ElementCollector<IStructuralFeature> collector = new ElementCollector<IStructuralFeature>();
		return collector.retrieveSingleElementWithAttrID(this,"role", IStructuralFeature.class);		
	}

	/**
	 * property Role
	*/
	public void setRole( IStructuralFeature feature )
	{
		super.addElementByID(feature,"role");
	}
	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 */	
	public void establishNodePresence(Document doc, Node parent)
	{
		buildNodePresence("UML:RoleBinding",doc,parent);
	}		
}


