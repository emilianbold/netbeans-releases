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
