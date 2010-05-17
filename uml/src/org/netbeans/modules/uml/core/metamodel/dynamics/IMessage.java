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
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IInteractionOperator;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IConnector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IMessage extends INamedElement
{
    // Directions for a new message
    public static final int MDK_FROM_TO = 0;
    public static final int MDK_TO_FROM = 1;
    
	/**
	 * Sets / Gets the enclosing Interaction owning the Message.
	*/
	public IInteraction getInteraction();

	/**
	 * Sets / Gets the enclosing Interaction owning the Message.
	*/
	public void setInteraction( IInteraction value );

	/**
	 * Sets / Gets the Connector on which this Message is sent.
	*/
	public IConnector getConnector();

	/**
	 * Sets / Gets the Connector on which this Message is sent.
	*/
	public void setConnector( IConnector value );

	/**
	 * Sets / Gets the Sending Event of the Message.
	*/
	public IEventOccurrence getSendEvent();

	/**
	 * Sets / Gets the Sending Event of the Message.
	*/
	public void setSendEvent( IEventOccurrence value );

	/**
	 * Sets / Gets the ReceiveEvent of the Message.
	*/
	public IEventOccurrence getReceiveEvent();

	/**
	 * Sets / Gets the ReceiveEvent of the Message.
	*/
	public void setReceiveEvent( IEventOccurrence value );

	/**
	 * Sets / Gets the Action that initiates the Message.
	*/
	public IExecutionOccurrence getInitiatingAction();

	/**
	 * Sets / Gets the Action that initiates the Message.
	*/
	public void setInitiatingAction( IExecutionOccurrence value );

	/**
	 * The kind of message this is.
	*/
	public int getKind();

	/**
	 * The kind of message this is.
	*/
	public void setKind( /* MessageKind */ int value );

	/**
	 * The operation this message is calling.
	*/
	public IOperation getOperationInvoked();

	/**
	 * The operation this message is calling.
	*/
	public void setOperationInvoked( IOperation value );

	/**
	 * The target lifeline of this message.
	*/
	public ILifeline getReceivingLifeline();

	/**
	 * The source lifeline of this message.
	*/
	public ILifeline getSendingLifeline();

	/**
	 * If the ReceivingLifeline represents a part of a classifier, that classifier is returned.
	*/
	public IClassifier getReceivingClassifier();

	/**
	 * If the SendingLifeline represents a part of a classifier, that classifier is returned.
	*/
	public IClassifier getSendingClassifier();

	/**
	 * If the ReceivingLifeline represents a part of a classifier, the Operations of that Classifier are returned.
	*/
	public ETList<IOperation> getReceivingOperations();

	/**
	 * Sets / Gets the sending message for a result message.
	*/
	public IMessage getSendingMessage();

	/**
	 * Sets / Gets the sending message for a result message.
	*/
	public void setSendingMessage( IMessage value );

	/**
	 * Retrieves the recurrence expression calculated in getAutoNumber.
	 */
	public ETPairT<Integer, String> getRecurrence();

	/**
	 * Determines the number for this message.
	*/
	public String getAutoNumber();

	/**
	 * Resets the auto-number for this message, so the next AutoNumber call recalculates the auto-number.
	*/
	public void resetAutoNumber();

	/**
	 * Retrieves the interaction operand associated with both ends of the message.
	*/
	public IInteractionOperand getInteractionOperand();

	/**
	 * Retrieves the interaction operand associated with both ends of the message.
	*/
	public void setInteractionOperand( IInteractionOperand value );
	
	/**
	 * Change to lifeline on the sending end of this message.
	 */
	public void changeSendingLifeline(ILifeline pFromLifeline,ILifeline pToLifeline);
	
	/**
	 *  Change to lifeline on the receiving end of this message.
	 */
	public void changeReceivingLifeline(ILifeline pFromLifeline, ILifeline pToLifeline);	
}
