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



package org.netbeans.modules.uml.ui.swing.drawingarea;

import com.tomsawyer.drawing.TSConnector;

import org.netbeans.modules.uml.core.metamodel.diagrams.ReconnectEdgeCreateConnectorKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;


public class ReconnectEdgeContext extends EdgeEventContext implements IReconnectEdgeContext {
	// The edge being reconnected
	protected IETEdge m_Edge = null;

	// Indicates the target end of the edge is being reconnected
	protected boolean m_bReconnectTarget = true;

	// The node that is not being modified
	protected IETNode m_AnchoredNode = null;

	// The node that the reconnect end is on
	protected IETNode m_PreConnectNode = null;

	// The proposed end node
	protected IETNode m_ProposedEndNode = null;

	// The coneector to attach to the edge when finished
	protected TSConnector m_AssociatedConnector = null;
	
	// Should the reconnect create a new connector or default to pointing to the center of the node?
	protected int m_ReconnectConnector = ReconnectEdgeCreateConnectorKind.RECCK_DONT_CREATE;

	/**
	 * 
	 */
	public ReconnectEdgeContext() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext#getEdge()
	 */
	public IETEdge getEdge() {
		return m_Edge;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext#setEdge(null)
	 */
	public void setEdge(IETEdge value) {
		m_Edge = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext#getReconnectTarget()
	 */
	public boolean getReconnectTarget() {
		return m_bReconnectTarget;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext#setReconnectTarget(boolean)
	 */
	public void setReconnectTarget(boolean value) {
		m_bReconnectTarget = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext#getAnchoredNode()
	 */
	public IETNode getAnchoredNode() {
		return m_AnchoredNode;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext#setAnchoredNode(null)
	 */
	public void setAnchoredNode(IETNode value) {
		m_AnchoredNode = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext#getPreConnectNode()
	 */
	public IETNode getPreConnectNode() {
		if (m_PreConnectNode != null)
			return m_PreConnectNode;
		else if (this.getEdge() != null)
		{
			if (m_bReconnectTarget)
			{
				return getEdge().getToNode();
			}
			else
				return getEdge().getFromNode();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext#setPreConnectNode(null)
	 */
	public void setPreConnectNode(IETNode value) {
		m_PreConnectNode = value;

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext#getProposedEndNode()
	 */
	public IETNode getProposedEndNode() {
		return m_ProposedEndNode;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext#setProposedEndNode(null)
	 */
	public void setProposedEndNode(IETNode value) {
		m_ProposedEndNode = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext#getAssociatedConnector()
	 */
	public TSConnector getAssociatedConnector() {
		return m_AssociatedConnector;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext#setAssociatedConnector(com.tomsawyer.editor.TSEConnector)
	 */
	public void setAssociatedConnector(TSConnector value) {
		m_AssociatedConnector = value;
	}
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext#getReconnectConnector()
	 */
	public int getReconnectConnector() {
		return m_ReconnectConnector;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext#setReconnectConnector(int)
	 */
	public void setReconnectConnector(int nReconnect)
   {
		m_ReconnectConnector = nReconnect;
	}

}
