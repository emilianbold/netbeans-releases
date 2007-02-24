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

public interface IInterGateConnector extends IElement
{
	/**
	 * Sets / Gets the Gate from which the InterGateConnector starts.
	*/
	public IGate getFromGate();

	/**
	 * Sets / Gets the Gate from which the InterGateConnector starts.
	*/
	public void setFromGate( IGate value );

	/**
	 * Sets / Gets the Gate to which the InterGateconnector targets.
	*/
	public IGate getToGate();

	/**
	 * Sets / Gets the Gate to which the InterGateconnector targets.
	*/
	public void setToGate( IGate value );

	/**
	 * Sets / Gets the enclosing InteractionFragment.
	*/
	public IInteractionFragment getFragment();

	/**
	 * Sets / Gets the enclosing InteractionFragment.
	*/
	public void setFragment( IInteractionFragment value );

	/**
	 * Sets / Gets the possible Event that may indicate either the start or the end of the InterGateConnector.
	*/
	public IEventOccurrence getEventOccurrence();

	/**
	 * Sets / Gets the possible Event that may indicate either the start or the end of the InterGateConnector.
	*/
	public void setEventOccurrence( IEventOccurrence value );

	/**
	 * Sets / Gets the Message type that may flow along the InterGateConnector.
	*/
	public IMessage getMessage();

	/**
	 * Sets / Gets the Message type that may flow along the InterGateConnector.
	*/
	public void setMessage( IMessage value );
}
