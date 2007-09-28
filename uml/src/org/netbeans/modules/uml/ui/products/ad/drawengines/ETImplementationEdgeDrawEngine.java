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
import java.awt.event.ActionEvent;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineArrowheadKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineLineKindEnum;


public class ETImplementationEdgeDrawEngine extends ETEdgeDrawEngine {
	private int m_lineKind = DrawEngineLineKindEnum.DELK_SOLID;
	private int m_endArrowKind = DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD; 
	
	public String getElementType() {
		String type = super.getElementType();
		if (type == null) {
			type = new String("Implementation");
		}
		return type;
	}

	public void doDraw(IDrawInfo drawInfo) {
		setAppearance();
   
		super.doDraw(drawInfo);
	}
	
	protected void setAppearance() {
		IEdgePresentation myPresentation = this.getIEdgePresentation();

		if(myPresentation == null)
			return;
			 
		ETPairT<IDrawEngine, IDrawEngine> nodes = myPresentation.getEdgeFromAndToDrawEngines();
		
		if(nodes == null)
			return;
		
		IDrawEngine fromEngine = nodes.getParamOne();
		IDrawEngine toEngine = nodes.getParamTwo();

		String fromEngineID = "";		
		if(fromEngine != null) {
			fromEngineID = fromEngine.getDrawEngineID();
		}
		
		String toEngineID = "";
		if(toEngine != null) {
			toEngineID = toEngine.getDrawEngineID();
		}
				
		if( fromEngineID.equals("InterfaceDrawEngine") || toEngineID.equals("InterfaceDrawEngine")) {
			m_lineKind = DrawEngineLineKindEnum.DELK_SOLID;
			m_endArrowKind = DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
		}
		else{
			m_lineKind = DrawEngineLineKindEnum.DELK_DASH;
			m_endArrowKind = DrawEngineArrowheadKindEnum.DEAK_FILLED_WHITE;
		}
	}
	
	public boolean isLollipop() {
		return m_endArrowKind == DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
	}

	protected int getLineKind() {
		return m_lineKind;
	}

	protected int getStartArrowKind() {
		return DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
	}

	protected int getEndArrowKind() {
		return m_endArrowKind;
	}

	public void onContextMenu(IMenuManager manager) {
		// Add the stereotype label pullright
		addStandardLabelsToPullright(StandardLabelKind.SLK_ALL, manager);
		super.onContextMenu(manager);
	}

	public boolean setSensitivityAndCheck(
		String id,
		ContextMenuActionClass pClass) {
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
		return "ImplementationEdgeDrawEngine";
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawengines.ETDrawEngine#getManagerMetaType(int)
	 */
	public String getManagerMetaType(int nManagerKind) {
		String sManager = null;

		if (nManagerKind == MK_LABELMANAGER)
		{
		   sManager = "SimpleStereotypeAndNameLabelManager";
		}

		return sManager;
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initResources()
	 */
	public void initResources()
	{
		this.setLineColor("implementationedgecolor", Color.BLACK);
		super.initResources();
	}
}
