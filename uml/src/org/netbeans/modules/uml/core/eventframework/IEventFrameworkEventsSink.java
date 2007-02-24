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


package org.netbeans.modules.uml.core.eventframework;

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IEventFrameworkEventsSink
{
	/**
	 * Fired before a new event context is pushed on the dispatcher.
	*/
	public void onPreEventContextPushed( IEventContext pContext, IResultCell pCell );

	/**
	 * Fired after a new event context is pushed on the dispatcher.
	*/
	public void onEventContextPushed( IEventContext pContext, IResultCell pCell );

	/**
	 * Fired before an event context is popped off the dispatcher.
	*/
	public void onPreEventContextPopped( IEventContext pContext, IResultCell pCell );

	/**
	 * Fired after an event context is popped off the dispatcher.
	*/
	public void onEventContextPopped( IEventContext pContext, IResultCell pCell );

	/**
	 * Fired after it has been discovered that an event dispatch process has been aborted.
	*/
	public void onEventDispatchCancelled( ETList<Object> pListeners, Object listenerWhoCancelled, IResultCell pCell );

	
}
