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

import org.netbeans.modules.uml.core.metamodel.infrastructure.IConnector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public interface IMessageConnector extends IConnector
{
    /**
     * Retrieves the destination lifeline this connector is connected to.
     *
     * @return The lifeline
     */
    public ILifeline getToLifeline();

    /**
     * Sets the destination lifeline this connector is connected to.
     *
     * @param lifeline The lifeline
     */
    public void setToLifeline(ILifeline lifeline);
    
    /**
     * Retrieves the lifeline that is the source of this connector.
     * 
     * @return The lifeline
     */
    public ILifeline getFromLifeline();
    
    /**
     * Sets the lifeline that is the source of this connector.
     * 
     * @param lifeline The lifeline
     */
    public void setFromLifeline(ILifeline lifeline);
    
    /**
     * Retrieves the lifelines on the ends of this connector
     *
     * @return The lifeline collection
     */
    public ETList<ILifeline> getConnectedLifelines();

    /**
     * Retrieves the messages associated with this connector.
     *
     * @return The collection of messages
     */
    public ETList<IMessage> getMessages();

    /**
     * Removes the given message from this connector's list of messages.
     *
     * @param message The message to remove.
     */
    public void removeMessage(IMessage message);
    
    /**
     * Adds the given message to this connector's list of messages.
     *
     * @param message The message to add.
     */
    public void addMessage(IMessage message);
    
    /**
     * Creates a new IMessage, placing it on the IInteraction that owns this 
     * connector, then associates the IOperation with the IMessage
     *
     * ASSUMPTIONS: This was written for the collaboration diagram messages. 
     *              We're assuming we're not crossing interactions.
     *
     * @param  directionKind  The direction the message should go (FROM to TO or 
     *                        TO to FROM)
     * @param  oper           The operation to associate with the new message.
     * 
     * @return message        The new IMessage
     */
    public IMessage addMessage(int nDirectionKind, IOperation pOper);
}