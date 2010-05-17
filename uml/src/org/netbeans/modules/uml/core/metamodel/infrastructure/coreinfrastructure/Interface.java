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


package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateMachine;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class Interface extends Classifier implements IInterface 
{
    public Interface()
    {
        super();
    }

	/**
	 * method AddReception
	*/
	public void addReception( IReception rec )
	{
		addFeature(rec);
	}

	/**
	 * method RemoveReception
	*/
	public void removeReception( IReception rec )
	{
		removeFeature(rec);
	}

	/**
	 * property Receptions
	*/
	public ETList<IReception> getReceptions()
	{
		ElementCollector<IReception> collector = new ElementCollector<IReception>();
		return collector.retrieveElementCollection(m_Node,
             "UML:Element.ownedElement/UML:Reception", IReception.class);
	}

	/**
	 * property ProtocolStateMachine
	*/
	public INamespace getProtocolStateMachine()
	{
		INamespace retSpace = null;
		ElementCollector<IStateMachine> collector = new ElementCollector<IStateMachine>();
		IStateMachine machine = collector.retrieveSingleElement(m_Node,"UML:Interface.protocolStateMachine/*", IStateMachine.class);
		if (machine != null)
			retSpace = (INamespace)machine;
		return retSpace;			
	}

	/**
	 * property ProtocolStateMachine
	*/
	public void setProtocolStateMachine( INamespace spcObj )
	{
		super.addChild("UML:Interface.protocolStateMachine",
						"UML:Interface.protocolStateMachine/UML:StateMachine",spcObj);
	}
	
	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 */	
    @Override
	public void establishNodePresence(Document doc, Node parent)
	{
		super.buildNodePresence("UML:Interface",doc,parent);
	}
	
    @Override
	public void establishNodeAttributes(Element node)
	{
		super.establishNodeAttributes(node);
		XMLManip.setAttributeValue(node,"isAbstract","true");
	}
	
	/**	 
	 * This routine is overloaded so that we may ensure that the
	 * correct stereotype is set on the interface after it has
	 * been created and added to a project.
	 */
    @Override
	public void setOwner(IElement owner)
	{
		super.setOwner(owner);
		// Make sure that we have an "interface" stereotype
		super.ensureStereotype("interface");
	}
	
	/**	 
	 * This routine is overloaded here so that certain stereotypes
	 * are removed before a transform is done.
	 */
    @Override
	public void preTransformNode(String typeName)
	{
            super.deleteStereotype("interface"); //Jyothi: Fix for Bug#6301700
	}
	
	public ETList<String> getCollidingNamesForElement(INamedElement ele)
	{
		ETList<String> values = new ETArrayList<String>();
		values.add("UML:Class");
		values.add("UML:Interface");
		values.add("UML:Enumeration");
		
		return values;
	}
}


