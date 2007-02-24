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

import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IEvent;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IEventOccurrence extends INamedElement
{
	/**
	 * Adds to the collection of GeneralOrdering that is also connected to an Event that should come before this Event
	*/
	public void addAfterOrdering( IGeneralOrdering pOrdering );

	/**
	 * Removes the given GeneralOrdering from the collection of GeneralOrdering that is also connected to an Event that should come before this Event
	*/
	public void removeAfterOrdering( IGeneralOrdering pOrdering );

	/**
	 * Retrieves the collection of GeneralOrdering that is also connected to an Event that should come before this Event
	*/
	public ETList<IGeneralOrdering> getAfterOrderings();

	/**
	 * Adds to the collection of GeneralOrdering that is also connected to an Event that should come after this Event
	*/
	public void addBeforeOrdering( IGeneralOrdering pOrdering );

	/**
	 * Removes the given GeneralOrdering from the collection of GeneralOrdering that is also connected to an Event that should come after this Event
	*/
	public void removeBeforeOrdering( IGeneralOrdering pOrdering );

	/**
	 * Retrieves the collection of GeneralOrdering that is also connected to an Event that should come after this Event
	*/
	public ETList<IGeneralOrdering> getBeforeOrderings();

	/**
	 * References the ExecutionOccurrence of a start Event (of action).
	*/
	public IExecutionOccurrence getStartExec();

	/**
	 * References the ExecutionOccurrence of a start Event (of action).
	*/
	public void setStartExec( IExecutionOccurrence value );

	/**
	 * References the ExecutionOccurrence of a finish Event (of action).
	*/
	public IExecutionOccurrence getFinishExec();

	/**
	 * References the ExecutionOccurrence of a finish Event (of action).
	*/
	public void setFinishExec( IExecutionOccurrence value );

	/**
	 * References the Message that contains the information of a sendEvent.
	*/
	public IMessage getSendMessage();

	/**
	 * References the Message that contains the information of a sendEvent.
	*/
	public void setSendMessage( IMessage value );

	/**
	 * References the Message that contains the information of a receiveEvent.
	*/
	public IMessage getReceiveMessage();

	/**
	 * References the Message that contains the information of a receiveEvent.
	*/
	public void setReceiveMessage( IMessage value );

	/**
	 * The enclosing Interaction owning the EventOccurrence.
	*/
	public IInteraction getInteraction();

	/**
	 * The type of the EventOccurrence.
	*/
	public IEvent getEventType();

	/**
	 * The type of the EventOccurrence.
	*/
	public void setEventType( IEvent value );

	/**
	 * References the Lifeline on which the Event appears.
	*/
	public ILifeline getLifeline();

	/**
	 * References the Lifeline on which the Event appears.
	*/
	public void setLifeline( ILifeline value );

	/**
	 * References an optional InterGateConnection attached to this (message) Event.
	*/
	public IInterGateConnector getConnection();

	/**
	 * References an optional InterGateConnection attached to this (message) Event.
	*/
	public void setConnection( IInterGateConnector value );
}