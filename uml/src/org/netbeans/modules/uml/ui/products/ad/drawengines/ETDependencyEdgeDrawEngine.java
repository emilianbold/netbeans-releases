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

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IUsage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IDelegate;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.support.NodeEndKindEnum;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineArrowheadKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineLineKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;

public class ETDependencyEdgeDrawEngine extends ETEdgeDrawEngine {
	private int m_lineKind = DrawEngineLineKindEnum.DELK_SOLID;
	private int m_startArrowKind = DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
	private int m_endArrowKind = DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
	
	public String getElementType() {
		String type = super.getElementType();
		if (type == null) {
			type = new String("Dependency");
		}
		return type;
	}

	public void doDraw(IDrawInfo drawInfo) {
		IElement modelElement = this.getFirstModelElement();
		IEdgePresentation edgePresentation = TypeConversions.getEdgePresentation(this);

		IDependency dependency = null;
		if(modelElement instanceof IDependency) {
			dependency = (IDependency)modelElement;
		}
		
		INamedElement supplier = dependency !=  null ? dependency.getSupplier() : null;
		if(edgePresentation != null && supplier != null ) {		
			String supplierXMIID = supplier.getXMIID();

			int nEndKind = edgePresentation.getNodeEnd3(supplierXMIID);			

			int startArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_UNFILLEDARROW;
			int endArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_UNFILLEDARROW;

			if(nEndKind == NodeEndKindEnum.NEK_TO) {
				startArrowheadKind = DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD ;
				endArrowheadKind   = DrawEngineArrowheadKindEnum.DEAK_UNFILLEDARROW ;
			}

			// Use a local draw Edge routine that overrides the drawing of usage dependencies.
			// These are used on the component diagram when drawing the ball and socket configuration
			int nLineKind = DrawEngineLineKindEnum.DELK_DASH;
			
//			// Now see if we need to override the normal drawing of a dependency
			IDelegate delegate = null;
			if(dependency instanceof IDelegate) {
				delegate = (IDelegate)dependency;
			}

			boolean isUsageConnectedToBall = getIsUsageConnectedToBall();
			
			if (isUsageConnectedToBall)
			{
				m_lineKind = DrawEngineLineKindEnum.DELK_SOLID;
				m_endArrowKind = DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
			}
			else if (delegate != null)
			{
				m_lineKind = DrawEngineLineKindEnum.DELK_SOLID;
				m_startArrowKind = startArrowheadKind;
				m_endArrowKind = endArrowheadKind;
			}
			else
			{
				m_lineKind = DrawEngineLineKindEnum.DELK_DASH;
				m_startArrowKind = startArrowheadKind;
				m_endArrowKind = endArrowheadKind;
			}
		}
		super.doDraw(drawInfo);
	}

	protected int getLineKind() {
		return m_lineKind;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawengines.ETEdgeDrawEngine#getStartArrowKind()
	 */
	protected int getStartArrowKind() {
		return m_startArrowKind;
	}

	protected int getEndArrowKind() {
		return m_endArrowKind;
	}

	public void onContextMenu(IMenuManager manager) {
		// Add the stereotype label pullright
		addStandardLabelsToPullright(StandardLabelKind.SLK_ALL, manager);
		super.onContextMenu(manager);
	}

	public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pClass) {
		boolean retVal = handleStandardLabelSensitivityAndCheck(id, pClass);
		if (!retVal) {
			super.setSensitivityAndCheck(id, pClass);
		}
		return retVal;
	}

	public boolean onHandleButton(ActionEvent e, String id) {
		boolean handled = handleStandardLabelSelection(e, id);
		if (!handled) {
			handled = super.onHandleButton(e, id);
		}
		return handled;
	}

	public String getDrawEngineID() {
		return "DependencyEdgeDrawEngine";
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawengines.ETDrawEngine#getManagerMetaType(int)
	 */
	public String getManagerMetaType(int nManagerKind) {
		String sManager = null;

		if (nManagerKind == MK_LABELMANAGER) {
			sManager = "SimpleStereotypeAndNameLabelManager";
		}

		return sManager;
	}

	/**
	 * The forced stereotype text on the label manager
	 *
	 * @param return The stereotype text that should appear in a label - readonly
	 */
	protected String getForcedStereotypeText() {
		String text = "";
		IElement modEle = getFirstModelElement();
		if (modEle != null) {
			String metaType = modEle.getElementType();
			if (metaType.equals("Extend")) {
				text = "extend";
			} else if (metaType.equals("Include")) {
				text = "include";
			} else if (metaType.equals("Permission")) {
				text = "permission";
			} else if (metaType.equals("Realization")) {
				text = "realization";
			} else if (metaType.equals("Abstraction")) {
				text = "abstraction";
			} else if (metaType.equals("Usage")) {
				text = "usage";
			} else if (metaType.equals("Delegate")) {
				text = "delegate";
			}
		}
		return text;
	}
	
	private boolean getIsUsageConnectedToBall() {
		boolean retVal = false;
		
		IElement modelElement = getFirstModelElement();
		IUsage usage = null;
		
		if(modelElement instanceof IUsage) {
			usage = (IUsage)modelElement;
		}
		
		if(usage != null) {
			IEdgePresentation edgePE = getIEdgePresentation();
			if(edgePE != null) {
				ETPairT<IDrawEngine, IDrawEngine> engines = edgePE.getEdgeFromAndToDrawEngines();
				String fromDEID = "";
				String toDEID = "";
				
				if(engines.getParamOne() != null) {
					fromDEID = engines.getParamOne().getDrawEngineID();
				}
				if(engines.getParamTwo() != null) {
					toDEID = engines.getParamTwo().getDrawEngineID();
				}
				
				retVal = fromDEID.compareTo("InterfaceDrawEngine") == 0 || toDEID.compareTo("InterfaceDrawEngine") == 0;
			}
		}
		return retVal;
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initResources()
	 */
	public void initResources()
	{
		this.setLineColor("dependencyedgecolor", Color.BLACK);
		super.initResources();
	}
}
