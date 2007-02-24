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

package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.dom4j.Node;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IParserData
{
	/**
	 * Retrieves the event data in a pure XMI format.  All TokenDescriptors will be removed from the string.
	*/
	public String getXMIString();

	/**
	 * The event data in its pure format.  All TokenDescriptors will still exist in the string.
	*/
	public String getRawData();

	/**
	 * Retrieves/Sets the event data.  The data should only be set by the sender of the event.
	*/
	public Node getEventData();

	/**
	 * Retrieves/Sets the event data.  The data should only be set by the sender of the event.
	*/
	public void setEventData( Node value );

	/**
	 * Retrieves the token descriptors for the parser event.
	*/
	public ETList<ITokenDescriptor> getTokenDescriptors();

	/**
	 * Retrieves a token descriptor with a specific type
	*/
	public ITokenDescriptor getTokenDescriptor( String type );

	/**
	 * The name of the file that was parsed.
	*/
	public String getFilename();
}