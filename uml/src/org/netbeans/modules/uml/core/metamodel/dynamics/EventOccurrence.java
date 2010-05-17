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
