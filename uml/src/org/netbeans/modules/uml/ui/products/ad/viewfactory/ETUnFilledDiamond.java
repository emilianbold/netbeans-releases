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

import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineArrowheadKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import com.tomsawyer.editor.TSEColor;
import java.awt.geom.GeneralPath;
import java.awt.Polygon;
import java.awt.Rectangle;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;
import java.awt.geom.GeneralPath;

public class ETUnFilledDiamond extends ETUnFilledArrow {

	public ETUnFilledDiamond()
	{
		super(DrawEngineArrowheadKindEnum.DEAK_UNFILLEDDIAMOND);
	}

	protected ETUnFilledDiamond(int kind)
	{
		super(kind);
	}

	protected boolean isFilled() { return false; }
	protected boolean isOpenRegion() { return false;};

	public boolean draw(IDrawInfo pInfo, TSConstPoint fromPt, TSConstPoint toPt, TSEColor color)
	{
		return super.draw(pInfo, fromPt, toPt, isFilled() ? color : TSEColor.white);
	}

	public GeneralPath getShape(IDrawInfo pInfo, TSConstPoint fromPt, TSConstPoint toPt)
	{
		TSTransform transform = pInfo.getTSTransform();
		double fromX = fromPt.getX();
		double fromY = fromPt.getY();
		double toX = toPt.getX();
		double toY = toPt.getY();

		float edgeVectorX = transform.widthToDevice(toX - fromX);
		float edgeVectorY = transform.heightToDevice(toY - fromY);
		float edgeLength =   (float) Math.sqrt(
		   (edgeVectorX * edgeVectorX) + (edgeVectorY * edgeVectorY));

		 // avoid divide-by-zero.

		 if (edgeLength == 0.0f)
		 {
			return null;
		 }

		int lineFromX = transform.xToDevice(fromX);
		int lineFromY = transform.yToDevice(fromY);
		int lineToX = transform.xToDevice(toX);
		int lineToY = transform.yToDevice(toY);

		float halfArrowWidth =	transform.widthToDevice(this.getWidth()) / 2;
		float arrowHeight = transform.heightToDevice(this.getHeight());

		// normalize the edge vector components.

		edgeVectorX /= edgeLength;
		edgeVectorY /= edgeLength;

		// calculate the vector from the center of the base to
		// the arrow tip

		float arrowHeightX = arrowHeight * edgeVectorX;
		float arrowHeightY = arrowHeight * edgeVectorY;

		// calculate the vector from the base center point to
		// the edge of the base

		float halfBaseVectorX = halfArrowWidth * edgeVectorY;
		float halfBaseVectorY = halfArrowWidth * edgeVectorX;

		// draw GeneralPath (polyline)

		GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

		polyline.moveTo(lineToX - (int) (arrowHeightX - halfBaseVectorX),
			lineToY + (int) (arrowHeightY + halfBaseVectorY));

		polyline.lineTo(lineToX, lineToY);

		polyline.lineTo(
		 lineToX - (int) (arrowHeightX + halfBaseVectorX),
		 lineToY + (int) (arrowHeightY - halfBaseVectorY));

		polyline.lineTo(
		 lineToX - (int) (arrowHeightX * 2),
		 lineToY + (int) (arrowHeightY * 2));

		polyline.closePath();
		return polyline;
	}

	public int getDefaultHeight() {
		return super.getDefaultHeight() /2;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETArrowHead#getDefaultWidth()
	 */
	public int getDefaultWidth() {
		return 8;
	}

}
