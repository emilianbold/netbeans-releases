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

import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSPoint;
import com.tomsawyer.drawing.geometry.TSPoint;

/**
 * @author sumitabhk
 *
 */
public class PointConversions
{

	/**
	 *
	 */
	public PointConversions()
	{
		super();
	}

	public static TSPoint newTSPoint(IDrawingAreaControl pCurrentEditor, IETPoint pInPoint)
	{
		if (pCurrentEditor != null)
		{
			return new TSPoint(pCurrentEditor.getGraphWindow().getTransform().xToWorld(pInPoint.getX()),
				pCurrentEditor.getGraphWindow().getTransform().yToWorld(pInPoint.getY()));
		}
		return null;
	}

	public static IETPoint newETPoint(TSConstPoint pInPoint)
	{
		return pInPoint != null ? new ETPointEx(pInPoint) : null;
	}

	public static IETPoint newETPoint(Point inPoint)
	{
		return inPoint != null ? new ETPoint(inPoint) : null;
	}

	public static Point ETPointToPoint(IETPoint pInPoint)
	{
		return pInPoint != null ? new Point(pInPoint.getX(), pInPoint.getY()) : null;
	}

	/*
	 * This method assumes that pInPoint is in Logical points, if its device use newTSPoint
	 */
	public static TSPoint ETPointToTSPoint(IETPoint pInPoint)
	{
		return pInPoint != null ? new TSPoint((double)pInPoint.getX(), (double)pInPoint.getY()) : null;
	}

}

