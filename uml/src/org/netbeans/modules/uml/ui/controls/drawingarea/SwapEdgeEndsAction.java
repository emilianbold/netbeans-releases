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



package org.netbeans.modules.uml.ui.controls.drawingarea;

import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IEdgeDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;

/**
 * @author KevinM
 *
 * This action contains the information to swap the ends of an edge.
 */
public class SwapEdgeEndsAction extends DelayedAction implements ISwapEdgeEndsAction {

	protected int m_sourceID = -1;
	protected int m_targetID = -1;
	protected IETEdge m_pEdge = null;

	public SwapEdgeEndsAction(IETEdge pEdge, int newSourceID, int newTargetID) {
		super();
		this.setEdgeToSwap(pEdge);
		this.setNewSourceEndID(newSourceID);
		this.setNewTargetEndID(newTargetID);
	}
	/**
	 * 
	 */
	public SwapEdgeEndsAction() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.ISwapEdgeEndsAction#getNewSourceEndID()
	 */
	public int getNewSourceEndID() {
		return m_sourceID;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.ISwapEdgeEndsAction#setNewSourceEndID()
	 */
	public void setNewSourceEndID(int endID) {
		m_sourceID = endID;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.ISwapEdgeEndsAction#getNewTargetEndID()
	 */
	public int getNewTargetEndID() {
		return m_targetID;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.ISwapEdgeEndsAction#setNewTargetEndID()
	 */
	public void setNewTargetEndID(int endID) {
		m_targetID = endID;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.ISwapEdgeEndsAction#getEdgeToSwap()
	 */
	public IETEdge getEdgeToSwap() {
		return m_pEdge;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.ISwapEdgeEndsAction#setEdgeToSwap(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge)
	 */
	public void setEdgeToSwap(IETEdge pEdge) {
		m_pEdge = pEdge;

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.ISwapEdgeEndsAction#execute()
	 */
	public void execute() {
		try {
			if (this.getEdgeToSwap() != null) {
				IEdgeDrawEngine pDrawEngine = (IEdgeDrawEngine) TypeConversions.getDrawEngine(getEdgeToSwap());
				if (pDrawEngine != null) {
					pDrawEngine.swapEdgeEnds(getNewSourceEndID(), getNewTargetEndID());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDelayedAction#getDescription()
	 */
	public String getDescription() {
		return this.getClass().getName();
	}

}
