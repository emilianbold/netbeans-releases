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

package org.netbeans.modules.uml.core.support.umlsupport;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import org.netbeans.modules.uml.core.support.Debug;

/**
 * @author brettb
 *
 */
public class ETDeviceRect extends Rectangle implements IETRect
{

   /**
    *
    */
   public ETDeviceRect()
   {
      super();
   }

   public ETDeviceRect( int x, int y, int width, int height )
   {
      super( x, y, width, height );
   }

   public ETDeviceRect( Point topLeft, Dimension dimension )
   {
      super( topLeft, dimension );
   }

   /*
    * This constructor does not assume the direction of the y axis.
    * Instead it sets the vertical values (y & height) from the given inputs
    */
   public ETDeviceRect( Point ptTopLeft, Point ptBottomRight )
   {
      final int iLeft = ptTopLeft.x;
      final int iTop = ptTopLeft.y;
      final int iRight = ptBottomRight.x;
      final int iBottom = ptBottomRight.y;
      
      x = Math.min( iLeft, iRight );
      y = Math.min( iTop, iBottom );
      width =  Math.abs( iRight - iLeft );
      height = Math.abs( iBottom - iTop );
   }
   
   public ETDeviceRect( Rectangle rect )
   {
      super( rect );
   }
   
   // we protect this call so that it is not accidentally used
   protected ETDeviceRect( IETRect rect )
   {
      x = rect.getLeft();
      y = ( rect instanceof ETDeviceRect ) ? rect.getTop() : rect.getBottom();
      width = rect.getIntWidth();
      height = rect.getIntHeight();
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#getLeft()
    */
   public int getLeft()
   {
      return x;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#setLeft(int)
    */
   public void setLeft(int value)
   {
      x = value;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#getTop()
    */
   public int getTop()
   {
      return y;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#setTop(int)
    */
   public void setTop(int value)
   {
      y = value;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#getRight()
    */
   public int getRight()
   {
      return x + width;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#setRight(int)
    */
   public void setRight(int value)
   {
      width = value - x;
      //Debug.assertTrue( width >= 0 );
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#getBottom()
    */
   public int getBottom()
   {
      // The y axis points down
      return y + height;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#setBottom(int)
    */
   public void setBottom(int value)
   {
      // The y axis points down
      height = value - y;
      //Debug.assertTrue( height >= 0 );
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#setSides(int, int, int, int)
    */
   public void setSides(int nLeft, int nTop, int nRight, int nBottom)
   {
      x = nLeft;
      y = nTop;
      setRight( nRight );
      setBottom( nBottom );
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#setCorners(org.netbeans.modules.uml.core.support.umlsupport.IETPoint, org.netbeans.modules.uml.core.support.umlsupport.IETPoint)
    */
   public void setCorners(IETPoint topLeft, IETPoint bottomRight)
   {
      setSides( topLeft.getX(), topLeft.getY(), bottomRight.getX(), bottomRight.getY() );
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#isContained(org.netbeans.modules.uml.core.support.umlsupport.IETRect)
    */
   public boolean isContained(IETRect pOtherRect)
   {
      return contains( pOtherRect );
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#normalizeRect()
    */
   public void normalizeRect()
   {
      normalizeRect(true);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#normalizeRect(boolean)
    */
   public void normalizeRect(boolean bInvertY)
   {
      if ( bInvertY )
      {
         y = Math.min(getTop(), getBottom());
      }
      else
      {
         y = Math.max(getTop(), getBottom());
      }

      if (width < 0)
      {
         x += width;
         width = -width;
      }

      if (height < 0)
      {
         y += height;
         height = -height;
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#unionWith(org.netbeans.modules.uml.core.support.umlsupport.IETRect)
    */
   public void unionWith(IETRect pOtherRect)
   {
      ETDeviceRect rect = ensureDeviceRect( pOtherRect );
      ((Rectangle)this).union((Rectangle)rect);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#intersectWith(org.netbeans.modules.uml.core.support.umlsupport.IETRect)
    */
   public boolean intersectWith(IETRect pOtherRect)
   {
      unionWith( pOtherRect );
      return !isEmpty();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#doesIntersect(org.netbeans.modules.uml.core.support.umlsupport.IETRect)
    */
   public boolean doesIntersect(IETRect pOtherRect)
   {
      ETDeviceRect rect = ensureDeviceRect( pOtherRect );
      return ((Rectangle)this).intersects((Rectangle)rect);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#contains(org.netbeans.modules.uml.core.support.umlsupport.IETPoint)
    */
   public boolean contains(IETPoint p)
   {
      return ((Rectangle)this).contains( p.asPoint( ));
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#contains(org.netbeans.modules.uml.core.support.umlsupport.IETRect)
    */
   public boolean contains(IETRect rectOther)
   {
      ETDeviceRect rect = ensureDeviceRect( rectOther );
      return ((Rectangle)this).contains((Rectangle)rect);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#getIntHeight()
    */
   public int getIntHeight()
   {
      return height;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#getIntWidth()
    */
   public int getIntWidth()
   {
      return width;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#getIntX()
    */
   public int getIntX()
   {
      return x;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#getIntY()
    */
   public int getIntY()
   {
      return y;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#inflate(int)
    */
   public void inflate(int size)
   {
      inflate( size, size );
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#inflate(int, int)
    */
   public void inflate(int xSize, int ySize)
   {
      x -= xSize;
      y -= ySize;
      width +=  (2 * xSize);
      height += (2 * ySize);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#getRectangle()
    */
   public Rectangle getRectangle()
   {
      return this;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#getTopLeft()
    */
   public Point getTopLeft()
   {
      return new Point( getLeft(), getTop() );
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#getBottomRight()
    */
   public Point getBottomRight()
   {
      return new Point( getRight(), getBottom() );
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#getTopRight()
    */
   public Point getTopRight()
   {
      return new Point( getRight(), getTop() );
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#getBottomLeft()
    */
   public Point getBottomLeft()
   {
      return new Point( getLeft(), getBottom() );
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#getCenterPoint()
    */
   public Point getCenterPoint()
   {
      return new Point( getLeft() + width/2, getTop() + height/2 );
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#deflateRect(int, int, int, int)
    */
   public void deflateRect(int left, int top, int right, int bottom)
   {
      x += left;
      y += top;
      width -=  (left + right);
      height -= (top + bottom);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#deflateRect(int, int)
    */
   public void deflateRect(int dx, int dy)
   {
      deflateRect( dx, dy, dx, dy );
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#isZero()
    */
   public boolean isZero()
   {
      return ((0 == x) && (0 == y) && (0 == width) && (0 == height));
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#offsetRect(int, int)
    */
   public void offsetRect(int dx, int dy)
   {
      x += dx;
      y += dy;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#offsetRect(IETPoint)
    */
   public void offsetRect( Point point )
   {
      offsetRect( point.x, point.y );
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#setRectEmpty()
    */
   public void setRectEmpty()
   {
      x = 0;
      y = 0;
      width = 0;
      height = 0;
   }

   public static ETDeviceRect ensureDeviceRect( IETRect rect )
   {
      ETDeviceRect rectDR = null;
      
      if( rect != null )
      {
         if (rect instanceof ETDeviceRect)
         {
            rectDR = (ETDeviceRect)rect;
         }
         else
         {
            rectDR = new ETDeviceRect( rect );
         }
      }
      
      return rectDR;
   }
   
   /// allows us to copy the underlying data without changing the internal data
   public Object clone()
   {
      return new ETDeviceRect( (Rectangle)this );
   }
}


