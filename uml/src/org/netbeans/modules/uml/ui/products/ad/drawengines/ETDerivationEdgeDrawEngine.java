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

import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineArrowheadKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineLineKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelKind;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;



public class ETDerivationEdgeDrawEngine extends ETEdgeDrawEngine
{
	public String getElementType()
	{
		String type = super.getElementType();
		if (type == null)
		{
			type = new String("Derivation");
		}
		return type;
	}

	public void doDraw(IDrawInfo drawInfo)
	{
		super.doDraw(drawInfo);
	}
	
	protected int getLineKind() 
	{
		return DrawEngineLineKindEnum.DELK_DASH;	 
	}
	
	protected int getStartArrowKind()
	{
		return DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
	}

	protected int getEndArrowKind()
	{
		return DrawEngineArrowheadKindEnum.DEAK_FILLED_WHITE;
	}

	public void onContextMenu(IMenuManager manager)
	{
		// Add the stereotype label pullright
		addStandardLabelsToPullright(StandardLabelKind.SLK_STEREOTYPE, manager);
		
		// Add the binding label pullright
		addBindLabelPullright(manager);
		
		super.onContextMenu(manager);
	}
			
	public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pClass)
	{
		 boolean retVal = handleStandardLabelSensitivityAndCheck(id, pClass);
		 
		 if (!retVal)
		 {
			ILabelManager labelMgr = getLabelManager();
			boolean isReadOnly = isParentDiagramReadOnly();
			if (labelMgr != null)
			{
				if (id.equals("MBK_SHOW_BINDING"))
				{
					boolean isDisplayed = labelMgr.isDisplayed(TSLabelKind.TSLK_DERIVATION_BINDING);
					pClass.setChecked(isDisplayed);
		 			
					retVal = isReadOnly ? false : true;
				}
			}
		 }
		 
		 if (!retVal)
		 {
			super.setSensitivityAndCheck(id, pClass);
		 }
		 return retVal;
	}
   
	public boolean onHandleButton(ActionEvent e, String id)
	{
		boolean handled = handleStandardLabelSelection(e, id);
		if (!handled)
		{
			if (id.equals("MBK_SHOW_BINDING"))
			{
				ILabelManager labelMgr = getLabelManager();
				IDrawingAreaControl pDiagram = getDrawingArea();
				if (labelMgr != null && pDiagram != null)
				{
					boolean isDisplayed = labelMgr.isDisplayed(TSLabelKind.TSLK_DERIVATION_BINDING);
					labelMgr.showLabel(TSLabelKind.TSLK_DERIVATION_BINDING, isDisplayed ? false : true);

					pDiagram.refresh(false);
					handled = true;
				}
			}
		}
		
		if (!handled)
		{
			super.onHandleButton(e, id);
		}
		return handled;
	}

	public String getDrawEngineID() 
	{
		return "DerivationEdgeDrawEngine";
	}
	
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawengines.ETDrawEngine#getManagerMetaType(int)
	 */
	public String getManagerMetaType(int nManagerKind) {
		String sManager = null;

		if (nManagerKind == MK_LABELMANAGER)
		{
		   sManager = "DerivationEdgeLabelManager";
		}

		return sManager;
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initResources()
	 */
	public void initResources()
	{
		this.setLineColor("derivationedgecolor", Color.BLACK);
		super.initResources();
	}
}
