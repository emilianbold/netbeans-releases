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


package org.netbeans.modules.uml.ui.products.ad.compartments;

import java.util.ListIterator;

import org.netbeans.modules.uml.core.support.Debug;
import org.netbeans.modules.uml.core.support.umlsupport.ETDeviceRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADNodeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETTransform;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IMouseEvent;

public class CollapsibleCompartmentDividers extends CompartmentDividers
{
   public static final int DIVIDERHEIGHT = 2;
   private ETDrawEngine m_pEngineImpl = null;

   public CollapsibleCompartmentDividers(ETDrawEngine pEngineImpl, int nPenStyle)
   {
      super(pEngineImpl, nPenStyle);
      m_pEngineImpl = pEngineImpl;
   }

   /**
    * Handle the left mouse double click event, if the event occured on a divider
    */
   public boolean handleLeftMouseButtonDoubleClick(IMouseEvent pMouseEvent)
   {
      // this is the relative position within the node (topleft = 0,0)
      final IETPoint ptMouseLoc = getTransform().getWinClientMouseLocation(pMouseEvent);

      final DividerInfo pDividerInfo = determineMouseEventDivider(ptMouseLoc);

      if (pDividerInfo != null)
      {
         // can it be collapsed?
         if ((pDividerInfo != null) && (pDividerInfo.m_bCollapsible))
         {
            boolean vbIsCollapsed = false;

            vbIsCollapsed = pDividerInfo.m_nextCompartment.getCollapsed();
            pDividerInfo.m_nextCompartment.setCollapsed(!vbIsCollapsed);

            if (getEngine() != null)
            {
               getEngine().invalidate();
            }
         }
      }

      return (pDividerInfo != null);
   }

   /**
    * Handle the left mouse begin drag event, if the event occured on a divider
    */
   public boolean handleLeftMouseBeginDrag(IETPoint pETStartPos)
   {
      boolean bHandled = false;

      DividerInfo prevDividerInfo = null;

      DividerInfo pDividerInfo = null;
      //      long x = 0;
      //      long y = 0;

      //TODO Watch out getPoints(x, y) not ported properly
      //      pETStartPos.getPoints(x, y);

      IETPoint tempPoint = new ETPoint(pETStartPos.asPoint());

      IETPoint ptMouseLoc = getTransform().getTSAbsoluteToWinScaledOwner(tempPoint);

      ListIterator iter = this.getDividers().listIterator();

      while (iter.hasNext())
      {
         DividerInfo divInfo = (DividerInfo)iter.next();
         Debug.assertTrue(divInfo != null);
         if (divInfo.m_rcDivider.contains(ptMouseLoc))
         {
            pDividerInfo = divInfo;
            prevDividerInfo = (DividerInfo)iter.previous();
            break;
         }

      }

      //      DIVIDERLIST : : iterator it = DetermineMouseEventDividerItr(ptMouseLoc);
      //
      //      if (it != m_Dividers.end())
      //      {
      //         if (it - > bResizeable)
      //         {
      //            pDividerInfo = & (* it);
      //         }
      //
      //         bHandled = true;
      //      }

      if (pDividerInfo.m_bResizeable)
      {
         bHandled = true;
      }

      if (pDividerInfo != null)
      {
         // dragging 
         if (pDividerInfo.m_bResizeable && (m_pEngineImpl != null))
         {
            IDrawEngine cpEngine = getEngine();

            IADNodeDrawEngine cpNodeDrawEngine = null;

            if (cpEngine instanceof IADNodeDrawEngine)
            {
               cpNodeDrawEngine = (IADNodeDrawEngine)cpEngine;
            }

            if (cpNodeDrawEngine != null)
            {
               IETRect rectBounds = ETDeviceRect.ensureDeviceRect(getEngine().getBoundingRect());

               //TODO
               //					  // set upper rectBounds to previous divider, if present
               //					  if (it != m_Dividers.begin())
               //					  {
               //						  it--;
               //						  rectBounds.top += (* it).rcDivider.CenterPoint().y;
               //					  }

               if (prevDividerInfo != null)
               {
                  rectBounds.setTop(prevDividerInfo.m_rcDivider.getCenterPoint().y);

               }

               ICompartment cpCompartment = pDividerInfo.m_prevCompartment;

               if (cpCompartment != null)
               {
                  // The bottom of the bounding rect is limited by the size of the next compartment
                  ICompartment cpNextCompartment = pDividerInfo.m_nextCompartment;

                  if (cpNextCompartment != null)
                  {
                     IETSize size = cpNextCompartment.getOptimumSize(false);
                     rectBounds.setBottom((rectBounds.getTop() + pDividerInfo.m_rcDivider.getCenterPoint().y) + size.getHeight());
                  }

                  ETTransform transform = getTransform();

                  transform.setWinClientRectangle(rectBounds);

                  IETRect rectTSAbsolute = transform.getTSAbsoluteRect();

                  //                 IETRect cpETBounds = RectConversions.newETRect(rectTSAbsolute);

                  cpNodeDrawEngine.launchNodeTool(pETStartPos, cpCompartment, rectTSAbsolute /*cpETBounds*/
                  );
               }
            }
         }
      }

      // turn off TS handling of the mouse, the node tool will take it from here.
      return bHandled;
   }

   public IDrawEngine getEngine()
   {

      return m_pEngineImpl;
   }
}
