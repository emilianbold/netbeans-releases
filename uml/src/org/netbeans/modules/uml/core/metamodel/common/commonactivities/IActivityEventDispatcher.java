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


package org.netbeans.modules.uml.core.metamodel.common.commonactivities;

import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;

public interface IActivityEventDispatcher extends IEventDispatcher
{
	/**
	 * Registers an event sink to handle ActivityEdge events.
	*/
	public void registerForActivityEdgeEvents( IActivityEdgeEventsSink handler );

	/**
	 * Removes a sink listening for ActivityEdge events.
	*/
	public void revokeActivityEdgeSink( IActivityEdgeEventsSink handler );

	/**
	 * Fired whenever the passed in ActivityEdge's weight property is about to change.
	*/
	public boolean firePreWeightModified( IActivityEdge pEdge, String newValue, IEventPayload payLoad );

	/**
	 * Fired whenever the passed in ActivityEdge's weight has been changed.
	*/
	public void fireWeightModified( IActivityEdge pEdge, IEventPayload payLoad );

	/**
	 * Fired whenever the passed in ActivityEdge's weight property is about to change.
	*/
	public boolean firePreGuardModified( IActivityEdge pEdge, String newValue, IEventPayload payLoad );

	/**
	 * Fired whenever the passed in ActivityEdge's weight has been changed.
	*/
	public void fireGuardModified( IActivityEdge pEdge, IEventPayload payLoad );
}
