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

public interface ICoreBusyCtrl
{
	/**
	 * Begins the busy state.
	*/
	public long begin();

	/**
	 * Begins the busy state with a message.
	*/
	public long begin( String sMsg );

	/**
	 * Begins the busy state with a message.
	*/
	public long begin( int hInstance, int nID );

	/**
	 * Ends the busy state.
	*/
	public long end();

	/**
	 * If we are in a busy state then this routine changes the text.
	*/
	public long updateIfActive( int hInstance, int nID );

	/**
	 * Begins the progress control (thermometer)
	*/
	public long beginProgress( String message, int nUpper, int nInitialPos );

	/**
	 * Ends the progress control (thermometer)
	*/
	public long endProgress();

	/**
	 * Sets the current position - leaves the text the same.
	*/
	public long setPos( int nPos );

	/**
	 * Sets the current position and text if it's a thermometer
	*/
	public long setPos( String message, int nPos );

}
