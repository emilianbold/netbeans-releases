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

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

public interface IEditControlEventSink
{
	/**
	 * Fired when data not consistent with the selected mask is passed
	*/
	public void onPreInvalidData( String ErrorData, IResultCell cell );

	/**
	 * Fired when data not consistent with the selected mask is passed
	*/
	public void onInvalidData( String ErrorData, IResultCell cell );

	/**
	 * Fired when user toggles Insert/Overstrike mode via the Insert key
	*/
	public void onPreOverstrike( boolean bOverstrike, IResultCell cell );

	/**
	 * Fired when user toggles Insert/Overstrike mode via the Insert key
	*/
	public void onOverstrike( boolean bOverstrike, IResultCell cell );

	/**
	 * The control is about to gain focus
	*/
	public void onPreActivate( IEditControl pControl, IResultCell cell );

	/**
	 * The control has gained focus
	*/
	public void onActivate( IEditControl pControl, IResultCell cell );

	/**
	 * The control has lost focus
	*/
	public void onDeactivate( IEditControl pControl, IResultCell cell );

	/**
	 * Sets an AxEditEvents object as owner of this event sink. Events will be routed to the owner
	*/
	public void setEventOwner( /* long */ int pOwner );

	/**
	 * Model element data is about to be saved.
	*/
	public void onPreCommit( IResultCell cell );

	/**
	 * Model element data has been saved.
	*/
	public void onPostCommit( IResultCell cell );

}
