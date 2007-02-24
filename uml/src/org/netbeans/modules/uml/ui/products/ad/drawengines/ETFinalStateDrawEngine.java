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


/*
 * Created on Oct 29, 2003
 *
 */
package org.netbeans.modules.uml.ui.products.ad.drawengines;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.event.ActionEvent;

import com.tomsawyer.editor.graphics.TSEGraphics;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.ReconnectEdgeCreateConnectorKind;
import org.netbeans.modules.uml.core.support.umlsupport.ETDeviceRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNodeUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelKind;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;

/**
 * @author jingmingm
 *
 */
public class ETFinalStateDrawEngine extends ETNodeDrawEngine
{
	public void initResources()
	{
		setFillColor("ellipsefill", 255, 51, 0);
        setLightGradientFillColor("ellipselightgradientfill", 255, 255, 255);
		setBorderColor("ellipseborder", Color.BLACK);

		super.initResources();
	}
	
	public String getElementType()
	{
		String type = super.getElementType();
		if (type == null)
		{
			type = new String("FinalState");
		}
		return type;
	}

	public void doDraw(IDrawInfo pDrawInfo)
	{
		TSEGraphics graphics = pDrawInfo.getTSEGraphics();
		
		IETNodeUI parentUI = (IETNodeUI)this.getParent();
		
		// draw yourself only if you have an owner
		if (parentUI.getOwner() != null)
		{
			if (!parentUI.isTransparent())
			{
				IETRect deviceRect = pDrawInfo.getDeviceBounds();
				// Draw outline
				graphics.setColor(getBorderBoundsColor());
				GDISupport.frameEllipse(graphics, deviceRect.getRectangle());
				
				// Draw center
				int h = deviceRect.getIntHeight();
				int w = deviceRect.getIntWidth();
				
				IETRect redCenter =(IETRect) deviceRect.clone(); 
				redCenter.deflateRect(w/4, h/4);
                float centerX = (float)redCenter.getCenterX();
                GradientPaint paint = new GradientPaint(centerX,
                                 redCenter.getBottom(),
                                 getBkColor(),
                                 centerX,
                                 redCenter.getTop(),
                                 getLightGradientFillColor());
            
				graphics.setPaint(paint);
				GDISupport.fillEllipse(graphics,redCenter.getRectangle());
                graphics.setColor(getBorderBoundsColor());
                GDISupport.frameEllipse(graphics, redCenter.getRectangle());
			}
		}
	}
	
	public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
	{
		IETSize retVal = new ETSize(16, 16);
			  		
		return bAt100Pct ? retVal : super.scaleSize(retVal, pDrawInfo != null ? pDrawInfo.getTSTransform() : getTransform());
	}

	public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pClass)
	{
		boolean bFlag = handleStandardLabelSensitivityAndCheck(id, pClass);
		if (!bFlag)
		{
			if (id.equals("MBK_SHOW_FINALSTATE_NAME"))
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
			if (id.equals("MBK_SHOW_FINALSTATE_NAME"))
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
		// Add the context menu items dealing with finalstate
		addFinalStateMenuItems(manager);
		
		// Add the stereotype label pullright
		addStandardLabelsToPullright(StandardLabelKind.SLK_STEREOTYPE, manager);
		
		super.onContextMenu(manager);
	}
	
	/**
	 * Adds FinalState specific stuff.
	 * *
	 * @param pContextMenu [in] The context menu about to be displayed
	 */
	protected void addFinalStateMenuItems(IMenuManager manager)
	{
		IMenuManager subMenu = manager.createOrGetSubMenu(loadString("IDS_LABELS_TITLE"), "");
		if (subMenu != null)
		{
			subMenu.add(createMenuAction(loadString("IDS_SHOW_FINALSTATENAME"), "MBK_SHOW_FINALSTATE_NAME"));
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
		return "FinalStateDrawEngine";
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
		if (metaType.equals("FinalState"))
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
		return nManagerKind == MK_LABELMANAGER ? "FinalStateLabelManager" : "";
//		String sManager = null;
//		if (nManagerKind == MK_LABELMANAGER) {
//			IElement pEle = getFirstModelElement();
//			if (pEle != null && pEle instanceof IFinalState) {
//				sManager = "FinalStateLabelManager";
//			}
//		}
//		return sManager;
	}
	
	/**
	 * During reconnection of an edge this flag is used to determine if a specified connector should be created
	 */
	public int getReconnectConnector(IPresentationElement pEdgePE)
	{
		return ReconnectEdgeCreateConnectorKind.RECCK_DONT_CREATE;
	}
	
}


