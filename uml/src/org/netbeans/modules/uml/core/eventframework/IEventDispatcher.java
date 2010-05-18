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
