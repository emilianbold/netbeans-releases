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

import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IConnector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Behavior;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ICollaborationOccurrence;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IDerivation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IImplementation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IIncrement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.support.umlsupport.INamedCollection;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class Interaction extends Behavior implements IInteraction
{
    private IInteractionOperand m_InteractionOperand = new InteractionOperand();
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction#getGates()
     */
    public ETList<IGate> getGates()
    {
        return new ElementCollector< IGate >()
            .retrieveElementCollection( 
                m_Node, 
                "UML:Element.ownedElement/UML:Gate", IGate.class); 
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction#addGate(org.netbeans.modules.uml.core.metamodel.dynamics.IGate)
     */
    public void addGate(IGate gate)
    {
        addOwnedElement(gate);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction#removeGate(org.netbeans.modules.uml.core.metamodel.dynamics.IGate)
     */
    public void removeGate(IGate gate)
    {
        removeOwnedElement(gate);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction#getConnectors()
     */
    public ETList<IConnector> getConnectors()
    {
        return new ElementCollector< IConnector >( )
            .retrieveElementCollection( 
                m_Node, 
                "UML:Element.ownedElement/*[ not( name(.) = 'UML:Gate' )]", IConnector.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction#addConnector(org.netbeans.modules.uml.core.metamodel.infrastructure.IConnector)
     */
    public void addConnector(IConnector connector)
    {
        addElement(connector);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction#removeConnector(org.netbeans.modules.uml.core.metamodel.infrastructure.IConnector)
     */
    public void removeConnector(IConnector connector)
    {
        removeElement(connector);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction#removeMessage(org.netbeans.modules.uml.core.metamodel.dynamics.IMessage)
     */
    public void removeMessage(final IMessage message)
    {
        new ElementConnector<IInteraction>( )
            .removeElement( 
                this, message, 
                "UML:Interaction.message/*",
                new IBackPointer<IInteraction>( )
                {
                    public void execute(IInteraction inter)
                    {
                        message.setInteraction(inter);
                    }
                } );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction#addMessage(org.netbeans.modules.uml.core.metamodel.dynamics.IMessage)
     */
    public void addMessage(final IMessage message)
    {
        new ElementConnector<IInteraction>( )
            .addChildAndConnect( 
                this, false, "UML:Interaction.message", 
                "UML:Interaction.message", message,
                new IBackPointer<IInteraction>( )
                {
                    public void execute(IInteraction interact)
                    {
                        message.setInteraction(interact);
                    }
                } );
    }
    
    public ETList<IMessage> getMessages()
    {
        return new ElementCollector<IMessage>()
            .retrieveElementCollection( m_Node, "UML:Interaction.message/*", IMessage.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction#addLifeline(org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline)
     */
    public void addLifeline(ILifeline line)
    {
        addElement(line);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction#removeLifeline(org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline)
     */
    public void removeLifeline(ILifeline line)
    {
        removeElement(line);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction#getLifelines()
     */
    public ETList<ILifeline> getLifelines()
    {
        return new ElementCollector<ILifeline>( )
            .retrieveElementCollection( 
                m_Node,
                "UML:Element.ownedElement/UML:Lifeline", ILifeline.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction#addEventOccurrence(org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence)
     */
    public void addEventOccurrence(IEventOccurrence pOcc)
    {
        addOwnedElement(pOcc);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction#removeEventOccurrence(org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence)
     */
    public void removeEventOccurrence(IEventOccurrence pOcc)
    {
        removeOwnedElement(pOcc);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction#getEventOccurrences()
     */
    public ETList<IEventOccurrence> getEventOccurrences()
    {
        return new ElementCollector<IEventOccurrence>( )
            .retrieveElementCollection( 
                this, 
                "UML:Element.ownedElement/UML:EventOccurrence", IEventOccurrence.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction#createMessage(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, int)
     */
    public IMessage createMessage(IElement toElement,
            IInteractionFragment toOwner, IOperation oper, int kind)
    {
        IMessage message = new DynamicsRelationFactory()
            .createMessage(this, null, toElement, toOwner, oper, kind);
        return message;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction#insertMessage(org.netbeans.modules.uml.core.metamodel.dynamics.IMessage, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, int)
     */
    public IMessage insertMessage(IMessage fromBeforeMessage,
            IElement toElement, IInteractionFragment toOwner, IOperation oper,
            int kind)
    {
        IMessage message = 
            new DynamicsRelationFactory()
                .insertMessage(
                    fromBeforeMessage, this, null, 
                    toElement, toOwner, oper, kind );
        
        return message;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction#insertMessageBefore(org.netbeans.modules.uml.core.metamodel.dynamics.IMessage, org.netbeans.modules.uml.core.metamodel.dynamics.IMessage)
     */
    public void insertMessageBefore(final IMessage message, 
                                    IMessage messageBefore)
    {
        new ElementConnector<IInteraction>( )
            .insertChildBeforeAndConnect( 
                this, false, 
                "UML:Interaction.message", 
                "UML:Interaction.message", 
                message, 
                messageBefore, 
                new IBackPointer<IInteraction>( )
                {
                    public void execute(IInteraction inter)
                    {
                        message.setInteraction(inter);
                    }
                } );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction#handleMessageAdded(org.netbeans.modules.uml.core.metamodel.dynamics.IMessage, org.netbeans.modules.uml.core.metamodel.dynamics.IMessage)
     */
    public void handleMessageAdded(IMessage message, IMessage messageBefore)
    {
        // C++ code doesn't seem to do anything.
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction#handleMessageDeleted(org.netbeans.modules.uml.core.metamodel.dynamics.IMessage)
     */
    public void handleMessageDeleted(IMessage message)
    {
        // C++ code does nothing
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction#getGeneralOrderings()
     */
    public ETList<IGeneralOrdering> getGeneralOrderings()
    {
        return new ElementCollector<IGeneralOrdering>( )
            .retrieveElementCollection( 
                this, 
                "UML:Element.ownedElement/UML:GeneralOrdering", IGeneralOrdering.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction#resetAutoNumbers(org.netbeans.modules.uml.core.metamodel.dynamics.IMessage)
     */
    public void resetAutoNumbers(IMessage pMessage)
    {
        ETList<IMessage> messages = 
           pMessage != null? 
						   new ElementCollector<IMessage>( )
						       .retrieveElementCollection( 
						           pMessage,
						       "following-sibling::*[not(@kind='result')]", IMessage.class )
                        :  getMessages();
        if (messages != null && messages.size() > 0)
        {
            for (Iterator<IMessage> iter = messages.iterator(); 
                    iter.hasNext(); )
            {
                IMessage mes = iter.next();
                if (mes != null)
                    mes.resetAutoNumber();
            }
        }
    }
    
    public void establishNodePresence(Document doc, Node parent)
    {
        buildNodePresence("UML:Interaction", doc, parent);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement#setNode(org.dom4j.Node)
     */
    public void setNode(Node n)
    {
        super.setNode(n);
        m_InteractionOperand.setNode(n);
    }
    
    ///////// IInteractionOperand delegate methods /////////
    // From org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand
    public IInteractionConstraint createGuard()
    {
        return m_InteractionOperand.createGuard();
    }

    // From org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand
    public IInteractionConstraint getGuard()
    {
        return m_InteractionOperand.getGuard();
    }

    // From org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment
    public IInteractionOperand getEnclosingOperand()
    {
        return m_InteractionOperand.getEnclosingOperand();
    }

    // From org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment
    public ETList<ILifeline> getCoveredLifelines()
    {
        return m_InteractionOperand.getCoveredLifelines();
    }

    // From org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand
    public ETList<IMessage> getCoveredMessages()
    {
        return m_InteractionOperand.getCoveredMessages();
    }

    // From org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand
    public ETList<IInteractionFragment> getFragments()
    {
        return m_InteractionOperand.getFragments();
    }

    // From org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment
    public ETList<IInterGateConnector> getGateConnectors()
    {
        return m_InteractionOperand.getGateConnectors();
    }

    // From org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment
    public void addCoveredLifeline(ILifeline lifeline)
    {
        m_InteractionOperand.addCoveredLifeline(lifeline);
    }

    // From org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand
    public void addFragment(IInteractionFragment fragment)
    {
        m_InteractionOperand.addFragment(fragment);
    }

    // From org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment
    public void addGateConnector(IInterGateConnector connector)
    {
        m_InteractionOperand.addGateConnector(connector);
    }

    // From org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment
    public void removeCoveredLifeline(ILifeline lifeline)
    {
        m_InteractionOperand.removeCoveredLifeline(lifeline);
    }

    // From org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand
    public void removeFragment(IInteractionFragment fragment)
    {
        m_InteractionOperand.removeFragment(fragment);
    }

    // From org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment
    public void removeGateConnector(IInterGateConnector connector)
    {
        m_InteractionOperand.removeGateConnector(connector);
    }

    // From org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand
    public void setGuard(IInteractionConstraint constraint)
    {
        m_InteractionOperand.setGuard(constraint);
    }
}