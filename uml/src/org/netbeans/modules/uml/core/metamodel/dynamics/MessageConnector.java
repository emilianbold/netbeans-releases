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
