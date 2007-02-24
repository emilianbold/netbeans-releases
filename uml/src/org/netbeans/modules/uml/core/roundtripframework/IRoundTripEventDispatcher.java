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

package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;

public interface IRoundTripEventDispatcher extends IEventDispatcher
{
	/**
	 * Registers a sink for the events specified in the IRoundTripAttributeEventsSink interface.
	*/
	public void registerForRoundTripAttributeEvents( IRoundTripAttributeEventsSink handler, String Language );

	/**
	 * Revokes the handler identified with the passed in cookie.
	*/
	public void revokeRoundTripAttributeSink( IRoundTripAttributeEventsSink handler );

        /**
	 * Registers a sink for the events specified in the IRoundTripAttributeEventsSink interface.
	*/
	public void registerForRoundTripEnumLiteralEvents( IRoundTripEnumLiteralEventsSink handler, String Language );

	/**
	 * Revokes the handler identified with the passed in cookie.
	*/
	public void revokeRoundTripEnumLiteralSink( IRoundTripEnumLiteralEventsSink handler );
        
	/**
	 * Registers a sink for the events specified in the IRoundTripOperationEventsSink interface.
	*/
	public void registerForRoundTripOperationEvents( IRoundTripOperationEventsSink handler, String Language );

	/**
	 * Revokes the handler identified with the passed in cookie.
	*/
	public void revokeRoundTripOperationSink( IRoundTripOperationEventsSink handler );

	/**
	 * Registers a sink for the events specified in the IRoundTripClassEventsSink interface.
	*/
	public void registerForRoundTripClassEvents( IRoundTripClassEventsSink handler, String Language );

	/**
	 * Revokes the handler identified with the passed in cookie.
	*/
	public void revokeRoundTripClassSink( IRoundTripClassEventsSink handler );
        
        /**
	 * Registers a sink for the events specified in the IRoundTripClassEventsSink interface.
	*/
	public void registerForRoundTripEnumEvents( IRoundTripEnumEventsSink handler, String Language );

	/**
	 * Revokes the handler identified with the passed in cookie.
	*/
	public void revokeRoundTripEnumSink( IRoundTripEnumEventsSink handler );

	/**
	 * Registers a sink for the events specified in the IRoundTripPackageEventsSink interface.
	*/
	public void registerForRoundTripPackageEvents( IRoundTripPackageEventsSink handler, String Language );

	/**
	 * Revokes the handler identified with the passed in cookie.
	*/
	public void revokeRoundTripPackageSink( IRoundTripPackageEventsSink handler );

	/**
	 * Registers a sink for the events specified in the IRoundTripRelationEventsSink interface.
	*/
	public void registerForRoundTripRelationEvents( IRoundTripRelationEventsSink handler, String Language );

	/**
	 * Revokes the handler identified with the passed in cookie.
	*/
	public void revokeRoundTripRelationSink( IRoundTripRelationEventsSink handler );

	/**
	 * Registers a sink for the events specified in the RequestProcessorInitEvents interface.
	*/
	public void registerForRequestProcessorInitEvents( IRequestProcessorInitEventsSink handler );

	/**
	 * Revokes the handler identified with the passed in cookie.
	*/
	public void revokeRequestProcessorInitEvents(IRequestProcessorInitEventsSink handler );

	/**
	 * Fired after the RequestProcessor has filtered its changes, but before actual processing of the requests has been done.
	*/
	public boolean firePreAttributeChangeRequest( IChangeRequest req, IEventPayload Payload );

	/**
	 * Fired after the RequestProcessor has filtered its changes, but before actual processing of the requests has been done.
	*/
	public void fireAttributeChangeRequest( IChangeRequest req, IEventPayload Payload );
        
        /**
	 * Fired after the RequestProcessor has filtered its changes, but before actual processing of the requests has been done.
	*/
	public boolean firePreEnumLiteralChangeRequest( IChangeRequest req, IEventPayload Payload );

	/**
	 * Fired after the RequestProcessor has filtered its changes, but before actual processing of the requests has been done.
	*/
	public void fireEnumLiteralChangeRequest( IChangeRequest req, IEventPayload Payload );

	/**
	 * Fired after the RequestProcessor has filtered its changes, but before actual processing of the requests has been done.
	*/
	public boolean firePreOperationChangeRequest( IChangeRequest req, IEventPayload Payload );

	/**
	 * Fired after the RequestProcessor has filtered its changes, but before actual processing of the requests has been done.
	*/
	public void fireOperationChangeRequest( IChangeRequest req, IEventPayload Payload );

	/**
	 * Fired after the RequestProcessor has filtered its changes, but before actual processing of the requests has been done.
	*/
	public boolean firePreClassChangeRequest( IChangeRequest req, IEventPayload Payload );

	/**
	 * Fired after the RequestProcessor has filtered its changes, but before actual processing of the requests has been done.
	*/
	public void fireClassChangeRequest( IChangeRequest req, IEventPayload Payload );
        
        /**
	 * Fired after the RequestProcessor has filtered its changes, but before actual processing of the requests has been done.
	*/
	public boolean firePreEnumerationChangeRequest( IChangeRequest req, IEventPayload Payload );

	/**
	 * Fired after the RequestProcessor has filtered its changes, but before actual processing of the requests has been done.
	*/
	public void fireEnumerationChangeRequest( IChangeRequest req, IEventPayload Payload );

	/**
	 * Fired after the RequestProcessor has filtered its changes, but before actual processing of the requests has been done.
	*/
	public boolean firePrePackageChangeRequest( IChangeRequest req, IEventPayload Payload );

	/**
	 * Fired after the RequestProcessor has filtered its changes, but before actual processing of the requests has been done.
	*/
	public void firePackageChangeRequest( IChangeRequest req, IEventPayload Payload );

	/**
	 * Fired after the RequestProcessor has filtered its changes, but before actual processing of the requests has been done.
	*/
	public boolean firePreRelationChangeRequest( IChangeRequest req, IEventPayload Payload );

	/**
	 * Fired after the RequestProcessor has filtered its changes, but before actual processing of the requests has been done.
	*/
	public void fireRelationChangeRequest( IChangeRequest req, IEventPayload Payload );

	/**
	 * Fired when the RequestProcessor is completely initialized.
	*/
	public boolean firePreInitialized( String proc, IEventPayload Payload );

	/**
	 * Fired when the RequestProcessor is completely initialized.
	*/
	public void fireInitialized( IRequestProcessor proc, IEventPayload Payload );

}
