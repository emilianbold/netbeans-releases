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

import java.awt.geom.Ellipse2D;
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.Color;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
//import com.tomsawyer.editor.ui.TSEDefaultPNodeUI;
import com.tomsawyer.editor.ui.TSEEdgeUI;

import com.tomsawyer.editor.TSEPNode;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
import com.tomsawyer.editor.graphics.TSEGraphics;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;
import com.tomsawyer.editor.TSEColor;

//public class ETGenericPathNodeUI extends TSEDefaultPNodeUI
public class ETGenericPathNodeUI extends TSEEdgeUI
{
	public ETGenericPathNodeUI()
	{
		super();		
	}
	
	public boolean isTransparent() 
	{
		 return true;
	}

	public boolean isTransparentByDefault()
	{
		return true;
	}
	
	public void drawSelected(TSEGraphics graphics)
	{
		doDraw(graphics);
	}
	
	public void draw(TSEGraphics graphics) 
	{
		doDraw(graphics);
	}

	public void drawOutline(TSEGraphics graphics)
	{ 
		doDraw(graphics);
	}
	
	public void drawSelectedOutline(TSEGraphics graphics)
	{
		doDraw(graphics);
	}
		
	public Object clone()
	{
		ETGenericPathNodeUI object = new ETGenericPathNodeUI();
		object.copy(this);
		return object;
	}
	
	protected void doDraw(TSEGraphics graphics)
	{
		TSConstRect bounds = this.getBounds();
		Rectangle dvRect = graphics.getTSTransform().boundsToDevice(bounds);
		double avg = (dvRect.getWidth() + dvRect.getHeight()) /2;
		Shape circle = new Ellipse2D.Double(dvRect.getX(), dvRect.getY(), avg, avg);		
		Color saveColor = graphics.getColor();
		graphics.setColor(TSEColor.white);
		graphics.fill(circle);
		graphics.setColor(saveColor);
		graphics.draw(circle);
	}
	
	public IElement getModelElement()
	{
		// no op. We don't have model elements for path nodes.
		return null; 
	}
	
	public void setModelElement(IElement element)
	{
		// We don't have model elements for path nodes.
	}
	
	public String getInitStringValue()
	{
		return null;
	}
	
	public void setInitStringValue(String string) 
	{
	}	
}
