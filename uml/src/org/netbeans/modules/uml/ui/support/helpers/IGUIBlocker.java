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


package org.netbeans.modules.uml.ui.support.helpers;

import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

/**
 * @author brettb
 *
 */
public interface IGUIBlocker
{
   public class GBK // GUIBlockerKind
   {
      /// Blocks processing of the delayed actions
      public static final int UNKNOWN = 0;
      /// Blocks processing of the delayed actions
      public static final int DIAGRAM_DELAYEDACTION = 1;
      /// Blocks all containment based operations
      public static final int DIAGRAM_CONTAINMENT = 2;
      /// Blocks the diagram from drawing
      public static final int PROJECT_TREE_DRAWING = 4;
      /// Blocks project tree refreshes
      public static final int PROJECT_TREE_REFRESH = 8;
      /// Blocks all keyboard input to the diagram
      public static final int DIAGRAM_KEYBOARD = 16;
      /// Blocks all selection/unselection operations
      public static final int DIAGRAM_SELECTION = 32;
      /// Blocks all movement operations
      public static final int DIAGRAM_MOVEMENT = 64;
      /// Blocks all resizing operations
      public static final int DIAGRAM_RESIZE = 128;
      /// Blocks the deletion of graph objects
      public static final int DIAGRAM_DELETION = 256;
      /// Blocks the track cars
      public static final int DIAGRAM_TRACK_CARS = 512;
      /// Blocks any stacking commands to the drawing area
      public static final int DIAGRAM_STACKING_COMMANDS = 1024;
      /// Blocks any laying out of labels
      public static final int DIAGRAM_LABEL_LAYOUT = 2048;
      /// Blocks any invalidates
      public static final int DIAGRAM_INVALIDATE = 4096;
   }

   /// The kind of the block.  OR up the GUIBlockerKinds 
   public int getKind();
   /// The kind of the block.  OR up the GUIBlockerKinds
   public void setKind( int newVal );
   
   /// decrements the counts on the previously specified block kinds
   public void clearBlockers();
      
   /// Some blockers will pump messages on this diagram before unblocking.  This allows the user to see the diagram changes immediately 
   public void setDiagramContext(IDiagram diagram);
   public void setDiagramContext(IDrawingAreaControl control);

   /// Examines each of the indicated kinds and returns true if any are blocked 
   public boolean getKindIsBlocked( int kinds );
}


