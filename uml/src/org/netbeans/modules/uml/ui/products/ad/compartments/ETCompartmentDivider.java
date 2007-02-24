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


//	 $Date$
package org.netbeans.modules.uml.ui.products.ad.compartments;

import java.awt.event.MouseEvent;

import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IListCompartment;
import com.tomsawyer.editor.graphics.TSEGraphics;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;

public class ETCompartmentDivider extends TSConstRect
{   
   private IListCompartment m_prevCompartment;
   private IListCompartment m_nextCompartment;

   private boolean m_isDragged = false;
   private IETPoint m_previousMousePos;
   public static final int LOGICAL_HEIGHT = 2;

   public static final int PHYSICAL_HEIGHT = 1;
   public static final int COLLAPSED_HEIGHT = 2;
   public static final int COLLAPSED_WIDTH = 0;

   public static final String COLLAPSED_INDICATOR = "+";

   public ETCompartmentDivider(double x, double y, double width, double height, TSEGraphics pGraphics, IListCompartment pPrevCompartment, IListCompartment pNextCompartment, boolean pIsDragged)
   {
      super(x, y, x + width, y - height);
      this.m_prevCompartment = pPrevCompartment;
      this.m_nextCompartment = pNextCompartment;
      this.m_isDragged = pIsDragged;
   }

   public IListCompartment getPrevCompartment()
   {
      return m_prevCompartment;
   }

   public IListCompartment getNextCompartment()
   {
      return m_nextCompartment;
   }

   public boolean handleLeftMouseButtonDoubleClick(MouseEvent pEvent)
   {
      boolean eventHandled = false;
      if (m_nextCompartment != null)
      {
         if (m_nextCompartment.getCollapsed())
         {
            m_nextCompartment.setCurrentSize(m_nextCompartment.getMaxSize());
         }
         m_nextCompartment.setCollapsed(!m_nextCompartment.getCollapsed());
         eventHandled = true;
      }
      return eventHandled;
   }

   public boolean handleLeftMouseBeginDrag(IETPoint pStartPos, IETPoint pCurrentPos, boolean bCancel)
   {
      boolean eventHandled = false;
      TSTransform transform = getTransform();

      if (!(m_prevCompartment instanceof INameListCompartment) && this.contains(transform.pointToWorld(pCurrentPos.getX(), pCurrentPos.getY())))
      {
         this.m_isDragged = true;
         eventHandled = true;
      }
      return eventHandled;
   }

   public boolean handleLeftMouseDrag(IETPoint pStartPos, IETPoint pCurrentPos)
   {
      boolean eventHandled = false;

      if (this.m_isDragged)
      {
         IETSize oldSize = new ETSize(m_prevCompartment.getCurrentSize(true));
         IETSize newSize = new ETSize(oldSize);

         if (oldSize != null)
         {
            if (this.m_previousMousePos == null)
            {
               this.m_previousMousePos = pCurrentPos;
            }

            int maxHeight = m_prevCompartment.getMaxSize().getHeight();
            int maxWidth = m_prevCompartment.getMaxSize().getWidth();

            int currPosY = pCurrentPos.getY();
            int prevPosY = m_previousMousePos.getY();

            int adj = 0;
            int newHeight = 0;

            if (currPosY <= prevPosY)
            {

               adj = prevPosY - currPosY;
               newHeight = oldSize.getHeight() - adj;

               if (newHeight <= 0)
               {
                  newHeight = 0;
                  this.m_prevCompartment.setCollapsed(true);
               }

               if (!(m_prevCompartment instanceof INameListCompartment))
               {
                  newSize = new ETSize(maxWidth, newHeight);
               }
               else
               {
                  newSize = new ETSize(maxWidth, Math.max(newHeight, maxHeight));
               }

            }
            else
            {
               adj = currPosY - prevPosY;
               newHeight = oldSize.getHeight() + adj;
               newSize = new ETSize(maxWidth, newHeight);
            }

            this.m_prevCompartment.setCurrentSize(new ETSize(newSize));

            this.m_previousMousePos = pCurrentPos;
            eventHandled = true;
         }
      }
      else
      {
         this.m_previousMousePos = null;
      }
      return eventHandled;
   }

   public boolean isDragged()
   {
      return m_isDragged;
   }

   public void setDragged(boolean b)
   {
      m_isDragged = b;
   }

   public boolean containsDevicePoint(int x, int y)
   {
      boolean eventHandled = false;
      TSTransform transform = getTransform();

      if (this.contains(transform.pointToWorld(x, y)))
      {
         eventHandled = true;
      }
      return eventHandled;
   }
   
   protected TSTransform getTransform()
   {
		if (m_prevCompartment instanceof ETCompartment)
		{
			ETCompartment compartment = (ETCompartment)m_prevCompartment;
			return compartment.getTransform();
		}
		else if (m_nextCompartment instanceof ETCompartment)
		{
			ETCompartment compartment = (ETCompartment)m_nextCompartment;
			return compartment.getTransform();	
		}
		return null;
   }
}
