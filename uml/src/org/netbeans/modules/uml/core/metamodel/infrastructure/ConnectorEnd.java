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

import org.netbeans.modules.uml.core.metamodel.core.foundation.Element;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;


public class ConnectorEnd extends Element implements IConnectorEnd
{
	public IMultiplicity getMultiplicity()
	{		
		ElementCollector<IMultiplicity> collector = new ElementCollector<IMultiplicity>();
		IMultiplicity mult = collector.retrieveSingleElement(m_Node,"UML:ConnectorEnd.multiplicity/*", IMultiplicity.class);
		if (mult == null)
		{
			//We currently don't have a Multiplicity, so let's create a new Multiplicity
			// and return it.
			TypedFactoryRetriever<IMultiplicity> retriever = 
											new TypedFactoryRetriever<IMultiplicity>();
			mult = retriever.createType("Multiplicity");
			//Add the child directly, so we don't cause any new events
			addMultiplicity(mult);
		}
		return mult;
	}
	
	public void setMultiplicity( IMultiplicity multiplicity )
	{
		addMultiplicity(multiplicity);
	}
	
	public IConnectableElement getPart()
	{
		ElementCollector<IConnectableElement> collector = new ElementCollector<IConnectableElement>();
		return collector.retrieveSingleElementWithAttrID(this,"part", IConnectableElement.class);	
	}
	
	public void setPart( IConnectableElement part )
	{
		final IConnectableElement connectElem = part;
		new ElementConnector<IConnectorEnd>().setSingleElementAndConnect
						(
							this, connectElem, 
							"part",
							 new IBackPointer<IConnectableElement>() 
							 {
								 public void execute(IConnectableElement obj) 
								 {
									obj.addEnd(ConnectorEnd.this);
								 }
							 },
							 new IBackPointer<IConnectableElement>() 
							 {
								 public void execute(IConnectableElement obj) 
								 {
									obj.removeEnd(ConnectorEnd.this);
								 }
							 }										
						);
	}
	
	public IPort getPort()
	{
		ElementCollector<IPort> collector = new ElementCollector<IPort>();
		return collector.retrieveSingleElementWithAttrID(this,"port", IPort.class);	
	}
	
	public void setPort( IPort portVal )
	{
		final IPort port = portVal;
		new ElementConnector<IConnectorEnd>().setSingleElementAndConnect
						(
							this, port, 
							"port",
							 new IBackPointer<IPort>() 
							 {
								 public void execute(IPort obj) 
								 {
									obj.addEnd(ConnectorEnd.this);
								 }
							 },
							 new IBackPointer<IPort>() 
							 {
								 public void execute(IPort obj) 
								 {
									obj.removeEnd(ConnectorEnd.this);
								 }
							 }										
						);
	}
	
	public int getInitialCardinality()
	{
		return super.getAttributeValueInt("initialCardinality");
	}
	
	public void setInitialCardinality( int newValue )
	{
		super.setAttributeValue("initialCardinality",newValue);
	}
	
	public IConnector getConnector()
	{
		ElementCollector<IConnector> collector = new ElementCollector<IConnector>();
		return collector.retrieveSingleElementWithAttrID(this,"connector", IConnector.class);	
	}
	
	public void setConnector( IConnector newValue )
	{
		final IConnector connector = newValue;
		new ElementConnector<IConnectorEnd>().setSingleElementAndConnect
						(
							this, connector, 
							"connector",
							 new IBackPointer<IConnector>() 
							 {
								 public void execute(IConnector obj) 
								 {
									obj.addEnd(ConnectorEnd.this);
								 }
							 },
							 new IBackPointer<IConnector>() 
							 {
								 public void execute(IConnector obj) 
								 {
									obj.removeEnd(ConnectorEnd.this);
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
		super.buildNodePresence("UML:ConnectorEnd",doc,parent);
	}
	
	protected void addMultiplicity(IMultiplicity mult)
	{
		super.addChild("UML:ConnectorEnd.multiplicity",
					   "UML:ConnectorEnd.multiplicity/UML:Multiplicity",
					   mult);
	}
	public IAssociationEnd getDefiningEnd()
	{
		return null;
	}
	
	public void setDefiningEnd( IAssociationEnd value )
	{
	}

    public String getRangeAsString()
    {
        return getMultiplicity().getRangeAsString();
    }
}
