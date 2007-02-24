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

/**
 *
 */
public interface IUMLMessagingHelper
{
	/// Send a message
	public void sendMessage( int messageType,String message);
	public void sendMessage( int messageType, int resourceHandle, int id );

	/// Send a message data out the interface
	public void sendMessage( IMessageData pMessageData);

	/// Convinience functions in case you don't want to remember the enumerations
	public void sendCriticalMessage(String message);
	public void sendErrorMessage(String message);
	public void sendWarningMessage(String message);
	public void sendInfoMessage(String message);
	public void sendDebugMessage(String message);
/*
	public void sendCriticalMessage( int resourceHandle, int id);
	public void sendErrorMessage( int resourceHandle, int id );
	public void sendWarningMessage( int resourceHandle, int id );
	public void sendInfoMessage( int resourceHandle, int id );
	public void sendDebugMessage( int resourceHandle, int id );
*/
	public IMessageService getMessageService();
}



