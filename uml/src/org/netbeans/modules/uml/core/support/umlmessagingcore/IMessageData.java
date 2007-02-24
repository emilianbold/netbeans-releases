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
