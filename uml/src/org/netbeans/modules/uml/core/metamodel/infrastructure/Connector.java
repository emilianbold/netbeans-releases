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

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.DirectedRelationship;
import org.netbeans.modules.uml.core.metamodel.core.foundation.Element;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDirectedRelationship;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.RelationshipEventsHelper;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.CollectionTranslator;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.StructuralFeature;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class Connector extends StructuralFeature
					   implements IConnector, IDirectedRelationship
{
	IDirectedRelationship m_DirectedRealationAggregate = new DirectedRelationship();
	
	public void addBehavior( IBehavior behavior )
	{
		addElementByID(behavior, "contract");
	}

	public void removeBehavior( IBehavior behavior )
	{
		removeElementByID(behavior,"contract");
	}
	
	public ETList<IBehavior> getBehaviors()
	{
		ElementCollector<IBehavior> collector = new ElementCollector<IBehavior>();
		return collector.retrieveElementCollectionWithAttrIDs(this,"contract", IBehavior.class);			
	}
	
	public void addEnd( IConnectorEnd end )
	{
		RelationshipEventsHelper help = new RelationshipEventsHelper(this);
		if ( help.firePreEndAdd(end,null) )
		{
			addElement(end);
			help.fireEndAdded();
		}
		else
		{
			//throw exception.
		}
	}
	
	public void removeEnd( IConnectorEnd end )
	{
		RelationshipEventsHelper help = new RelationshipEventsHelper(this);
		if ( help.firePreEndRemoved(end,null) )
		{
			removeElement(end);
			help.fireEndRemoved();
		}
		else
		{
			//throw exception.
		}
	}
	
	public ETList<IConnectorEnd> getEnds()
	{
		ETList<IElement> elements = getElements();
		ETList<IElement> elems = new ETArrayList<IElement>();
		for (int i=0;i<elements.size();i++)
		{
			elems.add(i,elements.get(i));
		}
		CollectionTranslator<IElement, IConnectorEnd> trans = new 
							CollectionTranslator<IElement, IConnectorEnd>();
		return trans.copyCollection(elems);		
	}
	
	public IConnectorEnd getFrom()
	{
		ElementCollector<IConnectorEnd> collector = new ElementCollector<IConnectorEnd>();
		return collector.retrieveSingleElement(this,"UML:Element.ownedElement/UML:ConnectorEnd", IConnectorEnd.class);	
	}
	
	public void setFrom( IConnectorEnd end )
	{
		addEnd(end);
	}
	
	public IConnectorEnd getTo()
	{
		ElementCollector<IConnectorEnd> collector = new ElementCollector<IConnectorEnd>();
		return collector.retrieveSingleElement(this,"UML:Element.ownedElement/UML:ConnectorEnd[2]", IConnectorEnd.class);
	}
	
	public void setTo( IConnectorEnd end )
	{
		addEnd(end);
	}

	// IDirectedRelationship methods
	public void addTarget(IElement elem)
	{
		m_DirectedRealationAggregate.addTarget(elem);
	}
	
	public void removeTarget(IElement elem)
	{
		m_DirectedRealationAggregate.removeTarget(elem);
	}
	
	public ETList<IElement> getTargets()
	{
		return m_DirectedRealationAggregate.getTargets();
	}
	
	public void addSource(IElement elem)
	{
		m_DirectedRealationAggregate.addSource(elem);
	}
	
	public void removeSource(IElement elem)
	{
		m_DirectedRealationAggregate.removeSource(elem);
	}
	
	public ETList<IElement> getSources()
	{
		return m_DirectedRealationAggregate.getSources();
	}
	
	public long getTargetCount()
	{
		return m_DirectedRealationAggregate.getTargetCount();
	}
	
	public long getSourceCount()
	{
		return m_DirectedRealationAggregate.getSourceCount();
	}
	
	//IRelationship method
	public ETList<IElement> getRelatedElements()
	{
		return m_DirectedRealationAggregate.getRelatedElements();
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement#setNode(org.dom4j.Node)
	 */
	public void setNode(Node n)
	{
		super.setNode(n);
		m_DirectedRealationAggregate.setNode(n);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#establishNodePresence(org.dom4j.Document, org.dom4j.Node)
	 */
	public void establishNodePresence(Document doc, Node node)
	{
		super.establishNodePresence(doc, node);
		buildNodePresence("UML:Connector", doc, node);		
	}
}
