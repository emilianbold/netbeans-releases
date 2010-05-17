/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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


//*****************************************************************************
// Workfile:: UserInputBlocker.java
// Desc     :  Implementation of class UserInputBlocker.java
//             This class blocks any input from the user (mouse down, keyboard).  It's designed
//             to be used during large processes like create diagram from selected
//
// Revision:: 4
//   Author:: KevinM                                                         
//     Date:: Feb 17, 2004 3:15:43 PM                                                                      
//  Modtime:: 4/14/2004 10:52:54 AM 3:15:43 PM                                              
//
//*****************************************************************************  
package org.netbeans.modules.uml.ui.support.helpers;

import org.netbeans.modules.uml.ui.support.helpers.IGUIBlocker.GBK;

/**
 * @author brettb
 *
 */
public class UserInputBlocker
{
   /**
    * 
    */
   UserInputBlocker( int nKind )
   {
      switch (nKind)
      {
      case GBK.DIAGRAM_KEYBOARD :            m_numKeyboardInstances++; break;
      case GBK.DIAGRAM_SELECTION :           m_numSelectionInstances++; break;
      case GBK.DIAGRAM_MOVEMENT :            m_numMovementInstances++; break;
      case GBK.DIAGRAM_RESIZE :              m_numResizeInstances++; break;
      case GBK.DIAGRAM_DELETION :            m_numDeletionInstances++; break;
      case GBK.DIAGRAM_CONTAINMENT :         m_numContainmentInstances++; break;
      case GBK.DIAGRAM_TRACK_CARS :          m_numTrackCarsInstances++; break;
      case GBK.DIAGRAM_STACKING_COMMANDS :   m_numStackingCommandInstances++; break;
      case GBK.DIAGRAM_LABEL_LAYOUT :        m_numLabelLayoutInstances++; break;
      case GBK.DIAGRAM_INVALIDATE :          m_numInvalidateInstances++; break;
      }
      
      m_thisKind = nKind;
   }

   void stopBlocking()
   {
      if( m_thisKind != GBK.UNKNOWN )
      {
         switch ( m_thisKind )
         {
         case GBK.DIAGRAM_KEYBOARD :            m_numKeyboardInstances--; break;
         case GBK.DIAGRAM_SELECTION :           m_numSelectionInstances--; break;
         case GBK.DIAGRAM_MOVEMENT :            m_numMovementInstances--; break;
         case GBK.DIAGRAM_RESIZE :              m_numResizeInstances--; break;
         case GBK.DIAGRAM_DELETION :            m_numDeletionInstances--; break;
         case GBK.DIAGRAM_CONTAINMENT :         m_numContainmentInstances--; break;
         case GBK.DIAGRAM_TRACK_CARS :          m_numTrackCarsInstances--; break;
         case GBK.DIAGRAM_STACKING_COMMANDS :   m_numStackingCommandInstances--; break;
         case GBK.DIAGRAM_LABEL_LAYOUT :        m_numLabelLayoutInstances--; break;
         case GBK.DIAGRAM_INVALIDATE :          m_numInvalidateInstances--; break;
         }
         
         m_thisKind = GBK.UNKNOWN;
      }
   }


   /// Returns true if events should not be handled right now.
   public static boolean getIsDisabled( int nKind )
   {
      boolean bIsDisabled = false;

      switch (nKind)
      {
      case GBK.DIAGRAM_KEYBOARD :    bIsDisabled =          (m_numKeyboardInstances > 0)?true:false; break;
      case GBK.DIAGRAM_SELECTION :   bIsDisabled =          (m_numSelectionInstances > 0)?true:false; break;
      case GBK.DIAGRAM_MOVEMENT :    bIsDisabled =          (m_numMovementInstances > 0)?true:false; break;
      case GBK.DIAGRAM_RESIZE :      bIsDisabled =          (m_numResizeInstances > 0)?true:false; break;
      case GBK.DIAGRAM_DELETION :    bIsDisabled =          (m_numDeletionInstances > 0)?true:false; break;
      case GBK.DIAGRAM_CONTAINMENT : bIsDisabled =          (m_numContainmentInstances > 0)?true:false; break;
      case GBK.DIAGRAM_TRACK_CARS :  bIsDisabled =          (m_numTrackCarsInstances > 0)?true:false; break;
      case GBK.DIAGRAM_STACKING_COMMANDS :  bIsDisabled =   (m_numStackingCommandInstances > 0)?true:false; break;
      case GBK.DIAGRAM_LABEL_LAYOUT :  bIsDisabled =        (m_numLabelLayoutInstances > 0)?true:false; break;
      case GBK.DIAGRAM_INVALIDATE :  bIsDisabled =          (m_numInvalidateInstances > 0)?true:false; break;
      }

      return bIsDisabled;
   }
   
   public static boolean getOneIsDisabled( int nKind )
   {
      boolean bIsDisabled = false;

      if ( (nKind & GBK.DIAGRAM_DELAYEDACTION) == GBK.DIAGRAM_DELAYEDACTION)
      {
         bIsDisabled = UserInputBlocker.getIsDisabled(GBK.DIAGRAM_DELAYEDACTION);
      }
      if (!bIsDisabled && (nKind & GBK.DIAGRAM_CONTAINMENT) == GBK.DIAGRAM_CONTAINMENT)
      {
         bIsDisabled = UserInputBlocker.getIsDisabled(GBK.DIAGRAM_CONTAINMENT);
      }
      if (!bIsDisabled && (nKind & GBK.PROJECT_TREE_DRAWING) == GBK.PROJECT_TREE_DRAWING)
      {
         bIsDisabled = UserInputBlocker.getIsDisabled(GBK.PROJECT_TREE_DRAWING);
      }
      if (!bIsDisabled && (nKind & GBK.PROJECT_TREE_REFRESH) == GBK.PROJECT_TREE_REFRESH)
      {
         bIsDisabled = UserInputBlocker.getIsDisabled(GBK.PROJECT_TREE_REFRESH);
      }
      if (!bIsDisabled && (nKind & GBK.DIAGRAM_KEYBOARD) == GBK.DIAGRAM_KEYBOARD)
      {
         bIsDisabled = UserInputBlocker.getIsDisabled(GBK.DIAGRAM_KEYBOARD);
      }
      if (!bIsDisabled && (nKind & GBK.DIAGRAM_SELECTION) == GBK.DIAGRAM_SELECTION)
      {
         bIsDisabled = UserInputBlocker.getIsDisabled(GBK.DIAGRAM_SELECTION);
      }
      if (!bIsDisabled && (nKind & GBK.DIAGRAM_MOVEMENT) == GBK.DIAGRAM_MOVEMENT)
      {
         bIsDisabled = UserInputBlocker.getIsDisabled(GBK.DIAGRAM_MOVEMENT);
      }
      if (!bIsDisabled && (nKind & GBK.DIAGRAM_RESIZE) == GBK.DIAGRAM_RESIZE)
      {
         bIsDisabled = UserInputBlocker.getIsDisabled(GBK.DIAGRAM_RESIZE);
      }
      if (!bIsDisabled && (nKind & GBK.DIAGRAM_DELETION) == GBK.DIAGRAM_DELETION)
      {
         bIsDisabled = UserInputBlocker.getIsDisabled(GBK.DIAGRAM_DELETION);
      }
      if (!bIsDisabled && (nKind & GBK.DIAGRAM_TRACK_CARS) == GBK.DIAGRAM_TRACK_CARS)
      {
         bIsDisabled = UserInputBlocker.getIsDisabled(GBK.DIAGRAM_TRACK_CARS);
      }
      if (!bIsDisabled && (nKind & GBK.DIAGRAM_STACKING_COMMANDS) == GBK.DIAGRAM_STACKING_COMMANDS)
      {
         bIsDisabled = UserInputBlocker.getIsDisabled(GBK.DIAGRAM_STACKING_COMMANDS);
      }

      return bIsDisabled;
   }

   
   private static long m_numKeyboardInstances = 0;          // GBK.DIAGRAM_KEYBOARD
   private static long m_numSelectionInstances = 0;         // GBK.DIAGRAM_SELECTION
   private static long m_numMovementInstances = 0;          // GBK.DIAGRAM_MOVEMENT
   private static long m_numResizeInstances = 0;            // GBK.DIAGRAM_RESIZE
   private static long m_numDeletionInstances = 0;          // GBK.DIAGRAM_DELETION
   private static long m_numContainmentInstances = 0;       // GBK.DIAGRAM_CONTAINMENT
   private static long m_numTrackCarsInstances = 0;         // GBK.DIAGRAM_TRACK_CARS
   private static long m_numStackingCommandInstances = 0;   // GBK.DIAGRAM_STACKING_COMMANDS
   private static long m_numLabelLayoutInstances = 0;       // GBK.DIAGRAM_LABEL_LAYOUT
   private static long m_numInvalidateInstances = 0;        // GBK.DIAGRAM_INVALIDATE
   
   private int m_thisKind = GBK.UNKNOWN;
}


