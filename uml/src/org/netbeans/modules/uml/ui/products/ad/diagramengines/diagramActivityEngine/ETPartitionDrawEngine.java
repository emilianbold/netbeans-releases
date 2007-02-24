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

import java.awt.Color;
import java.util.ArrayList;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADActivityPartitionsCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADZonesCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.INameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ContainmentTypeEnum;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETContainerDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import java.awt.GradientPaint;

/**
 * @author Embarcadero Technologies Inc.
 *
 */

public class ETPartitionDrawEngine extends ETContainerDrawEngine implements IPartitionDrawEngine
{
   protected static final int MIN_NAME_SIZE_X = 40;
   protected static final int MIN_NAME_SIZE_Y = 20;
   protected static final int MIN_NODE_WIDTH = 20;
   protected static final int MIN_NODE_HEIGHT = 20;

	public ETPartitionDrawEngine()
	{
		super();
		this.setContainmentType(ContainmentTypeEnum.CT_NAMESPACE | ContainmentTypeEnum.CT_ACTIVITYGROUP);
	}
	
   public void init() throws ETException
   {
      super.init();
      this.setContainmentType(ContainmentTypeEnum.CT_NAMESPACE | ContainmentTypeEnum.CT_ACTIVITYGROUP);
   }

   public String getDrawEngineID()
   {
      return "PartitionDrawEngine";
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getElementType()
    */
   public String getElementType()
   {
      return "Activity";
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#doDraw(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo)
    */
   public void doDraw(IDrawInfo pDrawInfo)
   {
      boolean bDrawnMoving = false;
      //_VH(DrawMoving(pInfo, &bDrawnMoving));

      if (!bDrawnMoving)
      {
         IETRect deviceBounds = pDrawInfo.getDeviceBounds();
         float centerX = (float) deviceBounds.getCenterX();
         GradientPaint paint = new GradientPaint(centerX, deviceBounds.getBottom(), getBkColor(), centerX, deviceBounds.getTop(), getLightGradientFillColor());
         GDISupport.drawRectangle(pDrawInfo.getTSEGraphics(), deviceBounds.getRectangle(), getBorderBoundsColor(), paint);

         // Draw each compartment
         handleNameListCompartmentDraw(pDrawInfo, deviceBounds, MIN_NAME_SIZE_X, MIN_NAME_SIZE_Y, false, 0);

         // This will draw an invalid frame around the node if it doesn't have an IElement
         //_VH( DrawInvalidRectangle( pInfo, 0 ));

         // Put the selection handles
         //CGDISupport::DrawSelectionHandles(pInfo);
      }
		// Give the container a chance to draw
      super.setDrawContained(true); //JM
      super.doDraw(pDrawInfo);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine#resizeToFitCompartment(org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment, boolean, boolean)
    */
   public void resizeToFitCompartment(ICompartment pCompartment, boolean bKeepUpperLeftPoint, boolean bIgnorePreferences)
   {

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#sizeToContents()
    */
   public void sizeToContents()
   {
      // Size but keep the current size if possible
      //		Since this is a container, only grow the right, and bottom
      sizeToContentsWithMin(MIN_NODE_WIDTH, MIN_NODE_HEIGHT, true, true);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#modelElementDeleted(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
    */
   public long modelElementDeleted(INotificationTargets pTargets)
   {
      return super.modelElementDeleted(pTargets);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#modelElementHasChanged(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
    */
   public long modelElementHasChanged(INotificationTargets pTargets)
   {
      try
      {
         IADZonesCompartment cpZones = getCompartmentByKind(IADZonesCompartment.class);
         if (cpZones != null)
         {
            cpZones.modelElementHasChanged(pTargets);
         }

         return super.handleNameListModelElementHasChanged(pTargets);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#createCompartments()
    */
   public void createCompartments() throws ETException
   {
      try
      {
         clearCompartments();

         ICompartment pCreatedNameCompartment = createAndAddCompartment("ADNameListCompartment", 0);
         createAndAddCompartment("ADActivityPartitionsCompartment");

         // The name compartment is the default
         setDefaultCompartment(pCreatedNameCompartment);

      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initCompartments(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
    */
   public void initCompartments(IPresentationElement pElement)
   {
      try
      {
         // We may get here with no compartments.  This happens if we've been created
         // by the user.  If we read from a file then the compartments have been pre-created and
         // we just need to initialize them.
         int numCompartments = getNumCompartments();
         if (numCompartments == 0)
         {
            createCompartments();
         }

         IElement pModelElement = pElement != null ? pElement.getFirstSubject() : null;
         if (pModelElement != null)
         {
            INameListCompartment pNameCompartment = getCompartmentByKind(INameListCompartment.class);
            if (pNameCompartment != null)
            {
               pNameCompartment.attach(pModelElement);
            }

            IADZonesCompartment cpZones = getCompartmentByKind(IADZonesCompartment.class);
            if (cpZones != null)
            {
               cpZones.initCompartments(pElement);
               cpZones.addModelElement(pModelElement, -1);
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public boolean isDrawEngineValidForModelElement()
   {
      String currentMetaType = getMetaTypeOfElement();
      return currentMetaType.equals("ActivityPartition");
   }

   public void initResources()
   {
      setFillColor("partitionfill", 105, 191, 105);
      setLightGradientFillColor("partitionlightgradientfill", 255, 255, 255);
      setBorderColor("partitionborder", Color.BLACK);

      super.initResources();
   }

   /**
    * IPartitionDrawEngine
    */
   public boolean fullyPopulatePartitions()
   {
      boolean bItemsAdded = false;

      IADActivityPartitionsCompartment pADActivityPartitionsCompartment = getCompartmentByKind(IADActivityPartitionsCompartment.class);

      if (pADActivityPartitionsCompartment != null)
      {
			bItemsAdded = pADActivityPartitionsCompartment.populateAllPartitions();
      }

      return bItemsAdded;
   }

   /**
    * Populates this container with what it's contents should be
    */
   public boolean populate()
   {
      return fullyPopulatePartitions();
   }
   
   protected int getNumSelectableCompartments() 
   {
       // The selectable compartments include all of zone compartments,
       // plus the NameListCompartment.
       IADZonesCompartment cpZones = getCompartmentByKind(IADZonesCompartment.class);
       return cpZones.getCompartments().size() + 1;
   }
   
   protected ICompartment getSelectableCompartment(int index) 
   {
       ICompartment retVal = null;
       
       ArrayList < ICompartment > ownedCompartments = new ArrayList < ICompartment >();
       
       INameListCompartment nameCompartment = getCompartmentByKind( INameListCompartment.class );
       ownedCompartments.add(nameCompartment);
       
       IADZonesCompartment cpZones = getCompartmentByKind(IADZonesCompartment.class);
       for(ICompartment curCompartment : cpZones.getCompartments()) {
           ownedCompartments.add(curCompartment);
       }
       
       return ownedCompartments.get(index);
   }

}
