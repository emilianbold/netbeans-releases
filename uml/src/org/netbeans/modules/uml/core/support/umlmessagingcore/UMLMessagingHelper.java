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

import org.netbeans.modules.uml.core.coreapplication.CoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductManager;

/**
 * This is a messenger helper class.  It automatically gets the correct message service so
 * you don't have to remember how to do it.  Users just call SendMessage to send messages out
 * the interface.
 *
 */
public class UMLMessagingHelper
{
	private IMessageService  m_pMessageService = null;
	private String m_Facility = null;
	
	public UMLMessagingHelper()
	{
	}
	
	public UMLMessagingHelper(String facility)
	{
		m_Facility = facility;
	}
	
	/**
	 * Sends a message.
	 *
	 * @param pMessageData The message to be sent.
	 */
	public void sendMessage( IMessageData pMessageData)
	{
		IMessageService pMessageService = getMessageService();
		if (pMessageService != null)
		{
			pMessageService.addMessage(pMessageData);	
		}
	}
	
	/**
	 * Sends a message.
	 *
	 * @param MESSAGE_TYPE The type of the message
	 * @param message The message
	 */
	public void sendMessage( int messageType,String message)
	{
		IMessageService pMessageService = getMessageService();
		if (pMessageService != null)
		{
			pMessageService.addMessage(messageType,m_Facility,message);	
		}
	}
	
	/**
	 * Returns an IMessageService.  If possible it returns a cached one, otherwise this routine goes to the
	 * product manager and then to the product to get the message service.  Note that the message service is
	 * not a smart pointer 'cause I don't want this little helper class to be holding onto the service.
	 */
	public IMessageService getMessageService()
	{
		if (m_pMessageService == null)
		{
			ICoreProductManager pProductManager =  CoreProductManager.instance();
			ICoreProduct pCoreProduct = pProductManager.getCoreProduct();
			if (pCoreProduct != null)
			{
				m_pMessageService = pCoreProduct.getMessageService();			
			}
		}
		return m_pMessageService;
	}

    /**
     * Sends a message that is marked as a critical error.
     *
     * @param message The message.
     */
	public void sendCriticalMessage(String message)
	{
		sendMessage(MsgCoreConstants.MT_CRITICAL, message);
	}
	
    /**
     * Sends a message that is marked as an error.
     *
     * @param message The message.
     */
	public void sendErrorMessage(String message)
	{
		sendMessage(MsgCoreConstants.MT_ERROR, message);
	}
	
	public void sendWarningMessage(String message)
	{
		sendMessage(MsgCoreConstants.MT_WARNING, message);
	}
	
    /**
     * Sends a message that is marked as an informational message.
     *
     * @param message The message.
     */
	public void sendInfoMessage(String message)
	{
		sendMessage(MsgCoreConstants.MT_INFO, message);
	}
	
    /**
     * Sends a message that is marked as a debug message.
     *
     * @param message The message.
     */
	public void sendDebugMessage(String message)
	{
		sendMessage(MsgCoreConstants.MT_DEBUG, message);
	}
    
    /**
     * Sends a message that is marked as an error message.  The error message
     * is retrieved from the throwable exception.
     *
     * @param message The message.
     */
	public void sendExceptionMessage(Throwable e)
	{
		sendMessage(MsgCoreConstants.MT_ERROR, e.getLocalizedMessage());
	}
}


