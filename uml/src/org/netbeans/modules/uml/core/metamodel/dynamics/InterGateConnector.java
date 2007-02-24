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

import org.netbeans.modules.uml.core.metamodel.core.foundation.Element;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;


public class InterGateConnector extends Element implements IInterGateConnector
{
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInterGateConnector#getMessage()
     */
    public IMessage getMessage()
    {
        return new ElementCollector<IMessage>()
            .retrieveSingleElementWithAttrID( this, "message", IMessage.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInterGateConnector#setMessage(org.netbeans.modules.uml.core.metamodel.dynamics.IMessage)
     */
    public void setMessage(IMessage value)
    {
        setElement(value, "message");
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInterGateConnector#getEventOccurrence()
     */
    public IEventOccurrence getEventOccurrence()
    {
        return new ElementCollector<IEventOccurrence>()
            .retrieveSingleElementWithAttrID( this, "event", IEventOccurrence.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInterGateConnector#setEventOccurrence(org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence)
     */
    public void setEventOccurrence(final IEventOccurrence event)
    {
        new ElementConnector<IInterGateConnector>()
            .addChildAndConnect( 
                this, true, "event", "event",
                event,
                new IBackPointer<IInterGateConnector>()
                {
                    public void execute(IInterGateConnector iigc)
                    {
                        event.setConnection(iigc);
                    }
                } );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInterGateConnector#getFragment()
     */
    public IInteractionFragment getFragment()
    {
        return new ElementCollector<IInteractionFragment>()
            .retrieveSingleElementWithAttrID( this, "fragment", IInteractionFragment.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInterGateConnector#setFragment(org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment)
     */
    public void setFragment(IInteractionFragment frag)
    {
        new ElementConnector<IInterGateConnector>( )
            .setSingleElementAndConnect( 
                this, frag, "fragment",
                new IBackPointer<IInteractionFragment>( )
                {
                    public void execute(IInteractionFragment ifrag)
                    {
                        ifrag.addGateConnector(InterGateConnector.this);
                    }
                },
                new IBackPointer<IInteractionFragment>( )
                {
                    public void execute(IInteractionFragment ifrag)
                    {
                        ifrag.removeGateConnector(InterGateConnector.this);
                    }
                } );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInterGateConnector#getToGate()
     */
    public IGate getToGate()
    {
        return new ElementCollector<IGate>( )
            .retrieveSingleElementWithAttrID( this, "toGate", IGate.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInterGateConnector#setToGate(org.netbeans.modules.uml.core.metamodel.dynamics.IGate)
     */
    public void setToGate(final IGate value)
    {
        new ElementConnector<IInterGateConnector>( )
            .addChildAndConnect( 
                this, true, "toGate", "toGate", 
                value,
                new IBackPointer<IInterGateConnector>( )
                {
                    public void execute(IInterGateConnector iigc)
                    {
                        value.setToConnector(iigc);
                    }
                } );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInterGateConnector#getFromGate()
     */
    public IGate getFromGate()
    {
        return new ElementCollector<IGate>( )
            .retrieveSingleElementWithAttrID( this, "fromGate", IGate.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInterGateConnector#setFromGate(org.netbeans.modules.uml.core.metamodel.dynamics.IGate)
     */
    public void setFromGate(final IGate value)
    {
        new ElementConnector<IInterGateConnector>( )
            .addChildAndConnect( 
                this, true, "fromGate", "fromGate", 
                value,
                new IBackPointer<IInterGateConnector>( )
                {
                    public void execute(IInterGateConnector iigc)
                    {
                        value.setFromConnector(iigc);
                    }
                } );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#establishNodePresence(org.dom4j.Document, org.dom4j.Node)
     */
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:InterGateConnector", doc, node);
    }
}