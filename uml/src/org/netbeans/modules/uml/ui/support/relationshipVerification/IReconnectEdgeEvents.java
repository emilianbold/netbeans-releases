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



package org.netbeans.modules.uml.ui.support.relationshipVerification;

import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;

/**
 * @author KevinM
 * This is a base class the various tools can derive from.  It provides the ability to
 * fire edge creation events.
 */
public interface IReconnectEdgeEvents {
	public IDiagram getParentDiagram();
	/// Set the parent diagram
	public void setParentDiagram(IDiagram pParent);

	public boolean fireReconnectEdgeStart(IReconnectEdgeContext pContext);
	public boolean fireReconnectEdgeMouseMove(IReconnectEdgeContext pContext);
	public boolean fireReconnectEdgeFinish(IReconnectEdgeContext pContext);

	/// Create the context used during the firing of events
	public IReconnectEdgeContext createReconnectEdgeContext(TSConstPoint point, IETEdge pEdge, boolean bReconnectTarget, IETNode pAnchoredNode, IETNode pPreConnectNode, IETNode pProposedEndNode);
}
