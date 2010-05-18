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
