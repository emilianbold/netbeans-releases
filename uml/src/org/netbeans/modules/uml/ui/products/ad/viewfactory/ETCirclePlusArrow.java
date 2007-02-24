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



package org.netbeans.modules.uml.ui.products.ad.viewfactory;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.Rectangle;
import java.awt.Color;

import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineArrowheadKindEnum;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
import com.tomsawyer.editor.TSEColor;
import com.tomsawyer.editor.graphics.TSEGraphics;

public class ETCirclePlusArrow extends ETArrowHead
{
	public ETCirclePlusArrow() {
		super(DrawEngineArrowheadKindEnum.DEAK_CIRCLE_WITH_PLUS);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.viewfactory.ETArrowHead#isOpenRegion()
	 */
	protected boolean isOpenRegion() {
		return false;
	}

	public boolean draw(IDrawInfo pInfo, TSConstPoint fromPt, TSConstPoint toPt, TSEColor color)
	{
		Rectangle dvRect = getCircleBounds(pInfo, fromPt, toPt);
		return drawCircle(pInfo, dvRect, TSEColor.white) && drawCross(pInfo, dvRect, color);
	}

	/*
	 * Draw the circle section of the shape.
	 */
	protected boolean drawCircle(IDrawInfo pInfo, Rectangle dvRect, TSEColor color)
	{
		Shape shape = getCircle(dvRect);
		TSEGraphics dc = pInfo.getTSEGraphics();
		boolean didDraw;
		if (shape != null)
		{
			if (isFilled() || isOpenRegion() == false)
			{
				Color prevColor = dc.getColor();
				dc.setColor(color);
				dc.fill(shape);
				dc.setColor(prevColor);
			}
			dc.draw(shape);
			didDraw = true;
		}
		else
			didDraw = false;

		return didDraw;
	}

	/*
	 * Draw the cross section of the shape.
	 */
	protected boolean drawCross(IDrawInfo pInfo, Rectangle dvRect, TSEColor color)
	{
		Shape shape = getCross(dvRect);
		TSEGraphics dc = pInfo.getTSEGraphics();
		boolean didDraw;
		if (shape != null)
		{
			dc.draw(shape);
			didDraw = true;
		}
		else
			didDraw = false;

		return didDraw;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETArrowHead#getDefaultHeight()
	 */
	public int getDefaultHeight()
	{
		return 14;  // Just a guess.
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETArrowHead#getDefaultWidth()
	 */
	public int getDefaultWidth()
	{
		return getDefaultHeight(); // We have to be a circle.
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.viewfactory.ETArrowHead#getShape(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, null, null)
	 */
	public Shape getShape(IDrawInfo pInfo, TSConstPoint fromPt, TSConstPoint toPt)
	{
		Rectangle dvRect = getCircleBounds(pInfo, fromPt, toPt);
                if (dvRect == null)
                  return null;

                GeneralPath finalShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

		Shape circle = getCircle(dvRect);
		Shape cross = getCross(dvRect);

		if (finalShape != null && circle != null && cross != null)
		{
			// Build the final comp, object.
			finalShape.append(circle, false);
			finalShape.append(cross, false);
		}
		return finalShape;
	}

	/*
	 * Returns the Circle shape given the circle device rectangle.
	 */
	protected Shape getCircle(Rectangle dvRect)
	{
          if (dvRect == null)
            return null;

		double max = Math.max(dvRect.getWidth(), dvRect.getHeight());
		return new Ellipse2D.Double(dvRect.getX(), dvRect.getY(), max, max);
	}

	/*
	 *  Returns the device rectangle the contains the circle.
	 */
	protected Rectangle getCircleBounds(IDrawInfo pInfo, TSConstPoint fromPt, TSConstPoint toPt)
	{
		// We use the filled diamond to detrimine the rectangle, it beats recalculating
		// all those vectors.
		ETArrowHead diamond = new ETFilledDiamond();
                if (diamond != null)
                {
                  diamond.setWidth(this.getWidth());
                  diamond.setHeight(this.getHeight() / 2);

                  Shape pShape = diamond.getShape(pInfo, fromPt, toPt);
                  return pShape != null ? pShape.getBounds() : null;
                 }
                return null;
	}

	/*
	 * Returns the cross shape given the circle bounding rectangle.
	 */
	protected Shape getCross(Rectangle dvRect)
	{
		int diamiter = (int)Math.max(dvRect.getWidth(), dvRect.getHeight());
		if (diamiter == 0)
			return null;

                GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

		int radius = diamiter /2;

		// Horizontal.
		polyline.moveTo((int)dvRect.x, (int)(dvRect.y + radius));
		polyline.lineTo((int)(dvRect.x + diamiter), (int)(dvRect.y + radius));

		// Vert.
		polyline.moveTo((int)(dvRect.x + radius), (int)dvRect.y);
		polyline.lineTo((int)(dvRect.x + radius), (int)(dvRect.y + diamiter));
		return polyline;
	}
}
