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



package org.netbeans.modules.uml.ui.support.viewfactorysupport;

import java.util.List;

import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;

/**
 * @author KevinM
 *
 */
public interface IETNode extends IETGraphObject {
	public boolean connected();
	public boolean hasParents();
	public boolean hasChildren();
	
	public List getInEdges();
	public List getOutEdges();
	public IETRect getEdgeBounds();	
	public void moveTo(TSConstPoint pt);
	public ETList<IETLabel> getLabels(boolean includeSelected, boolean includeNonselected);
	public ETList<IETLabel> getLabels();
	public ETList<IETEdge> getEdges(boolean includeInEdges, boolean includeOutEdges);
	public ETList<IETEdge> getEdges();
	public void invalidateEdges();
        
        IETNode createNodeCopy(IDiagram pDiagram, IETPoint pCenterPoint);
}
