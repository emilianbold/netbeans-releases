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



package org.netbeans.modules.uml.ui.swing.drawingarea;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;


/**
 * @author KevinM
 *
 */
public class EdgeMouseMoveContext extends EdgeEventContext implements IEdgeMouseMoveContext {

	protected IETNode m_startNode = null;
	protected IETNode m_nodeUnderMouse  = null;
	protected boolean m_bValid = true;			// If no one implements this we still want to continue.
	/**
	 * 
	 */
	public EdgeMouseMoveContext() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeMouseMoveContext#getStartNode()
	 */
	public IETNode getStartNode() {
		return m_startNode;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeMouseMoveContext#setStartNode(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode)
	 */
	public void setStartNode(IETNode value) {
		m_startNode = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeMouseMoveContext#getNodeUnderMouse()
	 */
	public IETNode getNodeUnderMouse() {
		return m_nodeUnderMouse;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeMouseMoveContext#setNodeUnderMouse(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode)
	 */
	public void setNodeUnderMouse(IETNode value) {
		m_nodeUnderMouse = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeMouseMoveContext#getStartNodeModelElement()
	 */
	public IElement getStartNodeModelElement() {
		return TypeConversions.getElement(getStartNode());
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeMouseMoveContext#getNodeUnderMouseModelElement()
	 */
	public IElement getNodeUnderMouseModelElement() {
		return TypeConversions.getElement(getNodeUnderMouse());
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeMouseMoveContext#getValid()
	 */
	public boolean getValid() {
		return m_bValid;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeMouseMoveContext#setValid(boolean)
	 */
	public void setValid(boolean value) {
		m_bValid = value;
	}
}

