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

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IEvent;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public class EventOccurrence extends NamedElement implements IEventOccurrence
{
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence#getConnection()
     */
    public IInterGateConnector getConnection()
    {
        return new ElementCollector<IInterGateConnector>()
            .retrieveSingleElementWithAttrID( this, "connection", IInterGateConnector.class);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence#setConnection(org.netbeans.modules.uml.core.metamodel.dynamics.IInterGateConnector)
     */
    public void setConnection(IInterGateConnector value)
    {
        setElement(value, "connection");
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence#getLifeline()
     */
    public ILifeline getLifeline()
    {
        return new ElementCollector<ILifeline>()
            .retrieveSingleElementWithAttrID( this, "lifeline", ILifeline.class);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence#setLifeline(org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline)
     */
    public void setLifeline(ILifeline value)
    {
        new ElementConnector<IEventOccurrence>()
            .setSingleElementAndConnect(
                this, value, "lifeline", 
                new IBackPointer<ILifeline>() 
                {
                    public void execute(ILifeline life) 
                    {
                        life.addEvent(EventOccurrence.this);
                    }
                },
                new IBackPointer<ILifeline>() 
                {
                    public void execute(ILifeline life) 
                    {
                        life.removeEvent(EventOccurrence.this);
                    }
                }
                );
    }
    
    public IEvent getEventType()
    {
        return new ElementCollector<IEvent>()
            .retrieveSingleElementWithAttrID( this, "eventType", IEvent.class);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence#setEventType(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IEvent)
     */
    public void setEventType(IEvent value)
    {
        setElement(value, "eventType");
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence#getInteraction()
     */
    public IInteraction getInteraction()
    {
        INamespace space = getNamespace();
        return space instanceof IInteraction? (IInteraction) space : null;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence#getReceiveMessage()
     */
    public IMessage getReceiveMessage()
    {
        return new ElementCollector<IMessage>()
            .retrieveSingleElementWithAttrID( this, "receiveMessage", IMessage.class);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence#setReceiveMessage(org.netbeans.modules.uml.core.metamodel.dynamics.IMessage)
     */
    public void setReceiveMessage(final IMessage message)
    {
        new ElementConnector<IEventOccurrence>().
            addChildAndConnect(this, true, "receiveMessage", "receiveMessage",
            message, new IBackPointer<IEventOccurrence>() 
            {
                public void execute(IEventOccurrence eo) 
                {
                    message.setReceiveEvent(EventOccurrence.this);
                }
            });
    }
    
    public IMessage getSendMessage()
    {
        return new ElementCollector<IMessage>()
            .retrieveSingleElementWithAttrID( this, "sendMessage", IMessage.class);
    }

    public void setSendMessage(final IMessage message)
    {
        new ElementConnector<IEventOccurrence>().
            addChildAndConnect(this, true, "sendMessage", "sendMessage",
            message, new IBackPointer<IEventOccurrence>() 
            {
                public void execute(IEventOccurrence eo) 
                {
                    message.setSendEvent(EventOccurrence.this);
                }
            });
    }
    
    public IExecutionOccurrence getFinishExec()
    {
        return new ElementCollector<IExecutionOccurrence>()
            .retrieveSingleElementWithAttrID(this, "finishExec", IExecutionOccurrence.class);
    }
    
    public void setFinishExec(final IExecutionOccurrence exec)
    {
        new ElementConnector<IEventOccurrence>()
            .addChildAndConnect( this, true, "finishExec", "finishExec", 
                exec, new IBackPointer<IEventOccurrence>() 
        {
            public void execute(IEventOccurrence eo) 
            {
                exec.setFinish(EventOccurrence.this);
            }
        });
    }
    
    public IExecutionOccurrence getStartExec()
    {
        return new ElementCollector<IExecutionOccurrence>()
            .retrieveSingleElementWithAttrID(this, "startExec", IExecutionOccurrence.class);
    }
    
    public void setStartExec(final IExecutionOccurrence exec)
    {
        new ElementConnector<IEventOccurrence>()
            .addChildAndConnect( this, true, "startExec", "startExec", 
                exec, new IBackPointer<IEventOccurrence>() 
        {
            public void execute(IEventOccurrence eo) 
            {
                exec.setStart(EventOccurrence.this);
            }
        });
    }
    
    public ETList<IGeneralOrdering> getBeforeOrderings()
    {
        return new ElementCollector<IGeneralOrdering>()
            .retrieveElementCollectionWithAttrIDs( 
                this, "toBefore", IGeneralOrdering.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence#addBeforeOrdering(org.netbeans.modules.uml.core.metamodel.dynamics.IGeneralOrdering)
     */
    public void addBeforeOrdering(final IGeneralOrdering order)
    {
        new ElementConnector<IEventOccurrence>()
            .addChildAndConnect( this, true, "toBefore", "toBefore", 
                order, 
                new IBackPointer<IEventOccurrence>() 
                {
                    public void execute(IEventOccurrence eo)
                    {
                        order.setBefore(EventOccurrence.this);
                    }
                } );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence#removeBeforeOrdering(org.netbeans.modules.uml.core.metamodel.dynamics.IGeneralOrdering)
     */
    public void removeBeforeOrdering(final IGeneralOrdering order)
    {
        new ElementConnector<IEventOccurrence>()
            .removeByID( this, order, "toBefore", 
                new IBackPointer<IEventOccurrence>() 
                {
                    public void execute(IEventOccurrence eo) 
                    {
                        order.setBefore(null);
                    }
                } );
    }
    
    public ETList<IGeneralOrdering> getAfterOrderings()
    {
        return new ElementCollector<IGeneralOrdering>()
            .retrieveElementCollectionWithAttrIDs( 
                this, "toAfter", IGeneralOrdering.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence#removeAfterOrdering(org.netbeans.modules.uml.core.metamodel.dynamics.IGeneralOrdering)
     */
    public void removeAfterOrdering(final IGeneralOrdering order)
    {
        new ElementConnector<IEventOccurrence>()
            .removeByID( this, order, "toAfter", 
                new IBackPointer<IEventOccurrence>() 
                {
                    public void execute(IEventOccurrence eo) 
                    {
                        order.setAfter(null);
                    }
                } );
        
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence#addAfterOrdering(org.netbeans.modules.uml.core.metamodel.dynamics.IGeneralOrdering)
     */
    public void addAfterOrdering(final IGeneralOrdering order)
    {
        new ElementConnector<IEventOccurrence>()
            .addChildAndConnect( this, true, "toAfter", "toAfter", 
                order, 
                new IBackPointer<IEventOccurrence>() 
                {
                    public void execute(IEventOccurrence eo)
                    {
                        order.setAfter(eo);
                    }
                } );
    }
    
    public void establishNodePresence(Document doc, Node parent)
    {
        buildNodePresence("UML:EventOccurrence", doc, parent);
    }
}