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

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;

import com.tomsawyer.editor.TSEGraphWindow;

public interface ISetCursorEvent
{
	/**
	 * Initialize this object
	*/
	//public long initialize( TSGraphDisplay pDisp, int hWnd, int hitTest, int message );

   /**
    * The TS TSGraphDisplay object
   */
   public TSEGraphWindow getGraphDisplay();

   /**
    * The location of the mouse in the device, not available in C++
   */
   public Point getWinClientLocation();
   
   /**
    * Sets the cursor for the current state, not necessary for C++ code
    */
   public void setCursor( Cursor cursor );

// TODO so far we don't need these in the java side
//	/**
//	 * The TS TSGraphDisplay object
//	*/
//	//public void setGraphDisplay( TSGraphDisplay value );
//
//	/**
//	 * The HWND for this event
//	*/
//	public int getHWnd();
//
//	/**
//	 * The HWND for this event
//	*/
//	public void setHWnd( int value );
//
//	/**
//	 * The TS hit test enum
//	*/
//	public int getHitTest();
//
//	/**
//	 * The TS hit test enum
//	*/
//	public void setHitTest( int value );
//
//	/**
//	 * The TS message type enum.
//	*/
//	public int getMessage();
//
//	/**
//	 * The TS message type enum.
//	*/
//	public void setMessage( int value );

}
