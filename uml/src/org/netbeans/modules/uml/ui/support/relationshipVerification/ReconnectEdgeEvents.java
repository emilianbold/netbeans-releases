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



package org.netbeans.modules.uml.ui.support.relationshipVerification;

import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETPointEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.ReconnectEdgeContext;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;

/**
 * @author KevinM
 *
 */
public class ReconnectEdgeEvents implements IReconnectEdgeEvents {
	
	protected IDrawingAreaEventDispatcher m_dispatcher;
	protected IDiagram m_parentDiagram;
	
	/**
	 * 
	 */
	public ReconnectEdgeEvents(IDrawingAreaEventDispatcher dipatcher, IDiagram pParentDiagram) {
		super();
		setParentDiagram(pParentDiagram);
		m_dispatcher = dipatcher;
	}


	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.relationshipVerification.IReconnectEdgeEvents#getParentDiagram(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram)
	 */
	public IDiagram getParentDiagram() {
		return m_parentDiagram;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.relationshipVerification.IReconnectEdgeEvents#setParentDiagram(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram)
	 */
	public void setParentDiagram(IDiagram pParent) {
		m_parentDiagram = pParent;

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.relationshipVerification.IReconnectEdgeEvents#fireReconnectEdgeStart(org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext)
	 */
	public boolean fireReconnectEdgeStart(IReconnectEdgeContext pContext) {
		if (m_dispatcher != null)
		{
			m_dispatcher.fireDrawingAreaReconnectEdgeStart(getParentDiagram(),pContext, null);
			return pContext.getCancel();
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.relationshipVerification.IReconnectEdgeEvents#fireReconnectEdgeMouseMove(org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext)
	 */
	public boolean fireReconnectEdgeMouseMove(IReconnectEdgeContext pContext) {
		if (m_dispatcher != null)
		{
			m_dispatcher.fireDrawingAreaReconnectEdgeMouseMove(getParentDiagram(),pContext, null);
			return pContext.getCancel();
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.relationshipVerification.IReconnectEdgeEvents#fireReconnectEdgeFinish(org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext)
	 */
	public boolean fireReconnectEdgeFinish(IReconnectEdgeContext pContext) {
		if (m_dispatcher != null)
		{
			m_dispatcher.fireDrawingAreaReconnectEdgeFinish(getParentDiagram(),pContext, null);
			return pContext.getCancel();
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.relationshipVerification.IReconnectEdgeEvents#createReconnectEdgeContext(com.tomsawyer.util.TSConstPoint, org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge, boolean, org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode, org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode, org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode)
	 */
	public IReconnectEdgeContext createReconnectEdgeContext(TSConstPoint point, IETEdge pEdge,
			 boolean bReconnectTarget, IETNode pAnchoredNode, IETNode pPreConnectNode, IETNode pProposedEndNode) {
		IReconnectEdgeContext reconnectContext = new ReconnectEdgeContext();
		reconnectContext.setEdge(pEdge);
		reconnectContext.setReconnectTarget(bReconnectTarget);
		reconnectContext.setAnchoredNode(pAnchoredNode);
		reconnectContext.setLogicalPoint(new ETPointEx(point));

		reconnectContext.setProposedEndNode(pProposedEndNode);
		return reconnectContext;
	}
}
