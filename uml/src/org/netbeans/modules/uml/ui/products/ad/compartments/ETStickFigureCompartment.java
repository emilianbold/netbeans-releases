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



package org.netbeans.modules.uml.ui.products.ad.compartments;

import java.awt.Color;
import java.awt.Point;

import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETRectEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IStickFigureCompartment;
import com.tomsawyer.editor.graphics.TSEGraphics;

/**
 * 
 * @author Trey Spiva
 */
public class ETStickFigureCompartment extends ETCompartment implements IStickFigureCompartment
{
	// The stick figure color
	protected int m_nStickFigureStringID = -1;
	// The stick figure head color
	protected int m_nStickFigureHeadColorStringID = -1;

	public ETStickFigureCompartment()
	{
		setIsTSWorldCoordinate(true);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#calculateOptimumSize(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, boolean)
	 */
	public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
	{
		return bAt100Pct ? new ETSize(50, 50) : this.scaleSize((IETSize) new ETSize(50, 50), pDrawInfo != null ? pDrawInfo.getTSTransform() : this.getTransform());
	}

	// The actor is a stick figure with these dimensions:
	// Head - Top 25%
	// Body - Middle 45%
	// Legs - Bottom 30%
	// Arms - 15% up from the middle of the body
	// Legs - 20% in from the sides of the box
	// The percentages are based on the size of the object - the size of the 
	// text to display

	protected final static double topPercent = 0.25;
	protected final static double middlePercent = 0.45;
	protected final static double armPctAboveBodyMiddle = 0.15;
	protected final static double legInFromSidePercent = 0.20;

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#draw(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, org.netbeans.modules.uml.core.support.umlsupport.IETRect)
	 */
	public void draw(IDrawInfo pDrawInfo, IETRect inBoundingRect)
	{
		if (this.isTSWorldCoordinate())      	
		    super.draw(pDrawInfo, new ETRectEx(pDrawInfo.getTSTransform().boundsToWorld(inBoundingRect.getRectangle())));
		else
		    super.draw(pDrawInfo, inBoundingRect);

		// fix for 6460801. To not let the stickfigure to widen beyond the default 2/5 proportion 
		// (that means to not let the shape defining bounding rectangle to widen beyond the default 2/3 proportion )
		// A user still has an option to resize the actor element to make stickfigure more slim than 2/5  
		// as there isn't an explicit reason to restrict the user from doing that. 
		IETRect pBoundingRect = (IETRect) inBoundingRect.clone();
		int inWidth   = (int) pBoundingRect.getWidth();	
		int calcWidth = (int) pBoundingRect.getHeight() * 2 / 3;
		if ((calcWidth < inWidth) && (calcWidth > 0)) {
		    int adj = ( inWidth - calcWidth ) / 2; 
		    pBoundingRect.setLeft(pBoundingRect.getLeft() + adj);
		    pBoundingRect.setRight(pBoundingRect.getLeft() + calcWidth);
		}

		Point centerPoint = pBoundingRect.getCenterPoint();
		double centerX = centerPoint.getX();
		double centerY = centerPoint.getY();

		// Create The top Rect.
		IETRect top = (IETRect) pBoundingRect.clone();
		IETPoint middleTop = new ETPoint();
		IETPoint middleBottom = new ETPoint();
		IETPoint legLeftBottom = new ETPoint();
		IETPoint legRightBottom = new ETPoint();
		IETPoint armLeft = new ETPoint();
		IETPoint armRight = new ETPoint();

		int currentHeight = (int) pBoundingRect.getHeight();

		// Create the top rectangle
		top.setBottom(top.getTop() + (int) ((float) currentHeight * topPercent));
		top.setLeft(top.getCenterPoint().x - (top.getIntHeight() / 2));
		top.setRight((int) (top.getLeft() + top.getHeight()));

		// Create the middle points
		middleTop.setY(top.getBottom());
		middleTop.setX(top.getCenterPoint().x);
		middleBottom.setY(middleTop.getY() + (int) ((float) currentHeight * middlePercent));
		middleBottom.setX(middleTop.getX());

		// Create the bottom points
		legLeftBottom.setX(pBoundingRect.getLeft() + (int) ((float) pBoundingRect.getWidth() * legInFromSidePercent));
		legLeftBottom.setY(pBoundingRect.getBottom());
		legRightBottom.setX(pBoundingRect.getRight() - (int) ((float) pBoundingRect.getWidth() * legInFromSidePercent));
		legRightBottom.setY(pBoundingRect.getBottom());

		// Create the arm points
		armLeft.setX((int) (pBoundingRect.getLeft() + pBoundingRect.getWidth() / 5));
		armLeft.setY(middleTop.getY() + (int) ((float) (((float) middleBottom.getY() - middleTop.getY()) / 2.0f) * (1.0 - armPctAboveBodyMiddle)));
		armRight.setX((int) (pBoundingRect.getRight() - pBoundingRect.getWidth() / 5));
		armRight.setY(armLeft.getY());

		TSEGraphics g = pDrawInfo.getTSEGraphics();
		Color figureColor = getFigureColor();
		g.setColor(figureColor);
		if (m_nStickFigureHeadColorStringID == -1)
			GDISupport.frameEllipse(g, top.getRectangle());	// let the background through.
		else
			GDISupport.drawEllipse(g,top.getRectangle(), figureColor,this.getHeadFillColor());
			
		GDISupport.drawLine(g, middleTop.asPoint(), middleBottom.asPoint());
		GDISupport.drawLine(g, middleBottom.asPoint(), legLeftBottom.asPoint());
		GDISupport.drawLine(g, middleBottom.asPoint(), legRightBottom.asPoint());
		GDISupport.drawLine(g, armLeft.asPoint(), armRight.asPoint());
	}

	/*
	 * Returns the Stick Color the default is a form of (Magenta)
	 */
	protected Color getFigureColor()
	{
		return new Color(m_ResourceUser.getCOLORREFForStringID(m_nStickFigureStringID));
	}

	protected Color getHeadFillColor()
	{
		return new Color(m_ResourceUser.getCOLORREFForStringID(m_nStickFigureHeadColorStringID));		
	}
	
	/**
	* This is the name of the drawengine used when storing and reading from the product archive.
	*
	* @param sID[out,retval] The unique name for this compartment.  Used when reading and writing the
	* product archive (etlp file).
	*/
	public String getCompartmentID()
	{
		return "StickFigureCompartment";
	}

	public void initResources()
	{
		// First setup our defaults in case the colors/fonts are not in the 
		// configuration file
		m_nStickFigureStringID = m_ResourceUser.setResourceStringID(m_nStickFigureStringID, "stickfigure", (new Color(255, 128, 255)).getRGB());
		m_nStickFigureHeadColorStringID = m_ResourceUser.setResourceStringID(m_nStickFigureHeadColorStringID, "stickfigurehead", (new Color(255, 255, 255)).getRGB());

		// Now call the base class so it can setup any string ids we haven't already set
		super.initResources();
	}
}
