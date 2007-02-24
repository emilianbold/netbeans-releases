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


package org.netbeans.modules.uml.core.eventframework;

public interface IEventDispatcher
{
	/**
	 * Registers an event sink to handle frameework events.
	*/
	public void registerForEventFrameworkEvents( IEventFrameworkEventsSink handler );

	/**
	 * Removes a sink listening for framework events.
	*/
	public void revokeEventFrameworkSink( IEventFrameworkEventsSink handler );

	/**
	 * Calling this method will result in the firing of any listeners who register for element modified events.
	*/
	public boolean firePreEventContextPushed( IEventContext pContext, IEventPayload payLoad );

	/**
	 * Calling this method will result in the firing of any listeners who register for element modified events.
	*/
	public void fireEventContextPushed( IEventContext pContext, IEventPayload payLoad );

	/**
	 * Fired before an event context is popped off the dispatcher.
	*/
	public boolean firePreEventContextPopped( IEventContext pContext, IEventPayload payLoad );

	/**
	 * Fired after an event context is popped off the dispatcher.
	*/
	public void fireEventContextPopped( IEventContext pContext, IEventPayload payLoad );

	/**
	 * Calling this method will result in the firing of any listeners who register for element modified events.
	*/
	public void fireEventDispatchCancelled( Object[] pListeners, Object listenerWhoCancelled, IEventPayload payLoad );

	/**
	 * Makes the EventContext of the passed in name the current EventContext.
	*/
	public void pushEventContext( String pContext );

	/**
	 * Makes the EventContext of the passed in name the current EventContext.
	*/
	public IEventContext pushEventContext2( String context );

	/**
	 * Pushes the passed in EventContext onto this dispatcher.
	*/
	public void pushEventContext3( IEventContext pContext );

	/**
	 * Pops the current context off the stack.
	*/
	public void popEventContext();

	/**
	 * Pops the current context off the stack, returning it.
	*/
	public IEventContext popEventContext2();

	/**
	 * Retrieves the current context on this dispatcher.
	*/
	public IEventContext getCurrentContext();

	/**
	 * Retrieves the name of the current context on this dispatcher.
	*/
	public String getCurrentContextName();

	/**
	 * Removes any EventContext with the passed in name, regardless of position in the stack.
	*/
	public void removeEventContextByName( String name );

	/**
	 * Removes any EventContext that contains in IEventFilter matching the passed in ID, regardless of stack position.
	*/
	public void removeEventContextByFilterID( String filterID );

	/**
	 * Creates a specific payload depending on the name of the trigger passed in.
	*/
	public IEventPayload createPayload( String triggerName );

	/**
	 * Sets / Gets the flag that determines whether or not any event should be fired.
	*/
	public boolean getPreventAllEvents();

	/**
	 * Sets / Gets the flag that determines whether or not any event should be fired.
	*/
	public void setPreventAllEvents( boolean value );

	/**
	 * Returns the number of sinks registered to this dispatcher
	*/
	public int getNumRegisteredSinks();

}
