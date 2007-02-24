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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Iterator;

import org.netbeans.modules.uml.core.support.Debug;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETTransform;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETTransformOwner;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IMouseEvent;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ISetCursorEvent;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.PointConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.DragManager;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.IDragManager;

/**
 * @author brettb
 *
 */
public class CompartmentDividers
{
   private static final long DIVIDERHEIGHT = 2;

   protected ETList < DividerInfo > m_dividers;

   private ETTransformOwner m_transformOwner;

   private int m_nPenStyle; /// Pen style to use for drawing all the dividers

   public CompartmentDividers(ETTransformOwner transformOwner, int nPenStyle)
   {
      super();

      m_transformOwner = transformOwner;
      m_nPenStyle = nPenStyle;
   }

   public void finalize()
   {
      clear();
   }

   /**
    Add a divider to the list of dividers
    */
   public void addDivider(double nZoom, ETRect rect, ICompartment prevCompartment, ICompartment nextCompartment)
   {
      Debug.assertTrue(prevCompartment != nextCompartment);

      if ((prevCompartment != null) && (nextCompartment != null))
      {
         boolean bResizeable = false;
         if (prevCompartment != null)
         {
            bResizeable = prevCompartment.isResizeable();
         }

         boolean bCollapsible = false;
         if (nextCompartment != null)
         {
            bCollapsible = nextCompartment.isCollapsible();
         }

         // calc area to watch for mouse movement around divider bars
         final int nDividerHitHeight = (int) (nZoom * DIVIDERHEIGHT);
         rect.inflate(0, nDividerHitHeight);

         m_dividers.add(new DividerInfo(rect, bResizeable, bCollapsible, prevCompartment, nextCompartment));
      }
   }

   /**
    Remove all the divider information
    */
   public void clear()
   {
      m_dividers.clear();
   }

   /**
    Draw all the dividers using the color provided
    */
   public void draw(Graphics2D g, Color color)
   {
      Stroke pen = new BasicStroke(); // TODO add the color

      /* CLEAN, used for testing
            CPen pens[3];
            pens[0].createPen( PS_DASH, 1, rGB( 255, 0, 0 ));
            pens[1].createPen( PS_DASH, 1, rGB( 0, 255, 0 ));
            pens[2].createPen( PS_DASH, 1, rGB( 0, 0, 255 ));
            long lPenIndx = 0;
      */

      for (Iterator iter = m_dividers.iterator(); iter.hasNext();)
      {
         DividerInfo dividerInfo = (DividerInfo)iter.next();

         // Determine if the compartment is collapsed
         boolean bIsCollapsed = false;

         if (dividerInfo.m_bCollapsible)
         {
            ICompartment compartment = dividerInfo.m_nextCompartment;

            bIsCollapsed = compartment.getCollapsed();
         }

         drawDividerBar(g, dividerInfo.m_rcDivider, bIsCollapsed, pen);
      }
   }

   /**
    Returns true when the mouse event occurs on any of the dividers
    */
   public boolean isMouseOnDivider(IMouseEvent mouseEvent)
   {
      boolean bMouseOnDivider = false;

      if (m_transformOwner != null)
      {
         // this is the relative position within the node (topleft = 0,0)
         final IETPoint ptMouseLoc = getTransform().getWinClientMouseLocation(mouseEvent);

         bMouseOnDivider = (determineMouseEventDivider(ptMouseLoc) != null);
      }

      return bMouseOnDivider;
   }

   /**
    Handle the left mouse begin drag event, if the event occured on a divider
    */
   public boolean handleLeftMouseBeginDrag(IETPoint startPos)
   {
      boolean bHandled = false;

      IETPoint ptMouseLoc = getTransform().getTSAbsoluteToWinScaledOwner(startPos);

      DividerInfo dividerInfo = determineMouseEventDivider(ptMouseLoc);
      if (dividerInfo != null)
      {
         if (!dividerInfo.m_bResizeable)
         {
            dividerInfo = null;
         }

         bHandled = true;
      }

      if (dividerInfo != null)
      {
         IDragManager tool = createDragManagerTool();

         if (tool != null)
         {
            // Determine the max min height for the drag operation
            IETRect rectBounds = getTransform().getWinClientRect();

            ICompartment compartment = dividerInfo.m_prevCompartment;
            Debug.assertTrue(compartment != null);
            if (compartment != null)
            {
               // Calculate the max height for the cursor
               IETRect rectPrevBounding = TypeConversions.getLogicalBoundingRect(compartment);
               tool.setTop(rectPrevBounding.getTop());

               // Calculate the max height for the cursor
               ICompartment nextCompartment = dividerInfo.m_nextCompartment;
               Debug.assertTrue(nextCompartment != null);
               if (nextCompartment != null)
               {
                  IETRect rectNextBounding = TypeConversions.getLogicalBoundingRect(nextCompartment);
                  tool.setBottom(rectNextBounding.getBottom());
               }

               tool.setStretchCompartment(compartment);
            }
         }
      }

      // turn off TS handling of the mouse, the node tool will take it from here.
      return bHandled;
   }

   protected class DividerInfo
   {
      public DividerInfo()
      {
         m_rcDivider = new ETRect(0, 0, 0, 0);
         m_bResizeable = false;
         m_bCollapsible = false;
         m_prevCompartment = null;
         m_nextCompartment = null;
      };

      public DividerInfo(ETRect rect, boolean resizeable, boolean collapsible, ICompartment prevCompartment, ICompartment nextCompartment)
      {
         m_rcDivider = rect;
         m_bResizeable = resizeable;
         m_bCollapsible = collapsible;
         m_prevCompartment = prevCompartment;
         m_nextCompartment = nextCompartment;
      };

      public ETRect m_rcDivider;
      public boolean m_bResizeable;
      public boolean m_bCollapsible;
      public ICompartment m_prevCompartment;
      public ICompartment m_nextCompartment;
   };

   /**
    * Returns a reference to the draw engine's transform
    */
   public ETTransform getTransform()
   {
      if (null == m_transformOwner)
         throw new IllegalStateException();

      return m_transformOwner;
   }

   /*
    * Draws the compartment's divider bar.  The Divider Bar is the line separating the top of the compartment from
    * the compartment above it.  If the compartment is visible a thin bar is drawn, if minimized a thick bar is drawn.
    *
    * @param dc - The device context on which to draw.
    */
   protected void drawDividerBar(Graphics2D dc, ETRect rect, boolean bCollapsed, Stroke pen)
   {
      // draw a line across the middle of the dividerbar
      final int iY = rect.getCenterPoint().y;
      Point topLeft = new Point(rect.getLeft(), iY);
      Point topRight = new Point(rect.getRight(), iY);

      GDISupport.drawLine(dc, topLeft, topRight, pen);

      if (bCollapsed)
      {
         Color clrText = Color.BLACK; // TODO dc.getTextColor();

         // create background brush
         // TODO Brush br( clrText );
         // TODO CBrush oldBrush  = (CBrush*) dc.selectObject(&br);

         // we're collapsed so draw our thick divider
         // expand the bar's size for mouse
         ETRect rc = new ETRect(topLeft, topRight);
         double zoomLevel = getTransform().getZoomLevel();
         rect.setBottom((int) (rect.getTop() + (zoomLevel * DIVIDERHEIGHT)));
         rect.inflate((int) (-5 * zoomLevel), 0);

         GDISupport.drawRectangle(dc, rect, clrText, clrText);

         // TODO dc.selectObject( pOldBrush );
      }
   }

   /**
    * Handle the set cursor event, if the event occured on a divider
    */
   protected boolean handleSetCursor(IETPoint ptMouseLoc, ISetCursorEvent event)
   {
      DividerInfo dividerInfo = determineMouseEventDivider(ptMouseLoc);
      if (dividerInfo != null)
      {
         int nCursor = 0;

         if (dividerInfo.m_bResizeable) // can the one above be resize?
         {
            // TODO nCursor = IDC_DRAG_H;
         }
         else if (dividerInfo.m_bCollapsible) // can it be collapsed?
         {
            ICompartment compartment = dividerInfo.m_nextCompartment;
            if (compartment != null)
            {
               // TODO nCursor = IDC_EXPAND;

               boolean bCollapsed = compartment.getCollapsed();
               if (!bCollapsed)
               {
                  // TODO nCursor = IDC_COLLAPSE;
               }
            }
         }

         // yes, set cursor
         if (nCursor != 0)
         {
            // TODO setCursor( .loadCursor( _Module.m_hInst, mAKEINTRESOURCE(nCursor) ) );
         }
      }

      return (dividerInfo != null);
   }

   /**
    * Find the divider bar located under the mouse location
    */
   protected DividerInfo determineMouseEventDivider(IETPoint ptClientMouseLocation)
   {
      DividerInfo dividerInfo = null;

      Iterator iter = m_dividers.iterator();
      for (; iter.hasNext();)
      {
         DividerInfo testInfo = (DividerInfo)iter.next();
         Debug.assertTrue(testInfo != null);
         if (testInfo.m_rcDivider.contains(ptClientMouseLocation))
         {
            dividerInfo = testInfo;
            break;
         }
      }

      return dividerInfo;
   }

   /**
    Create a drag manager tool
    */
   protected IDragManager createDragManagerTool()
   {
      DragManager dragMgr = new DragManager(getTransform().getGraphWindow());
      if (dragMgr != null)
      {
         //this.getTransform().getGraphWindow().switchState(dragMgr);
          this.getTransform().getGraphWindow().switchTool(dragMgr);
      }
      return dragMgr;
   }

   public ETList < DividerInfo > getDividers()
   {
      return m_dividers;
   }
}
