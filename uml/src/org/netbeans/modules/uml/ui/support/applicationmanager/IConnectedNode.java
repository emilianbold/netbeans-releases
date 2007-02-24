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
public interface IConnectedNode {
//	 The edge connecting to the other node
//	HRESULT IntermediateEdge([out, retval] IEdgePresentation* *pVal);
	IEdgePresentation getIntermediateEdge();

//	 The edge connecting to the other node
//	HRESULT IntermediateEdge([in] IEdgePresentation* newVal);
	void setIntermediateEdge(IEdgePresentation intermediateEdge);

//	 The node at the other end
//	HRESULT NodeAtOtherEnd([out, retval] INodePresentation* *pVal);
	INodePresentation getNodeAtOtherEnd();

//	 The node at the other end
//	HRESULT NodeAtOtherEnd([in] INodePresentation* newVal);
	void setNodeAtOtherEnd(INodePresentation nodePE); 
}

