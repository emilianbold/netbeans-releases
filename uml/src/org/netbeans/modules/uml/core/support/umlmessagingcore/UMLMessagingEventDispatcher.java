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


