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

import org.netbeans.modules.uml.ui.products.ad.compartments.IADAttributeListCompartment;
import java.awt.Color;
import java.util.Collection;
import java.util.Iterator;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlsupport.ETDeviceRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETClassNameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADClassNameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADEnumerationLiteralListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADOperationListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.INameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IListCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ISimpleListCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ModelElementChangedKind;
import com.tomsawyer.editor.TSEFont;
import com.tomsawyer.editor.graphics.TSEGraphics;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;
import java.awt.GradientPaint;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;

public class ETEnumerationDrawEngine extends ETClassDrawEngine
{

   private TSEFont m_staticTextFont = new TSEFont("Arial-italic-11");
   private Color m_defaultTextColor = Color.black;
   /// Used to automatically hide the operations compartment when dropping
   private boolean m_CollapseOperationsCompartment = true;
   private boolean m_CollapseAttributesCompartment = true;

   public String getElementType()
   {
      String type = super.getElementType();
      if (type == null)
      {
         type = new String("Enumeration");
      }
      return type;
   }

   public void initResources()
   {
      setFillColor("enumerationfill", 251, 233, 126);
      setLightGradientFillColor("enumerationlightgradientfill", 254, 254, 254);
      setBorderColor("enumerationborder", Color.BLACK);

      super.initResources();
   }

   public String getDrawEngineID()
   {
      return "EnumerationDrawEngine";
   }

   public void createCompartments() throws ETException
   {
      this.clearCompartments();
      INameListCompartment pNameCompartment = (INameListCompartment)createAndAddCompartment("ADClassNameListCompartment");
      ISimpleListCompartment pEnumCompartment = (ISimpleListCompartment)createAndAddCompartment("ADEnumerationLiteralListCompartment");
      ISimpleListCompartment pAttributesCompartment = (ISimpleListCompartment)createAndAddCompartment("ADAttributeListCompartment");
      ISimpleListCompartment pOperationsCompartment = (ISimpleListCompartment)createAndAddCompartment("ADOperationListCompartment");
   }

   public void drawContents(IDrawInfo pDrawInfo)
   {
       TSEGraphics graphics = pDrawInfo.getTSEGraphics();
       IETGraphObjectUI parentUI = this.getParent();
       TSTransform transform = graphics.getTSTransform();
       
       IListCompartment prevFoundComparmtent = null;
       
       if (parentUI.getOwner() != null)
       {
           IETRect deviceRect = pDrawInfo.getDeviceBounds();
           
           TSConstRect localBounds = transform.boundsToWorld(deviceRect.getRectangle());
           
           float centerX = (float) deviceRect.getCenterX();
           GradientPaint paint = new GradientPaint(centerX, deviceRect.getBottom(), getBkColor(), centerX, deviceRect.getTop(), getLightGradientFillColor());
           GDISupport.drawRectangle(graphics, deviceRect.getRectangle(), getBorderBoundsColor(), paint);
           
           graphics.clipRect(localBounds);
           
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
                   
                   String staticText = foundCompartment.getName();
                   if (staticText != null && staticText.length() > 0)
                   {
                       graphics.setFont(m_staticTextFont.getScaledFont(pDrawInfo.getFontScaleFactor()));
                       graphics.setColor(this.m_defaultTextColor);
                       
                       // advance to the next line
                       this.updateLastDrawPointWorldY(graphics.getFontMetrics(m_staticTextFont).getHeight() / 1.5);
                       
                       // draw the static text
                       graphics.drawString(staticText, new TSConstPoint(localBounds.getCenterX() - (graphics.getFontMetrics(m_staticTextFont).stringWidth(staticText) / 2), this.getLastDrawPointWorldY()));
                       
                       // advance to the next line
                       this.updateLastDrawPointY(graphics.getFontMetrics().getHeight() / 2);
                   }
                   
                   // draw the sub compartments
                   IETSize nameListSize = foundCompartment.calculateOptimumSize(pDrawInfo, false);
                   
                   IETRect compartmentDrawRect = new ETRect(deviceRect.getLeft(), transform.yToDevice(this.getLastDrawPointWorldY()), deviceRect.getIntWidth(), nameListSize.getHeight());
                   
                   foundCompartment.draw(pDrawInfo, compartmentDrawRect);
                   
                   // advance to the next line
                   this.updateLastDrawPointWorldY(transform.heightToWorld(nameListSize.getHeight()));
                   
                   prevFoundComparmtent = foundCompartment;
                   
               }
               else
               {
                   foundCompartment.calculateOptimumSize(pDrawInfo, false);
                   IETSize listSize = foundCompartment.getCurrentSize(transform, false);
                   
                   IETRect compartmentDrawRect = new ETRect(deviceRect.getLeft(), transform.yToDevice(this.getLastDrawPointWorldY()), deviceRect.getIntWidth(), listSize.getHeight());
                   
                   //Redraw the background to handle list resizing issues
                   IETRect tmpRect = new ETDeviceRect(compartmentDrawRect.getRectangle());
                   tmpRect.inflate(-1, -1);
                   if (deviceRect.getBottom() <= tmpRect.getBottom())
                   {
                       tmpRect.setBottom(deviceRect.getBottom() - 2);
                   }
                   GDISupport.drawRectangle(graphics, tmpRect.getRectangle(), paint, paint);
                   
                   if (!foundCompartment.getCollapsed())
                   {
                       // Draw the divider
                       this.drawCompartmentDivider(graphics, localBounds, false, prevFoundComparmtent, foundCompartment);
                       
                       foundCompartment.draw(pDrawInfo, compartmentDrawRect);
                       
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
           
           if (prevFoundComparmtent != null)
           {
               if (prevFoundComparmtent.getBoundingRect().getBottom() < deviceRect.getBottom() - 2)
               {
                   prevFoundComparmtent.getBoundingRect().setBottom(deviceRect.getBottom() - 2);
               }
           }
           
       }
   }

   public long modelElementHasChanged(INotificationTargets pTargets)
   {
      IElement pSecondaryChangedModelElement = pTargets.getSecondaryChangedModelElement();
      IElement pModelElement = pTargets.getChangedModelElement();
      int nKind = pTargets.getKind();

      if (nKind != ModelElementChangedKind.MECK_ELEMENTMODIFIED)
      {
         if (pSecondaryChangedModelElement instanceof IEnumerationLiteral)
         {
            IEnumerationLiteral pEnumerationLiteral = (IEnumerationLiteral)pSecondaryChangedModelElement;
            IADEnumerationLiteralListCompartment pEnumListCompartment = getCompartmentByKind(IADEnumerationLiteralListCompartment.class);
            if (pEnumListCompartment != null)
            {
               pEnumListCompartment.modelElementHasChanged(pTargets);
            }
         }
         else
         {
            super.modelElementHasChanged(pTargets);
         }
      }
      sizeToContents();
      return 0;
   }

   /**
    * Initializes our compartments.
    *
    * @param pElement [in] The presentation element we are representing
    */
   public void initCompartments(IPresentationElement pElement)
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

      IElement pModelElement = pElement.getFirstSubject();

      IEnumeration pEnumeration = (pModelElement instanceof IEnumeration) ? (IEnumeration)pModelElement : null;

      if (pEnumeration != null && numComps > 0)
      {

         // Get all the compartments
         IADClassNameListCompartment pNameCompartment = getCompartmentByKind(IADClassNameListCompartment.class);
         IADEnumerationLiteralListCompartment pEnumCompartment = getCompartmentByKind(IADEnumerationLiteralListCompartment.class);
         IADAttributeListCompartment pAttributesCompartment = getCompartmentByKind(IADAttributeListCompartment.class);
         IADOperationListCompartment pOperationsCompartment = getCompartmentByKind(IADOperationListCompartment.class);

         if (pNameCompartment != null)
         {
            pNameCompartment.attach(pModelElement);
            pNameCompartment.addStaticText("<<enumeration>>");
         }

         // Attach the enumeration literals
         ETList < IEnumerationLiteral > pLiterals = pEnumeration.getLiterals();

         if (pLiterals != null && pEnumCompartment != null)
         {
            pEnumCompartment.attachElements(new ETArrayList < IElement > ((Collection)pLiterals), true, false);
         }

		 // attach attributes #79833
		 ETList <IAttribute> attributes = pEnumeration.getAttributes();
		 if (attributes != null && attributes.size()>0 && pAttributesCompartment != null)
		 {
			 pAttributesCompartment.attachElements(new ETArrayList <IElement> ((Collection)attributes), true, false);
		 }
		 
         // Attach the operations
         ETList < IOperation > pOperations = pEnumeration.getNonRedefiningOperations();
         if (pOperations != null && pOperationsCompartment != null)
         {
            pOperationsCompartment.attachElements(new ETArrayList < IElement > ((Collection)pOperations), true, false);

            // W4984 Automatically hide the operations compartment when dropping an enumeration.
            m_CollapseOperationsCompartment = true;
            pOperationsCompartment.setCollapsed(true);
            
            m_CollapseAttributesCompartment = true;
            pAttributesCompartment.setCollapsed(true);
         }
      }
   }

}