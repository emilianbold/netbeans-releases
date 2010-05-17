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


