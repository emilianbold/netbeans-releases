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


package org.netbeans.modules.uml.ui.support.viewfactorysupport;


public interface IMouseEvent
{
//	/**
//	 * Initialize this object
//	*/
//	//public long initialize( TSGraphDisplay pDisp, int deviceX, int deviceY, int logicalX, int logicalY, int modKeys, int newTSMouseEventType );
//
//	/**
//	 * The TS TSGraphDisplay object
//	*/
//	//public TSGraphDisplay getGraphDisplay();
//
//	/**
//	 * The TS TSGraphDisplay object
//	*/
//	//public void setGraphDisplay( TSGraphDisplay value );
//
	/**
	 * The device x for this event, set via Initializ()
	*/
	public int getDeviceX();

	/**
	 * The device y for this event, set via Initializ()
	*/
	public int getDeviceY();

	/**
	 * The logical x for this event, set via Initializ()
	*/
	public int getLogicalX();

	/**
	 * The logical y for this event, set via Initializ()
	*/
	public int getLogicalY();

//	/**
//	 * The modkeys for this event
//	*/
//	public int getModKeys();
//
//	/**
//	 * The modkeys for this event
//	*/
//	public void setModKeys( int value );
//
//	/**
//	 * The TS MouseEventType enum
//	*/
//	public int getMouseEventType();
//
//	/**
//	 * The TS MouseEventType enum
//	*/
//	public void setMouseEventType( int value );
//
//	/**
//	 * The client x for this event (relative to node)
//	*/
//	public int getClientX();
//
	/**
	 * The client x for this event (relative to node)
	*/
	public void setClientX( int value );

//	/**
//	 * The client y for this event (relative to node)
//	*/
//	public int getClientY();

	/**
	 * The client y for this event (relative to node)
	*/
	public void setClientY( int value );

//	/**
//	 * The device rectangle during the mouse event
//	*/
//	public IETRect getDeviceRect();
//
//	/**
//	 * The device rectangle during the mouse event
//	*/
//	public void setDeviceRect( IETRect value );

}
