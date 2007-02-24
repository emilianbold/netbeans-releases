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


package org.netbeans.modules.uml.ui.support.applicationmanager;

/**
 * @author josephg
 *
 *
 */
public class ConnectedNode implements IConnectedNode {
	protected INodePresentation m_NodeAtOtherEnd = null;
	protected IEdgePresentation m_IntermediateEdge = null;
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IConnectedNode#getIntermediateEdge()
	 */
	public IEdgePresentation getIntermediateEdge() {
		return m_IntermediateEdge;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IConnectedNode#setIntermediateEdge(org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation)
	 */
	public void setIntermediateEdge(IEdgePresentation intermediateEdge) {
		m_IntermediateEdge = intermediateEdge;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IConnectedNode#getNodeAtOtherEnd()
	 */
	public INodePresentation getNodeAtOtherEnd() {
		return m_NodeAtOtherEnd;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IConnectedNode#setNodeAtOtherEnd(org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation)
	 */
	public void setNodeAtOtherEnd(INodePresentation nodePE) {
		m_NodeAtOtherEnd = nodePE;
	}

}

