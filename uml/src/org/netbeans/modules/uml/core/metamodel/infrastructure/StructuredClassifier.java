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


