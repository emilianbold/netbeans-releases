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



package org.netbeans.modules.uml.ui.swing.drawingarea.cursors;

import java.awt.Cursor;
import java.awt.Point;

/**
 * @author KevinM
 *
 * Cursor Used by the Partition Zones Object
 */
public class ETHorzDragCursor extends ETCustomCursor {
	/**
	  * A static method used to retrieve the cursor.  The image and hot spot are
	  * updated.
	  */
	 public static Cursor getCursor() {
		if (m_customCursor == null)
		{
		   ETCustomCursor instance = new ETHorzDragCursor();
		   m_customCursor = instance.createCursor();
		}
		return m_customCursor;
	 }

	 /* (non-Javadoc)
	  * @see org.netbeans.modules.uml.ui.swing.drawingarea.cursors.ETCustomCursor#getCursorName()
	  */
	 protected String getCursorName() {
		return "HorzBarDragCursor";
	 }

	 /**
	  * Retrieves the cursors hot spot.
	  */
	 protected Point getHotSpot()
	 {
		return new Point(7, 7);
	 }
   
	 /** The static instance of the cursor. */
	 protected static Cursor m_customCursor = null;

}
