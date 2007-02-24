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
import java.awt.geom.AffineTransform;
import java.awt.Color;

import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETArrowHead;
import com.tomsawyer.editor.TSEColor;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
import com.tomsawyer.editor.graphics.TSEGraphics;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;

public abstract class ETArrowHead implements IETArrowHead {

	/**
	 * 	Protectect constructor, this class is abstract.
	 */
	protected ETArrowHead(int nKind)
	{		
		super();
		kind = nKind;
	}
	
	/* (non-Javadoc)
	 * Please note you must have color selected in the Graphics object, the color param is used to fill interior regions.
	 */
	public boolean draw(IDrawInfo pInfo, TSConstPoint fromPt, TSConstPoint toPt, TSEColor color)
	{
		Shape shape = getShape(pInfo, fromPt, toPt);
		if (shape == null)
			return false;
		
		boolean didDraw;
		AffineTransform rotationTransform = getRotationTransform(pInfo, fromPt, toPt);
				
		TSEGraphics dc = pInfo.getTSEGraphics();
		AffineTransform prevTransform;
		
		if (rotationTransform != null)
		{	
			// Save the current transform
			prevTransform = dc.getTransform();
			dc.setTransform(rotationTransform);
		}
		else
			prevTransform = null;
				
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

		// restore the transform
		if (rotationTransform != null)
			dc.setTransform(prevTransform);
			
		return didDraw;
	}
	
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETArrowHead#getKind()
	 */
	public int getKind() {
		return kind;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETArrowHead#setHeight(int)
	 */
	public void setHeight(int height) {
		this.m_height = height;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETArrowHead#setWidth(int)
	 */
	public void setWidth(int width) {
		this.m_width = width;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETArrowHead#getHeight()
	 */
	public int getHeight() {
		return m_height == 0 ? getDefaultHeight() : m_height;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETArrowHead#getWidth()
	 */
	public int getWidth() {		
		return m_width == 0 ? getDefaultWidth() : m_width;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETArrowHead#getDefaultHeight()
	 */
	public int getDefaultHeight() {
		return 10;  // Just a guess.
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETArrowHead#getDefaultWidth()
	 */
	public int getDefaultWidth() {
		return 8; // Just a guess.
	}
	
	/*
	 * return true if your closed and filled.
	 */
	protected boolean isFilled() {return isOpenRegion() == false; }

	/*
	 *  return true if you don't closed region. 
	 */
	protected abstract boolean isOpenRegion();
	
	// Derived classes must determine their shape.
	public abstract Shape getShape(IDrawInfo pInfo, TSConstPoint fromPt, TSConstPoint toPt);
	
	/*
	 * Some arrow heads mgiht require rotation to drawn with respect to the line seqment direction.
	 */
	protected boolean requiresRotation() { return false; }

	protected AffineTransform getRotationTransform(IDrawInfo pInfo, TSConstPoint fromPt, TSConstPoint toPt)
	{
		if (requiresRotation())
		{
			TSTransform transform = pInfo.getTSTransform();
			double rotateX = (double)transform.xToDevice((double)toPt.getX());
			double rotateY =(double)transform.yToDevice((double)toPt.getY());
			return pInfo.getTSEGraphics().getTransform().getRotateInstance(getTheta(
				fromPt, toPt), rotateX, rotateY);
		}
		return null;
	}

	/**
	 * Calculates the angle of a line in radians defined by points 1 and 2, with 
	 * point 1 at the origin.  The atan function alone is not sufficient because 
	 * it only returns values in the range of -pi/2 < theta < pi/2.
	 */
	public static double getTheta(double x1, double x2, double y1, double y2)
	{
		double theta=0.0f;
		double pi = (double)Math.PI;
    
		if (x2==x1) // Can't div by zero.
		{
			if (y2 > y1)
				theta = pi/2.0f;
			if (y2 < y1)
				theta = 3.0f*pi/2.0f;
		}
		else
		{
			double slope = (y2-y1)/(x2-x1);
			theta = (float)Math.atan(slope);
			// if point 2 is to the left of point1, then it's in the 2nd or 3rd quadrants
			// and must add pi radians to angle
			if (x2 < x1)
				theta += pi;
		}
 		return theta;
	}
		
	/*
	 * the angle of rotation in radians
	 */
	protected double getTheta(TSConstPoint fromPt, TSConstPoint toPt)
	{
		return getTheta(fromPt.getX(), toPt.getX(), fromPt.getY(), toPt.getY());
	}
	
	protected int m_width = 0;
	protected int m_height = 0;
	protected int kind;	
}
