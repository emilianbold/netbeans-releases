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
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.Iterator;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlsupport.ETDeviceRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETClassNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETClassNameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADClassNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.INameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IPackageImportCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IStereotypeCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ITaggedValuesCompartment;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IListCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject;
import com.tomsawyer.editor.TSEColor;
import com.tomsawyer.editor.TSEFont;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.graphics.TSEGraphics;
import com.tomsawyer.drawing.geometry.TSConstRect;
import com.tomsawyer.editor.TSTransform;

/**
 * @author Embarcadero Technologies Inc.
 *
 * 
 */
public class ETPackageDrawEngine extends ETContainerDrawEngine implements IPackageDrawEngine
{

   protected final int MIN_NODE_WIDTH = 140;
   protected final int MIN_NODE_HEIGHT = 80;

   protected final int MAX_TAB_HEIGHT = 30;
   protected final int MAX_TAB_WIDTH = 150;
   protected final int INNER_TO_OUTER_RECT_DISTANCE = 6;
   protected final float BIG_TAB_MULTIPLIER = 1.5f;

   public static String PACKAGECOMPARTMENT_TABLOCATION = "TabLocation";

   /// The last place we drew the tab
   IETRect m_TabRect = null;

   /// The last place we drew the body of the package
   IETRect m_BodyRect = null;

   // Should we display in the tab or in the center of the node
   boolean m_bDisplayInTab = false;

   // This bool is set if our shape has changed and we need to call SetNodeShape
   boolean m_bShapeChanged = false;

	public ETPackageDrawEngine()
	{
		super();
		this.setContainmentType(ContainmentTypeEnum.CT_NAMESPACE);
	}
	
   public void init() throws ETException
   {
      super.init();
      this.setContainmentType(ContainmentTypeEnum.CT_NAMESPACE);
   }

   public String getDrawEngineID()
   {
      return "PackageDrawEngine";
   }

   public String getElementType()
   {
      return new String("Package");
   }

   public void initResources()
   {
      setFillColor("packagefill", 105, 191, 105);
      setLightGradientFillColor("packagelightgradientfill", 188, 223, 198);
      setBorderColor("packageborder", Color.BLACK);

      super.initResources();
   }

   /**
    * Here's where the node drawing actually happens.  Drawing is done in the coordinate system of the
    * current zoom, so do the calculations and multiply by the zoom factory and then draw.
    *
    * @param pInfo [in] This interface contains information pertaining to the current drawing operation.
    */
   public void doDraw(IDrawInfo pInfo)
   {
      // Get the number of compartments, if 0 then create
      long numCompartments = getNumCompartments();

      if (numCompartments == 0)
      {
         // UPGRADE : In 6.03 there was a PackageCompartment, with 
         // the first enterprise release we change the compartment to
         // a normal ADNameListCompartment.  So if we fail to create
         // the PackageCompartment we'll get here during the upgrade and
         // will need to re-create our normal compartments.
         IPresentationElement pPresentationElement = this.getPresentationElement();

         createCompartments();
         initCompartments(pPresentationElement);
      }

      boolean bDrawnMoving = false;

      // drawMoving(pInfo, bDrawnMoving);

      if (!bDrawnMoving)
      {

         if (pInfo != null)
         {
            IETRect boundingRect = pInfo.getDeviceBounds();

            // draw our frame
            drawPackageFrame(pInfo, boundingRect);
         }

         // This will draw an invalid frame around the node if it doesn't have an IElement
         //				_VH( DrawInvalidRectangle( pInfo, 0 ));

         // Put the selection handles
         //				CGDISupport::DrawSelectionHandles(pInfo);

         // Set the node shape since we're not rectangular
         if (m_bShapeChanged)
         {
            // Note to TS : I have two nodes and, when the zoom changes, I set their node 
            // shape.  Turns out that setting their node shape reroutes the edges to 
            // center-on-center.
            // 
            // This was commented out to avoid the problem above until TS can tell me
            // what the deal is.
            //_VH(SetNodeShape(pInfo));
            m_bShapeChanged = false;
         }
      }
      super.doDraw(pInfo);
   }

   /**
    * Draws the package frame
    *
    * @param pInfo [in] This interface contains information pertaining to the current drawing operation.
    * @param boundingRect [in] The calculated bounding rect to draw to
    */

   private void drawPackageFrame(IDrawInfo pInfo, IETRect boundingRect)
   {
      if (pInfo == null)
      {
         return;
      }

      TSEGraphics graphics = pInfo.getTSEGraphics();

      INameListCompartment pNameListCompartment = null;
      IStereotypeCompartment pStereotypeCompartment = null;
      ITaggedValuesCompartment pTaggedValuesCompartment = null;
      IPackageImportCompartment pPackageImportCompartment = null;
      IADClassNameCompartment pNameCompartment = null;

      IETSize nameCompartmentSize = new ETSize(0, 0);
      IETSize stereotypeCompartmentSize = new ETSize(0, 0);
      IETSize taggedValuesCompartmentSize = new ETSize(0, 0);
      IETSize packageImportCompartmentSize = new ETSize(0, 0);

      Iterator < ICompartment > iterator = this.getCompartments().iterator();

      while (iterator.hasNext())
      {
         ICompartment foundCompartment = iterator.next();

         if (foundCompartment instanceof INameListCompartment)
         {
            pNameListCompartment = (INameListCompartment)foundCompartment;
            IETSize nameListSize = pNameListCompartment.calculateOptimumSize(pInfo, false);

            Iterator < ICompartment > compartmentIterator = pNameListCompartment.getCompartments().iterator();

            while (compartmentIterator.hasNext())
            {
               ICompartment nameCompartment = compartmentIterator.next();

               if (nameCompartment instanceof IStereotypeCompartment)
               {
                  pStereotypeCompartment = (IStereotypeCompartment)nameCompartment;
                  stereotypeCompartmentSize = pStereotypeCompartment.calculateOptimumSize(pInfo, false);
               }
               else if (nameCompartment instanceof ITaggedValuesCompartment)
               {
                  pTaggedValuesCompartment = (ITaggedValuesCompartment)nameCompartment;
                  taggedValuesCompartmentSize = pTaggedValuesCompartment.calculateOptimumSize(pInfo, false);

               }
               else if (nameCompartment instanceof IPackageImportCompartment)
               {
                  pPackageImportCompartment = (IPackageImportCompartment)nameCompartment;
                  packageImportCompartmentSize = pPackageImportCompartment.calculateOptimumSize(pInfo, false);

               }
               else if (nameCompartment instanceof IADClassNameCompartment)
               {
                  pNameCompartment = (IADClassNameCompartment)nameCompartment;
                  nameCompartmentSize = pNameCompartment.calculateOptimumSize(pInfo, false);

               }
            }

         }
      }

      if (pNameListCompartment != null && pNameCompartment != null)
      {
         // Make sure the text in the name compartment draws in the center
         //////////////////////////////////////////////////////////////////////
         //
         // Package ...
         //
         //    ------
         //    |    |
         //    ------------------------------
         //    |                            |
         //    |----------------------------|
         //    ||                          ||
         //    ||                          ||
         //    ||                          ||
         //    ||        <un-named>        ||
         //    ||                          ||
         //    ||                          ||
         //    ||                          ||
         //    |----------------------------|
         //    ------------------------------
         //
         //////////////////////////////////////////////////////////////////////

         IETGraphObjectUI pView = this.getParent();
         ITSGraphObject graphObj = pView.getTSObject();

         // Determine where the name should be displayed
         boolean bDisplayInTab = (m_bDisplayInTab != false);

         if (!bDisplayInTab)
         {
            // Force the name to be in the tab of the package, when there are contained elements
            m_bDisplayInTab = bDisplayInTab = this.hasContained();
         }

         if (!bDisplayInTab)
         {
            // Force the name to be in the tab of the package, when we are expanded
            bDisplayInTab = ((graphObj != null) && (graphObj instanceof TSENode) && (((TSENode)graphObj).isExpanded()));
         }

         double dZoom = pInfo.getOnDrawZoom();

         // save our last bounding rect for future reference
         IETRect rect = new ETDeviceRect(boundingRect.getRectangle());

         // perform color overrides
         Color crBorderColor = this.getBorderColor();
         Color crBackgroundColor = new Color(255, 255, 255); // color around the center area is always white
         Color crInteriorColor = this.getFillColor();

         // This segment draws the rectangles making up the package node
         if (bDisplayInTab)
         {
            // Set the width of the compartment
            if ((nameCompartmentSize.getWidth() + (INNER_TO_OUTER_RECT_DISTANCE * dZoom)) < boundingRect.getWidth())
            {
               rect.setRight(rect.getLeft() + nameCompartmentSize.getWidth() + (int) ((float)INNER_TO_OUTER_RECT_DISTANCE * dZoom));
            }
            else
            {
               // Clip the name
               rect.setRight(rect.getLeft() + (int) ((float)boundingRect.getWidth() / (BIG_TAB_MULTIPLIER)));
            }
            // Set the height of the compartment
            if ((nameCompartmentSize.getHeight() + (INNER_TO_OUTER_RECT_DISTANCE * dZoom)) < boundingRect.getHeight())
            {
               rect.setBottom(rect.getTop() + nameCompartmentSize.getHeight() + (int) ((float)INNER_TO_OUTER_RECT_DISTANCE * dZoom));
            }
            else
            {
               // Clip the name
               rect.setBottom(rect.getTop() + (int) ((float)boundingRect.getHeight() / (BIG_TAB_MULTIPLIER)));
            }

            // draw tab rectangle 
            GDISupport.drawRectangle(graphics, rect.getRectangle(), crBorderColor, crBackgroundColor);

            setTabRect(new ETDeviceRect(rect.getRectangle()));
         }
         else
         {
            // Make the tab smaller if the name is in the main compartment area
            rect.setRight(rect.getLeft() + (int) (boundingRect.getWidth() / 4));
            rect.setBottom(1 + rect.getTop() + (int) (rect.getHeight() / 5));

            // Don't make the tab too large in height
            if (rect.getHeight() > (MAX_TAB_HEIGHT * dZoom))
            {
               rect.setBottom(rect.getTop() + (int) ((float)MAX_TAB_HEIGHT * dZoom));
            }
            // Don't make the tab too large in width
            if (rect.getWidth() > (MAX_TAB_WIDTH * dZoom))
            {
               rect.setRight(rect.getLeft() + (int) ((float)MAX_TAB_WIDTH * dZoom));
            }

            // draw tab rectangle 
            GDISupport.drawRectangle(graphics, rect.getRectangle(), crBorderColor, crBackgroundColor);

            setTabRect(new ETDeviceRect(rect.getRectangle()));
         }

         rect.setTop(rect.getBottom() - (int) ((float)1 * dZoom));
         rect.setBottom(boundingRect.getBottom());
         rect.setRight(boundingRect.getRight());         
         
         // draw outer rectangle
         GDISupport.drawRectangle(graphics, rect.getRectangle(), crBorderColor, crBackgroundColor);
         setBodyRect(new ETDeviceRect(rect.getRectangle()));

         boolean bDrewInnerRect = false;

         if (rect.getWidth() > (12 * dZoom) && rect.getHeight() > (12 * dZoom))
         {
            int n = (int) ((float)INNER_TO_OUTER_RECT_DISTANCE * dZoom);

            rect.inflate(-n, -n);

            // draw inner rectangle
            float centerX = (float)rect.getCenterX();
            GradientPaint paint = new GradientPaint(centerX,
                             rect.getBottom(),
                             crInteriorColor,
                             centerX,
                             rect.getTop(),
                             getLightGradientFillColor());
        
            GDISupport.drawRectangle(graphics, rect.getRectangle(), crBorderColor, paint);

            rect.inflate(-1, 0);
            bDrewInnerRect = true;
         }

         if (pNameCompartment != null)
         {
            clearVisibleCompartments();
            addVisibleCompartment(pNameListCompartment);
            addVisibleCompartment(pNameCompartment);

            // draw name in center of this area or in the tab
            IETRect pETRect = null;

            if (bDisplayInTab)
            {
               pETRect = new ETDeviceRect(m_TabRect.getRectangle());

               // Draw the compartment
               pNameCompartment.draw(pInfo, pETRect);

               // Draw the stereotype compartment in the top part of the package body
               if (pStereotypeCompartment != null && bDrewInnerRect)
               {
                  IETRect stereotypeRect = new ETDeviceRect(rect.getRectangle());

                  stereotypeRect.setBottom(Math.min((rect.getTop() + stereotypeCompartmentSize.getHeight()), rect.getCenterPoint().y));

                  // Draw the stereotype compartment
                  pETRect = new ETDeviceRect(stereotypeRect.getRectangle());

                  pStereotypeCompartment.draw(pInfo, pETRect);
               }

               // Draw the import and tagged values compartments in the bottom part of the package body.
               ETSize taggedValueAndPkgImportSize =
                  new ETSize(taggedValuesCompartmentSize.getWidth() + packageImportCompartmentSize.getWidth(), taggedValuesCompartmentSize.getHeight() + packageImportCompartmentSize.getHeight());

               if (taggedValueAndPkgImportSize.getHeight() > 0 && bDrewInnerRect)
               {
                  IETRect combinedRect = new ETDeviceRect(rect.getRectangle());

                  combinedRect.setTop(Math.max((rect.getBottom() - taggedValueAndPkgImportSize.getHeight()), rect.getCenterPoint().y));

                  // Now divide that rect into sizes for the tagged values and package import
                  IETRect taggedValuesRect = new ETDeviceRect(combinedRect.getRectangle());
                  IETRect packageImportRect = new ETDeviceRect(combinedRect.getRectangle());

                  taggedValuesRect.setBottom(Math.min((combinedRect.getTop() + taggedValuesCompartmentSize.getHeight()), combinedRect.getBottom()));
                  packageImportRect.setTop(taggedValuesRect.getBottom());
                  packageImportRect.setBottom(combinedRect.getBottom());

                  // Draw the tagged values compartment
                  if (pTaggedValuesCompartment != null)
                  {
                     pETRect = new ETDeviceRect(taggedValuesRect.getRectangle());
                     pTaggedValuesCompartment.draw(pInfo, pETRect);
                  }
                  // Draw the package import compartment
                  if (pPackageImportCompartment != null)
                  {
                     pETRect = new ETDeviceRect(packageImportRect.getRectangle());
                     pPackageImportCompartment.draw(pInfo, pETRect);
                  }
               }

            }
            else if (rect.getWidth() > (12 * dZoom) && rect.getHeight() > (12 * dZoom))
            {
               IETRect pkgBodyRect = new ETDeviceRect(rect.getRectangle());
               IETRect nameRect = new ETDeviceRect(rect.getRectangle());

               int nameCompartmentSlop = 4;

               // Adjust the name rect to the center of the body of the package
               nameCompartmentSize.setHeight(nameCompartmentSize.getHeight() + nameCompartmentSlop);

               if (nameCompartmentSize.getHeight() < nameRect.getHeight())
               {
                  nameRect.offsetRect(0, (int) ((nameRect.getHeight() - nameCompartmentSize.getHeight()) / 2));
                  nameRect.setBottom(nameRect.getTop() + nameCompartmentSize.getHeight());
               } 
               if (nameRect.getWidth() > (8 * dZoom))
               {
                  nameRect.setLeft(nameRect.getLeft() + (int) ((2 * dZoom)));
                  nameRect.setRight(nameRect.getRight() - (int) ((2 * dZoom)));
               }
               nameCompartmentSize.setHeight(nameCompartmentSize.getHeight() - nameCompartmentSlop);

               // Draw the name compartment
               pETRect = new ETDeviceRect(nameRect.getRectangle());
               pNameCompartment.draw(pInfo, pETRect);

               // Now draw the stereotype above it if we have space
               if (pStereotypeCompartment != null && nameRect.getTop() > pkgBodyRect.getTop())
               {
                  IETRect stereotypeRect = new ETDeviceRect(nameRect.getRectangle());

                  stereotypeRect.setBottom(nameRect.getTop());
                  stereotypeRect.setTop(stereotypeRect.getBottom() - stereotypeCompartmentSize.getHeight());

                  stereotypeRect.setTop(Math.max(stereotypeRect.getTop(), pkgBodyRect.getTop()));

                  // Draw the stereotype
                  pETRect = new ETDeviceRect(stereotypeRect.getRectangle());
                  pStereotypeCompartment.draw(pInfo, pETRect);
               }

               // Now draw the tagged values and package import compartment
               IETSize taggedValueAndPkgImportSize =
                  new ETSize(taggedValuesCompartmentSize.getWidth() + packageImportCompartmentSize.getWidth(), taggedValuesCompartmentSize.getHeight() + packageImportCompartmentSize.getHeight());
               if (taggedValueAndPkgImportSize.getHeight() > 0 && nameRect.getBottom() < pkgBodyRect.getBottom())
               {
                  IETRect combinedRect = new ETDeviceRect(rect.getRectangle());

                  combinedRect.setTop(Math.max((rect.getBottom() - taggedValueAndPkgImportSize.getHeight()), rect.getCenterPoint().y));

                  combinedRect.setTop(nameRect.getBottom());
                  combinedRect.setBottom(combinedRect.getTop() + taggedValueAndPkgImportSize.getHeight());

                  combinedRect.setBottom(Math.min(combinedRect.getBottom(), pkgBodyRect.getBottom()));

                  // Now divide that rect into sizes for the tagged values and package import
                  IETRect taggedValuesRect = new ETDeviceRect(combinedRect.getRectangle());
                  IETRect packageImportRect = new ETDeviceRect(combinedRect.getRectangle());

                  taggedValuesRect.setBottom(Math.min((combinedRect.getTop() + taggedValuesCompartmentSize.getHeight()), combinedRect.getBottom()));
                  packageImportRect.setTop(taggedValuesRect.getBottom());
                  packageImportRect.setBottom(combinedRect.getBottom());

                  // Draw the tagged values compartment
                  if (pTaggedValuesCompartment != null)
                  {
                     pETRect = new ETDeviceRect(taggedValuesRect.getRectangle());
                     pTaggedValuesCompartment.draw(pInfo, pETRect);
                  }
                  // Draw the package import compartment
                  if (pPackageImportCompartment != null)
                  {
                     pETRect = new ETDeviceRect(packageImportRect.getRectangle());
                     pPackageImportCompartment.draw(pInfo, pETRect);
                  }
               }
            }
         } // end of  if (pNameCompartment != null)

      }
   }

   /**
    * Create the compartments for this node.
    */
   public void createCompartments()
   {
      clearCompartments();

      ETClassNameListCompartment newClassNameList = new ETClassNameListCompartment(this);
      newClassNameList.addCompartment(new ETClassNameCompartment(this), -1, false);
      this.addCompartment(newClassNameList);

      //     createAndAddCompartment("ADNameListCompartment", 0);
   }

   /**
    * Initializes our compartments.
    *
    * @param pElement [in] The presentation element we are representing
    */
   public void initCompartments(IPresentationElement pElement)
   {

      try
      {
         // We may get here with no compartments.  This happens if we've been created
         // by the user.  If we read from a file then the compartments have been pre-created and
         // we just need to initialize them.
         long numCompartments = getNumCompartments();

         if (numCompartments == 0)
         {
            createCompartments();
         }

         IElement pModelElement = pElement.getFirstSubject();

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
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Called when the context menu is about to display.
    *
    * @param pContextMenu [in] The context menu about to be displayed
    * @param logicalX [in] The logical x location of the context menu event
    * @param logicalY [in] The logical y location of the context menu event
    */
   public void onContextMenu(IMenuManager manager)
   {
      // Packages don't support the compartment color/font settings for the name compartment,
      // so for now I've turned that functionality off here.  In the future if a compartment
      // is added to the package and it needs to have a menu we'll have to re-enable by calling
      // the base class.
   }

   /**
    * Tells the draw engine to write its data to the IProductArchive
    *
    * @param pProductArchive [in] The archive we're saving to
    * @param pParentElement [in] The current element, or parent for any new attributes or elements.
    */
   public long writeToArchive(IProductArchive pProductArchive, IProductArchiveElement pElement)
   {
      super.writeToArchive(pProductArchive, pElement);
      IProductArchiveElement engineEle = pElement.getElement(IProductArchiveDefinitions.ENGINENAMEELEMENT_STRING);
      if (engineEle != null)
      {
         engineEle.addAttributeBool(PACKAGECOMPARTMENT_TABLOCATION, m_bDisplayInTab);
      }
      return 0;
   }

   /**
    * Tells the draw engine to read its data to the IProductArchive
    *
    * @param pProductArchive [in] The archive we're reading from
    * @param pEngineElement [in] The element where this draw engine's information should exist.
    */
   public long readFromArchive(IProductArchive pProductArchive, IProductArchiveElement pParentElement)
   {
      super.readFromArchive(pProductArchive, pParentElement);
      boolean bVal = pParentElement.getAttributeBool(PACKAGECOMPARTMENT_TABLOCATION);
      m_bDisplayInTab = bVal;
      return 0;
   }

   public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
   {

      IETSize retVal = new ETSize(0, 0);

      IETSize tempSize = super.calculateOptimumSize(pDrawInfo, true);

      retVal.setWidth(Math.max(tempSize.getWidth(), MIN_NODE_WIDTH));
      retVal.setHeight(Math.max(tempSize.getHeight(), MIN_NODE_HEIGHT));

      return bAt100Pct || retVal == null ? retVal : scaleSize(retVal, pDrawInfo != null ? pDrawInfo.getTSTransform() : getTransform());
   }

   /**
    * Call this routine to change the body or tab rectangle.  It'll change the
    * m_bShapeChanged flag if necessary
    *
    * @param tabRect [in] The location of the tab
    */
   private void setTabRect(IETRect tabRect)
   {
      if (m_TabRect != tabRect)
      {
         m_bShapeChanged = true;
         m_TabRect = tabRect;
      }
   }
   /**
    * Call this routine to change the body or tab rectangle.  It'll change the
    * m_bShapeChanged flag if necessary
    *
    * @param tabRect [in] The location of the package body
    */
   private void setBodyRect(IETRect bodyRect)
   {
      if (m_BodyRect != bodyRect)
      {
         m_bShapeChanged = true;
         m_BodyRect = bodyRect;
      }
   }

   public boolean getNameInTab()
   {
      return this.m_bDisplayInTab;
   }

   public void setNameInTab(boolean bNameInTab)
   {

      this.m_bDisplayInTab = bNameInTab;
   }

   /**
    * Returns the optimum size for an item.  This is used when an item is created from the toolbar
    */
   public void sizeToContents()
   {
      // Size but keep the current size if possible
      sizeToContentsWithMin(MIN_NODE_WIDTH, MIN_NODE_HEIGHT, false, true);

   }

   /**
    * Is this draw engine valid for the element it is representing?
    *
    * @param bIsValid [in] VARIANT_TRUE if this draw engine can correctly represent the attached model element.
    */
   public boolean isDrawEngineValidForModelElement()
   {

      String currentMetaType = getMetaTypeOfElement();
      return currentMetaType != null && currentMetaType.equals("Package");

   }

	/**
	 * Tells the container that it should start containing the argument presentation elements
	 */
	public void beginContainment2( INodePresentation pPreviousContainer, ETList <IPresentationElement> pPresentationElements )
	{		
		//TODO
	}
}
