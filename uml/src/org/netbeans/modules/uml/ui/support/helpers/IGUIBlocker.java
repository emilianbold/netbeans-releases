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


package org.netbeans.modules.uml.ui.support.helpers;

import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;

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

   /// Examines each of the indicated kinds and returns true if any are blocked 
   public boolean getKindIsBlocked( int kinds );
}


