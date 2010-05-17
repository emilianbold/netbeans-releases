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
