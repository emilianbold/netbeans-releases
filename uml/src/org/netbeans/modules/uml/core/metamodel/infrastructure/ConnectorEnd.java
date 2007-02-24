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