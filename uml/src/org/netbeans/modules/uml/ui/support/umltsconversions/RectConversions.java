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



package org.netbeans.modules.uml.ui.support.umltsconversions;

import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
//import com.tomsawyer.jnilayout.TSSide;
import org.netbeans.modules.uml.ui.support.TSSide;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
//import com.tomsawyer.util.TSConstSize;
import com.tomsawyer.drawing.geometry.TSConstSize;
//import com.tomsawyer.util.TSPoint;
import com.tomsawyer.drawing.geometry.TSPoint;
//import com.tomsawyer.util.TSRect;
import com.tomsawyer.drawing.geometry.TSRect;

/**
 * 
 * @author Trey Spiva
 */
public class RectConversions
{
	public static IETRect newETRect(TSConstRect rect)
	{
		return rect != null ? new ETRect(rect.getLeft(), rect.getBottom(), rect.getWidth(), rect.getHeight()) : null;
	}

	public static TSRect etRectToTSRect(IETRect rect)
	{
		if (rect != null)
		{
			ETRect rectLogical = ETRect.ensureLogicalRect(rect);
			return new TSRect(rectLogical.getLeft(), rectLogical.getTop(), rectLogical.getRight(), rectLogical.getBottom());
		}
		else
			return null;
	}

	/**
	 * Creates a rectangle that is the union of the two input rectangles
	 *
	 * @param pRect1 [in] Pointer to the IETRect interface that contains the first source rectangle
	 * @param pRect2 [in] Pointer to the IETRect interface that contains the second source rectangle
	 * @return The created IETRect that contains the union of the source rectangles, using TS coordinates 
	 *
	 * @note If either of the two source retangles contain all zero values, then it is not used in the union.
	 *
	 * @return HRESULT
	 */
	public static IETRect unionTSCoordinates(IETRect pRect1, IETRect pRect2)
	{
		IETRect retVal = null;

		if ((pRect1 != null) && (pRect2 != null))
		{
			retVal = (IETRect) pRect1.clone();
			if (retVal != null)
			{
				retVal.unionWith(pRect2);
			}
		}

		return retVal;
	}

	public static boolean moveToNearestPoint(IETRect thisRect, TSPoint point)
	{
		if (thisRect != null && point != null)
		{
			ETRect rectLogical = ETRect.ensureLogicalRect(thisRect);
			TSConstRect rect = new TSConstRect(rectLogical.getLeft(), rectLogical.getTop(), rectLogical.getRight(), rectLogical.getBottom());
			return moveToNearestPoint(rect, point);
		}
		return false;
	}

	// don't break!  Has lots of edge cases, all of them currently work
	public static boolean moveToNearestPoint(TSConstRect thisRect, TSPoint point)
	{
		double dleft = Math.abs(thisRect.getLeft() - point.getX());
		double dright = Math.abs(thisRect.getRight() - point.getX());
		double dtop = Math.abs(thisRect.getTop() - point.getY());
		double dbottom = Math.abs(thisRect.getBottom() - point.getY());

		boolean moving = false;

		if (dleft != 0 && dright != 0)
			moving = true;
		else if (dtop != 0 && dbottom != 0)
		{
			moving = true;
		}

		if (moving == false)
			return moving;

		double dx = Math.min(dleft, dright);
		double dy = Math.min(dtop, dbottom);
		boolean insideX = thisRect.withinXRange(point);
		boolean insideY = thisRect.withinYRange(point);

		if (insideX && insideY)
		{
			if (dx <= dy)
			{
				if (dleft < dright)
					point.setX(thisRect.getLeft());
				else
					point.setX(thisRect.getRight());
			}
			else
			{
				if (dtop < dbottom)
					point.setY(thisRect.getTop());
				else
					point.setY(thisRect.getBottom());
			}

			return moving;
		}

		if (insideX)
		{
			if (dtop < dbottom)
				point.setY(thisRect.getTop());
			else
				point.setY(thisRect.getBottom());
		}
		else if (insideY)
		{
			if (dleft < dright)
				point.setX(thisRect.getLeft());
			else
				point.setX(thisRect.getRight());
		}
		else
		{
			if (dtop < dbottom)
				point.setY(thisRect.getTop());
			else
				point.setY(thisRect.getBottom());
			if (dleft < dright)
				point.setX(thisRect.getLeft());
			else
				point.setX(thisRect.getRight());
		}

		return moving;
	}

	public static /* TSSide */
	int getClosestSide(TSConstRect rect, TSConstPoint point)
	{
		double dtop = Math.abs(rect.getTop() - point.getY());
		double dbottom = Math.abs(rect.getBottom() - point.getY());
		double dleft = Math.abs(rect.getLeft() - point.getX());
		double dright = Math.abs(rect.getRight() - point.getX());

		double dy = Math.min(dtop, dbottom);
		double dx = Math.min(dleft, dright);

		if (dx > dy)
		{
			if (dtop <= dbottom)
				return TSSide.TS_SIDE_TOP;
			else
				return TSSide.TS_SIDE_BOTTOM;
		}
		else
		{
			if (dleft < dright)
				return TSSide.TS_SIDE_LEFT;
			else
				return TSSide.TS_SIDE_RIGHT;
		}
	}

	/* 
	 * Inflate Logical rects from the center point.
	 */
	public static TSRect inflate(TSConstRect logicalRect, double dx, double dy)
	{
		if (logicalRect != null)
		{
			TSRect inflatedRect = new TSRect();
			TSConstSize clipSize = new TSConstSize(logicalRect.getWidth() + dx, logicalRect.getHeight() + dy);
			inflatedRect.setBoundsFromCenter(logicalRect.getCenter(), clipSize);	
			return inflatedRect;			
		}
		return null;	
	}
}
