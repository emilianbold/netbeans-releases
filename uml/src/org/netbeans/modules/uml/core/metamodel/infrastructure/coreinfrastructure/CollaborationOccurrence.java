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

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class CollaborationOccurrence extends TypedElement 
									 implements ICollaborationOccurrence 
{
	public CollaborationOccurrence()
	{
		super();
	}

	/**
	 * method AddRoleBinding
	*/
	public void addRoleBinding( IRoleBinding binding )
	{
		final IRoleBinding roleBinding = binding;		
		new ElementConnector<ICollaborationOccurrence>().addChildAndConnect(this, false, 
							"UML:CollaborationOccurrence.roleBinding", 
							"UML:CollaborationOccurrence.roleBinding", roleBinding,
							 new IBackPointer<ICollaborationOccurrence>() 
							 {
								 public void execute(ICollaborationOccurrence obj) 
								 {
									 roleBinding.setCollaboration(obj);
								 }
							 }										
							);		
	}

	/**
	 * method RemoveRoleBinding
	*/
	public void removeRoleBinding( IRoleBinding binding )
	{
		final IRoleBinding bind = binding;
		new ElementConnector<ICollaborationOccurrence>().removeElement(
										   this,bind,
										   "UML:CollaborationOccurrence.roleBinding/*",
										   new IBackPointer<ICollaborationOccurrence>() 
										   {
											  public void execute(ICollaborationOccurrence obj) 
											  {
												bind.setCollaboration(obj);
											  }
										  }
										);
	}

	/**
	 * property RoleBindings
	*/
	public ETList<IRoleBinding> getRoleBindings()
	{
		ElementCollector<IRoleBinding> collector = new ElementCollector<IRoleBinding>();
		return collector.retrieveElementCollection(m_Node, "UML:CollaborationOccurrence.roleBinding/*", IRoleBinding.class);				
	}

	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 */	
	public void establishNodePresence(Document doc, Node parent)
	{
		super.buildNodePresence("UML:CollaborationOccurrence",doc,parent);
	}	
}


