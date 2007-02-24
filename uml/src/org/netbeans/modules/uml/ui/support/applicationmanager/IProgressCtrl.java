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


package org.netbeans.modules.uml.ui.support.applicationmanager;

public interface IProgressCtrl
{
	/**
	 * Begins the progress control
	*/
	public long beginProgress( String message, int nLower, int nUpper, int nInitialPos );

	/**
	 * Ends the progress control
	*/
	public long endProgress();

	/**
	 * Returns the current range
	*/
	public long getRange( int pLower, int pUpper );

	/**
	 * Returns the current position
	*/
	public long getPos( int pPos );

	/**
	 * Sets the current position - leaves the text the same.
	*/
	public long setPos( int nPos );

	/**
	 * Sets the current position and text
	*/
	public long setPos2( String message, int nPos );

	/**
	 * Begins the busy state for the progress control. Use for busy state where you don't know how long the process will last.
	*/
	public long beginBusyState( String message );

	/**
	 * Ends the busy state for the progress control. Use for busy state where you don't know how long the process will last.
	*/
	public long endBusyState();

}
