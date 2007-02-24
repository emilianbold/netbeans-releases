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
import java.awt.Graphics2D;
import java.awt.Polygon;

import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETRectEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IBoxCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ISupportEnums;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETNodeDrawEngine;
import com.tomsawyer.editor.graphics.TSEGraphics;
import java.awt.GradientPaint;

/**
 * @author jingmingm
 *
 */
public class ETBoxCompartment extends ETCompartment implements IBoxCompartment
{
	protected final Color FILL_COLOR = new Color(0, 150, 150);
	protected int m_BoxKind = ISupportEnums.BK_SIMPLE_BOX;
	private int m_nBoxFillStringID = -1;
        private int m_nBoxLightFillStringID = -1;
	private int m_nBoxBorderStringID = -1;
	protected static final String BOXCOMPARTMENTBOXKIND_STRING = "BoxKind";
	
   public ETBoxCompartment()
	{
		super();
		this.init();
	}

	public ETBoxCompartment(IDrawEngine pDrawEngine)
	{
		super(pDrawEngine);
		this.init();
	}

	private void init()
	{
		this.initResources();
	}

	public void initResources()
	{
		this.setName("");

                //**** Bug 6270565 fix. Section 1 of 2. Begin ****
		//// First setup our defaults in case the colors/fonts are not in the 
		//// configuration file
                //m_nBoxFillStringID = m_ResourceUser.setResourceStringID(m_nBoxFillStringID, "boxfill", (new Color(41,161,160)).getRGB());
                //m_nBoxLightFillStringID = m_ResourceUser.setResourceStringID(m_nBoxLightFillStringID, "boxlightgradientfill", m_ResourceUser.getCOLORREFForStringID(m_nBoxFillStringID));
		//m_nBoxBorderStringID = m_ResourceUser.setResourceStringID(m_nBoxBorderStringID, "boxborder", 0);
                //**** Bug 6270565 fix. Section 1 of 2. End ****
                
		// Call the base interface so that the compartments get initialized.
		super.initResources();
	}
	
	public void setBoxKind(int kind)
	{
		m_BoxKind = kind;
	}
	
	public int getBoxKind()
	{
		return m_BoxKind;
	}
	
	public void draw(IDrawInfo pDrawInfo, IETRect pBoundingRect)
	{
		super.draw(pDrawInfo, pBoundingRect);
		IDrawEngine drawEngine = this.getEngine();

                //**** Bug 6270565 fix. Section 2 of 2. Begin ****
                //fillColor = new Color(m_ResourceUser.getCOLORREFForStringID(m_nBoxFillStringID));
                //Color lightFillColor = new Color(m_ResourceUser.getCOLORREFForStringID(m_nBoxLightFillStringID));
		//Color borderColor = new Color(m_ResourceUser.getCOLORREFForStringID(m_nBoxBorderStringID));
                Color fillColor = ((ETNodeDrawEngine) drawEngine).getFillColor(); // ((ETNodeDrawEngine) drawEngine).getFillColor();
                Color lightFillColor = ((ETNodeDrawEngine) drawEngine).getLightGradientFillColor();
                Color borderColor = ((ETNodeDrawEngine) drawEngine).getBorderColor();
                //**** Bug 6270565 fix. Section 2 of 2. End ****

		TSEGraphics graphics = pDrawInfo.getTSEGraphics();
		ETGenericNodeUI parentUI = (ETGenericNodeUI) drawEngine.getParent();
		//pBoundingRect = new ETRectEx(graphics.getTSTransform().boundsToDevice(getObject().getBounds()));

//		if (m_BoxKind == ISupportEnums.BK_3DBOX || m_BoxKind == ISupportEnums.BK_ELONGATED_3DBOX)
//		{
//			long nMaxNormalBoxSize = 10;
//			// Draw a 3d box around the name compartment
//			//
//			// NCBK_DRAW_3DBOX :
//			//       /--------------/
//			//      /              / |
//			//     /              /  |
//			//    |--------------|   |
//			//    |              |   |
//			//    |              |   |
//			//    |              |   |
//			//    |    Name      |  /
//			//    |              | /---- Size max is nMaxNormalBoxSize
//			//    |              |/
//			//    |--------------|
//			//
//			// NCBK_DRAW_ELONGATED_3DBOX :
//			//
//			//         /--------------/|
//			//        /              / |
//			//       /              /  |
//			//      /              /   |
//			//     /              /   /
//			//    |--------------|   /
//			//    |              |  /
//			//    |    Name      | /
//			//    |              |/
//			//    |--------------|
//			//
//
//			float boxTopOffset = 0.2f;
//			float boxRightOffset = 0.2f;
//			float boxIncetPct  = 0.2f;
//			IETRect  frontBoundingRect = (IETRect)pBoundingRect.clone();
//            
//			if (m_BoxKind == ISupportEnums.BK_3DBOX) {
//                            boxTopOffset    = 0.2f;
//                            boxRightOffset  = 0.2f;
//                            boxIncetPct     = 0.2f;
////			   frontBoundingRect.setTop((int) (frontBoundingRect.getTop() + Math.min( (int)((float)pBoundingRect.getHeight() * boxTopOffset), nMaxNormalBoxSize)));
////			   frontBoundingRect.setRight((int)(frontBoundingRect.getRight() - Math.min( (int)((float)pBoundingRect.getWidth()  * boxRightOffset), nMaxNormalBoxSize)));
//                            frontBoundingRect.setSides(
//                                    frontBoundingRect.getLeft(),
//                                    (int) (frontBoundingRect.getTop() + Math.min( (int)((float)pBoundingRect.getHeight() * boxTopOffset), nMaxNormalBoxSize)),
//                                    (int)(frontBoundingRect.getRight() - Math.min( (int)((float)pBoundingRect.getWidth()  * boxRightOffset), nMaxNormalBoxSize)),
//                                    frontBoundingRect.getBottom());				
//			}
//			else if (m_BoxKind == ISupportEnums.BK_ELONGATED_3DBOX)
//			{
//			   boxTopOffset    = 0.5f;
//			   boxRightOffset  = 0.2f;
//			   boxIncetPct     = 0.5f;
//			   frontBoundingRect.setTop(frontBoundingRect.getTop() + (int)((float)pBoundingRect.getHeight() * boxTopOffset));
//			   frontBoundingRect.setRight(frontBoundingRect.getRight() - (int)((float)pBoundingRect.getWidth()  * boxRightOffset));
//			}
//
//
//			IETPoint topLeft  = new ETPoint(frontBoundingRect.getTopLeft());
//			IETPoint topRight = new ETPoint(frontBoundingRect.getRight(), frontBoundingRect.getTop());
//			IETPoint bottomRight = new ETPoint(frontBoundingRect.getBottomRight());
//			IETPoint backTopLeft = new ETPoint(pBoundingRect.getRight() - (topRight.getX() - topLeft.getX()), pBoundingRect.getTop());       
//			IETPoint backTopRight = new ETPoint(pBoundingRect.getRight(), pBoundingRect.getTop());
//			IETPoint backBottomRight = new ETPoint(pBoundingRect.getRight(), pBoundingRect.getBottom() - (int)((float)pBoundingRect.getHeight() * boxIncetPct));
//            
//         Color crText = parentUI.getBorderColor().getColor();
//            
//                        float centerX = (float) frontBoundingRect.getCenterX();
//                        GradientPaint paint = new GradientPaint(centerX, frontBoundingRect.getBottom(), fillColor, centerX, frontBoundingRect.getTop(), lightFillColor);
//			//GDISupport.drawRectangle(pDrawInfo.getTSEGraphics(),frontBoundingRect.getRectangle(), crText, FILL_COLOR);
//			GDISupport.drawRectangle(pDrawInfo.getTSEGraphics(),frontBoundingRect.getRectangle(), borderColor, paint);
//
//			ETList<IETPoint> polygonPoints = new ETArrayList<IETPoint>(); //polygonPoints();
//            
//			polygonPoints.add(topLeft);
//			polygonPoints.add(backTopLeft);
//			polygonPoints.add(backTopRight);
//			polygonPoints.add(topRight);
//			polygonPoints.add(topLeft);
//                        centerX = (float) ((topLeft.getX() + backTopRight.getX()) / 2.0);
//                        paint = new GradientPaint(centerX, topLeft.getY(), fillColor, centerX, backTopLeft.getY(), lightFillColor);
//
//			//GDISupport.drawPolygon(pDrawInfo.getTSEGraphics(), polygonPoints, crText, 1,  FILL_COLOR);
//			GDISupport.drawPolygon(pDrawInfo.getTSEGraphics(), polygonPoints, borderColor, 1,  paint);
//
//			polygonPoints.clear();
//			topRight.offset(-1,-1);
//			polygonPoints.add(topRight);
//			polygonPoints.add(backTopRight);
//			polygonPoints.add(backBottomRight);
//			bottomRight.offset(-1,-1);
//			polygonPoints.add(bottomRight);
//			polygonPoints.add(topRight);
//			//GDISupport.drawPolygon(pDrawInfo.getTSEGraphics(), polygonPoints, crText, 1,  FILL_COLOR);
//                        centerX = (float) ((topRight.getX() + backTopRight.getX()) / 2.0);
//                        paint = new GradientPaint(centerX, bottomRight.getY(), fillColor, centerX, backTopRight.getY(), lightFillColor);
//			GDISupport.drawPolygon(pDrawInfo.getTSEGraphics(), polygonPoints, borderColor, 1,  paint);
//			
//			//public static void drawPolygon(Graphics2D dc, ETList < IETPoint > pPoints, final Color penColor, int penWidth, final Color bkColor)
//		}
//		else if (m_BoxKind == ISupportEnums.BK_DIAMOND)
                if (m_BoxKind == ISupportEnums.BK_DIAMOND)
		{
			//Graphics2D dc = graphics.getGraphics();
                        float centerX = (float) pBoundingRect.getCenterX();
                        GradientPaint paint = new GradientPaint(centerX, pBoundingRect.getBottom(), fillColor, centerX, pBoundingRect.getTop(), lightFillColor);
			ETList<IETPoint> diamondPts = GDISupport.getDiamondPolygonPoints(graphics, pBoundingRect.getRectangle());
			GDISupport.drawPolygon(graphics, diamondPts, borderColor, paint);
		}
		else if (m_BoxKind == ISupportEnums.BK_SIMPLE_FILLED_BOX)
		{
                    float centerX = (float) pBoundingRect.getCenterX();
                    GradientPaint paint = new GradientPaint(centerX, pBoundingRect.getBottom(), fillColor, centerX, pBoundingRect.getTop(), lightFillColor);
		    GDISupport.drawRectangle( graphics.getGraphics(),pBoundingRect.getRectangle(),borderColor,paint);
		}
		else if (m_BoxKind == ISupportEnums.BK_SIMPLE_BOX)
		{
                        float centerX = (float) pBoundingRect.getCenterX();
                        GradientPaint paint = new GradientPaint(centerX, pBoundingRect.getBottom(), fillColor, centerX, pBoundingRect.getTop(), lightFillColor);
			graphics.setPaint(paint);
			graphics.fillRect(pBoundingRect.getLeft(),pBoundingRect.getTop(),(int)pBoundingRect.getWidth(),(int)pBoundingRect.getHeight());
		
			graphics.setColor(parentUI.getBorderColor());
			graphics.drawRect(pBoundingRect.getLeft(),pBoundingRect.getTop(),(int)pBoundingRect.getWidth(),(int)pBoundingRect.getHeight());
		}
		
	}
   
   /**
    * This is the name of the drawengine used when storing and reading from the product archive.
    *
    * @param sID[out,retval] The unique name for this compartment.  Used when reading and writing the
    * product archive (etlp file).
    */
   public String getCompartmentID()
   {
      return "BoxCompartment";
   }

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#readFromArchive(org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive, org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement)
	 */
	public void readFromArchive(IProductArchive pProductArchive, IProductArchiveElement pCompartmentElement)
	{
		
		super.readFromArchive(pProductArchive, pCompartmentElement);
		m_BoxKind = (int)pCompartmentElement.getAttributeLong(BOXCOMPARTMENTBOXKIND_STRING);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#writeToArchive(org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive, org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement)
	 */
	public IProductArchiveElement writeToArchive(IProductArchive pProductArchive, IProductArchiveElement pEngineElement)
	{
		IProductArchiveElement retObj = super.writeToArchive(pProductArchive, pEngineElement);
		if (retObj != null)
		{
			retObj.addAttributeLong(BOXCOMPARTMENTBOXKIND_STRING, m_BoxKind);
		}
		return retObj;
	}

	/**
	 * Initializes the resources that should be used for the fill and border.
	 */
	public void initBoxResources(String sFillResource, String sBorderResource)
	{
		if (sFillResource != null && sFillResource.length() > 0)
		{
			m_nBoxFillStringID = m_ResourceUser.getResourceMgr().getStringID(sFillResource);

			// Set the default color
			setDefaultColor(sFillResource, (new Color(41,161,160)).getRGB());
		}
		
		if (sBorderResource != null && sBorderResource.length() > 0)
		{
			m_nBoxBorderStringID = m_ResourceUser.getResourceMgr().getStringID(sBorderResource);

			// Set the default color
			setDefaultColor(sBorderResource, 0);
		}
	}

	public void initBoxResources(String sFillResource, String sLightFillResource, String sBorderResource)
	{
		if (sFillResource != null && sFillResource.length() > 0)
		{
			m_nBoxFillStringID = m_ResourceUser.getResourceMgr().getStringID(sFillResource);

			// Set the default color
			setDefaultColor(sFillResource, (new Color(41,161,160)).getRGB());
		}
		
		if (sLightFillResource != null && sLightFillResource.length() > 0)
		{
			m_nBoxLightFillStringID = m_ResourceUser.getResourceMgr().getStringID(sLightFillResource);

			// Set the default color
			setDefaultColor(sLightFillResource, m_ResourceUser.getCOLORREFForStringID(m_nBoxFillStringID));
		}
		
		if (sBorderResource != null && sBorderResource.length() > 0)
		{
			m_nBoxBorderStringID = m_ResourceUser.getResourceMgr().getStringID(sBorderResource);

			// Set the default color
			setDefaultColor(sBorderResource, 0);
		}
	}
}
