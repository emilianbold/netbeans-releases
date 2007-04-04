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

import com.tomsawyer.drawing.geometry.TSConstRect;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.event.ActionEvent;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IPseudoState;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IPseudostateKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.ReconnectEdgeCreateConnectorKind;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNodeUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelKind;
import com.tomsawyer.editor.graphics.TSEGraphics;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;

/**
 * @author jingmingm
 *
 */
public class ETPseudoStateDrawEngine extends ETNodeDrawEngine
{
   //private boolean bHorizontal = false;
   
   private static final int DEFAULT_WIDTH  = 22;
   private static final int DEFAULT_HEIGHT = 22;
   private static final int FORK_NODE_HEIGHT = 80;
   private static final int FORK_NODE_WIDTH = 7;
   private static final int DECISION_NODE_WIDTH = 20;
   private static final int DECISION_NODE_HEIGHT = 30;
   
   private static final String HORIZONTAL = "Horizontal";          // NOI18N
   private static final String PSEUDOSTATE = "PseudoState";       // NOI18N
   private static final String INITIAL = PSEUDOSTATE + " Initial"; // NOI18N
   private static final String CHOICE = PSEUDOSTATE + " Choice";   // NOI18N
   private static final String FORK = PSEUDOSTATE + " Fork";       // NOI18N
   private static final String JOIN = PSEUDOSTATE + " Join";       // NOI18N
   private static final String JOIN_HORIZONTAL = JOIN + " " + HORIZONTAL;          // NOI18N
   private static final String SHALLOW_HISTORY = PSEUDOSTATE + " ShallowHistory";  // NOI18N
   private static final String DEEP_HISTORY = PSEUDOSTATE + " DeepHistory";        // NOI18N
   private static final String ENTRYPOINT = PSEUDOSTATE + " EntryPoint";           // NOI18N
   private static final String JUNCTION = PSEUDOSTATE + " Junction";               // NOI18N
   
   public String getElementType()
   {
      String type = super.getElementType();
      if (type == null)
      {
         type = new String(PSEUDOSTATE);
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
                  //String kindStrLower = kindStr.toLowerCase();
                  
                  if( kindStr.equalsIgnoreCase(JOIN))
                  {
                     retVal.setSize(FORK_NODE_WIDTH, FORK_NODE_HEIGHT);
                  }
                  else if( kindStr.equalsIgnoreCase(JOIN_HORIZONTAL))
                  {
                     retVal.setSize(FORK_NODE_HEIGHT, FORK_NODE_WIDTH);
                  }
                  else if( kindStr.equalsIgnoreCase(CHOICE))
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
            //String kindStrLower = kindStr.toLowerCase();
            
            if( kindStr.equalsIgnoreCase(CHOICE))
            {
               kind = IPseudostateKind.PK_CHOICE;
            }
            else if( kindStr.equalsIgnoreCase(DEEP_HISTORY))
            {
               kind = IPseudostateKind.PK_DEEPHISTORY;
            }
            else if( kindStr.equalsIgnoreCase(FORK))
            {
               kind = IPseudostateKind.PK_FORK;
            }
            else if( kindStr.equalsIgnoreCase(JOIN))
            {
               kind = IPseudostateKind.PK_JOIN;
            }
            else if( kindStr.equalsIgnoreCase(JOIN_HORIZONTAL ))
            {
               kind = IPseudostateKind.PK_JOIN;
               //bHorizontal = true;
            }
            else if( kindStr.equalsIgnoreCase(JUNCTION))
            {
               kind = IPseudostateKind.PK_JUNCTION;
            }
            else if( kindStr.equalsIgnoreCase(SHALLOW_HISTORY))
            {
               kind = IPseudostateKind.PK_SHALLOWHISTORY;
            }
            else if( kindStr.equalsIgnoreCase(ENTRYPOINT))
            {
               kind = IPseudostateKind.PK_ENTRYPOINT;
            }
         }
      }
      return kind;
   }
   
   public boolean isHorizontalJoin()
   {
      boolean bHorizontalJoin = false;
      IETGraphObjectUI nodeUI = this.getParent();
      String initStr = (nodeUI != null ? nodeUI.getInitStringValue() : "");
      if (initStr != null || initStr.length() > 0)
      {
         bHorizontalJoin = (initStr.indexOf(JOIN_HORIZONTAL) != -1);  // NOI18N
      }
      return bHorizontalJoin;
   }
   
   public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pClass)
   {
      boolean isReadOnly = isParentDiagramReadOnly();
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
               bFlag = isReadOnly ? false : true;
            }
         }
         else if (id.equals("MBK_SHOW_VERTICAL_FORK") ||
               id.equals("MBK_SHOW_HORIZONTAL_FORK"))
         {
            bFlag = !isReadOnly;
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
         // Fixed IZ=78636
         // Added an menu item to the context menu to change a join/merge node 
         // from vertical to horizontal and vice versa.
         else if (id.equals("MBK_SHOW_VERTICAL_FORK") ||
               id.equals("MBK_SHOW_HORIZONTAL_FORK"))
         {
            IETNodeUI nodeUI = getNodeUI();
            if (nodeUI != null)
            {  
               TSConstRect rect = nodeUI.getBounds();
               double width = rect.getWidth();
               double height = rect.getHeight();
               // rotate the dimension of the join/merge around its center point
               resize(Math.round((long)height), Math.round((long)width), false);
               
               //Change the initString accordingly
               String oldInitStr = getInitializationString();
               String newInitStr = "";
               if (oldInitStr != null && oldInitStr.length() > 0)
               {
                  int strHorizontalIndex = oldInitStr.lastIndexOf(HORIZONTAL);
                  
                  newInitStr = (strHorizontalIndex > -1 ?
                     (oldInitStr.substring(0, strHorizontalIndex)).trim() :
                     oldInitStr.concat(" " + HORIZONTAL));
                  
                  nodeUI.setInitStringValue(newInitStr);
               }
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
      
      // Fixed IZ=78636
      // Added an menu item to the context menu to change a join/merge node 
      // from vertical to horizontal and vice versa.
      int nKind = getPseudostateKind();
      if (nKind == IPseudostateKind.PK_JOIN )
      {
         ContextMenuActionClass menuItem = (isHorizontalJoin() ?
            createMenuAction(loadString("IDS_POPUP_STATE_TO_VERTICAL_JOIN"), "MBK_SHOW_VERTICAL_FORK") :
            createMenuAction(loadString("IDS_POPUP_STATE_TO_HORIZONTAL_JOIN"), "MBK_SHOW_HORIZONTAL_FORK") );
         manager.add(menuItem);
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
      if (metaType.equals(PSEUDOSTATE))
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
