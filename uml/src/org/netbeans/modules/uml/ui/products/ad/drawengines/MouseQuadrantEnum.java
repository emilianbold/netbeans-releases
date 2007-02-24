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



package org.netbeans.modules.uml.ui.products.ad.drawengines;

import com.tomsawyer.diagramming.TSResizeControl;
//import com.tomsawyer.editor.TSEWindowState;
import com.tomsawyer.editor.TSEWindowTool;
//import com.tomsawyer.editor.state.TSEResizeGraphObjectState;
import com.tomsawyer.editor.tool.TSEResizeGraphObjectTool;

/**
 * @author nickl
 *
 */
public class MouseQuadrantEnum
{
	public static final int MQ_UNKNOWN = 0;
	public static final int	MQ_TOP = 1;
	public static final int	MQ_LEFT = 2;
	public static final int	MQ_BOTTOM = 4;
	public static final int	MQ_RIGHT = 8;
	public static final int	MQ_TOPLEFT =     MQ_TOP    | MQ_LEFT;
	public static final int	MQ_TOPRIGHT =    MQ_TOP    | MQ_RIGHT;
	public static final int	MQ_BOTTOMLEFT =  MQ_BOTTOM | MQ_LEFT;
	public static final int	MQ_BOTTOMRIGHT = MQ_BOTTOM | MQ_RIGHT;

   /**
    * This routine converts the TS state's grapple handle location into
    * our mouse quadrant value.
    *  
    * @param state TS state
    * @return The quadrant value associated with the TS state's grapple value
    */
//   public static int getQuadrant( TSEWindowState state )
   public static int getQuadrant( TSEWindowTool state )
   {
      int quadrant = MQ_UNKNOWN;

//      if (state instanceof TSEResizeGraphObjectState)
      if (state instanceof TSEResizeGraphObjectTool)
      {
//         TSEResizeGraphObjectState resizeState = (TSEResizeGraphObjectState)state;
         TSEResizeGraphObjectTool resizeState = (TSEResizeGraphObjectTool)state;
         
         int grapple = resizeState.getGrapple();
         switch( grapple )
         {
            case TSResizeControl.GRAPPLE_N:
               quadrant = MQ_TOP;
               break;
               
            case TSResizeControl.GRAPPLE_S:
               quadrant = MQ_BOTTOM;
               break;
               
            case TSResizeControl.GRAPPLE_E:
               quadrant = MQ_RIGHT;
               break;
               
            case TSResizeControl.GRAPPLE_W:
               quadrant = MQ_LEFT;
               break;
               
            case TSResizeControl.GRAPPLE_NE:
               quadrant = MQ_TOPRIGHT;
               break;
               
            case TSResizeControl.GRAPPLE_NW:
               quadrant = MQ_TOPLEFT;
               break;
               
            case TSResizeControl.GRAPPLE_SE:
               quadrant = MQ_BOTTOMRIGHT;
               break;
               
            case TSResizeControl.GRAPPLE_SW:
            quadrant = MQ_BOTTOMLEFT;
               break;
         }
      }
      
      return quadrant;
   }
}


