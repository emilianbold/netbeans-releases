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

import java.util.ArrayList;
import java.util.Iterator;

import org.omg.CORBA.UNKNOWN;

import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.ui.support.helpers.IGUIBlocker.GBK;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

/**
 * @author brettb
 *
 */
public class GUIBlocker implements IGUIBlocker
{

   /**
    *
    */
   public GUIBlocker( int nKind )
   {
      this();
      setKind( nKind );
   }

   /**
    *
    */
   public GUIBlocker()
   {
      super();
      
      // m_Kind already initialized
      // TODO m_DelayedActionBlocker = 0;
      // TODO m_ProjectTreeRefreshBlocker = 0;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.helpers.IGUIBlocker#getKind()
    */
   public int getKind()
   {
      return m_Kind;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.helpers.IGUIBlocker#setKind(long)
    */
   public void setKind(int newVal)
   {
      m_Kind |= newVal;

      clearBlockers();
/* TODO
      if ( (m_Kind & GBK.DIAGRAM_DELAYEDACTION) == GBK.DIAGRAM_DELAYEDACTION)
      {
         m_DelayedActionBlocker = new CDelayedActionBlocker;
      }
*/
      if ( (m_Kind & GBK.DIAGRAM_KEYBOARD) == GBK.DIAGRAM_KEYBOARD)
      {
         m_userInputBlockers.add( new UserInputBlocker(GBK.DIAGRAM_KEYBOARD) );
      }
      if ( (m_Kind & GBK.DIAGRAM_SELECTION) == GBK.DIAGRAM_SELECTION)
      {
         m_userInputBlockers.add( new UserInputBlocker(GBK.DIAGRAM_SELECTION) );
      }
      if ( (m_Kind & GBK.DIAGRAM_MOVEMENT) == GBK.DIAGRAM_MOVEMENT)
      {
         m_userInputBlockers.add( new UserInputBlocker(GBK.DIAGRAM_MOVEMENT) );
      }
      if ( (m_Kind & GBK.DIAGRAM_RESIZE) == GBK.DIAGRAM_RESIZE)
      {
         m_userInputBlockers.add( new UserInputBlocker(GBK.DIAGRAM_RESIZE) );
      }
      if ( (m_Kind & GBK.DIAGRAM_DELETION) == GBK.DIAGRAM_DELETION)
      {
         m_userInputBlockers.add( new UserInputBlocker(GBK.DIAGRAM_DELETION) );
      }
      if ( (m_Kind & GBK.DIAGRAM_CONTAINMENT) == GBK.DIAGRAM_CONTAINMENT)
      {
         m_userInputBlockers.add( new UserInputBlocker(GBK.DIAGRAM_CONTAINMENT) );
      }
      if ( (m_Kind & GBK.DIAGRAM_TRACK_CARS) == GBK.DIAGRAM_TRACK_CARS)
      {
         m_userInputBlockers.add( new UserInputBlocker(GBK.DIAGRAM_TRACK_CARS) );
      }
      if ( (m_Kind & GBK.DIAGRAM_STACKING_COMMANDS) == GBK.DIAGRAM_STACKING_COMMANDS)
      {
         m_userInputBlockers.add( new UserInputBlocker(GBK.DIAGRAM_STACKING_COMMANDS) );
      }
      if ( (m_Kind & GBK.DIAGRAM_LABEL_LAYOUT) == GBK.DIAGRAM_LABEL_LAYOUT)
      {
         m_userInputBlockers.add( new UserInputBlocker(GBK.DIAGRAM_LABEL_LAYOUT) );
      }
      if ( (m_Kind & GBK.DIAGRAM_INVALIDATE) == GBK.DIAGRAM_INVALIDATE)
      {
         m_userInputBlockers.add( new UserInputBlocker(GBK.DIAGRAM_INVALIDATE) );
      }
/* TODO
      if ( (m_Kind & GBK.PROJECT_TREE_DRAWING) == GBK.PROJECT_TREE_DRAWING)
      {
         // Get the trees and block them.
         CComPtr < IAxProjectTreeControl > pProjectTree;
         CComPtr < IAxProjectTreeControl > pDesignCenter;

         CProductHelper.instance().getProjectTree(&pProjectTree);
         CProductHelper.instance().getDesignCenterTree(&pDesignCenter);

         if (pProjectTree)
         {
            m_ProjectTreeUpdateLocker = 0;
            m_ProjectTreeUpdateLocker.coCreateInstance(__uuidof(ProjectTreeUpdateLocker));
            m_ProjectTreeUpdateLocker.lockTree(pProjectTree);
         }
         if (pDesignCenter)
         {
            m_DesignCenterTreeUpdateLocker = 0;
            m_DesignCenterTreeUpdateLocker.coCreateInstance(__uuidof(ProjectTreeUpdateLocker));
            m_DesignCenterTreeUpdateLocker.lockTree(pDesignCenter);
         }
      }
      if ( (m_Kind & GBK.PROJECT_TREE_REFRESH) == GBK.PROJECT_TREE_REFRESH)
      {
         m_ProjectTreeRefreshBlocker = new CProjectTreeRefreshBlocker;
      }
*/
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.helpers.IGUIBlocker#setDiagramContext(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram)
    */
   public void setDiagramContext(IDiagram diagram)
   {
      m_diagram = diagram;
   }
   
   public void setDiagramContext(IDrawingAreaControl control)
   {
      if( control != null )
      {
         m_diagram = control.getDiagram();
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.helpers.IGUIBlocker#getKindIsBlocked(long)
    */
   public boolean getKindIsBlocked(int kinds)
   {
      boolean bAnyBlocked = false;
      
/* TODO
      if ( (kinds & GBK.DIAGRAM_DELAYEDACTION) == GBK.DIAGRAM_DELAYEDACTION)
      {
         bAnyBlocked |= (CDelayedActionBlocker.getIsDisabled() != false);
      }
*/
      if ( (kinds & GBK.DIAGRAM_KEYBOARD) == GBK.DIAGRAM_KEYBOARD)
      {
         bAnyBlocked |= (UserInputBlocker.getIsDisabled(GBK.DIAGRAM_KEYBOARD) != false);
      }
      if ( (kinds & GBK.DIAGRAM_SELECTION) == GBK.DIAGRAM_SELECTION)
      {
         bAnyBlocked |= (UserInputBlocker.getIsDisabled(GBK.DIAGRAM_SELECTION) != false);
      }
      if ( (kinds & GBK.DIAGRAM_MOVEMENT) == GBK.DIAGRAM_MOVEMENT)
      {
         bAnyBlocked |= (UserInputBlocker.getIsDisabled(GBK.DIAGRAM_MOVEMENT) != false);
      }
      if ( (kinds & GBK.DIAGRAM_RESIZE) == GBK.DIAGRAM_RESIZE)
      {
         bAnyBlocked |= (UserInputBlocker.getIsDisabled(GBK.DIAGRAM_RESIZE) != false);
      }
      if ( (kinds & GBK.DIAGRAM_DELETION) == GBK.DIAGRAM_DELETION)
      {
         bAnyBlocked |= (UserInputBlocker.getIsDisabled(GBK.DIAGRAM_DELETION) != false);
      }
      if ( (kinds & GBK.DIAGRAM_CONTAINMENT) == GBK.DIAGRAM_CONTAINMENT)
      {
         bAnyBlocked |= (UserInputBlocker.getIsDisabled(GBK.DIAGRAM_CONTAINMENT) != false);
      }
      if ( (kinds & GBK.DIAGRAM_TRACK_CARS) == GBK.DIAGRAM_TRACK_CARS)
      {
         bAnyBlocked |= (UserInputBlocker.getIsDisabled(GBK.DIAGRAM_TRACK_CARS) != false);
      }
/* TODO
      if ( (kinds & GBK.PROJECT_TREE_DRAWING) == GBK.PROJECT_TREE_DRAWING)
      {
         if (m_ProjectTreeUpdateLocker || m_DesignCenterTreeUpdateLocker)
         {
            bAnyBlocked = true;
         }
      }
      if ( (kinds & GBK.PROJECT_TREE_REFRESH) == GBK.PROJECT_TREE_REFRESH)
      {
         bAnyBlocked |= (CProjectTreeRefreshBlocker.getIsDisabled() != false);
      }
*/
      return bAnyBlocked;
   }


   /**
    * Deletes our blocker and initializes the memory to zero.
    */
   public void clearBlockers()
   {
/* TODO
      delete m_DelayedActionBlocker;
      m_DelayedActionBlocker = 0;
*/

      for (Iterator iter = m_userInputBlockers.iterator(); iter.hasNext();)
      {
         UserInputBlocker blocker = (UserInputBlocker)iter.next();
         blocker.stopBlocking();
         
         iter.remove();
      }

      if (m_diagram != null)
      {
         // Flush any events prior to unlocking the project tree so the user sees the
         // diagram update.
         m_diagram.pumpMessages(false);
      }

/* TODO
      m_DesignCenterTreeUpdateLocker = 0;
      m_ProjectTreeUpdateLocker = 0;

      delete m_ProjectTreeRefreshBlocker;
      m_ProjectTreeRefreshBlocker = 0;
*/
   }


   /// The kind of blockers to create
   protected int m_Kind = GBK.UNKNOWN;

   /// The delayed action blocker - blocks delayed actions
   // TODO protected DelayedActionBlocker m_delayedActionBlocker;

   /// This blocker stops all user input (mousemove, keypress...)
   ArrayList< UserInputBlocker > m_userInputBlockers = new ArrayList< UserInputBlocker >();

   /// Blocks the project tree refreshes
   // TODO ProjectTreeRefreshBlocker * m_ProjectTreeRefreshBlocker;

   /// Blocks the drawing of the project tree
   // TODO CComPtr < IProjectTreeUpdateLocker > m_DesignCenterTreeUpdateLocker;
   // TODO CComPtr < IProjectTreeUpdateLocker > m_ProjectTreeUpdateLocker;

   /// The diagram context
   IDiagram m_diagram = null;
}


