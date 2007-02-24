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
import java.awt.Shape;
import java.util.Iterator;
import java.util.Vector;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.common.ETStrings;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.structure.IDeploymentSpecification;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETClassNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETClassNameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADClassNameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.INameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IListCompartment;
import com.tomsawyer.editor.TSEColor;
import com.tomsawyer.editor.TSEFont;
import com.tomsawyer.editor.graphics.TSEGraphics;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;

public class ETDeploymentSpecDrawEngine extends ETNodeDrawEngine
{

   protected final int NODE_WIDTH = 140;
   protected final int NODE_HEIGHT = 100;
   private TSEFont m_staticTextFont = new TSEFont("Arial-plain-12");
   private TSEColor m_defaultTextColor = new TSEColor(TSEColor.black);

   public String getDrawEngineID()
   {
      return "DeploymentSpecDrawEngine";
   }

   public String getElementType()
   {
      String type = super.getElementType();
      if (type == null)
      {
         type = new String("DeploymentSpecification");
      }
      return type;
   }

   public void initResources()
   {
      setFillColor("deploymentspecfill", 195, 226, 203);
      setLightGradientFillColor("deploymentspeclightgradientfill", 255, 255, 255);
      setBorderColor("deploymentspecborder", Color.BLACK);

      super.initResources();
   }

   public void createCompartments()
   {
      clearCompartments();
      ETClassNameListCompartment newClassNameList = new ETClassNameListCompartment(this);
      newClassNameList.addCompartment(new ETClassNameCompartment(this), -1, false);
      newClassNameList.setName("<<deployment spec>>");
      this.addCompartment(newClassNameList);
   }

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

      IElement modEle = presEle.getFirstSubject();

      if (modEle != null && numComps > 0)
      {
         // Get the metatype of the element, we use it later to turn off the package import compartment
         // if necessary.
         String elemType = modEle.getElementType();

         if (modEle instanceof IDeploymentSpecification)
         {
            IADClassNameListCompartment pNameCompartment = getCompartmentByKind(IADClassNameListCompartment.class);

            // create a compartment for the classifier's name, it will contain the classifier IElement
            if (pNameCompartment != null)
            {
               pNameCompartment.attach(modEle);
            }

         }
         else
         {
            // not a classifier, something is wrong
         }
      }
   }

   public void drawContents(IDrawInfo pDrawInfo)
   {
      TSEGraphics graphics = pDrawInfo.getTSEGraphics();
      TSTransform transform = graphics.getTSTransform();

      if (pDrawInfo != null)
      {

         IETRect deviceRect = pDrawInfo.getDeviceBounds();
         TSConstRect localBounds = transform.boundsToWorld(deviceRect.getRectangle());

         float centerX = (float)deviceRect.getCenterX();
         GradientPaint paint = new GradientPaint(centerX,
                         deviceRect.getBottom(),
                         getBkColor(),
                         centerX,
                         deviceRect.getTop(),
                         getLightGradientFillColor());
    
         GDISupport.drawRectangle(graphics, deviceRect.getRectangle(), getBorderBoundsColor(), paint);

         Font originalFont = graphics.getFont();

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

                  // advance to the next line 
                  this.updateLastDrawPointWorldY(transform.heightToWorld(graphics.getFontMetrics().getHeight()) / 1.5);

                  // set the color of the pen to the text color
                  graphics.setColor(m_defaultTextColor);

                  // draw the static text
                  graphics.drawString(
                     staticText,
                     transform.xToDevice(localBounds.getLeft() + localBounds.getWidth() / 2) - (graphics.getFontMetrics().stringWidth(staticText) / 2),
                     transform.yToDevice(this.getLastDrawPointWorldY()));

                  // advance to the next line 
                  this.updateLastDrawPointWorldY(transform.heightToWorld(graphics.getFontMetrics().getHeight() / 2));
               }

               // draw the sub compartments
               IETSize nameListSize = foundCompartment.calculateOptimumSize(pDrawInfo, false);

               IETRect compartmentDrawRect = new ETRect(deviceRect.getLeft(), transform.yToDevice(this.getLastDrawPointWorldY()), deviceRect.getIntWidth(), nameListSize.getHeight());

               //TODO we wont need to convert once everything speaks in world coordinates
               foundCompartment.draw(pDrawInfo, compartmentDrawRect);

               // advance to the next line 
               this.updateLastDrawPointWorldY(transform.heightToWorld(nameListSize.getHeight()));

            }

         } //end while

         graphics.setFont(originalFont);
      }
   }

   public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
   {

      IETSize retVal = new ETSize(0, 0);

      IETSize tempSize = super.calculateOptimumSize(pDrawInfo, true);

      retVal.setWidth(Math.max(tempSize.getWidth(), NODE_WIDTH));
      retVal.setHeight(Math.max(tempSize.getHeight(), NODE_HEIGHT));

      return bAt100Pct || retVal == null ? retVal : scaleSize(retVal, pDrawInfo != null ? pDrawInfo.getTSTransform() : getTransform());
   }

}
