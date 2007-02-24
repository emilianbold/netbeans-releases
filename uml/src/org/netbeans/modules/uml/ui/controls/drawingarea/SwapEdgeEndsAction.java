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
