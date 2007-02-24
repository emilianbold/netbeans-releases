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
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.geom.GeneralPath;

import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IPseudoState;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IPseudostateKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.ReconnectEdgeCreateConnectorKind;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNodeUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelKind;
import com.tomsawyer.editor.TSESolidObject;
import com.tomsawyer.editor.graphics.TSEGraphics;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;
import java.awt.Dimension;

/**
 * @author jingmingm
 *
 */
public class ETPseudoStateDrawEngine extends ETNodeDrawEngine
{
   private boolean bHorizontal = false;
   
   private static final int DEFAULT_WIDTH  = 22;
   private static final int DEFAULT_HEIGHT = 22;
   private static final int FORK_NODE_HEIGHT = 80;
   private static final int FORK_NODE_WIDTH = 7;
   private static final int DECISION_NODE_WIDTH = 20;
   private static final int DECISION_NODE_HEIGHT = 30;
   
   public String getElementType()
   {
      String type = super.getElementType();
      if (type == null)
      {
         type = new String("PseudoState");
      }
      return type;
   }
   
   public void initResources()
   {
      // Set the fill and border colors
      String sFontResource = "";
      String sFillResource = "boxfill";
      String sLightFillResource = null;
      String sBorderResource = "boxborder";
      Color nFillColor = null;
      Color nLightFillColor = null;
      Color nBorderColor = null;
      
      int nPSKind = getPsuedoKindFromParameter((ETGenericNodeUI)this.getParent());
      //setPseudoState(nPSKind);
      
      // Please change this to a switch statement.
      if (nPSKind == IPseudostateKind.PK_CHOICE)
      {
         sFillResource = "choicestatefill";
         sLightFillResource = "choicestatelightgradientfill";
         sBorderResource = "choicestateborder";
         nFillColor = new Color(167, 240, 240);
         nLightFillColor = new Color(255, 255, 255);
      }
      else if (nPSKind == IPseudostateKind.PK_DEEPHISTORY)
      {
         sFontResource = "deephistorystatefont";
         sFillResource = "deephistorystatefill";
         sLightFillResource = "deephistorystatelightgradientfill";
         sBorderResource = "deephistorystateborder";
         nFillColor = new Color(101, 154, 210);
         nLightFillColor = new Color(255, 255, 255);
      }
      else if (nPSKind == IPseudostateKind.PK_SHALLOWHISTORY)
      {
         sFontResource = "shallowhistorystatefont";
         sFillResource = "shallowhistorystatefill";
         sLightFillResource = "shallowhistorystatelightgradientfill";
         sBorderResource = "shallowhistorystateborder";
         nFillColor = new Color(255, 255, 0);
         nLightFillColor = new Color(255, 255, 255);
      }
      else if (nPSKind == IPseudostateKind.PK_INITIAL)
      {
         sFillResource = "initialstatefill";
         sLightFillResource = "initialstatelightgradientfill";
         sBorderResource = "initialstateborder";
         nFillColor = new Color(0, 146, 0);
         nLightFillColor = new Color(238, 247, 238);
      }
      else if (nPSKind == IPseudostateKind.PK_JOIN ||
      nPSKind == IPseudostateKind.PK_FORK)
      {
         sFillResource = "joinstatefill";
         sBorderResource = "joinstateborder";
      }
      else if (nPSKind == IPseudostateKind.PK_JUNCTION)
      {
         sFillResource = "junctionstatefill";
         sLightFillResource = "junctionstatelightgradientfill";
         sBorderResource = "junctionstateborder";
         nFillColor = new Color(102, 102, 204);
         nLightFillColor = new Color(255, 255, 255);
      }
      else if (nPSKind == IPseudostateKind.PK_ENTRYPOINT)
      {
         sFillResource = "entrypointstatefill";
         sLightFillResource = "entrypointstatelightgradientfill";
         sBorderResource = "entrypointstateborder";
         nFillColor = new Color(255, 153, 51);
         nLightFillColor = new Color(255, 255, 255);
      }
      else if (nPSKind == IPseudostateKind.PK_STOP)
      {
         sFillResource = "stopstatefill";
         sLightFillResource = "stopstatelightgradientfill";
         sBorderResource = "stopstateborder";
         nFillColor = new Color(255, 51, 0);
         nLightFillColor = new Color(255, 255, 255);
      }
      
      setFillColor(sFillResource, nFillColor);
      if (sLightFillResource != null) {
          setLightGradientFillColor(sLightFillResource, nLightFillColor);
      }
      setBorderColor(sBorderResource, nBorderColor);
      
      super.initResources();
   }
   
   public void doDraw(IDrawInfo pDrawInfo)
   {
      IETNodeUI parentUI = (IETNodeUI)this.getParent();
      
      // draw yourself only if you have an owner
      if (parentUI.getOwner() != null)
      {
         TSEGraphics graphics = pDrawInfo.getTSEGraphics();
         IETRect deviceRect = pDrawInfo.getDeviceBounds();
         
         if (!parentUI.isTransparent())
         {
            // Get pseudo state kind
            int kind = getPseudostateKind();
            if (parentUI.getModelElement() != null)
            {
               IElement element = parentUI.getModelElement();
               IPseudoState pseudoState = (IPseudoState)element;
               
               //kind = getPseudostateKind();
               
               // Set pseudo state kind
               BaseElement baseElement = (BaseElement)element;
               //					baseElement.setPseudostateKind("kind", kind);
            }
            
            // Draw
            Color fillColor = getBkColor();
            Color borderColor = getBorderBoundsColor();
            switch(kind)
            {
               case IPseudostateKind.PK_FORK:
               case IPseudostateKind.PK_JOIN:
               {
                  graphics.setColor(fillColor);
                  GDISupport.fillRectangle(graphics,deviceRect.getRectangle());
                  break;
               }
               case IPseudostateKind.PK_CHOICE:
               {
                  ETList<IETPoint> diamondPts = GDISupport.getDiamondPolygonPoints(graphics, deviceRect.getRectangle());
                  float centerX = (float)deviceRect.getCenterX();
                  GradientPaint paint = new GradientPaint(centerX,
                                 deviceRect.getBottom(),
                                 fillColor,
                                 centerX,
                                 deviceRect.getTop(),
                                 getLightGradientFillColor());
            
			      GDISupport.drawPolygon(graphics, diamondPts, borderColor, paint);
                  break;
               }
               case IPseudostateKind.PK_DEEPHISTORY:
               {
                  float centerX = (float)deviceRect.getCenterX();
                  GradientPaint paint = new GradientPaint(centerX,
                                 deviceRect.getBottom(),
                                 fillColor,
                                 centerX,
                                 deviceRect.getTop(),
                                 getLightGradientFillColor());
            
                  GDISupport.drawEllipse(graphics, deviceRect.getRectangle(), borderColor, paint);
                  
                  try
                  {
                     ICompartment nameCompartment = ETDrawEngineFactory.createCompartment(ETDrawEngineFactory.CLASS_NAME_COMPARTMENT);
                     nameCompartment.setEngine(this);
                     nameCompartment.initResources();
                     nameCompartment.setName(new String("H*"));
                     nameCompartment.setFontString("Arial-12");
                     
                     int X = deviceRect.getLeft();
                     int Y = deviceRect.getTop();
                     int H = deviceRect.getIntHeight();
                     int W = deviceRect.getIntWidth();
                     
                     IETSize compartmentSize = nameCompartment.calculateOptimumSize(pDrawInfo, false);
                     ETRect compartmentRect = new ETRect(X, (int)(Y + H/2 - graphics.getFontMetrics().getHeight()/2), W, compartmentSize.getHeight());
                     nameCompartment.draw(pDrawInfo, compartmentRect);
                  }
                  catch (Exception e)
                  {
                  }
                  break;
               }
               case IPseudostateKind.PK_INITIAL:
               {
                  float centerX = (float)deviceRect.getCenterX();
                  GradientPaint paint = new GradientPaint(centerX,
                                 deviceRect.getBottom(),
                                 fillColor,
                                 centerX,
                                 deviceRect.getTop(),
                                 getLightGradientFillColor());
            
                  GDISupport.drawEllipse(graphics, deviceRect.getRectangle(), borderColor, paint);
                  break;
               }
               case IPseudostateKind.PK_JUNCTION:
               {
                  float centerX = (float)deviceRect.getCenterX();
                  GradientPaint paint = new GradientPaint(centerX,
                                 deviceRect.getBottom(),
                                 fillColor,
                                 centerX,
                                 deviceRect.getTop(),
                                 getLightGradientFillColor());
            
                  GDISupport.drawEllipse(graphics, deviceRect.getRectangle(), borderColor, paint);
                  break;
               }
               case IPseudostateKind.PK_SHALLOWHISTORY:
               {
                  float centerX = (float)deviceRect.getCenterX();
                  GradientPaint paint = new GradientPaint(centerX,
                                 deviceRect.getBottom(),
                                 fillColor,
                                 centerX,
                                 deviceRect.getTop(),
                                 getLightGradientFillColor());
            
                  GDISupport.drawEllipse(graphics, deviceRect.getRectangle(), borderColor, paint);
                  
                  try
                  {
                     ICompartment nameCompartment = ETDrawEngineFactory.createCompartment(ETDrawEngineFactory.CLASS_NAME_COMPARTMENT);
                     nameCompartment.setEngine(this);
                     nameCompartment.initResources();
                     nameCompartment.setName(new String("H"));
                     nameCompartment.setFontString("Arial-12");
                     
                     int X = deviceRect.getLeft();
                     int Y = deviceRect.getTop();
                     int H = deviceRect.getIntHeight();
                     int W = deviceRect.getIntWidth();
                     IETSize compartmentSize = nameCompartment.calculateOptimumSize(pDrawInfo, false);
                     ETRect compartmentRect = new ETRect(X, (int)(Y + H/2 - graphics.getFontMetrics().getHeight()/2), W, compartmentSize.getHeight());
                     nameCompartment.draw(pDrawInfo, compartmentRect);
                  }
                  catch (Exception e)
                  {
                  }
                  break;
               }
               case IPseudostateKind.PK_ENTRYPOINT:
               {
                  float centerX = (float)deviceRect.getCenterX();
                  GradientPaint paint = new GradientPaint(centerX,
                                 deviceRect.getBottom(),
                                 fillColor,
                                 centerX,
                                 deviceRect.getTop(),
                                 getLightGradientFillColor());
            
                  GDISupport.drawEllipse(graphics, deviceRect.getRectangle(), borderColor, paint);
                  
                  break;
               }
               case IPseudostateKind.PK_STOP: break;
               default:
               {
                  float centerX = (float)deviceRect.getCenterX();
                  GradientPaint paint = new GradientPaint(centerX,
                                 deviceRect.getBottom(),
                                 fillColor,
                                 centerX,
                                 deviceRect.getTop(),
                                 getLightGradientFillColor());
            
                  GDISupport.drawRectangle(graphics, deviceRect.getRectangle(), borderColor, paint);
               }
            }
            
         }
      }
   }
   
   public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
   {
   /*	
      IETSize retVal = new ETSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
      
      int kind = getPseudostateKind();
      switch(kind)
      {
         case IPseudostateKind.PK_CHOICE:
         {
            retVal.setSize(DECISION_NODE_WIDTH, DECISION_NODE_HEIGHT);
            break;
         }
         case IPseudostateKind.PK_FORK:
         case IPseudostateKind.PK_JOIN:
         {
            if (bHorizontal == true)
            {
               retVal.setSize(FORK_NODE_HEIGHT, FORK_NODE_WIDTH);
            }
            else
            {
               retVal.setSize(FORK_NODE_WIDTH, FORK_NODE_HEIGHT);
            }
            break;
         }
      }
   */   
      IETSize retVal = getOptimalHeightAndWidth();
      
      return bAt100Pct || retVal == null ? retVal : scaleSize(retVal, pDrawInfo != null ? pDrawInfo.getTSTransform() : getTransform());
   }
   
   protected int getPseudostateKind()
   {
      ETGenericNodeUI parentUI = (ETGenericNodeUI)this.getParent();
      int kind = IPseudostateKind.PK_INITIAL;
      
      if (parentUI.getModelElement() != null)
      {
         try
         {
            IElement element = parentUI.getModelElement();
            IPseudoState pseudoState = (IPseudoState)element;
            kind = pseudoState.getKind();
         }
         catch (Exception e)
         {
            kind = getPsuedoKindFromParameter(parentUI);
         }
         
         
      }
      else
      {
         kind = getPsuedoKindFromParameter(parentUI);
      }
      
      return kind;
   }
   
   public void setupOwner()
   {
      
   }
   
   /**
    * Returns the optimal height and width based on the initialization string
    *
    * @return The width and height of the node as it is being created
    */
   protected IETSize getOptimalHeightAndWidth()
   {
      IETSize retVal = new ETSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
      
      boolean useInitString = true;
      
      IElement subject = getFirstModelElement();
      if(subject instanceof IPseudoState)
      {
         IPseudoState state = (IPseudoState)subject;
         int kind = state.getKind();
         
         switch(kind)
         {
            case IPseudostateKind.PK_CHOICE:
               retVal.setSize(DECISION_NODE_WIDTH, DECISION_NODE_HEIGHT);
               useInitString = false;
               break;
               
            case IPseudostateKind.PK_FORK:
            case IPseudostateKind.PK_JOIN:
               // 
               // We have to use the initialization string because we may have
               // a horizonatl or vertical fork.  Since we have to use the 
               // initialization string to determine the width and hieght do 
               // not set useInitString to false.
               retVal.setSize(FORK_NODE_WIDTH, FORK_NODE_HEIGHT);
               break;
               
            default:
               retVal.setSize(DEFAULT_WIDTH,DEFAULT_HEIGHT);
               useInitString = false;
               break;
         }
         
         if(useInitString == true)
         {
            String initStr = getParent().getInitStringValue();
            if((initStr != null) && (initStr.length() > 0))
            {
               int delimiter = initStr.indexOf(' ');
               if (delimiter > 0)
               {
                  String kindStr = initStr.substring(delimiter + 1);
                  String kindStrLower = kindStr.toLowerCase();
                  
                  if( kindStrLower.equals("pseudostate join"))
                  {
                     retVal.setSize(FORK_NODE_WIDTH, FORK_NODE_HEIGHT);
                  }
                  else if( kindStrLower.equals("pseudostate join horizontal"))
                  {
                     retVal.setSize(FORK_NODE_HEIGHT, FORK_NODE_WIDTH);
                  }
                  else if( kindStrLower.equals("pseudostate choice"))
                  {
                     retVal.setSize(DECISION_NODE_WIDTH,DECISION_NODE_HEIGHT);
                  }
               }
            }
         }
      }
      return retVal;
   }
   
   private int getPsuedoKindFromParameter(ETGenericNodeUI parentUI)
   {
      int kind = IPseudostateKind.PK_INITIAL;
      
      IElement subject = getFirstModelElement();
      // && (parentUI.getInitStringValue() == null || parentUI.getInitStringValue().length() == 0)
      if(subject instanceof IPseudoState)
      {
         IPseudoState state = (IPseudoState)subject;
         kind = state.getKind();
      }
      else
      {
         String initialStr = parentUI.getInitStringValue();
         int delimiter = initialStr.indexOf(' ');
         if (delimiter > 0)
         {
            String kindStr = initialStr.substring(delimiter + 1);
            String kindStrLower = kindStr.toLowerCase();
            if( kindStrLower.equals("pseudostate choice"))
            {
               kind = IPseudostateKind.PK_CHOICE;
            }
            else if( kindStrLower.equals("pseudostate deephistory"))
            {
               kind = IPseudostateKind.PK_DEEPHISTORY;
            }
            else if( kindStrLower.equals("pseudostate fork"))
            {
               kind = IPseudostateKind.PK_FORK;
            }
            else if( kindStrLower.equals("pseudostate join"))
            {
               kind = IPseudostateKind.PK_JOIN;
            }
            else if( kindStrLower.equals("pseudostate join horizontal"))
            {
               kind = IPseudostateKind.PK_JOIN;
               bHorizontal = true;
            }
            else if( kindStrLower.equals("pseudostate junction"))
            {
               kind = IPseudostateKind.PK_JUNCTION;
            }
            else if( kindStrLower.equals("pseudostate shallowhistory"))
            {
               kind = IPseudostateKind.PK_SHALLOWHISTORY;
            }
            else if( kindStrLower.equals("pseudostate entrypoint"))
            {
               kind = IPseudostateKind.PK_ENTRYPOINT;
            }
         }
      }
      return kind;
   }
   
   public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pClass)
   {
      boolean bFlag = handleStandardLabelSensitivityAndCheck(id, pClass);
      if (!bFlag)
      {
         if (id.equals("MBK_SHOW_PSEUDOSTATE_NAME"))
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
         if (id.equals("MBK_SHOW_PSEUDOSTATE_NAME"))
         {
            ILabelManager labelMgr = getLabelManager();
            if (labelMgr != null)
            {
               boolean isDisplayed = labelMgr.isDisplayed(TSLabelKind.TSLK_NAME);
               labelMgr.showLabel(TSLabelKind.TSLK_NAME, isDisplayed ? false : true);
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
      // Add the context menu items dealing with finalstate
      addPseudoStateMenuItems(manager);
      
      // Add the stereotype label pullright
      addStandardLabelsToPullright(StandardLabelKind.SLK_STEREOTYPE, manager);
      
      super.onContextMenu(manager);
   }
   
   /**
    * Adds PseudoState specific stuff.
    *
    * @param pContextMenu [in] The context menu about to be displayed
    */
   protected void addPseudoStateMenuItems(IMenuManager manager)
   {
      IMenuManager subMenu = manager.createOrGetSubMenu(loadString("IDS_LABELS_TITLE"), "");
      if (subMenu != null)
      {
         subMenu.add(createMenuAction(loadString("IDS_SHOW_PSEUDOSTATENAME"), "MBK_SHOW_PSEUDOSTATE_NAME"));
         //manager.add(subMenu);
      }
   }
   
   /**
    * This is the name of the drawengine used when storing and reading from the product archive
    *
    * @param sID A unique identifier for this draw engine.  Used when persisting to the etlp file.
    */
   public String getDrawEngineID()
   {
      return "PseudoStateDrawEngine";
   }
   
   /**
    * This is the string to be used when looking for other similar drawengines.
    *
    * @param sID [out,retval] The unique engine identifier
    */
   public String getDrawEngineMatchID()
   {
      String id = getDrawEngineID();
      
      int kind = getPseudoState();
      switch (kind)
      {
         case IPseudostateKind.PK_CHOICE :
            id += "Choice";
            break;
            
         case IPseudostateKind.PK_DEEPHISTORY :
            id += "DeepHistory";
            break;
            
         case IPseudostateKind.PK_FORK :
            id += "Fork";
            break;
            
         case IPseudostateKind.PK_INITIAL :
            id += "Initial";
            break;
            
         case IPseudostateKind.PK_JOIN :
            id += "Join";
            break;
            
         case IPseudostateKind.PK_JUNCTION :
            id += "Junction";
            break;
            
         case IPseudostateKind.PK_SHALLOWHISTORY :
            id += "ShallowHistory";
            break;
            
         case IPseudostateKind.PK_ENTRYPOINT :
            id += "EntryPoint";
            break;
            
         case IPseudostateKind.PK_STOP :
            id += "Stop";
            break;
            
         default :
            break;
      }
      
      return id;
   }
   
   /**
    * Is this draw engine valid for the element it is representing?
    *
    * @param bIsValid[in] true if this draw engine can correctly represent the attached model element.
    */
   public boolean isDrawEngineValidForModelElement()
   {
      boolean isValid = false;
      
      // Make sure we're a control node
      // DecisionNode, FlowFinalNode, ForkNode, InitialNode, JoinNode, MergeNode &
      // ActivityFinalNode
      String metaType = getMetaTypeOfElement();
      if (metaType.equals("PseudoState"))
      {
         isValid = true;
      }
      
      return isValid;
   }
   
   /**
    * Returns the metatype of the label manager we should use
    *
    * @param return The metatype in essentialconfig.etc that defines the label manager
    */
   public String getManagerMetaType(int nManagerKind)
   {
      return nManagerKind == MK_LABELMANAGER ? "PseudoStateLabelManager" : "";
   }
   
   /**
    * During reconnection of an edge this flag is used to determine if a specified connector should be created
    */
   public int getReconnectConnector(IPresentationElement pEdgePE)
   {
      int retInt = 0;
      int nKind = getPseudostateKind();
      if (nKind == IPseudostateKind.PK_CHOICE ||
      nKind == IPseudostateKind.PK_DEEPHISTORY ||
      nKind == IPseudostateKind.PK_SHALLOWHISTORY)
      {
         retInt = ReconnectEdgeCreateConnectorKind.RECCK_DONT_CREATE;
      }
      else
      {
         retInt = super.getReconnectConnector(pEdgePE);
      }
      return retInt;
   }
   
   /**
    * Sets the pseudo state kind of the underlying IPseudoState
    *
    * @param nKind [in] The kind to set on the pseudo state
    */
   public void setPseudoState(int nKind)
   {
      IElement pSubject = getFirstModelElement();
      if (pSubject != null && pSubject instanceof IPseudoState)
      {
         ((IPseudoState)pSubject).setKind(nKind);
      }
   }
   
   /**
    * Returns the pseudo state kind of the underlying IPseudoState
    *
    * @param pKind [out] The kind of the pseudo state
    */
   public int getPseudoState()
   {
      int kind = IPseudostateKind.PK_CHOICE;
      IElement pSubject = getFirstModelElement();
      if (pSubject != null && pSubject instanceof IPseudoState)
      {
         kind = ((IPseudoState)pSubject).getKind();
      }
      return kind;
   }
}
