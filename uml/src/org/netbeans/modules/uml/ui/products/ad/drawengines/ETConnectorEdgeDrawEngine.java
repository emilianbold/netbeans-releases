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

import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineArrowheadKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineLineKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;

public class ETConnectorEdgeDrawEngine extends ETEdgeDrawEngine
{
	protected int getLineKind()
	{
		return DrawEngineLineKindEnum.DELK_SOLID;	
	}
	
	protected int getEndArrowKind()
	{
		return DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
	}
	
	public String getElementType()
	{
		String type = super.getElementType();
		if (type == null)
		{
			type = new String("Connector");
		}
		return type;
	}

	public boolean isDrawEngineValidForModelElement()
	{
		boolean isValid = false;

		// Make sure we're a control node
		// DecisionNode, FlowFinalNode, ForkNode, InitialNode, JoinNode, MergeNode &
		// ActivityFinalNode
		String metaType = getMetaTypeOfElement();
		if (metaType.equals("Connector") || metaType.equals("MessageConnector"))
		{
			isValid = true;
		}
		
		return isValid;
	}
	
	public void doDraw(IDrawInfo drawInfo)
	{
		/*
		try
		{
			IElement pElement = ((IETEdge)getParent().getTSObject()).getPresentationElement().getFirstSubject();
			if (pElement != null)
			{
				super.doDraw(drawInfo);
			}
		}
		catch (NullPointerException e)
		{}
		*/
		super.doDraw(drawInfo);
	}

	public String getDrawEngineID() 
	{
		return "ConnectorEdgeDrawEngine";
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawengines.ETDrawEngine#getManagerMetaType(int)
	 */
	public String getManagerMetaType(int nManagerKind)
	{
		String sManager = null;

		if (nManagerKind == MK_LABELMANAGER)
		{
		   sManager = "MessageConnectorLabelManager";
		}

		return sManager;
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initResources()
	 */
	public void initResources()
	{
		this.setLineColor("connectoredgecolor", Color.BLACK);
		super.initResources();
	}
}
