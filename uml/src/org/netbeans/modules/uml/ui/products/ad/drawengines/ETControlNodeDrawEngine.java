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
import java.awt.event.ActionEvent;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IJoinNode;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IMergeNode;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.ADNodeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETBoxCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IBoxCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IEllipseCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ISupportEnums;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelKind;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;

/**
 * @author KevinM
 *
 */
public class ETControlNodeDrawEngine extends ADNodeDrawEngine
{

   public static final long FORK_NODE_HEIGHT = 80;
   public static final long FORK_NODE_WIDTH = 7;
   public static final long DECISION_NODE_HEIGHT = 30;
   public static final long DECISION_NODE_WIDTH = 20;
   public static final long OTHER_NODES_HEIGHT = 16;
   public static final long OTHER_NODES_WIDTH = 16;
   public static final long FLOW_FINAL_NODE_HEIGHT = 27;
   public static final long FLOW_FINAL_NODE_WIDTH = 27;

   /**
    * 
    */
   public ETControlNodeDrawEngine()
   {
      super();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#calculateOptimumSize(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, boolean)
    */
   public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
   {
      try
      {
         ETPairT < Long, Long > widthAndHeight = getOptimalHeightAndWidth();
         long nWidth = widthAndHeight.getParamOne().longValue();
         long nHeight = widthAndHeight.getParamTwo().longValue();

         IETSize retVal = new ETSize((int)nWidth, (int)nHeight);
         if (!bAt100Pct)
         {
            retVal = this.scaleSize(retVal, pDrawInfo != null ? pDrawInfo.getTSTransform() : getTransform());
         }

         return retVal;
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#doDraw(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo)
    */
   public void doDraw(IDrawInfo pDrawInfo)
   {
      /// Draw all the compartments
      dispatchDrawToCompartments(pDrawInfo, pDrawInfo.getDeviceBounds());

      // This will draw an invalid frame around the node if it doesn't have an IElement
      drawInvalidRectangle(pDrawInfo);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getDrawEngineID()
    */
   public String getDrawEngineID()
   {
      return "ControlNodeDrawEngine";
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#sizeToContents()
    */
   public void sizeToContents()
   {
      try
      {
         ETPairT < Long, Long > widthAndHeight = getOptimalHeightAndWidth();
         long nWidth = widthAndHeight.getParamOne().longValue();
         long nHeight = widthAndHeight.getParamTwo().longValue();

         //sizeToContentsWithMin(nWidth, nHeight, true, false);
         sizeToContentsWithMin(nWidth, nHeight, false, false);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /* (non-Javadoc)
    * @see java.lang.Object#clone()
    */
   public Object clone()
   {
      // TODO Auto-generated method stub
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
         String currentMetaType = getMetaTypeOfElement();
         if (currentMetaType == null)
            return;

         if (currentMetaType.equals("ActivityFinalNode") || currentMetaType.equals("FlowFinalNode") || currentMetaType.equals("InitialNode"))
         {
            createAndAddCompartment("EllipseCompartment", 0);
         }
         else if (currentMetaType.equals("ForkNode") || currentMetaType.equals("JoinNode") || currentMetaType.equals("JoinForkNode"))
         {
            // Both of these are very skinny, filled rectangles
            createAndAddCompartment("BoxCompartment", 0);
            initResources();
         }
         else if (currentMetaType.equals("DecisionNode") || currentMetaType.equals("MergeNode") || currentMetaType.equals("DecisionMergeNode"))
         {
            // Diamond
            createAndAddCompartment("BoxCompartment", 0);
            initResources();
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

         String currentMetaType = getMetaTypeOfElement();
         if (currentMetaType == null)
            return;

         IEllipseCompartment pEllipseCompartment = (IEllipseCompartment)getCompartmentByKind(IEllipseCompartment.class);
         IBoxCompartment pBoxCompartment = (IBoxCompartment)getCompartmentByKind(IBoxCompartment.class);
         if (pEllipseCompartment != null)
         {
            // Draw depends on the metatype
            if (currentMetaType.equals("ActivityFinalNode"))
            {
               // ActivityFinalNode - Two circles, one inside each other.  The inside one filled black.
               pEllipseCompartment.setEllipseKind(ISupportEnums.EK_CIRCLE_INSIDE_CIRCLE_CENTER_FILLED);
            }
            else if (currentMetaType.equals("FlowFinalNode"))
            {
               // FlowFinalNode - circle with an X
               pEllipseCompartment.setEllipseKind(ISupportEnums.EK_CIRCLE_WITH_X);
            }
            else if (currentMetaType.equals("InitialNode"))
            {
               // InitialNode - Filled circle
               pEllipseCompartment.setEllipseKind(ISupportEnums.EK_CIRCLE_INSIDE_FILLED);
            }
            else
            {
               pEllipseCompartment.setEllipseKind(ISupportEnums.EK_UNKNOWN);
            }

            // These objects should not be resizable
            if (getNodeUI() != null)
            {
               getNodeUI().setResizable(false);
            }
         }
         else if (pBoxCompartment != null)
         {
            if (currentMetaType.equals("ForkNode") || currentMetaType.equals("JoinNode") || currentMetaType.equals("JoinForkNode"))
            {
               pBoxCompartment.setBoxKind(ISupportEnums.BK_SIMPLE_FILLED_BOX);
            }
            else if (currentMetaType.equals("DecisionNode") || currentMetaType.equals("MergeNode") || currentMetaType.equals("DecisionMergeNode"))
            {
               pBoxCompartment.setBoxKind(ISupportEnums.BK_DIAMOND);
            }
            // These objects should be resizable
            if (getNodeUI() != null)
            {
               getNodeUI().setResizable(true);
            }
         }

         // Since we don't resize the compartments we need to invalidate the bounds.
         invalidate();
         getDiagram().refresh(true);

      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initResources()
    */
   public void initResources()
   {
      String currentMetaType = getMetaTypeOfElement();

      // Set the fill and border color
      Color nFillColor = null;
      Color nLightFillColor = null;
      Color nBorderColor = Color.BLACK;
      String sFillResource = "boxfill";
      String sLightFillResource = "boxlightgradientfill";
      String sBorderResource = "boxborder";

      if (currentMetaType != null)
      {
         if (currentMetaType.equals("ForkNode") || currentMetaType.equals("JoinNode") || currentMetaType.equals("JoinForkNode"))
         {
            sFillResource = "forkfill";
            sLightFillResource = "forklightgradientfill";
            sBorderResource = "forkborder";
            nLightFillColor = new Color(191, 235, 235);
            nFillColor = new Color(50, 199, 199);
         }
         else if (currentMetaType.equals("InitialNode"))
         {
            sFillResource = "initialnodefill";
            sLightFillResource = "initialnodelightgradientfill";
            sBorderResource = "initialnodeborder";
            nLightFillColor = new Color(116, 189, 136);
            nFillColor = new Color(26, 120, 51);
         }
         else if (currentMetaType.equals("ActivityFinalNode"))
         {
            sFillResource = "activityfinalfill";
            sLightFillResource = "activityfinallightgradientfill";
            sBorderResource = "activityfinalborder";
            nLightFillColor = new Color(255, 225, 217);
            nFillColor = new Color(255, 51, 0);
         }
         else if (currentMetaType.equals("FlowFinalNode"))
         {
            sFillResource = "flowfinalfill";
            sLightFillResource = "flowfinallightgradientfill";
            sBorderResource = "flowfinalborder";
            nLightFillColor = new Color(255, 225, 217);
            nFillColor = new Color(255, 51, 0);
         }
         else if (currentMetaType.equals("DecisionMergeNode") || currentMetaType.equals("DecisionNode") || currentMetaType.equals("MergeNode"))
         {
            sFillResource = "mergefill";
            sLightFillResource = "mergelightgradientfill";
            sBorderResource = "mergeborder";
            nLightFillColor = new Color(255, 255, 255);
            nFillColor = new Color(167, 240, 240);
         }
      }

      if (nBorderColor != null)
      {
         setBorderColor(sBorderResource, nBorderColor);
      }

      // Now set the resource
      if (nFillColor != null)
      {
         setFillColor(sFillResource, nFillColor);
         setLightGradientFillColor(sLightFillResource, nLightFillColor);
      }

      // Get the box compartment and initialize it with the fill and border resources
		IBoxCompartment pBoxCompartment = getCompartmentByKind(IBoxCompartment.class);
//		IEllipseCompartment pEllipseCompartment = getCompartmentByKind(IEllipseCompartment.class);
		if (pBoxCompartment != null)
		{
			((ETBoxCompartment)pBoxCompartment).initBoxResources(sFillResource, sLightFillResource, sBorderResource);
		}
//		if (pEllipseCompartment != null)
//		{
//			pEllipseCompartment.initEllipseResources(sFillResource, sBorderResource));
//		}

      // Now call the base class so it can setup any string ids we haven't already set
      super.initResources();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#isDrawEngineValidForModelElement()
    */
   public boolean isDrawEngineValidForModelElement()
   {
      String currentMetaType = getMetaTypeOfElement();
      return currentMetaType != null
         && (currentMetaType.equals("DecisionNode")
            || currentMetaType.equals("FlowFinalNode")
            || currentMetaType.equals("ForkNode")
            || currentMetaType.equals("JoinForkNode")
            || currentMetaType.equals("InitialNode")
            || currentMetaType.equals("JoinNode")
            || currentMetaType.equals("MergeNode")
            || currentMetaType.equals("DecisionMergeNode")
            || currentMetaType.equals("ActivityFinalNode"));
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#setupOwner()
    */
   public void setupOwner()
   {
      this.sizeToContents();
      super.setupOwner();
   }

   public String getElementType()
   {
      String type = super.getElementType();
      if (type == null)
      {
         type = new String("Control Class");
      }
      return type;
   }

   /* (non-Javadoc)	
    * @see org.netbeans.modules.uml.ui.products.ad.drawengines.ETDrawEngine#getManagerType(int)	
    */
   public String getManagerMetaType(int pManagerType)
   {
      if (pManagerType == MK_EVENTMANAGER)
      {
         IElement pModelElement = this.getFirstModelElement();
         if (pModelElement instanceof IJoinNode || pModelElement instanceof IMergeNode)
         {
            return "JoinForkNodeEventManager";
         }
      }
      else if (pManagerType == MK_LABELMANAGER)
      {
         return "SimpleStereotypeAndNameLabelManager";
      }
      return null;
   }

   /**
    * Returns the optimal height and width based on the initialization string
    *
    * @param nWidth [in,out] The width of the node as it is being created
    * @param nHeight [in,out] The height of the node as it is being created
    */
   protected ETPairT < Long, Long > getOptimalHeightAndWidth()
   {
      long nWidth = OTHER_NODES_WIDTH;
      long nHeight = OTHER_NODES_HEIGHT;
      try
      {
         boolean bUseInitString = true;

         IElement pElement = this.getFirstModelElement();
         if (pElement != null)
         {
            String currentMetaType = getMetaTypeOfElement();
            if (currentMetaType != null
               && currentMetaType.equals("ForkNode")
               || currentMetaType.equals("JoinNode")
               || currentMetaType.equals("JoinForkNode")
               || currentMetaType.equals("DecisionNode")
               || currentMetaType.equals("DecisionMergeNode")
               || currentMetaType.equals("MergeNode")
               || currentMetaType.equals("FlowFinalNode")
               || currentMetaType.equals("ActivityFinalNode"))
            {
               bUseInitString = true;
            }
            else
            {
               bUseInitString = false;
            }
         }

         if (bUseInitString)
         {
            // Get it from the initialization string
            String sInitString = getInitializationString();
            if (sInitString != null && sInitString.length() > 0)
            {
               if (sInitString.indexOf("Horizontal") >= 0)
               {
                  nWidth = FORK_NODE_HEIGHT;
                  nHeight = FORK_NODE_WIDTH;
               }
               else if (sInitString.indexOf("ActivityFinalNode") >= 0)
               {
                  nWidth = FLOW_FINAL_NODE_HEIGHT;
                  nHeight = FLOW_FINAL_NODE_WIDTH;
               }
               else if (sInitString.indexOf("ForkNode") >= 0 || sInitString.indexOf("JoinNode") >= 0 || sInitString.indexOf("JoinForkNode") >= 0)
               {
                  nWidth = FORK_NODE_WIDTH;
                  nHeight = FORK_NODE_HEIGHT;
               }
               else if (sInitString.indexOf("DecisionNode") >= 0)
               {
                  nWidth = DECISION_NODE_WIDTH;
                  nHeight = DECISION_NODE_HEIGHT;
               }
               else
               {
                  nWidth = OTHER_NODES_WIDTH;
                  nHeight = OTHER_NODES_HEIGHT;
               }
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return new ETPairT < Long, Long > (new Long(nWidth), new Long(nHeight));
   }

   /** 
    * Returns true if this node is a horizontal join or merge node
    *
    * @return true if this is a horizontal join or merge node.
    */
   protected boolean isHorizontalJoinOrMergeNode()
   {
      boolean bIsHorizontal = false;

      try
      {
         String currentMetaType = getMetaTypeOfElement();
         if (currentMetaType.equals("ForkNode") || currentMetaType.equals("JoinNode") || currentMetaType.equals("JoinForkNode"))
         {
            if (this.getNodeUI() != null)
            {
               TSConstRect rect = getNodeUI().getBounds();

               if (rect.getWidth() > rect.getHeight())
               {
                  bIsHorizontal = true;
               }
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return bIsHorizontal;
   }

   public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pClass)
   {
      boolean bFlag = handleStandardLabelSensitivityAndCheck(id, pClass);
      if (!bFlag)
      {
         if (id.equals("MBK_SHOW_NAME_LABEL"))
         {
            ILabelManager labelMgr = getLabelManager();
            if (labelMgr != null)
            {
               boolean isDisplayed = labelMgr.isDisplayed(TSLabelKind.TSLK_NAME);
               pClass.setChecked(isDisplayed);
               bFlag = isParentDiagramReadOnly() ? false : true;
            }
         }
         else
         {
            bFlag = super.setSensitivityAndCheck(id, pClass);
         }
      }

      return bFlag;
   }

   public boolean onHandleButton(ActionEvent e, String id)
   {
      boolean handled = handleStandardLabelSelection(e, id);
      if (!handled)
      {
         if (id.equals("MBK_SHOW_NAME_LABEL"))
         {
            ILabelManager labelMgr = getLabelManager();
            if (labelMgr != null)
            {
               boolean isDisplayed = labelMgr.isDisplayed(TSLabelKind.TSLK_NAME);
               labelMgr.showLabel(TSLabelKind.TSLK_NAME, isDisplayed ? false : true);
               invalidate();
            }
            handled = true;
         }
      }
      if (!handled)
      {
         handled = super.onHandleButton(e, id);
      }
      return handled;
   }

   public void onContextMenu(IMenuManager manager)
   {

      addControlNodeMenuItems(manager);

      super.onContextMenu(manager);
   }

   protected void addControlNodeMenuItems(IMenuManager manager)
   {
      IMenuManager subMenu = manager.createOrGetSubMenu(loadString("IDS_LABELS_TITLE"), "");

      if (subMenu != null)
      {
         subMenu.add(createMenuAction(loadString("IDS_NAME_LABEL"), "MBK_SHOW_NAME_LABEL"));
      }
   }
}
