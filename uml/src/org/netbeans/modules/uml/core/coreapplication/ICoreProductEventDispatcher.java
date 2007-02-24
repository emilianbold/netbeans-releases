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


package org.netbeans.modules.uml.core.coreapplication;

import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;

public interface ICoreProductEventDispatcher extends IEventDispatcher
{
	/**
	 * Registers a sink for the events specified in the ICoreProductInitEventsSink interface
	*/
	public void registerForInitEvents( ICoreProductInitEventsSink handler );

	/**
	 * Revokes the handler identified with the passed in cookie
	*/
	public void revokeInitSink( ICoreProductInitEventsSink handler );

	/**
	 * Fired before initialization of the product commences.
	*/
	public boolean fireCoreProductPreInit( ICoreProduct prod, IEventPayload payload );

	/**
	 * Fired after the CoreProduct has been fully initialized
	*/
	public void fireCoreProductInitialized( ICoreProduct prod, IEventPayload payload );

	/**
	 * Fired before the product quits.
	*/
	public void fireCoreProductPreQuit( ICoreProduct prod, IEventPayload payload );

	/**
	 * Fired before the product saves all modified data.
	*/
	public boolean fireCoreProductPreSaved( ICoreProduct prod, IEventPayload payload );

	/**
	 * Fired after the CoreProduct has successfully saved all modified data.
	*/
	public void fireCoreProductSaved( ICoreProduct prod, IEventPayload payload );

}
