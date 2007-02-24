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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.netbeans.modules.uml.core.coreapplication.CoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductManager;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.IEventDispatchController;
import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author sumitabhk
 *
 */
public class MessageService implements IMessageService
{
	private IUMLMessagingEventDispatcher m_EventDispatcher = null;
	//	The facilities that have added messages or ones that we know about beforehand (hardcoded)
	private Map<String,IMessageFacility> m_FacilityMap = new HashMap<String,IMessageFacility>(); 

	/**
	 * Fires the message added connection point to let those listening know that a new
	 * message should be responded to.
	 *
	 * @param pMessage The message to be added
	 */
	public void addMessage(IMessageData pMessage) 
	{
		ICoreProductManager pProductManager =  CoreProductManager.instance();
		ICoreProduct pCoreProduct = pProductManager.getCoreProduct();
		
		// Now get the core event dispatcher so we can dispatch events
		IEventDispatchController cont = pCoreProduct.getEventDispatchController();	
		
		IEventDispatcher disp = pCoreProduct.getEventDispatcher(
										EventDispatchNameKeeper.messagingName());
											
		if (disp != null && (disp instanceof IUMLMessagingEventDispatcher))
		{
			m_EventDispatcher = (IUMLMessagingEventDispatcher)disp;		
		}

		if ( m_EventDispatcher != null)
		{
			IEventPayload payload = m_EventDispatcher.createPayload("MessageAdded");
			m_EventDispatcher.fireMessageAdded(pMessage,payload);
		}
	}

	/**
	 * Fires the message added connection point to let those listening know that a new
	 * message should be responded to.  This routine constructs a message based on
	 * the arguments and then fires AddMessage.
	 *
	 * @param nMessageType The message type for the message to be added
	 * @param sFacility The facility that generated this message
	 * @param sMessageString The text of the message
	 *
	 * @see MessageService::AddMessage
	 */
	public void addMessage(int nMessageType, String sFacility, String sMessageString) 
	{
		IMessageData  pMessageData = new MessageData();
		if (sMessageString != null)
		{
			pMessageData.setMessageString(sMessageString);
			pMessageData.setMessageType(nMessageType);
			pMessageData.setFacility(sFacility);
		}
		addMessage(pMessageData);
	}

	/**
	 * Fires the message added connection point to let those listening that a new
	 * message should be responded to.  This routine constructs a message based on
	 * the arguments and then fires AddMessage.
	 *
	 * @param nMessageType The message type for the message to be added
	 * @param hInstance The HINSTANCE where the facility string can be found
	 * @param nFacilityStringID The id of the facility string
	 * @param sMessageString The text of the message
	 *
	 * @see MessageService::AddMessage
	 */
	public void addMessage(int nMessageType, int hInstance, int nFacilityStringID, String sMessageString) 
	{
		//AZTEC: need to be replaced
		// Get the facility string
		//sFacilityString.LoadString((HINSTANCE)hInstance, (UINT)nFacilityStringID);
		String sFacilityString = "";
		if (sFacilityString == null || sFacilityString.length() == 0)
		{
			sFacilityString = "Unknown";
		}
		addMessage(nMessageType, sFacilityString, sMessageString);
	}

	/**
	 * MessageService keeps a list of all facilities that it knows about.  This is helpful
	 * for the filter object which can then create filters based on user preferences.  This
	 * routine verifies that the facility that generated this message is known.  If not
	 * then a new facility is added to our list.
	 *
	 * @param pMessage The message that should be verified
	 */
	protected void addFacility(IMessageData pMessage)
	{
		String facility = pMessage.getFacility();
		if (facility != null)
		{
			addFacility(facility);
		}
	}
	
	/**
	 * MessageService keeps a list of all facilities that it knows about.  This is helpful
	 * for the filter object which can then create filters based on user preferences.  This
	 * routine verifies that the facility that generated this message is known.  If not
	 * then a new facility is added to our list.
	 *
	 * @param sFacility The facility that we should verify is in our list
	 */
	protected void addFacility(String sFacility)
	{
		if (sFacility != null && sFacility.length() > 0)
		{
			if (!m_FacilityMap.containsKey(sFacility))
			{
				IMessageFacility msgFacility = new MessageFacility();
				m_FacilityMap.put(sFacility,msgFacility);
			}
		}
	}
	
	/**
	 * CMessenger keeps a list of all facilities that it knows about.  This is helpful
	 * for the filter object which can then create filters based on user preferences.  This
	 * routine returns a list of all known facilities.
	 *
	 * @param pFacilities A returned list containing all our known facilities
	 */
	public ETList<IMessageFacility> getMessageFacilities() 
	{		
		ETList<IMessageFacility> facilities = new ETArrayList<IMessageFacility>();
		Set keys = m_FacilityMap.keySet();
		Iterator iter = keys.iterator();
		while (iter.hasNext())
		{
			String key = (String)iter.next();
			if (m_FacilityMap.get(key) != null)
			{
				facilities.add((IMessageFacility)m_FacilityMap.get(key)); 
		    } 
		}
		return facilities;
	}

}


