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

package org.netbeans.modules.uml.core.support.umlsupport;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

public class ETRect extends Rectangle implements IETRect {
	public ETRect() {
		super();
	}

	public ETRect(int width, int height) {
		super(width, height);
	}

	public ETRect(double x, double y, double width, double height) {
		this((int) x, (int) y, (int) width, (int) height);
	}

	public ETRect(int x, int y, int width, int height) {
		super(x, y, width, height);
	}

	public ETRect(Dimension d) {
		super(d);
	}

	public ETRect(Point p) {
		super(p);
	}

	public ETRect(Rectangle r) {
		super(r);
	}

   // we protect this call so that it is not accidentally used
	protected ETRect(IETRect rect)
   {
		if (rect != null)
      {
			x = rect.getLeft();
			y = (rect instanceof ETRect) ? rect.getTop() : rect.getBottom();
			width = rect.getIntWidth();
			height = rect.getIntHeight();
		}
      else
      {
			setRectEmpty();
		}
	}

	public ETRect(Point p, Dimension d) {
		super(p, d);
	}

	public ETRect(Point topLeft, Point bottomRight) {
		super(topLeft);
		add(bottomRight);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#doesIntersect(org.netbeans.modules.uml.core.support.umlsupport.IETRect)
	 */
	public boolean doesIntersect(IETRect pOtherRect) {
		return pOtherRect != null ? intersects(pOtherRect.getRectangle()) : isZero();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#getBottom()
	 */
	public int getBottom() {
		// This value is allowed to be negative
		return getIntY() - getIntHeight();
	}

	public int getIntHeight() {
		return super.height;
	}

	public int getIntWidth() {
		return super.width;
	}

	public int getIntX() {
		return super.x;
	}

	public int getIntY() {
		return super.y;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#getLeft()
	 */
	public int getLeft() {
		return getIntX();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#getRight()
	 */
	public int getRight() {
		return getIntX() + getIntWidth();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#getTop()
	 */
	public int getTop() {
		return getIntY();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#intersectWith(org.netbeans.modules.uml.core.support.umlsupport.IETRect)
	 */
	public boolean intersectWith(IETRect pOtherRect) {
		return doesIntersect(pOtherRect);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#isContained(org.netbeans.modules.uml.core.support.umlsupport.IETRect)
	 */
	public boolean isContained(IETRect pOtherRect) {
		return pOtherRect != null ? pOtherRect.contains(this) : false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#normalizeRect()
	 */
	public void normalizeRect() {
		normalizeRect(true);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#normalizeRect(boolean)
	 */
	public void normalizeRect(boolean bInvertY) {
		x = Math.min(getLeft(), getRight());

		if (bInvertY == true) {
			y = Math.min(getTop(), getBottom());
		} else {
			y = Math.max(getTop(), getBottom());
		}

		if (width < 0) {
			x += width;
			width = -width;
		}

		if (height < 0) {
			y += height;
			height = -height;
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#setSides(int, int, int, int)
	 */
	public void setSides(int nLeft, int nTop, int nRight, int nBottom) {
		x = nLeft;
		y = nTop;
		width = Math.abs(nRight - nLeft);
		height = Math.abs(nBottom - nTop);
	}

	/**
	 * Setting the bottom of the rectangle affects the height of the rectangle.
	 * 
	 * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#setBottom(int)
	 */
	public void setBottom(int value) {
		//changeY(value);
		//normalizeRect();
		height = Math.abs(value - y);
	}

	protected void changeY(int value) {
		int min = Math.min(y, value);
		int max = Math.max(y, value);

		y = min;
		height = max - min;
	}

	/**
	 * Setting the left side affects the x position of the rectangle.
	 * 
	 * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#setLeft(int)
	 */
	public void setLeft(int value) {
		//changeX(value);
		x = value;
	}

	protected void changeX(int value) {
		int min = Math.min(x, value);
		int max = Math.max(x, value);

		x = min;
		width = max - min;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#setRight(int)
	 */
	public void setRight(int value) {
		//normalizeRect();
		width = Math.abs(getLeft() - value);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#setTop(int)
	 */
	public void setTop(int value) {
		//changeY(value);
		y = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#unionWith(org.netbeans.modules.uml.core.support.umlsupport.IETRect)
	 */
	public void unionWith(IETRect pOtherRect)
   {
		if (pOtherRect != null)
      {
         if( isZero() )
         {
            x = pOtherRect.getLeft();
            y = (pOtherRect instanceof ETRect) ? pOtherRect.getTop() : pOtherRect.getBottom();
            width = pOtherRect.getIntWidth();
            height = pOtherRect.getIntHeight();
         }
         else
         {
            ETRect normalOtherRect = new ETRect(pOtherRect);
            normalOtherRect.normalizeRect(false);
            normalizeRect(false);

            int right = Math.max(getRight(), normalOtherRect.getRight());
            int bottom = Math.min(getBottom(), normalOtherRect.getBottom());

            x = Math.min(getLeft(), normalOtherRect.getLeft());
            y = Math.max(getTop(), normalOtherRect.getTop());

            height = y - bottom;
            width = right - x;
         }
		}
	}

	public boolean contains(Point p) {
		return contains((double) p.x, (double) p.y);
	}

	public boolean contains(IETPoint p) {
		return p != null ? contains(p.asPoint()) : false;
	}

	public boolean contains(IETRect rect)
   {
		return contains(rect.getTopLeft()) && contains(rect.getBottomRight());
	}

	public boolean contains(double x, double y)
   {
		// Because ETRect represents a rectangle with the axis going up,
		// we can not use our super classes contains(),
		// and must perform the calculation ourselves.
		return (x >= getLeft()) && (x <= getRight()) && (y <= getTop()) && (y >= getBottom());
	}

	public String toString() {
		return "[x=" + x + ",y=" + y + ",width=" + width + ",height=" + height + "]";
	}

	/**
	 * Inflates the rectangle in both the X and Y axis.
	 * 
	 * @param size The size to grow the rectangle.
	 * @see #inflate(int, int)
	 */
	public void inflate(int size) {
		inflate(size, size);
	}

	/**
	 * Inflates the rectangle in both the X and Y axis.
	 * 
	 * @param xSize The size that grow the left and right sides of the rectangle.
	 * @param xSize The size that grow the top and bottom sides of the rectangle.
	 */
	public void inflate(int xSize, int ySize) {
		// normalizeRect(true);
		x -= xSize;
		
		if (getTop() > getBottom())
			y += ySize;
		else
			y -= ySize;
			
		width += (xSize * 2);
		height += (ySize * 2);
	}

	/**
	 *
	 * @return AWT Window class Rectangle repesenting this IETRect.
	 */
	public Rectangle getRectangle() {		
		return this;
	}

	public Point getTopLeft() {
		return new Point(this.getLeft(), this.getTop());
	}

	public Point getBottomRight() {
		return new Point(this.getRight(),this.getBottom());
	}

	public Point getTopRight() {
		return new Point(this.getRight(), this.getTop());
	}

	public Point getBottomLeft() {
		return new Point(this.getLeft(), this.getBottom());
	}

   public Point getCenterPoint()
   {
      return new Point((int) (getLeft() + this.getWidth() / 2), (int) ((getTop() + getBottom()) / 2));
   }

   public double getCenterY()
   {
      // We override this operation so that the RectangularShape's code is not used,
      // because this formula works for which ever way the axis is pointing.
      return ((getTop() + getBottom()) / 2);
   }

	public void deflateRect(int left, int top, int right, int bottom) {
		this.setLeft(this.getLeft() + left);
		this.setTop(this.getTop() + top);
		this.setRight(this.getRight() - right);
		this.setBottom(this.getBottom() - bottom);
	}

	public void deflateRect(int dx, int dy) {
		int top = Math.min(getTop(), getBottom()) + getIntHeight();
		int bottom = Math.max(getTop(), getBottom()) + getIntHeight();
		int ileft = getLeft();
		int iright = getRight();
		this.setLeft(ileft + dx);
		this.setTop(top + dy);
		this.setRight(iright - dx);
		this.setBottom(bottom - dy);
	}

	public boolean isZero() {
		return !(this.getX() != 0 || this.getY() != 0 || this.getWidth() != 0 || this.getHeight() != 0);
	}

	public void setCorners(IETPoint topLeft, IETPoint bottomRight) {
		setLeft(topLeft.getX());
		setTop(topLeft.getY());
		setRight(bottomRight.getX());
		setBottom(bottomRight.getY());
	}

	/*
	 * 
	 */
	public void offsetRect(int dx, int dy) {
		ETPoint topLeft = new ETPoint(this.getLeft(), this.getTop());
		topLeft.offset(dx, dy);
		ETPoint bottomRight = new ETPoint(this.getBottom(), this.getRight());
		bottomRight.offset(dx, dy);
		setCorners(topLeft, bottomRight);
	}

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlsupport.IETRect#offsetRect(IETPoint)
    */
   public void offsetRect( Point point )
   {
      offsetRect( point.x, point.y );
   }

	/*
	 *  sets the internal variables so isZero() returns true
	 */
	public void setRectEmpty() {
		x = 0;
		y = 0;
		width = 0;
		height = 0;
	}

	public static ETRect ensureLogicalRect(IETRect rect) {
		if (rect instanceof ETRect) {
			return (ETRect) rect;
		} else {
			return rect != null ? new ETRect(rect) : null;
		}
	}
   
   // This function is used when we know the ETRect was created using
   // device coordinates for the x, y location, i.e. the y is the top left
   // An example is in ETCompartment.getBoundingAsDeviceRect() using the compartments bounding rect
   public ETDeviceRect getAsDeviceRect()
   {
      return new ETDeviceRect( x, y, width, height );
   }
   
   /// allows us to copy the underlying data without changing the internal data
   public Object clone()
   {
      return new ETRect( (Rectangle)this );
   }
}
