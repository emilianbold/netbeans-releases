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



package org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces;

import java.awt.Dimension;
import java.awt.Point;

import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.ETLifelineCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.LifelineConnectorLocation;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.umltsconversions.RectConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;

/**
 * 
 * @author Trey Spiva
 */
public abstract class LifelineCompartmentPiece
{   
   /** This is the width of all pieces */
   public static final int PIECE_WIDTH = 10;
   
   /** This is the height of all pieces */
    public static final int PIECE_HEIGHT = 40;
    
   /** 
    * Added value when creating an activation bar under a suspension area (logical units).
    */
   public static final int ACTIVATION_BAR_BUFFER = 10;   
   
   /** 
    * added value when creating an activation bar under a suspension area (logical units).
    */
   public static final int MIN_SIBLING_SPACE = ACTIVATION_BAR_BUFFER;
   
   /** The total number of corner connectors available */
   public static final int ACTIVATION_CORNER_COUNT = LifelineConnectorLocation.LCL_BOTTOMLEFT + 1;
   
   public static final int CHILD_OFFSET = (PIECE_WIDTH / 2);
   
   /** The compartment that ownes the piece. */
   private ETLifelineCompartment    m_Parent      = null;
   
   /** THe owner piece of this lifeline piece.  If the piece is a root piece the parent will be null. */
   private LifelineCompartmentPiece m_ParentPiece = null;
   
   /** Distance from the top of the parent piece or compartment to the top of this piece */
   private IETPoint                 m_TopLeft     = null;
   
   /** Distance from the top of this piece to the bottom of this piece. */
   private int                      m_Height      = -1;
   
   public LifelineCompartmentPiece(ETLifelineCompartment    parent, 
                                   LifelineCompartmentPiece parentPiece,
                                   IETPoint                 topLeft,
                                   int                      height)   
   {
       setParent(parent);
       setParentPiece(parentPiece);
       setTopLeft(topLeft);
       m_Height = height;
   }
   
   //**************************************************
   // Data Acess Memebers
   //**************************************************
   
   /**
    * Access to the member parent compartment.
    */
   public ETLifelineCompartment getParent()
   {
      return m_Parent;
   }

   /**
    * The horizontal offset for the children of this piece.
    */
   public int getChildOffset()
   {
      return CHILD_OFFSET;
   }
   
   /**
    * @param compartment
    */
   public void setParent(ETLifelineCompartment compartment)
   {
      m_Parent = compartment;
   }

   /**
    * Access to the member parent piece
    */
   public LifelineCompartmentPiece getParentPiece()
   {
      return m_ParentPiece;
   }

   /**
    * Used to set the parent piece for this lifeline piece.
    */
   public void setParentPiece(LifelineCompartmentPiece piece)
   {
      if( (m_ParentPiece != null) && (piece != null))
      {
         int newY = m_TopLeft.getY() - (piece.getTop() - m_ParentPiece.getTop());
         m_TopLeft.setY(Math.max( ACTIVATION_BAR_BUFFER, newY ));
      }      

      m_ParentPiece = piece;
      if((m_ParentPiece != null) && (m_ParentPiece.getLeft() != 0))
      {
         setLeft( m_ParentPiece.getLeft() + m_ParentPiece.getChildOffset() );
      }
   }

   /**
    * @return
    */
   public IETPoint getTopLeft()
   {
      return m_TopLeft;
   }

   /**
    * @param point
    */
   public void setTopLeft(IETPoint point)
   {
      m_TopLeft = point;
   }

   /**
    * Retrieve the height of this piece
    */
   public int getHeight()
   {      
      return m_Height;
   }

   /**
    * Set the height of this piece
    */
   public void setHeight(int value)
   {
      if(value < 0)
      {
         m_Height = value;
      }

      m_Height = value;
   }

   /**
    * Get the bottom location (within the lifeline compartment)
    */
   public int getBottom()
   {
      return (int)getTop() + (int)getHeight();
   }
   
   /**
    * Sets the bottom location of the piece, by changing the height
    */
   public void setBottom(int y)
   {
      setHeight(y - getTop());
   }
   
   /**
    * Sets the logical bottom location of the piece, by changing the height
    */
   public void setLogicalBottom(int value)
   {
      // Since the Y axis is oriented in such a way that smaller Y values
      // move down the axis the top should always be a greater number than the 
      // bottom number.
      setHeight(getLogicalTop() - value);
   }
   
   /**
    * Get the logical bottom location
    */
   public int getLogicalBottom()
   {
      int compartmentTop = getCompartmentLogicalBoundingRect().getTop();
      return compartmentTop - getBottom();
//      return getBottom() - compartmentTop;
   }
   
   public void setLeft(int left)
   {
      if(m_TopLeft == null)
      {
         m_TopLeft = new ETPoint(0, 0);
      }
      m_TopLeft.setX(left);
      
      getParent().updateSides(left, left + PIECE_WIDTH);
   }
   
   /**
    * Returns the left offset from the lifeline compartment.
    */
   public int getLeft()
   {
      return m_TopLeft.getX();
   }
   
   public int getLeftRelativeTo(IETRect rectEngine)
   {
      int retVal = getLeft();
      
      if(rectEngine != null)
      {
         retVal += rectEngine.getTop();
      }
      
      return retVal;
   }
   
   public void moveBy(int delta)
   {
      moveBy(delta, true);
   }
   
   public void moveBy(int delta, boolean adjustOthers)
   {
       int newPosition = getY() + delta;
       if(adjustOthers == true)
       {
           setY(newPosition);
       }
       else
       {
           m_TopLeft.setY(newPosition);
       }
   }
   
   public void setTop(int top)
   {
      setY(top - getParentTop());
   }
   
   public int getY()
   {
      return m_TopLeft.getY();
   }
   
   /**
    * Sets the y offset from the parent of the piece, without changing the height.
    */
   public void setY(long value)
   {
      long yValue = value;
      
      long delta = yValue - ACTIVATION_BAR_BUFFER;
      if(delta < 0)
      {
         LifelineCompartmentPiece parent = getParentPiece();
         if(parent != null)
         {
            long newTop = parent.getTop() + delta;
            parent.setTop((int)newTop);
         }
         
         // It is possible for the parent to change because of a merge.
         // So, use the current Y if there was a merge
         if(parent == getParentPiece())
         {
            yValue = ACTIVATION_BAR_BUFFER;  
         }
         else
         {
            yValue = getY();
         }
      }
      
      m_TopLeft.setY((int)yValue);
   }
   
   /**
    * Get the top location (within the lifeline compartment)
    */
   public int getTop()
   {
      return getParentTop() + getY(); 
   }
   
   /** 
    *  Get the parent's top location (within the lifeline compartment).
    */
   public int getParentTop()
   {
      // default is the top of the compartment
      int retVal = 0;
      
      LifelineCompartmentPiece parent = getParentPiece();
      if(parent != null)
      {
         retVal = parent.getTop();
      }
      return retVal; 
   }
   
   /**
    * Get the size of this piece when drawn.
    */
   public Dimension getDrawSize()
   {
      return new Dimension(PIECE_WIDTH, getHeight());
   }
   
   /**
    * Get the rectangle, in node coords, for drawing the piece.
    * 
    * @param zoom The zoom factor used to scale the draw rectangle.  
    *             <i><b>Note: </b> currently this parameter is not used.</i>
    * @return The rectangle that is to be drawned.
    */
   public IETRect getDrawRect(double zoom)
   {
      Dimension dim = getDrawSize();
      
      IETRect engineRect = getEngineLogicalBoundingRect();
      
      double xOffset = engineRect.getCenterX();
      return new ETRect(xOffset + getLeft(), 
                        getDETop(), 
                        dim.getWidth(), 
                        dim.getHeight());
   }
   
   public IETRect getAbsoluteDrawRect(double zoom)
   {
      IETRect retVal = getDrawRect(zoom);
      
      if(retVal != null)
      {
         IETRect engineRect = getEngineLogicalBoundingRect();
         retVal.setTop(engineRect.getTop() - retVal.getTop());
      }
      
      return retVal;
   }
   
   /** 
    * Retrieves the name of the lifeline piece.
    */
   protected abstract String getID();
   
   /**
    * Returns the lifeline pieces kind for this piece.
    */
   public abstract int getLifelinePiecesKind();
   
   public IProductArchiveElement writeToArchive(IProductArchiveElement pParentElement)
   {
      IProductArchiveElement retVal = null;
      if(pParentElement != null)
      {
         retVal = pParentElement.createElement(getID());
         if(retVal != null)
         {         
            retVal.addAttributeLong(IProductArchiveDefinitions.ADLIFELINECOMPARTMENTPIECE_LEFT_STRING, getLeft());
            retVal.addAttributeLong(IProductArchiveDefinitions.ADLIFELINECOMPARTMENTPIECE_DY_STRING, getTop());
            retVal.addAttributeLong(IProductArchiveDefinitions.ADLIFELINECOMPARTMENTPIECE_HEIGHT_STRING, getHeight());
         }
      }
      
      return retVal;
   }
   
   public void readFromArchive(IProductArchiveElement pParentElement)
   {
      if(pParentElement != null)
      {
         long left = pParentElement.getAttributeLong(IProductArchiveDefinitions.ADLIFELINECOMPARTMENTPIECE_LEFT_STRING);
         long top  = pParentElement.getAttributeLong(IProductArchiveDefinitions.ADLIFELINECOMPARTMENTPIECE_DY_STRING);
         setHeight((int)pParentElement.getAttributeLong(IProductArchiveDefinitions.ADLIFELINECOMPARTMENTPIECE_HEIGHT_STRING));
         
         if((left == 0) && (top == left))
         {
            // When these values are zero, we need to upgrade from the previous 
            // y value
            top = pParentElement.getAttributeLong(IProductArchiveDefinitions.ADLIFELINECOMPARTMENTPIECE_Y_STRING);
         }
         setLeft((int)left);
         setTop((int)top);
      }
   }
   
   /**
    * Retrieves the diagram contianing this piece
    */
   public IDiagram getDiagram()
   {
      IDiagram retVal = null;
      
      IDrawEngine engine = getDrawEngine();
      if(engine != null)
      {
         retVal = TypeConversions.getDiagram(engine);
      }
      
      return retVal;
   }
   
   //**************************************************
   // Helper Methods
   //**************************************************
   
   /**
    * Retrieves the draw engine contianing this piece.
    */
   protected IDrawEngine getDrawEngine()
   {
      IDrawEngine retVal = null;
      
      ETLifelineCompartment parent = getParent();
      if(parent != null)
      {
         retVal = parent.getEngine();
      }
      
      return retVal;
   }
   
   /**
    * Returns the (normalized) logical bounding rect of the draw engine (node) 
    * containing this piece.
    */
   protected IETRect getEngineLogicalBoundingRect(boolean normalize)
   {
      return getParent().getEngineLogicalBoundingRect(normalize);
   }
   
   /**
    * Returns the (normalized) logical bounding rect of the draw engine (node) 
    * containing this piece.  The rectangle will not be normalized.
    */
   protected IETRect getEngineLogicalBoundingRect()
   {
      return getParent().getEngineLogicalBoundingRect(false);
   }
   
   /**
    * Returns the (normalized) logical bounding rect of the draw engine (node) 
    * containing this piece.
    */
   protected IETRect getCompartmentLogicalBoundingRect()
   {
        // Fix J2573:  For some reason the device rect was being passed back
        //             here, but the function name implies that this rect
        //             should be logical.  The problem seen was when the
        //             create message feeds back a logical location for
        //             updating the attached "suspension area."
       
        return getParent().getLogicalBoundingRect();
      
        /// CLEAN IETRect deviceRect = getParent().getBoundingRect();
        // TSTransform transform = getParent().getTransform();
        //return new ETRect(transform.boundsToDevice(RectConversions.etRectToTSRect(deviceRect)));
        // return deviceRect;
   }

   /**
    * Get the distance from the top of the draw engine
    */
   public int getDETop()
   {
      int drawTop = getParent().getDrawTop();
      int top = getTop();
      
      //ETSystem.out.println("Draw Top = " + drawTop + " Top = " + top);
      return drawTop + top;
      //return top;
      
   }
   
   /**
    * Get the logical top location
    */
   public int getLogicalTop()
   {
      int drawEngineTop = getEngineLogicalBoundingRect().getTop();
      return drawEngineTop - getDETop();
      
   }

   /**
    * Get the value that is used to restrict the upward movement of the accordion tool
    */
   public int getRestrictedY()
   {
      return getLogicalTop();
   }
   
   public void setLogicalTop(int value)
   {
      int compartmentTop = getCompartmentLogicalBoundingRect().getTop();
      if(value > compartmentTop)
      {
         value = compartmentTop;
      }
      
      setTop(compartmentTop - value);
   }
   
   /**
    * Get logical bounding rect for this piece.
    */
   public IETRect getLogicalBoundingRect()
   {
      IETRect compartmentRect = getCompartmentLogicalBoundingRect();
      
      Point logicalToLeft = new Point(compartmentRect.getLeft() + getTop(),
                                      compartmentRect.getTop() - getY());
      
      return new ETRect(logicalToLeft, getDrawSize());
   }
}
