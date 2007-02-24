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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
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
import org.netbeans.modules.uml.ui.products.ad.compartments.IADZonesCompartment;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IProcedure;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IRegion;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITaggedValue;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.ui.support.umltsconversions.RectConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETTaggedValuesCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETStereoTypeCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETClassNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETClassNameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETStaticTextCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADClassNameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADEditableCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETStateEventsAndTransitionsListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADNamedElementListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADStateEventsAndTransitionsListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.INameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETRegionsCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IRegionsCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNodeUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IListCompartment;
import org.netbeans.modules.uml.core.support.umlsupport.ETDeviceRect;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADCompartment;
import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.common.ETStrings;

/**
 * @author jingmingm
 *
 */
public class ETStateDrawEngine extends ETContainerDrawEngine implements IStateDrawEngine
{
   protected final int MIN_ELIPSE_SIZE = 15;
   protected final int MIN_NAME_SIZE_X = 40;
   protected final int MIN_NAME_SIZE_Y = 20;
   protected final int MIN_NODE_WIDTH = 150;
   protected final int MIN_NODE_HEIGHT = 80;
   protected final int NAME_COMPARTMENT_LEFT_OFFSET = 10;

   protected final int SETK_UNKNOWN = 0;
   protected final int SETK_YES = 1;
   protected final int SETK_NO = 2;

   protected int m_bShowEventsAndTransitions = SETK_UNKNOWN;

   protected boolean isComposite()
   {
      boolean bIsComposite = false;

      IElement element = getParent().getModelElement();
      if (element instanceof IState)
      {
         IState state = (IState) element;
         bIsComposite = state.getIsComposite();
         if (bIsComposite == false)
         {
            // See if it's orthogonal
            bIsComposite = state.getIsOrthogonal();
         }
      }

      return bIsComposite;
   }

   protected boolean isSubmachineState()
   {
      boolean bIsSubmachineState = false;

      IElement element = getParent().getModelElement();
      if (element instanceof IState)
      {
         IState state = (IState) element;
         bIsSubmachineState = state.getIsSubmachineState();
      }

      return bIsSubmachineState;
   }

   protected boolean isSimpleState()
   {
      boolean bIsSimpleState = false;

      IElement element = getParent().getModelElement();
      if (element instanceof IState)
      {
         IState state = (IState) element;
         bIsSimpleState = state.getIsSimple();
         if (bIsSimpleState == false)
         {
            if (isComposite() == false && isSubmachineState() == false)
            {
               bIsSimpleState = true;
            }
         }
      }
      return bIsSimpleState;
   }

   protected void verifyCompartments() throws ETException
   {
      boolean bIsSimpleState = isSimpleState();
      boolean bIsComposite = isComposite();
      boolean bIsSubmachineState = isSubmachineState();

      IETGraphObjectUI parentUI =  this.getParent();
      IElement pModelElement = parentUI.getModelElement();

      // Verify that we have the correct compartments
      INameListCompartment pNameListCompartment = (INameListCompartment) getCompartmentByKind(ETClassNameListCompartment.class);
      ICompartment pTransitionListCompartment = (ICompartment) getCompartmentByKind(ETStateEventsAndTransitionsListCompartment.class);
      ETRegionsCompartment pRegionsCompartment = getCompartmentByKind(ETRegionsCompartment.class);

      // All have a name compartment
      if (pNameListCompartment == null)
      {
         pNameListCompartment = (INameListCompartment) createAndAddCompartment("ADClassNameListCompartment");
         if (pNameListCompartment != null && pModelElement != null)
         {
            pNameListCompartment.attach(pModelElement);

            IADNameCompartment pNameCompartment = pNameListCompartment.getNameCompartment();
            if (pNameCompartment != null)
            {
               pNameCompartment.setTextWrapping(bIsSimpleState);
               pNameCompartment.setVerticallyCenterText(bIsSimpleState);
            }

            setDefaultCompartment(pNameListCompartment);
         }
      }

      if (bIsSimpleState || bIsSubmachineState)
      {
         // Remove the regions compartment if there is one.  Also make sure we are not a container.

         if (pRegionsCompartment != null)
         {
            removeCompartment(pRegionsCompartment);
         }

         // If we need to initialize the events and transitions do it here
         if (m_bShowEventsAndTransitions == SETK_UNKNOWN)
         {
            if (bIsSimpleState)
            {
               m_bShowEventsAndTransitions = SETK_NO;
            }
            else
            {
               m_bShowEventsAndTransitions = SETK_YES;
            }
         }

         // Make sure we have a transition list compartment
         if (m_bShowEventsAndTransitions == SETK_YES && pTransitionListCompartment == null)
         {
//            ETStaticTextCompartment pETStaticTextCompartment = new ETStaticTextCompartment();
//            pETStaticTextCompartment.setName("Transitions");
//            pETStaticTextCompartment.setFontString("Arial-italic-12");
//            pETStaticTextCompartment.setEngine(this);
//            this.addCompartment(pETStaticTextCompartment);

            pTransitionListCompartment = (ICompartment) createAndAddCompartment("ADStateEventsAndTransitionsListCompartment");
            if (pModelElement != null)
            {
               pTransitionListCompartment.addModelElement(pModelElement, -1);
			   pTransitionListCompartment.setName("Transitions");
            }
         }
         else if (m_bShowEventsAndTransitions == SETK_NO && pTransitionListCompartment != null)
         {
            ICompartment pCompartment = (ICompartment) pTransitionListCompartment;
            this.removeCompartment(pCompartment, false);
         }

         // We are a disabled container
         this.setIsGraphicalContainer(false);
      }
      else if (bIsComposite)
      {
         if (pRegionsCompartment == null)
         {
            // For some reason C++ uses zero for the compartment location.  For Java we need -1 so the compartment goes on the end
            pRegionsCompartment = (ETRegionsCompartment)createAndAddCompartment( "ADRegionsCompartment", -1 );

            // Attach the regions compartment to this model element
            if (pModelElement != null)
            {
               pRegionsCompartment.addModelElement(pModelElement, -1);
            }

            if (pNameListCompartment != null)
            {
               IADNameCompartment pNameCompartment = pNameListCompartment.getNameCompartment();
               if (pNameCompartment != null)
               {
                  pNameCompartment.setHorizontalAlignment(IADCompartment.LEFT);
               }
            }
         }

         setDefaultCompartment(pNameListCompartment);

         // We are a container
         this.setIsGraphicalContainer(true);
         this.setContainmentType(ContainmentTypeEnum.CT_STATE_REGION);

         // Make sure the regions don't draw that line above them.  It makes the draw engine draw ugly
         pRegionsCompartment.setDrawTopLine(false);

         // Make sure we don't have a transition list compartment
         if (pTransitionListCompartment != null)
         {
            ICompartment pCompartment = (ICompartment) pTransitionListCompartment;
            removeCompartment(pCompartment);
         }
      }
   }

   protected IState getState()
   {
      IElement pElement = this.getFirstModelElement();
      return pElement instanceof IState ? (IState) pElement : null;
   }

   public int getProcedureOrTransitionType(IElement pElement)
   {
      int pType = IStateDrawEngine.SPTT_UNKNOWN;

      IState pState = this.getState();
      if (pState != null)
      {
         if (pElement instanceof IProcedure)
         {
            IProcedure pProcedure = (IProcedure) pElement;
            pType = IStateDrawEngine.SPTT_INVALID_PROCEDURE;

            /*
            IProcedure pEntry = pState.getEntry();
            if (pEntry != null)
            {
            	if (pEntry.isSame(pProcedure) == false)
            	{
            		IProcedure pExit = pState.getExit();
            		if (pExit != null)
            		{
            			if (pExit.isSame(pProcedure) == false)
            			{
            				IProcedure pDoActivity = pState.getDoActivity();
            				if (pDoActivity != null)
            				{
            					if (pDoActivity.isSame(pProcedure) == true)
            					{
            						pType = IStateDrawEngine.SPTT_DOACTIVITY;
            					}
            				}
            			}
            			else
            			{
            				// We have an exit
            				pType = IStateDrawEngine.SPTT_EXIT;
            			}
            		}
            	}
            	else
            	{
            		// We have an entry
            		pType = IStateDrawEngine.SPTT_ENTRY;
            	}
            }
            	*/
            IProcedure pEntry = pState.getEntry();
            IProcedure pExit = pState.getExit();
            IProcedure pDoActivity = pState.getDoActivity();
            if (pEntry != null && pEntry.isSame(pProcedure))
            {
               pType = IStateDrawEngine.SPTT_ENTRY;
            }
            else if (pExit != null && pExit.isSame(pProcedure))
            {
               pType = IStateDrawEngine.SPTT_EXIT;
            }
            else if (pDoActivity != null && pDoActivity.isSame(pProcedure))
            {
               pType = IStateDrawEngine.SPTT_DOACTIVITY;
            }
         }
         else if (pElement instanceof ITransition)
         {
            ITransition pTransition = (ITransition) pElement;
            boolean bIsInternal = pTransition.getIsInternal();
            if (bIsInternal == true)
            {
               ETList < ITransition > pTransitions = pState.getIncomingTransitions();
               if (pTransitions != null && pTransitions.size() > 0)
               {
                  if (pTransitions.contains(pTransition) == true)
                  {
                     pType = IStateDrawEngine.SPTT_INCOMING_TRANSITION;
                  }
                  else
                  {
                     pTransitions = pState.getOutgoingTransitions();
                     if (pTransitions != null)
                     {
                        if (pTransitions.contains(pTransition) == true)
                        {
                           pType = IStateDrawEngine.SPTT_OUTGOING_TRANSITION;
                        }
                     }
                  }
               }
            }
         }
      }

      return pType;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getElementType()
    */
   public String getElementType()
   {
      String type = super.getElementType();
      if (type == null)
      {
         type = new String("State");
      }
      return type;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getDrawEngineID()
    */
   public String getDrawEngineID()
   {
      return "StateDrawEngine";
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#isDrawEngineValidForModelElement()
    */
   public boolean isDrawEngineValidForModelElement()
   {
      String metaType = getMetaTypeOfElement();
      return metaType != null && metaType.equals("State");
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initResources()
    */
   public void initResources()
   {
      setFillColor("statefill", 142, 203, 219);
      setLightGradientFillColor("statelightgradientfill", 220, 239, 244);
      setBorderColor("stateborder", Color.BLACK);
      super.initResources();
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initCompartments(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
    */
   public void initCompartments(IPresentationElement pPE)
   {
		//	We may get here with no compartments.  This happens if we've been created
		// by the user.  If we read from a file then the compartments have been pre-created and
		// we just need to initialize them.
		long numCompartments = getNumCompartments();
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

		IElement pModelElement = pPE.getFirstSubject();
		if (pModelElement != null)
		{
			//	nitialize the name list comparment
			INameListCompartment pNameListCompartment = getCompartmentByKind(INameListCompartment.class);
			if (pNameListCompartment != null)
			{
				pNameListCompartment.attach(pModelElement);

				boolean bIsSimpleState = isSimpleState();
				IADNameCompartment pNameCompartment = pNameListCompartment.getNameCompartment();
				if (pNameCompartment != null)
				{
					// Make sure the name compartment wraps
					pNameCompartment.setTextWrapping(bIsSimpleState);
					pNameCompartment.setVerticallyCenterText(bIsSimpleState);
				}
			}
      
			// Initialize the regions compartment
			IRegionsCompartment pZones = getCompartmentByKind(IRegionsCompartment.class);
			if (pZones != null)
			{
				pZones.addModelElement(pModelElement, -1);
			}

			// Initialize the transition list compartment
			IADStateEventsAndTransitionsListCompartment pADTransitionListCompartment = getCompartmentByKind(IADStateEventsAndTransitionsListCompartment.class);
			if (pADTransitionListCompartment != null)
			{
				pADTransitionListCompartment.addModelElement(pModelElement, -1);
				pADTransitionListCompartment.setName("Transitions");
			}
		}
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#createCompartments()
    */
   public void createCompartments() throws ETException
   {
      IETGraphObjectUI parentUI = this.getParent();

      if (parentUI.getOwner() != null)
      {
         if (parentUI.getModelElement() != null)
         {
            try
            {
               this.verifyCompartments();
            }
            catch (Exception e)
            {
               throw new ETException(ETStrings.E_CMP_CREATE_FAILED, e.getMessage());
            }
         }
         else // Create the default compartments
            {
            this.initCompartments(this.getPresentationElement());
         }
      }
   }

   /*
    * Draws the simple state case.
    */
   protected void drawSimpleState(IDrawInfo pDrawInfo)
   {
      // Fill background
      TSEGraphics graphics = pDrawInfo.getTSEGraphics();
      IETRect deviceBounds = pDrawInfo.getDeviceBounds();

      int x = deviceBounds.getLeft();
      int w = deviceBounds.getIntWidth();
      
      float centerX = (float)deviceBounds.getCenterX();
      GradientPaint paint = new GradientPaint(centerX,
                         deviceBounds.getBottom(),
                         getFillColor(),
                         centerX,
                         deviceBounds.getTop(),
                         getLightGradientFillColor());
    
      GDISupport.drawRoundRect(graphics, deviceBounds.getRectangle(), getZoomLevel(pDrawInfo), this.getBorderBoundsColor(), paint);

      // Draw compartments
//		TSConstRect localBounds = this.getLogicalBounds();
//      IListCompartment listCompartment = (IListCompartment) getCompartmentByKind(ETClassNameListCompartment.class);
//      if (listCompartment != null && listCompartment.getCompartments() != null)
//      {
//         Iterator < ICompartment > ETClassNameListCompartmentIterator = listCompartment.getCompartments().iterator();
//         while (ETClassNameListCompartmentIterator.hasNext())
//         {
//            ICompartment compartment = ETClassNameListCompartmentIterator.next();
//            IETSize compartmentSize = compartment.calculateOptimumSize(pDrawInfo, false);
//
//            if (compartment instanceof ETClassNameCompartment)
//            {
//               // It should be virtically centered, so it needs the top of the device bounds.
//               this.setLastDrawPointY(deviceBounds.getTop());
//            }
//            else if (compartment instanceof ETTaggedValuesCompartment)
//            {
//               // Watch out compartmentSize.getHeight is already in scaled device. Kevin.
//               this.setLastDrawPointY(transform.yToDevice(localBounds.getBottom() + compartmentSize.getHeight()));
//            }
//            else if (compartment instanceof ETStereoTypeCompartment)
//            {
//               this.setLastDrawPointY(deviceBounds.getTop());
//            }
//
//            IETRect compartmentDrawRect = new ETRect(x, this.getLastDrawPointY(), w, deviceBounds.getIntHeight());
//            compartment.draw(pDrawInfo, compartmentDrawRect);
//
//            // Advance to the next line 
//            this.updateLastDrawPointY(compartmentSize.getHeight());
//         }
//      }
		handleNameListCompartmentDraw(pDrawInfo,deviceBounds,MIN_NAME_SIZE_X,MIN_NAME_SIZE_Y,false,0);
		ICompartment pNameListCompartment = (ICompartment) getCompartmentByKind(ETClassNameListCompartment.class);
		ICompartment pTransitionListCompartment = (ICompartment) getCompartmentByKind(ETStateEventsAndTransitionsListCompartment.class);
		if (pTransitionListCompartment != null && pTransitionListCompartment.getVisible() == true)
		{
			IETSize nameSize = pNameListCompartment.calculateOptimumSize(pDrawInfo, false);
			graphics.setColor(getBorderBoundsColor());
			graphics.drawLine(deviceBounds.getLeft(), deviceBounds.getTop() + nameSize.getHeight(), deviceBounds.getLeft() + deviceBounds.getIntWidth(), deviceBounds.getTop() + nameSize.getHeight());
		}
   }
   /*
    * Draws the sub state machine.
    */
   protected void drawSubmachineState(IDrawInfo pDrawInfo)
   {
      Color fillColor = getBkColor();
      Color borderColor = getBorderBoundsColor();
      IETRect deviceBounds = pDrawInfo.getDeviceBounds();
      ICompartment pNameListCompartment = (ICompartment) getCompartmentByKind(ETClassNameListCompartment.class);
      ICompartment pTransitionListCompartment = (ICompartment) getCompartmentByKind(ETStateEventsAndTransitionsListCompartment.class);

      IETSize nameOptimumSize = pNameListCompartment != null ? pNameListCompartment.calculateOptimumSize(pDrawInfo, false) : new ETSize(0, 0);
      IETSize transitionOptimumSize = pTransitionListCompartment != null ? pTransitionListCompartment.calculateOptimumSize(pDrawInfo, false) : new ETSize(0, 0);

      // Get boundry
      int x = deviceBounds.getLeft();
      int w = deviceBounds.getIntWidth();
      int y = deviceBounds.getTop();
      int h = deviceBounds.getIntHeight();
      TSEGraphics graphics = pDrawInfo.getTSEGraphics();

      float centerX = (float)deviceBounds.getCenterX();
      GradientPaint paint = new GradientPaint(centerX,
                         deviceBounds.getBottom(),
                         fillColor,
                         centerX,
                         deviceBounds.getTop(),
                         getLightGradientFillColor());
    
      GDISupport.drawRoundRect(graphics, deviceBounds.getRectangle(), getZoomLevel(pDrawInfo), borderColor, paint);

//      // Draw compartments
//      if (pNameListCompartment != null && pTransitionListCompartment != null)
//      {
//         pNameListCompartment.draw(pDrawInfo, deviceBounds);
//
//         IETSize nameSize = pNameListCompartment.calculateOptimumSize(pDrawInfo, false);
//         graphics.setColor(borderColor);
//         graphics.drawLine(x, y + nameSize.getHeight(), x + w, y + nameSize.getHeight());
//
//         ICompartment pETStaticTextCompartment = (ICompartment) getCompartmentByKind(ETStaticTextCompartment.class);
//         IETSize textSize = pETStaticTextCompartment.calculateOptimumSize(pDrawInfo, false);
//         IETRect staticTextRect = new ETRect(x, y + nameSize.getHeight(), w, h - nameSize.getHeight());
//         pETStaticTextCompartment.draw(pDrawInfo, staticTextRect);
//
//         IETRect eventsRect = new ETRect(x, y + nameSize.getHeight() + textSize.getHeight(), w, h - nameSize.getHeight() - textSize.getHeight());
//         pTransitionListCompartment.draw(pDrawInfo, eventsRect);
//         //graphics.setColor(Color.RED);
//         //graphics.drawRect(eventsRect.getLeft(), eventsRect.getTop(), eventsRect.getIntWidth(), eventsRect.getIntHeight());
//      }
		handleNameListCompartmentDraw(pDrawInfo,deviceBounds,MIN_NAME_SIZE_X,MIN_NAME_SIZE_Y,false,0);
		if (pTransitionListCompartment != null && pTransitionListCompartment.getVisible() == true)
		{
			IETSize nameSize = pNameListCompartment.calculateOptimumSize(pDrawInfo, false);
			graphics.setColor(borderColor);
			graphics.drawLine(x, y + nameSize.getHeight(), x + w, y + nameSize.getHeight());
		}
   }

   /*
    * Draws the composite state.
    */
   protected void drawComposite(IDrawInfo pDrawInfo)
   {
      // Similar to a package
      //
      //       --------------
      //       |            |
      //       |    Name    | 
      //       |            | 
      //      /----------------------------------------\   
      //     /                                          \
      //    |                                           |
      //    |                                           |
      //    |                                           |
      //    |                                           |
      //     \                                         /
      //       \--------------------------------------/
      //

      IETRect boundingRect = pDrawInfo.getDeviceBounds();
      IETRect nameRect = (IETRect) boundingRect.clone();
      IETRect boxRect = (IETRect) boundingRect.clone();
      ICompartment pNameListCompartment = (ICompartment) getCompartmentByKind(ETClassNameListCompartment.class);
      ICompartment pRegionsCompartment = (ICompartment) getCompartmentByKind(ETRegionsCompartment.class);

      IETSize nameOptimumSize = pNameListCompartment != null ? pNameListCompartment.calculateOptimumSize(pDrawInfo, false) : new ETSize(0, 0);
      // IETSize regionsOptimumSize = pRegionsCompartment != null ? pRegionsCompartment.calculateOptimumSize(pDrawInfo, false) : new ETSize(0, 0);

      nameOptimumSize.setWidth(Math.max(nameOptimumSize.getWidth(), MIN_NAME_SIZE_X));
      nameOptimumSize.setWidth((int) Math.min(nameOptimumSize.getWidth(), boundingRect.getWidth()));

      nameOptimumSize.setHeight(Math.max(nameOptimumSize.getHeight(), MIN_NAME_SIZE_Y));
      nameOptimumSize.setHeight((int) Math.min(nameOptimumSize.getHeight(), boundingRect.getWidth()));

      nameRect.setBottom(Math.min((nameRect.getTop() + nameOptimumSize.getHeight()), boundingRect.getBottom()));
      nameRect.setRight(Math.min((nameRect.getLeft() + nameOptimumSize.getWidth()), boundingRect.getRight()));

      boxRect.setTop(nameRect.getBottom());
      boxRect.setBottom(boundingRect.getBottom());

      // Draw the tab on the top
      if ((nameRect.getLeft() + NAME_COMPARTMENT_LEFT_OFFSET) < nameRect.getRight())
      {
         nameRect.setLeft(nameRect.getLeft() + NAME_COMPARTMENT_LEFT_OFFSET);
         nameRect.setRight(nameRect.getRight() + NAME_COMPARTMENT_LEFT_OFFSET);
      }

      Color fillColor = getBkColor();
      Color borderColor = getBorderBoundsColor();

      float centerX = (float)nameRect.getCenterX();
      GradientPaint paint = new GradientPaint(centerX,
                         nameRect.getBottom(),
                         fillColor,
                         centerX,
                         nameRect.getTop(),
                         getLightGradientFillColor());
    
      GDISupport.drawRectangle(pDrawInfo.getTSEGraphics(), nameRect.getRectangle(), borderColor, paint);

      // Draw the rectangle on the bottom
      boxRect.setTop(boxRect.getTop() - 1);
      centerX = (float)boxRect.getCenterX();
      paint = new GradientPaint(centerX,
                         boxRect.getBottom(),
                         fillColor,
                         centerX,
                         boxRect.getTop(),
                         getLightGradientFillColor());
    
      GDISupport.drawRoundRect(pDrawInfo.getTSEGraphics(), boxRect.getRectangle(), getZoomLevel(pDrawInfo), borderColor, paint);

      // Draw each compartment
      //handleNameListCompartmentDraw(pDrawInfo, boundingRect);
//      this.handleNameListCompartmentDraw(pDrawInfo, boundingRect, MIN_NAME_SIZE_X, MIN_NAME_SIZE_Y, true, NAME_COMPARTMENT_LEFT_OFFSET);
     handleNameListCompartmentDrawForContainers(pDrawInfo, boundingRect, MIN_NAME_SIZE_X, MIN_NAME_SIZE_Y, true, NAME_COMPARTMENT_LEFT_OFFSET);
      // Now draw the zones..
      if (pRegionsCompartment != null)
      {
			pRegionsCompartment.draw(pDrawInfo, boxRect);
      }      
   }

   protected TSConstRect getLogicalBounds()
   {
      IETGraphObjectUI ui = this.getParent();
      return ui != null && ui.getOwner() != null ? ui.getBounds() : null;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#doDraw(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo)
    */
   public void drawContents(IDrawInfo pDrawInfo)
   {
      IETNodeUI parentUI = (IETNodeUI) getParent();

      // draw yourself only if you have an owner
      if (parentUI != null && parentUI.getOwner() != null && !parentUI.isTransparent() && parentUI.isBorderDrawn())
      {
         TSEGraphics graphics = pDrawInfo.getTSEGraphics();

         // Fix J2476:  This code was in C++, so I (BDB) added it here to fix the issue when reopening diagrams with composite states
         // UPDATE:  There is still a problem where the state resizes itself to be larger.
         // Verify we have the correct compartments
         try
         {
            verifyCompartments();
         }
         catch( ETException e )
         {
         }
         
         // Draw state			
         if (isSimpleState())
         {
            drawSimpleState(pDrawInfo);
         }
         else if (isComposite())
         {
            drawComposite(pDrawInfo);
         }
         else if (isSubmachineState())
         {
            drawSubmachineState(pDrawInfo);
         }
      }
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#calculateOptimumSize(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, boolean)
    */
   public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
   {
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

      if (bAt100Pct || retVal == null)
         return retVal;
      else
      {
         return scaleSize(retVal, pDrawInfo != null ? pDrawInfo.getTSTransform() : getTransform());
      }
   }

   public long writeToArchive(IProductArchive pProductArchive, IProductArchiveElement pElement)
   {
   	long retVal = super.writeToArchive(pProductArchive, pElement);
      IProductArchiveElement cpEngineElement = pElement.getElement(IProductArchiveDefinitions.ENGINENAMEELEMENT_STRING);
      if (cpEngineElement != null)
      {
         cpEngineElement.addAttributeLong(IProductArchiveDefinitions.SHOWEVENTSANDTRANSITIONS_BOOL, m_bShowEventsAndTransitions);
      }
      return retVal;
   }

   public long readFromArchive(IProductArchive pProductArchive, IProductArchiveElement pParentElement)
   {
      long retVal = super.readFromArchive(pProductArchive, pParentElement);
      m_bShowEventsAndTransitions = (int) pParentElement.getAttributeLong(IProductArchiveDefinitions.SHOWEVENTSANDTRANSITIONS_BOOL);
      return retVal;
   }

   public String getDrawEngineMatchID()
   {
      String sTempID = this.getDrawEngineID();

      if (isSimpleState())
      {
         sTempID += "Simple";
      }
      else if (isComposite())
      {
         sTempID += "Composite";
      }
      else if (isSubmachineState())
      {
         sTempID += "Submachine";
      }

      return sTempID;
   }

   public ICompartment getDefaultCompartment()
   {
      ICompartment pCompartment = null;
      IADClassNameListCompartment pNameListCompartment = getCompartmentByKind(IADClassNameListCompartment.class);
      if (pNameListCompartment != null)
      {
         IADNameCompartment pNameCompartment = (IADNameCompartment) pNameListCompartment.getNameCompartment();
         if (pNameCompartment != null)
         {
            this.setDefaultCompartment(pNameCompartment);
            pCompartment = (ICompartment) pNameCompartment;
         }
      }

      return pCompartment;
   }

   public void onContextMenu(IMenuManager manager)
   {
      // Create the context button handler on demand
      if (isSimpleState() || isSubmachineState())
      {
         if (m_bShowEventsAndTransitions == SETK_NO)
         {
            addEventTransitionMenuItems(manager, true);
         }
         else if (m_bShowEventsAndTransitions == SETK_YES)
         {
            addEventTransitionMenuItems(manager, false);
         }
      }
      super.onContextMenu(manager);
   }

   public boolean onHandleButton(ActionEvent e, String id)
   {
      boolean handled = handleStandardLabelSelection(e, id);
      try
      {
         if (!handled && id != null)
         {
            if (id.equals("MBK_STATE_EVENT_TRANSITIONS_COMPARTMENT_SHOW"))
            {
               m_bShowEventsAndTransitions = SETK_YES;
               verifyCompartments();
               delayedSizeToContents();
               handled = true;
            }
            else if (id.equals("MBK_STATE_EVENT_TRANSITIONS_COMPARTMENT_HIDE"))
            {
               m_bShowEventsAndTransitions = SETK_NO;
               verifyCompartments();
               delayedSizeToContents();
               handled = true;
            }
         }

         if (!handled)
         {
            handled = super.onHandleButton(e, id);
         }
      }
      catch (Exception ex)
      {
         handled = false;
      }

      return handled;
   }

   public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pClass)
   {
      boolean bFlag = isParentDiagramReadOnly() ? false : true;
      return bFlag;
   }

   protected void addEventTransitionMenuItems(IMenuManager manager, boolean bShow)
   {
      if (bShow)
      {
         manager.add(createMenuAction(loadString("IDS_POPUPMENU_STATE_EVENT_TRANSITIONS_COMPARTMENT_SHOW"), "MBK_STATE_EVENT_TRANSITIONS_COMPARTMENT_SHOW"));
      }
      else
      {
         manager.add(createMenuAction(loadString("IDS_POPUPMENU_STATE_EVENT_TRANSITIONS_COMPARTMENT_HIDE"), "MBK_STATE_EVENT_TRANSITIONS_COMPARTMENT_HIDE"));
      }
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
        
        protected int getNumSelectableCompartments() {
            // The selectable compartments include all of zone compartments,
            // plus the NameListCompartment.
            int retVal = 0;
            IADZonesCompartment cpZones = getCompartmentByKind(IADZonesCompartment.class);            
            if (cpZones != null) {
                retVal = cpZones.getCompartments().size() + 1;
            }
            else if (cpZones == null) {
                IADStateEventsAndTransitionsListCompartment evtAndTrans = getCompartmentByKind(IADStateEventsAndTransitionsListCompartment.class);
                if (evtAndTrans != null) {
                    retVal = evtAndTrans.getCompartments().size() + 1;
                }
                else
                    retVal = retVal + 1;
            }                
            return retVal;
        }
        
        protected ICompartment getSelectableCompartment(int index) {
            ICompartment retVal = null;
            
            ArrayList < ICompartment > ownedCompartments = new ArrayList < ICompartment >();
            
            INameListCompartment nameCompartment = getCompartmentByKind( INameListCompartment.class );
            ownedCompartments.add(nameCompartment);
            
            IADZonesCompartment cpZones = getCompartmentByKind(IADZonesCompartment.class);            
            if (cpZones != null) {
                for(ICompartment curCompartment : cpZones.getCompartments()) {
                    ownedCompartments.add(curCompartment);
                }
            }
            else if (cpZones == null) {
                IADStateEventsAndTransitionsListCompartment evtAndTrans = getCompartmentByKind(IADStateEventsAndTransitionsListCompartment.class);
                if (evtAndTrans != null) {
                    for(ICompartment evtCompartment : evtAndTrans.getCompartments()) {
                        ownedCompartments.add(evtCompartment);
                    }
                }
            }
            return ownedCompartments.get(index);
        }

}
