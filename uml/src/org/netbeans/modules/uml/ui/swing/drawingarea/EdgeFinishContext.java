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
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import com.tomsawyer.drawing.TSConnector;
import com.tomsawyer.editor.TSEConnector;

/**
 * @author KevinM
 *
 */
public class EdgeFinishContext extends EdgeEventContext implements IEdgeFinishContext {

	IETNode m_startNode = null;
	IETNode m_finishNode = null;
	TSConnector m_startConnector = null;
	TSConnector m_finishConnector = null;
	
	
	/**
	 * 
	 */
	public EdgeFinishContext() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext#getStartNode()
	 */
	public IETNode getStartNode() {
		return m_startNode;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext#setStartNode(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode)
	 */
	public void setStartNode(IETNode value) {
		m_startNode = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext#getFinishNode()
	 */
	public IETNode getFinishNode() {
		return m_finishNode;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext#setFinishNode(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode)
	 */
	public void setFinishNode(IETNode value) {
		m_finishNode = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext#getStartNodeModelElement()
	 */
	public IElement getStartNodeModelElement() {
		return getModelElement(getStartNode());
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext#getFinishNodeModelElement()
	 */
	public IElement getFinishNodeModelElement() {
		return getModelElement(getFinishNode());
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext#getStartConnector()
	 */
	public TSConnector getStartConnector() {
		return m_startConnector;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext#setStartConnector(com.tomsawyer.editor.TSEConnector)
	 */
	public void setStartConnector(TSConnector value) {
		m_startConnector = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext#getFinishConnector()
	 */
	public TSConnector getFinishConnector() {
		return m_finishConnector;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeFinishContext#setFinishConnector(com.tomsawyer.editor.TSEConnector)
	 */
	public void setFinishConnector(TSConnector value) {
		m_finishConnector = value;
	}
	
	protected IElement getModelElement(IETGraphObject obj)
	{
		return obj != null && obj.getETUI() != null ? obj.getETUI().getModelElement() : null;
	}
}
