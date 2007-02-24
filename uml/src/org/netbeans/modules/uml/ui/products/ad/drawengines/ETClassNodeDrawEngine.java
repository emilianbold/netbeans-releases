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
import java.awt.Shape;
import java.awt.Rectangle;
import java.util.Iterator;
import com.tomsawyer.editor.graphics.TSEGraphics;
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
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETClassNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETBoxCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETClassNameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.INameListCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IListCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ISupportEnums;
import org.netbeans.modules.uml.core.support.umlsupport.ETDeviceRect;
import java.awt.GradientPaint;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.common.ETException;
import org.openide.ErrorManager;
import com.tomsawyer.drawing.event.TSDrawingChangeEvent;
import com.tomsawyer.graph.TSGraph;

/*
 * 
 * @author Embarcadero Tech
 * 
 */
public class ETClassNodeDrawEngine extends ETContainerDrawEngine
{
   protected final int NODE_WIDTH  = 50;
	protected final int NODE_HEIGHT = 80;
        
	public ETClassNodeDrawEngine()
	{
		super();
		this.setContainmentType(ContainmentTypeEnum.CT_NAMESPACE);
	}
	
   /**
    * Size this node so all compartments are completely visible.
    */
   public void sizeToContents()
   {
      // Size with no minimum, but keep the current size
      sizeToContentsWithMin( NODE_WIDTH, NODE_HEIGHT, false, true );
   }

	public void init() throws ETException
	{
		super.init();
		this.setContainmentType(ContainmentTypeEnum.CT_NAMESPACE);
	}
	
	public void initResources()
	{
		setFillColor("boxfill", 72, 132, 84);
		setLightGradientFillColor("boxlightgradientfill", 183, 207, 188);
		setBorderColor("boxborder", Color.BLACK);
		
		super.initResources();
	}

	public String getElementType()
	{
		String type = super.getElementType();
		if (type == null)
		{
			type = new String("Node");
		}
		return type;
	}

	public void initCompartments()
	{
		ETClassNameListCompartment newClassNameList = new ETClassNameListCompartment(this);
		newClassNameList.addCompartment(new ETClassNameCompartment(this), -1, false);			
		this.addCompartment(newClassNameList);
		
//		this.addCompartment(new ETBoxCompartment(this));
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
				
//				ETBoxCompartment boxCompartment = new ETBoxCompartment(this);
//				boxCompartment.setBoxKind(ISupportEnums.BK_3DBOX);
//				this.addCompartment(boxCompartment);
			}
			else 
			{
				this.initCompartments();
			}
		}
	}
	
//        public void drawContents(IDrawInfo pDrawInfo) {
//            TSEGraphics graphics = pDrawInfo.getTSEGraphics();
//            IETGraphObjectUI parentUI = this.getParent();
//            
//            if (parentUI.getOwner() != null) {
//                IETRect deviceBounds = pDrawInfo.getDeviceBounds();
//                
//                //	Draw the compartments
//                Iterator<ICompartment> iterator = getCompartments().iterator();
//                IETSize nameSize = null;
//                // This is the border size set in the NameListCompartment, but its protected, so just for now. (Kevin)
//                int broderHeight = graphics.getTSTransform().heightToDevice(4.0);
//                
//                while (iterator.hasNext()) {
//                    ICompartment compartment = iterator.next();
//                    if (compartment instanceof ETBoxCompartment) {
//                        IETRect boxRect = (IETRect) deviceBounds.clone();
//                        if (nameSize != null){
//                            boxRect.setBottom(boxRect.getBottom() - nameSize.getHeight());
//                        }
//                        compartment.draw(pDrawInfo, boxRect);
//                    } else if (compartment instanceof ETClassNameListCompartment) {
//                        nameSize = compartment.calculateOptimumSize(pDrawInfo, false);
//                        IETRect nameRect = (IETRect)deviceBounds.clone();
//                        //nameRect.setTop(nameRect.getBottom() - (nameSize.getHeight() - broderHeight * 2));
//                        nameRect.setTop(nameRect.getBottom() - nameSize.getHeight());
//                        compartment.draw(pDrawInfo, nameRect);
//                    }
//                }
//            }
//        }

        public void doDraw(IDrawInfo pDrawInfo) {
            TSEGraphics graphics = pDrawInfo.getTSEGraphics();
            IETRect deviceRect = pDrawInfo.getDeviceBounds();
            ETGenericNodeUI parentUI = (ETGenericNodeUI)this.getParent();
      
            Color fillColor = getFillColor();
            Color lightFillColor = getLightGradientFillColor();
            Color borderColor = getBorderColor();
            
//            if (m_BoxKind == ISupportEnums.BK_3DBOX || m_BoxKind == ISupportEnums.BK_ELONGATED_3DBOX) {
                long nMaxNormalBoxSize = 10;
                // Draw a 3d box around the name compartment
                //
                // NCBK_DRAW_3DBOX :
                //       /--------------/
                //      /              / |
                //     /              /  |
                //    |--------------|   |
                //    |              |   |
                //    |              |   |
                //    |              |   |
                //    |    Name      |  /
                //    |              | /---- Size max is nMaxNormalBoxSize
                //    |              |/
                //    |--------------|
                //
                // NCBK_DRAW_ELONGATED_3DBOX :
                //
                //         /--------------/|
                //        /              / |
                //       /              /  |
                //      /              /   |
                //     /              /   /
                //    |--------------|   /
                //    |              |  /
                //    |    Name      | /
                //    |              |/
                //    |--------------|
                //
                
                float boxTopOffset = 0.2f;
                float boxRightOffset = 0.2f;
                float boxIncetPct  = 0.2f;
                IETRect  frontBoundingRect = (IETRect)deviceRect.clone();
                
//                if (m_BoxKind == ISupportEnums.BK_3DBOX) {
                    boxTopOffset    = 0.2f;
                    boxRightOffset  = 0.2f;
                    boxIncetPct     = 0.2f;
                                        
                    frontBoundingRect.setSides(
                            frontBoundingRect.getLeft(),
                            (int) (frontBoundingRect.getTop() + Math.min( (int)((float)deviceRect.getHeight() * boxTopOffset), nMaxNormalBoxSize)),
                            (int)(frontBoundingRect.getRight() - Math.min( (int)((float)deviceRect.getWidth()  * boxRightOffset), nMaxNormalBoxSize)),
                            frontBoundingRect.getBottom());                    
                     
//                } else if (m_BoxKind == ISupportEnums.BK_ELONGATED_3DBOX) {
//                    boxTopOffset    = 0.5f;
//                    boxRightOffset  = 0.2f;
//                    boxIncetPct     = 0.5f;
//                    frontBoundingRect.setTop(frontBoundingRect.getTop() + (int)((float)deviceRect.getHeight() * boxTopOffset));
//                    frontBoundingRect.setRight(frontBoundingRect.getRight() - (int)((float)deviceRect.getWidth()  * boxRightOffset));
//                }
                
                
                IETPoint topLeft  = new ETPoint(frontBoundingRect.getTopLeft());
                IETPoint topRight = new ETPoint(frontBoundingRect.getRight(), frontBoundingRect.getTop());
                IETPoint bottomRight = new ETPoint(frontBoundingRect.getBottomRight());
                IETPoint backTopLeft = new ETPoint(deviceRect.getRight() - (topRight.getX() - topLeft.getX()), deviceRect.getTop());
                IETPoint backTopRight = new ETPoint(deviceRect.getRight(), deviceRect.getTop());
                IETPoint backBottomRight = new ETPoint(deviceRect.getRight(), deviceRect.getBottom() - (int)((float)deviceRect.getHeight() * boxIncetPct));
                
                Color crText = parentUI.getBorderColor().getColor();
                
                float centerX = (float) frontBoundingRect.getCenterX();
                GradientPaint paint = new GradientPaint(centerX, frontBoundingRect.getBottom(), fillColor, centerX, frontBoundingRect.getTop(), lightFillColor);
                GDISupport.drawRectangle(pDrawInfo.getTSEGraphics(),frontBoundingRect.getRectangle(), borderColor, paint);
                
                ETList<IETPoint> polygonPoints = new ETArrayList<IETPoint>(); //polygonPoints();
                
                polygonPoints.add(topLeft);
                polygonPoints.add(backTopLeft);
                polygonPoints.add(backTopRight);
                polygonPoints.add(topRight);
                polygonPoints.add(topLeft);
                centerX = (float) ((topLeft.getX() + backTopRight.getX()) / 2.0);
                
                paint = new GradientPaint(centerX, topLeft.getY(), fillColor, centerX, backTopLeft.getY(), lightFillColor);
                GDISupport.drawPolygon(pDrawInfo.getTSEGraphics(), polygonPoints, borderColor, 1,  paint);
                
                polygonPoints.clear();
                topRight.offset(-1,-1);
                polygonPoints.add(topRight);
                polygonPoints.add(backTopRight);
                polygonPoints.add(backBottomRight);
                bottomRight.offset(-1,-1);
                polygonPoints.add(bottomRight);
                polygonPoints.add(topRight);
                centerX = (float) ((topRight.getX() + backTopRight.getX()) / 2.0);
                
                paint = new GradientPaint(centerX, bottomRight.getY(), fillColor, centerX, backTopRight.getY(), lightFillColor);
                GDISupport.drawPolygon(pDrawInfo.getTSEGraphics(), polygonPoints, borderColor, 1,  paint);
                
//            }            
                // Draw each compartment now               
               handleNameListCompartmentDrawForContainers(pDrawInfo, frontBoundingRect); 
               super.doDraw(pDrawInfo);              
        }

   public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
   {
      IETSize retVal = new ETSize( NODE_WIDTH, NODE_HEIGHT );

      {
         IETSize tempSize = super.calculateOptimumSize(pDrawInfo, true);
      
         // This is not necessary in the C++ code because there is a minimum
         // size set via the call from CGenericNode::setupOwner(), but I (BDB)
         // don't know how to get that to work.
         
         retVal.setWidth( Math.max( tempSize.getWidth(), NODE_WIDTH ));
         retVal.setHeight( Math.max( tempSize.getHeight(), NODE_HEIGHT ));
      }
      
      return bAt100Pct || retVal == null ? retVal : scaleSize(retVal, pDrawInfo != null ? pDrawInfo.getTSTransform() : getTransform());
   }

	public String getDrawEngineID() 
	{
		return "ClassNodeDrawEngine";
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