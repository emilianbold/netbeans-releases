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


package org.netbeans.modules.uml.core.metamodel.dynamics;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.OwnerRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.infrastructure.Connector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IConnectableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IConnectorEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class MessageConnector extends Connector implements IMessageConnector
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessageConnector#getToLifeline()
     */
    public ILifeline getToLifeline()
    {
        return new ElementCollector<ILifeline>( )
            .retrieveSingleElementWithAttrID( this, "toLine", ILifeline.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessageConnector#setToLifeline(org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline)
     */
    public void setToLifeline(ILifeline line)
    {
        IConnectableElement conEl = (IConnectableElement) line.getRepresents();
        if (conEl != null)
        {
            IConnectorEnd end = new TypedFactoryRetriever<IConnectorEnd>()
                                    .createType("ConnectorEnd");
            end.setPart(conEl);
            setTo(end);
            setElement(line, "toLine");
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessageConnector#getFromLifeline()
     */
    public ILifeline getFromLifeline()
    {
        return new ElementCollector<ILifeline>( )
            .retrieveSingleElementWithAttrID( this, "fromLine", ILifeline.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessageConnector#setFromLifeline(org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline)
     */
    public void setFromLifeline(ILifeline line)
    {
        IConnectableElement conEl = (IConnectableElement) line.getRepresents();
        if (conEl != null)
        {
            IConnectorEnd end = new TypedFactoryRetriever<IConnectorEnd>()
                                    .createType("ConnectorEnd");
            end.setPart(conEl);
            setFrom(end);
            setElement(line, "fromLine");
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessageConnector#getConnectedLifelines()
     */
    public ETList<ILifeline> getConnectedLifelines()
    {
        ETList<ILifeline> lifelines = new ETArrayList<ILifeline>( 2 );
        ILifeline from =    getFromLifeline(), to = getToLifeline();
        if (to != null && from != null)
        {
            lifelines.add(to);
            lifelines.add(from);
        }
        return lifelines;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessageConnector#getMessages()
     */
    public ETList<IMessage> getMessages()
    {
        return new ElementCollector<IMessage>( )
            .retrieveElementCollectionWithAttrIDs( this, "message", IMessage.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessageConnector#removeMessage(org.netbeans.modules.uml.core.metamodel.dynamics.IMessage)
     */
    public void removeMessage(IMessage message)
    {
        removeElementByID( message, "message" );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessageConnector#addMessage(org.netbeans.modules.uml.core.metamodel.dynamics.IMessage)
     */
    public void addMessage(IMessage message)
    {
        addElementByID( message, "message" );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IMessageConnector#addMessage(int, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation)
     */
    public IMessage addMessage(int nDirectionKind, IOperation pOper)
    {
		IInteraction owner = OwnerRetriever.getOwnerByType(this, IInteraction.class);
        IMessage message = null;
        if (owner != null)
        {
            IDynamicsRelationFactory factory = new DynamicsRelationFactory();
            ILifeline fromLine = getFromLifeline(),
                      toLine   = getToLifeline();
            if (fromLine != null && toLine != null)
            {
                if (nDirectionKind == IMessage.MDK_FROM_TO)
                    message = factory.createMessage(fromLine, owner, toLine, 
                            owner, pOper, BaseElement.MK_SYNCHRONOUS);
                else
                    message = factory.createMessage(toLine, owner, fromLine, 
                            owner, pOper, BaseElement.MK_SYNCHRONOUS);
            }
        }
        return message;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.Connector#establishNodePresence(org.dom4j.Document, org.dom4j.Node)
     */
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:MessageConnector", doc, node);
    }
}