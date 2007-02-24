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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

import java.util.List;


/**
 * @author KevinM
 *
 */
public interface IETEdge extends IETGraphObject {
	public ITSGraphObject getFromObject();	// Might be the connector
	public ITSGraphObject getToObject();	// Might be the connector
	public IETNode getFromNode();			
	public IETNode getToNode();
	public List bendPoints();
	public boolean hasBends();
	public ETList<IETLabel> getLabels(boolean includeSelected, boolean includeNonselected);
	public ETList<IETLabel> getLabels();
        
        public IETEdge createEdgeCopy(IDiagram pDiagram, IETPoint pCenter,
            IPresentationElement pNewSourceNode, IPresentationElement pNewTargetNode);
}
