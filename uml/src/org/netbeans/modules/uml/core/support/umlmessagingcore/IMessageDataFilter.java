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

public interface IMessageDataFilter
{
	/**
	 * method Initialize
	*/
	public void initialize( String fileLocation, IMessageService pMessenger );

	/**
	 * Get the filters that are active
	*/
	public ETList<IMessageFacilityFilter> getFilters();

	/**
	 * Should this message be processed?
	*/
	public boolean getIsDisplayed( IMessageData pMessageData );

	/**
	 * Should this message be processed?
	*/
	public void setIsDisplayed( IMessageData pMessageData, boolean value );

	/**
	 * Should this message be processed?
	*/
	public boolean getIsDisplayed( /* MESSAGE_TYPE */ int nMessageType, String sFacility );

	/**
	 * Should this message be processed?
	*/
	public void setIsDisplayed( /* MESSAGE_TYPE */ int nMessageType, String sFacility, boolean value );

	/**
	 * Save the filter settings
	*/
	public void save();

	/**
	 * ReRead the filter settings from disk
	*/
	public void reRead();

}
