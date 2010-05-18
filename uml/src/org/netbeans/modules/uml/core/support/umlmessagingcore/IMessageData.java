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

import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IMessageData
{	
	/**
	 * Get/Set the text of the message
	*/
	public String getMessageString();

	/**
	 * Get/Set the text of the message
	*/
	public void setMessageString( String value );

	/**
	 * Get/Set the message type.
	*/
	public int getMessageType();

	/**
	 * Get/Set the message type.
	*/
	public void setMessageType( /* MESSAGE_TYPE */ int value );

	/**
	 * Clone this message data object to create a new one.
	*/
	public IMessageData clone();

	/**
	 * Get/Set the name of the facility that is generating this message data object.
	*/
	public String getFacility();

	/**
	 * Get/Set the name of the facility that is generating this message data object.
	*/
	public void setFacility( String value );

	/**
	 * Get a formatted message string that is suitable for display.
	*/
	public String getFormattedMessageString( boolean bAddTimestamp );

	/**
	 * Get the timestamp that this message data's messagestring was set in a character format
	*/
	public String getTimestamp();

	/**
	 * Get the message type of this object in a character format rather then an enumeration
	*/
	public String getMessageTypeString();

	/**
	 * Get/Set the sub messages
	*/
	public ETList<IMessageData> getSubMessages();

	/**
	 * Get/Set the sub messages
	*/
	public void setSubMessages( ETList<IMessageData> value );

	/**
	 * Get/Set the raw time which is in time_t format.
	*/
	public long getTimeT();

	/**
	 * Get/Set the raw time which is in time_t format.
	*/
	public void setTimeT( long value );

	/**
	 * Sets various details of the message
	*/
	public void setDetails( /* MESSAGE_TYPE */ int nMessageType, String sFacility, String value );

	/**
	 * Adds this message to the list of sub messages
	*/
	public void addSubMessage( IMessageData pSubMessage );

	/**
	 * Adds this message to the list of sub messages
	*/
	public IMessageData addSubMessage( /* MESSAGE_TYPE */ int nMessageType, String sFacility, String sMessageString );

	/**
	 * Adds this message to the list of sub messages.  The facility string is loaded from hInstance with the id being nFacilityStringID.
	*/
	public IMessageData addSubMessage( /* MESSAGE_TYPE */ int nMessageType, int hInstance, int nFacilityStringID, String sMessageString );

}
