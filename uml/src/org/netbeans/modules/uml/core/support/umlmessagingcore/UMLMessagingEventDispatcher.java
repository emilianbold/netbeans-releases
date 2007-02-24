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

package org.netbeans.modules.uml.core.support.umlmessagingcore;

import org.netbeans.modules.uml.core.eventframework.EventDispatcher;
import org.netbeans.modules.uml.core.eventframework.EventFunctor;
import org.netbeans.modules.uml.core.eventframework.EventManager;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

/**
 *
 */
public class UMLMessagingEventDispatcher extends EventDispatcher
						implements IUMLMessagingEventDispatcher
{
	private EventManager< IMessengerEventsSink > m_MessengerSink = null;
	
	/**
	 * 
	 */
	public UMLMessagingEventDispatcher() 
	{
		super();
		m_MessengerSink = new EventManager< IMessengerEventsSink >();
	}

	/**
	 * Registers a sink that will receive all notifications concerning Messaging.
	 *
	 * @param handler[in]
	 */
   public void registerMessengerEvents(IMessengerEventsSink handler)
   {   	
		m_MessengerSink.addListener(handler,null);
   }

   /**
	* Removes a sink listening for messaging events.
	*
	* @param cookie[in]
	*
	* @result S_OK
	*/
   public void revokeMessengerSink(IMessengerEventsSink handler)
   {
		m_MessengerSink.removeListener(handler);      
   }

   /**
	* Calling this method will result in the firing of any listeners
	* interested in this event
	*
	* @param pMessage[in]
	* @param payload[in]
	*/
   public void fireMessageAdded(IMessageData pMessage, IEventPayload payload)
   {
		if (validateEvent("MessageAdded", pMessage))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor messengerMessageAdded = new EventFunctor("org.netbeans.modules.uml.core.support.umlmessagingcore.IMessengerEventsSink", 
						"onMessageAdded");
			
			Object[] parms = new Object[2];
			parms[0] = pMessage;	
			parms[1] = cell;
			messengerMessageAdded.setParameters(parms);			
			m_MessengerSink.notifyListeners(messengerMessageAdded);
		}      
   }

   /**
	* IEventDispatcher override.  Returns the number of registered sinks
	*/
   public int getNumRegisteredSinks()
   {
		return m_MessengerSink.getNumListeners();
   }
}


