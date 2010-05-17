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
