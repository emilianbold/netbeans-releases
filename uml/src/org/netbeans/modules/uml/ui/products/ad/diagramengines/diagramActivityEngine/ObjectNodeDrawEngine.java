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



package org.netbeans.modules.uml.ui.products.ad.diagramengines.diagramActivityEngine;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.ADNodeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADStaticTextCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.INameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineLineKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETRectEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import com.tomsawyer.editor.TSESolidObject;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Rectangle;

/**
 * @author KevinM
 *
 */
public class ObjectNodeDrawEngine extends ADNodeDrawEngine implements IObjectNodeDrawEngine {

	protected static final int NODE_WIDTH = 50;
	protected static final int NODE_HEIGHT = 40;

	/**
	 * 
	 */
	public ObjectNodeDrawEngine() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#doDraw(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo)
	 */
	public void doDraw(IDrawInfo pDrawInfo) {

		try {
			if (pDrawInfo != null) {
				// Get the bounding rectangle of the node.
				IETRect boundingRect = pDrawInfo.getDeviceBounds();

				// draw our frame

				//				GetColorDefaultText( CK_BORDERCOLOR, pTSEDrawInfo->dc() );
				Color crLineColor = this.getBorderBoundsColor();
				Color crFill = this.getBkColor(); // = GetBrush( CK_FILLCOLOR );
                                Color crLightFill = this.getLightGradientFillColor();
                                float centerX = (float) boundingRect.getCenterX();
                                GradientPaint paint = new GradientPaint(centerX, boundingRect.getBottom(), crFill, centerX, boundingRect.getTop(), crLightFill);

				String currentMetaType = getMetaTypeOfElement();
				if (currentMetaType != null && currentMetaType.equals("SignalNode") == true) {

					// make the point inset 15 or width/5
					int rectWidth = boundingRect.getIntWidth() / 5;
					int pointInset = Math.min(15, rectWidth);
					// Draw a rectangle around the name compartment
					//
					//    |------------\   
					//    |             \  
					//    |              \ 
					//    |               \
					//    |    Name       /
					//    |              /
					//    |             /
					//    |------------/
					//
					
					ETList < IETPoint > signalPoints = new ETArrayList < IETPoint > ();


					signalPoints.add(new ETPoint(boundingRect.getTopLeft()));
					signalPoints.add(new ETPoint(boundingRect.getLeft(), boundingRect.getBottom()));
					signalPoints.add(new ETPoint(boundingRect.getRight() - pointInset, boundingRect.getBottom()));
					signalPoints.add(new ETPoint(boundingRect.getRight(), (int)boundingRect.getCenterPoint().getY()));
					signalPoints.add(new ETPoint(boundingRect.getRight() - pointInset, boundingRect.getTop()));

					signalPoints.add(signalPoints.get(0));

					GDISupport.drawPolygon(pDrawInfo.getTSEGraphics(), signalPoints, crLineColor, paint);

				} else {
					// ParameterUsage and DataStore Nodes are rectangles
					GDISupport.drawRectangle(pDrawInfo.getTSEGraphics(), boundingRect.getRectangle(), crLineColor, paint);
				}
				// Draw each compartment now
				handleNameListCompartmentDraw(pDrawInfo, boundingRect);

				// This will draw an invalid frame around the node if it doesn't have an IElement
				drawInvalidRectangle(pDrawInfo);

				// Put the selection handles
				// CGDISupport::DrawSelectionHandles(pInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#sizeToContents()
	 */
	public void sizeToContents() {
		// Size but keep the current size if possible
		sizeToContentsWithMin(NODE_WIDTH, NODE_HEIGHT, true, true);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		// TODO Auto-generated method stub
		return super.clone();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#createCompartments()
	 */
	public void createCompartments() throws ETException {
		try {
			clearCompartments();

			createAndAddCompartment("ADNameListCompartment", 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the meta type of the IElement this draw engine represents.
	 *
	 * @return The metatype of the element this draw engine represents.
	 */
	protected String getMetaTypeOfElement() {
		//return getUI() != null && getUI().getModelElement() != null ? getUI().getModelElement().getElementType() : null; // getMetaTypeOfElement();
		return super.getMetaTypeOfElement();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initCompartments(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
	 */
	public void initCompartments(IPresentationElement pElement) {
		try {
			// We may get here with no compartments.  This happens if we've been created
			// by the user.  If we read from a file then the compartments have been pre-created and
			// we just need to initialize them.
			int numCompartments = getNumCompartments();

			if (numCompartments == 0) {
				createCompartments();
			}

			String currentMetaType = getMetaTypeOfElement();

			if (currentMetaType != null && currentMetaType.equals("DataStoreNode")) {
				// Make sure we have a static text compartment showing the label <<datastore>>
				IADStaticTextCompartment pADStaticTextCompartment = getCompartmentByKind(IADStaticTextCompartment.class);
				if (pADStaticTextCompartment == null) {
					// Create the label for DataStores
					ICompartment pCompartment = createAndAddCompartment("ADStaticTextCompartment", 0);
					IADStaticTextCompartment pStaticTextCompartment = pCompartment instanceof IADStaticTextCompartment ? (IADStaticTextCompartment) pCompartment : null;
					if (pStaticTextCompartment != null) {
						pStaticTextCompartment.setName("<<datastore>>");
						pStaticTextCompartment.setReadOnly(true);
					}
				}
			}

			IElement pModelElement = pElement != null ? pElement.getFirstSubject() : null;
			if (pModelElement != null) {
				INameListCompartment pNameCompartment = getCompartmentByKind(INameListCompartment.class);

				if (pNameCompartment != null) {
					pNameCompartment.attach(pModelElement);

					// Make sure this node resizes to fit its compartments
					pNameCompartment.setResizeToFitCompartments(true);
               
               IADNameCompartment nameCompartment = pNameCompartment.getNameCompartment();
               if(nameCompartment != null)
               {
                  nameCompartment.setTextWrapping(true);
                  nameCompartment.setCenterText(true);
                  nameCompartment.setVerticallyCenterText(true); 
               }
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initResources()
	 */
	public void initResources()
	{
		setFillColor("objectnodefill", 255, 182, 105);
		setLightGradientFillColor("objectnodelightgradientfill", 255, 239, 208);
		setBorderColor("objectnodeborder", Color.BLACK);
		super.initResources();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#isDrawEngineValidForModelElement()
	 */
	public boolean isDrawEngineValidForModelElement() {
		String currentMetaType = getMetaTypeOfElement();
		return currentMetaType != null && (currentMetaType.equals("SignalNode") || currentMetaType.equals("DataStoreNode") || currentMetaType.equals("ParameterUsageNode"));
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#modelElementDeleted(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
	 */
	public long modelElementDeleted(INotificationTargets pTargets) {
		return handleNameListModelElementDeleted(pTargets);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#modelElementHasChanged(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
	 */
	public long modelElementHasChanged(INotificationTargets pTargets) {
		return handleNameListModelElementHasChanged(pTargets);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#onContextMenu(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, int, int)
	 */
	public long onContextMenu(IProductContextMenu pContextMenu, int logicalX, int logicalY) {
		// TODO Auto-generated method stub
		return super.onContextMenu(pContextMenu, logicalX, logicalY);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#onContextMenuHandleSelection(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem)
	 */
	public long onContextMenuHandleSelection(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem) {
		// TODO Auto-generated method stub
		return super.onContextMenuHandleSelection(pContextMenu, pMenuItem);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#setSensitivityAndCheck(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem, int)
	 */
	public boolean setSensitivityAndCheck(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem, int buttonKind) {
		// TODO Auto-generated method stub
		return super.setSensitivityAndCheck(pContextMenu, pMenuItem, buttonKind);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getDrawEngineID()
	 */
	public String getDrawEngineID() {
		return "ObjectNodeDrawEngine";
	}
}
