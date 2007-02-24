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
import java.awt.Point;
import java.awt.Rectangle;

import java.util.Iterator;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETNameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETClassNameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.INameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IListCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import com.tomsawyer.editor.graphics.TSEGraphics;


/*
 * 
 * @author Trey
 *
 */
public class ETClassRobustnessDrawEngine extends ETNodeDrawEngine
{
	public static final int RK_NONE = 0;
	public static final int RK_BOUNDARY = 1;
	public static final int RK_CONTROL = 2;
	public static final int RK_ENTITY = 3;
	
	protected final String BOUNDARY_STEREO = "boundary";
	protected final String CONTROL_STEREO = "controller";
	protected final String ENTITY_STEREO = "entity";
	protected final String FONT_STRING = "Arial-plain-14";
	protected final int NODE_HEIGHT = 100;
	protected final int NODE_WIDTH  = 100;
	
	protected int m_RobustnessKind = RK_NONE;

	protected void setRobustnessKind(int robustnessKind)
	{
		m_RobustnessKind = robustnessKind;
		
		// Set the stereotype on the element
		IElement pElement = getFirstModelElement();
		if (pElement != null)
		{
			pElement.removeStereotype2(BOUNDARY_STEREO);
			pElement.removeStereotype2(CONTROL_STEREO);
			pElement.removeStereotype2(ENTITY_STEREO);
			
			if (m_RobustnessKind == RK_BOUNDARY)
			{
				pElement.applyStereotype2(BOUNDARY_STEREO); 
			}
			else if (m_RobustnessKind == RK_CONTROL)
			{
				pElement.applyStereotype2(CONTROL_STEREO); 
			}
			else if (m_RobustnessKind == RK_ENTITY)
			{
				pElement.applyStereotype2(ENTITY_STEREO); 
			}
		}
	}
	
	protected int getRobustnessKind()
	{
		return m_RobustnessKind;
	}
	
	protected void setRobustnessKindMember()
	{
		m_RobustnessKind = RK_NONE;
		
		IElement pElement = getFirstModelElement();
		if (pElement != null)
		{
			if (pElement.retrieveAppliedStereotype(BOUNDARY_STEREO) != null)
			{
				m_RobustnessKind = RK_BOUNDARY;
			}
			else if (pElement.retrieveAppliedStereotype(CONTROL_STEREO) != null)
			{
				m_RobustnessKind = RK_CONTROL;
			}
			else if (pElement.retrieveAppliedStereotype(ENTITY_STEREO) != null)
			{
				m_RobustnessKind = RK_ENTITY;
			}
		}
	}
	
	protected void drawRobustnessFigure(IDrawInfo pDrawInfo)
	{
		TSEGraphics graphics = pDrawInfo.getTSEGraphics();
		Color borderColor = getBorderBoundsColor();
		Color fillColor = getBkColor();
		
		IETRect deviceRect = pDrawInfo.getDeviceBounds();	
		int x = deviceRect.getLeft();
		int w = deviceRect.getIntWidth();
		int y = deviceRect.getTop();
		int h = deviceRect.getIntHeight();	
		Point centerPt = deviceRect.getCenterPoint();
					
		switch(m_RobustnessKind)
		{
			case RK_BOUNDARY:
			{
				float lineSizePct = 0.08f;
				float lineFromTopPct = 0.25f;
				
				int lineSizePointsX = (int)(w * lineSizePct);
				int lineSizePointsY = (int)(h * lineSizePct);
				
				graphics.setColor(borderColor);
				
				// Draw the virtical line on the left
				IETPoint topLeft = new ETPoint(x, deviceRect.getTop());
				IETPoint bottomLeft = new ETPoint(x, deviceRect.getBottom());
				topLeft.setY(topLeft.getY() + (int)((float)(h) * lineFromTopPct));
				bottomLeft.setY(bottomLeft.getY() - (int)((float)(h) * lineFromTopPct));
				graphics.drawLine(topLeft.getX(), topLeft.getY(), bottomLeft.getX(), bottomLeft.getY());
				
				// Draw the horizonal line
				IETPoint lineLeft = new ETPoint(x, centerPt.y);
				IETPoint lineRight = new ETPoint(x + lineSizePointsX, centerPt.y);
				graphics.drawLine(lineLeft.getX(), lineLeft.getY(), lineRight.getX(), lineRight.getY());
				
				// Draw the ellipse
				Rectangle rect = new Rectangle(x + lineSizePointsX, y, w - lineSizePointsX, h);
            
            ////////////////////////////////////////////////////////////////////
            // Testing Gradiant Fill
            float centerX = (float)centerPt.getX();
            java.awt.GradientPaint paint = new java.awt.GradientPaint(centerX,
                                                                     deviceRect.getBottom(), 
                                                                     fillColor, 
                                                                     centerX, 
                                                                     deviceRect.getTop(), 
                                                                     getLightGradientFillColor());
//            graphics.getGraphics().setPaint(paint);            
//            GDISupport.drawEllipse(graphics.getGraphics(), deviceRect.getRectangle());
//            
//            graphics.getGraphics().setColor(borderColor);
//            GDISupport.frameEllipse(graphics.getGraphics(), deviceRect.getRectangle());
            GDISupport.drawEllipse(graphics.getGraphics(), rect, borderColor, paint);
            ////////////////////////////////////////////////////////////////////
            // Testing Gradiant Fill
            
				//GDISupport.drawEllipse(graphics.getGraphics(), rect, borderColor, fillColor);
				
				break;
			}
			case RK_CONTROL:
			{
				int nHeight = (int)(h/10);
				Rectangle rect = new Rectangle(x, y + nHeight, w, h - nHeight);
            
            ////////////////////////////////////////////////////////////////////
            // Testing Gradiant Fill
            float centerX = (float)centerPt.getX();
            java.awt.GradientPaint paint = new java.awt.GradientPaint(centerX,
                                                                     deviceRect.getBottom(), 
                                                                     fillColor,
                                                                     centerX, 
                                                                     deviceRect.getTop(), 
                                                                     getLightGradientFillColor());
//            graphics.getGraphics().setPaint(paint);            
//            GDISupport.drawEllipse(graphics.getGraphics(), deviceRect.getRectangle());
//            
//            graphics.getGraphics().setColor(borderColor);
//            GDISupport.frameEllipse(graphics.getGraphics(), deviceRect.getRectangle());
            GDISupport.drawEllipse(graphics.getGraphics(), rect, borderColor, paint);
            ////////////////////////////////////////////////////////////////////
            // Testing Gradiant Fill
            
				//GDISupport.drawEllipse(graphics.getGraphics(), rect, borderColor, fillColor);
				
				IETPoint pt1 = new ETPoint(centerPt.x, deviceRect.getTop() + nHeight);
				IETPoint pt2 = new ETPoint(centerPt.x + nHeight, deviceRect.getTop());
				
				// don't let the legs extend outside the bounding rect
				pt2.setX(Math.min(pt2.getX(), x + w));
				
				graphics.setColor(borderColor);
				
				// draw top leg
				graphics.drawLine(pt1.getX(), pt1.getY(), pt2.getX(), pt2.getY());
				
				// draw bottom leg
				graphics.drawLine(pt1.getX(), pt1.getY(), pt2.getX(), pt2.getY() + 2*nHeight);

				break;
			}
			case RK_ENTITY:
			{				
            ////////////////////////////////////////////////////////////////////
            // Testing Gradiant Fill
            float centerX = (float)centerPt.getX();
            java.awt.GradientPaint paint = new java.awt.GradientPaint(centerX,
                                                                     deviceRect.getBottom(), 
                                                                     fillColor, 
                                                                     centerX, 
                                                                     deviceRect.getTop(), 
                                                                     getLightGradientFillColor());
//            graphics.getGraphics().setPaint(paint);            
//            GDISupport.drawEllipse(graphics.getGraphics(), deviceRect.getRectangle());
//            
//            graphics.getGraphics().setColor(borderColor);
//            GDISupport.frameEllipse(graphics.getGraphics(), deviceRect.getRectangle());
            GDISupport.drawEllipse(graphics.getGraphics(), deviceRect.getRectangle(), borderColor, paint);
            ////////////////////////////////////////////////////////////////////
            // Testing Gradiant Fill
            
				//GDISupport.drawEllipse(graphics.getGraphics(), deviceRect.getRectangle(), borderColor, fillColor);

				IETPoint bottomLeft = new ETPoint(x, deviceRect.getBottom());
				IETPoint bottomRight = new ETPoint(x + w, deviceRect.getBottom());
				
				graphics.setColor(borderColor);
				graphics.drawLine(bottomLeft.getX(), bottomLeft.getY(), bottomRight.getX(), bottomRight.getY());
				
				break;
			}
		}
	}
	
	public boolean isDrawEngineValidForModelElement()
	{
		boolean valid = false;
		String metaType = getMetaTypeOfElement();
		if (metaType != null && metaType.equals("Class"))
		{
			IElement pElement = getFirstModelElement();
			if (pElement != null && pElement instanceof IClass)
			{
				if (pElement.retrieveAppliedStereotype(BOUNDARY_STEREO) != null ||
					pElement.retrieveAppliedStereotype(CONTROL_STEREO) != null ||
					pElement.retrieveAppliedStereotype(ENTITY_STEREO) != null)
				{
					valid = true;
				}
			}
		}
		return valid;
	}
	
	public void onGraphEvent(int nKind)
	{
		super.onGraphEvent(nKind);
		
		switch(nKind)
		{
			case IGraphEventKind.GEK_POST_CREATE :
			{
				IETGraphObjectUI parentUI =  this.getParent();
				String initialStr = parentUI.getInitStringValue();
				int delimiter = initialStr.indexOf(' ');
				String kindStr = initialStr.substring(delimiter + 1);
				String kindStrLower = kindStr.toLowerCase();
				
				IElement pElement = getFirstModelElement();
				if (pElement != null)
				{
					int robustnessKind = RK_NONE;
					
					if (kindStrLower.equals(BOUNDARY_STEREO))
					{
						robustnessKind = RK_BOUNDARY;
					}
					else if (kindStrLower.equals(CONTROL_STEREO))
					{
						robustnessKind = RK_CONTROL;
					}
					else if (kindStrLower.equals(ENTITY_STEREO))
					{
						robustnessKind = RK_ENTITY;
					}
					
					this.setRobustnessKind(robustnessKind);
				}
				break;
			}
		}
	}
	
	public String getElementType()
	{
		String type = super.getElementType();
		if (type == null)
		{
			type = new String("Class");
		}
		return type;
	}

	public void initResources()
	{
		setFillColor("classrobustnessfill",254, 243, 168);
      setLightGradientFillColor("classrobustnesslightfill", 255, 255, 255);
		setBorderColor("classrobustnessborder", Color.BLACK);

		super.initResources();
	}
	
	public void drawContents(IDrawInfo pDrawInfo)
	{   
		TSEGraphics graphics = pDrawInfo.getTSEGraphics();
		IETGraphObjectUI parentUI =this.getParent();
	
		// draw yourself only if you have an owner
		if (parentUI.getOwner() != null)
		{
			IETRect deviceRect = pDrawInfo.getDeviceBounds();	
			int x = deviceRect.getLeft();
			int w = deviceRect.getIntWidth();
			int y = deviceRect.getTop();
			int h = deviceRect.getIntHeight();	
			
			// Draw figure
			this.setRobustnessKindMember();
			this.drawRobustnessFigure(pDrawInfo);

			// Save old clip
			
			// Draw compartment
			Iterator iterator = this.getCompartments().iterator();			
			if (iterator.hasNext())
			{
				IListCompartment compartment = (IListCompartment) iterator.next();
				if (compartment instanceof ETClassNameListCompartment)
				{
					IETSize pETSize = compartment.calculateOptimumSize(pDrawInfo, false);
					IETRect pETRect = new ETRect(x, y, w, h);
					if (pETRect.getIntWidth() > pETSize.getWidth())
					{
						int xDeflate = (int)((pETRect.getIntWidth() - pETSize.getWidth())/2);
						pETRect.setLeft(x + xDeflate);
						pETRect.setRight(x + w - xDeflate);
					}
					if (pETRect.getIntHeight() > pETSize.getHeight())
					{
						int yDeflate = (int)((pETRect.getIntHeight() - pETSize.getHeight())/2);
						pETRect.setBottom(y + h - yDeflate);
						pETRect.setTop(y + yDeflate);
					}
					compartment.draw(pDrawInfo, pETRect);
				}
			}
		}	
	}
	
	public void initCompartments()
	{
		ETNameListCompartment newNameList = new ETNameListCompartment(this);
		newNameList.addCompartment(new ETNameCompartment(this), -1, false);
		this.addCompartment(newNameList);
	}

	public void createCompartments() throws ETException 
	{
		IETGraphObjectUI parentUI =  this.getParent();

		if (parentUI != null && parentUI.getOwner() != null) 
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
				}
			}
			else
			{
				this.initCompartments();
			}
		}
	}

	public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
	{
		IETSize nameVal = null, retVal = null;
		Iterator iterator = this.getCompartments().iterator();			
		while (iterator.hasNext())
		{
			ICompartment compartment = (ICompartment) iterator.next();
			if (compartment instanceof ETNameListCompartment)
			{
				nameVal = compartment.calculateOptimumSize(pDrawInfo, true);
				break;
			}
		}
	
		if (nameVal == null)
		{
			retVal = new ETSize(NODE_WIDTH, NODE_HEIGHT); 
		}
		else
		{
			// Make sure we don't have an egg.
			if (m_RobustnessKind == RK_BOUNDARY)
			{
				retVal = new ETSize(Math.max(nameVal.getWidth(), NODE_WIDTH + 10), Math.max(nameVal.getHeight(), NODE_HEIGHT));
			}
			else
			{
				retVal = new ETSize(Math.max(nameVal.getWidth(), NODE_WIDTH), Math.max(nameVal.getHeight(), NODE_HEIGHT));
			}
		}
		  		
		return bAt100Pct || retVal == null ? retVal : scaleSize(retVal, pDrawInfo != null ? pDrawInfo.getTSTransform() : getTransform());
	}
		
	public String getDrawEngineID() 
	{
		return "ClassRobustnessDrawEngine";
	}
	
	public String getDrawEngineMatchID()
	{
		if (m_RobustnessKind == RK_BOUNDARY)
		{
			return "ClassRobustnessDrawEngineImpl Boundary";
		}
		else if (m_RobustnessKind == RK_CONTROL)
		{
			return "ClassRobustnessDrawEngineImpl Control";
		}
		else if (m_RobustnessKind == RK_ENTITY)
		{
			return "ClassRobustnessDrawEngineImpl Entity";
		}
		else
		{
			return "ClassRobustnessDrawEngineImpl";
		}
	}
	
	public long modelElementHasChanged(INotificationTargets pTargets)
	{
		try
		{
			this.clearCompartments();
			this.createCompartments();
		} catch (Exception e) 
		{
		}
		return super.modelElementHasChanged(pTargets);
	}
	
	/**
	 * Initializes our compartments by attaching modelelements to each. Previously existing compartments remain,
	 * so if a compartment already exists it is reattached, if not one is created.
	 *
	 * @param pElement [in] The presentation element we are representing
	 */
	public void initCompartments(IPresentationElement presEle)
	{
		// We may get here with no compartments.  This happens if we've been created
		// by the user.  If we read from a file then the compartments have been pre-created and
		// we just need to initialize them.
		int numCompartments = getNumCompartments();
		if (numCompartments == 0)
		{
			try
			{
				createCompartments();
			}
			catch(Exception e)
			{
			}
		}

		IElement pModelElement = presEle.getFirstSubject();

		String currentMetaType = getMetaTypeOfElement();
		if (pModelElement != null)
		{
			// Tell the name compartment about the model element it should display
			INameListCompartment pNameCompartment = getCompartmentByKind(INameListCompartment.class);
			if (pNameCompartment != null)
			{
				pNameCompartment.attach(pModelElement);
			}
		}
	}
}
