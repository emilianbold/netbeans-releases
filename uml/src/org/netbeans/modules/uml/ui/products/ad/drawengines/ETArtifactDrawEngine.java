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



package org.netbeans.modules.uml.ui.products.ad.drawengines;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.Iterator;
import com.tomsawyer.editor.graphics.TSEGraphics;
import com.tomsawyer.editor.TSESolidObject;
import com.tomsawyer.editor.TSEFont;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITaggedValue;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETClassNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETClassNameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.INameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNodeUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IListCompartment;
import org.netbeans.modules.uml.common.ETException;

public class ETArtifactDrawEngine extends ETNodeDrawEngine
{
	protected final int NODE_HEIGHT = 45;
	protected final int NODE_WIDTH  = 105;
	protected final String STATIC_TEXT_FONT = "Arial-12";
	
	public void initResources()
	{
		setFillColor("artifactfill", 163,184,204);
      setLightGradientFillColor("artifactlightfill", 204,216,227);
		setBorderColor("artifactborder", Color.BLACK);

		super.initResources();
	}

	public String getElementType()
	{
		String type = super.getElementType();
		if (type == null)
		{
			type = new String("Artifact"); // or "SourceFileArtifact"
		}
		return type;
	}

	public void createCompartments() throws ETException
	{
		ETClassNameListCompartment newClassNameList = new ETClassNameListCompartment(this);
		newClassNameList.addCompartment(new ETClassNameCompartment(this), -1, false);
		newClassNameList.setName("<<artifact>>");
			
		this.addCompartment(newClassNameList);
	}
	
	public void initCompartments(IPresentationElement presEle)
	{
		ETGenericNodeUI parentUI = (ETGenericNodeUI) this.getParent();

		if (parentUI.getOwnerNode() != null) 
		{
			if (parentUI.getModelElement() != null)
			{
				IElement element = parentUI.getModelElement();
				createAndAddCompartment("ADClassNameListCompartment");
				
				INameListCompartment pNameCompartment = getCompartmentByKind(INameListCompartment.class);
				if (pNameCompartment != null)
				{
					pNameCompartment.attach(element);
					setDefaultCompartment(pNameCompartment);
					
					String elementType = this.getElementType();
					if (elementType.equals("Artifact"))
					{
						pNameCompartment.setName("<<artifact>>");
					}
					else if (elementType.equals("SourceFileArtifact"))
					{
						pNameCompartment.setName("<<sourcefileartifact>>");
					}
				}
			}
			else 
			{
				try {
                    this.createCompartments();
                }
                catch(Exception e)
                {
                	e.printStackTrace();
                }
                
			}
		}
	}
	
	public void doDraw(IDrawInfo pDrawInfo)
	{
		TSEGraphics graphics = pDrawInfo.getTSEGraphics();
		IETNodeUI parentUI = (IETNodeUI)this.getParent();

		Color borderColor = getBorderBoundsColor();
		Color fillColor = getBkColor();
		
		if (parentUI.getOwner() != null)
		{
			IETRect deviceRect = pDrawInfo.getDeviceBounds();
			
			int x = deviceRect.getLeft();
			int w = deviceRect.getIntWidth();
			int y = deviceRect.getTop();
			int h = deviceRect.getIntHeight();	

			// Background
         // Change if you want to demo the Class UI changes.
         float centerX = (float)deviceRect.getCenterX();
         GradientPaint paint = new GradientPaint(centerX,
                                                 deviceRect.getBottom(), 
                                                 getBkColor(),
                                                 centerX, 
                                                 deviceRect.getTop(), 
                                                 getLightGradientFillColor());
         
			GDISupport.drawRectangle(graphics,deviceRect.getRectangle(),borderColor,paint);
	
			// Draw static text
			TSEFont originalFont = parentUI.getFont();
			TSEFont staticFont = new TSEFont(STATIC_TEXT_FONT);
			parentUI.setFont(staticFont);
			Font staticTextFont = staticFont.getScaledFont(pDrawInfo.getFontScaleFactor());
			
			graphics.setFont(staticTextFont);
			graphics.setColor(borderColor);
			String elementType = this.getElementType(), staticText = "<<artifact>>";
			if (elementType.equals("SourceFileArtifact"))
			{
				staticText = "<<sourcefileartifact>>";
			}
			int staticTextX = graphics.getFontMetrics().stringWidth(staticText);
			int staticTextY = graphics.getFontMetrics().getHeight();
			graphics.drawString(staticText,	
				deviceRect.getLeft() + deviceRect.getIntWidth() / 2 - (graphics.getFontMetrics().stringWidth(staticText) / 2),
				y + graphics.getFontMetrics().getHeight());
			graphics.setFont(originalFont);
																	
			//	Draw the compartments
			Iterator iterator = this.getCompartments().iterator();			
			IETRect compartmentDrawRect =  new ETRect(x, y + staticTextY, w, h - staticTextY);		
			if (iterator.hasNext())
			{
				IListCompartment compartment = (IListCompartment) iterator.next();
				if (compartment instanceof ETClassNameListCompartment)
				{
					compartment.draw(pDrawInfo, compartmentDrawRect);
				}
			}
		}
	}
	
	public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
	{
		TSEGraphics graphics = pDrawInfo.getTSEGraphics();
		TSTransform transform = graphics.getTSTransform();
		ETGenericNodeUI parentUI = (ETGenericNodeUI)this.getParent();
	
		// Get static text size
		TSEFont originalFont = parentUI.getFont();
		TSEFont staticFont = new TSEFont(STATIC_TEXT_FONT);
		parentUI.setFont(staticFont);
		Font staticTextFont = staticFont.getFont();
		
		graphics.setFont(staticTextFont);
		String elementType = this.getElementType(), staticText = "<<artifact>>";
		if (elementType.equals("sourcefileartifact"))
		{
			staticText = "<<sourcefileartifact>>";
		}
		int staticTextX = graphics.getFontMetrics().stringWidth(staticText);
		int staticTextY = graphics.getFontMetrics().getHeight();
		graphics.setFont(originalFont);
	
		// Get compartment size
		IETSize retVal = null;
		Iterator iterator = this.getCompartments().iterator();			
		while (iterator.hasNext())
		{
			ICompartment compartment = (ICompartment) iterator.next();
			if (compartment instanceof ETClassNameListCompartment)
			{
				retVal = compartment.calculateOptimumSize(pDrawInfo, true);
				break;
			}
		}
	
		// Calculate size and return
		if (retVal != null)
		{
			retVal.setSize(Math.max(retVal.getWidth(), staticTextX), retVal.getHeight() + 2*staticTextY);
			retVal.setSize(Math.max(retVal.getWidth(), NODE_WIDTH), Math.max(retVal.getHeight(), NODE_HEIGHT));
		}

		return bAt100Pct || retVal == null ? retVal : scaleSize(retVal, transform);
	}

	public String getDrawEngineID() 
	{
		return "ArtifactDrawEngine";
	}
}
