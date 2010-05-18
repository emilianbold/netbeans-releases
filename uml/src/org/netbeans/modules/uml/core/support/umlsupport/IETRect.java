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

import java.awt.Point;
import java.awt.Rectangle;

public interface IETRect extends Cloneable
{
	/**
	 * The left side of the rectangle
	*/
	public int getLeft();

	/**
	 * The left side of the rectangle
	*/
	public void setLeft(int value);

	/**
	 * The top side of the rectangle
	*/
	public int getTop();

	/**
	 * The top side of the rectangle
	*/
	public void setTop(int value);

	/**
	 * The right side of the rectangle
	*/
	public int getRight();

	/**
	 * The right side of the rectangle
	*/
	public void setRight(int value);

	/**
	 * The bottom side of the rectangle
	*/
	public int getBottom();

	/**
	 * The bottom side of the rectangle
	*/
	public void setBottom(int value);

	/**
	 * The sides of the rectangle
	*/
	public void setSides(int nLeft, int nTop, int nRight, int nBottom);
	
	/*
	 * Set the corners of the retangle
	 */
	public void setCorners(IETPoint topLeft, IETPoint bottomRight);
	
	/**
	 * Is the input rectangle fully contained in this rectangle.
	*/
	public boolean isContained(IETRect pOtherRect);

	/**
	 * Normalize the rectangle.  +y is always up.
	*/
	public void normalizeRect();

	/**
	 * Normalize the rectangle.  +y is down unless bInvertY is <code>true</code>.
	*/
	public void normalizeRect(boolean bInvertY);

	/**
	 * Unions this rectangle with the argument one.
	*/
	public void unionWith(IETRect pOtherRect);

	/**
	 * Intersects this rectangle with the argument one.  If no intersection then 
    * this rect is set to 0,0,0,0 and bFoundIntersection is FALSE
	*/
	public boolean intersectWith(IETRect pOtherRect);

	/**
	 * Returns whether or not this rect intersects the argument one.  This rect 
    * is not changed, unlink IntersectWith.  bFoundIntersection is FALSE if no 
    * intersection.
	*/
	public boolean doesIntersect(IETRect pOtherRect);
   
   /** 
    * Checks whether or not this Rectangle contains the point at the specified 
    * location (x, y). 
    * 
    * @param p the Point to test.
    * @return <code>true</code> if the point (x, y) is inside this Rectangle; 
    *         <code>false</code> otherwise.
    */
	public boolean contains(Point p);
   
   /** 
    * Checks whether or not this Rectangle contains the point at the specified 
    * location (x, y). 
    * 
    * @param p the Point to test.
    * @return <code>true</code> if the point (x, y) is inside this Rectangle; 
    *         <code>false</code> otherwise.
    */
   public boolean contains(IETPoint p);
      
   /** 
    * Checks whether or not this Rectangle entirely contains the specified 
    * rectangle. 
    * 
    * @param rect the specified Rectangle.
    * @return <code>true</code> if the Rectangle is contained entirely inside 
    *         this Rectangle; <code>false</code> otherwise.
    */
   public boolean contains(IETRect rect);
      
   /** 
    * Checks whether or not this Rectangle contains the point at the specified 
    * location (x, y). 
    * 
    * @param x the specified x coordinate.
    * @param y the specified y coordinate.
    * @return <code>true</code> if the point (x, y) is inside this Rectangle; 
    *         <code>false</code> otherwise.
    */
   public boolean contains(double x, double y);

	/**
	 * Returns the width.
	*/
	public double getWidth();

	/**
	 * Returns the height.
	*/
	public double getHeight();

	public int getIntHeight();

	public int getIntWidth();

	public int getIntX();

	public int getIntY();

	/**
	 * Returns the X coordinate of the center of the framing rectangle of the
	 * rectangle in double precision.
	 *  
	 * @return The X coordinate of the framing rectangle of the Rectangle object's 
	 *         center.
	 */
	public double getCenterX();

	/**
	 * Returns the Y coordinate of the center of the framing rectangle of the
	 * rectangle in double precision.
	 *  
	 * @return The Y coordinate of the framing rectangle of the Rectangle object's 
	 *         center.
	 */
	public double getCenterY();

	/**
	 * Inflates the rectangle in both the X and Y axis.
	 * 
	 * @param size The size to grow the rectangle.
	 * @see #inflate(int, int)
	 */
	public void inflate(int size);

	/**
	 * Inflates the rectangle in both the X and Y axis.
	 * 
	 * @param xSize The size that grow the left and right sides of the rectangle.
	 * @param xSize The size that grow the top and bottom sides of the rectangle.
	 */
	public void inflate(int xSize, int ySize);

	public Rectangle getRectangle();

	public Point getTopLeft();
	public Point getBottomRight();
	public Point getTopRight();
	public Point getBottomLeft();
	public Point getCenterPoint();
	public void deflateRect(int left, int top, int right, int bottom);
	public void deflateRect(int dx, int dy);
	public boolean isZero();
   public void offsetRect(int dx, int dy);
   public void offsetRect( Point point );
   
   /// sets the internal variables so isZero() returns true
   public void setRectEmpty();
   
   /// allows us to copy the underlying data without changing the internal data
   public Object clone();
}
