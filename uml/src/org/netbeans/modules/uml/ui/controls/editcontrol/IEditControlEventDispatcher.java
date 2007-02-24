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


package org.netbeans.modules.uml.ui.controls.editcontrol;

import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;

public interface IEditControlEventDispatcher extends IEventDispatcher
{
	/**
	 *
	*/
	public void registerEditCtrlEvents( IEditControlEventSink handler );

	/**
	 *
	*/
	public void revokeEditCtrlSink( IEditControlEventSink handler );

	/**
	 * Creates a new event payload for edit control events
	*/
	public IEditEventPayload createEventPayload();

	/**
	 *
	*/
	public boolean firePreInvalidData( String ErrorData, IEditEventPayload payload );

	/**
	 * 
	*/
	public void fireInvalidData( String ErrorData, IEditEventPayload payload );

	/**
	 * 
	*/
	public boolean firePreOverstrike( boolean bOverstrike, IEditEventPayload payload );

	/**
	 * 
	*/
	public void fireOverstrike( boolean bOverstrike, IEditEventPayload payload );

	/**
	 * 
	*/
	public boolean firePreActivate( IEditControl pControl, IEditEventPayload payload );

	/**
	 * 
	*/
	public void fireActivate( IEditControl pControl, IEditEventPayload payload );

	/**
	 * 
	*/
	public void fireDeactivate( IEditControl pControl, IEditEventPayload payload );

	/**
	 * 
	*/
	public boolean firePreCommit( IEditEventPayload payload );

	/**
	 * 
	*/
	public void firePostCommit( IEditEventPayload payload );

}
