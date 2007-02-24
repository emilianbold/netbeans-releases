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
import java.util.Map;

/**
 *
 */
public class MessageFacilityFilter implements IMessageFacilityFilter
{
	
	private Map<Integer,Boolean> m_IsDisplayedMap = new HashMap<Integer,Boolean>();
	private String m_Name = new String();
	
	public MessageFacilityFilter()
	{
		setIsDisplayed (MsgCoreConstants.MT_CRITICAL,true);
		setIsDisplayed (MsgCoreConstants.MT_ERROR,true);
		setIsDisplayed (MsgCoreConstants.MT_WARNING,true);
		setIsDisplayed (MsgCoreConstants.MT_INFO,true);
		setIsDisplayed (MsgCoreConstants.MT_DEBUG, false);
	}	

	/**
	 * Gets the name of this facility.
	 *
	 * @return String The name of the facility
	 */
	public String getName() 
	{
		return m_Name;
	}

	/**
	 * Sets the name of the facility that this filter applies to.
	 *
	 * @param newName[in] The name of the facility
	 */
	public void setName(String newName) 
	{
		m_Name = newName;
	}

	/**
	 * Returns whether a particular message type for this facility is displayed.
	 *
	 * @param nMessageType[in] message type to query
	 * @return pVal[out] Is this message type displayed for this facility
	 */
	public boolean getIsDisplayed(int nMessageType)
	{
		boolean retVal = false;
		Boolean bool = m_IsDisplayedMap.get(new Integer(nMessageType));
		if (bool != null)
		{
			retVal = bool.booleanValue();
		}
		return retVal;
	}

	/**
	 * Given a particular message this routine sets the facility and message type to
	 * be displayed according to the parameter newVal.
	 *
	 * @param pMessageData[in] The message under question.  Should this be displayed
	 * @param newVal[in] Sets if it should be displayed
	 */
	public void setIsDisplayed(int nMessageType, boolean value)
	{
		m_IsDisplayedMap.put(new Integer(nMessageType), new Boolean(value));
	}
}


