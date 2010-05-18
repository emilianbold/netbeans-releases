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

import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IConnector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Behavior;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
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
        ETList<IMessage> messages = (pMessage != null ? 
            new ElementCollector<IMessage>().retrieveElementCollection(
            pMessage, "following-sibling::*[not(@kind='result')]", IMessage.class) :
            getMessages());
        
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
