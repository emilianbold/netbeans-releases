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
import java.awt.event.ActionEvent;

import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.ADEdgeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.drawengines.StandardLabelKind;
import org.netbeans.modules.uml.ui.support.NodeEndKindEnum;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineArrowheadKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelKind;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

/**
 * @author KevinM
 *
 */
public class ETActivityEdgeDrawEngine extends ADEdgeDrawEngine implements IActivityEdgeDrawEngine {
        
        public ETActivityEdgeDrawEngine() 
        {
                super();
        }
        
        public String getElementType() 
        {
                return "ActivityEdge";
        }
        
        /*
         * Returns the IActivityEdge we're attached to
         */
        protected IActivityEdge getActivityEdge() {
                IElement pElement = this.getUI().getModelElement();// getFirstModelElement();
		if (pElement == null)
		{
                        pElement = getFirstModelElement();
                        this.getUI().setModelElement(pElement);
                }
                
                return pElement instanceof IActivityEdge ? (IActivityEdge) pElement : null;
        }
        
        protected int getNodeEndKind(boolean targetNode) {
                IActivityEdge pActivity = getActivityEdge();
                IEdgePresentation pPE = this.getIEdgePresentation();
                return pPE != null && pActivity != null ? pPE.getNodeEnd(targetNode ? pActivity.getTarget() : pActivity.getSource()) : NodeEndKindEnum.NEK_UNKNOWN;
        }
        
        protected int getSourceNodeEndKind() {
                return getNodeEndKind(false);
        }
        
        protected int getTargetNodeEndKind() {
                return getNodeEndKind(true);
        }
        
        public IActivityNode getSourceActivityNode() {
                IActivityEdge pActivity = getActivityEdge();
                return pActivity != null ? pActivity.getSource() : null;
        }
        
        public IActivityNode getTargetActivityNode() {
                IActivityEdge pActivity = getActivityEdge();
                return pActivity != null ? pActivity.getTarget() : null;
        }
        
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#doDraw(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo)
         */
        public void doDraw(IDrawInfo pDrawInfo) {
                
                int nEndKind = getTargetNodeEndKind();
                
                //		if (nEndKind == NodeEndKindEnum.NEK_FROM ||
                //			nEndKind == NodeEndKindEnum.NEK_BOTH)
                //		{
                //		   startArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_UNFILLEDARROW;
                //		}
                
                // Fixed issue 83107.
                // The NodeEndKind of target node is NodeEndKindEnum.NEK_TO which is correct.
                // Do not switch the edge ends
                if (nEndKind == NodeEndKindEnum.NEK_TO) {
                        // This is wrong.  It ends up with an incorrect parent/child relationship.  Switch it.
                        // postSwapEdgeEnds();
                }
                super.doDraw(pDrawInfo);
                
                /*
                 
                                All this code is run in getStartArrowKind, this way we can use the base draw routens.
                                try
                                {
                                   boolean bDidDraw = false;
                 
                                   IActivityEdge pActivity = getActivityEdge();
                                   IEdgePresentation pPE = getIEdgePresentation();
                 
                                   if (pPE != null && pActivity != null)
                                   {
                 
                //			  IActivityNode pTarget = pActivity.getTarget();
                //			  IActivityNode pSource = pActivity.getSource();
                //			  ISignalNode pSourceSignalNode = pSource instanceof ISignalNode ? (ISignalNode)pSource :null;
                //			  if (pSourceSignalNode != null)
                //			  {
                //				 // Need to create icon label
                //			  }
                 
                                          // See what end the general is on (the one with the arrowhead)
                                          int nEndKind = getTargetNodeEndKind();
                 
                                          int startArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
                                          int endArrowheadKind   = DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
                                          if (nEndKind == NodeEndKindEnum.NEK_FROM ||
                                                  nEndKind == NodeEndKindEnum.NEK_BOTH)
                                          {
                                                 startArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_UNFILLEDARROW;
                                          }
                                          else if (nEndKind == NodeEndKindEnum.NEK_TO)
                                          {
                                                 // This is wrong.  It ends up with an incorrect parent/child relationship.  Switch it.
                                                 postSwapEdgeEnds();
                 
                                                 // Draw as a line until the refresh comes through
                                                 startArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
                                                 endArrowheadKind   = DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
                                          }
                                          drawEdge( pDrawInfo, startArrowheadKind, endArrowheadKind, getLineKind());
                                          bDidDraw = true;
                                   }
                 
                                   if (bDidDraw == false)
                                   {
                                          super.doDraw(pDrawInfo);
                                   }
                                }
                                catch ( Exception e)
                                {
                                   e.printStackTrace();
                                }
                 */
        }
        
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getDrawEngineID()
         */
        public String getDrawEngineID() {
                return "ActivityEdgeDrawEngine";
        }
        
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#copy(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine)
         */
        public boolean copy(IDrawEngine pConstDrawEngine) {
                // TODO Auto-generated method stub
                return super.copy(pConstDrawEngine);
        }
        
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#isDrawEngineValidForModelElement()
         */
        public boolean isDrawEngineValidForModelElement() {
                String metaType = getMetaTypeOfElement();
                return metaType != null && metaType.equals("ActivityEdge");
        }
        
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#setSensitivityAndCheck(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem, int)
         */
        //	public boolean setSensitivityAndCheck(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem, int buttonKind) {
        //		boolean bHandled = false;
        //		try
        //		{
        //		   // Handle stereotypes through the base class
        //		   bHandled = handleStereotypeSensitivityAndCheck(pContextMenu, pMenuItem, buttonKind);
        //
        //		   if (!bHandled)
        //		   {
        //			 ILabelManager pLabelManager = getLabelManager();
        //
        //			  // Set the check state and sensitivities
        //			  switch (buttonKind)
        //			  {
        //			  case IADDrawEngineButtonHandler.MBK_SHOW_GUARD_CONDITION :
        //				 {
        //					boolean bIsDisplayed = false;
        //					if (pLabelManager != null)
        //					{
        //						bIsDisplayed = pLabelManager.isDisplayed(TSLabelKind.TSLK_GUARD_CONDITION);
        //					}
        //					pMenuItem.setChecked(bIsDisplayed);
        //					pMenuItem.setSensitive(parentDiagramIsReadOnly() ? false : true);
        //				 }
        //				 break;
        //			  case IADDrawEngineButtonHandler.MBK_SHOW_ACTIVITYEDGE_NAME :
        //				 {
        //					boolean bIsDisplayed = false;
        //					if (pLabelManager!= null)
        //					{
        //						bIsDisplayed = pLabelManager.isDisplayed(TSLabelKind.TSLK_ACTIVITYEDGE_NAME);
        //					}
        //					pMenuItem.setChecked(bIsDisplayed);
        //					pMenuItem.setSensitive(parentDiagramIsReadOnly() ? false:true);
        //				 }
        //				 break;
        //			  default :
        //				 {
        //					super.setSensitivityAndCheck(pContextMenu, pMenuItem, buttonKind);
        //				 }
        //			  }
        //		   }
        //		}
        //		catch(Exception e )
        //		{
        //		   e.printStackTrace();
        //		}
        //		return bHandled;
        //	}
        
        
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.products.ad.drawengines.ETEdgeDrawEngine#getEndArrowKind()
         */
        //	protected int getEndArrowKind() {
        //		return DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
        //	}
        
        // Fixed 83107. Returns NO_ARROWHEAD kind for start arrow kind.
        protected int getStartArrowKind() {
                return DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
        }
        
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.products.ad.drawengines.ETEdgeDrawEngine#getStartArrowKind()
         */
        //      protected int getStartArrowKind() {
        //		// See what end the general is on (the one with the arrowhead)
        //		int nEndKind = getTargetNodeEndKind();
        //
        //		return nEndKind == NodeEndKindEnum.NEK_FROM || nEndKind == NodeEndKindEnum.NEK_BOTH ? DrawEngineArrowheadKindEnum.DEAK_UNFILLEDARROW : DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
        //	}
        
        // Fixed 83107. Returns UNFILLEDARROW arrow head kind if target node is a TO node.
        protected int getEndArrowKind() {
                // See what end the general is on (the one with the arrowhead)
                int nEndKind = getTargetNodeEndKind();
                return nEndKind == NodeEndKindEnum.NEK_TO ? DrawEngineArrowheadKindEnum.DEAK_UNFILLEDARROW : DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
        }
        
        /**
         * Returns the metatype of the label manager we should use
         *
         * @param return The metatype in essentialconfig.etc that defines the label manager
         */
        public String getManagerMetaType(int nManagerKind) {
                return nManagerKind == MK_LABELMANAGER ? "ActivityEdgeLabelManager" : "";
        }
        
        
        public void onContextMenu(IMenuManager manager) 
        {
                IActivityEdge pActivityEdge = getActivityEdge();
                
                if (pActivityEdge != null) 
                {
                        // Add the activity edge menu items
                        addActivityEdgeMenuItems(manager);
                        
                        // Add the stereotype label pullright
                        addStandardLabelsToPullright(StandardLabelKind.SLK_STEREOTYPE, manager);
                        
                        // Call the base class
                        super.onContextMenu(manager);
                }
        }
        
        public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pClass) 
        {
                boolean bHandled = handleStandardLabelSensitivityAndCheck(id, pClass);
                
                if (!bHandled) 
                {
                        ILabelManager pLabelManager = getLabelManager();
                        
                        // Set the check state and sensitivities
                        if (id.equals("MBK_SHOW_GUARD_CONDITION")) 
                        {
                                boolean bIsDisplayed = false;
                                if (pLabelManager != null) 
                                {
                                        bIsDisplayed = pLabelManager.isDisplayed(TSLabelKind.TSLK_GUARD_CONDITION);
                                }
                                pClass.setChecked(bIsDisplayed);
                                bHandled = (isParentDiagramReadOnly() ? false : true);
                        } 
                        else if (id.equals("MBK_SHOW_ACTIVITYEDGE_NAME")) 
                        {
                                boolean bIsDisplayed = false;
                                if (pLabelManager != null) 
                                {
                                        bIsDisplayed = pLabelManager.isDisplayed(TSLabelKind.TSLK_ACTIVITYEDGE_NAME);
                                }
                                pClass.setChecked(bIsDisplayed);
                                bHandled = (isParentDiagramReadOnly() ? false : true);
                        }
                }
                
                if (!bHandled) 
                {
                        super.setSensitivityAndCheck(id, pClass);
                }
                
                return bHandled;
        }
        
        public boolean onHandleButton(ActionEvent e, String id) 
        {
                
                boolean bHandled = handleStandardLabelSelection(e, id);
                
                if (!bHandled) 
                {
                        IDrawingAreaControl pDiagram = this.getDrawingArea();
                        ILabelManager pLabelManager = this.getLabelManager();
                        IElement pElement = getFirstModelElement();
                        
                        if (pElement != null && pLabelManager != null && pDiagram != null) 
                        {
                                ILabelPresentation pLabelPE = null;
                                
                                if (id.equals("MBK_SHOW_GUARD_CONDITION")) 
                                {
                                        boolean bIsDisplayed = false;
                                        
                                        bIsDisplayed = pLabelManager.isDisplayed(TSLabelKind.TSLK_GUARD_CONDITION);
                                        
                                        pLabelManager.showLabel(TSLabelKind.TSLK_GUARD_CONDITION, (bIsDisplayed) ? false : true);
                                        
                                        if (bIsDisplayed == false) // We just displayed it so edit
                                        {
                                                IPresentationElement pPE = pLabelManager.getLabel(TSLabelKind.TSLK_GUARD_CONDITION);
                                                
                                                
                                                if (pPE instanceof ILabelPresentation) 
                                                {
                                                        pLabelPE = (ILabelPresentation)pPE;
                                                        
                                                        if (pLabelPE != null) 
                                                        {
                                                                pDiagram.postEditLabel(pLabelPE);
                                                        }
                                                }
                                                invalidate();
                                        }
                                } 
                                else if (id.equals("MBK_SHOW_ACTIVITYEDGE_NAME")) 
                                {
                                        
                                        boolean bIsDisplayed = false;
                                        
                                        pLabelManager.isDisplayed(TSLabelKind.TSLK_NAME);
                                        pLabelManager.showLabel(TSLabelKind.TSLK_NAME, (bIsDisplayed) ? false : true);
                                        
                                        if (bIsDisplayed == false) // We just displayed it so edit
                                        {
                                                IPresentationElement pPE = pLabelManager.getLabel(TSLabelKind.TSLK_NAME);
                                                
                                                
                                                if (pPE instanceof ILabelPresentation) 
                                                {
                                                        pLabelPE = (ILabelPresentation)pPE;
                                                        
                                                        if (pLabelPE != null) 
                                                        {
                                                                pDiagram.postEditLabel(pLabelPE);
                                                        }
                                                }
                                                invalidate();
                                        }
                                }
                        }
                }
                
                if (!bHandled) 
                {
                        bHandled = super.onHandleButton(e, id);
                }
                
                return bHandled;
        }
        
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initResources()
         */
        public void initResources() 
        {
                this.setLineColor("activityedgecolor", Color.BLACK);
                super.initResources();
        }
}
