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
//import com.tomsawyer.editor.TSEWindowState;
import com.tomsawyer.editor.TSEWindowTool;

/**
 * @author brettb
 *
 */
public class SetCursorEvent implements ISetCursorEvent
{
   /**
    *
    */
//   public SetCursorEvent( MouseEvent event, TSEWindowState state )
   public SetCursorEvent( MouseEvent event, TSEWindowTool state )
   {
      m_event = event;
      m_state = state;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ISetCursorEvent#getGraphDisplay()
    */
   public TSEGraphWindow getGraphDisplay()
   {
      return m_state.getGraphWindow();
   }

   /**
    * The location of the mouse in the device, not available in C++
   */
   public Point getWinClientLocation()
   {
      return m_event.getPoint();
   }
   
   /**
    * Sets the cursor for the current state, not necessary for C++ code
    */
   public void setCursor( Cursor cursor )
   {
      m_state.setCursor( cursor );
   }


   private MouseEvent m_event;
//   private TSEWindowState m_state;
   private TSEWindowTool m_state;
}


