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
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IProtocolStateMachine;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Feature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class Port extends Feature implements IPort, IConnectableElement
{
    IConnectableElement m_ConnectElementAggregate = new ConnectableElement();
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement#setNode(org.dom4j.Node)
     */
    public void setNode(Node n)
    {
        super.setNode(n);
        m_ConnectElementAggregate.setNode(n);
    }
    
    public ETList<IConnectorEnd> getEnds()
    {
        ElementCollector<IConnectorEnd> collector = new ElementCollector<IConnectorEnd>();
        return collector.retrieveElementCollectionWithAttrIDs(this,"portEnd", IConnectorEnd.class);
    }
    
    public void removeEnd( IConnectorEnd end )
    {
        final IConnectorEnd connectorEnd = end;
        new ElementConnector<IPort>().removeByID
                (
                this,connectorEnd,"portEnd",
                new IBackPointer<IPort>()
        {
            public void execute(IPort obj)
            {
                connectorEnd.setPort(obj);
            }
        }
        );
    }
    
    public void addEnd( IConnectorEnd end )
    {
        final IConnectorEnd connectorEnd = end;
        new ElementConnector<IPort>().addChildAndConnect(
                this, true, "portEnd",
                "portEnd", connectorEnd,
                new IBackPointer<IPort>()
        {
            public void execute(IPort obj)
            {
                connectorEnd.setPort(obj);
            }
        }
        );
    }
    
    public IProtocolStateMachine getProtocol()
    {
        ElementCollector<IProtocolStateMachine> collector = new ElementCollector<IProtocolStateMachine>();
        return collector.retrieveSingleElementWithAttrID(this,"protocol", IProtocolStateMachine.class);
    }
    
    public void setProtocol( IProtocolStateMachine value )
    {
        super.addElementByID(value,"protocol");
    }
    
    public boolean getIsService()
    {
        return super.getBooleanAttributeValue("isService",false);
    }
    
    public void setIsService( boolean value )
    {
        super.setBooleanAttributeValue("isService",value);
    }
    
    public boolean getIsSignal()
    {
        return super.getBooleanAttributeValue("isSignal",false);
    }
    
    public void setIsSignal( boolean value )
    {
        super.setBooleanAttributeValue("isSignal",value);
    }
    
    public void addRequiredInterface( IInterface pInter )
    {
        boolean isProvided = false;
        isProvided = getIsProvidedInterface(pInter);
        if (!isProvided)
        {
            super.addElementByID(pInter, "required");
        }
    }
    
    public void removeRequiredInterface( IInterface end )
    {
        super.removeElementByID(end,"required");
    }
    
    public ETList<IInterface> getRequiredInterfaces()
    {
        ElementCollector<IInterface> collector = new ElementCollector<IInterface>();
        return collector.retrieveElementCollectionWithAttrIDs(this,"required", IInterface.class);
    }
    /**
     * Checks the list of required interfaces to see if pInter is in the list.
     *
     * @param pInter [in] The interface to look for
     */
    public boolean getIsRequiredInterface( IInterface pInter )
    {
        boolean isRequired = false;
        ETList<IInterface> interfaces = getRequiredInterfaces();
        if (interfaces != null)
        {
            isRequired = interfaces.contains(pInter);
        }
        return isRequired;
    }
    
    public void addProvidedInterface( IInterface pInter )
    {
        boolean isRequired = false;
        isRequired = getIsRequiredInterface(pInter);
        if (!isRequired)
        {
            super.addElementByID(pInter, "provided");
        }
    }
    
    public void removeProvidedInterface( IInterface end )
    {
        super.removeElementByID(end,"provided");
    }
    
    public ETList<IInterface> getProvidedInterfaces()
    {
        ElementCollector<IInterface> collector = new ElementCollector<IInterface>();
        return collector.retrieveElementCollectionWithAttrIDs(this,"provided", IInterface.class);
    }
    
    public boolean getIsProvidedInterface( IInterface pInter )
    {
        boolean isProvided = false;
        ETList<IInterface> interfaces = getProvidedInterfaces();
        if (interfaces != null)
        {
            isProvided = interfaces.contains(pInter);
        }
        return isProvided;
    }
    
    /**
     * Establishes the appropriate XML elements for this UML type.
     *
     * [in] The document where this element will reside
     * [in] The element's parent node.
     */
    public void establishNodePresence(Document doc, Node parent)
    {
        super.buildNodePresence("UML:Port",doc,parent);
    }
    
    
    /**
     * method AddRoleContext
     */
    public void addRoleContext( IStructuredClassifier pClassifier )
    {
        m_ConnectElementAggregate.addRoleContext(pClassifier);
    }
    
    /**
     * method RemoveRoleContext
     */
    public void removeRoleContext( IStructuredClassifier pClassifier )
    {
        m_ConnectElementAggregate.removeRoleContext(pClassifier);
    }
    
    /**
     * property RoleContexts
     */
    public ETList<IStructuredClassifier> getRoleContexts()
    {
        return m_ConnectElementAggregate.getRoleContexts();
    }
    
    public  void delete()
    {
        ETList<IInterface> list = getRequiredInterfaces();
        for (IInterface intface: list)
        {
            ETList<IDependency> dep = intface.getSupplierDependencies();
            for (IDependency d: dep)
            {
                d.delete();
            }
        }
        super.delete();
    }
}

