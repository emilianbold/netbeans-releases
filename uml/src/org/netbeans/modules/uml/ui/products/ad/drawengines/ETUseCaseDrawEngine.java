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
import java.awt.GradientPaint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;
import com.tomsawyer.editor.graphics.TSEGraphics;
import com.tomsawyer.editor.TSESolidObject;
import com.tomsawyer.drawing.geometry.TSConstRect;
import com.tomsawyer.editor.TSTransform;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IPartFacade;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IExtensionPoint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI;
import org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.ADNodeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETClassNameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETExtensionPointListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADExtensionPointListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.INameListCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IListCompartment;
import javax.swing.SwingConstants;
import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.core.support.umlsupport.ETDeviceRect;

/**
 * @author jingmingm
 *
 */
public class ETUseCaseDrawEngine extends ADNodeDrawEngine
{
    protected static int MIN_NODE_WIDTH = 90;
    protected static int MIN_NODE_HEIGHT = 60;
    
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
        setLightGradientFillColor("usecaselightgradient", 255, 255, 255);
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
        ETGenericNodeUI parentUI = (ETGenericNodeUI)this.getParent();
        
        // draw yourself only if you have an owner
        if (parentUI != null && parentUI.getOwner() != null)
        {
            TSEGraphics graphics = pDrawInfo.getTSEGraphics();
            TSTransform transform = graphics.getTSTransform();
            TSESolidObject owner = parentUI.getOwnerNode();
            TSConstRect localBounds = owner.getLocalBounds();   // node bound
            Shape oldClip = graphics.getClip();
            Rectangle newClip = transform.boundsToDevice(localBounds);
            graphics.clipRect(newClip.x, newClip.y, newClip.width, newClip.height);
            
            Color borderColor = getBorderBoundsColor();
            Color fillColor = getBkColor();
            IETRect deviceRect = pDrawInfo.getDeviceBounds();
            
            // draw the background of the node if necessary,
            // i.e. fill the ellipse shape of the node with the bkground color
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
            
            // draw the border of the node if necessary,
            // i.e. draw the frame of the ellipse with border color
            if (parentUI.isBorderDrawn())
            {
                graphics.setColor(borderColor);
                GDISupport.frameEllipse(graphics, deviceRect.getRectangle());
            }
            
            // draw the layout constraint badge if necessary
            ((ETGenericNodeUI)parentUI).drawConstraintBadge(graphics);
            
            // draw the compartments
            IListCompartment pExtensionPointListCompartment = (IListCompartment)getCompartmentByKind(ETExtensionPointListCompartment.class);
            IListCompartment pNameListCompartment = (IListCompartment)getCompartmentByKind(ETClassNameListCompartment.class);
            drawListCompartment(pDrawInfo,borderColor,pNameListCompartment,pExtensionPointListCompartment);
            
            // now restore the old clipping
            graphics.setClip(oldClip);
        }
    }
    
    private void drawListCompartment(IDrawInfo pDrawInfo, Color borderColor,
            IListCompartment pNameListCompartment,
            IListCompartment pExtensionPointListCompartment)
    {
        if (pDrawInfo == null)
        {
            return;
        }
        IETRect boundingRect = pDrawInfo.getDeviceBounds();
        int nameBottom = 0;
        ETDeviceRect thisCompartmentBoundingRect = null;
        //clearVisibleCompartments();
        boolean hasExtensionPoints = (pExtensionPointListCompartment != null &&
                pExtensionPointListCompartment.getNumCompartments() > 0);
        
        // draw name compartment
        if (pNameListCompartment != null)
        {
            IETSize nameSize = pNameListCompartment.calculateOptimumSize(pDrawInfo, false);
            thisCompartmentBoundingRect = ETDeviceRect.ensureDeviceRect((IETRect)boundingRect.clone());
            
            nameBottom = hasExtensionPoints ?
                (boundingRect.getTop() + nameSize.getHeight()) :
                boundingRect.getBottom();
            
            thisCompartmentBoundingRect.setBottom(nameBottom);
            addVisibleCompartment(pNameListCompartment);
            pNameListCompartment.draw(pDrawInfo, thisCompartmentBoundingRect);
        }
        
        // draw extensionPointList compartment if there's some extension points
        if (hasExtensionPoints)
        {
            thisCompartmentBoundingRect = ETDeviceRect.ensureDeviceRect((IETRect)boundingRect.clone());
            thisCompartmentBoundingRect.setTop(nameBottom);
            addVisibleCompartment(pExtensionPointListCompartment);
            pExtensionPointListCompartment.draw(pDrawInfo, thisCompartmentBoundingRect);
            
            // Draw the line
            double x0 = boundingRect.getCenterX();
            double y0 = boundingRect.getCenterY();
            double a = boundingRect.getWidth()/2;
            double b = boundingRect.getHeight()/2;
            int y = boundingRect.getTop();
            
            double yLine = nameBottom;
            double xLine = a*Math.sqrt(1.0 - ((yLine - y0)*(yLine - y0))/(b*b));
            
            TSEGraphics graphics = pDrawInfo.getTSEGraphics();
            // save the current color
            Color prevColor = graphics.getColor();
            // set the color to the borderColor
            graphics.setColor(borderColor != null ? borderColor : getBorderBoundsColor());
            // draw a line between nameList compartment and extensionPointList compartment
            GDISupport.drawLine(graphics,
                    new Point((int)(x0 + xLine), (int)yLine),
                    new Point((int)(x0 - xLine), (int)yLine));
            graphics.setColor(prevColor);          // restore the color to the saved one
        }
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
                    if(pElements != null )
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
        // 6390542, remove the min size setting to fit the short name element
        // minimum size is the text size, now compute the minimum ellipse that will cover the text
        //		val.setWidth(val.getWidth() + val.getHeight());
        //		val.setHeight((int)(val.getHeight() * 1.5));
        //
        int optimumSizeX = 0, optimumSizeY = 0;
        
        Iterator iterator = this.getCompartments().iterator();
        while (iterator.hasNext())
        {
            ICompartment compartment = (ICompartment) iterator.next();
            
            IETSize curSize = compartment.calculateOptimumSize(pDrawInfo, true);
            optimumSizeX = Math.max(optimumSizeX, curSize.getWidth());
            optimumSizeY += curSize.getHeight();
        }
        
        optimumSizeX = Math.max(optimumSizeX, MIN_NODE_WIDTH);
        optimumSizeY = Math.max(optimumSizeY, MIN_NODE_HEIGHT);
        
        IETSize retVal = new ETSize(optimumSizeX, optimumSizeY);
        
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
    
    protected void setPadlockLocation(IDrawInfo pDrawInfo, Padlock padlock)
    {
        TSTransform tsTransform = pDrawInfo.getTSTransform();
        //
        IETRect deviceRect = pDrawInfo.getDeviceBounds();
        TSConstRect worldRect = tsTransform.boundsToWorld(deviceRect.getRectangle());
        //
        padlock.setOriginalPoint(SwingConstants.CENTER);
        padlock.setLocation(worldRect.getCenterX() - 2d, worldRect.getCenterY() - 2d);
    }
    
}
