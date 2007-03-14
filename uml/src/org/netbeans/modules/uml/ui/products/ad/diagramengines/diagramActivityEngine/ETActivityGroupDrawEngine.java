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
import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.ComplexActivityGroup;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IComplexActivityGroup;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IIterationActivityGroup;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADExpressionCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.INameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ContainmentTypeEnum;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETContainerDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IBoxCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ISupportEnums;
import com.tomsawyer.editor.TSEColor;
import com.tomsawyer.editor.ui.TSENodeUI;
import java.awt.GradientPaint;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;

/**
 * @author KevinM
 *
 */
public class ETActivityGroupDrawEngine extends ETContainerDrawEngine implements IActivityGroupDrawEngine
{
   
   // The activity group represents an IterationActivityGroup
   public static final int AGK_ITERATION = 0;
   // The activity group represents an StructuredActivityGroup.
   public static final int AGK_STRUCTURED = 1;
   // The activity group represents an InterruptibleActivityGroup." )
   public static final int AGK_INTERRUPTIBLE = 2;
   
   public static final int IAG_TEST_AT_BEGIN = 0;
   public static final int IAG_TEST_AT_END = IAG_TEST_AT_BEGIN + 1;
   public static final int MIN_NAME_SIZE_X = 40;
   public static final int MIN_NAME_SIZE_Y = 20;
   public static final int MIN_NODE_WIDTH = 20;
   public static final int MIN_NODE_HEIGHT = 20;
   
   protected String m_LastActivityGroupKind = "";
   
   /*
    *
    */
   public ETActivityGroupDrawEngine()
   {
      setContainmentType(ContainmentTypeEnum.CT_GRAPHICAL);
      
   }
   
   public void initResources()
   {
      setFillColor("boxfill", 225, 255, 255);
      setLightGradientFillColor("boxlightgradientfill", 255, 255, 255);
      setBorderColor("boxborder", 0, 0, 0);
      super.initResources();
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#doDraw(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo)
    */
   public void doDraw(IDrawInfo pDrawInfo)
   {
      if (pDrawInfo == null)
         return;
      
      TSEColor textColor = this.getTextColor();
      
      // draw our frame
      
      // Draw a dashed, rounded rectangle around the entire node
      //
      //      /--------\
      //     /          \
      //    |            |
      //    |            |
      //    |    Name    |
      //    |            |
      //     \           /
      //      \--------/
      //
      
      IETRect deviceBounds = pDrawInfo.getDeviceBounds();
      float centerX = (float) deviceBounds.getCenterX();
      GradientPaint paint = new GradientPaint(centerX, deviceBounds.getBottom(), getBkColor(), centerX, deviceBounds.getTop(), getLightGradientFillColor());
      GDISupport.drawDashedRoundRect(pDrawInfo.getTSEGraphics(), deviceBounds.getRectangle(), pDrawInfo.getOnDrawZoom(), this.getBorderBoundsColor(), paint);
      
      //TODO Workaround in order to force the expression compartment to refresh.
      // This problem is carried over from C++. We never get an elementmodified event in order to refresh the expression
      // When we do get the proper event then we can remove the hack below:
      IADExpressionCompartment pExpressionCompartment = getCompartmentByKind(IADExpressionCompartment.class);
      if (pExpressionCompartment != null)
      {
         ((ETCompartment)pExpressionCompartment).reattach();
      }
      //////////////
      
      // Draw each compartment
      handleNameListCompartmentDraw(pDrawInfo, deviceBounds, MIN_NAME_SIZE_X, MIN_NAME_SIZE_Y, false, 0);
      // Give the container a chance to draw
      super.setDrawContained(true); //Jyothi
      super.doDraw(pDrawInfo);
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getDrawEngineID()
    */
   public String getDrawEngineID()
   {
      return "ActivityGroupDrawEngine";
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#sizeToContents()
    */
   public void sizeToContents()
   {
      //		super.sizeToContents();
      
      sizeToContentsWithMin(MIN_NODE_WIDTH, MIN_NODE_HEIGHT, false, true);
   }
   
   /* (non-Javadoc)
    * @see java.lang.Object#clone()
    */
   public Object clone()
   {
      return super.clone();
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#createCompartments()
    */
   public void createCompartments() throws ETException
   {
      try
      {
         clearCompartments();
         // Create a box compartment after the expression to have the expression compartment butt up against
         // the top of the namelist using minimal space.
         ICompartment pCompartment = createAndAddCompartment("ADNameListCompartment");
         ICompartment pExpressionCompartment = createAndAddCompartment("ADExpressionCompartment");
         ICompartment pBoxCompartment = createAndAddCompartment("BoxCompartment");
         
         //			if (pExpressionCompartment != null)
         //			{
         //			   pExpressionCompartment.setStyle(DT_SINGLELINE | DT_BOTTOM | DT_CENTER | DT_END_ELLIPSIS);
         //			}
         
         IBoxCompartment pActualBoxCompartment = pBoxCompartment instanceof IBoxCompartment ? (IBoxCompartment)pBoxCompartment : null;
         if (pActualBoxCompartment != null)
         {
            pActualBoxCompartment.setBoxKind(ISupportEnums.BK_NO_BORDER);
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initCompartments(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
    */
   public void initCompartments(IPresentationElement pPresElement)
   {
      try
      {
         // We may get here with no compartments.  This happens if we've been created
         // by the user.  If we read from a file then the compartments have been pre-created and
         // we just need to initialize them.
         if (getNumCompartments() == 0)
         {
            createCompartments();
         }
         
         // Set the static text compartment text
         String currentMetaType = getMetaTypeOfElement();
         if (currentMetaType == null)
            return;
         
         INameListCompartment pNameCompartment = getCompartmentByKind(INameListCompartment.class);
         IADExpressionCompartment pExpressionCompartment = getCompartmentByKind(IADExpressionCompartment.class);
         
         // If it's a complex group change the metatype to one of the simpler ones
         if (currentMetaType.equals("ComplexActivityGroup"))
         {
            IElement pElement = getFirstModelElement();
            if (pElement instanceof IComplexActivityGroup)
            {
               IComplexActivityGroup pComplex = (IComplexActivityGroup)pElement;
               int nKind = AGK_ITERATION;
               if (pComplex instanceof ComplexActivityGroup)
               {
                  nKind = ((ComplexActivityGroup)pComplex).getGroupKind();
               }
               if (nKind == AGK_ITERATION)
               {
                  currentMetaType = "IterationActivityGroup";
               }
               else if (nKind == AGK_STRUCTURED)
               {
                  currentMetaType = "StructuredActivityGroup";
               }
               else if (nKind == AGK_INTERRUPTIBLE)
               {
                  currentMetaType = "InterruptibleActivityRegion";
               }
            }
         }
         
         // If the activity group kind changed, or if it's an IteractionActivityGroup then
         // there might be changes that affect the presentation.
         if (m_LastActivityGroupKind != currentMetaType || currentMetaType.equals("IterationActivityGroup"))
         {
            m_LastActivityGroupKind = currentMetaType;
            
            IElement pElement = getFirstModelElement();
            if (currentMetaType.equals("IterationActivityGroup"))
            {
               int nKind = IAG_TEST_AT_BEGIN;
               
               IIterationActivityGroup pIterationActivityGroup = pElement instanceof IIterationActivityGroup ? (IIterationActivityGroup)pElement : null;
               if (pIterationActivityGroup != null)
               {
                  IValueSpecification pExpression = getTestExpression();
                  if (pExpression != null && pExpressionCompartment != null)
                  {
                     pExpressionCompartment.addModelElement(pExpression, 0);
                  }
               }
               
               // Attach the name compartment
               pNameCompartment.attach(pElement);
               pNameCompartment.setResizeToFitCompartments(true);
               
               // Now setup the static compartment
               if (pIterationActivityGroup != null)
               {
                  nKind = pIterationActivityGroup.getKind();
               }
               
               pNameCompartment.addStaticText(nKind == IAG_TEST_AT_BEGIN ? "<<testAtBegin>>" : "<<testAtEnd>>");
            }
            else if (currentMetaType.equals("StructuredActivityGroup"))
            {
               if (pNameCompartment != null)
               {
                  // The name compartment is not used
                  pNameCompartment.attach(pElement);
                  pNameCompartment.setName("");
                  pNameCompartment.setReadOnly(true);
                  pNameCompartment.addStaticText("<<structured>>");
               }
            }
            else if (currentMetaType.equals("InterruptibleActivityRegion"))
            {
               if (pNameCompartment != null)
               {
                  // The name compartment is not used
                  pNameCompartment.attach(pElement);
                  pNameCompartment.setName("");
                  pNameCompartment.setReadOnly(true);
                  pNameCompartment.addStaticText("<<structured>>");
               }
            }
         }
         initResources();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#isDrawEngineValidForModelElement()
    */
   public boolean isDrawEngineValidForModelElement()
   {
      String currentMetaType = getMetaTypeOfElement();
      return currentMetaType != null
            && currentMetaType.equals("InterruptibleActivityRegion")
            || currentMetaType.equals("IterationActivityGroup")
            || currentMetaType.equals("StructuredActivityGroup")
            || currentMetaType.equals("ComplexActivityGroup");
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#modelElementDeleted(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
    */
   public long modelElementDeleted(INotificationTargets pTargets)
   {
      // TODO Auto-generated method stub
      return super.modelElementDeleted(pTargets);
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#modelElementHasChanged(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
    */
   public long modelElementHasChanged(INotificationTargets pTargets)
   {
      // Initialize the compartments in case the kind was set and the activity group basic kind was
      // changed.
      initCompartments(null);
      // Use the convinient routine to update the name list compartment
      return handleNameListModelElementHasChanged(pTargets);
   }
   
   
   /*
    * If this is an Interruptible activity group then this gets the expression
    */
   protected IValueSpecification getTestExpression()
   {
      IValueSpecification pExpression = null;
      try
      {
         // Get the parent element and the namespace of the diagram
         IElement pElement = getFirstModelElement();
         
         IIterationActivityGroup pIteration = pElement instanceof IIterationActivityGroup ? (IIterationActivityGroup)pElement : null;
         if (pIteration != null)
         {
            pExpression = pIteration.getTest();
            if (pExpression == null)
            {
               // Create one if the activity doesn't already have one.
               TypedFactoryRetriever < IExpression > factory = new TypedFactoryRetriever < IExpression > ();
               
               IExpression pNewExpression = factory.createType("Expression");
               if (pNewExpression != null)
               {
                  pIteration.addElement(pNewExpression);
                  pIteration.setTest(pNewExpression);
                  pExpression = pIteration.getTest();
               }
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         pExpression = null;
      }
      return pExpression;
   }
   
   /**
    * Used in ResizeToFitCompartment.  Returns the resize behavior
    * PSK_RESIZE_ASNEEDED     :  Always resize to fit. May grow or shrink.
    * PSK_RESIZE_EXPANDONLY   :  Grows only if necessary, never shrinks.
    * PSK_RESIZE_UNLESSMANUAL :  Grows only if the user has not manually resized. Never shrinks.
    * PSK_RESIZE_NEVER        :  Never resize.
    *
    */
   
   public String getResizeBehavior()
   {
      return "PSK_RESIZE_EXPANDONLY";
   }
   
   // Added this method to fixed issue 78320. Not always to shrink this element to fit the compartment but to
   // adjust the size of the element based on the "resize behavior" attribute.
   public void resizeToFitCompartment(ICompartment pCompartment, boolean bKeepUpperLeftPoint, boolean bIgnorePreferences)
   {
      if (getOwnerNode() != null)
      {
         // default is to always resize
         
         String sPreference = bIgnorePreferences ? "PSK_RESIZE_ASNEEDED" : getResizeBehavior();
         
         if (sPreference != null && !sPreference.equals("PSK_RESIZE_NEVER"))
         {
            IETSize szDesired = this.calculateOptimumSize(null, false);
            // Get our current size
            // if we're to expand only, new size is max of current vs desire size
            // if max is not equal to current then we resize
            // resize to desired size
            IETRect rect = this.getDeviceBoundingRect();
            
            // this size is in zoomed coordinates
            IETSize szOrig = new ETSize(rect.getIntWidth(), rect.getIntHeight());
            
            // choices at this point are either expandonly or as needed
            if (sPreference.equals("PSK_RESIZE_EXPANDONLY"))
            {
               // Grow if necessary, never shrink
               szDesired.setWidth(Math.max(szDesired.getWidth(), szOrig.getWidth()));
               szDesired.setHeight(Math.max(szDesired.getHeight(), szOrig.getHeight()));
            }
            else
            {
               // Adjust size to allow for border thickness (nodes that don't have borders
               // or rectangular borders should override)
               int borderThickness = getBorderThickness();
               szDesired.setWidth(szDesired.getWidth() + (2 * borderThickness));
               szDesired.setHeight(szDesired.getHeight() + (2 * borderThickness));
            }
            
            // resize if we've changed
            if (szDesired.getWidth() != szOrig.getWidth() || szDesired.getHeight() != szOrig.getHeight())
            {
               // Retrieve the graphical container before the resize
               INodePresentation cpContainer = TypeConversions.getGraphicalContainer(this);
               
               // perform resize
               resize(szDesired.getWidth(), szDesired.getHeight(), bKeepUpperLeftPoint);
               
               // Make sure the container is resized
               if (cpContainer != null)
               {
                  INodePresentation cpNodePE = TypeConversions.getNodePresentation(this);
                  
                  if (cpNodePE != null)
                  {
                     cpContainer.resizeToContain(cpNodePE);
                  }
               }
               
               // Make sure any qualifiers are relocated
               this.relocateQualifiers(false);
            }
         }
      }
   }
}