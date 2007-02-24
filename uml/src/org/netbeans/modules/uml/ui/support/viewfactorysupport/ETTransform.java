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

import java.awt.Point;
import java.awt.geom.AffineTransform;

import javax.swing.SwingUtilities;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.ETDeviceRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.ui.controls.drawingarea.IUIDiagram;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.graph.TSGraphObject;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;

/**
 * 
 * @author Trey Spiva
 */
public class ETTransform
{
   /**
    * The parent of a drawengine or compartment.  pGraphObject can be null.  
    * This happens when the draw engine or compartment is reparented.
    */
   private TSGraphObject m_GraphObject = null;
   //	private IETPoint m_Origin = new ETPointEx(0, 0);
   //	private IETSize m_Size = new ETSize(0, 0);

   private IETPoint m_Origin = null;
   private IETSize m_Size = null;

   /**
    * Set the graph object for this transform, the transform is invalid until 
    * this is set
    * 
    * @param value The parent graph window.
    */
   public void setGraphObject(TSGraphObject value)
   {
      m_GraphObject = value;
   }

   /**
    * Get the graph object for this transform, the transform is invalid until 
    * this is set
    * 
    * @return The parent graph window.
    */
   public TSGraphObject getGraphObject()
   {
      return getOwnerGraphObject();
   }

   public void setTSAbsolute(IETRect tsAbsolute)
   {
      IETRect graphObjectAbsoluteBounding = getGraphObjectAbsoluteBoundingRect();
      if (graphObjectAbsoluteBounding != null && tsAbsolute != null)
      {
         initializeOrigin();
         m_Origin.setX(tsAbsolute.getLeft() - graphObjectAbsoluteBounding.getLeft());
         m_Origin.setY(graphObjectAbsoluteBounding.getTop() - tsAbsolute.getTop());

         initializeSize();
         int width = tsAbsolute.getIntWidth();
         int height = tsAbsolute.getIntHeight();
         m_Size.setSize(width, height);
      }
   }

   public void setWinClientRectangle(IETRect winClient)
   {

      if (isTSWorldCoordinate() == true)
      {
         setTSAbsolute(winClient);
      }
      else
      {
         setTSAbsolute(getDrawingAreaControl().deviceToLogicalRect(winClient));
      }
   }

   /**
    * Sets the internal variables from a Windows rectangle using the owner's 
    * origin, upper left corner
    */
   public void setWinScaledOwnerRect(IETRect rectWinScaledOwner)
   {
      final double zoom = getZoomLevel();

      initializeOrigin();
      m_Origin.setX((int)Math.round(rectWinScaledOwner.getLeft() * zoom));
      m_Origin.setY((int)Math.round(rectWinScaledOwner.getTop() * zoom));

      initializeSize();
      int x = (int)Math.round(rectWinScaledOwner.getWidth() / zoom);
      int y = (int)Math.round(rectWinScaledOwner.getHeight() / zoom);

      m_Size.setSize(x, y);
   }

   // This member and data accessors are used to determine if the coordinate 
   // system is based on World or Device coordinates.  In the future we need to
   // always use World coordinates.
   private boolean m_IsWorldBasedCoor = false;
   protected boolean isTSWorldCoordinate()
   {
      return m_IsWorldBasedCoor;
   }

   protected void setIsTSWorldCoordinate(boolean value)
   {
      m_IsWorldBasedCoor = value;
   }

   /**
    * Returns the Tom Sawyer rectangle when the zoom is 100%.
    */
   public IETRect getTSAbsoluteRect()
   {
      // Using the internal data, calculate the logical rectangle
      IETRect retVal = null;
      IETRect graphRect = getGraphObjectAbsoluteBoundingRect();

      if (graphRect != null)
      {
         //         // When the size is EXPAND_TO_NODE, use the Tom Sawyer graph 
         //         // object's size
         //         if(m_Size.getWidth() != ICompartment.EXPAND_TO_NODE)
         //         {
         //            retVal.setRight(retVal.getLeft() + m_Size.getWidth());
         //         }
         //         
         //         if(m_Size.getHeight()!= ICompartment.EXPAND_TO_NODE)
         //         {
         //            retVal.setBottom(retVal.getTop() + m_Size.getHeight());
         //         }
         //         
         //         int left   = retVal.getLeft() + m_Origin.getX();
         //         int right  = retVal.getRight() + m_Origin.getX();
         //         int top    = retVal.getTop() + m_Origin.getY();
         //         int bottom = retVal.getBottom() + m_Origin.getY();
         //         retVal.setLeft(left);
         //         retVal.setRight(right);
         //         retVal.setTop(top);
         //         retVal.setBottom(bottom);

         // Determine the Offset
         int x = graphRect.getLeft();
         int y = graphRect.getTop();
         if (m_Origin != null)
         {
            x += m_Origin.getX();
            y -= m_Origin.getY();
         }

         double width = graphRect.getWidth();
         double height = graphRect.getHeight();
         if (m_Size != null)
         {
            width = m_Size.getWidth();
            height = m_Size.getHeight();
         }

         retVal = new ETRect(x, y, width, height);
      }

      return retVal;
   }

   /**
    * Returns the Windows rectangle using the screen's origin
    */
   public IETRect getWinScreenRect()
   {
      IETRect clientRect = getWinClientRect();
      TSGraphObject window = getGraphObject();

      Point topLeft = new Point(clientRect.getLeft(), clientRect.getTop());
      SwingUtilities.convertPointToScreen(topLeft, getGraphWindow());

      Point bottomRight = new Point(clientRect.getRight(), clientRect.getBottom());
      SwingUtilities.convertPointToScreen(bottomRight, getGraphWindow());

      return new ETDeviceRect(topLeft, bottomRight);
   }

   public IETRect getWinClientRect()
   {
      IETRect rectTSAbsolute = getTSAbsoluteRect();
      if (rectTSAbsolute != null)
      {
         Point ptTopLeft = rectTSAbsolute.getTopLeft();
         Point ptBottomRight = rectTSAbsolute.getBottomRight();
         TSConstRect tsRect = new TSConstRect(ptTopLeft.x, ptTopLeft.y, ptBottomRight.x, ptBottomRight.y);
         return new ETDeviceRect(getTransform().boundsToDevice(tsRect));
      }
      else
      {
         return null;
      }
   }

   /**
    * Returns the Windows absolute rectangle using the owner's origin, upper 
    * left corner
    */
   public IETRect getWinAbsoluteOwnerRect()
   {

      int x;
      int y;
      if (m_Origin != null)
      {
         x = m_Origin.getX();
         y = m_Origin.getY();
      }
      else
      {
         x = 0;
         y = 0;
      }

      int width;
      int height;
      if (m_Size != null)
      {
         width = m_Size.getWidth();
         height = m_Size.getHeight();
      }
      else
      {
         width = 0;
         height = 0;
      }

      IETRect retVal = new ETDeviceRect(x, y, width, height);

      if (m_Size != null && ((m_Size.getWidth() == ICompartment.EXPAND_TO_NODE) || (height == ICompartment.EXPAND_TO_NODE)))
      {
         IETRect graphObjectAbsolute = getGraphObjectAbsoluteBoundingRect();
         if (width == ICompartment.EXPAND_TO_NODE)
         {
            retVal.setRight((int)graphObjectAbsolute.getWidth());
         }

         if (height == ICompartment.EXPAND_TO_NODE)
         {
            retVal.setBottom((int)graphObjectAbsolute.getHeight());
         }
      }

      return retVal;
   }

   /**
    * Returns the Windows rectangle using the owner's origin, upper left corner
    */
   public IETRect getWinScaledOwnerRect()
   {
      IETRect retVal = getWinAbsoluteOwnerRect();

      double zoom = getZoomLevel();

      int left = (int)Math.round(retVal.getLeft() * zoom);
      int top = (int)Math.round(retVal.getTop() * zoom);
      int right = (int)Math.round(retVal.getRight() * zoom);
      int bottom = (int)Math.round(retVal.getBottom() * zoom);

      retVal.setSides(left, top, right, bottom);
      return retVal;
   }

   /**
    * Returns the rectangle in in TS mils WRT the owner
    */
   public IETRect getMilsRect()
   {
      IETRect absoluteRect = getTSAbsoluteRect();

      double centerX = absoluteRect.getWidth() / 2.0;
      double centerY = -absoluteRect.getHeight() / 2.0; // vertical size is negative WRT Tom Sawyer

      int x;
      int y;
      if (m_Origin != null)
      {
         x = m_Origin.getX();
         y = m_Origin.getY();
      }
      else
      {
         x = 0;
         y = 0;
      }

      int width;
      int height;
      if (m_Size != null)
      {
         width = m_Size.getWidth();
         height = m_Size.getHeight();
      }
      else
      {
         width = 0;
         height = 0;
      }

      return new ETRect(
         Math.round((x - centerX) * 1000 / centerX),
         Math.round((centerY - y) * 1000 / centerY),
         Math.round((x - width) * 1000 / centerX),
         Math.round((centerY - (y - height)) * 1000 / centerY));
   }

   /**
    * Set the size of this transform using scaled dimensions
    */
   public void setScaledSize(final ETSize sizeScaled)
   {
      final double zoomLevel = getZoomLevel();

      initializeSize();
      m_Size.setSize((int)Math.round(sizeScaled.getWidth() / zoomLevel), (int)Math.round(sizeScaled.getHeight() / zoomLevel));
   }

   /**
    * Get the location within the owner of the origin in Tom Sawyer 100% 
    * dimensions
    */
   public IETPoint getAbsoluteOwnerOrigin()
   {
      return m_Origin != null ? m_Origin : new ETPoint(0, 0);

   }

   /**
    * Set the location within the owner of the origin in Tom Sawyer 100% 
    * dimensions
    */
   public void setAbsoluteOwnerOrigin(IETPoint value)
   {
      initializeOrigin();
      m_Origin = value;
   }

   /**
    * Get the size of this transform using Tom Sawyer 100% dimensions
    */
   public IETSize getAbsoluteSize()
   {
      return m_Size != null ? m_Size : new ETSize(0, 0);
   }

   /**
    * Set the size of this transform using Tom Sawyer 100% dimensions
    */
   public void setAbsoluteSize(IETSize value)
   {
      initializeOrigin();
      m_Size = value;
   }

   /**
    * Set the size of this transform using Tom Sawyer 100% dimensions
    * 
    * @param width The width of the absolute size.
    * @param height The height of the absolute size.
    */
   public void setAbsoluteSize(int width, int height)
   {
      if (m_Size != null)
      {
         m_Size.setSize(width, height);
      }
      else
      {
         setAbsoluteSize(new ETSize(width, height));
      }
   }

   /**
    * Get the size of this transform using scaled dimensions.
    */
   public IETSize getScaledSize()
   {
      final double zoom = getZoomLevel();

      return new ETSize((int)Math.round(m_Size.getWidth() * zoom), (int)Math.round(m_Size.getHeight() * zoom));
   }

   ///   Mouse Location Routines

   /**
    Returns the Windows scaled owner point for the input mouse event
    The mouse event's client data is updated with the converted value
    */
   public IETPoint getWinClientMouseLocation(IMouseEvent mouseEvent)
   {
      if (null == mouseEvent)
         throw new IllegalArgumentException();

      Point ptMouseLocation = new Point(mouseEvent.getDeviceX(), mouseEvent.getDeviceY());

      final IETRect rectWinClient = getWinClientRect();
      ptMouseLocation.x -= rectWinClient.getLeft();
      ptMouseLocation.y -= rectWinClient.getTop();

      mouseEvent.setClientX(ptMouseLocation.x);
      mouseEvent.setClientY(ptMouseLocation.y);

      return PointConversions.newETPoint(ptMouseLocation);
   }

   // I (BDB) was not able to get this to work so I've used a work-around when needed
   //   /**
   //    * Returns the Windows scaled owner position of the cursor
   //    */
   //   public IETPoint getWinScaledOwnerCursorPosition()
   //   {
   //      return null;
   //   }

   /**
    Returns the Windows scaled owner point for the input cursor event
    */
   public IETPoint getWinScaledOwnerCursorPosition(ISetCursorEvent setCursorEvent)
   {
      if (null == setCursorEvent)
         throw new IllegalArgumentException();

      IETPoint ptCursorPosition = null;

      // I (BDB) am unable to figure out how to replicate the C++ code here.
      // So, I'm using the original mouse event location to find the win scaled owner location

      Point point = setCursorEvent.getWinClientLocation();
      final IETRect rectWinClient = getWinClientRect();

		if (point != null && rectWinClient != null){
			ptCursorPosition = new ETPoint(point.x - rectWinClient.getLeft(), point.y - rectWinClient.getTop());			
		}

      return ptCursorPosition;
   }

   /**
    * Returns a point in Windows client coordinates, from Tom Sawyer absolute coordinates
    */
   public IETPoint getTSAbsoluteToWinClient(IETPoint tsAbsolute)
   {
      IETPoint ptWinClient = null;

      TSTransform transform = getTransform();
      if (transform != null)
      {
         Point point = transform.pointToDevice(tsAbsolute.getX(), tsAbsolute.getY());
         ptWinClient = PointConversions.newETPoint(point);
      }

      return ptWinClient;
   }

   /**
    * Returns a point in Windows scaled owner coordinates, from Tom Sawyer absolute coordinates
    */
   public IETPoint getTSAbsoluteToWinScaledOwner(IETPoint ptTSAbsolute)
   {

      int x;
      int y;
      if (m_Origin != null)
      {
         x = m_Origin.getX();
         y = m_Origin.getY();
      }
      else
      {
         x = 0;
         y = 0;
      }

      // The origin needs to be taken into account,
      // so we offset the input point by the origin amount
      final IETPoint ptOffset = new ETPoint(ptTSAbsolute.getX() + x, ptTSAbsolute.getY() - y);

      final IETPoint ptWinClient = getTSAbsoluteToWinClient(ptOffset);
      final IETRect rectWinClient = getWinClientRect();

      return new ETPoint(ptWinClient.getX() - rectWinClient.getLeft(), ptWinClient.getY() - rectWinClient.getTop());
   }

   /**
    * Returns a point in Tom Sawyer absolute coordinates, from Windows absolute owner coordinates 
    */
   public IETPoint getWinAbsoluteOwnerToTSAbsolute(final IETPoint ptWinAbsoluteOwner)
   {
      final IETRect rectTSAbsolute = getTSAbsoluteRect();
      return new ETPoint(ptWinAbsoluteOwner.getX() + rectTSAbsolute.getLeft(), rectTSAbsolute.getTop() - ptWinAbsoluteOwner.getY());
   }

   /**
    * Returns a point in Tom Sawyer absolute coordinates, from Windows Client
    * Owner coordinates
    */
   IETPoint getWinClientToTSAbsolute(final IETPoint ptWinClient)
   {
      TSTransform pTransform = getTransform();

      if (pTransform != null && ptWinClient != null)
      {
         return new ETPointEx(pTransform.pointToWorld(ptWinClient.getX(), ptWinClient.getX()));
      }
      return null;
   }

   /**
    * Returns a point in Windows absolute owner coordinates, from Tom Sawyer
    * absolute coordinates
    */
   public IETPoint getTSAbsoluteToWinAbsoluteOwner(final IETPoint ptTSAbsolute)
   {
      final IETRect rectTSAbsoluteBounding = getTSAbsoluteRect();
      return new ETPoint(ptTSAbsolute.getX() - rectTSAbsoluteBounding.getLeft(), rectTSAbsoluteBounding.getTop() - ptTSAbsolute.getY());
   }

   public double getZoomLevel()
   {
      return getZoomLevel(null);
   }

   /**
    * Returns the zoom level for the owning graph editor
    */
   public double getZoomLevel(IDrawInfo pDrawInfo)
   {
      double zoom = 1.0;
      if (pDrawInfo != null)
      {
         zoom = pDrawInfo.getOnDrawZoom();
      }
      else
      {
         IDrawingAreaControl control = getDrawingAreaControl();
         if (control != null)
         {
            zoom = control.getCurrentZoom();
         }
      }

      return zoom;
   }

   /**
    * Returns true when this transform is valid for performing transform operations 
    */
   public boolean isValid()
   {
      return (getGraphObject() != null);
   }

   /**
    * Retrieve the drawing area control associated with this
    */
   IDrawingAreaControl getDrawingAreaControl()
   {

      if (isValid())
      {
         IDiagram diagram = TypeConversions.getDiagram(getGraphObject());
         if (diagram instanceof IUIDiagram)
         {
            IUIDiagram adDiagram = (IUIDiagram)diagram;
            return adDiagram.getDrawingArea();
         }
      }

      return null;
   }

   //**************************************************
   // Helper Methods
   //**************************************************

   /**
    * Returns the absolute bounding rect for the Tom Sawyer graph object
    */
   protected IETRect getGraphObjectAbsoluteBoundingRect()
   {
      // Get the logical bounding rect of the Tom Sawyer graph object
      TSGraphObject graphObject = getGraphObject();

      if (graphObject instanceof ITSGraphObject)
      {
         ITSGraphObject tsGraphObject = (ITSGraphObject)graphObject;
         return new ETRectEx(tsGraphObject.getBounds());
      }
      else
      {
         ETSystem.out.println("Error! invalid graph object.");
      }
      return null;
   }

   /**
    * Retrieves the transform used to convert Tom Sawyer coordinates to 
    * Windows coordinates.
    */
   public TSTransform getTransform()
   {
      TSEGraphWindow graphWindow = getGraphWindow();
      return graphWindow != null ? graphWindow.getTransform() : null;
   }

   /**
    * Retrieves the graph editor associated with this Tom Sawyer graph object
    * @throws NullPointerException if the graph object is not set.
    */
   public TSEGraphWindow getGraphWindow() throws NullPointerException
   {
      IDrawingAreaControl control = getDrawingAreaControl();
      if (control == null)
         throw new NullPointerException();

      return control.getGraphWindow();
   }

   public TSGraphObject getOwnerGraphObject()
   {
      return m_GraphObject;
   }

   // Iinitialize the origin data member.  The origin is only initialized if
   // it has not been already initialized.
   protected void initializeOrigin()
   {
      if (m_Origin == null)
      {
         m_Origin = new ETPointEx(0, 0);
      }
   }

   // Iinitialize the size data member.  The size is only initialized if
   // it has not been already initialized.
   protected void initializeSize()
   {
      if (m_Size == null)
      {
         m_Size = new ETSize(0, 0);
      }
   }

    /**
     * Constructs the standard Graphics2D AffineTransform oblect 
     * from the Tom Sawyer transformation. 
     */
    public static AffineTransform convertTransform(TSTransform tsTransform) {
        if (tsTransform == null) return null;
        //
        AffineTransform result = new AffineTransform(
                tsTransform.getScaleX(), 
                0d, 
                0d, 
                tsTransform.getScaleY(), 
                tsTransform.getOffsetX(), 
                tsTransform.getOffsetY()); 
        return result;
    }

}
