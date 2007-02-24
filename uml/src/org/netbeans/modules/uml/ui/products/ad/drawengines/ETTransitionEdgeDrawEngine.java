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
import java.awt.event.ActionEvent;

import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.support.NodeEndKindEnum;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineArrowheadKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineLineKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ISupportEnums;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelKind;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

/**
 * @author jingmingm
 *
 */
public class ETTransitionEdgeDrawEngine extends ETEdgeDrawEngine
{
	public String getElementType()
	{
		String type = super.getElementType();
		if (type == null)
		{
			type = new String("Transition");
		}
		return type;
	}

	public void doDraw(IDrawInfo drawInfo)
	{
		super.doDraw(drawInfo);

	}
	
	protected int getLineKind() 
	{
		return DrawEngineLineKindEnum.DELK_SOLID;	 
	}
	
	protected int getEndArrowKind()
	{
		return DrawEngineArrowheadKindEnum.DEAK_UNFILLEDARROW;
	}

	public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pClass)
	{
		boolean bFlag = handleStandardLabelSensitivityAndCheck(id, pClass);
		if (!bFlag)
		{
			if (id.equals("MBK_SHOW_PRE_CONDITION"))
			{
				ILabelManager labelMgr = getLabelManager();
				if (labelMgr != null)
				{
					boolean isDisplayed = labelMgr.isDisplayed(TSLabelKind.TSLK_PRE_CONDITION);
					pClass.setChecked(isDisplayed);
					bFlag = isParentDiagramReadOnly() ? false : true;
				}
			}
			else if (id.equals("MBK_SHOW_POST_CONDITION"))
			{
				ILabelManager labelMgr = getLabelManager();
				if (labelMgr != null)
				{
					boolean isDisplayed = labelMgr.isDisplayed(TSLabelKind.TSLK_POST_CONDITION);
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
			IDrawingAreaControl pDiagram = getDrawingArea();
			ILabelManager labelMgr = getLabelManager();
			IElement pEle = getFirstModelElement();
			if (pEle != null && labelMgr != null && pDiagram != null)
			{
				if (id.equals("MBK_SHOW_PRE_CONDITION"))
				{
					boolean isDisplayed = labelMgr.isDisplayed(TSLabelKind.TSLK_PRE_CONDITION);
					labelMgr.showLabel(TSLabelKind.TSLK_PRE_CONDITION, isDisplayed ? false : true);
				
					if (!isDisplayed)
					{
						IPresentationElement pPE = labelMgr.getLabel(TSLabelKind.TSLK_PRE_CONDITION);
						if (pPE != null && pPE instanceof ILabelPresentation)
						{
							pDiagram.postEditLabel((ILabelPresentation)pPE);
						}
					}
					invalidate();
					handled = true;
				}
				else if (id.equals("MBK_SHOW_POST_CONDITION"))
				{
					boolean isDisplayed = labelMgr.isDisplayed(TSLabelKind.TSLK_POST_CONDITION);
					labelMgr.showLabel(TSLabelKind.TSLK_POST_CONDITION, isDisplayed ? false : true);
				
					if (!isDisplayed)
					{
						IPresentationElement pPE = labelMgr.getLabel(TSLabelKind.TSLK_POST_CONDITION);
						if (pPE != null && pPE instanceof ILabelPresentation)
						{
							pDiagram.postEditLabel((ILabelPresentation)pPE);
						}
					}
					invalidate();
					handled = true;
				}
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
		addTransitionEdgeMenuItems(manager);
		
		// Add the stereotype label pullright
		addStandardLabelsToPullright(StandardLabelKind.SLK_ALL, manager);
		
		super.onContextMenu(manager);
	}
	
	/**
	 * Adds Transition Edge specific items.
	 *
	 *  Pre Condition
	 *  Post Condition
	 *
	 * @param pContextMenu[in] The context menu we're adding to
	 * @param pLinkElement[in] The link element that this context menu applies to
	 */
	protected void addTransitionEdgeMenuItems(IMenuManager manager)
	{
		IMenuManager subMenu = manager.createOrGetSubMenu(loadString("IDS_LABELS_TITLE"), "");
		if (subMenu != null)
		{
			subMenu.add(createMenuAction(loadString("IDS_SHOW_PRE_CONDITION"), "MBK_SHOW_PRE_CONDITION"));
			subMenu.add(createMenuAction(loadString("IDS_SHOW_POST_CONDITION"), "MBK_SHOW_POST_CONDITION"));
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
		return "TransitionEdgeDrawEngine";
	}
	
	/**
	 * Verify the ends are correct
	 */
	public void verifyEdgeEnds()
	{
		IEdgePresentation pPE = getIEdgePresentation();
		ITransition pTransition = getTransition();
		if (pTransition != null)
		{
			IStateVertex pSource = pTransition.getSource();
			IStateVertex pTarget = pTransition.getTarget();
			
			// See what end the target is on (the one with the arrowhead)
			int endKind = NodeEndKindEnum.NEK_UNKNOWN;
			endKind = pPE.getNodeEnd(pTarget);
			
			if (endKind == NodeEndKindEnum.NEK_TO)
			{
				if (!isParentDiagramReadOnly())
				{
					// This is wrong.  It ends up with an incorrect parent/child relationship.  
					// Switch it unless the diagram is readonly
					postSwapEdgeEnds();
				}
			}
		}
	}
	
	/**
	 * Returns the ITransition we're attached to
	 *
	 * @param pTransition [out,retval] Returns the ITransition we're attached to
	 */
	private ITransition getTransition()
	{
		ITransition pTransition = null;
		IElement pEle = getFirstModelElement();
		if (pEle != null && pEle instanceof ITransition)
		{
			pTransition = (ITransition)pEle;
		}
		return pTransition;
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
		if (metaType.equals("AssemblyConnector") || metaType.equals("DelegationConnector"))
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
		return nManagerKind == MK_LABELMANAGER ? "TransitionLabelManager" : "";
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initResources()
	 */
	public void initResources()
	{
		this.setLineColor("transitioncolor", Color.BLACK);
		super.initResources();
	}
}


