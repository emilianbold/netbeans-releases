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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;


public interface IDynamicsRelationFactory
{
    /**
     * Changes the message's ends to be constrained by the to/from interaction
     * operands.
     *
     * @param message    message being moved
     * @param fromOwner  the interaction operand owner for the sending side of
     *                   the message
     * @param toOwner    the interaction operand owner for the receiving side 
     *                   of the message
     */
    public void moveMessageToInteractionOperands(IMessage message,
            IInteractionOperand fromOwner, IInteractionOperand toOwner);

    /**
     * Creates a new Message between the from lifeline and the passed in to 
     * lifeline.
     *
     * @param fromElement Element where the message should start, must convert 
     *                    to a Lifeline, Interaction, or InteractionOccurrence
     * @param fromOwner   The owning interaction fragment.  If NULL, the 
     *                    fromElement's interaction fragment is used
     * @param toElement   Element where the message should end, must convert to 
     *                    a Lifeline, Interaction, or InteractionOccurrence
     * @param toOwner     The owning interaction fragment.  If NULL, the 
     *                    toElement's interaction fragment is used
     * @param oper        The operation being invoked.
     *                    If null, Uninterpretted actions will be created on 
     *                    both sides of the message.
     * @param kind        The type of message to create. Only certain types of 
     *                    messages can be created between specific elements.
     * @return The new IMessage.
     */
    public IMessage insertMessage(IMessage fromBeforeMessage,
            IElement fromElement, IInteractionFragment fromOwner,
            IElement toElement, IInteractionFragment toOwner, IOperation oper,
            int kind);

    /**
	 * Creates a new Message between the from lifeline and the passed in to
	 * lifeline.
	 * 
	 * @param fromElement
	 *            Element where the message should start, must convert to a
	 *            Lifeline, Interaction, or InteractionOccurrence
	 * @param fromOwner
	 *            The owning interaction fragment. If NULL, the fromElement's
	 *            interaction fragment is used
	 * @param toElement
	 *            Element where the message should end, must convert to a
	 *            Lifeline, Interaction, or InteractionOccurrence
	 * @param toOwner
	 *            The owning interaction fragment. If NULL, the toElement's
	 *            interaction fragment is used
	 * @param oper
	 *            The operation being invoked. If NULL, Uninterpretted actions
	 *            will be created on both sides of the message.
	 * @param kind
	 *            The type of message to create. Only certain types of messages
	 *            can be created between specific elements.
	 * @return The new message
	 */
    public IMessage createMessage(IElement fromElement,
            IInteractionFragment fromOwner, IElement toElement,
            IInteractionFragment toOwner, IOperation oper, int kind);

    /**
	 * Creates a new Action Occurrence on the Lifeline.
	 * 
	 * @param lifeline
	 *            The action occurrence will be added to this lifeline
	 * @param bstrActionType
	 *            The type of action occurrence to add to the lifeline
	 * @return The created action occurrence
	 */
    public IActionOccurrence createActionOccurrence(IInteractionFragment owner,
            ILifeline lifeline, String sActionType);

    

    /**
	 * Creates a new MessageConnector between the two passed in life lines
	 * 
	 * @param from
	 *            The from lifeline
	 * @param to
	 *            The to lifeline
	 * @return The new connector, else an error occurred. The connector is
	 *         placed in the from lifeline's Interaction.
	 */
    public IMessageConnector createMessageConnector(ILifeline from, ILifeline to);
}