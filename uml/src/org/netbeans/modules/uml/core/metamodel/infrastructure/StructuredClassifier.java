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

package org.netbeans.modules.uml.core.metamodel.infrastructure;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Classifier;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class StructuredClassifier extends Classifier 
								  implements IStructuredClassifier
{

	/**
	 * 
	 */
	public StructuredClassifier() 
	{
		super();		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.IStructuredClassifier#addRole(org.netbeans.modules.uml.core.metamodel.infrastructure.IConnectableElement)
	 */
	public void addRole(IConnectableElement element) 
	{
		final IConnectableElement elem = element;
		new ElementConnector<IStructuredClassifier>().addChildAndConnect(
											this, true, "role", 
											"role", elem,
											 new IBackPointer<IStructuredClassifier>() 
											 {
												 public void execute(IStructuredClassifier obj) 
												 {
													elem.addRoleContext(obj);
												 }
											 }										
											);	
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.IStructuredClassifier#removeRole(org.netbeans.modules.uml.core.metamodel.infrastructure.IConnectableElement)
	 */
	public void removeRole(IConnectableElement element)
	{
		final IConnectableElement elem = element;
		new ElementConnector<IStructuredClassifier>().removeByID
							   (
								 this,element,"role",
								 new IBackPointer<IStructuredClassifier>() 
								 {
								 	public void execute(IStructuredClassifier obj) 
									{
									   elem.removeRoleContext(obj);
							 		}
								 }										
							    );
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.IStructuredClassifier#getRoles()
	 */
	public ETList<IConnectableElement> getRoles() 
	{
		ElementCollector<IConnectableElement> collector = new ElementCollector<IConnectableElement>();
		return collector.retrieveElementCollectionWithAttrIDs(this,"role", IConnectableElement.class);	
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.IStructuredClassifier#addPart(org.netbeans.modules.uml.core.metamodel.infrastructure.IPart)
	 */
	public void addPart(IPart part) 
	{
		super.addOwnedElement(part);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.IStructuredClassifier#removePart(org.netbeans.modules.uml.core.metamodel.infrastructure.IPart)
	 */
	public void removePart(IPart part) 
	{
		super.removeElement(part);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.IStructuredClassifier#getParts()
	 */
	public ETList<IPart> getParts() 
	{
		ElementCollector<IPart> coll = new ElementCollector<IPart>();
		return coll.retrieveElementCollection(
                this,
		        "UML:Element.ownedElement/*" +
                "[ not( name(.) = \"UML:Connector\" )]",
                IPart.class);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.IStructuredClassifier#addConnector(org.netbeans.modules.uml.core.metamodel.infrastructure.IConnector)
	 */
	public void addConnector(IConnector connector) 
	{
		super.addElement(connector);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.IStructuredClassifier#removeConnector(org.netbeans.modules.uml.core.metamodel.infrastructure.IConnector)
	 */
	public void removeConnector(IConnector connector) 
	{
		super.removeElement(connector);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.IStructuredClassifier#getConnectors()
	 */
	public ETList<IConnector> getConnectors() 
	{
		ElementCollector<IConnector> coll = new ElementCollector<IConnector>();
		return coll.retrieveElementCollection
											((IElement)this,"UML:Element.ownedElement/UML:Connector", IConnector.class);
	}

}


