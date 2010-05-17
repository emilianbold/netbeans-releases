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

import java.util.Calendar;
import java.util.Date;

import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;

/**
 *
 */
public class MessageData implements IMessageData, Cloneable
{
	private String m_MessageString = null;
	private int m_MessageType = 0;
	private String m_Facility = null;
	private ETList<IMessageData> m_SubMessages = new ETArrayList<IMessageData>();
	private long m_osBinaryTime = new Date().getTime();
	/**
	 * Adds this message to the list of sub messages
	 *
	 * @param nMessageType[in]
	 * @param hInstance[in]
	 * @param nFacilityStringID[in]
	 * @param sMessageString[in]
	 * @return pCreatedMessage[out]
	 */
	public IMessageData addSubMessage( /* MESSAGE_TYPE */ int nMessageType, int hInstance, int nFacilityStringID, String sMessageString )
	{
		String sFacilityString = "";
		if (sFacilityString == null || sFacilityString.length() == 0)
		{
			sFacilityString = "Unknown";
		}
		return addSubMessage(nMessageType, sFacilityString, sMessageString);
	}
	
	/**
	 * Adds this message to the list of sub messages
	 *
	 * @param nMessageType[in]
	 * @param sFacility[in]
	 * @param sMessageString[in]
	 * @return pCreateMessage[out]
	 */
	public IMessageData addSubMessage( /* MESSAGE_TYPE */ int nMessageType, String sFacility, String sMessageString )
	{
		IMessageData subMsg = new MessageData();
		if (subMsg != null)
		{
			subMsg.setDetails(nMessageType,sFacility,sMessageString);
			addSubMessage(subMsg);
		}
		return subMsg;
	}
	
	/**
	 * Adds this message to the list of sub messages
	 *
	 * @param pSubMessage [in] The child message to this one.
	 */
	public void addSubMessage( IMessageData pSubMessage )
	{
		ETList<IMessageData> msgDatas = getSubMessages();		
		if (msgDatas != null)
		{			
			msgDatas.add(pSubMessage);
		}
		this.m_SubMessages = msgDatas;
	}
	
	/**
	 * Sets various details of the message
	 */
	public void setDetails( /* MESSAGE_TYPE */ int nMessageType, String sFacility, String value )
	{
		setMessageType(nMessageType);
		setFacility(sFacility);
		setMessageString(value);
	}
	
	/**
	 * Returns the message string for this particular message.
	 *
	 * @param pVal The message string
	 */
	public String getMessageString()
	{
		return this.m_MessageString;		
	}
	
	/**
	 * Sets the message string for this particular message.
	 *
	 * @param newVal The message string
	 */
	public void setMessageString( String value )
	{
		this.m_MessageString = value;				
	}
	
	
	/**
	 * @return
	 */
	public int getMessageType() 
	{
		return this.m_MessageType;
	}

	/**
	 * @param i
	 */
	public void setMessageType(int i) 
	{
		this.m_MessageType = i;
	}
	
	
	public IMessageData clone()
	{
		MessageData clone = new MessageData();
		clone.setFacility(getFacility()); 
		clone.setMessageType(getMessageType());
		clone.setMessageString(getMessageString());
		clone.setSubMessages(getSubMessages());
		clone.setTimeT(getTimeT());
		return clone;
	}
	
	

	/**
	 * @return
	 */
	public String getFacility() 
	{
		return this.m_Facility;
	}

	/**
	 * @param string
	 */
	public void setFacility(String string) 
	{
		this.m_Facility = string;
	}

	/**
	 * Gets the string formatted such that the time stamp, facility, message type, and
	 * message string are returned in one BSTR.
	 *
	 * @param bAddTimestamp Should the time.stamp be added to the message
	 * @param pFormattedString The returned formatted message string.
	 */
	public String getFormattedMessageString( boolean bAddTimestamp )
	{
		String messageType = getMessageTypeString();
		if (messageType == null || messageType.length() ==0)
		{
			messageType = "Unknown";
		}
		StringBuffer formatedBuff = new StringBuffer();
		if (bAddTimestamp)
		{
			String timestamp = getTimestamp();		
	
			if (timestamp != null)
			{				
				formatedBuff.append(timestamp)
							.append(" [")
							.append(this.m_Facility)
							.append(" (")
							.append(messageType)
							.append(")] ")
							.append(this.m_MessageString);							
			}
			else
			{
				formatedBuff.append(" [")
							.append(this.m_Facility)
							.append(" (")
							.append(messageType)
							.append(")] ")
							.append(this.m_MessageString);
			}
		}
		else
		{
			formatedBuff.append(" [")
						.append(this.m_Facility)
						.append(" (")
						.append(this.m_Facility)
						.append(")] ")
						.append(this.m_MessageString);
		}
		return formatedBuff.toString();		
	}
	
	public String getTimestamp()
	{
		Date dat = new Date(this.m_osBinaryTime);
		Calendar cal = Calendar.getInstance();
		cal.setTime(dat);
		StringBuffer buf = new StringBuffer();
		buf.append(cal.get(Calendar.YEAR))
		   .append("-")
		   .append(cal.get(Calendar.MONTH))
		   .append("-")
		   .append(cal.get(Calendar.DAY_OF_WEEK))
		   .append(" ")
		   .append(cal.get(Calendar.HOUR))
		   .append(":")
		   .append(cal.get(Calendar.MINUTE))
		   .append(":")
		   .append(cal.get(Calendar.SECOND));			
		return buf.toString();
	}
	
	/**
	 * Returns the message type for this message as a string.
	 *
	 * @param pVal The message type
	 */
	public String getMessageTypeString()
	{
		String messageType = new String();
		if (this.m_MessageType == MsgCoreConstants.MT_ERROR)
		{
		   messageType = "Error";
		}
		else if (m_MessageType == MsgCoreConstants.MT_WARNING)
		{
		   messageType = "Warning";
		}
		else if (m_MessageType == MsgCoreConstants.MT_INFO)
		{
		   messageType = "Info";
		}
		else if (m_MessageType == MsgCoreConstants.MT_DEBUG)
		{
		   messageType = "Debug";
		}
		else if (m_MessageType == MsgCoreConstants.MT_CRITICAL)
		{
		   messageType = "Critical";
		}
		else
		{
		   messageType = "Unknown";
		}
		return messageType;
	}
	

	/**
	 * Returns a list of all the submessages for this message.
	 *
	 * @param pVal The sub messages
	 */
	public ETList<IMessageData> getSubMessages()
	{		
		return this.m_SubMessages;
	}
	
	public void setSubMessages( ETList<IMessageData> value )
	{
		this.m_SubMessages = value;
	}
	public long getTimeT()
	{
		return this.m_osBinaryTime;
	}
	public void setTimeT(long time)
	{
		this.m_osBinaryTime = time;
	}
}


