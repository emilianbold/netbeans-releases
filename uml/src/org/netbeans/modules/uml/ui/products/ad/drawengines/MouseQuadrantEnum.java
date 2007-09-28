/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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


