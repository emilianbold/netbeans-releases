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


//	 $Date$

package org.netbeans.modules.uml.ui.products.ad.drawengines;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.GradientPaint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IPartFacade;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITaggedValue;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.CollectionTranslator;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.structure.IAssociationClass;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.support.umlsupport.ETDeviceRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.INamedCollection;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETAttributeListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETClassNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETClassNameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETCompartmentDivider;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETOperationListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADAttributeListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADClassNameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADOperationListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.INameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ITemplateParametersCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.TemplateParametersCompartment;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.IADCoreEngine;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IListCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ISetCursorEvent;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ModelElementChangedKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.PointConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.graphics.TSEGraphics;
import com.tomsawyer.graph.TSEdge;
import com.tomsawyer.graph.TSNode;
import com.tomsawyer.drawing.geometry.TSConstRect;
import com.tomsawyer.editor.TSTransform;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;


public class ETClassDrawEngine extends ETNodeDrawEngine
{
    
    protected final int NODE_WIDTH = 120;
    protected final int NODE_HEIGHT = 100;
    
    protected Map m_compartmentDividers;
    private ETCompartmentDivider m_draggedDivider = null;
    
    private static final String BUNDLE_NAME = "org.netbeans.modules.uml.ui.products.ad.diagramengines.Bundle"; //$NON-NLS-1$
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
    
    private static final String IDS_EXPAND_TOOL_TIP_TEXT
            = "IDS_EXPAND_TOOL_TIP_TEXT";

    private String redefined_attribute_compartment_ID = "ADAttributeListCompartment";
    private String redefined_operation_compartment_ID = "ADRedefinedOperationListCompartment";
    
    public ETClassDrawEngine()
    {
        super();
        this.m_compartmentDividers = new Hashtable();
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
    
    /**
     * Establish names for our resources
     */
    public void initResources()
    {
        setFillColor("classfill", 251, 233, 127);
        setLightGradientFillColor("classlightgradient", 254,254,254);
        setBorderColor("classborder", Color.BLACK);
        
        super.initResources();
    }
    
    //   protected void drawBorder(IDrawInfo pDrawInfo)
    //   {
    //      TSEGraphics graphics = pDrawInfo.getTSEGraphics();
    //      IETGraphObjectUI parentUI = this.getParent();
    //      if (parentUI.getOwner() != null)
    //      {
    ////         IETRect deviceRect = pDrawInfo.getDeviceBounds();
    ////
    ////         TSTransform transform = pDrawInfo.getTSTransform();
    ////         TSConstRect localBounds = transform.boundsToWorld(deviceRect.getRectangle());
    //
    //         Color bkColor = getBkColor();
    ////         BevelBorder border = new BevelBorder(BevelBorder.RAISED,
    ////                                              bkColor.darker(),
    ////                                              bkColor.brighter());
    ////
    ////         ADGraphWindow wnd = getDrawingArea().getGraphWindow();
    ////         Point topLeft = deviceRect.getTopLeft();
    ////         border.paintBorder((java.awt.Component)wnd,
    ////                            (java.awt.Graphics2D)graphics,
    ////                            topLeft.x,
    ////                            topLeft.y,
    ////                            (int)deviceRect.getWidth(),
    ////                            (int)deviceRect.getHeight());
    //
    //         java.awt.BasicStroke stroke = new java.awt.BasicStroke(3);
    //         java.awt.Stroke curStroke = graphics.getStroke();
    //         graphics.setStroke(stroke);
    //
    //         Color curColor = graphics.getColor();
    //
    //         graphics.setColor(bkColor);
    //
    //         IETRect deviceRect = pDrawInfo.getDeviceBounds();
    //         graphics.draw(deviceRect.getRectangle());
    //
    //         graphics.setColor(curColor);
    //         graphics.setStroke(curStroke);
    //
    //      }
    //   }
    
    public void drawContents(IDrawInfo pDrawInfo)
    {
        
        TSEGraphics graphics = pDrawInfo.getTSEGraphics();
        IETGraphObjectUI parentUI = this.getParent();
        TSTransform transform = pDrawInfo.getTSTransform();
        
        IListCompartment prevFoundComparmtent = null;
        
        if (parentUI.getOwner() != null)
        {
            IETRect deviceRect = pDrawInfo.getDeviceBounds();
            
            TSConstRect localBounds = transform.boundsToWorld(deviceRect.getRectangle());
            
            // Check draw rect
            IETRect templateRect = checkTemplateDrawRect(pDrawInfo);
            
            if (templateRect != null)
            {
                TSConstRect worldRect = transform.boundsToWorld(templateRect.getRectangle());
                localBounds = new TSConstRect(localBounds.getLeft(), localBounds.getBottom(), localBounds.getRight() - 10.0, localBounds.getTop() - worldRect.getHeight() + 10.0);
                
                deviceRect = new ETDeviceRect(transform.boundsToDevice(localBounds));
            }
            
            // Change if you want to demo the Class UI changes.
            float centerX = (float)deviceRect.getCenterX();
            java.awt.GradientPaint paint = new java.awt.GradientPaint(centerX,
                    deviceRect.getBottom(),
                    getBkColor(),
                    centerX,
                    deviceRect.getTop(),
                    getLightGradientFillColor());
            
            //GDISupport.drawRectangle(graphics, deviceRect.getRectangle(), getBorderBoundsColor(), getBkColor());
            GDISupport.drawRectangle(graphics, deviceRect.getRectangle(), getBorderBoundsColor(), paint);
            
            this.setLastDrawPointWorldY(localBounds.getTop() - 5);
            
            // draw the compartments
            Iterator < ICompartment > iterator = this.getCompartments().iterator();
            while (iterator.hasNext())
            {
                ICompartment pCompartment = iterator.next();
                if (!(pCompartment instanceof IListCompartment))
                {
                    continue;
                }
                
                IListCompartment foundCompartment = (IListCompartment)pCompartment;
                
                // Draw the name compartment(s)
                if (foundCompartment instanceof ETClassNameListCompartment)
                {
                    foundCompartment.calculateOptimumSize(pDrawInfo, false);
                    
                    // draw the sub compartments
                    IETSize nameListSize = foundCompartment.getCurrentSize(transform, false);
                    if (nameListSize != null)
                    {
                        IETRect compartmentDrawRect = new ETRect(deviceRect.getLeft(), transform.yToDevice(this.getLastDrawPointWorldY()), deviceRect.getIntWidth(), nameListSize.getHeight());
                        
                        //                  // Change if you want to demo the Class UI changes.
                        //                  Color bk = getBkColor();
                        //                  java.awt.GradientPaint paint = new java.awt.GradientPaint(compartmentDrawRect.getLeft(),
                        //                                                                            compartmentDrawRect.getTop(),
                        //                                                                            bk,
                        //                                                                            compartmentDrawRect.getRight(),
                        //                                                                            compartmentDrawRect.getBottom(),
                        //                                                                            getLightGradientFillColor());
                        //                  graphics.setPaint(paint);
                        //                  graphics.fill(compartmentDrawRect.getRectangle());
                        //                  graphics.setPaint(null);
                        foundCompartment.draw(pDrawInfo, compartmentDrawRect);
                        
                        // advance to the next line
                        this.updateLastDrawPointWorldY(transform.heightToWorld(nameListSize.getHeight()));
                    }
                    
                    prevFoundComparmtent = foundCompartment;
                }
                else
                {
                    foundCompartment.calculateOptimumSize(pDrawInfo, false);
                    IETSize listSize = foundCompartment.getCurrentSize(transform, false);
                    
                    IETRect compartmentDrawRect = new ETRect(deviceRect.getLeft(), transform.yToDevice(this.getLastDrawPointWorldY()), deviceRect.getIntWidth(), listSize.getHeight());
                    
                    //Redraw the background to handle list resizing issues
                    IETRect tmpRect = new ETDeviceRect(compartmentDrawRect.getRectangle());
                    
                    int clipBottom = Math.min(tmpRect.getBottom(),
                            deviceRect.getBottom());
                    
                    tmpRect.inflate(-1, -1);
                    
                    if (deviceRect.getBottom() <= tmpRect.getBottom())
                    {
                        tmpRect.setBottom(deviceRect.getBottom()-2);
                    }
                    // Comment out if you want to demo the Class UI changes.
                    // GDISupport.drawRectangle(graphics, tmpRect.getRectangle(), getBkColor(), getBkColor());
                    
                    
                    if (!foundCompartment.getCollapsed())
                    {
                        // Draw the divider
                        drawCompartmentDivider(graphics, localBounds, false, prevFoundComparmtent, foundCompartment);
                        
                        // save current clipping area
                        Shape clipBackup = graphics.getClip();
                        
                        
                        graphics.setClip(tmpRect.getLeft(), tmpRect.getTop(),
                                tmpRect.getRight() - tmpRect.getLeft(),
                                clipBottom - tmpRect.getTop());
                        
                        foundCompartment.draw(pDrawInfo, compartmentDrawRect);
                        
                        // restore clipping area;
                        graphics.setClip(clipBackup);
                        
                        
                        // advance to the next line
                        this.updateLastDrawPointWorldY(transform.heightToWorld(listSize.getHeight()));
                    }
                    else
                    {
                        // Draw the divider
                        this.drawCompartmentDivider(graphics, localBounds, true, prevFoundComparmtent, foundCompartment);
                    }
                    
                    prevFoundComparmtent = foundCompartment;
                }
            } //end while
            
            if (prevFoundComparmtent != null )
            {
                if (prevFoundComparmtent.getBoundingRect().getBottom() < deviceRect.getBottom()-2)
                {
                    prevFoundComparmtent.getBoundingRect().setBottom(deviceRect.getBottom()-2);
                }
            }
            
            // Draw template
            if (templateRect != null)
            {
                ICompartment pTemplateParametersCompartment = (ICompartment)getCompartmentByKind(ITemplateParametersCompartment.class);
                if (pTemplateParametersCompartment != null)
                {
                    float templateCenterX = (float)deviceRect.getCenterX();
                    GradientPaint templatePaint = new GradientPaint(templateCenterX,
                            (float)deviceRect.getTop() - (float)templateRect.getWidth(),
                            getBkColor(),
                            templateCenterX,
                            (float)deviceRect.getTop(),
                            getLightGradientFillColor());
                    GDISupport.drawDashedRectangle(graphics.getGraphics(), templateRect.getRectangle(), getBorderBoundsColor(), templatePaint);
                    //GDISupport.drawDashedRectangle(graphics.getGraphics(), templateRect.getRectangle(), Color.BLACK, Color.YELLOW);
                    //templateRect.setTop(templateRect.getTop() - 10);
                    pTemplateParametersCompartment.draw(pDrawInfo, templateRect);
                }
            }
        }
        
        // Uncomment if you want to demo the Class UI changes.
        //drawBorder(pDrawInfo);
        
    }
    
    protected void drawCompartmentDivider(TSEGraphics pGraphics,
            TSConstRect pLocalBounds,
            boolean isCollapsed,
            IListCompartment pPrevCompartment,
            IListCompartment pNextCompartment)
    {
        double x = pLocalBounds.getLeft();
        double w = pLocalBounds.getWidth();
        
        Color prevColor = pGraphics.getColor();
        //pGraphics.setColor(this.getBkColor().darker());
        pGraphics.setColor(getBorderColor());
        
        if (!isCollapsed)
        {
            // Draw the divider
            this.drawWorldRect(pGraphics, x, this.getLastDrawPointWorldY(), w, ETCompartmentDivider.PHYSICAL_HEIGHT);
        }
        else
        {
            // Draw the divider
            this.drawWorldRect(pGraphics, x + 15, this.getLastDrawPointWorldY() + ETCompartmentDivider.COLLAPSED_HEIGHT, w - 30, ETCompartmentDivider.COLLAPSED_HEIGHT);
            this.drawWorldRect(pGraphics, x, this.getLastDrawPointWorldY(), w, ETCompartmentDivider.PHYSICAL_HEIGHT);
        }
        
        pGraphics.setColor(prevColor);
        
        ETCompartmentDivider prevValue = (ETCompartmentDivider) (m_compartmentDividers.get(pNextCompartment.getCompartmentID()));
        
        boolean isDragged = (prevValue != null) ? prevValue.isDragged() : false;
        
        this.m_compartmentDividers.put(
                pNextCompartment.getCompartmentID(),
                new ETCompartmentDivider(x, this.getLastDrawPointWorldY(), w, ETCompartmentDivider.LOGICAL_HEIGHT, pGraphics, pPrevCompartment, pNextCompartment, isDragged));
        
        this.updateLastDrawPointWorldY(pGraphics.getTSTransform().heightToWorld(ETCompartmentDivider.PHYSICAL_HEIGHT));
    }
    
    protected void drawWorldRect(TSEGraphics pGraphics, double x, double y, double w, double h)
    {
/*
                // We need to round down so we can land on a full pixel.
                int roundX = (int)x;
                int roundY = (int)y;
                int roundWidth = (int) (x + w);
                int roundHeight = (int) (y - h);
 
                TSConstRect worldRect = new TSConstRect(roundX, roundY, roundWidth, roundHeight);
        //IETRect deviceRect =
 
      GDISupport.fillRectangle(pGraphics, pGraphics.getTSTransform().boundsToDevice(worldRect));
 */
        pGraphics.fillRect( new TSConstRect(x,y,x + w, y - h));
    }
    
    /**
     * Create the compartments for this node
     */
    public void createCompartments() throws ETException
    {
        clearCompartments();
        this.m_compartmentDividers.clear();
        
        // Check for template
        this.checkTemplate();
        
        ETClassNameListCompartment newClassNameList = new ETClassNameListCompartment(this);
        newClassNameList.setResizeToFitCompartments(true);
        newClassNameList.addCompartment(new ETClassNameCompartment(this), -1, false);
        
        this.addCompartment(newClassNameList);
        
        ETAttributeListCompartment newAttributeListCompartment = new ETAttributeListCompartment(this);
        this.addCompartment(newAttributeListCompartment);
        
        ETOperationListCompartment newOperationListCompartment = new ETOperationListCompartment(this);
        this.addCompartment(newOperationListCompartment);
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
        int numComps = getNumCompartments();
        if (numComps == 0)
        {
            try
            {
                createCompartments();
                numComps = getNumCompartments();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        checkTemplate();
        
        IElement modEle = presEle.getFirstSubject();
        if (modEle != null && numComps > 0)
        {
            // Get the metatype of the element, we use it later to turn off the package import compartment
            // if necessary.
            String elemType = modEle.getElementType();
            
            if (modEle instanceof IClassifier)
            {
                IADClassNameListCompartment pNameCompartment = getCompartmentByKind(IADClassNameListCompartment.class);
                IADAttributeListCompartment pAttributesCompartment = getCompartmentByKind(IADAttributeListCompartment.class);
                IADOperationListCompartment pOperationsCompartment = getCompartmentByKind(IADOperationListCompartment.class);
                INameListCompartment pNameListCompartment = getCompartmentByKind(INameListCompartment.class);
                
                // create a compartment for the classifier's name, it will contain the classifier IElement
                if (pNameCompartment != null)
                {
                    pNameCompartment.attach(modEle);
                    if (elemType != null && elemType.equals("PartFacade"))
                    {
                        // Disable the package import compartment if we have a part facade
                        pNameCompartment.setPackageImportCompartmentEnabled(false);
                    }
                    pNameCompartment.setTemplateParameterCompartmentEnabled(true);
                    pNameCompartment.updateAllOptionalCompartments(modEle);
                }
                
                // Set the text in the static region of the name list
                if (pNameListCompartment != null)
                {
                    setStaticText(pNameListCompartment);
                }
                
                IClassifier pClassifier = (IClassifier)modEle;
                ETList < IAttribute > attrs = pClassifier.getNonRedefiningAttributes();
                ETList < IOperation > opers = pClassifier.getNonRedefiningOperations();
                
                if (attrs != null && pAttributesCompartment != null)
                {
                    pAttributesCompartment.attachElements(new ETArrayList < IElement > ((Collection)attrs), true, false);
                }
                if (opers != null && pOperationsCompartment != null)
                {
                    pOperationsCompartment.attachElements(new ETArrayList < IElement > ((Collection)opers), true, false);
                }
                
                //Call PreAttach() for all redefining list compartments, this ensures that orphaned compartments will be removed
                for (int i = 0; i < numComps; i++)
                {
                    ICompartment pComp = getCompartment(i);
                    if (pComp != null && pComp instanceof IListCompartment)
                    {
                        IListCompartment pListCompartment = (IListCompartment)pComp;
                        if (!pListCompartment.equals(pNameCompartment) && !pListCompartment.equals(pAttributesCompartment) && !pListCompartment.equals(pOperationsCompartment))
                        {
                            pListCompartment.preAttach();
                        }
                    }
                }
                
                // process redefining attributes
                ETList < INamedCollection > pElements = pClassifier.getRedefiningAttributes();
                processRedefines(pElements);
                
                pElements = null;
                pElements = pClassifier.getRedefiningOperations();
                processRedefines(pElements);
                
                // Call PostAttach() to whack orphaned compartments
                Vector listCompartments = new Vector();
                for (int j = numComps; j > 0; j--)
                {
                    ICompartment pComp = getCompartment(j);
                    if (pComp != null && pComp instanceof IListCompartment)
                    {
                        IListCompartment pListCompartment = (IListCompartment)pComp;
                        if (!pListCompartment.equals(pNameCompartment) && !pListCompartment.equals(pAttributesCompartment) && !pListCompartment.equals(pOperationsCompartment))
                        {
                            // whack orphaned compartments
                            pListCompartment.postAttach();
                            
                            // if the compartment is empty whack it as well
                            int count = pListCompartment.getNumCompartments();
                            if (count == 0)
                            {
                                // add the compartment to the list to be deleted
                                //listCompartments.add(pComp);
                                removeCompartment(pComp);
                            }
                        }
                    }
                }
            }
            else
            {
                // not a classifier, something is wrong
            }
        }
    }
    
    /**
     *
     * Creates a redefined compartment for the collection.
     *
     * @param pElements[in] A INamedCollections collection.  Can be NULL.
     *
     * @return HRESULT
     *
     */
    private void processRedefines(ETList < INamedCollection > pElements)
    {
        if (pElements != null)
        {
            // each element in the collection is a collection of redefined elements
            // need to get the type of the elements in the collection, locate their
            // list compartment if it exists and add them to the list.  Create the
            // list compartment if it doesn't exist
            int count = pElements.size();
            for (int i = 0; i < count; i++)
            {
                INamedCollection pCol = pElements.get(i);
                String name = pCol.getName();
                String compartmentTypeName = null;
                ETList < IRedefinableElement > pRedefinedElements = null;
                ETList < IElement > elements = null;
                Object obj = pCol.getData();
                if (obj != null && obj instanceof Collection)
                {
                    Collection col = (Collection)obj;
                    Iterator iter = col.iterator();
                    if (iter.hasNext())
                    {
                        Object containedObj = iter.next();
                        if (containedObj instanceof IAttribute)
                        {
                            ETList < IAttribute > elemCol = new ETArrayList < IAttribute > (col);
                            //sName.Format( IDS_REDEFINED_ATTRIBUTES, W2T( sCollectionName ) );
                            compartmentTypeName = redefined_attribute_compartment_ID; //"ADAttributeListCompartment";
                            elements = (new CollectionTranslator < IAttribute, IElement > ()).copyCollection(elemCol);
                        }
                        else if (containedObj instanceof IOperation)
                        {
                            ETList < IOperation > elemCol = new ETArrayList < IOperation > (col);
                            compartmentTypeName = redefined_operation_compartment_ID; // "ADRedefinedOperationListCompartment";
                            elements = (new CollectionTranslator < IOperation, IElement > ()).copyCollection(elemCol);
                        }
                    }
                }
                
                if (elements != null)
                {
                    pRedefinedElements = (new CollectionTranslator < IElement, IRedefinableElement > ()).copyCollection(elements);
                }
                
                if (pRedefinedElements != null)
                {
                    attachToRedefiningCompartment(pRedefinedElements);
                }
            }
        }
    }
    
    /**
     *
     * Attaches a collection of redefined elements to a compartment
     *
     * @param pRedefinedElements[in] Pointer to a collection that contains the redefined elements.
     *
     * @return
     *
     */
    private void attachToRedefiningCompartment(ETList < IRedefinableElement > pRedefinedElements)
    {
        if (pRedefinedElements != null)
        {
            int count = pRedefinedElements.size();
            
            // Per Cam 06/24/02 we assume all elements in this list come from the same classifier
            for (int i = 0; i < count; i++)
            {
                IRedefinableElement pEle = pRedefinedElements.get(i);
                if (pEle instanceof IFeature)
                {
                    IFeature pFeature = (IFeature)pEle;
                    ETList < IRedefinableElement > pRedefines = pFeature.getRedefinedElements();
                    if (pRedefines != null)
                    {
                        attachToRedefiningCompartment(pFeature, pRedefines);
                    }
                }
            }
        }
    }
    
    /**
     *
     * Attaches a redefined element to a compartment
     *
     * @param pFeature[in] Pointer to a collection that contains the redefined elements.
     * @param pRedefinedElements[in] Pointer to a collection that contains the redefined elements.
     *
     * @return
     *
     */
    private void attachToRedefiningCompartment(IFeature pFeature, ETList < IRedefinableElement > pRedefinedElements)
    {
        if (pRedefinedElements != null && pFeature != null)
        {
            int count = pRedefinedElements.size();
            
            // Per Cam 06/24/02 we assume all elements in this list come from the same classifier
            if (count > 0)
            {
                IRedefinableElement pEle = pRedefinedElements.get(0);
                if (pEle instanceof IFeature)
                {
                    IFeature origFeature = (IFeature)pEle;
                    ETList < IElement > elements = new ETArrayList < IElement > ();
                    elements.add(pFeature);
                    
                    ICompartment pCompartment = getFeaturesRedefiningCompartment(origFeature, elements);
                    if (pCompartment != null)
                    {
                        if (pCompartment instanceof IADListCompartment)
                        {
                            ((IADListCompartment)pCompartment).attachElements(elements, false, false);
                        }
                        resizeToFitCompartment(pCompartment, true, false);
                    }
                }
            }
        }
    }
    
    private ICompartment getFeaturesRedefiningCompartment(IFeature pFeature, ETList < IElement > pElements)
    {
        return getFeaturesRedefiningCompartment(pFeature, pElements, true);
    }
    
    /**
     * Locates the list compartment for the classifier's redefined elements, creates a new one if not found (and asked)
     */
    private ICompartment getFeaturesRedefiningCompartment(IFeature pFeature, ETList < IElement > pElements, boolean bCreateIfNotFound)
    {
        ICompartment foundComp = null;
        if (pFeature != null && pElements != null)
        {
            IClassifier pClassifier = pFeature.getFeaturingClassifier();
            if (pClassifier != null)
            {
                String compartmentTypeName = null;
                String xmiid = pClassifier.getXMIID();
                
                if (pFeature instanceof IAttribute)
                {
                    compartmentTypeName = redefined_attribute_compartment_ID; //"ADAttributeListCompartment";
                }
                else if (pFeature instanceof IOperation)
                {
                    compartmentTypeName = redefined_operation_compartment_ID; // "ADRedefinedOperationListCompartment";
                }
                
                // check if this compartment already exists 79838
                foundComp = findCompartmentByXMIID(xmiid+compartmentTypeName);
                //                if (foundComp == null)
                //                {
                //                    foundComp = findCompartmentByTitle(strTitleAlias);
                //                }
                
                // if we didn't find the compartment above, look for one that contains one of the elements in our list
                if (foundComp == null)
                {
                    int count = pElements.size();
                    for (int i = 0; i < count; i++)
                    {
                        IElement pEle = pElements.get(i);
                        
                        // searchs all list compartments for on containing this element
                        IListCompartment pListComp = findListCompartmentContainingElement(pEle);
                        if (pListComp != null)
                        {
                            foundComp = pListComp;
                            break;
                        }
                    }
                }
                
                // if we didn't find the compartment above, create a new one
                if (foundComp == null && bCreateIfNotFound)
                {
                    foundComp = createAndAddCompartment(compartmentTypeName);
                    if (foundComp != null && foundComp instanceof IADListCompartment)
                    {
                        foundComp.setModelElementXMIID(xmiid);
                        ((IADListCompartment)foundComp).setDeleteIfEmpty(true);
                    }
                }   
            }
        }
        return foundComp;
    }
    
    
    public ICompartment findCompartmentByXMIID(String val)
    {
        
        ICompartment retValue = null;
        
        Iterator<ICompartment> iterator = getCompartments().iterator();
        
        while (iterator.hasNext())
        {
            
            ICompartment curObject = iterator.next();
            ICompartment compartment = curObject;
            
            if (compartment != null && 
               (compartment.getModelElementXMIID() + compartment.getCompartmentID()).equals(val))
            {
                retValue = curObject;
                break;
            }
        }
        
        return retValue;
    }
    
    
    /**
     * Sets the static text compartment in the name list if necessary
     *
     * @param pCompartment [in] Our name list comparment, just so we don't have to reget it.
     */
    private void setStaticText(INameListCompartment pCompartment)
    {
        ETGenericNodeUI parentUI = (ETGenericNodeUI)this.getParent();
        IElement pElement = parentUI.getModelElement();
        if (pElement instanceof IPartFacade)
        {
            IPartFacade pPartFacade = (IPartFacade)pElement;
            if (pPartFacade instanceof IParameterableElement)
            {
                IParameterableElement pParameterableElement = (IParameterableElement)pPartFacade;
                String sTypeConstraint = pParameterableElement.getTypeConstraint();
                
                String sStaticText = "<<role>>";
                if (sTypeConstraint != null && sTypeConstraint.equals("Interface"))
                {
                    sStaticText = "<<interface,role>>";
                }
                
                pCompartment.addStaticText(sStaticText);
            }
        }
    }
    
    public void onContextMenu(IMenuManager manager)
    {
        super.onContextMenu(manager);
        addCustomizeMenuItems(manager);
    }
    
    public String getDrawEngineID()
    {
        return "ClassDrawEngine";
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.drawengines.ETDrawEngine#getManagerMetaType(int)
    */
    public String getManagerMetaType(int nManagerKind)
    {
        String sManager = null;
        
        if (nManagerKind == MK_EVENTMANAGER)
        {
            IElement modelElement = getFirstModelElement();
            // order is important here, IAssociationClass is an instance of IClass (Kevin)
            if (modelElement instanceof IAssociationClass)
            {
                sManager = "AssociationClassEventManager";
            }
            else if (modelElement instanceof IClass)
            {
                sManager = "ADClassifierEventManager";
            }
        }
        
        return sManager;
    }
    
    protected boolean checkTemplate(/*IElement element*/)
    {
        if (!hasTemplateParameters())
        {
            removeCompartment((ICompartment)getCompartmentByKind(ITemplateParametersCompartment.class));
            
            return false;
        }
        ICompartment compartment =
                (ICompartment)getCompartmentByKind(ITemplateParametersCompartment.class);
        if (compartment!=null)
            return false;
        
        // check for newly added template parameters and create compartment to display them 78838
        ETGenericNodeUI parentUI = (ETGenericNodeUI)this.getParent();
        IElement element = parentUI != null ? parentUI.getModelElement() : null;
        
        TemplateParametersCompartment pTemplateParametersCompartment = new TemplateParametersCompartment();
        ICompartment pCompartment = (ICompartment)pTemplateParametersCompartment;
        pCompartment.setEngine(this);
        pCompartment.addModelElement(element, -1);
        this.addCompartment(pCompartment);
        
        return true;
        
    }
    
    private boolean hasTemplateParameters()
    {
        ETGenericNodeUI parentUI = (ETGenericNodeUI)this.getParent();
        IElement element = parentUI != null ? parentUI.getModelElement() : null;
        
        if (element instanceof IClassifier)
        {
            IClassifier pClassifier = (IClassifier)element;
            ETList < IParameterableElement > pParameters = pClassifier.getTemplateParameters();
            
            pParameters = pClassifier.getTemplateParameters();
            return (pParameters != null && pParameters.size() > 0);
        }
        return false;
        
    }
    
   /*
    * Returns scaled device UNITS of the ITemplateParametersCompartment optimal size or null if no ITemplateParametersCompartment exists.
    */
    protected IETRect checkTemplateDrawRect(IDrawInfo pDrawInfo)
    {
        IETRect templateRect;
        ICompartment pTemplateParametersCompartment = (ICompartment)getCompartmentByKind(ITemplateParametersCompartment.class);
        if (pTemplateParametersCompartment != null)
        {
            IETRect deviceRect = pDrawInfo.getDeviceBounds();
            
            int x = deviceRect.getLeft();
            int w = deviceRect.getIntWidth();
            int y = deviceRect.getTop();
            int h = deviceRect.getIntHeight();
            
            IETSize templateSize = pTemplateParametersCompartment.calculateOptimumSize(pDrawInfo, false);
            templateRect = new ETRect(x + 10, y, w - 10, templateSize.getHeight());
            // We need to return world un
        }
        else
        {
            templateRect = null;
        }
        
        return templateRect;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#onGraphEvent(int)
    */
    public void onGraphEvent(int nKind)
    {
        super.onGraphEvent(nKind);
        
        switch (nKind)
        {
        case IGraphEventKind.GEK_PRE_MOVE :
            this.handlePreMove();
            break;
            
        case IGraphEventKind.GEK_POST_MOVE :
            this.handlePostMove();
            break;
        }
    }
    
    private void handlePreMove()
    {
        selectLollipops(true);
    }
    
    private void handlePostMove()
    {
        selectLollipops(false);
    }
    
    private void selectLollipops(boolean select)
    {
        TSENode myNode = getNode();
        List edges = myNode.buildInOutEdges(TSNode.CONNECTED_EDGES);
        
        Iterator i = edges.iterator();
        while (i.hasNext())
        {
            TSEdge edge = (TSEdge)i.next();
            INodePresentation interfacePE = getLollipopNode(edge);
            if (interfacePE != null)
            {
                interfacePE.setSelected(select);
            }
        }
    }
    
    private INodePresentation getLollipopNode(TSEdge edge)
    {
        IDrawEngine edgeDE = TypeConversions.getDrawEngine(edge);
        
        if (edgeDE == null || !(edgeDE instanceof ETImplementationEdgeDrawEngine))
        {
            return null;
        }
        
        ETImplementationEdgeDrawEngine implDE = (ETImplementationEdgeDrawEngine)edgeDE;
        
        if (!implDE.isLollipop())
        {
            return null;
        }
        
        TSNode interfaceNode = edge.getTargetNode();
        return (INodePresentation)TypeConversions.getPresentationElement(interfaceNode);
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#modelElementHasChanged(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
    */
    public long modelElementHasChanged(INotificationTargets pTargets)
    {
        if (pTargets == null)
            return 0;
        
        int nKind = pTargets.getKind();
        
        IElement modelElement = pTargets.getChangedModelElement();
        IElement secondaryChangedME = pTargets.getSecondaryChangedModelElement();
        IFeature feature = secondaryChangedME instanceof IFeature ? (IFeature)secondaryChangedME : null;
        
        boolean isTaggedValue = modelElement instanceof ITaggedValue;
        ITaggedValue taggedValue = isTaggedValue ? (ITaggedValue)modelElement : null;
        
        if (isTaggedValue && feature == null)
        {
            IElement owner = taggedValue.getOwner();
            
            if (owner instanceof IFeature)
            {
                feature = (IFeature)owner;
                pTargets.setSecondaryChangedModelElement(feature);
            }
        }
        
        boolean addedOrRemovedCompartment = false;
        
        if ((nKind == ModelElementChangedKind.MECK_ELEMENTMODIFIED
                || nKind == ModelElementChangedKind.MECK_STEREOTYPEDELETED
                || nKind == ModelElementChangedKind.MECK_STEREOTYPEAPPLIED
                || nKind == ModelElementChangedKind.MECK_ELEMENTADDEDTONAMESPACE)
                || isTaggedValue)
        {
            
            IADClassNameListCompartment nameCompartment = getCompartmentByKind(IADClassNameListCompartment.class);
            
            if (nameCompartment != null)
            {
                addedOrRemovedCompartment = nameCompartment.updateAllOptionalCompartments(null);
                if (addedOrRemovedCompartment || checkTemplate())
                    setIsDirty();
            }
            
            // Fixed iz=78803. Update the operation compartment when there's 
            // any change to the Operation element, e.g. parameter deleted, modified...)
            String featureType = feature != null ? feature.getElementType() : "";
            
            if ( nKind == ModelElementChangedKind.MECK_ELEMENTMODIFIED  &&
                    featureType.equals("Operation") )
            {    
                IADOperationListCompartment operationsCompartment = 
                        getCompartmentByKind(IADOperationListCompartment.class);
                if ( operationsCompartment != null)
                {
                    operationsCompartment.modelElementHasChanged(pTargets);
                }
            }
        }
        
        if ((nKind != ModelElementChangedKind.MECK_ELEMENTMODIFIED || isTaggedValue) && !addedOrRemovedCompartment)
        {
            ETList < IRedefinableElement > redefines = null;
            
            if (feature != null)
            {
                redefines = feature.getRedefinedElements();
            }
            
            IADClassNameListCompartment nameCompartment = getCompartmentByKind(IADClassNameListCompartment.class);
            IADAttributeListCompartment attributesCompartment = getCompartmentByKind(IADAttributeListCompartment.class);
            IADOperationListCompartment operationsCompartment = getCompartmentByKind(IADOperationListCompartment.class);
            
            String featureType = feature != null ? feature.getElementType() : "";
            
            if (nKind == ModelElementChangedKind.MECK_REDEFININGELEMENTREMOVED)
            {
                removeFromRedefiningCompartment(feature);
            }
            
            if (feature == null && nameCompartment != null)
            {
                nameCompartment.modelElementHasChanged(pTargets);
            }
            else if ((redefines != null && redefines.size() > 0) && feature != null)
            {
                attachToRedefiningCompartment(feature, redefines);
            }
            else if (featureType.equals("Attribute") && attributesCompartment != null)
            {
                attributesCompartment.modelElementHasChanged(pTargets);
            }
            else if (featureType.equals("Operation") && operationsCompartment != null)
            {
                operationsCompartment.modelElementHasChanged(pTargets);
            }
            else
            {
                if (modelElement instanceof IClassifier)
                {
                    long numCompartments = getNumCompartments();
                    
                    for (int i = 0; i < numCompartments; ++i)
                    {
                        ICompartment compartment = getCompartment(i);
                        if (compartment != null)
                        {
                            compartment.modelElementHasChanged(pTargets);
                        }
                    }
                }
            }
        }
        
        postInvalidate();
        return 0;
    }
    
   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#modelElementDeleted(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
    */
    public long modelElementDeleted(INotificationTargets pTargets)
    {
        
        IElement pModelElement = pTargets.getChangedModelElement();
        IElement pSecondaryChangedME = pTargets.getSecondaryChangedModelElement();
        IFeature pFeature = pSecondaryChangedME instanceof IFeature ? (IFeature)pSecondaryChangedME : null;
        
        IElement theElement = pModelElement;
        IFeature theFeature = pFeature;
        
        if (pModelElement != null)
        {
            int numCompartments = getNumCompartments();
            
            for (int i = 0; i < numCompartments; i++)
            {
                ICompartment pCompartment = getCompartment(i);
                
                if (pCompartment != null)
                {
                    pCompartment.modelElementDeleted(pTargets);
                    
                    // if compartment is a list compartment and if empty and if DeleteWhenEmpty whack it
                    IListCompartment pListCompartment = null;
                    
                    if (pCompartment instanceof IListCompartment)
                    {
                        
                        pListCompartment = (IListCompartment)pCompartment;
                    }
                    
                    if (pListCompartment != null)
                    {
                        int nCount = pListCompartment.getNumCompartments();
                        if (nCount == 0)
                        {
                            boolean bDelete = false;
                            bDelete = pListCompartment.getDeleteIfEmpty();
                            if (bDelete == true)
                            {
                                removeCompartment(pCompartment);
                                sizeToContents();
                            }
                        }
                    }
                }
            }
        }
        return 0;
    }
    
    protected void removeFromRedefiningCompartment(IFeature feature)
    {
        if (feature == null)
            return;
        
        IListCompartment listCompartment = findListCompartmentContainingElement(feature);
        if (listCompartment == null)
            return;
        
        ICompartment compartment = listCompartment.findCompartmentContainingElement(feature);
        if (compartment != null)
            listCompartment.removeCompartment(compartment, false);
        
        long nCount = listCompartment.getNumCompartments();
        if (nCount == 0)
        {
            IADClassNameListCompartment nameCompartment = getCompartmentByKind(IADClassNameListCompartment.class);
            IADAttributeListCompartment attributeCompartment = getCompartmentByKind(IADAttributeListCompartment.class);
            IADOperationListCompartment operationCompartment = getCompartmentByKind(IADOperationListCompartment.class);
            
            if (listCompartment != nameCompartment && listCompartment != attributeCompartment && listCompartment != operationCompartment)
            {
                removeCompartment(listCompartment);
            }
        }
    }
    
   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#calculateOptimumSize(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, boolean)
    */
    public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
    {
        IETSize retVal = new ETSize(0, 0);
        
        IETSize tempSize = super.calculateOptimumSize(pDrawInfo, true);
        
        // allow 1 pixel all around for border thickness
        // NOTE: when border thickness becomes a preference we'll have to multiply it by 2
        if (tempSize != null)
        {
            retVal.setWidth(Math.max(tempSize.getWidth(), NODE_WIDTH) + 4);
            retVal.setHeight(Math.max(tempSize.getHeight(), NODE_HEIGHT) + 4);
        }
        
        if (m_compartmentDividers != null)
        {
            retVal.setHeight(retVal.getHeight() + (m_compartmentDividers.size() * ETCompartmentDivider.LOGICAL_HEIGHT));
        }
        
        TSTransform transform = pDrawInfo != null ? pDrawInfo.getTSTransform() : getTransform();
        return bAt100Pct || retVal == null ? retVal : scaleSize(retVal, transform);
    }
    
   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#handleAccelerator(java.lang.String)
    */
    public boolean handleAccelerator(String accelerator)
    {
        boolean bHandled = false;
        
        boolean bIsCreateAttributeAccelerator = false;
        boolean bIsCreateOperationAccelerator = false;
        boolean bIsCreateParameterAccelerator = false;
        
        IDrawingAreaControl pControl = getDrawingArea();
        if (pControl != null)
        {
            IDiagramEngine pEngine = pControl.getDiagramEngine();
            if (pEngine instanceof IADCoreEngine)
            {
                IADCoreEngine pADDiagramEngine = (IADCoreEngine)pEngine;
                bIsCreateAttributeAccelerator = pADDiagramEngine.getIsCreateAttributeAccelerator(accelerator);
                bIsCreateOperationAccelerator = pADDiagramEngine.getIsCreateOperationAccelerator(accelerator);
                bIsCreateParameterAccelerator = pADDiagramEngine.getIsCreateParameterAccelerator(accelerator);
            }
        }
        
        if (bIsCreateAttributeAccelerator)
        {
            IADAttributeListCompartment pCompartment = getCompartmentByKind(IADAttributeListCompartment.class);
            if (pCompartment != null)
            {
                pCompartment.addCompartment(null, -1, true);
                bHandled = true;
            }
        }
        else if (bIsCreateOperationAccelerator)
        {
            IADOperationListCompartment pCompartment = getCompartmentByKind(IADOperationListCompartment.class);
            if (pCompartment != null)
            {
                pCompartment.addCompartment(null, -1, true);
                bHandled = true;
            }
        }
        else if (bIsCreateParameterAccelerator)
        {
            IADOperationListCompartment pCompartment = getCompartmentByKind(IADOperationListCompartment.class);
            if (pCompartment != null)
            {
                bHandled = pCompartment.insertParameterAtSelectedOperation();
            }
        }
        
        return bHandled;
    }
    
    public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pClass)
    {
        boolean bEnabled = false;
        
        if (id.equals("MBK_CUSTOMIZE"))
        {
            IDrawingAreaControl control = getDrawingArea();
            if (control != null)
            {
                bEnabled = ! control.getReadOnly();
            }
        }
        else
        {
            bEnabled = super.setSensitivityAndCheck(id, pClass);
        }
        
        return bEnabled;
    }
    
    public boolean onHandleButton(ActionEvent e, String id)
    {
        boolean handled = handleStandardLabelSelection(e, id);
        if (!handled)
        {
            if (id.equals("MBK_CUSTOMIZE"))
            {
                CustomizeDlg dlg = new CustomizeDlg(this);
                handled = true;
            }
        }
        
        if (!handled)
        {
            handled = super.onHandleButton(e, id);
        }
        return handled;
    }
    
   /*
    *
    */
    public ETCompartmentDivider getCompartmentDividerAtPoint(IETPoint pCurrentPos)
    {
        ETCompartmentDivider retValue = null;
        try
        {
            
            // must follow the order of the actual compartments in order to handle multiple collapsed lists
            Iterator < ICompartment > iter = this.getCompartments().iterator();
            while (iter.hasNext())
            {
                ICompartment curCompartment = iter.next();
                if (curCompartment instanceof IListCompartment)
                {
                    ETCompartmentDivider compartmentDivider = (ETCompartmentDivider)m_compartmentDividers.get(curCompartment.getCompartmentID());
                    if (compartmentDivider != null && compartmentDivider.containsDevicePoint(pCurrentPos.getX(), pCurrentPos.getY()))
                    {
                        retValue = compartmentDivider;
                        break;
                    }
                }
            }
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return retValue;
    }
    
    public boolean handleLeftMouseBeginDrag(IETPoint pStartPos, IETPoint pCurrentPos)
    {
        boolean eventHandled = false;
        
        Iterator < ETCompartmentDivider > iterator1 = m_compartmentDividers.values().iterator();
        while (iterator1.hasNext())
        {
            iterator1.next().setDragged(false);
        }
        
        Iterator < ETCompartmentDivider > iterator2 = m_compartmentDividers.values().iterator();
        while (iterator2.hasNext() && !eventHandled)
        {
            eventHandled = iterator2.next().handleLeftMouseBeginDrag(pStartPos, pCurrentPos, false);
        }
        
        if (!eventHandled)
        {
            eventHandled = super.handleLeftMouseBeginDrag(pStartPos, pCurrentPos);
        }
        
        return eventHandled;
    }
    
    public boolean handleLeftMouseButtonDoubleClick(MouseEvent pEvent)
    {
        boolean eventHandled = false;
        Point mousePos = pEvent.getPoint();
        
        ETCompartmentDivider compartmentDivider = getCompartmentDividerAtPoint(new ETPoint(mousePos));
        if (compartmentDivider != null)
        {
            eventHandled = compartmentDivider.handleLeftMouseButtonDoubleClick(pEvent);
        }
        
        if (!eventHandled)
        {
            eventHandled = super.handleLeftMouseButtonDoubleClick(pEvent);
        }
        
        return eventHandled;
    }
    
   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#handleLeftMouseDrag(org.netbeans.modules.uml.core.support.umlsupport.IETPoint, org.netbeans.modules.uml.core.support.umlsupport.IETPoint)
    */
    public boolean handleLeftMouseDrag(IETPoint pStartPos, IETPoint pCurrentPos)
    {
        boolean eventHandled = false;
        
        if (m_draggedDivider != null)
        {
            eventHandled = m_draggedDivider.handleLeftMouseDrag(pStartPos, pCurrentPos);
        }
        else
        {
            Iterator iter = m_compartmentDividers.values().iterator();
            while (iter.hasNext() && !eventHandled)
            {
                ETCompartmentDivider divider = (ETCompartmentDivider)iter.next();
                if (divider.isDragged())
                {
                    this.m_draggedDivider = divider;
                    eventHandled = divider.handleLeftMouseDrag(pStartPos, pCurrentPos);
                    break;
                }
            }
        }
        
        if (!eventHandled)
        {
            eventHandled = super.handleLeftMouseDrag(pStartPos, pCurrentPos);
        }
        
        return eventHandled;
    }
    
   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#handleLeftMouseDrop(org.netbeans.modules.uml.core.support.umlsupport.IETPoint, java.util.List, boolean)
    */
    public boolean handleLeftMouseDrop(IETPoint pCurrentPos, List pElements, boolean bMoving)
    {
        boolean eventHandled = false;
        
        if (this.m_draggedDivider != null)
        {
            //         this.m_compartmentDividers.clear();
            this.m_draggedDivider = null;
            eventHandled = true;
        }
        
        if (!eventHandled)
        {
            
            eventHandled = super.handleLeftMouseDrop(pCurrentPos, pElements, bMoving);
        }
        return eventHandled;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#handleSetCursor(org.netbeans.modules.uml.ui.support.viewfactorysupport.ISetCursorEvent)
    */
    public boolean handleSetCursor(ISetCursorEvent event)
    {
        boolean bEventHandled = false;
        
        IETPoint currentPos = PointConversions.newETPoint(event.getWinClientLocation());
        if (getCompartmentDividerAtPoint(currentPos) != null)
        {
            event.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
            
            bEventHandled = true;
        }
        else
        {
            bEventHandled = super.handleSetCursor(event);
        }
        
        return bEventHandled;
    }
        /*
         * Loads a string from default resource bundle.
         */
    public String loadString(String key)
    {
        try
        {
            return RESOURCE_BUNDLE.getString(key);
        }
        catch (MissingResourceException e)
        {
            return '!' + key + '!';
        }
    }
    
    
    /**
     * @return Tool tip text "Double click to expand" if mouse is over
     *         top divider of collapsed comparement
     *         and <code>null</code> otherwise;
     *
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow#getToolTipText
     */
    
    public String getToolTipText(MouseEvent event)
    {
        Point position = event.getPoint();
        
        boolean mouseOverDivider = false;
        
        Iterator<ICompartment> iterator = this.getCompartments().iterator();
        
        while (iterator.hasNext())
        {
            ICompartment compartment = iterator.next();
            if (!(compartment instanceof IListCompartment))
            { continue; }
            
            IListCompartment curCompartment = (IListCompartment) compartment;
            
            if (curCompartment.getCollapsed())
            {
                ETCompartmentDivider divider
                        = (ETCompartmentDivider) m_compartmentDividers
                        .get(curCompartment.getCompartmentID());
                
                if ((divider != null)
                        && divider.containsDevicePoint(position.x, position.y))
                {
                    mouseOverDivider = true;
                    break;
                }
            }
        }
        
        if (mouseOverDivider)
        {
            return loadString(IDS_EXPAND_TOOL_TIP_TEXT);
        }
        
        return null;
    }
}
