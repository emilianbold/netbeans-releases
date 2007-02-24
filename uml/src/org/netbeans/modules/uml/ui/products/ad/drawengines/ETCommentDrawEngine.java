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
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlsupport.ETDeviceRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETCommentBodyCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADCommentBodyCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IPackageImportCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IStereotypeCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ITaggedValuesCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import com.tomsawyer.editor.TSEColor;
import com.tomsawyer.editor.ui.TSERectangularUI;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.structure.IComment;
import org.netbeans.modules.uml.common.generics.IteratorT;
import org.netbeans.modules.uml.ui.products.ad.compartments.INameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETEditableCompartment;
import java.awt.GradientPaint;

import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingPropertyProvider;



/*
 * 
 * @author KevinM
 */
public class ETCommentDrawEngine extends ETNodeDrawEngine {

	public ETCommentDrawEngine() {
		m_nBorderThickness = 1;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getElementType()
	 */
	public String getElementType() {
		String type = super.getElementType();
		if (type == null) {
			type = new String("Comment");
		}
		return type;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initResources()
	 */
	public void initResources() {
		super.initResources();
		setFillColor("commentfill", 211, 227, 244);
                setLightGradientFillColor("commentlightgradientfill", 255, 255, 255);
		setBorderColor("commentborder", 0, 0, 128);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getDrawEngineID()
	 */
	public String getDrawEngineID() {
		return "CommentDrawEngine";
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#doDraw(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo)
	 */
	public void doDraw(IDrawInfo pDrawInfo) {
		if (this.getParent() == null)
			return;

		drawCompartments(pDrawInfo);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawengines.ETNodeDrawEngine#drawContents(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo)
	 */
	protected void drawContents(IDrawInfo pDrawInfo) {
		super.drawContents(pDrawInfo);
		drawCompartments(pDrawInfo);
	}

	/*
	 * Returns the name compartment.
	 */
	protected IADNameCompartment getNameCompartmentFromList() {
		INameListCompartment nameList = this.getNameListCompartment();
		IADNameCompartment nameCompartment = nameList != null ? nameList.getNameCompartment() : null;

		return nameCompartment != null ? nameCompartment : (IADNameCompartment) getCompartmentByKind(IADNameCompartment.class);
	}

	/*
	 * Renders the Compartments.
	 */
	protected void drawCompartments(IDrawInfo pDrawInfo) {
		INameListCompartment nameList = this.getNameListCompartment();

		IETRect boundingRect = pDrawInfo.getDeviceBounds();

		INameListCompartment pNameListCompartment = nameList;
		IADCommentBodyCompartment pCommentBodyCompartment = this.getCommentBodyCompartment();
		IStereotypeCompartment pStereotypeCompartment = (IStereotypeCompartment) nameList.getStereotypeCompartment();
		ITaggedValuesCompartment pTaggedValuesCompartment = nameList.getTaggedValuesCompartment();
		IPackageImportCompartment pPackageImportCompartment = nameList.getPackageImportCompartment();
		IADNameCompartment pNameCompartment = getNameCompartmentFromList();

		IETSize nameCompartmentSize = pNameCompartment != null ? pNameCompartment.calculateOptimumSize(pDrawInfo, false) : new ETSize(0, 0);
		IETSize commentBodyCompartmentSize = new ETSize(0, 0);
		IETSize stereotypeCompartmentSize = new ETSize(0, 0);
		IETSize taggedValuesCompartmentSize = new ETSize(0, 0);
		IETSize packageImportCompartmentSize = new ETSize(0, 0);
		boolean nameCompartmentVisible = false;
		if (pNameCompartment != null) {
			String sNameText = pNameCompartment.getName();

			if (sNameText == null || sNameText.length() == 0) {
				// If the name compartment isn't showing anything then reduce the size to 0
				nameCompartmentSize.setHeight(0);
				pNameCompartment.setEnableContextMenu(false);
			} else {
				pNameCompartment.setEnableContextMenu(true);
				nameCompartmentVisible = true;
			}
		}

		if (pCommentBodyCompartment != null) {
			IETSize optimumSize = pCommentBodyCompartment.calculateOptimumSize(pDrawInfo, false);
			commentBodyCompartmentSize.setWidth(optimumSize.getWidth());
			commentBodyCompartmentSize.setHeight(Math.max(optimumSize.getHeight(), 15)); // Make 15 the min compartment body height
		}

		if (pStereotypeCompartment != null) {
			IETSize optimumSize = pStereotypeCompartment.calculateOptimumSize(pDrawInfo, false);
			stereotypeCompartmentSize.setWidth(optimumSize.getWidth());
			stereotypeCompartmentSize.setHeight(optimumSize.getHeight());
		}

		if (pTaggedValuesCompartment != null) {
			IETSize optimumSize = pTaggedValuesCompartment.calculateOptimumSize(pDrawInfo, false);
			taggedValuesCompartmentSize.setWidth(optimumSize.getWidth());
			taggedValuesCompartmentSize.setHeight(optimumSize.getHeight());
		}

		if (pPackageImportCompartment != null) {
			IETSize optimumSize = pPackageImportCompartment.calculateOptimumSize(pDrawInfo, false);
			packageImportCompartmentSize.setWidth(optimumSize.getWidth());
			packageImportCompartmentSize.setHeight(optimumSize.getHeight());
		}

		// Calculate the sizes for all the various compartments
		IETRect nameCompartmentRect = (IETRect) boundingRect.clone();
		IETRect commentBodyCompartmentRect = (IETRect) boundingRect.clone();
		IETRect stereotypeCompartmentRect = (IETRect) boundingRect.clone();
		IETRect taggedValuesCompartmentRect = (IETRect) boundingRect.clone();
		IETRect packageImportCompartmentRect = (IETRect) boundingRect.clone();

		// Calculate the top compartment - the name
		if (nameCompartmentSize.getHeight() != 0) {
			nameCompartmentRect.setBottom(Math.min(nameCompartmentRect.getBottom(), nameCompartmentRect.getTop() + nameCompartmentSize.getHeight()));
		} else {
			nameCompartmentRect.setBottom(nameCompartmentRect.getTop());
		}

		if (nameCompartmentRect.getBottom() < boundingRect.getBottom()) {
			commentBodyCompartmentRect.setTop(nameCompartmentRect.getBottom());
			if (commentBodyCompartmentRect.getTop() + commentBodyCompartmentSize.getHeight() + stereotypeCompartmentSize.getHeight() + taggedValuesCompartmentSize.getHeight() + packageImportCompartmentSize.getHeight() < boundingRect.getBottom()) {
				commentBodyCompartmentRect.setBottom(boundingRect.getBottom() - (stereotypeCompartmentSize.getHeight() + taggedValuesCompartmentSize.getHeight() + packageImportCompartmentSize.getHeight()));
			} else {
				// The comment body takes up the middle
				commentBodyCompartmentRect.setBottom(Math.min(commentBodyCompartmentRect.getTop() + commentBodyCompartmentSize.getHeight(), boundingRect.getBottom()));
			}
		}
		int nextBottom = commentBodyCompartmentRect.getBottom();

		// Now calculate the text at the bottom of the comment body compartment
		if (stereotypeCompartmentSize.getHeight() != 0 && commentBodyCompartmentRect.getBottom() < boundingRect.getBottom()) {
			stereotypeCompartmentRect.setTop(nextBottom);
			stereotypeCompartmentRect.setBottom(Math.min(stereotypeCompartmentRect.getTop() + stereotypeCompartmentSize.getHeight(), boundingRect.getBottom()));
			nextBottom = stereotypeCompartmentRect.getBottom();
		} else {
			stereotypeCompartmentRect.setSides(0, 0, 0, 0);
		}

		if (taggedValuesCompartmentSize.getHeight() != 0 && nextBottom < boundingRect.getBottom()) {
			taggedValuesCompartmentRect.setTop(nextBottom);
			taggedValuesCompartmentRect.setBottom(Math.min(taggedValuesCompartmentRect.getTop() + taggedValuesCompartmentSize.getHeight(), boundingRect.getBottom()));
			nextBottom = taggedValuesCompartmentRect.getBottom();
		} else {
			taggedValuesCompartmentRect.setSides(0, 0, 0, 0);
		}

		if (packageImportCompartmentSize.getHeight() != 0 && nextBottom < boundingRect.getBottom()) {
			packageImportCompartmentRect.setTop(nextBottom);
			packageImportCompartmentRect.setBottom(Math.min(packageImportCompartmentRect.getTop() + packageImportCompartmentSize.getHeight(), boundingRect.getBottom()));
		} else {
			packageImportCompartmentRect.setSides(0, 0, 0, 0);
		}

		// Draw each of the compartments now
		if (pNameCompartment != null && nameCompartmentRect.getHeight() != 0) {
			pNameCompartment.draw(pDrawInfo, nameCompartmentRect);
		}

		if (pCommentBodyCompartment != null && commentBodyCompartmentRect.getHeight() != 0) {
			if (nameCompartmentVisible)
			{
				commentBodyCompartmentRect.setTop(commentBodyCompartmentRect.getTop() + 2);
				commentBodyCompartmentRect.setBottom(boundingRect.getBottom());
			}
			
			drawTabBorder(pDrawInfo, commentBodyCompartmentRect);
			pCommentBodyCompartment.draw(pDrawInfo, commentBodyCompartmentRect);
		}

		if (pStereotypeCompartment != null && stereotypeCompartmentRect.getHeight() != 0) {
			pStereotypeCompartment.draw(pDrawInfo, stereotypeCompartmentRect);
		}

		if (pTaggedValuesCompartment != null && taggedValuesCompartmentRect.getHeight() != 0) {
			pTaggedValuesCompartment.draw(pDrawInfo, taggedValuesCompartmentRect);
		}

		if (pPackageImportCompartment != null && packageImportCompartmentRect.getHeight() != 0) {
			pPackageImportCompartment.draw(pDrawInfo, packageImportCompartmentRect);
		}
	}

	/*
	 * Draws the Tab Section of the around the Comment body.
	 */
	protected void drawTabBorder(IDrawInfo pDrawInfo, IETRect deviceRect) {
		Rectangle deviceBounds = deviceRect.getRectangle();
		int nFold = Math.min((int) deviceBounds.getWidth() / 4, 10);
		nFold = Math.min((int) deviceBounds.getHeight() / 4, nFold);

		IETPoint topLeft = new ETPoint((int) deviceBounds.x, (int) deviceBounds.y);
		IETPoint bottomLeft = new ETPoint((int) deviceBounds.x, (int) deviceBounds.getMaxY());
		IETPoint bottomRight = new ETPoint((int) deviceBounds.getMaxX(), (int) deviceBounds.getMaxY());
		IETPoint foldUpperLeft = new ETPoint((int) deviceBounds.getMaxX() - nFold, deviceBounds.y);
		IETPoint foldLowerRight = new ETPoint((int) deviceBounds.getMaxX(), deviceBounds.y + nFold);
		IETPoint foldCorner = new ETPoint((int) deviceBounds.getMaxX() - nFold, deviceBounds.y + nFold);

		ETList < IETPoint > pMainPoints = new ETArrayList < IETPoint > ();

		pMainPoints.add(topLeft);
		pMainPoints.add(bottomLeft);
		pMainPoints.add(bottomRight);
		pMainPoints.add(foldLowerRight);
		pMainPoints.add(foldCorner);
		pMainPoints.add(foldUpperLeft);
		pMainPoints.add(topLeft);

                float centerX = (float) deviceRect.getCenterX();
                GradientPaint paint = new GradientPaint(centerX, deviceRect.getBottom(), getBkColor(), centerX, deviceRect.getTop(), getLightGradientFillColor());
		GDISupport.drawPolygon(pDrawInfo.getTSEGraphics().getGraphics(), pMainPoints, this.getBorderBoundsColor(), getBorderThickness(), paint);

		ETList < IETPoint > pTabPoints = new ETArrayList < IETPoint > ();
		pTabPoints.add(foldLowerRight);
		pTabPoints.add(foldUpperLeft);
		pTabPoints.add(foldCorner);
		GDISupport.drawPolygon(pDrawInfo.getTSEGraphics().getGraphics(), pTabPoints, this.getBorderBoundsColor(), getBorderThickness(), this.getBkColor());
	}

	/**
	 * Returns the optimum size for an item.  This is used when an item is created from the toolbar.
	 */
	public void sizeToContents() {
		// super.sizeToContents();		
		sizeToContentsWithMin(150, 80);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#calculateOptimumSize(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, boolean)
	 */
	public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct) {
		ETSystem.out.println("calculateOptimumSize");
		//return this.getCompartments() == null || this.getCompartments().size() == 0 ? 
		IETSize retVal = new ETSize(150, 80); //: super.calculateOptimumSize(pDrawInfo, bAt100Pct);
		
		TSTransform transform = pDrawInfo != null ? pDrawInfo.getTSTransform() : getTransform();
		return bAt100Pct && transform != null ? retVal : super.scaleSize(retVal, transform);	
	}

	/**
	 * Create the compartments for this node.
	 */
	public void createCompartments() throws ETException {
		clearCompartments();

		ICompartment pCreatedCompartment = createAndAddCompartment("ADCommentBodyCompartment");
		createAndAddCompartment("ADNameListCompartment");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initCompartments(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
	 */
	public void initCompartments(IPresentationElement pElement) {
		super.initCompartments(pElement);

		if (getNumCompartments() == 0) {
			try {
				createCompartments();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		IElement pModelElement = TypeConversions.getElement(this.getNode());
		if (pModelElement != null) {

			INameListCompartment pNameCompartment = getCompartmentByKind(INameListCompartment.class);
			if (pNameCompartment != null) {
				pNameCompartment.setNameCompartment("ADCommentNameCompartment");
				pNameCompartment.attach(pModelElement);
				pNameCompartment.setReadOnly(true);

				// Disable the context menu until we know that the name compartment has been drawn.
				pNameCompartment.setEnableContextMenu(false);
			}

			// Init the comment body compartment
			IADCommentBodyCompartment pCommentBodyCompartment = getCompartmentByKind(IADCommentBodyCompartment.class);
			if (pCommentBodyCompartment != null) {
				pCommentBodyCompartment.setNameCompartmentBorderKind(IADNameCompartment.NCBK_DRAW_JUST_NAME);
				pCommentBodyCompartment.addModelElement(pModelElement, -1);
            pCommentBodyCompartment.setTextWrapping(true);
				setDefaultCompartment(pCommentBodyCompartment);
			}
		}
	}

	/*
	 * returns the Name List Compartment interface.
	 */
	public INameListCompartment getNameListCompartment() {
		return (INameListCompartment) getCompartmentByKind(INameListCompartment.class);
	}

	/*
	 * Returns the Comment Body Compartment interface.
	 */
	public IADCommentBodyCompartment getCommentBodyCompartment() {
		return (IADCommentBodyCompartment) getCompartmentByKind(IADCommentBodyCompartment.class);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#isDrawEngineValidForModelElement()
	 */
	public boolean isDrawEngineValidForModelElement() {
		String currentMetaType = getMetaTypeOfElement();
		return currentMetaType != null && currentMetaType.equals("Comment");

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#modelElementHasChanged(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
	 */
	public long modelElementHasChanged(INotificationTargets pTargets) {
		return super.modelElementHasChanged(pTargets);
	}

	/*
	 * Returns the text in the comment body.
	 */
	protected String getBodyText() {
		IADCommentBodyCompartment bodyCompartment = getCommentBodyCompartment();
		IElement element = bodyCompartment != null ? bodyCompartment.getModelElement() : null;
		if (element instanceof IComment) {
			IComment comment = (IComment) element;
			return comment.getBody();
		} else
			return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#handleLeftMouseButtonDoubleClick(java.awt.event.MouseEvent)
	 */
	public boolean handleLeftMouseButtonDoubleClick(MouseEvent pEvent) {
		IADNameCompartment nameCompartment = this.getNameCompartmentFromList();
		boolean handled = false;
		if (nameCompartment instanceof ETNameCompartment) {
                    ETNameCompartment etNameComp = (ETNameCompartment) nameCompartment;
                    if (etNameComp.isMouseInBoundingRect(pEvent, handled)) {
                            // Do not allow edit the name - for now, the related "Comment" property definition has "getBody" as the get 
                            // method  and "setBody" as the set method - it allows to edit body part only (if we allow to edit name, 
                            // the edited text will be set into body, not into name)
                            handled = true;
                    }

                    if (!handled) {
                            handled = nameCompartment.handleLeftMouseButtonDoubleClick(pEvent);
                    }
		}

		return handled ? true : super.handleLeftMouseButtonDoubleClick(pEvent);
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#postLoad()
	 */
	public long postLoad()
	{
		long retVal =  super.postLoad();
		// DT # j1233
		performDeepSynch();
		return retVal;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#handleLeftMouseButtonPressed(java.awt.event.MouseEvent)
	 */
	public boolean handleLeftMouseButtonPressed(MouseEvent pEvent)
	{
		IADCommentBodyCompartment pCommentBodyCompartment = this.getCommentBodyCompartment();
		if (pCommentBodyCompartment != null && pCommentBodyCompartment instanceof ETCommentBodyCompartment)
		{
			ETCommentBodyCompartment body = (ETCommentBodyCompartment)pCommentBodyCompartment;
			boolean pHandled = false;
			if (body.isMouseInTextRect(pEvent, pHandled) && !body.isEditing())
			{
				// Allow the user to drag the node if they hit the comment body.
				return false;
			}
		}
		// Give the name list compartments a crack at it.
		return super.handleLeftMouseButtonPressed(pEvent);
	}
	
	/*Added by Smitha- Fix for bug# 6267806*/
	public void saveFont(  String sDrawEngineName,
						   String sResourceName,
						   String sFaceName,
						   int nHeight,
						   int nWeight,
						   boolean bItalic,
						   int nColor)
	{
		super.saveFont(sDrawEngineName, sResourceName, sFaceName, nHeight, nWeight, bItalic, nColor );
	}
	/*Added by Smitha*/
	
	/*Added by Smitha - Fix for bug# 6267806*/
	public void saveColor(String sDrawEngineType, String sResourceName, int nColor)
	{            
		super.saveColor(sDrawEngineType, sResourceName, nColor);
	}
	/*Added by Smitha*/	

	// J2833-Comment body is not selectable but still enable compartment menu
	public void onContextMenu(IMenuManager manager)
	{
		super.onContextMenu(manager);
		int count = getNumCompartments();
		for (int i = 0; i < count; i++)
		{
			ICompartment pCompartment = getCompartment(i);
			if (pCompartment != null && pCompartment instanceof ETCommentBodyCompartment)
			{
				((ETEditableCompartment)pCompartment).addColorAndFontMenuButton(manager);
			}
		}
	}
}
