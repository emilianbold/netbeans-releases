/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
