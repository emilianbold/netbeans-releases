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
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class InteractionOperand extends InteractionFragment
        implements IInteractionOperand
{
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement#delete()
     */
    public void delete()
    {
        IInteractionOperand op = getEnclosingOperand();
        if (op != null)
        {
            ETList<IMessage> messages = getCoveredMessages();
            if (messages != null && messages.size() > 0)
            {
                for (Iterator<IMessage> iter = messages.iterator();
                        iter.hasNext(); )
                {
                    IMessage mes = iter.next();
                    if (mes != null)
                        mes.setInteractionOperand(op);
                }
            }
        }
        super.delete();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand#getFragments()
     */
    public ETList<IInteractionFragment> getFragments()
    {
        return new ElementCollector<IInteractionFragment>( )
            .retrieveElementCollection( 
                m_Node, "UML:Element.ownedElement/*", IInteractionFragment.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand#removeFragment(org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment)
     */
    public void removeFragment(IInteractionFragment frag)
    {
        removeElement(frag);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand#addFragment(org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment)
     */
    public void addFragment(IInteractionFragment frag)
    {
        addElement(frag);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand#createGuard()
     */
    public IInteractionConstraint createGuard()
    {
        IInteractionConstraint guard = getGuard();
        if (guard == null)
        {
            guard = new TypedFactoryRetriever<IInteractionConstraint>()
                        .createType("InteractionConstraint");
            if (guard != null)
            {
                IExpression expression = 
                    new TypedFactoryRetriever<IExpression>()
                        .createType("Expression");
                if (expression != null)
                {
                    guard.setSpecification(expression);
                    expression.setBody("<expression>");
                }
                setGuard(guard);
            }
        }
        return guard;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand#getGuard()
     */
    public IInteractionConstraint getGuard()
    {
        return new ElementCollector<IInteractionConstraint>( )
            .retrieveSingleElement( 
                m_Node, 
                "UML:InteractionOperand.guard/*", IInteractionConstraint.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand#setGuard(org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionConstraint)
     */
    public void setGuard(IInteractionConstraint value)
    {
        addChild("UML:InteractionOperand.guard", 
                "UML:InteractionOperand.guard", value );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand#getCoveredMessages()
     */
    public ETList<IMessage> getCoveredMessages()
    {
        ETList<IMessage> messages = new ETArrayList<IMessage>();
        ETList<IInteractionFragment> frags = getFragments();
        
        if (frags != null && frags.size() > 0)
        {
            IMessage prevMessage = null;
            for (Iterator<IInteractionFragment> iter = frags.iterator();
                    iter.hasNext(); )
            {
                IInteractionFragment frag = iter.next();
                if (frag instanceof IAtomicFragment)
                {
                    IEventOccurrence event = 
                        ((IAtomicFragment) frag).getEvent();
                    if (event != null)
                    {
                        IMessage message = event.getSendMessage();
                        if (message == null)
                            message = event.getReceiveMessage();
                        if (message != null && 
                                (prevMessage == null || 
                                        !message.isSame(prevMessage)))
                        {
                            messages.add(message);
                        }
                        prevMessage = message;
                    }
                }
            }
        }
        
        return messages;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#establishNodePresence(org.dom4j.Document, org.dom4j.Node)
     */
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:InteractionOperand", doc, node);
    }
}