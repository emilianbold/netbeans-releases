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
import java.awt.Point;
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.Collection;
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
import org.netbeans.modules.uml.core.metamodel.core.constructs.IPartFacade;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IExtensionPoint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITaggedValue;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.ADNodeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETNameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETPackageImportCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETTaggedValuesCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETStereoTypeCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETStaticTextCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETClassNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETClassNameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETExtensionPointListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETExtensionPointCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADClassNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADExtensionPointListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.INameListCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNodeUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IListCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import javax.swing.SwingConstants;
import org.netbeans.modules.uml.common.ETException;

/**
 * @author jingmingm
 *
 */
public class ETUseCaseDrawEngine extends ADNodeDrawEngine
{
//	protected static int MIN_WIDTH = 160;
//	protected static int MIN_HEIGHT = 80;
	
	public String getElementType()
	{
		String type = super.getElementType();
		if (type == null)
		{
			type = new String("UseCase");
		}
		return type;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initResources()
	 */
	public void initResources()
	{
		setFillColor("usecasefill", 170, 244, 243);
        setLightGradientFillColor("usecaselightgradientfill", 255, 255, 255);
		setBorderColor("usecaseborder", Color.BLACK);

		super.initResources();
	}

	protected void drawLineUnderNameCompartmentClippedToEllipse(IDrawInfo pTSEDrawInfo, IETRect boundingRect, Color lineColor)
	{
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#doDraw(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo)
	 */
	public void drawContents(IDrawInfo pDrawInfo)
	{
//		TSEGraphics graphics = pDrawInfo.getTSEGraphics();
//		ETGenericNodeUI parentUI = (ETGenericNodeUI)this.getParent();
//		
//		Color borderColor = getBorderBoundsColor();
//		Color fillColor = getBkColor();
//		
//		IETRect boundingRect = pDrawInfo.getDeviceBounds();
//		// draw the background of the node if necessary		
//		if (!parentUI.isTransparent())
//		{				
//			graphics.setColor(fillColor);
//			GDISupport.fillEllipse(graphics, boundingRect.getRectangle());
//		}
//
//		// draw the border of the node if necessary
//		if (parentUI.isBorderDrawn())
//		{
//			graphics.setColor(borderColor);
//			GDISupport.frameEllipse(graphics, boundingRect.getRectangle());
//		}
//		
//		// Draw the compartments
//		IListCompartment pExtensionListCompartment = (IListCompartment)getCompartmentByKind(ETExtensionPointListCompartment.class);
//		if (pExtensionListCompartment != null)
//		{
//			int num = pExtensionListCompartment.getNumCompartments();
//
//			// Collapse the compartment if there aren't any extension points.  Make sure to
//			// do it in a way that doesn't set the diagram to readonly
//			pExtensionListCompartment.setCollapsed(num>0?false:true);
//
//			// Draw each compartment
//			handleNameListCompartmentDraw(pDrawInfo, boundingRect, 0, 0, false, 0);
//
//			//if (num > 0)
//			{
//				// Now draw the line under the name compartment
//				drawLineUnderNameCompartmentClippedToEllipse(pDrawInfo, boundingRect, borderColor);
//			}
//		}



		TSEGraphics graphics = pDrawInfo.getTSEGraphics();
		ETGenericNodeUI parentUI = (ETGenericNodeUI)this.getParent();
		
		TSTransform transform = graphics.getTSTransform();
		TSESolidObject owner = parentUI.getOwnerNode();
		TSConstRect localBounds = owner.getLocalBounds();
		Shape oldClip = graphics.getClip();
		Rectangle newClip = transform.boundsToDevice(localBounds);
		graphics.clipRect(newClip.x, newClip.y, newClip.width, newClip.height);
		
		Color borderColor = getBorderBoundsColor();
		Color fillColor = getBkColor();
		
		// draw yourself only if you have an owner
		if (parentUI.getOwner() != null)
		{
			IETRect deviceRect = pDrawInfo.getDeviceBounds();
			// draw the background of the node if necessary		
			if (!parentUI.isTransparent())
			{				
                float centerX = (float) deviceRect.getCenterX();
                GradientPaint paint = new GradientPaint(centerX,
                                                 deviceRect.getBottom(),
                                                 fillColor,
                                                 centerX,
                                                 deviceRect.getTop(),
                                                 getLightGradientFillColor());
        
				graphics.setPaint(paint);
				GDISupport.fillEllipse(graphics, deviceRect.getRectangle());
			}

			// draw the border of the node if necessary

			if (parentUI.isBorderDrawn())
			{
				graphics.setColor(borderColor);
				GDISupport.frameEllipse(graphics, deviceRect.getRectangle());
			}
			
			// draw the layout constraint badge if necessary
			
			((ETGenericNodeUI)parentUI).drawConstraintBadge(graphics);
			
			// Draw the compartments
			int x = deviceRect.getLeft();
			int w = deviceRect.getIntWidth();
			int y = deviceRect.getTop();
			int h = deviceRect.getIntHeight();	
			
			IListCompartment pExtensionPointListCompartment = (IListCompartment)getCompartmentByKind(ETExtensionPointListCompartment.class);
			IListCompartment pNameListCompartment = (IListCompartment)getCompartmentByKind(ETClassNameListCompartment.class);
			int nameHeight = 0, extensionHeight = 0;
			if (pExtensionPointListCompartment != null && pExtensionPointListCompartment.getNumCompartments() > 0)
			{
				extensionHeight = pExtensionPointListCompartment.calculateOptimumSize(pDrawInfo, false).getHeight() + 22;
			}
			
			if (pNameListCompartment != null)
			{
				int staticHeight = 0;
				nameHeight = h - extensionHeight;
				Iterator < ICompartment > ETClassNameListCompartmentIterator = pNameListCompartment.getCompartments().iterator();
				while (ETClassNameListCompartmentIterator.hasNext())
				{
					// Draw the compartment
					ICompartment compartment = ETClassNameListCompartmentIterator.next();
					IETSize compartmentSize = compartment.calculateOptimumSize(pDrawInfo, false);

					if (compartment instanceof ETClassNameCompartment)
					{
						this.setLastDrawPointY((int)(y/* + nameHeight/2 - compartmentSize.getHeight()/2*/));
						compartmentSize.setHeight(nameHeight);
					}
					else if (compartment instanceof ETPackageImportCompartment)
					{
						this.setLastDrawPointY(y + nameHeight - compartmentSize.getHeight());
					}
					else if (compartment instanceof ETTaggedValuesCompartment)
					{
						if (((ETNameListCompartment)pNameListCompartment).getCompartmentByKind(ETPackageImportCompartment.class) == null)
						{
							this.setLastDrawPointY(y + nameHeight - compartmentSize.getHeight());
						}
						else
						{
							this.setLastDrawPointY(y + nameHeight - 2*compartmentSize.getHeight());
						}
					}
					else if (compartment instanceof ETStereoTypeCompartment)
					{
						this.setLastDrawPointY(y + staticHeight);
					}
					else if (compartment instanceof ETStaticTextCompartment)
					{
						this.setLastDrawPointY(y);
						staticHeight = compartment.calculateOptimumSize(pDrawInfo, false).getHeight();
					}
							
					compartment.draw(pDrawInfo, new ETRect(x, this.getLastDrawPointY(), w, compartmentSize.getHeight()));
				}
			}
			
			if (pExtensionPointListCompartment != null)
			{
				pExtensionPointListCompartment.draw(pDrawInfo, new ETRect(x, y + nameHeight, w, extensionHeight));
				
				// Draw the line
				double x0 = deviceRect.getCenterX();
				double y0 = deviceRect.getCenterY();
				double a = deviceRect.getWidth()/2;
				double b = deviceRect.getHeight()/2;
				double yLine = y + nameHeight;
				//if (yLine > y0 - b)
				{
					double xLine = a*Math.sqrt(1.0 - ((yLine - y0)*(yLine - y0))/(b*b));
					//GDISupport.drawLine(pDrawInfo.getTSEGraphics(), new Point((int)(x0 + xLine), (int)yLine), new Point((int)(x0 - xLine), (int)yLine));
                                        Color prevColor = graphics.getColor(); // save the current color
                                        graphics.setColor(borderColor);        // set the color to the borderColor
                                        GDISupport.drawLine(graphics, new Point((int)(x0 + xLine), (int)yLine), new Point((int)(x0 - xLine), (int)yLine));
                                        graphics.setColor(prevColor);          // restore the color to the saved one
				}
			}
		}
		
		// now restore the old clipping
		graphics.setClip(oldClip);
	}
	
	public void initCompartments(IPresentationElement pElement)
	{
		//	We may get here with no compartments.  This happens if we've been created
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

		IElement pModelElement = pElement.getFirstSubject();
		if (pModelElement != null)
		{
			// Get the metatype of the element, we use it later to turn off the package import compartment
			// if necessary.
			String sElementType = pModelElement.getElementType();

			INameListCompartment pNameListCompartment = getCompartmentByKind(ETClassNameListCompartment.class);
			IListCompartment pExtensionPointsCompartment = getCompartmentByKind(ETExtensionPointListCompartment.class);

			if (pNameListCompartment != null && pExtensionPointsCompartment != null)
			{
				if (sElementType != null && sElementType.equals("PartFacade"))
				{
					// Disable the package import compartment if we have a part facade
					pNameListCompartment.setPackageImportCompartmentEnabled(false);
				}

				pNameListCompartment.attach(pModelElement);
				
				// Make sure this node resizes to fit its compartments
				pNameListCompartment.setResizeToFitCompartments(true);

				// Make sure the name compartment wraps
				IADNameCompartment pNameCompartment = pNameListCompartment.getNameCompartment();
				if (pNameCompartment != null)
				{
					pNameCompartment.setTextWrapping(true);
					pNameCompartment.setCenterText(true);
					pNameCompartment.setVerticallyCenterText(true);
				}

				// Set the static text
				setName(pNameListCompartment);

				// Init the extension points compartment
				if (pModelElement instanceof IUseCase)
				{
					IUseCase pUseCase = (IUseCase)pModelElement;
					ETList<IExtensionPoint> pExtensionPoints = pUseCase.getExtensionPoints();
					ETList<IElement> pElements = new ETArrayList<IElement>((Collection)pExtensionPoints);
					if(pElements != null)
					{
						pExtensionPointsCompartment.attachElements(pElements, true, true);
						pExtensionPointsCompartment.setName("values");
					}
				}
			}
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#createCompartments()
	 */
	public void createCompartments() throws ETException 
	{
		clearCompartments();
		createAndAddCompartment("ADClassNameListCompartment");
		createAndAddCompartment("ADExtensionPointListCompartment");
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#calculateOptimumSize(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, boolean)
	 */
	public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
	{
		// call parent to get the size of our text
		IETSize retVal = super.calculateOptimumSize(pDrawInfo, true);
		// 6390542, remove the min size setting to fit the short name element
		// minimum size is the text size, now compute the minimum ellipse that will cover the text
//		val.setWidth(val.getWidth() + val.getHeight());
//		val.setHeight((int)(val.getHeight() * 1.5));
//		
		// Check min
//		IETSize retVal = new ETSize(Math.max(val.getWidth(), MIN_WIDTH), Math.max(val.getHeight(), MIN_HEIGHT));
		return bAt100Pct || retVal == null ? retVal : scaleSize(retVal,  pDrawInfo != null ? pDrawInfo.getTSTransform() : getTransform());
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.application.action.IETContextMenuHandler#setSensitivityAndCheck(java.lang.String, org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass)
	 */
	public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pClass)
	{
		boolean bFlag;
		
		if (id != null && id.equals("MBK_INSERT_USECASE_EXTENSIONPOINT"))
		{
			// Always sensitive, unless the diagram is readonly
			bFlag = isParentDiagramReadOnly() ? false : true;
		}
		else
		{
			bFlag = super.setSensitivityAndCheck(id, pClass);
		}
		
		return bFlag;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.application.action.IETContextMenuHandler#onHandleButton(java.awt.event.ActionEvent, java.lang.String)
	 */
	public boolean onHandleButton(ActionEvent e, String id)
	{
		boolean handled = false;
		if (id != null && id.equals("MBK_INSERT_USECASE_EXTENSIONPOINT"))
		{
			IADExtensionPointListCompartment comp = (IADExtensionPointListCompartment)getCompartmentByKind(IADExtensionPointListCompartment.class);
			if (comp == null)
			{
				comp = (IADExtensionPointListCompartment)createAndAddCompartment("ADExtensionPointListCompartment");
			}
			if (comp != null)
			{
				comp.addCompartment(null, 0, false);
			}
		}
		else
		{
			handled = super.onHandleButton(e, id);
		}
		return handled;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#onContextMenu(org.netbeans.modules.uml.ui.products.ad.application.IMenuManager)
	 */
	public void onContextMenu(IMenuManager manager)
	{
		ICompartment pCompartment = getCompartmentByKind(IADExtensionPointListCompartment.class);
		boolean collapsed = false;
		if (pCompartment != null && pCompartment instanceof IADExtensionPointListCompartment)
		{
			IADExtensionPointListCompartment comp = (IADExtensionPointListCompartment)pCompartment;
			collapsed = comp.getCollapsed();
		}
		if (collapsed || ((IListCompartment)pCompartment).getNumCompartments() == 0)
		{
			// Add the extension point add button.
			manager.add(createMenuAction(loadString("IDS_POPUP_INSERT_EXTENSIONPOINT"), "MBK_INSERT_USECASE_EXTENSIONPOINT"));
		}
		
		super.onContextMenu(manager);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getDrawEngineID()
	 */
	public String getDrawEngineID() 
	{
		return "UseCaseDrawEngine";
	}

	/**
	 * Is this draw engine valid for the element it is representing?
	 *
	 * @param bIsValid[in] true if this draw engine can correctly represent the attached model element.
	 */
	public boolean isDrawEngineValidForModelElement()
	{
		String metaType = getMetaTypeOfElement();
		return metaType != null && (metaType.equals("UseCase") || metaType.equals("PartFacade"));
	}

	/**
	 * Sets the static text compartment in the name list if necessary
	 *
	 * @param pCompartment [in] Our name list comparment, just so we don't have to reget it.
	 */
	private void setName(INameListCompartment pCompartment)
	{
		String text = "";
		if (pCompartment != null)
		{
			IElement pEle = getFirstModelElement();
			if (pEle != null && pEle instanceof IPartFacade)
			{
				text = "<<role>>";
			}
		}
		
		pCompartment.addStaticText(text);
	}
	
	/**
	 * This is the string to be used when looking for other similar drawengines.
	 *
	 * @param sID [out,retval] The unique engine identifier
	 */
	public String getDrawEngineMatchID()
	{
		IElement pEle = getFirstModelElement();
		if (pEle instanceof IPartFacade)
		{
			return "ActorDrawEngine PartFacade";
		}
		else
		{
			return super.getDrawEngineMatchID();
		}
	}
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#readFromArchive(org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive, org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement)
	 */
	public long readFromArchive(IProductArchive pProductArchive, IProductArchiveElement pEngineElement)
	{	
		long retval = super.readFromArchive(pProductArchive, pEngineElement);
		if (this.getPresentationElement() != null)
		{
			this.initCompartments(this.getPresentationElement());
		}
		return retval;
	}

	/**
	 * Used in ResizeToFitCompartment.  Returns the resize behavior
	 * PSK_RESIZE_ASNEEDED     :  Always resize to fit. May grow or shrink.
	 * PSK_RESIZE_EXPANDONLY   :  Grows only if necessary, never shrinks.
	 * PSK_RESIZE_UNLESSMANUAL :  Grows only if the user has not manually resized. Never shrinks.
	 * PSK_RESIZE_NEVER        :  Never resize.
	 *
	 * @param sBehavior [out,retval] The behavior when resize to fit compartment is called.
	 */
	public String getResizeBehavior()
	{
		return "PSK_RESIZE_EXPANDONLY";
	}

   protected void setPadlockLocation(IDrawInfo pDrawInfo, Padlock padlock) {
       TSTransform tsTransform = pDrawInfo.getTSTransform();
       //
       IETRect deviceRect = pDrawInfo.getDeviceBounds();
       TSConstRect worldRect = tsTransform.boundsToWorld(deviceRect.getRectangle());
       //
       padlock.setOriginalPoint(SwingConstants.CENTER);
       padlock.setLocation(worldRect.getCenterX() - 2d, worldRect.getCenterY() - 2d);
   }
        
}
