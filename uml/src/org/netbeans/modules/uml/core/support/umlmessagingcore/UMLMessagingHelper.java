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


